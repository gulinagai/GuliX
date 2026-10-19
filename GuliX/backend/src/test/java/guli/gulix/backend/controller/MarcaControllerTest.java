package guli.gulix.backend.controller;

import guli.gulix.backend.dto.MarcaRequestDTO;
import guli.gulix.backend.dto.MarcaResponseDTO;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.fixture.MarcaFixture;
import guli.gulix.backend.repository.UsuarioRepository;
import guli.gulix.backend.security.JwtUtil;
import guli.gulix.backend.service.MarcaService;
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

@WebMvcTest(MarcaController.class)
class MarcaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MarcaService marcaService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    // getListMarca()

    @Test
    void deveRetornar200AoBuscarTodasAsMarcas() throws Exception {
        // Arrange
        MarcaResponseDTO marcaEsperada = MarcaFixture.marcaResponseDTO();
        when(marcaService.getListMarca()).thenReturn(List.of(marcaEsperada));

        // Act & Assert
        mockMvc.perform(get("/api/v1/marcas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(marcaEsperada.getId()))
                .andExpect(jsonPath("$[0].nome").value(marcaEsperada.getNome()));

        verify(marcaService).getListMarca();
    }

    @Test
    void deveRetornar200ComListaVaziaQuandoNaoExistiremMarcas() throws Exception {
        // Arrange
        when(marcaService.getListMarca()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/marcas"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(marcaService).getListMarca();
    }

    // getMarcaById()

    @Test
    void deveRetornar200AoBuscarMarcaPorId() throws Exception {
        // Arrange
        MarcaResponseDTO marcaEsperada = MarcaFixture.marcaResponseDTO();
        when(marcaService.getMarcaById(1)).thenReturn(marcaEsperada);

        // Act & Assert
        mockMvc.perform(get("/api/v1/marcas/{marcaId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(marcaEsperada.getId()))
                .andExpect(jsonPath("$.nome").value(marcaEsperada.getNome()));

        verify(marcaService).getMarcaById(1);
    }

    @Test
    void deveRetornar404QuandoMarcaNaoExistirAoBuscarPorId() throws Exception {
        // Arrange
        when(marcaService.getMarcaById(99))
                .thenThrow(new RecursoNaoEncontradoException("Marca com id 99 não encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/marcas/{marcaId}", 99))
                .andExpect(status().isNotFound());

        verify(marcaService).getMarcaById(99);
    }

    @Test
    void deveRetornar400QuandoIdDaMarcaForInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/marcas/{marcaId}", "abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(marcaService);
    }

    // createNewMarca()

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar201AoCriarMarca() throws Exception {
        // Arrange
        MarcaRequestDTO request = MarcaFixture.marcaRequestDTO();
        MarcaResponseDTO response = MarcaFixture.marcaResponseDTO();
        when(marcaService.createNewMarca(request)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/marcas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/marcas/" + response.getId()))
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.nome").value(response.getNome()));

        verify(marcaService).createNewMarca(request);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400AoCriarMarcaComNomeEmBranco() throws Exception {
        // Arrange
        MarcaRequestDTO request = new MarcaRequestDTO(" ");

        // Act & Assert
        mockMvc.perform(post("/api/v1/marcas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(marcaService);
    }

    // updateMarcaById()

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoAtualizarMarca() throws Exception {
        // Arrange
        MarcaRequestDTO request = MarcaFixture.marcaRequestDTO();
        MarcaResponseDTO response = MarcaFixture.marcaResponseDTO();
        when(marcaService.updateMarcaById(1, request)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/marcas/{marcaId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.nome").value(response.getNome()));

        verify(marcaService).updateMarcaById(1, request);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400AoAtualizarMarcaComNomeEmBranco() throws Exception {
        // Arrange
        MarcaRequestDTO request = new MarcaRequestDTO("");

        // Act & Assert
        mockMvc.perform(put("/api/v1/marcas/{marcaId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(marcaService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404AoAtualizarMarcaInexistente() throws Exception {
        // Arrange
        MarcaRequestDTO request = MarcaFixture.marcaRequestDTO();
        when(marcaService.updateMarcaById(99, request))
                .thenThrow(new RecursoNaoEncontradoException("Marca com id 99 não encontrado"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/marcas/{marcaId}", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(marcaService).updateMarcaById(99, request);
    }

    // deleteMarcaById()

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar204AoExcluirMarca() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/marcas/{marcaId}", 1))
                .andExpect(status().isNoContent());

        verify(marcaService).deleteMarcaById(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404AoExcluirMarcaInexistente() throws Exception {
        // Arrange
        doThrow(new RecursoNaoEncontradoException("Marca com id 99 não encontrado"))
                .when(marcaService).deleteMarcaById(99);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/marcas/{marcaId}", 99))
                .andExpect(status().isNotFound());

        verify(marcaService).deleteMarcaById(99);
    }
}
