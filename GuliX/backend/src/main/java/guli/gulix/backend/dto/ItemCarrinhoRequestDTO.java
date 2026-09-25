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
@Schema(description = "Dados necessários para adicionar um produto ao carrinho")
public class ItemCarrinhoRequestDTO {

    @Schema(
            description = "Identificador do produto que será adicionado ao carrinho",
            example = "10",
            minimum = "1"
    )
    @NotNull(message = "O produto é obrigatório")
    @Positive(message = "O ID do produto deve ser positivo")
    private Integer produtoId;

    @Schema(
            description = "Quantidade do produto a ser adicionada ao carrinho",
            example = "2",
            minimum = "1"
    )
    @NotNull(message = "A quantidade é obrigatória")
    @Positive(message = "A quantidade deve ser maior que zero")
    private Integer quantidade;
}
