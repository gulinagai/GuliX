package guli.gulix.backend.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Dados necessários para criar uma política de frete")
public record PoliticaFreteCreateDTO(

        @Schema(
                description = "ID do estado ao qual a política de frete será aplicada",
                example = "35"
        )
        @NotNull
        Integer estadoId,

        @Schema(
                description = "Valor base do frete para o estado",
                example = "15.00",
                minimum = "0.01"
        )
        @NotNull
        @DecimalMin("0.01")
        BigDecimal valorBase

) {
}
