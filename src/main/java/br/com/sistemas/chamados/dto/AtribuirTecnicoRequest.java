
package br.com.sistemas.chamados.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AtribuirTecnicoRequest(

    @NotNull(message = "O técnico é obrigatório")
    @Positive(message = "O ID do técnico deve ser positivo")
    Long tecnicoId

) {}
