package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Condições de parcelamento sem juros de um produto.")
public class CondicoesPagamentoResponseDTO {

    @Schema(
            description = "Quantidade máxima de parcelas sem juros.",
            example = "6"
    )
    private Integer quantidadeParcelas;

    @Schema(
            description = "Valor de cada parcela sem juros, calculado sobre o preço efetivo de venda.",
            example = "833.33"
    )
    private BigDecimal valorParcela;
}
