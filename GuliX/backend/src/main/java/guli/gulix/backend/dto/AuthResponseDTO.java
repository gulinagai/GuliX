package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema( description = "Resposta retornada após uma autenticação ou renovação dos tokens." )
public record AuthResponseDTO(

        @Schema( description = "Token (JWT) de acesso utilizado para autenticar as requisições protegidas da API.", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NSJ9..." )
        String token,

        @Schema( description = "Token (refresh Token) utilizado para solicitar a renovação do token (JWT) de acesso.", example = "550e8400-e29b-41d4-a716-446655440000" )
        String refreshToken,

        @Schema( description = "Tipo do token de autenticação.", example = "Bearer" )
        String type
) {}
