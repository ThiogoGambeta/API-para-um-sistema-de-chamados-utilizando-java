
package br.com.sistemas.chamados.service;

import br.com.sistemas.chamados.dto.ComentarioRequest;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Comentario;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.ComentarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComentarioServiceTest {

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private ChamadoRepository chamadoRepository;

    @InjectMocks
    private ComentarioService comentarioService;

    @Test
    void deveAdicionarComentarioEmChamadoExistente() {

        Chamado chamado = mock(Chamado.class);
        when(chamado.getId()).thenReturn(1L);

        when(chamadoRepository.findById(1L))
                .thenReturn(Optional.of(chamado));

        when(comentarioRepository.save(any(Comentario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var resposta = comentarioService.adicionar(
                1L,
                new ComentarioRequest("Técnico iniciou o atendimento")
        );

        assertEquals(
                "Técnico iniciou o atendimento",
                resposta.texto()
        );

        assertEquals(1L, resposta.chamadoId());

        verify(comentarioRepository).save(any(Comentario.class));
    }

    @Test
    void naoDeveAdicionarComentarioEmChamadoInexistente() {

        when(chamadoRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> comentarioService.adicionar(
                        99L,
                        new ComentarioRequest("Teste")
                )
        );

        verify(comentarioRepository, never())
                .save(any(Comentario.class));
    }

    @Test
    void deveListarComentariosDoChamado() {

        Chamado chamado = mock(Chamado.class);
        when(chamado.getId()).thenReturn(1L);

        Comentario comentario = new Comentario();
        comentario.setTexto("Chamado em análise");
        comentario.setChamado(chamado);

        when(chamadoRepository.findById(1L))
                .thenReturn(Optional.of(chamado));

        when(comentarioRepository
                .findByChamadoIdOrderByDataCriacaoAsc(1L))
                .thenReturn(List.of(comentario));

        var comentarios = comentarioService.listarPorChamado(1L);

        assertEquals(1, comentarios.size());
        assertEquals("Chamado em análise", comentarios.get(0).texto());
        assertEquals(1L, comentarios.get(0).chamadoId());
    }
}
