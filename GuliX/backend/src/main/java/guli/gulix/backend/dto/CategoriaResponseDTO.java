package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        description = "Dados retornados de uma categoria."
)
public class CategoriaResponseDTO {

    @Schema(
            description = "Identificador único da categoria",
            example = "1"
    )
    private Integer id;

    @Schema(
            description = "Nome da categoria",
            example = "Memória RAM"
    )
    private String nome;
}
