package guli.gulix.backend.controller;

import guli.gulix.backend.dto.PoliticaFreteCreateDTO;
import guli.gulix.backend.dto.PoliticaFreteResponseDTO;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.fixture.PoliticaFreteFixture;
import guli.gulix.backend.repository.UsuarioRepository;
import guli.gulix.backend.security.JwtUtil;
import guli.gulix.backend.service.PoliticaFreteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PoliticaFreteController.class)
class PoliticaFreteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PoliticaFreteService politicaFreteService;

    // Dependências usadas pelo filtro JWT carregado no contexto MVC.
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoListarPoliticasDeFrete() throws Exception {
        PoliticaFreteResponseDTO politica = PoliticaFreteFixture.politicaFreteResponseDTO();
        when(politicaFreteService.getAllPoliticaFrete()).thenReturn(List.of(politica));

        mockMvc.perform(get("/api/v1/politicas-frete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(politica.id()))
                .andExpect(jsonPath("$[0].estadoId").value(politica.estadoId()))
                .andExpect(jsonPath("$[0].estadoSigla").value(politica.estadoSigla()))
                .andExpect(jsonPath("$[0].valorBase").value(politica.valorBase().doubleValue()))
                .andExpect(jsonPath("$[0].ativo").value(politica.ativo()));

        verify(politicaFreteService).getAllPoliticaFrete();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornarListaVaziaQuandoNaoExistiremPoliticasDeFrete() throws Exception {
        when(politicaFreteService.getAllPoliticaFrete()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/politicas-frete"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(politicaFreteService).getAllPoliticaFrete();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoBuscarPoliticaDeFretePorId() throws Exception {
        PoliticaFreteResponseDTO politica = PoliticaFreteFixture.politicaFreteResponseDTO();
        when(politicaFreteService.getPoliticaFreteById(1)).thenReturn(politica);

        mockMvc.perform(get("/api/v1/politicas-frete/{politicaFreteId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(politica.id()))
                .andExpect(jsonPath("$.estadoId").value(politica.estadoId()))
                .andExpect(jsonPath("$.estadoSigla").value(politica.estadoSigla()))
                .andExpect(jsonPath("$.valorBase").value(politica.valorBase().doubleValue()))
                .andExpect(jsonPath("$.ativo").value(politica.ativo()));

        verify(politicaFreteService).getPoliticaFreteById(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404QuandoPoliticaDeFreteNaoForEncontrada() throws Exception {
        when(politicaFreteService.getPoliticaFreteById(999))
                .thenThrow(new RecursoNaoEncontradoException("Política de frete não encontrada"));

        mockMvc.perform(get("/api/v1/politicas-frete/{politicaFreteId}", 999))
                .andExpect(status().isNotFound());

        verify(politicaFreteService).getPoliticaFreteById(999);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar201AoCadastrarPoliticaDeFrete() throws Exception {
        PoliticaFreteCreateDTO dto = PoliticaFreteFixture.politicaFreteCreateDTO();
        PoliticaFreteResponseDTO response = PoliticaFreteFixture.politicaFreteResponseDTO();
        when(politicaFreteService.createNewPoliticaFrete(any(PoliticaFreteCreateDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/politicas-frete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/politicas-frete/1"))
                .andExpect(jsonPath("$.id").value(response.id()))
                .andExpect(jsonPath("$.estadoId").value(response.estadoId()))
                .andExpect(jsonPath("$.estadoSigla").value(response.estadoSigla()))
                .andExpect(jsonPath("$.valorBase").value(response.valorBase().doubleValue()))
                .andExpect(jsonPath("$.ativo").value(response.ativo()));

        verify(politicaFreteService).createNewPoliticaFrete(any(PoliticaFreteCreateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400AoCadastrarPoliticaDeFreteComEstadoNulo() throws Exception {
        PoliticaFreteCreateDTO dto = new PoliticaFreteCreateDTO(
                null,
                new BigDecimal("15.00")
        );

        mockMvc.perform(post("/api/v1/politicas-frete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(politicaFreteService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400AoCadastrarPoliticaDeFreteComValorBaseZero() throws Exception {
        PoliticaFreteCreateDTO dto = new PoliticaFreteCreateDTO(
                1,
                BigDecimal.ZERO
        );

        mockMvc.perform(post("/api/v1/politicas-frete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(politicaFreteService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400AoCadastrarPoliticaDeFreteComValorBaseNegativo() throws Exception {
        PoliticaFreteCreateDTO dto = new PoliticaFreteCreateDTO(
                1,
                new BigDecimal("-5.00")
        );

        mockMvc.perform(post("/api/v1/politicas-frete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(politicaFreteService);
    }
}
