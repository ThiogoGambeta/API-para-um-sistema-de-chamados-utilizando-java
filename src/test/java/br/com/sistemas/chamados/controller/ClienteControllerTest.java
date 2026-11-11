package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.ClienteResponse;
import br.com.sistemas.chamados.exception.GlobalExceptionHandler;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
@Import(GlobalExceptionHandler.class)
class ClienteControllerTest {
    @Autowired private MockMvc mvc;
    @MockitoBean private ClienteService clienteService;
    @MockitoBean private ChamadoService chamadoService;

    @Test
    void deveCriarCliente() throws Exception {
        when(clienteService.criar(any())).thenReturn(new ClienteResponse(1L, "Ana", "ana@test.local", "47999999999"));
        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Ana\",\"email\":\"ana@test.local\",\"telefone\":\"47999999999\"}"))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/clientes/1"));
    }

    @Test
    void deveValidarEmailInvalido() throws Exception {
        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Ana\",\"email\":\"email-ruim\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar404ParaClienteInexistente() throws Exception {
        when(clienteService.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Cliente 99 não encontrado"));
        mvc.perform(get("/clientes/99")).andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar409ParaEmailDuplicado() throws Exception {
        when(clienteService.criar(any())).thenThrow(new RegraNegocioException("Já existe cliente com o e-mail ana@test.local"));
        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Ana\",\"email\":\"ana@test.local\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void deveListarChamadosDoCliente() throws Exception {
        when(chamadoService.listarPorCliente(1L)).thenReturn(List.of());
        mvc.perform(get("/clientes/1/chamados")).andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
