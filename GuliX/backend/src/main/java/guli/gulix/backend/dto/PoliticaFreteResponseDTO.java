package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Dados de uma política de frete")
public record PoliticaFreteResponseDTO(

        @Schema(
                description = "Identificador da política de frete",
                example = "1"
        )
        Integer id,

        @Schema(
                description = "Identificador do estado associado à política",
                example = "35"
        )
        Integer estadoId,

        @Schema(
                description = "Sigla do estado associado à política",
                example = "SP"
        )
        String estadoSigla,

        @Schema(
                description = "Valor base do frete",
                example = "15.00"
        )
        BigDecimal valorBase,

        @Schema(
                description = "Indica se a política de frete está atualmente ativa",
                example = "true"
        )
        Boolean ativo
) {
}
