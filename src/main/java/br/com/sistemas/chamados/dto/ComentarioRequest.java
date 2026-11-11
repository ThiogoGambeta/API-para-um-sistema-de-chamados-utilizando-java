
package br.com.sistemas.chamados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComentarioRequest(

    @NotBlank(message = "O comentário não pode estar vazio")
    @Size(max = 2000, message = "O comentário deve ter no máximo 2000 caracteres")
    String texto

) {}
