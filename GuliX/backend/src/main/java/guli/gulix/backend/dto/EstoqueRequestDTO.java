package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(
        description = "Dados necessários para realizar movimentação de estoque."
)
public record EstoqueRequestDTO(

        @NotNull
        @Positive
        @Schema(
                description = "Quantidade de unidades a ser movimentada no estoque.",
                example = "50",
                minimum = "1"
        )
        Integer quantidade
) {
}
