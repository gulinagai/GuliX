package guli.gulix.backend.fixture;

import guli.gulix.backend.entity.RefreshToken;
import guli.gulix.backend.entity.Usuario;

import java.time.LocalDateTime;

public final class RefreshTokenFixture {

    private RefreshTokenFixture() {
    }

    public static RefreshToken refreshToken() {
        return refreshToken(UsuarioFixture.usuario());
    }

    public static RefreshToken refreshToken(Usuario usuario) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setId(1);
        refreshToken.setToken("refresh-token-teste");
        refreshToken.setExpiracao(LocalDateTime.now().plusDays(7));
        refreshToken.setRevogado(false);
        refreshToken.setUsuario(usuario);

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

