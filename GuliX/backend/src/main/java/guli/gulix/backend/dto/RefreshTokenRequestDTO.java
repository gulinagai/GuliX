package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema( description = "Dados necessários para operações que utilizam um refresh token." )
public record RefreshTokenRequestDTO(

        @NotBlank(message = "O refresh token é obrigatório")
        @Schema( description = "Refresh token utilizado para renovar os tokens ou encerrar a sessão.", example = "550e8400-e29b-41d4-a716-446655440000" )
        String refreshToken
) {
}
