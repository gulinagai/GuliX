package guli.gulix.backend.controller;

import guli.gulix.backend.dto.AuthResponseDTO;
import guli.gulix.backend.dto.LoginRequestDTO;
import guli.gulix.backend.dto.RefreshTokenRequestDTO;
import guli.gulix.backend.dto.UsuarioCreateDTO;
import guli.gulix.backend.dto.UsuarioResponseDTO;
import guli.gulix.backend.fixture.AuthFixture;
import guli.gulix.backend.fixture.UsuarioFixture;
import guli.gulix.backend.repository.UsuarioRepository;
import guli.gulix.backend.security.JwtUtil;
import guli.gulix.backend.service.AuthService;
import guli.gulix.backend.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UsuarioService usuarioService;

    // Dependências utilizadas pelo filtro JWT carregado no contexto MVC.
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;


    // login(LoginRequestDTO dto)

    @Test
    void deveRetornar200AoRealizarLogin() throws Exception {
        // Arrange
        LoginRequestDTO request = AuthFixture.loginRequestDTO();
        AuthResponseDTO response = AuthFixture.authResponseDTO();

        when(authService.login(any(LoginRequestDTO.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(response.token()))
                .andExpect(jsonPath("$.refreshToken").value(response.refreshToken()))
                .andExpect(jsonPath("$.type").value(response.type()));

        verify(authService).login(any(LoginRequestDTO.class));
        verifyNoInteractions(usuarioService);
    }

    @Test
    void deveRetornar400AoRealizarLoginComEmailInvalido() throws Exception {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO(
                "email-invalido",
                "SenhaTeste123"
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService, usuarioService);
    }

    @Test
    void deveRetornar400AoRealizarLoginComCamposObrigatoriosVazios() throws Exception {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO("", "");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService, usuarioService);
    }


    // createNewUsuario(UsuarioCreateDTO usuarioRequest)

    @Test
    void deveRetornar201AoCadastrarUsuario() throws Exception {
        // Arrange
        UsuarioCreateDTO request = UsuarioFixture.usuarioCreateDTO();
        UsuarioResponseDTO response = UsuarioFixture.usuarioResponseDTO();

        when(usuarioService.createNewUsuario(any(UsuarioCreateDTO.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/v1/usuarios/" + response.getId()
                ))
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.nome").value(response.getNome()))
                .andExpect(jsonPath("$.email").value(response.getEmail()))
                .andExpect(jsonPath("$.telefone").value(response.getTelefone()));

        verify(usuarioService).createNewUsuario(any(UsuarioCreateDTO.class));
        verifyNoInteractions(authService);
    }

    @Test
    void deveRetornar400AoCadastrarUsuarioComEmailInvalido() throws Exception {
        // Arrange
        UsuarioCreateDTO request = UsuarioFixture.usuarioCreateDTO();
        request.setEmail("email-invalido");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService, authService);
    }

    @Test
    void deveRetornar400AoCadastrarUsuarioComSenhaFraca() throws Exception {
        // Arrange
        UsuarioCreateDTO request = UsuarioFixture.usuarioCreateDTO();
        request.setSenha("abc");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService, authService);
    }


    // refresh(RefreshTokenRequestDTO dto)

    @Test
    void deveRetornar200AoRenovarTokens() throws Exception {
        // Arrange
        RefreshTokenRequestDTO request = AuthFixture.refreshTokenRequestDTO();
        AuthResponseDTO response = AuthFixture.authResponseDTO();

        when(authService.refresh(any(RefreshTokenRequestDTO.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(response.token()))
                .andExpect(jsonPath("$.refreshToken").value(response.refreshToken()))
                .andExpect(jsonPath("$.type").value(response.type()));

        verify(authService).refresh(any(RefreshTokenRequestDTO.class));
        verifyNoInteractions(usuarioService);
    }

    @Test
    void deveRetornar400AoRenovarTokensComRefreshTokenVazio() throws Exception {
        // Arrange
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService, usuarioService);
    }


    // logout(RefreshTokenRequestDTO dto)

    @Test
    void deveRetornar204AoRealizarLogout() throws Exception {
        // Arrange
        RefreshTokenRequestDTO request = AuthFixture.refreshTokenRequestDTO();

        doNothing().when(authService).logout(any(RefreshTokenRequestDTO.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(authService).logout(any(RefreshTokenRequestDTO.class));
        verifyNoInteractions(usuarioService);
    }

    @Test
    void deveRetornar400AoRealizarLogoutComRefreshTokenVazio() throws Exception {
        // Arrange
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService, usuarioService);
    }
}
