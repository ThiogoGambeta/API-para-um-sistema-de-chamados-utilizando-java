
package br.com.sistemas.chamados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComentarioRequest(

    @NotBlank(message = "O comentário não pode estar vazio")
    @Size(max = 2000, message = "O comentário deve ter no máximo 2000 caracteres")
    String texto,

    @NotBlank(message = "O autor é obrigatório")
    @Size(max = 120, message = "O autor deve ter no máximo 120 caracteres")
    String autor

) {}
