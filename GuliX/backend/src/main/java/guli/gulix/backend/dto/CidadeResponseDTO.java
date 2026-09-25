package guli.gulix.backend.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(
        description = "Dados de uma cidade."
)
public record CidadeResponseDTO(

        @Schema(
                description = "Identificador único da cidade.",
                example = "1"
        )
        Integer id,

        @Schema(
                description = "Nome da cidade.",
                example = "São Paulo"
        )
        String nome,

        @Schema(
                description = "ID do estado ao qual a cidade pertence.",
                example = "1"
        )
        Integer estadoId,

        @Schema(
                description = "Latitude central da cidade em graus decimais.",
                example = "-23.550520",
                minimum = "-90",
                maximum = "90"
        )
        BigDecimal latitudeCentral,

        @Schema(
                description = "Longitude central da cidade em graus decimais.",
                example = "-46.633308",
                minimum = "-180",
                maximum = "180"
        )
        BigDecimal longitudeCentral

) {}
