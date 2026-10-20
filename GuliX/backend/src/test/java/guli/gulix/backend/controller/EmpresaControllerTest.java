package guli.gulix.backend.controller;

import guli.gulix.backend.dto.EmpresaResponseDTO;
import guli.gulix.backend.dto.EmpresaUpdateDTO;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.fixture.EmpresaFixture;
import guli.gulix.backend.repository.UsuarioRepository;
import guli.gulix.backend.security.JwtUtil;
import guli.gulix.backend.service.EmpresaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmpresaController.class)
class EmpresaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmpresaService empresaService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;


    // getEmpresa()

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoBuscarEmpresa() throws Exception {

        // Arrange

        EmpresaResponseDTO empresaEsperada = EmpresaFixture.empresaResponseDTO();

        when(empresaService.getEmpresa()).thenReturn(empresaEsperada);

        // Act & Assert

        mockMvc.perform(get("/api/v1/empresas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(empresaEsperada.id()))
                .andExpect(jsonPath("$.razaoSocial").value(empresaEsperada.razaoSocial()))
                .andExpect(jsonPath("$.nomeFantasia").value(empresaEsperada.nomeFantasia()))
                .andExpect(jsonPath("$.cnpj").value(empresaEsperada.cnpj()))
                .andExpect(jsonPath("$.valorPorKmFrete")
                        .value(empresaEsperada.valorPorKmFrete().doubleValue()));

        verify(empresaService).getEmpresa();
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404QuandoEmpresaNaoEncontradaAoBuscar() throws Exception {

        // Arrange

        when(empresaService.getEmpresa())
                .thenThrow(new RecursoNaoEncontradoException("Empresa não encontrada"));

        // Act & Assert

        mockMvc.perform(get("/api/v1/empresas"))
                .andExpect(status().isNotFound());

        verify(empresaService).getEmpresa();
    }


    // updateEmpresa(EmpresaUpdateDTO dto)

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoAtualizarEmpresa() throws Exception {

        // Arrange

        EmpresaUpdateDTO empresaUpdateDTO = EmpresaFixture.empresaUpdateDTO();

        EmpresaResponseDTO empresaAtualizada = new EmpresaResponseDTO(
                EmpresaFixture.empresa().getId(),
                empresaUpdateDTO.razaoSocial(),
                empresaUpdateDTO.nomeFantasia(),
                empresaUpdateDTO.cnpj(),
                empresaUpdateDTO.valorPorKmFrete()
        );

        when(empresaService.updateEmpresa(any(EmpresaUpdateDTO.class)))
                .thenReturn(empresaAtualizada);

        // Act & Assert

        mockMvc.perform(patch("/api/v1/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(empresaAtualizada.id()))
                .andExpect(jsonPath("$.razaoSocial").value(empresaAtualizada.razaoSocial()))
                .andExpect(jsonPath("$.nomeFantasia").value(empresaAtualizada.nomeFantasia()))
                .andExpect(jsonPath("$.cnpj").value(empresaAtualizada.cnpj()))
                .andExpect(jsonPath("$.valorPorKmFrete")
                        .value(empresaAtualizada.valorPorKmFrete().doubleValue()));

        verify(empresaService).updateEmpresa(any(EmpresaUpdateDTO.class));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404QuandoEmpresaNaoEncontradaAoAtualizar() throws Exception {

        // Arrange

        EmpresaUpdateDTO empresaUpdateDTO = EmpresaFixture.empresaUpdateDTO();

        when(empresaService.updateEmpresa(any(EmpresaUpdateDTO.class)))
                .thenThrow(new RecursoNaoEncontradoException("Empresa não encontrada"));

        // Act & Assert

        mockMvc.perform(patch("/api/v1/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaUpdateDTO)))
                .andExpect(status().isNotFound());

        verify(empresaService).updateEmpresa(any(EmpresaUpdateDTO.class));
    }

}

