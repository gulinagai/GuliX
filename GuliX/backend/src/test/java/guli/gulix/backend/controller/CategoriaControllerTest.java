package guli.gulix.backend.controller;

import guli.gulix.backend.dto.CategoriaRequestDTO;
import guli.gulix.backend.dto.CategoriaResponseDTO;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.fixture.CategoriaFixture;
import guli.gulix.backend.repository.UsuarioRepository;
import guli.gulix.backend.security.JwtUtil;
import guli.gulix.backend.service.CategoriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoriaService categoriaService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    // getListCategoria()

    @Test
    void deveRetornar200AoBuscarTodasAsCategorias() throws Exception {
        // Arrange
        CategoriaResponseDTO categoriaEsperada = CategoriaFixture.categoriaResponseDTO();
        when(categoriaService.getListCategoria()).thenReturn(List.of(categoriaEsperada));

        // Act & Assert
        mockMvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(categoriaEsperada.getId()))
                .andExpect(jsonPath("$[0].nome").value(categoriaEsperada.getNome()));

        verify(categoriaService).getListCategoria();
    }

    @Test
    void deveRetornar200AoBuscarTodasAsCategoriasQuandoListaVazia() throws Exception {
        // Arrange
        when(categoriaService.getListCategoria()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(categoriaService).getListCategoria();
    }

    // getCategoriaById()

    @Test
    void deveRetornar200AoBuscarCategoriaPorId() throws Exception {
        // Arrange
        CategoriaResponseDTO categoriaEsperada = CategoriaFixture.categoriaResponseDTO();
        when(categoriaService.getCategoriaById(1)).thenReturn(categoriaEsperada);

        // Act & Assert
        mockMvc.perform(get("/api/v1/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoriaEsperada.getId()))
                .andExpect(jsonPath("$.nome").value(categoriaEsperada.getNome()));

        verify(categoriaService).getCategoriaById(1);
    }

    @Test
    void deveRetornar404AoBuscarCategoriaInexistente() throws Exception {
        // Arrange
        when(categoriaService.getCategoriaById(99))
                .thenThrow(new RecursoNaoEncontradoException("Categoria com id 99 não encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/categorias/99"))
                .andExpect(status().isNotFound());

        verify(categoriaService).getCategoriaById(99);
    }

    // createNewCategoria()

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar201QuandoCategoriaCriada() throws Exception {
        // Arrange
        CategoriaRequestDTO categoriaRequest = CategoriaFixture.categoriaRequestDTO();
        CategoriaResponseDTO categoriaEsperada = CategoriaFixture.categoriaResponseDTO();
        when(categoriaService.createNewCategoria(any(CategoriaRequestDTO.class)))
                .thenReturn(categoriaEsperada);

        // Act & Assert
        mockMvc.perform(post("/api/v1/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/categorias/" + categoriaEsperada.getId()))
                .andExpect(jsonPath("$.id").value(categoriaEsperada.getId()))
                .andExpect(jsonPath("$.nome").value(categoriaEsperada.getNome()));

        verify(categoriaService).createNewCategoria(any(CategoriaRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400QuandoCategoriaCriadaComNomeInvalido() throws Exception {
        // Arrange
        CategoriaRequestDTO categoriaRequest = new CategoriaRequestDTO("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoriaService);
    }

    // updateCategoriaById()

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoAtualizarCategoriaPorId() throws Exception {
        // Arrange
        CategoriaRequestDTO categoriaRequest = CategoriaFixture.categoriaRequestDTO();
        CategoriaResponseDTO categoriaEsperada = CategoriaFixture.categoriaResponseDTO();
        when(categoriaService.updateCategoriaById(eq(1), any(CategoriaRequestDTO.class)))
                .thenReturn(categoriaEsperada);

        // Act & Assert
        mockMvc.perform(put("/api/v1/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoriaEsperada.getId()))
                .andExpect(jsonPath("$.nome").value(categoriaEsperada.getNome()));

        verify(categoriaService).updateCategoriaById(eq(1), any(CategoriaRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400AoAtualizarCategoriaComNomeInvalido() throws Exception {
        // Arrange
        CategoriaRequestDTO categoriaRequest = new CategoriaRequestDTO("   ");

        // Act & Assert
        mockMvc.perform(put("/api/v1/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(categoriaService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404AoAtualizarCategoriaInexistente() throws Exception {
        // Arrange
        CategoriaRequestDTO categoriaRequest = CategoriaFixture.categoriaRequestDTO();
        when(categoriaService.updateCategoriaById(eq(99), any(CategoriaRequestDTO.class)))
                .thenThrow(new RecursoNaoEncontradoException("Categoria com id 99 não encontrado"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/categorias/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaRequest)))
                .andExpect(status().isNotFound());

        verify(categoriaService).updateCategoriaById(eq(99), any(CategoriaRequestDTO.class));
    }

    // deleteCategoriaById()

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar204AoExcluirCategoriaPorId() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/categorias/1"))
                .andExpect(status().isNoContent());

        verify(categoriaService).deleteCategoriaById(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404AoExcluirCategoriaInexistente() throws Exception {
        // Arrange
        doThrow(new RecursoNaoEncontradoException("Categoria com id 99 não encontrado"))
                .when(categoriaService).deleteCategoriaById(99);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/categorias/99"))
                .andExpect(status().isNotFound());

        verify(categoriaService).deleteCategoriaById(99);
    }
}
