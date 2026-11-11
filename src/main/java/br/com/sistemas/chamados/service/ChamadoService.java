
package br.com.sistemas.chamados.service;

import br.com.sistemas.chamados.dto.ChamadoRequest;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.entity.*;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.*;
import br.com.sistemas.chamados.specification.ChamadoSpecification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final ClienteRepository clienteRepository;
    private final CategoriaRepository categoriaRepository;
    private final TecnicoRepository tecnicoRepository;
    private final HistoricoChamadoService historicoService;
    private final ComentarioRepository comentarioRepository;
    private final HistoricoChamadoRepository historicoRepository;

    public ChamadoService(
            ChamadoRepository chamadoRepository,
            ClienteRepository clienteRepository,
            CategoriaRepository categoriaRepository,
            TecnicoRepository tecnicoRepository,
            HistoricoChamadoService historicoService,
            ComentarioRepository comentarioRepository,
            HistoricoChamadoRepository historicoRepository) {

        this.chamadoRepository = chamadoRepository;
        this.clienteRepository = clienteRepository;
        this.categoriaRepository = categoriaRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.historicoService = historicoService;
        this.comentarioRepository = comentarioRepository;
        this.historicoRepository = historicoRepository;
    }

    // CADASTRAR CHAMADO
    @Transactional
    public ChamadoResponse criar(ChamadoRequest request) {

        Chamado chamado = new Chamado();

        preencherDados(chamado, request);
        chamado.setStatus(StatusChamado.ABERTO);

        Chamado salvo = chamadoRepository.save(chamado);

        historicoService.registrar(
                salvo,
                "CRIACAO",
                "Chamado criado com status ABERTO"
        );

        return toResponse(salvo);
    }

    // LISTAR TODOS OS CHAMADOS
    @Transactional(readOnly = true)
    public List<ChamadoResponse> listar() {

        return chamadoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // LISTAR COM FILTROS E PAGINACAO
    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listarComFiltros(
            StatusChamado status,
            Prioridade prioridade,
            Long clienteId,
            int pagina,
            int tamanho) {

        if (pagina < 0) {
            throw new RegraNegocioException(
                    "A página não pode ser negativa"
            );
        }

        if (tamanho < 1 || tamanho > 100) {
            throw new RegraNegocioException(
                    "O tamanho da página deve estar entre 1 e 100"
            );
        }

        Pageable pageable = PageRequest.of(
                pagina,
                tamanho,
                Sort.by(Sort.Direction.DESC, "dataAbertura")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Specification<Chamado> filtros = Specification.allOf(
                ChamadoSpecification.comStatus(status),
                ChamadoSpecification.comPrioridade(prioridade),
                ChamadoSpecification.comCliente(clienteId)
        );

        return chamadoRepository
                .findAll(filtros, pageable)
                .map(this::toResponse);
    }

    // BUSCAR CHAMADO POR ID
    @Transactional(readOnly = true)
    public ChamadoResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    // ATUALIZAR CHAMADO
    @Transactional
    public ChamadoResponse atualizar(
            Long id,
            ChamadoRequest request) {

        Chamado chamado = buscarEntidade(id);

        validarChamadoNaoFechado(chamado);
        preencherDados(chamado, request);

        Chamado salvo = chamadoRepository.save(chamado);

        historicoService.registrar(
                salvo,
                "ATUALIZACAO",
                "Dados do chamado atualizados"
        );

        return toResponse(salvo);
    }

    // EXCLUIR CHAMADO COM PROTECAO DE INTEGRIDADE
    @Transactional
    public void excluir(Long id) {

        Chamado chamado = buscarEntidade(id);

        validarChamadoNaoFechado(chamado);

        if (comentarioRepository.existsByChamadoId(id)) {
            throw new RegraNegocioException(
                    "Não é possível excluir um chamado que possui comentários"
            );
        }

        if (historicoRepository.existsByChamadoId(id)) {
            throw new RegraNegocioException(
                    "Não é possível excluir um chamado que possui histórico"
            );
        }

        chamadoRepository.delete(chamado);
    }

    // PREENCHER DADOS E VALIDAR RELACIONAMENTOS
    private void preencherDados(
            Chamado chamado,
            ChamadoRequest request) {

        Cliente cliente = clienteRepository
                .findById(request.clienteId())
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Cliente não encontrado com ID: "
                                        + request.clienteId()
                        )
                );

        Categoria categoria = categoriaRepository
                .findById(request.categoriaId())
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Categoria não encontrada com ID: "
                                        + request.categoriaId()
                        )
                );

        Tecnico tecnico = null;

        if (request.tecnicoId() != null) {
            tecnico = tecnicoRepository
                    .findById(request.tecnicoId())
                    .orElseThrow(() ->
                            new RecursoNaoEncontradoException(
                                    "Técnico não encontrado com ID: "
                                            + request.tecnicoId()
                            )
                    );
        }

        chamado.setTitulo(request.titulo());
        chamado.setDescricao(request.descricao());
        chamado.setPrioridade(request.prioridade());
        chamado.setCliente(cliente);
        chamado.setCategoria(categoria);
        chamado.setTecnico(tecnico);
    }

    // ALTERAR STATUS DO CHAMADO
    @Transactional
    public ChamadoResponse alterarStatus(
            Long id,
            StatusChamado novoStatus) {

        Chamado chamado = buscarEntidade(id);

        validarChamadoNaoFechado(chamado);

        if (novoStatus == null) {
            throw new RegraNegocioException(
                    "O novo status é obrigatório"
            );
        }

        StatusChamado statusAnterior = chamado.getStatus();

        if (statusAnterior == novoStatus) {
            throw new RegraNegocioException(
                    "O chamado já possui este status"
            );
        }

        validarTransicao(statusAnterior, novoStatus);

        if (novoStatus == StatusChamado.FECHADO) {
            chamado.setDataFechamento(LocalDateTime.now());
        }

        chamado.setStatus(novoStatus);

        Chamado salvo = chamadoRepository.save(chamado);

        historicoService.registrar(
                salvo,
                novoStatus == StatusChamado.FECHADO
                        ? "FECHAMENTO"
                        : "ALTERACAO_STATUS",
                "Status alterado de "
                        + statusAnterior
                        + " para "
                        + novoStatus
        );

        return toResponse(salvo);
    }

    // ATRIBUIR TECNICO AO CHAMADO
    @Transactional
    public ChamadoResponse atribuirTecnico(
            Long chamadoId,
            Long tecnicoId) {

        Chamado chamado = buscarEntidade(chamadoId);

        validarChamadoNaoFechado(chamado);

        Tecnico tecnico = tecnicoRepository
                .findById(tecnicoId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Técnico não encontrado com ID: "
                                        + tecnicoId
                        )
                );

        Long tecnicoAnteriorId =
                chamado.getTecnico() != null
                        ? chamado.getTecnico().getId()
                        : null;

        chamado.setTecnico(tecnico);

        Chamado salvo = chamadoRepository.save(chamado);

        historicoService.registrar(
                salvo,
                "ATRIBUICAO_TECNICO",
                "Técnico alterado de "
                        + (tecnicoAnteriorId == null
                                ? "nenhum"
                                : tecnicoAnteriorId)
                        + " para "
                        + tecnicoId
        );

        return toResponse(salvo);
    }

    // BLOQUEAR ALTERACOES EM CHAMADOS FECHADOS
    private void validarChamadoNaoFechado(Chamado chamado) {

        if (chamado.getStatus() == StatusChamado.FECHADO) {
            throw new RegraNegocioException(
                    "Não é possível modificar um chamado fechado"
            );
        }
    }

    // VALIDAR TRANSICOES DE STATUS
    private void validarTransicao(
            StatusChamado atual,
            StatusChamado novo) {

        boolean permitida = switch (atual) {

            case ABERTO ->
                    novo == StatusChamado.EM_ATENDIMENTO;

            case EM_ATENDIMENTO ->
                    novo == StatusChamado.AGUARDANDO_CLIENTE
                            || novo == StatusChamado.FECHADO;

            case AGUARDANDO_CLIENTE ->
                    novo == StatusChamado.EM_ATENDIMENTO
                            || novo == StatusChamado.FECHADO;

            case FECHADO -> false;
        };

        if (!permitida) {
            throw new RegraNegocioException(
                    "Transição de status não permitida: "
                            + atual + " -> " + novo
            );
        }
    }

    // BUSCAR ENTIDADE
    private Chamado buscarEntidade(Long id) {

        return chamadoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Chamado não encontrado com ID: " + id
                        )
                );
    }

    // CONVERTER ENTIDADE PARA RESPONSE
    private ChamadoResponse toResponse(Chamado chamado) {

        return new ChamadoResponse(
                chamado.getId(),
                chamado.getTitulo(),
                chamado.getDescricao(),
                chamado.getDataAbertura(),
                chamado.getDataFechamento(),
                chamado.getPrioridade(),
                chamado.getStatus(),
                chamado.getCliente().getId(),
                chamado.getCategoria().getId(),
                chamado.getTecnico() != null
                        ? chamado.getTecnico().getId()
                        : null
        );
    }
}
