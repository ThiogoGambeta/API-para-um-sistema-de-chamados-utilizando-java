
package br.com.sistemas.chamados.controller;

import br.com.sistemas.chamados.dto.AtribuirTecnicoRequest;
import br.com.sistemas.chamados.dto.ChamadoRequest;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.dto.StatusChamadoRequest;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.service.ChamadoService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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

    // LISTAR COM FILTROS E PAGINACAO
    @GetMapping
    public ResponseEntity<Page<ChamadoResponse>> listar(
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {

        return ResponseEntity.ok(
                service.listarComFiltros(
                        status,
                        prioridade,
                        clienteId,
                        pagina,
                        tamanho
                )
        );
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
