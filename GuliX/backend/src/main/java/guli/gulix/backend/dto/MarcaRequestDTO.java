package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        description = "Dados necessários para criar ou atualizar uma marca."
)
public class MarcaRequestDTO {
    @NotBlank(message = "O nome da marca é obrigatório")
    @Size(max = 100, message = "O nome da marca deve possuir no máximo 100 caracteres")
    @Schema(
            description = "Nome da marca",
            example = "AMD",
            maxLength = 100
    )
    private String nome;
}
