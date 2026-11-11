
package br.com.sistemas.chamados.service;

import br.com.sistemas.chamados.dto.HistoricoChamadoResponse;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.HistoricoChamado;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.HistoricoChamadoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HistoricoChamadoService {

    private final HistoricoChamadoRepository historicoRepository;
    private final ChamadoRepository chamadoRepository;

    public HistoricoChamadoService(
            HistoricoChamadoRepository historicoRepository,
            ChamadoRepository chamadoRepository) {

        this.historicoRepository = historicoRepository;
        this.chamadoRepository = chamadoRepository;
    }

    // REGISTRAR UMA ALTERACAO NO HISTORICO
    @Transactional
    public void registrar(
            Chamado chamado,
            String acao,
            String descricao) {

        HistoricoChamado historico = new HistoricoChamado();

        historico.setChamado(chamado);
        historico.setAcao(acao);
        historico.setDescricao(descricao);

        historicoRepository.save(historico);
    }

    // LISTAR O HISTORICO DE UM CHAMADO
    @Transactional(readOnly = true)
    public List<HistoricoChamadoResponse> listarPorChamado(
            Long chamadoId) {

        if (!chamadoRepository.existsById(chamadoId)) {
            throw new RecursoNaoEncontradoException(
                "Chamado não encontrado com ID: " + chamadoId
            );
        }

        return historicoRepository
                .findByChamadoIdOrderByDataAlteracaoAsc(chamadoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // CONVERTER ENTIDADE PARA DTO
    private HistoricoChamadoResponse toResponse(
            HistoricoChamado historico) {

        return new HistoricoChamadoResponse(
                historico.getId(),
                historico.getAcao(),
                historico.getDescricao(),
                historico.getDataAlteracao(),
                historico.getChamado().getId()
        );
    }
}
