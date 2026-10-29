package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para destacar um produto e definir o percentual de desconto.")
public class ProdutoDestaqueDTO {

    @NotNull(message = "O desconto é obrigatório")
    @DecimalMin(value = "0.00", inclusive = true, message = "O desconto não pode ser negativo")
    @DecimalMax(value = "100.00", inclusive = true, message = "O desconto não pode ser superior a 100%")
    @Digits(integer = 3, fraction = 2, message = "O desconto deve possuir no máximo 2 casas decimais")
    @Schema(
            description = "Percentual de desconto do produto em destaque. Use 0.00 para destacar sem desconto.",
            example = "10.00"
    )
    private BigDecimal desconto;
}
