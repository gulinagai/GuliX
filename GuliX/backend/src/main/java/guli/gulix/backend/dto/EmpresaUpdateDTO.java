package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(
        description = "Dados para atualização de uma empresa. Os campos são opcionais e somente os campos informados serão alterados."
)
public record EmpresaUpdateDTO(

        @Size(max = 150)
        @Schema(
                description = "Nova razão social da empresa.",
                example = "GuliX Tecnologia LTDA",
                maxLength = 150
        )
        String razaoSocial,

        @Size(max = 100)
        @Schema(
                description = "Novo nome fantasia da empresa.",
                example = "GuliX",
                maxLength = 100
        )
        String nomeFantasia,

        @Pattern(
                regexp = "\\d{14}",
                message = "CNPJ deve conter 14 dígitos"
        )
        @Schema(
                description = "Novo CNPJ da empresa. Deve conter exatamente 14 dígitos, sem pontuação.",
                example = "12345678000199",
                minLength = 14,
                maxLength = 14,
                pattern = "\\d{14}"
        )
        String cnpj,

        @DecimalMin(
                value = "0.00",
                inclusive = true,
                message = "Valor por km deve ser maior ou igual a zero"
        )
        @Schema(
                description = "Novo valor do frete cobrado por quilômetro.",
                example = "2.50",
                minimum = "0.00"
        )
        BigDecimal valorPorKmFrete
) {
}