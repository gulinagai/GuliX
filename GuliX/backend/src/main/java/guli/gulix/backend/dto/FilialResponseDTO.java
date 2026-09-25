package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Dados de uma filial."
)
public record FilialResponseDTO(

        @Schema(
                description = "Identificador único da filial.",
                example = "1"
        )
        Integer id,

        @Schema(
                description = "Nome da filial.",
                example = "Filial São Paulo",
                maxLength = 100
        )
        String nome,

        @Schema(
                description = "Dados do endereço da filial."
        )
        EnderecoFilialDTO endereco,

        @Schema(
                description = "Indica se a filial está ativa.",
                example = "true"
        )
        Boolean ativo
) {
}