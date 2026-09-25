package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados de um item presente no carrinho")
public class ItemCarrinhoResponseDTO {

    @Schema(
            description = "Identificador do item do carrinho",
            example = "1"
    )
    private Integer id;

    @Schema(
            description = "Identificador do produto",
            example = "10"
    )
    private Integer produtoId;

    @Schema(
            description = "Nome do produto",
            example = "Teclado Mecânico RGB"
    )
    private String nomeProduto;

    @Schema(
            description = "Preço unitário do produto",
            example = "249.90"
    )
    private BigDecimal preco;

    @Schema(
            description = "Quantidade do produto no carrinho",
            example = "2"
    )
    private Integer quantidade;


}


