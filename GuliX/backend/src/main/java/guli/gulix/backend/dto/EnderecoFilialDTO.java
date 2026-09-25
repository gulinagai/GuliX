package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(
        description = "Dados do endereço de uma filial."
)
public record EnderecoFilialDTO(

        @NotBlank
        @Schema(
                description = "Nome da rua onde a filial está localizada.",
                example = "Avenida Paulista"
        )
        String rua,

        @NotBlank
        @Schema(
                description = "Número do endereço da filial.",
                example = "1000"
        )
        String numero,

        @NotBlank
        @Schema(
                description = "Nome da cidade onde a filial está localizada.",
                example = "São Paulo"
        )
        String cidade,

        @NotBlank
        @Size(min = 2, max = 2)
        @Schema(
                description = "Sigla do estado onde a filial está localizada.",
                example = "SP",
                minLength = 2,
                maxLength = 2
        )
        String estado,

        @NotBlank
        @Schema(
                description = "CEP do endereço da filial.",
                example = "01310100"
        )
        String cep,

        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        @Schema(
                description = "Latitude da localização da filial em graus decimais.",
                example = "-23.561684",
                minimum = "-90.0",
                maximum = "90.0"
        )
        BigDecimal latitude,

        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        @Schema(
                description = "Longitude da localização da filial em graus decimais.",
                example = "-46.655981",
                minimum = "-180.0",
                maximum = "180.0"
        )
        BigDecimal longitude
) {
}