
package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.AtribuirTecnicoRequest;
import br.com.sistemas.chamados.dto.ChamadoRequest;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.dto.StatusChamadoRequest;
import br.com.sistemas.chamados.service.ChamadoService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    private final ChamadoService service;

    public ChamadoController(ChamadoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ChamadoResponse> criar(
            @Valid @RequestBody ChamadoRequest request) {

        ChamadoResponse response = service.criar(request);

        return ResponseEntity
                .created(URI.create("/chamados/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ChamadoResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChamadoResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChamadoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ChamadoRequest request) {

        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ChamadoResponse> alterarStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusChamadoRequest request) {

        return ResponseEntity.ok(
                service.alterarStatus(id, request.status())
        );
    }

    @PatchMapping("/{id}/tecnico")
    public ResponseEntity<ChamadoResponse> atribuirTecnico(
            @PathVariable Long id,
            @Valid @RequestBody AtribuirTecnicoRequest request) {

        return ResponseEntity.ok(
                service.atribuirTecnico(id, request.tecnicoId())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
