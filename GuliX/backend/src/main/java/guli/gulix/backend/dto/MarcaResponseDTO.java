package guli.gulix.backend.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        description = "Dados retornados de uma marca."
)
public class MarcaResponseDTO {

    @Schema(
            description = "Identificador único da marca",
            example = "1"
    )
    private Integer id;

    @Schema(
            description = "Nome da marca",
            example = "AMD"
    )
    private String nome;
}
