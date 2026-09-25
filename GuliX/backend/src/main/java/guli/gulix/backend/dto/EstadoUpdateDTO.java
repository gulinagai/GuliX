package guli.gulix.backend.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record EstadoUpdateDTO(

        @Size(max = 100, message = "O nome deve possuir no máximo 100 caracteres")
        @Schema(
                description = "Novo nome do estado.",
                example = "São Paulo"
        )
        String nome,

        @Size(min = 2, max = 2, message = "A sigla deve possuir exatamente 2 caracteres")
        @Schema(
                description = "Nova sigla oficial do estado.",
                example = "SP"
        )
        String sigla

) {
}
