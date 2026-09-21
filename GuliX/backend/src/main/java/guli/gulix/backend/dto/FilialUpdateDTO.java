package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema( description = "Dados para atualização de uma filial. Os campos informados serão utilizados para atualizar os dados da filial." )
public record FilialUpdateDTO(

        @NotBlank
        @Size(max = 100)
        @Schema(
                description = "Nome da filial.",
                example = "Filial São Paulo",
                maxLength = 100
        )
        String nome,

        @Valid
        @Schema(
                description = "Dados do endereço da filial."
        )
        EnderecoFilialDTO endereco,

        Boolean ativo
) {
}