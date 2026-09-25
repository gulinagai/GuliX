package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema( description = "Estrutura utilizada pela API para representar erros de validação dos dados enviados na requisição." )
public record ValidationErrorResponseDTO(

        @Schema( description = "Data e hora em que o erro ocorreu.", example = "2026-09-13T11:30:00" )
        LocalDateTime timestamp,

        @Schema( description = "Código HTTP correspondente ao erro.", example = "400" )
        int status,

        @Schema( description = "Descrição resumida do tipo de erro.", example = "Erro de validação" )
        String error,

        @Schema( description = "Mensagem geral informando que existem campos inválidos na requisição.", example = "Existem campos inválidos na requisição." )
        String message,

        @Schema( description = "Endpoint em que o erro de validação ocorreu.", example = "/api/v1/produtos" )
        String path,

        @Schema( description = "Mapa contendo os campos que falharam na validação e suas respectivas mensagens de erro.",
                example = "{\"nome\": \"O nome é obrigatório\", \"preco\": \"O preço deve ser maior que zero\"}" )
        Map<String, String> fields
) {
}
