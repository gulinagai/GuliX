package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Informações de estoque de um produto.")
public class EstoqueResponseDTO {

    @Schema(
            description = "Identificador do produto relacionado ao estoque.",
            example = "1"
    )
    private Integer produtoId;

    @Schema(
            description = "Quantidade total de unidades do produto em estoque.",
            example = "50"
    )
    private Integer estoqueTotal;

    @Schema(
            description = "Quantidade de unidades do produto atualmente reservadas.",
            example = "10"
    )
    private Integer estoqueReservado;

    @Schema(
            description = "Quantidade de unidades disponíveis para venda.",
            example = "40"
    )
    private Integer estoqueDisponivel;
}
