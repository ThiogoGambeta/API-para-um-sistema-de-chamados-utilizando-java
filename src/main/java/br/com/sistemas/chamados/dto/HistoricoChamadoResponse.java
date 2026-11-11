
package br.com.sistemas.chamados.dto;

import java.time.LocalDateTime;

public record HistoricoChamadoResponse(
    Long id,
    String acao,
    String descricao,
    LocalDateTime dataAlteracao,
    Long chamadoId
) {}
