
package br.com.sistemas.chamados.dto;

import br.com.sistemas.chamados.entity.StatusChamado;
import jakarta.validation.constraints.NotNull;

public record StatusChamadoRequest(
    @NotNull(message = "O status é obrigatório")
    StatusChamado status
) {}
