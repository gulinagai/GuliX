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
@Schema(description = "Dados necessários para atualizar completamente um produto.")
public class ProdutoUpdateDTO {

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 100,
            message = "O nome deve possuir entre 3 e 100 caracteres")
    @Schema(
            description = "Nome do produto.",
            example = "Placa de Vídeo RX 6650 XT"
    )
    private String nome;

    @NotBlank(message = "O resumo é obrigatório")
    @Size(min = 3, max = 255,
            message = "O resumo deve possuir entre 3 e 255 caracteres")
    @Schema(
            description = "Resumo das características do produto.",
            example = "Placa de vídeo AMD Radeon RX 6650 XT com 8 GB de memória."
    )
    private String resumo;

    @NotNull(message = "O preço é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço deve ser maior que zero")
    @Schema(
            description = "Preço atual do produto.",
            example = "1899.90"
    )
    private BigDecimal preco;

    @NotNull(message = "A categoria é obrigatória")
    @Schema(
            description = "Identificador da categoria à qual o produto pertence.",
            example = "3"
    )
    private Integer categoriaId;

    @NotBlank(message = "A URL da imagem é obrigatória.")
    @Pattern(
            regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*\\.jpg$",
            message = "A URL da imagem deve seguir o formato: palavras-separadas-por-hifen.jpg"
    )
    @Schema(
            description = "Nome do arquivo de imagem do produto.",
            example = "rx-6650-xt.jpg"
    )
    private String imagemURL;

    @NotNull(message = "A marca é obrigatória")
    @Schema(
            description = "Identificador da marca do produto.",
            example = "5"
    )
    private Integer marcaId;

    @NotNull(message = "O destaque é obrigatório")
    @Schema(
            description = "Indica se o produto deve ser exibido como destaque.",
            example = "true"
    )
    private Boolean destaque;

    @DecimalMin(value = "0.0", message = "O desconto não pode ser negativo")
    @Schema(
            description = "Valor de desconto aplicado ao produto, caso aplicado.",
            example = "100.00"
    )
    private BigDecimal desconto;

}
