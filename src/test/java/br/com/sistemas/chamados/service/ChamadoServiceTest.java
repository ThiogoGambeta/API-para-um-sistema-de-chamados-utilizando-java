
package br.com.sistemas.chamados.service;

import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChamadoServiceTest {

    @Mock
    private ChamadoRepository chamadoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private TecnicoRepository tecnicoRepository;

    @Mock
    private HistoricoChamadoService historicoService;

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private HistoricoChamadoRepository historicoRepository;

    @InjectMocks
    private ChamadoService chamadoService;

    private Chamado chamado;

    @BeforeEach
    void preparar() {
        chamado = new Chamado();
        chamado.setStatus(StatusChamado.FECHADO);
    }

    @Test
    void naoDeveExcluirChamadoFechado() {

        when(chamadoRepository.findById(1L))
                .thenReturn(Optional.of(chamado));

        RegraNegocioException exception =
                assertThrows(
                        RegraNegocioException.class,
                        () -> chamadoService.excluir(1L)
                );

        assertEquals(
                "Não é possível modificar um chamado fechado",
                exception.getMessage()
        );

        verify(chamadoRepository, never()).delete(any(Chamado.class));
    }


@Test
void deveAlterarStatusDeAbertoParaEmAtendimento() {

    chamado.setStatus(StatusChamado.ABERTO);

    when(chamadoRepository.findById(1L))
            .thenReturn(Optional.of(chamado));

    when(chamadoRepository.save(any(Chamado.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    // O método retorna um DTO, então precisamos dos
    // relacionamentos usados na conversão.
    br.com.sistemas.chamados.entity.Cliente cliente =
            mock(br.com.sistemas.chamados.entity.Cliente.class);

    br.com.sistemas.chamados.entity.Categoria categoria =
            mock(br.com.sistemas.chamados.entity.Categoria.class);

    when(cliente.getId()).thenReturn(1L);
    when(categoria.getId()).thenReturn(1L);

    chamado.setCliente(cliente);
    chamado.setCategoria(categoria);

    chamadoService.alterarStatus(
            1L,
            StatusChamado.EM_ATENDIMENTO
    );

    assertEquals(
            StatusChamado.EM_ATENDIMENTO,
            chamado.getStatus()
    );

    verify(historicoService).registrar(
            chamado,
            "ALTERACAO_STATUS",
            "Status alterado de ABERTO para EM_ATENDIMENTO"
    );
}

@Test
void naoDevePermitirTransicaoInvalidaDeStatus() {

    chamado.setStatus(StatusChamado.ABERTO);

    when(chamadoRepository.findById(1L))
            .thenReturn(Optional.of(chamado));

    assertThrows(
            RegraNegocioException.class,
            () -> chamadoService.alterarStatus(
                    1L,
                    StatusChamado.FECHADO
            )
    );

    verify(chamadoRepository, never())
            .save(any(Chamado.class));

    verifyNoInteractions(historicoService);
}


@Test
void deveAtribuirTecnicoAoChamadoAberto() {

    chamado.setStatus(StatusChamado.ABERTO);

    var cliente = mock(
            br.com.sistemas.chamados.entity.Cliente.class
    );

    var categoria = mock(
            br.com.sistemas.chamados.entity.Categoria.class
    );

    var tecnico = mock(
            br.com.sistemas.chamados.entity.Tecnico.class
    );

    chamado.setCliente(cliente);
    chamado.setCategoria(categoria);

    when(cliente.getId()).thenReturn(1L);
    when(categoria.getId()).thenReturn(1L);
    when(tecnico.getId()).thenReturn(2L);

    when(chamadoRepository.findById(1L))
            .thenReturn(Optional.of(chamado));

    when(tecnicoRepository.findById(2L))
            .thenReturn(Optional.of(tecnico));

    when(chamadoRepository.save(any(Chamado.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    var resposta = chamadoService.atribuirTecnico(1L, 2L);

    assertEquals(2L, resposta.tecnicoId());
    assertSame(tecnico, chamado.getTecnico());

    verify(historicoService).registrar(
            chamado,
            "ATRIBUICAO_TECNICO",
            "Técnico alterado de nenhum para 2"
    );
}

@Test
void naoDeveAtribuirTecnicoAChamadoFechado() {

    chamado.setStatus(StatusChamado.FECHADO);

    when(chamadoRepository.findById(1L))
            .thenReturn(Optional.of(chamado));

    assertThrows(
            RegraNegocioException.class,
            () -> chamadoService.atribuirTecnico(1L, 2L)
    );

    verify(tecnicoRepository, never()).findById(anyLong());
    verify(chamadoRepository, never()).save(any(Chamado.class));
    verifyNoInteractions(historicoService);
}

}
