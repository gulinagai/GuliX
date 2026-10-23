package guli.gulix.backend.controller;

import guli.gulix.backend.dto.CidadeCreateDTO;
import guli.gulix.backend.dto.CidadeResponseDTO;
import guli.gulix.backend.dto.CidadeUpdateDTO;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.fixture.CidadeFixture;
import guli.gulix.backend.repository.UsuarioRepository;
import guli.gulix.backend.security.JwtUtil;
import guli.gulix.backend.service.CidadeService;
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

@WebMvcTest(CidadeController.class)
class CidadeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CidadeService cidadeService;

    // Dependências usadas pelo filtro JWT carregado no contexto MVC.
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoListarCidades() throws Exception {
        CidadeResponseDTO cidade = CidadeFixture.cidadeResponseDTO();
        when(cidadeService.getAllCidade()).thenReturn(List.of(cidade));

        mockMvc.perform(get("/api/v1/cidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(cidade.id()))
                .andExpect(jsonPath("$[0].nome").value(cidade.nome()))
                .andExpect(jsonPath("$[0].estadoId").value(cidade.estadoId()))
                .andExpect(jsonPath("$[0].latitudeCentral").value(cidade.latitudeCentral().doubleValue()))
                .andExpect(jsonPath("$[0].longitudeCentral").value(cidade.longitudeCentral().doubleValue()));

        verify(cidadeService).getAllCidade();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornarListaVaziaQuandoNaoExistiremCidades() throws Exception {
        when(cidadeService.getAllCidade()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/cidades"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(cidadeService).getAllCidade();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoBuscarCidadePorId() throws Exception {
        CidadeResponseDTO cidade = CidadeFixture.cidadeResponseDTO();
        when(cidadeService.getCidadeById(1)).thenReturn(cidade);

        mockMvc.perform(get("/api/v1/cidades/{cidadeId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cidade.id()))
                .andExpect(jsonPath("$.nome").value(cidade.nome()))
                .andExpect(jsonPath("$.estadoId").value(cidade.estadoId()))
                .andExpect(jsonPath("$.latitudeCentral").value(cidade.latitudeCentral().doubleValue()))
                .andExpect(jsonPath("$.longitudeCentral").value(cidade.longitudeCentral().doubleValue()));

        verify(cidadeService).getCidadeById(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404QuandoCidadeNaoForEncontrada() throws Exception {
        when(cidadeService.getCidadeById(999))
                .thenThrow(new RecursoNaoEncontradoException("Cidade não encontrada"));

        mockMvc.perform(get("/api/v1/cidades/{cidadeId}", 999))
                .andExpect(status().isNotFound());

        verify(cidadeService).getCidadeById(999);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar201AoCadastrarCidade() throws Exception {
        CidadeCreateDTO dto = CidadeFixture.cidadeCreateDTO();
        CidadeResponseDTO response = CidadeFixture.cidadeResponseDTO();
        when(cidadeService.createNewCidade(any(CidadeCreateDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/cidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/cidades/1"))
                .andExpect(jsonPath("$.id").value(response.id()))
                .andExpect(jsonPath("$.nome").value(response.nome()))
                .andExpect(jsonPath("$.estadoId").value(response.estadoId()));

        verify(cidadeService).createNewCidade(any(CidadeCreateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400AoCadastrarCidadeComDadosInvalidos() throws Exception {
        CidadeCreateDTO dto = new CidadeCreateDTO(
                " ",
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/v1/cidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cidadeService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400AoCadastrarCidadeComLatitudeForaDoIntervalo() throws Exception {
        CidadeCreateDTO dto = new CidadeCreateDTO(
                "São Paulo",
                1,
                new java.math.BigDecimal("91"),
                new java.math.BigDecimal("-46.633308")
        );

        mockMvc.perform(post("/api/v1/cidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cidadeService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoAtualizarCidade() throws Exception {
        CidadeUpdateDTO dto = CidadeFixture.cidadeUpdateDTO();
        CidadeResponseDTO response = CidadeFixture.cidadeResponseDTO();
        when(cidadeService.updateCidadeById(eq(1), any(CidadeUpdateDTO.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/cidades/{cidadeId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id()))
                .andExpect(jsonPath("$.nome").value(response.nome()))
                .andExpect(jsonPath("$.estadoId").value(response.estadoId()));

        verify(cidadeService).updateCidadeById(eq(1), any(CidadeUpdateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404AoAtualizarCidadeInexistente() throws Exception {
        CidadeUpdateDTO dto = CidadeFixture.cidadeUpdateDTO();
        when(cidadeService.updateCidadeById(eq(999), any(CidadeUpdateDTO.class)))
                .thenThrow(new RecursoNaoEncontradoException("Cidade não encontrada"));

        mockMvc.perform(patch("/api/v1/cidades/{cidadeId}", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(cidadeService).updateCidadeById(eq(999), any(CidadeUpdateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400AoAtualizarCidadeComNomeMuitoLongo() throws Exception {
        CidadeUpdateDTO dto = new CidadeUpdateDTO(
                "A".repeat(151),
                null,
                null,
                null
        );

        mockMvc.perform(patch("/api/v1/cidades/{cidadeId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cidadeService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar204AoExcluirCidade() throws Exception {
        doNothing().when(cidadeService).deleteCidadeById(1);

        mockMvc.perform(delete("/api/v1/cidades/{cidadeId}", 1))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(cidadeService).deleteCidadeById(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404AoExcluirCidadeInexistente() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Cidade não encontrada"))
                .when(cidadeService).deleteCidadeById(999);

        mockMvc.perform(delete("/api/v1/cidades/{cidadeId}", 999))
                .andExpect(status().isNotFound());

        verify(cidadeService).deleteCidadeById(999);
    }
}
