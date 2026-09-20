package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Dados necessários para criar uma nova cidade.")
public record CidadeCreateDTO(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve possuir no máximo 150 caracteres")
        @Schema(
                description = "Nome da cidade.",
                example = "São Paulo",
                maxLength = 150
        )
        String nome,

        @NotNull(message = "O estado é obrigatório")
        @Schema(
                description = "ID do estado.",
                example = "1"
        )
        Integer estadoId,

        @NotNull(message = "A latitude central da cidade é obrigatória")
        @DecimalMin(value = "-90.0", message = "A latitude deve estar entre -90 e 90")
        @DecimalMax(value = "90.0", message = "A latitude deve estar entre -90 e 90")
        @Schema(
                description = "Latitude central da cidade em graus decimais.",
                example = "-23.550520",
                minimum = "-90",
                maximum = "90"
        )
        BigDecimal latitudeCentral,

        @NotNull(message = "A longitude central da cidade é obrigatória")
        @DecimalMin(value = "-180.0", message = "A longitude deve estar entre -180 e 180")
        @DecimalMax(value = "180.0", message = "A longitude deve estar entre -180 e 180")
        @Schema(
                description = "Longitude central da cidade em graus decimais.",
                example = "-46.633308",
                minimum = "-180",
                maximum = "180"
        )
        BigDecimal longitudeCentral

) {
}
