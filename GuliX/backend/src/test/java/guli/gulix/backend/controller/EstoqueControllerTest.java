package guli.gulix.backend.controller;

import guli.gulix.backend.dto.EstoqueRequestDTO;
import guli.gulix.backend.dto.EstoqueRequestInventarioDTO;
import guli.gulix.backend.dto.EstoqueResponseDTO;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.exception.RegraNegocioException;
import guli.gulix.backend.fixture.EstoqueFixture;
import guli.gulix.backend.repository.UsuarioRepository;
import guli.gulix.backend.security.JwtUtil;
import guli.gulix.backend.service.EstoqueService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EstoqueController.class)
class EstoqueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EstoqueService estoqueService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    // getAllEstoque()

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoBuscarTodosOsEstoques() throws Exception {
        EstoqueResponseDTO estoqueEsperado = EstoqueFixture.estoqueResponseDTO();
        when(estoqueService.getAllEstoque()).thenReturn(List.of(estoqueEsperado));

        mockMvc.perform(get("/api/v1/estoques"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].produtoId").value(estoqueEsperado.getProdutoId()))
                .andExpect(jsonPath("$[0].estoqueTotal").value(estoqueEsperado.getEstoqueTotal()))
                .andExpect(jsonPath("$[0].estoqueReservado").value(estoqueEsperado.getEstoqueReservado()))
                .andExpect(jsonPath("$[0].estoqueDisponivel").value(estoqueEsperado.getEstoqueDisponivel()));

        verify(estoqueService).getAllEstoque();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200ComListaVaziaQuandoNaoExistiremEstoques() throws Exception {
        when(estoqueService.getAllEstoque()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/estoques"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(estoqueService).getAllEstoque();
    }

    // getEstoqueByProdutoId()

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoBuscarEstoquePorProdutoId() throws Exception {
        EstoqueResponseDTO estoqueEsperado = EstoqueFixture.estoqueResponseDTO();
        when(estoqueService.getEstoqueByProdutoId(1)).thenReturn(estoqueEsperado);

        mockMvc.perform(get("/api/v1/estoques/{produtoId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoId").value(estoqueEsperado.getProdutoId()))
                .andExpect(jsonPath("$.estoqueTotal").value(estoqueEsperado.getEstoqueTotal()))
                .andExpect(jsonPath("$.estoqueReservado").value(estoqueEsperado.getEstoqueReservado()))
                .andExpect(jsonPath("$.estoqueDisponivel").value(estoqueEsperado.getEstoqueDisponivel()));

        verify(estoqueService).getEstoqueByProdutoId(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404QuandoEstoqueNaoExistirAoBuscarPorProdutoId() throws Exception {
        when(estoqueService.getEstoqueByProdutoId(99))
                .thenThrow(new RecursoNaoEncontradoException("Estoque do produto de id 99 não encontrado"));

        mockMvc.perform(get("/api/v1/estoques/{produtoId}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(estoqueService).getEstoqueByProdutoId(99);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400QuandoProdutoIdNaoForNumerico() throws Exception {
        mockMvc.perform(get("/api/v1/estoques/{produtoId}", "abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(estoqueService);
    }

    // adicionarEstoque()

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoAdicionarEstoque() throws Exception {
        EstoqueRequestDTO request = EstoqueFixture.estoqueRequestDTO();
        EstoqueResponseDTO response = EstoqueFixture.estoqueResponseDTO();
        when(estoqueService.adicionarEstoque(1, request)).thenReturn(response);

        mockMvc.perform(put("/api/v1/estoques/{produtoId}/entrada", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoId").value(response.getProdutoId()))
                .andExpect(jsonPath("$.estoqueTotal").value(response.getEstoqueTotal()))
                .andExpect(jsonPath("$.estoqueDisponivel").value(response.getEstoqueDisponivel()));

        verify(estoqueService).adicionarEstoque(1, request);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400QuandoQuantidadeDeEntradaForZero() throws Exception {
        EstoqueRequestDTO request = new EstoqueRequestDTO(0);

        mockMvc.perform(put("/api/v1/estoques/{produtoId}/entrada", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.quantidade").exists());

        verifyNoInteractions(estoqueService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400QuandoQuantidadeDeEntradaForNula() throws Exception {
        EstoqueRequestDTO request = new EstoqueRequestDTO(null);

        mockMvc.perform(put("/api/v1/estoques/{produtoId}/entrada", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.quantidade").exists());

        verifyNoInteractions(estoqueService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar404QuandoEstoqueNaoExistirAoAdicionar() throws Exception {
        EstoqueRequestDTO request = EstoqueFixture.estoqueRequestDTO();
        when(estoqueService.adicionarEstoque(99, request))
                .thenThrow(new RecursoNaoEncontradoException("Estoque do produto de id 99 não encontrado"));

        mockMvc.perform(put("/api/v1/estoques/{produtoId}/entrada", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(estoqueService).adicionarEstoque(99, request);
    }

    // removerEstoque()

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoRemoverEstoque() throws Exception {
        EstoqueRequestDTO request = EstoqueFixture.estoqueRequestDTO();
        EstoqueResponseDTO response = EstoqueFixture.estoqueResponseDTO();
        when(estoqueService.removerEstoque(1, request)).thenReturn(response);

        mockMvc.perform(put("/api/v1/estoques/{produtoId}/saida", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoId").value(response.getProdutoId()))
                .andExpect(jsonPath("$.estoqueTotal").value(response.getEstoqueTotal()));

        verify(estoqueService).removerEstoque(1, request);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400QuandoQuantidadeDeSaidaForNegativa() throws Exception {
        EstoqueRequestDTO request = new EstoqueRequestDTO(-1);

        mockMvc.perform(put("/api/v1/estoques/{produtoId}/saida", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.quantidade").exists());

        verifyNoInteractions(estoqueService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400QuandoRemocaoViolarRegraDeNegocio() throws Exception {
        EstoqueRequestDTO request = EstoqueFixture.estoqueRequestDTO();
        when(estoqueService.removerEstoque(1, request))
                .thenThrow(new RegraNegocioException("A quantidade solicitada para subtração é maior do que a quantidade em estoque"));

        mockMvc.perform(put("/api/v1/estoques/{produtoId}/saida", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(estoqueService).removerEstoque(1, request);
    }

    // realizarInventario()

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar200AoRealizarInventario() throws Exception {
        EstoqueRequestInventarioDTO request = new EstoqueRequestInventarioDTO(15);
        EstoqueResponseDTO response = EstoqueFixture.estoqueResponseDTO();
        when(estoqueService.realizarInventario(1, request)).thenReturn(response);

        mockMvc.perform(put("/api/v1/estoques/{produtoId}/inventario", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoId").value(response.getProdutoId()))
                .andExpect(jsonPath("$.estoqueTotal").value(response.getEstoqueTotal()));

        verify(estoqueService).realizarInventario(1, request);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400QuandoQuantidadeDoInventarioForNegativa() throws Exception {
        EstoqueRequestInventarioDTO request = new EstoqueRequestInventarioDTO(-1);

        mockMvc.perform(put("/api/v1/estoques/{produtoId}/inventario", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.quantidade").exists());

        verifyNoInteractions(estoqueService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400QuandoInventarioViolarRegraDeNegocio() throws Exception {
        EstoqueRequestInventarioDTO request = new EstoqueRequestInventarioDTO(1);
        when(estoqueService.realizarInventario(1, request))
                .thenThrow(new RegraNegocioException("A quantidade informada no inventário é menor do que a quantidade em reserva"));

        mockMvc.perform(put("/api/v1/estoques/{produtoId}/inventario", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(estoqueService).realizarInventario(1, request);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornar400QuandoCorpoDaRequisicaoForInvalido() throws Exception {
        mockMvc.perform(put("/api/v1/estoques/{produtoId}/inventario", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalido"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(estoqueService);
    }
}
