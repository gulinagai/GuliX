package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Menor e maior preço efetivo de venda entre os produtos disponíveis.")
public class FaixaPrecoResponseDTO {

    @Schema(
            description = "Menor preço efetivo de venda entre os produtos com estoque disponível.",
            example = "49.90"
    )
    private BigDecimal menorPreco;

    @Schema(
            description = "Maior preço efetivo de venda entre os produtos com estoque disponível.",
            example = "5999.90"
    )
    private BigDecimal maiorPreco;
}
