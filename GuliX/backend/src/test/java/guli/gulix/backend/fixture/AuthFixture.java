package guli.gulix.backend.fixture;

import guli.gulix.backend.dto.AuthResponseDTO;
import guli.gulix.backend.dto.LoginRequestDTO;
import guli.gulix.backend.dto.RefreshTokenRequestDTO;
import guli.gulix.backend.entity.RefreshToken;
import guli.gulix.backend.entity.Usuario;

import java.time.LocalDateTime;

public class AuthFixture {

    private AuthFixture() {
    }

    public static LoginRequestDTO loginRequestDTO() {
        return new LoginRequestDTO(
                "usuario.teste@email.com",
                "SenhaTeste123"
        );
    }

    public static LoginRequestDTO loginRequestDTOEmailInvalido() {
        return new LoginRequestDTO(
                "inexistente@email.com",
                "SenhaTeste123"
        );
    }

    public static LoginRequestDTO loginRequestDTOSenhaInvalida() {
        return new LoginRequestDTO(
                "usuario.teste@email.com",
                "SenhaIncorreta123"
        );
    }

    public static RefreshTokenRequestDTO refreshTokenRequestDTO() {
        return new RefreshTokenRequestDTO(
                "refresh-token-teste"
        );
    }

    public static RefreshTokenRequestDTO refreshTokenRequestDTOInvalido() {
        return new RefreshTokenRequestDTO(
                "refresh-token-invalido"
        );
    }

    public static AuthResponseDTO authResponseDTO() {
        return new AuthResponseDTO(
                "jwt-token-teste",
                "refresh-token-teste",
                "Bearer"
        );
    }

    public static RefreshToken refreshToken() {
        return refreshToken(UsuarioFixture.usuario());
    }

    public static RefreshToken refreshToken(Usuario usuario) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setId(1);
        refreshToken.setToken("refresh-token-teste");
        refreshToken.setUsuario(usuario);
        refreshToken.setExpiracao(LocalDateTime.now().plusDays(7));
        refreshToken.setRevogado(false);
        refreshToken.setCriadoEm(LocalDateTime.now());

        return refreshToken;
    }

    public static RefreshToken refreshTokenExpirado() {
        RefreshToken refreshToken = refreshToken();

        refreshToken.setExpiracao(LocalDateTime.now().minusDays(1));

        return refreshToken;
    }

    public static RefreshToken refreshTokenRevogado() {
        RefreshToken refreshToken = refreshToken();

        refreshToken.setRevogado(true);

        return refreshToken;
    }
}

