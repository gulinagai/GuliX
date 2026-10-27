package guli.gulix.backend.controller;

import guli.gulix.backend.dto.CarrinhoResponseDTO;
import guli.gulix.backend.dto.ItemCarrinhoRequestDTO;
import guli.gulix.backend.dto.ItemCarrinhoUpdateDTO;
import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.exception.RegraNegocioException;
import guli.gulix.backend.fixture.CarrinhoFixture;
import guli.gulix.backend.fixture.UsuarioFixture;
import guli.gulix.backend.repository.UsuarioRepository;
import guli.gulix.backend.security.JwtUtil;
import guli.gulix.backend.service.CarrinhoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarrinhoController.class)
@Import(CarrinhoControllerTest.AuthenticationPrincipalTestConfig.class)
class CarrinhoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CarrinhoService carrinhoService;

    // Dependências utilizadas pelo filtro JWT carregado no contexto MVC.
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    private Usuario usuarioAutenticado;

    @BeforeEach
    void setup() {
        usuarioAutenticado = UsuarioFixture.usuario();
        when(jwtUtil.validarToken(anyString())).thenReturn(true);
        when(jwtUtil.extrairEmail(anyString())).thenReturn(usuarioAutenticado.getEmail());
        when(usuarioRepository.findByEmail(usuarioAutenticado.getEmail()))
                .thenReturn(Optional.of(usuarioAutenticado));
    }

    // buscarCarrinho()

    @Test
    void deveRetornar200AoConsultarCarrinho() throws Exception {
        CarrinhoResponseDTO response = CarrinhoFixture.carrinhoResponseDTO();

        when(carrinhoService.buscarCarrinho(usuarioAutenticado.getId())).thenReturn(response);

        mockMvc.perform(requisicaoAutenticada(get("/api/v1/carrinho")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(response.getTotal().doubleValue()))
                .andExpect(jsonPath("$.itens[0].id").value(response.getItens().get(0).getId()))
                .andExpect(jsonPath("$.itens[0].produtoId").value(response.getItens().get(0).getProdutoId()))
                .andExpect(jsonPath("$.itens[0].nomeProduto").value(response.getItens().get(0).getNomeProduto()))
                .andExpect(jsonPath("$.itens[0].preco").value(response.getItens().get(0).getPreco().doubleValue()))
                .andExpect(jsonPath("$.itens[0].quantidade").value(response.getItens().get(0).getQuantidade()));

        verify(carrinhoService).buscarCarrinho(usuarioAutenticado.getId());
    }

    @Test
    void deveRetornar200AoConsultarCarrinhoVazio() throws Exception {
        CarrinhoResponseDTO response = CarrinhoFixture.carrinhoResponseDTOVazio();

        when(carrinhoService.buscarCarrinho(usuarioAutenticado.getId())).thenReturn(response);

        mockMvc.perform(requisicaoAutenticada(get("/api/v1/carrinho")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens").isEmpty())
                .andExpect(jsonPath("$.total").value(0));

        verify(carrinhoService).buscarCarrinho(usuarioAutenticado.getId());
    }

    // adicionarItem()

    @Test
    void deveRetornar201AoAdicionarItemAoCarrinho() throws Exception {
        ItemCarrinhoRequestDTO request = CarrinhoFixture.itemCarrinhoRequestDTO();

        mockMvc.perform(requisicaoAutenticada(post("/api/v1/carrinho/itens"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(carrinhoService).adicionarItem(eq(usuarioAutenticado.getId()), any(ItemCarrinhoRequestDTO.class));
    }

    @Test
    void deveRetornar400AoAdicionarItemComQuantidadeInvalida() throws Exception {
        ItemCarrinhoRequestDTO request = new ItemCarrinhoRequestDTO(1, 0);

        mockMvc.perform(requisicaoAutenticada(post("/api/v1/carrinho/itens"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(carrinhoService);
    }

    @Test
    void deveRetornar400AoAdicionarItemSemProduto() throws Exception {
        ItemCarrinhoRequestDTO request = new ItemCarrinhoRequestDTO(null, 2);

        mockMvc.perform(requisicaoAutenticada(post("/api/v1/carrinho/itens"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(carrinhoService);
    }

    @Test
    void deveRetornar404AoAdicionarItemQuandoProdutoNaoExistir() throws Exception {
        ItemCarrinhoRequestDTO request = CarrinhoFixture.itemCarrinhoRequestDTO();

        doThrow(new RecursoNaoEncontradoException("Produto não encontrado"))
                .when(carrinhoService).adicionarItem(eq(usuarioAutenticado.getId()), any(ItemCarrinhoRequestDTO.class));

        mockMvc.perform(requisicaoAutenticada(post("/api/v1/carrinho/itens"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(carrinhoService).adicionarItem(eq(usuarioAutenticado.getId()), any(ItemCarrinhoRequestDTO.class));
    }

    @Test
    void deveRetornar400AoAdicionarItemQuandoEstoqueForInsuficiente() throws Exception {
        ItemCarrinhoRequestDTO request = CarrinhoFixture.itemCarrinhoRequestDTO();

        doThrow(new RegraNegocioException("Quantidade solicitada maior que a quantidade disponível em estoque"))
                .when(carrinhoService).adicionarItem(eq(usuarioAutenticado.getId()), any(ItemCarrinhoRequestDTO.class));

        mockMvc.perform(requisicaoAutenticada(post("/api/v1/carrinho/itens"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(carrinhoService).adicionarItem(eq(usuarioAutenticado.getId()), any(ItemCarrinhoRequestDTO.class));
    }

    // atualizarQuantidade()

    @Test
    void deveRetornar200AoAtualizarQuantidadeDoItem() throws Exception {
        ItemCarrinhoUpdateDTO request = CarrinhoFixture.itemCarrinhoUpdateDTO();

        mockMvc.perform(requisicaoAutenticada(put("/api/v1/carrinho/itens/{itemId}", 1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(carrinhoService).atualizarQuantidade(
                eq(usuarioAutenticado.getId()),
                eq(1),
                any(ItemCarrinhoUpdateDTO.class)
        );
    }

    @Test
    void deveRetornar400AoAtualizarItemComQuantidadeInvalida() throws Exception {
        ItemCarrinhoUpdateDTO request = new ItemCarrinhoUpdateDTO(0);

        mockMvc.perform(requisicaoAutenticada(put("/api/v1/carrinho/itens/{itemId}", 1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(carrinhoService);
    }

    @Test
    void deveRetornar400QuandoIdDoItemForInvalidoAoAtualizar() throws Exception {
        ItemCarrinhoUpdateDTO request = CarrinhoFixture.itemCarrinhoUpdateDTO();

        mockMvc.perform(requisicaoAutenticada(put("/api/v1/carrinho/itens/{itemId}", "abc"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(carrinhoService);
    }

    @Test
    void deveRetornar404AoAtualizarItemInexistente() throws Exception {
        ItemCarrinhoUpdateDTO request = CarrinhoFixture.itemCarrinhoUpdateDTO();

        doThrow(new RecursoNaoEncontradoException("Item do carrinho de id 99 não encontrado no carrinho."))
                .when(carrinhoService).atualizarQuantidade(
                        eq(usuarioAutenticado.getId()),
                        eq(99),
                        any(ItemCarrinhoUpdateDTO.class)
                );

        mockMvc.perform(requisicaoAutenticada(put("/api/v1/carrinho/itens/{itemId}", 99))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(carrinhoService).atualizarQuantidade(
                eq(usuarioAutenticado.getId()),
                eq(99),
                any(ItemCarrinhoUpdateDTO.class)
        );
    }

    // removerItem()

    @Test
    void deveRetornar204AoRemoverItemDoCarrinho() throws Exception {
        mockMvc.perform(requisicaoAutenticada(delete("/api/v1/carrinho/itens/{itemId}", 1)))
                .andExpect(status().isNoContent());

        verify(carrinhoService).removerItem(usuarioAutenticado.getId(), 1);
    }

    @Test
    void deveRetornar404AoRemoverItemInexistente() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Item do carrinho de id 99 não encontrado no carrinho."))
                .when(carrinhoService).removerItem(usuarioAutenticado.getId(), 99);

        mockMvc.perform(requisicaoAutenticada(delete("/api/v1/carrinho/itens/{itemId}", 99)))
                .andExpect(status().isNotFound());

        verify(carrinhoService).removerItem(usuarioAutenticado.getId(), 99);
    }

    @Test
    void deveRetornar400QuandoIdDoItemForInvalidoAoRemover() throws Exception {
        mockMvc.perform(requisicaoAutenticada(delete("/api/v1/carrinho/itens/{itemId}", "abc")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(carrinhoService);
    }

    private MockHttpServletRequestBuilder requisicaoAutenticada(MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer token-teste");
    }

    @TestConfiguration
    static class AuthenticationPrincipalTestConfig implements WebMvcConfigurer {

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new AuthenticationPrincipalArgumentResolver());
        }
    }
}
