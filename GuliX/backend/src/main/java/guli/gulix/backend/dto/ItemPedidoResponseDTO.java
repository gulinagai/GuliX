package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados de um item pertencente a um pedido")
public class ItemPedidoResponseDTO {

    @Schema(
            description = "Identificador do item do pedido",
            example = "1"
    )
    private Long id;

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
            description = "Quantidade do produto no pedido",
            example = "2"
    )
    private Integer quantidade;

    @Schema(
            description = "Preço unitário do produto no momento do pedido",
            example = "249.90"
    )
    private BigDecimal precoUnitario;

    @Schema(
            description = "Valor total do item considerando sua quantidade",
            example = "499.80"
    )
    private BigDecimal subtotal;
}
