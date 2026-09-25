package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados do carrinho de compras do usuário")
public class CarrinhoResponseDTO {

    @Schema(
            description = "Lista de itens presentes no carrinho",
            example = "[{\"id\":1,\"produtoId\":10,\"nomeProduto\":\"Teclado Mecânico RGB\",\"preco\":249.90,\"quantidade\":2}]"
    )
    private List<ItemCarrinhoResponseDTO> itens;

    @Schema(
            description = "Valor total dos itens presentes no carrinho",
            example = "499.80"
    )
    private BigDecimal total;
}
