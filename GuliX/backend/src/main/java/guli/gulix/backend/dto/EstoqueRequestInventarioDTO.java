package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(
        description = "Dados necessários para realizar o inventário do estoque."
)
public record EstoqueRequestInventarioDTO(

        @NotNull
        @PositiveOrZero
        @Schema(
                description = "Quantidade atual de unidades identificada no inventário.",
                example = "100",
                minimum = "0"
        )
        Integer quantidade

) {

}
