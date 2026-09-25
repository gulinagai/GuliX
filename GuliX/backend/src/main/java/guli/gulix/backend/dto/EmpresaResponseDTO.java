package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(
        description = "Dados de uma empresa."
)
public record EmpresaResponseDTO(

        @Schema(
                description = "Identificador único da empresa.",
                example = "1"
        )
        Integer id,

        @Schema(
                description = "Razão social da empresa.",
                example = "GuliX Tecnologia LTDA"
        )
        String razaoSocial,

        @Schema(
                description = "Nome fantasia da empresa.",
                example = "GuliX"
        )
        String nomeFantasia,

        @Schema(
                description = "CNPJ da empresa, contendo 14 dígitos.",
                example = "12345678000199",
                minLength = 14,
                maxLength = 14,
                pattern = "\\d{14}"
        )
        String cnpj,

        @Schema(
                description = "Valor do frete cobrado por quilômetro.",
                example = "0.05",
                minimum = "0.00"
        )
        BigDecimal valorPorKmFrete
) {
}