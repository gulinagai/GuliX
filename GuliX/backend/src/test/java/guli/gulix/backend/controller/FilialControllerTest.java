package guli.gulix.backend.controller;

import guli.gulix.backend.dto.FilialResponseDTO;
import guli.gulix.backend.dto.FilialUpdateDTO;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.fixture.FilialFixture;
import guli.gulix.backend.repository.UsuarioRepository;
import guli.gulix.backend.security.JwtUtil;
import guli.gulix.backend.service.FilialService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilialController.class)
class FilialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FilialService filialService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    // getFilial()

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoBuscarFilial() throws Exception {
        // Arrange
        FilialResponseDTO filialEsperada = FilialFixture.filialResponseDTO();
        when(filialService.getFilial()).thenReturn(filialEsperada);

        // Act & Assert
        mockMvc.perform(get("/api/v1/filiais"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(filialEsperada.id()))
                .andExpect(jsonPath("$.nome").value(filialEsperada.nome()))
                .andExpect(jsonPath("$.ativo").value(filialEsperada.ativo()))
                .andExpect(jsonPath("$.endereco.rua").value(filialEsperada.endereco().rua()))
                .andExpect(jsonPath("$.endereco.numero").value(filialEsperada.endereco().numero()))
                .andExpect(jsonPath("$.endereco.cidade").value(filialEsperada.endereco().cidade()))
                .andExpect(jsonPath("$.endereco.estado").value(filialEsperada.endereco().estado()))
                .andExpect(jsonPath("$.endereco.cep").value(filialEsperada.endereco().cep()));

        verify(filialService).getFilial();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404QuandoFilialNaoEncontradaAoBuscar() throws Exception {
        // Arrange
        when(filialService.getFilial())
                .thenThrow(new RecursoNaoEncontradoException("Filial não encontrada"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/filiais"))
                .andExpect(status().isNotFound());

        verify(filialService).getFilial();
    }

    // updateFilial(FilialUpdateDTO dto)

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoAtualizarFilial() throws Exception {
        // Arrange
        FilialUpdateDTO filialUpdateDTO = FilialFixture.filialUpdateDTO();
        FilialResponseDTO filialAtualizada = FilialFixture.filialResponseDTO();
        when(filialService.updateFilial(any(FilialUpdateDTO.class))).thenReturn(filialAtualizada);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/filiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filialUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(filialAtualizada.id()))
                .andExpect(jsonPath("$.nome").value(filialAtualizada.nome()))
                .andExpect(jsonPath("$.ativo").value(filialAtualizada.ativo()));

        verify(filialService).updateFilial(any(FilialUpdateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400AoAtualizarFilialComNomeInvalido() throws Exception {
        // Arrange
        FilialUpdateDTO filialUpdateDTO = new FilialUpdateDTO(
                " ", FilialFixture.enderecoFilialDTO(), true
        );

        // Act & Assert
        mockMvc.perform(patch("/api/v1/filiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filialUpdateDTO)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(filialService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404QuandoFilialNaoEncontradaAoAtualizar() throws Exception {
        // Arrange
        FilialUpdateDTO filialUpdateDTO = FilialFixture.filialUpdateDTO();
        when(filialService.updateFilial(any(FilialUpdateDTO.class)))
                .thenThrow(new RecursoNaoEncontradoException("Filial não encontrada"));

        // Act & Assert
        mockMvc.perform(patch("/api/v1/filiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filialUpdateDTO)))
                .andExpect(status().isNotFound());

        verify(filialService).updateFilial(any(FilialUpdateDTO.class));
    }
}
