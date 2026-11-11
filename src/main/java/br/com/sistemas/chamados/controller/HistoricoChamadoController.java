
package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.HistoricoChamadoResponse;
import br.com.sistemas.chamados.service.HistoricoChamadoService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chamados/{chamadoId}/historico")
public class HistoricoChamadoController {

    private final HistoricoChamadoService service;

    public HistoricoChamadoController(
            HistoricoChamadoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<HistoricoChamadoResponse>> listar(
            @PathVariable Long chamadoId) {

        return ResponseEntity.ok(
                service.listarPorChamado(chamadoId)
        );
    }
}
