package br.com.sistemas.chamados.service;

import br.com.sistemas.chamados.dto.ChamadoRequest;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.entity.*;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.repository.*;
import br.com.sistemas.chamados.exception.RegraNegocioException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final ClienteRepository clienteRepository;
    private final CategoriaRepository categoriaRepository;
    private final TecnicoRepository tecnicoRepository;

    public ChamadoService(
            ChamadoRepository chamadoRepository,
            ClienteRepository clienteRepository,
            CategoriaRepository categoriaRepository,
            TecnicoRepository tecnicoRepository) {

        this.chamadoRepository = chamadoRepository;
        this.clienteRepository = clienteRepository;
        this.categoriaRepository = categoriaRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    @Transactional
    public ChamadoResponse criar(ChamadoRequest request) {

        Chamado chamado = new Chamado();

        preencherDados(chamado, request);

        chamado.setStatus(StatusChamado.ABERTO);

        return toResponse(chamadoRepository.save(chamado));
    }

    @Transactional(readOnly = true)
    public List<ChamadoResponse> listar() {
        return chamadoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChamadoResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    
@Transactional
public ChamadoResponse atualizar(Long id, ChamadoRequest request) {

    Chamado chamado = buscarEntidade(id);

    if (chamado.getStatus() == StatusChamado.FECHADO) {
        throw new RegraNegocioException(
            "Não é possível atualizar um chamado fechado"
        );
    }

    preencherDados(chamado, request);

    return toResponse(chamadoRepository.save(chamado));
}


    
@Transactional
public void excluir(Long id) {

    Chamado chamado = buscarEntidade(id);

    if (chamado.getStatus() == StatusChamado.FECHADO) {
        throw new RegraNegocioException(
            "Não é possível excluir um chamado fechado"
        );
    }

    chamadoRepository.delete(chamado);
}


    private void preencherDados(Chamado chamado, ChamadoRequest request) {

        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cliente não encontrado com ID: " + request.clienteId()
                ));

        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria não encontrada com ID: " + request.categoriaId()
                ));

        Tecnico tecnico = null;

        if (request.tecnicoId() != null) {
            tecnico = tecnicoRepository.findById(request.tecnicoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Técnico não encontrado com ID: " + request.tecnicoId()
                    ));
        }

        chamado.setTitulo(request.titulo());
        chamado.setDescricao(request.descricao());
        chamado.setPrioridade(request.prioridade());
        chamado.setCliente(cliente);
        chamado.setCategoria(categoria);
        chamado.setTecnico(tecnico);
    }


@Transactional
public ChamadoResponse alterarStatus(Long id, StatusChamado novoStatus) {

    Chamado chamado = buscarEntidade(id);

    if (chamado.getStatus() == StatusChamado.FECHADO) {
        throw new RegraNegocioException(
            "Não é possível alterar um chamado fechado"
        );
    }

    if (novoStatus == null) {
        throw new RegraNegocioException(
            "O novo status é obrigatório"
        );
    }

    if (chamado.getStatus() == novoStatus) {
        throw new RegraNegocioException(
            "O chamado já possui este status"
        );
    }

    if (novoStatus == StatusChamado.FECHADO) {
        chamado.setDataFechamento(java.time.LocalDateTime.now());
    }

    chamado.setStatus(novoStatus);

    return toResponse(chamadoRepository.save(chamado));
}


@Transactional
public ChamadoResponse atribuirTecnico(Long chamadoId, Long tecnicoId) {

    Chamado chamado = buscarEntidade(chamadoId);

    if (chamado.getStatus() == StatusChamado.FECHADO) {
        throw new RegraNegocioException(
            "Não é possível atribuir um técnico a um chamado fechado"
        );
    }

    Tecnico tecnico = tecnicoRepository.findById(tecnicoId)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Técnico não encontrado com ID: " + tecnicoId
            ));

    chamado.setTecnico(tecnico);

    return toResponse(chamadoRepository.save(chamado));
}


    private Chamado buscarEntidade(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Chamado não encontrado com ID: " + id
                ));
    }

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
