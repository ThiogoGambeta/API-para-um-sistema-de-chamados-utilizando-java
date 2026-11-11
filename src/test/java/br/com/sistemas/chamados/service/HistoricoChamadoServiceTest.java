
package br.com.sistemas.chamados.service;

import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.HistoricoChamado;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.HistoricoChamadoRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoricoChamadoServiceTest {

    @Mock
    private HistoricoChamadoRepository historicoRepository;

    @Mock
    private ChamadoRepository chamadoRepository;

    @InjectMocks
    private HistoricoChamadoService historicoService;

    @Test
    void deveRegistrarHistoricoDoChamado() {

        Chamado chamado = new Chamado();

        historicoService.registrar(
                chamado,
                "ALTERACAO_STATUS",
                "Status alterado de ABERTO para EM_ATENDIMENTO"
        );

        ArgumentCaptor<HistoricoChamado> captor =
                ArgumentCaptor.forClass(HistoricoChamado.class);

        verify(historicoRepository).save(captor.capture());

        HistoricoChamado historico = captor.getValue();

        assertSame(chamado, historico.getChamado());
        assertEquals("ALTERACAO_STATUS", historico.getAcao());
        assertEquals(
                "Status alterado de ABERTO para EM_ATENDIMENTO",
                historico.getDescricao()
        );
    }

    @Test
    void deveListarHistoricoDeChamadoExistente() {

        Chamado chamado = mock(Chamado.class);
        when(chamado.getId()).thenReturn(1L);

        HistoricoChamado historico = new HistoricoChamado();
        historico.setChamado(chamado);
        historico.setAcao("CRIACAO");
        historico.setDescricao("Chamado criado");

        when(chamadoRepository.existsById(1L)).thenReturn(true);

        when(historicoRepository
                .findByChamadoIdOrderByDataAlteracaoAsc(1L))
                .thenReturn(List.of(historico));

        var resultado = historicoService.listarPorChamado(1L);

        assertEquals(1, resultado.size());
        assertEquals("CRIACAO", resultado.get(0).acao());
        assertEquals("Chamado criado", resultado.get(0).descricao());
        assertEquals(1L, resultado.get(0).chamadoId());
    }

    @Test
    void naoDeveListarHistoricoDeChamadoInexistente() {

        when(chamadoRepository.existsById(99L))
                .thenReturn(false);

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> historicoService.listarPorChamado(99L)
        );

        verify(historicoRepository, never())
                .findByChamadoIdOrderByDataAlteracaoAsc(anyLong());
    }
}
