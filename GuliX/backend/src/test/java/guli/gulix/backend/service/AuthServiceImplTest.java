package guli.gulix.backend.service;

import guli.gulix.backend.dto.AuthResponseDTO;
import guli.gulix.backend.dto.LoginRequestDTO;
import guli.gulix.backend.dto.RefreshTokenRequestDTO;
import guli.gulix.backend.entity.RefreshToken;
import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.exception.CredenciaisInvalidasException;
import guli.gulix.backend.fixture.AuthFixture;
import guli.gulix.backend.fixture.UsuarioFixture;
import guli.gulix.backend.repository.UsuarioRepository;
import guli.gulix.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    private Usuario usuario;
    private RefreshToken refreshToken;
    private LoginRequestDTO loginRequestDTO;
    private RefreshTokenRequestDTO refreshTokenRequestDTO;
    private AuthResponseDTO authResponseDTO;

    @BeforeEach
    void setup() {
        usuario = UsuarioFixture.usuario();
        refreshToken = AuthFixture.refreshToken(usuario);
        loginRequestDTO = AuthFixture.loginRequestDTO();
        refreshTokenRequestDTO = AuthFixture.refreshTokenRequestDTO();
        authResponseDTO = AuthFixture.authResponseDTO();
    }

    // login(LoginRequestDTO dto)

    @Test
    void deveRetornarTokensQuandoCredenciaisForemValidas() {
        // Arrange
        when(usuarioRepository.findByEmail(loginRequestDTO.email()))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(
                loginRequestDTO.senha(),
                usuario.getSenhaHash()
        )).thenReturn(true);

        when(jwtUtil.gerarToken(usuario))
                .thenReturn(authResponseDTO.token());

        when(refreshTokenService.criarRefreshToken(usuario))
                .thenReturn(refreshToken);

        // Act
        AuthResponseDTO resultado = authService.login(loginRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(authResponseDTO.token(), resultado.token());
        assertEquals(refreshToken.getToken(), resultado.refreshToken());
        assertEquals("Bearer", resultado.type());

        verify(usuarioRepository).findByEmail(loginRequestDTO.email());
        verify(passwordEncoder).matches(
                loginRequestDTO.senha(),
                usuario.getSenhaHash()
        );
        verify(jwtUtil).gerarToken(usuario);
        verify(refreshTokenService).criarRefreshToken(usuario);
    }

    @Test
    void deveLancarExcecaoQuandoEmailNaoExistir() {
        // Arrange
        when(usuarioRepository.findByEmail(loginRequestDTO.email()))
                .thenReturn(Optional.empty());

        // Act
        CredenciaisInvalidasException exception = assertThrows(
                CredenciaisInvalidasException.class,
                () -> authService.login(loginRequestDTO)
        );

        // Assert
        assertEquals("Email ou senha inválidos.", exception.getMessage());

        verify(usuarioRepository).findByEmail(loginRequestDTO.email());
        verifyNoInteractions(passwordEncoder, jwtUtil, refreshTokenService);
    }

    @Test
    void deveLancarExcecaoQuandoSenhaForInvalida() {
        // Arrange
        when(usuarioRepository.findByEmail(loginRequestDTO.email()))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(
                loginRequestDTO.senha(),
                usuario.getSenhaHash()
        )).thenReturn(false);

        // Act
        CredenciaisInvalidasException exception = assertThrows(
                CredenciaisInvalidasException.class,
                () -> authService.login(loginRequestDTO)
        );

        // Assert
        assertEquals("Email ou senha inválidos.", exception.getMessage());

        verify(usuarioRepository).findByEmail(loginRequestDTO.email());
        verify(passwordEncoder).matches(
                loginRequestDTO.senha(),
                usuario.getSenhaHash()
        );
        verifyNoInteractions(jwtUtil, refreshTokenService);
    }

    // refresh(RefreshTokenRequestDTO dto)

    @Test
    void deveRetornarNovosTokensQuandoRefreshTokenForValido() {
        // Arrange
        RefreshToken refreshTokenNovo = AuthFixture.refreshToken(usuario);
        refreshTokenNovo.setId(2);
        refreshTokenNovo.setToken("novo-refresh-token");

        when(refreshTokenService.validarRefreshToken(
                refreshTokenRequestDTO.refreshToken()
        )).thenReturn(refreshToken);

        when(jwtUtil.gerarToken(usuario))
                .thenReturn("novo-jwt-token");

        when(refreshTokenService.criarRefreshToken(usuario))
                .thenReturn(refreshTokenNovo);

        // Act
        AuthResponseDTO resultado = authService.refresh(refreshTokenRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("novo-jwt-token", resultado.token());
        assertEquals("novo-refresh-token", resultado.refreshToken());
        assertEquals("Bearer", resultado.type());

        verify(refreshTokenService).validarRefreshToken(
                refreshTokenRequestDTO.refreshToken()
        );
        verify(jwtUtil).gerarToken(usuario);
        verify(refreshTokenService).criarRefreshToken(usuario);
        verify(refreshTokenService).revogarRefreshToken(refreshToken);
    }

    @Test
    void devePropagarExcecaoQuandoRefreshTokenForInvalido() {
        // Arrange
        when(refreshTokenService.validarRefreshToken(
                refreshTokenRequestDTO.refreshToken()
        )).thenThrow(
                new CredenciaisInvalidasException("Refresh Token inválido")
        );

        // Act
        CredenciaisInvalidasException exception = assertThrows(
                CredenciaisInvalidasException.class,
                () -> authService.refresh(refreshTokenRequestDTO)
        );

        // Assert
        assertEquals("Refresh Token inválido", exception.getMessage());

        verify(refreshTokenService).validarRefreshToken(
                refreshTokenRequestDTO.refreshToken()
        );
        verifyNoInteractions(jwtUtil);
        verify(refreshTokenService, never()).criarRefreshToken(any());
        verify(refreshTokenService, never()).revogarRefreshToken(any());
    }

    // logout(RefreshTokenRequestDTO dto)

    @Test
    void deveRevogarRefreshTokenQuandoLogoutForRealizado() {
        // Arrange
        when(refreshTokenService.validarRefreshToken(
                refreshTokenRequestDTO.refreshToken()
        )).thenReturn(refreshToken);

        // Act
        assertDoesNotThrow(() -> authService.logout(refreshTokenRequestDTO));

        // Assert
        verify(refreshTokenService).validarRefreshToken(
                refreshTokenRequestDTO.refreshToken()
        );
        verify(refreshTokenService).revogarRefreshToken(refreshToken);

        verifyNoInteractions(usuarioRepository, passwordEncoder, jwtUtil);
    }

    @Test
    void devePropagarExcecaoQuandoRefreshTokenForInvalidoNoLogout() {
        // Arrange
        when(refreshTokenService.validarRefreshToken(
                refreshTokenRequestDTO.refreshToken()
        )).thenThrow(
                new CredenciaisInvalidasException("Refresh Token inválido")
        );

        // Act
        CredenciaisInvalidasException exception = assertThrows(
                CredenciaisInvalidasException.class,
                () -> authService.logout(refreshTokenRequestDTO)
        );

        // Assert
        assertEquals("Refresh Token inválido", exception.getMessage());

        verify(refreshTokenService).validarRefreshToken(
                refreshTokenRequestDTO.refreshToken()
        );
        verify(refreshTokenService, never()).revogarRefreshToken(any());

        verifyNoInteractions(usuarioRepository, passwordEncoder, jwtUtil);
    }
}
