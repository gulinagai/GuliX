package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados necessários para cadastrar um novo estado.")
public record EstadoCreateDTO(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve possuir no máximo 100 caracteres")
        @Schema(
                description = "Nome do estado.",
                example = "São Paulo"
        )
        String nome,

        @NotBlank(message = "A sigla é obrigatória")
        @Size(min = 2, max = 2, message = "A sigla deve possuir exatamente 2 caracteres")
        @Schema(
                description = "Sigla oficial do estado.",
                example = "SP"
        )
        String sigla

) {
}
