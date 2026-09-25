package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados de um produto retornados pela API.")
public class ProdutoResponseDTO {

    @Schema( description = "Identificador único do produto.", example = "1" )
    private Integer id;

    @Schema( description = "Nome do produto.", example = "Placa de Vídeo RX 6650 XT" )
    private String nome;

    @Schema( description = "Resumo das características do produto.", example = "Placa de vídeo AMD Radeon RX 6650 XT com 8 GB de memória." )
    private String resumo;

    @Schema( description = "Preço atual do produto.", example = "1899.90" )
    private BigDecimal preco;

    @Schema( description = "Informações de estoque do produto." )
    private EstoqueResponseDTO estoque;

    @Schema( description = "Indica se o produto está marcado como destaque.", example = "true" )
    private Boolean destaque;

    @Schema( description = "Nome do arquivo ou URL da imagem do produto.", example = "rx-6650-xt.jpg" )
    private String imagemURL;

    @Schema( description = "Valor de desconto aplicado ao produto.", example = "100.00" )
    private BigDecimal desconto;

    @Schema( description = "Identificador da categoria à qual o produto pertence.", example = "3" )
    private Integer categoriaId;

    @Schema( description = "Identificador da marca do produto.", example = "5" )
    private Integer marcaId;

    @Schema( description = "Data e hora em que o produto foi cadastrado.", example = "2026-09-13T11:30:00" )
    private LocalDateTime criadoEm;

    @Schema( description = "Data e hora da última atualização do produto.", example = "2026-09-13T12:15:00" )
    private LocalDateTime atualizadoEm;
}
