package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema( description = "Estrutura padrão utilizada pela API para representar erros." )
public record ErrorResponseDTO (
        @Schema( description = "Data e hora em que o erro ocorreu.", example = "2026-09-13T11:30:00" )
        LocalDateTime timestamp,

        @Schema( description = "Código HTTP correspondente ao erro.", example = "404" )
        int status,

        @Schema( description = "Descrição resumida do tipo de erro.", example = "Recurso não encontrado" )
        String error,

        @Schema( description = "Mensagem detalhada sobre o erro.", example = "Produto não encontrado." )
        String message,

        @Schema( description = "Endpoint em que o erro ocorreu.", example = "/api/v1/produtos/10" )
        String path
) {
}
