package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record EstadoResponseDTO(

        @Schema(
                description = "Identificador único do estado.",
                example = "1"
        )
        Integer id,

        @Schema(
                description = "Nome do estado.",
                example = "São Paulo"
        )
        String nome,

        @Schema(
                description = "Sigla oficial do estado.",
                example = "SP"
        )
        String sigla

) {
}
