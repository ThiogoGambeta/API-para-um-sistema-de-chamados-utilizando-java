
package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.ComentarioRequest;
import br.com.sistemas.chamados.dto.ComentarioResponse;
import br.com.sistemas.chamados.service.ComentarioService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/chamados/{chamadoId}/comentarios")
public class ComentarioController {

    private final ComentarioService service;

    public ComentarioController(ComentarioService service) {
        this.service = service;
    }

    // ADICIONAR COMENTARIO
    @PostMapping
    public ResponseEntity<ComentarioResponse> adicionar(
            @PathVariable Long chamadoId,
            @Valid @RequestBody ComentarioRequest request) {

        ComentarioResponse response = service.adicionar(
                chamadoId,
                request
        );

        return ResponseEntity
                .created(URI.create(
                        "/chamados/" + chamadoId
                        + "/comentarios/" + response.id()
                ))
                .body(response);
    }

    // LISTAR COMENTARIOS DO CHAMADO
    @GetMapping
    public ResponseEntity<List<ComentarioResponse>> listar(
            @PathVariable Long chamadoId) {

        return ResponseEntity.ok(
                service.listarPorChamado(chamadoId)
        );
    }
}
