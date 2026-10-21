package guli.gulix.backend.controller;

import guli.gulix.backend.dto.EstadoCreateDTO;
import guli.gulix.backend.dto.EstadoResponseDTO;
import guli.gulix.backend.dto.EstadoUpdateDTO;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.fixture.EstadoFixture;
import guli.gulix.backend.repository.UsuarioRepository;
import guli.gulix.backend.security.JwtUtil;
import guli.gulix.backend.service.EstadoService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EstadoController.class)
class EstadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EstadoService estadoService;

    // Dependências usadas pelo filtro JWT carregado no contexto MVC.
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoListarEstados() throws Exception {
        EstadoResponseDTO estado = EstadoFixture.estadoResponseDTO();
        when(estadoService.getAllEstado()).thenReturn(List.of(estado));

        mockMvc.perform(get("/api/v1/estados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(estado.id()))
                .andExpect(jsonPath("$[0].nome").value(estado.nome()))
                .andExpect(jsonPath("$[0].sigla").value(estado.sigla()));

        verify(estadoService).getAllEstado();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornarListaVaziaQuandoNaoExistiremEstados() throws Exception {
        when(estadoService.getAllEstado()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/estados"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(estadoService).getAllEstado();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoBuscarEstadoPorId() throws Exception {
        EstadoResponseDTO estado = EstadoFixture.estadoResponseDTO();
        when(estadoService.getEstadoById(1)).thenReturn(estado);

        mockMvc.perform(get("/api/v1/estados/{estadoId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(estado.id()))
                .andExpect(jsonPath("$.nome").value(estado.nome()))
                .andExpect(jsonPath("$.sigla").value(estado.sigla()));

        verify(estadoService).getEstadoById(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404QuandoEstadoNaoForEncontrado() throws Exception {
        when(estadoService.getEstadoById(999))
                .thenThrow(new RecursoNaoEncontradoException("Estado não encontrado"));

        mockMvc.perform(get("/api/v1/estados/{estadoId}", 999))
                .andExpect(status().isNotFound());

        verify(estadoService).getEstadoById(999);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar201AoCadastrarEstado() throws Exception {
        EstadoCreateDTO dto = EstadoFixture.estadoCreateDTO();
        EstadoResponseDTO response = EstadoFixture.estadoResponseDTO();
        when(estadoService.createNewEstado(any(EstadoCreateDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/estados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/estados/1"))
                .andExpect(jsonPath("$.id").value(response.id()))
                .andExpect(jsonPath("$.nome").value(response.nome()))
                .andExpect(jsonPath("$.sigla").value(response.sigla()));

        verify(estadoService).createNewEstado(any(EstadoCreateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400AoCadastrarEstadoComDadosInvalidos() throws Exception {
        EstadoCreateDTO dto = new EstadoCreateDTO(" ", "SPS");

        mockMvc.perform(post("/api/v1/estados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(estadoService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoAtualizarEstado() throws Exception {
        EstadoUpdateDTO dto = EstadoFixture.estadoUpdateDTO();
        EstadoResponseDTO response = EstadoFixture.estadoResponseDTO();
        when(estadoService.updateEstadoById(eq(1), any(EstadoUpdateDTO.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/estados/{estadoId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id()))
                .andExpect(jsonPath("$.nome").value(response.nome()))
                .andExpect(jsonPath("$.sigla").value(response.sigla()));

        verify(estadoService).updateEstadoById(eq(1), any(EstadoUpdateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404AoAtualizarEstadoInexistente() throws Exception {
        EstadoUpdateDTO dto = EstadoFixture.estadoUpdateDTO();
        when(estadoService.updateEstadoById(eq(999), any(EstadoUpdateDTO.class)))
                .thenThrow(new RecursoNaoEncontradoException("Estado não encontrado"));

        mockMvc.perform(patch("/api/v1/estados/{estadoId}", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(estadoService).updateEstadoById(eq(999), any(EstadoUpdateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400AoAtualizarEstadoComSiglaInvalida() throws Exception {
        EstadoUpdateDTO dto = new EstadoUpdateDTO("São Paulo", "SPS");

        mockMvc.perform(patch("/api/v1/estados/{estadoId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(estadoService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar204AoExcluirEstado() throws Exception {
        doNothing().when(estadoService).deleteEstadoById(1);

        mockMvc.perform(delete("/api/v1/estados/{estadoId}", 1))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(estadoService).deleteEstadoById(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404AoExcluirEstadoInexistente() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Estado não encontrado"))
                .when(estadoService).deleteEstadoById(999);

        mockMvc.perform(delete("/api/v1/estados/{estadoId}", 999))
                .andExpect(status().isNotFound());

        verify(estadoService).deleteEstadoById(999);
    }
}
