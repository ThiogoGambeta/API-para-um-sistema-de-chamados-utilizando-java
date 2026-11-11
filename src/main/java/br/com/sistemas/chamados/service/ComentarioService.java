
package br.com.sistemas.chamados.service;

import br.com.sistemas.chamados.dto.ComentarioRequest;
import br.com.sistemas.chamados.dto.ComentarioResponse;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Comentario;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.ComentarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ChamadoRepository chamadoRepository;

    public ComentarioService(
            ComentarioRepository comentarioRepository,
            ChamadoRepository chamadoRepository) {

        this.comentarioRepository = comentarioRepository;
        this.chamadoRepository = chamadoRepository;
    }

    // ADICIONAR COMENTARIO
    @Transactional
    public ComentarioResponse adicionar(
            Long chamadoId,
            ComentarioRequest request) {

        Chamado chamado = buscarChamado(chamadoId);

        Comentario comentario = new Comentario();

        comentario.setTexto(request.texto());
        comentario.setAutor(request.autor());
        comentario.setChamado(chamado);

        Comentario salvo = comentarioRepository.save(comentario);

        return toResponse(salvo);
    }

    // LISTAR COMENTARIOS DE UM CHAMADO
    @Transactional(readOnly = true)
    public List<ComentarioResponse> listarPorChamado(Long chamadoId) {

        buscarChamado(chamadoId);

        return comentarioRepository
                .findByChamadoIdOrderByDataCriacaoAsc(chamadoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // VALIDAR SE O CHAMADO EXISTE
    private Chamado buscarChamado(Long id) {

        return chamadoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Chamado não encontrado com ID: " + id
                        )
                );
    }

    // CONVERTER ENTIDADE PARA DTO
    private ComentarioResponse toResponse(Comentario comentario) {

        return new ComentarioResponse(
                comentario.getId(),
                comentario.getTexto(),
                comentario.getAutor(),
                comentario.getDataCriacao(),
                comentario.getChamado().getId()
        );
    }
}
