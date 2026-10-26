package guli.gulix.backend.controller;

import guli.gulix.backend.dto.UsuarioAdminResponseDTO;
import guli.gulix.backend.dto.UsuarioResponseDTO;
import guli.gulix.backend.dto.UsuarioUpdateDTO;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.fixture.UsuarioFixture;
import guli.gulix.backend.repository.UsuarioRepository;
import guli.gulix.backend.security.JwtUtil;
import guli.gulix.backend.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService usuarioService;

    // Dependências utilizadas pelo filtro JWT carregado no contexto MVC.
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;


    // getListUsuario()

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoListarUsuarios() throws Exception {

        // Arrange

        UsuarioAdminResponseDTO usuario = UsuarioFixture.usuarioAdminResponseDTO();

        when(usuarioService.getListUsuario()).thenReturn(List.of(usuario));

        // Act & Assert

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(usuario.getId()))
                .andExpect(jsonPath("$[0].nome").value(usuario.getNome()))
                .andExpect(jsonPath("$[0].email").value(usuario.getEmail()))
                .andExpect(jsonPath("$[0].telefone").value(usuario.getTelefone()))
                .andExpect(jsonPath("$[0].role").value(usuario.getRole().name()));

        verify(usuarioService).getListUsuario();
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornarListaVaziaQuandoNaoExistiremUsuarios() throws Exception {

        // Arrange

        when(usuarioService.getListUsuario()).thenReturn(List.of());

        // Act & Assert

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(usuarioService).getListUsuario();
    }


    // getUsuarioById()

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoBuscarUsuarioPorId() throws Exception {

        // Arrange

        UsuarioResponseDTO usuario = UsuarioFixture.usuarioResponseDTO();

        when(usuarioService.getUsuarioById(1)).thenReturn(usuario);

        // Act & Assert

        mockMvc.perform(get("/api/v1/usuarios/{usuarioId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(usuario.getId()))
                .andExpect(jsonPath("$.nome").value(usuario.getNome()))
                .andExpect(jsonPath("$.email").value(usuario.getEmail()))
                .andExpect(jsonPath("$.telefone").value(usuario.getTelefone()));

        verify(usuarioService).getUsuarioById(1);
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404QuandoUsuarioNaoForEncontrado() throws Exception {

        // Arrange

        when(usuarioService.getUsuarioById(999))
                .thenThrow(new RecursoNaoEncontradoException("Usuário não encontrado"));

        // Act & Assert

        mockMvc.perform(get("/api/v1/usuarios/{usuarioId}", 999))
                .andExpect(status().isNotFound());

        verify(usuarioService).getUsuarioById(999);
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400QuandoIdDoUsuarioForInvalido() throws Exception {

        // Act & Assert

        mockMvc.perform(get("/api/v1/usuarios/{usuarioId}", "abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }


    // updateUsuarioById()

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoAtualizarUsuario() throws Exception {

        // Arrange

        UsuarioUpdateDTO request = UsuarioFixture.usuarioUpdateDTO();
        UsuarioResponseDTO response = UsuarioFixture.usuarioResponseDTO();

        response.setNome(request.getNome());
        response.setTelefone(request.getTelefone());

        when(usuarioService.updateUsuarioById(eq(1), any(UsuarioUpdateDTO.class)))
                .thenReturn(response);

        // Act & Assert

        mockMvc.perform(put("/api/v1/usuarios/{usuarioId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.nome").value(response.getNome()))
                .andExpect(jsonPath("$.telefone").value(response.getTelefone()));

        verify(usuarioService).updateUsuarioById(eq(1), any(UsuarioUpdateDTO.class));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400AoAtualizarUsuarioComNomeVazio() throws Exception {

        // Arrange

        UsuarioUpdateDTO request = new UsuarioUpdateDTO(
                "",
                "NovaSenha123",
                "11912345678"
        );

        // Act & Assert

        mockMvc.perform(put("/api/v1/usuarios/{usuarioId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400AoAtualizarUsuarioComSenhaInvalida() throws Exception {

        // Arrange

        UsuarioUpdateDTO request = new UsuarioUpdateDTO(
                "Usuario Teste",
                "senha",
                "11912345678"
        );

        // Act & Assert

        mockMvc.perform(put("/api/v1/usuarios/{usuarioId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400AoAtualizarUsuarioComTelefoneInvalido() throws Exception {

        // Arrange

        UsuarioUpdateDTO request = new UsuarioUpdateDTO(
                "Usuario Teste",
                "NovaSenha123",
                "123"
        );

        // Act & Assert

        mockMvc.perform(put("/api/v1/usuarios/{usuarioId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404AoAtualizarUsuarioInexistente() throws Exception {

        // Arrange

        UsuarioUpdateDTO request = UsuarioFixture.usuarioUpdateDTO();

        when(usuarioService.updateUsuarioById(eq(999), any(UsuarioUpdateDTO.class)))
                .thenThrow(new RecursoNaoEncontradoException("Usuário não encontrado"));

        // Act & Assert

        mockMvc.perform(put("/api/v1/usuarios/{usuarioId}", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(usuarioService).updateUsuarioById(eq(999), any(UsuarioUpdateDTO.class));
    }


    // updatePatchUsuarioById()

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoAtualizarUsuarioParcialmente() throws Exception {

        // Arrange

        UsuarioUpdateDTO request = new UsuarioUpdateDTO(
                "Usuario Atualizado",
                null,
                "11912345678"
        );

        UsuarioResponseDTO response = UsuarioFixture.usuarioResponseDTO();
        response.setNome(request.getNome());
        response.setTelefone(request.getTelefone());

        when(usuarioService.updatePatchUsuarioById(eq(1), any(UsuarioUpdateDTO.class)))
                .thenReturn(response);

        // Act & Assert

        mockMvc.perform(patch("/api/v1/usuarios/{usuarioId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.nome").value(response.getNome()))
                .andExpect(jsonPath("$.telefone").value(response.getTelefone()));

        verify(usuarioService).updatePatchUsuarioById(eq(1), any(UsuarioUpdateDTO.class));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400AoAtualizarParcialmenteUsuarioComNomeVazio() throws Exception {

        // Arrange

        UsuarioUpdateDTO request = new UsuarioUpdateDTO(
                "",
                null,
                null
        );

        // Act & Assert

        mockMvc.perform(patch("/api/v1/usuarios/{usuarioId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404AoAtualizarParcialmenteUsuarioInexistente() throws Exception {

        // Arrange

        UsuarioUpdateDTO request = UsuarioFixture.usuarioUpdateDTO();

        when(usuarioService.updatePatchUsuarioById(eq(999), any(UsuarioUpdateDTO.class)))
                .thenThrow(new RecursoNaoEncontradoException("Usuário não encontrado"));

        // Act & Assert

        mockMvc.perform(patch("/api/v1/usuarios/{usuarioId}", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(usuarioService).updatePatchUsuarioById(eq(999), any(UsuarioUpdateDTO.class));
    }


    // deleteUsuarioById()

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar204AoExcluirUsuario() throws Exception {

        // Act & Assert

        mockMvc.perform(delete("/api/v1/usuarios/{usuarioId}", 1))
                .andExpect(status().isNoContent());

        verify(usuarioService).deleteUsuarioById(1);
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404AoExcluirUsuarioInexistente() throws Exception {

        // Arrange

        doThrow(new RecursoNaoEncontradoException("Usuário não encontrado"))
                .when(usuarioService).deleteUsuarioById(999);

        // Act & Assert

        mockMvc.perform(delete("/api/v1/usuarios/{usuarioId}", 999))
                .andExpect(status().isNotFound());

        verify(usuarioService).deleteUsuarioById(999);
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400QuandoIdDoUsuarioForInvalidoAoExcluir() throws Exception {

        // Act & Assert

        mockMvc.perform(delete("/api/v1/usuarios/{usuarioId}", "abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }
}
