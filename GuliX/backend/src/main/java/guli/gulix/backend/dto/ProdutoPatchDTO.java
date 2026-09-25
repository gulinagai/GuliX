package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para atualização parcial de um produto.")
public class ProdutoPatchDTO {

    @Size(min = 3, max = 100,
            message = "O nome deve possuir entre 3 e 100 caracteres")
    @Schema(
            description = "Novo nome do produto, caso seja necessário alterá-lo.",
            example = "Placa de Vídeo RX 6650 XT"
    )
    private String nome;

    @Size(min = 3, max = 255,
            message = "O resumo deve possuir entre 3 e 255 caracteres")
    @Schema(
            description = "Novo resumo das características do produto.",
            example = "Placa de vídeo AMD Radeon RX 6650 XT com 8 GB de memória."
    )
    private String resumo;

    @DecimalMin(value = "0.0", inclusive = false, message = "O preço deve ser maior que zero")
    @Schema(
            description = "Novo preço do produto.",
            example = "1899.90"
    )
    private BigDecimal preco;

    @Schema(
            description = "Identificador da nova categoria do produto.",
            example = "3"
    )
    private Integer categoriaId;

    @Pattern(
            regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*\\.jpg$",
            message = "A URL da imagem deve seguir o formato: palavras-separadas-por-hifen.jpg"
    )
    @Schema(
            description = "Novo nome do arquivo de imagem do produto.",
            example = "rx-6650-xt.jpg"
    )
    private String imagemURL;

    @Schema(
            description = "Identificador da nova marca do produto.",
            example = "5"
    )
    private Integer marcaId;

    @Schema(
            description = "Define se o produto deve ser exibido como destaque.",
            example = "true"
    )
    private Boolean destaque;

    @Schema(
            description = "Novo valor de desconto aplicado ao produto.",
            example = "100.00"
    )
    @DecimalMin(value = "0.0", message = "O desconto não pode ser negativo")
    private BigDecimal desconto;

}
