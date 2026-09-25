package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados necessários para atualizar a quantidade de um item do carrinho")
public class ItemCarrinhoUpdateDTO {

    @Schema(
            description = "Nova quantidade do produto no carrinho",
            example = "3",
            minimum = "1"
    )
    @NotNull(message = "A quantidade é obrigatória")
    @Positive(message = "A quantidade deve ser maior que zero")
    private Integer quantidade;

}
