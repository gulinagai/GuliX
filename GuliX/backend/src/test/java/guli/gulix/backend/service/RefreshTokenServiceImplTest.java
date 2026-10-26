package guli.gulix.backend.service;

import guli.gulix.backend.entity.RefreshToken;
import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.exception.CredenciaisInvalidasException;
import guli.gulix.backend.fixture.RefreshTokenFixture;
import guli.gulix.backend.fixture.UsuarioFixture;
import guli.gulix.backend.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private Usuario usuario;
    private RefreshToken refreshToken;

    private static final long REFRESH_TOKEN_EXPIRATION = 604800000L;

    @BeforeEach
    void setup() {
        usuario = UsuarioFixture.usuario();
        refreshToken = RefreshTokenFixture.refreshToken(usuario);

        ReflectionTestUtils.setField(
                refreshTokenService,
                "refreshTokenExpiration",
                REFRESH_TOKEN_EXPIRATION
        );
    }

    // criarRefreshToken(Usuario usuario)

    @Test
    void deveCriarRefreshTokenParaUsuario() {

        // Arrange

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime inicio = LocalDateTime.now();

        // Act

        RefreshToken resultado = refreshTokenService.criarRefreshToken(usuario);

        LocalDateTime fim = LocalDateTime.now();

        // Assert

        assertNotNull(resultado);
        assertNotNull(resultado.getToken());
        assertEquals(usuario, resultado.getUsuario());
        assertFalse(resultado.isRevogado());

        assertTrue(
                Pattern.matches("[A-Za-z0-9_-]{43}", resultado.getToken()),
                "O token deve ser uma string Base64 URL-safe sem padding"
        );

        assertFalse(resultado.getExpiracao().isBefore(
                inicio.plusSeconds(REFRESH_TOKEN_EXPIRATION / 1000)
        ));

        assertFalse(resultado.getExpiracao().isAfter(
                fim.plusSeconds(REFRESH_TOKEN_EXPIRATION / 1000)
        ));

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void deveGerarTokensDiferentesParaCriacoesConsecutivas() {

        // Arrange

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act

        RefreshToken primeiro = refreshTokenService.criarRefreshToken(usuario);
        RefreshToken segundo = refreshTokenService.criarRefreshToken(usuario);

        // Assert

        assertNotEquals(primeiro.getToken(), segundo.getToken());

        verify(refreshTokenRepository, times(2))
                .save(any(RefreshToken.class));
    }

    @Test
    void deveAssociarRefreshTokenAoUsuarioInformado() {

        // Arrange

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act

        RefreshToken resultado = refreshTokenService.criarRefreshToken(usuario);

        // Assert

        assertEquals(usuario, resultado.getUsuario());

        verify(refreshTokenRepository).save(argThat(token ->
                token.getUsuario().equals(usuario)
        ));
    }

    // validarRefreshToken(String token)

    @Test
    void deveRetornarRefreshTokenQuandoTokenForValido() {

        // Arrange

        when(refreshTokenRepository.findByToken(refreshToken.getToken()))
                .thenReturn(Optional.of(refreshToken));

        // Act

        RefreshToken resultado = refreshTokenService.validarRefreshToken(
                refreshToken.getToken()
        );

        // Assert

        assertNotNull(resultado);
        assertEquals(refreshToken, resultado);
        assertFalse(resultado.isRevogado());
        assertTrue(resultado.getExpiracao().isAfter(LocalDateTime.now()));

        verify(refreshTokenRepository).findByToken(refreshToken.getToken());
    }

    @Test
    void deveLancarExcecaoQuandoRefreshTokenNaoExistir() {

        // Arrange

        String tokenInexistente = "refresh-token-inexistente";

        when(refreshTokenRepository.findByToken(tokenInexistente))
                .thenReturn(Optional.empty());

        // Act

        CredenciaisInvalidasException exception = assertThrows(
                CredenciaisInvalidasException.class,
                () -> refreshTokenService.validarRefreshToken(tokenInexistente)
        );

        // Assert

        assertEquals("Refresh Token inválido", exception.getMessage());

        verify(refreshTokenRepository).findByToken(tokenInexistente);
    }

    @Test
    void deveLancarExcecaoQuandoRefreshTokenEstiverRevogado() {

        // Arrange

        RefreshToken tokenRevogado = RefreshTokenFixture.refreshTokenRevogado();

        when(refreshTokenRepository.findByToken(tokenRevogado.getToken()))
                .thenReturn(Optional.of(tokenRevogado));

        // Act

        CredenciaisInvalidasException exception = assertThrows(
                CredenciaisInvalidasException.class,
                () -> refreshTokenService.validarRefreshToken(
                        tokenRevogado.getToken()
                )
        );

        // Assert

        assertEquals("Refresh Token inválido", exception.getMessage());

        verify(refreshTokenRepository).findByToken(tokenRevogado.getToken());
    }

    @Test
    void deveLancarExcecaoQuandoRefreshTokenEstiverExpirado() {

        // Arrange

        RefreshToken tokenExpirado = RefreshTokenFixture.refreshTokenExpirado();

        when(refreshTokenRepository.findByToken(tokenExpirado.getToken()))
                .thenReturn(Optional.of(tokenExpirado));

        // Act

        CredenciaisInvalidasException exception = assertThrows(
                CredenciaisInvalidasException.class,
                () -> refreshTokenService.validarRefreshToken(
                        tokenExpirado.getToken()
                )
        );

        // Assert

        assertEquals("Refresh Token expirado", exception.getMessage());

        verify(refreshTokenRepository).findByToken(tokenExpirado.getToken());
    }

    @Test
    void deveLancarExcecaoQuandoRefreshTokenExpirarNoInstanteAtual() {

        // Arrange

        refreshToken.setExpiracao(LocalDateTime.now());

        when(refreshTokenRepository.findByToken(refreshToken.getToken()))
                .thenReturn(Optional.of(refreshToken));

        // Act

        CredenciaisInvalidasException exception = assertThrows(
                CredenciaisInvalidasException.class,
                () -> refreshTokenService.validarRefreshToken(
                        refreshToken.getToken()
                )
        );

        // Assert

        assertEquals("Refresh Token expirado", exception.getMessage());

        verify(refreshTokenRepository).findByToken(refreshToken.getToken());
    }

    // revogarRefreshToken(RefreshToken refreshToken)

    @Test
    void deveRevogarRefreshToken() {

        // Arrange

        assertFalse(refreshToken.isRevogado());

        // Act

        assertDoesNotThrow(
                () -> refreshTokenService.revogarRefreshToken(refreshToken)
        );

        // Assert

        assertTrue(refreshToken.isRevogado());

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void naoDeveAlterarEstadoQuandoRefreshTokenJaEstiverRevogado() {

        // Arrange

        RefreshToken tokenRevogado = RefreshTokenFixture.refreshTokenRevogado();

        // Act

        assertDoesNotThrow(
                () -> refreshTokenService.revogarRefreshToken(tokenRevogado)
        );

        // Assert

        assertTrue(tokenRevogado.isRevogado());

        verifyNoInteractions(refreshTokenRepository);
    }
}
