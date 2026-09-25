package guli.gulix.backend.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(
        description = "Dados para atualização de uma cidade. Os campos são opcionais e somente os campos informados serão alterados."
)
public record CidadeUpdateDTO(

        @Size(max = 150, message = "O nome deve possuir no máximo 150 caracteres")
        @Schema(
                description = "Novo nome da cidade.",
                example = "São Paulo",
                maxLength = 150
        )
        String nome,

        @Schema(
                description = "ID do novo estado ao qual a cidade pertence.",
                example = "1",
                minimum = "1"
        )
        Integer estadoId,

        @DecimalMin(value = "-90.0", message = "A latitude deve estar entre -90 e 90")
        @DecimalMax(value = "90.0", message = "A latitude deve estar entre -90 e 90")
        @Schema(
                description = "Nova latitude central da cidade em graus decimais.",
                example = "-23.550520",
                minimum = "-90",
                maximum = "90"
        )
        BigDecimal latitudeCentral,

        @DecimalMin(value = "-180.0", message = "A longitude deve estar entre -180 e 180")
        @DecimalMax(value = "180.0", message = "A longitude deve estar entre -180 e 180")
        @Schema(
                description = "Nova longitude central da cidade em graus decimais.",
                example = "-46.633308",
                minimum = "-180",
                maximum = "180"
        )
        BigDecimal longitudeCentral

) {
}
