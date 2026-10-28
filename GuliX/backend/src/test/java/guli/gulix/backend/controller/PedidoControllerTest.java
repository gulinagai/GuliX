package guli.gulix.backend.controller;

import guli.gulix.backend.dto.PedidoCreateDTO;
import guli.gulix.backend.dto.PedidoResponseDTO;
import guli.gulix.backend.dto.PedidoUpdateStatusDTO;
import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.entity.enums.MetodoPagamento;
import guli.gulix.backend.entity.enums.StatusPedido;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.exception.RegraNegocioException;
import guli.gulix.backend.fixture.PedidoFixture;
import guli.gulix.backend.fixture.UsuarioFixture;
import guli.gulix.backend.repository.UsuarioRepository;
import guli.gulix.backend.security.JwtUtil;
import guli.gulix.backend.service.PedidoService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
@Import(PedidoControllerTest.AuthenticationPrincipalTestConfig.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PedidoService pedidoService;

    // Dependências utilizadas pelo filtro JWT carregado no contexto MVC.
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    private Usuario usuarioAutenticado;

    @BeforeEach
    void setup() {
        autenticar(UsuarioFixture.usuario());
    }

    // getListPedidos()

    @Test
    void deveRetornar200AoListarPedidosDoUsuario() throws Exception {
        PedidoResponseDTO response = PedidoFixture.pedidoResponseDTO();

        when(pedidoService.getListPedidos(usuarioAutenticado)).thenReturn(List.of(response));

        mockMvc.perform(requisicaoAutenticada(get("/api/v1/pedidos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(response.getId()))
                .andExpect(jsonPath("$[0].usuarioId").value(response.getUsuarioId()))
                .andExpect(jsonPath("$[0].statusPedido").value(response.getStatusPedido().name()))
                .andExpect(jsonPath("$[0].subtotal").value(response.getSubtotal().doubleValue()))
                .andExpect(jsonPath("$[0].valorFrete").value(response.getValorFrete().doubleValue()))
                .andExpect(jsonPath("$[0].total").value(response.getTotal().doubleValue()))
                .andExpect(jsonPath("$[0].enderecoEntrega.rua").value(response.getEnderecoEntrega().getRua()))
                .andExpect(jsonPath("$[0].itens[0].produtoId").value(response.getItens().get(0).getProdutoId()))
                .andExpect(jsonPath("$[0].itens[0].nomeProduto").value(response.getItens().get(0).getNomeProduto()))
                .andExpect(jsonPath("$[0].itens[0].quantidade").value(response.getItens().get(0).getQuantidade()))
                .andExpect(jsonPath("$[0].itens[0].precoUnitario").value(response.getItens().get(0).getPrecoUnitario().doubleValue()));

        verify(pedidoService).getListPedidos(usuarioAutenticado);
    }

    @Test
    void deveRetornar200ComListaVaziaQuandoUsuarioNaoPossuirPedidos() throws Exception {
        when(pedidoService.getListPedidos(usuarioAutenticado)).thenReturn(List.of());

        mockMvc.perform(requisicaoAutenticada(get("/api/v1/pedidos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(pedidoService).getListPedidos(usuarioAutenticado);
    }

    // getPedidoById()

    @Test
    void deveRetornar200AoConsultarPedidoPorId() throws Exception {
        PedidoResponseDTO response = PedidoFixture.pedidoResponseDTO();

        when(pedidoService.getPedidoById(usuarioAutenticado, 1)).thenReturn(response);

        mockMvc.perform(requisicaoAutenticada(get("/api/v1/pedidos/{pedidoId}", 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.usuarioId").value(response.getUsuarioId()))
                .andExpect(jsonPath("$.statusPedido").value(response.getStatusPedido().name()))
                .andExpect(jsonPath("$.subtotal").value(response.getSubtotal().doubleValue()))
                .andExpect(jsonPath("$.valorFrete").value(response.getValorFrete().doubleValue()))
                .andExpect(jsonPath("$.total").value(response.getTotal().doubleValue()))
                .andExpect(jsonPath("$.enderecoEntrega.cidade").value(response.getEnderecoEntrega().getCidade()))
                .andExpect(jsonPath("$.itens[0].nomeProduto").value(response.getItens().get(0).getNomeProduto()))
                .andExpect(jsonPath("$.pagamento.id").value(response.getPagamento().getId()))
                .andExpect(jsonPath("$.pagamento.checkoutUrl").value(response.getPagamento().getCheckoutUrl()));

        verify(pedidoService).getPedidoById(usuarioAutenticado, 1);
    }

    @Test
    void deveRetornar404AoConsultarPedidoInexistente() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Pedido não encontrado"))
                .when(pedidoService).getPedidoById(usuarioAutenticado, 99);

        mockMvc.perform(requisicaoAutenticada(get("/api/v1/pedidos/{pedidoId}", 99)))
                .andExpect(status().isNotFound());

        verify(pedidoService).getPedidoById(usuarioAutenticado, 99);
    }

    @Test
    void deveRetornar400QuandoIdDoPedidoForInvalido() throws Exception {
        mockMvc.perform(requisicaoAutenticada(get("/api/v1/pedidos/{pedidoId}", "abc")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pedidoService);
    }

    // createNewPedido()

    @Test
    void deveRetornar201AoCriarPedido() throws Exception {
        PedidoCreateDTO request = PedidoFixture.pedidoCreateDTO();
        PedidoResponseDTO response = PedidoFixture.pedidoResponseDTO();

        when(pedidoService.createNewPedido(eq(usuarioAutenticado), any(PedidoCreateDTO.class))).thenReturn(response);

        mockMvc.perform(requisicaoAutenticada(post("/api/v1/pedidos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/pedidos/" + response.getId()))
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.statusPedido").value(response.getStatusPedido().name()))
                .andExpect(jsonPath("$.total").value(response.getTotal().doubleValue()))
                .andExpect(jsonPath("$.pagamento.checkoutUrl").value(response.getPagamento().getCheckoutUrl()));

        verify(pedidoService).createNewPedido(eq(usuarioAutenticado), any(PedidoCreateDTO.class));
    }

    @Test
    void deveRetornar400AoCriarPedidoSemEndereco() throws Exception {
        PedidoCreateDTO request = new PedidoCreateDTO(null, MetodoPagamento.CARTAO_CREDITO, 3);

        mockMvc.perform(requisicaoAutenticada(post("/api/v1/pedidos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.enderecoId").exists());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void deveRetornar400AoCriarPedidoComEnderecoInvalido() throws Exception {
        PedidoCreateDTO request = new PedidoCreateDTO(0, MetodoPagamento.CARTAO_CREDITO, 3);

        mockMvc.perform(requisicaoAutenticada(post("/api/v1/pedidos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.enderecoId").exists());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void deveRetornar400AoCriarPedidoSemMetodoDePagamento() throws Exception {
        PedidoCreateDTO request = new PedidoCreateDTO(5, null, 3);

        mockMvc.perform(requisicaoAutenticada(post("/api/v1/pedidos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.metodoPagamento").exists());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void deveRetornar400AoCriarPedidoComNumeroDeParcelasZero() throws Exception {
        PedidoCreateDTO request = new PedidoCreateDTO(5, MetodoPagamento.CARTAO_CREDITO, 0);

        mockMvc.perform(requisicaoAutenticada(post("/api/v1/pedidos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.numeroParcelas").exists());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void deveRetornar400AoCriarPedidoComNumeroDeParcelasAcimaDoLimite() throws Exception {
        PedidoCreateDTO request = new PedidoCreateDTO(5, MetodoPagamento.CARTAO_CREDITO, 49);

        mockMvc.perform(requisicaoAutenticada(post("/api/v1/pedidos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.numeroParcelas").exists());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void deveRetornar404AoCriarPedidoQuandoCarrinhoOuEnderecoNaoExistir() throws Exception {
        PedidoCreateDTO request = PedidoFixture.pedidoCreateDTO();

        doThrow(new RecursoNaoEncontradoException("Carrinho não encontrado"))
                .when(pedidoService).createNewPedido(eq(usuarioAutenticado), any(PedidoCreateDTO.class));

        mockMvc.perform(requisicaoAutenticada(post("/api/v1/pedidos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(pedidoService).createNewPedido(eq(usuarioAutenticado), any(PedidoCreateDTO.class));
    }

    @Test
    void deveRetornar400AoCriarPedidoQuandoEstoqueForInsuficiente() throws Exception {
        PedidoCreateDTO request = PedidoFixture.pedidoCreateDTO();

        doThrow(new RegraNegocioException("Quantidade solicitada maior que a quantidade disponível em estoque"))
                .when(pedidoService).createNewPedido(eq(usuarioAutenticado), any(PedidoCreateDTO.class));

        mockMvc.perform(requisicaoAutenticada(post("/api/v1/pedidos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(pedidoService).createNewPedido(eq(usuarioAutenticado), any(PedidoCreateDTO.class));
    }

    // cancelPedido()

    @Test
    void deveRetornar200AoCancelarPedido() throws Exception {
        PedidoResponseDTO response = PedidoFixture.pedidoResponseDTO();
        response.setStatusPedido(StatusPedido.CANCELADO);

        when(pedidoService.cancelPedido(1, usuarioAutenticado)).thenReturn(response);

        mockMvc.perform(requisicaoAutenticada(patch("/api/v1/pedidos/{pedidoId}/cancelar", 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.statusPedido").value(StatusPedido.CANCELADO.name()));

        verify(pedidoService).cancelPedido(1, usuarioAutenticado);
    }

    @Test
    void deveRetornar404AoCancelarPedidoInexistente() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Pedido não encontrado"))
                .when(pedidoService).cancelPedido(99, usuarioAutenticado);

        mockMvc.perform(requisicaoAutenticada(patch("/api/v1/pedidos/{pedidoId}/cancelar", 99)))
                .andExpect(status().isNotFound());

        verify(pedidoService).cancelPedido(99, usuarioAutenticado);
    }

    @Test
    void deveRetornar400AoCancelarPedidoQueNaoPodeSerCancelado() throws Exception {
        doThrow(new RegraNegocioException("Somente pedidos pendentes podem ser cancelados."))
                .when(pedidoService).cancelPedido(1, usuarioAutenticado);

        mockMvc.perform(requisicaoAutenticada(patch("/api/v1/pedidos/{pedidoId}/cancelar", 1)))
                .andExpect(status().isBadRequest());

        verify(pedidoService).cancelPedido(1, usuarioAutenticado);
    }

    @Test
    void deveRetornar400QuandoIdDoPedidoForInvalidoAoCancelar() throws Exception {
        mockMvc.perform(requisicaoAutenticada(patch("/api/v1/pedidos/{pedidoId}/cancelar", "abc")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pedidoService);
    }

    // getAllPedidos()

    @Test
    void deveRetornar200AoListarTodosOsPedidosComoAdministrador() throws Exception {
        autenticar(UsuarioFixture.usuarioAdmin());
        PedidoResponseDTO response = PedidoFixture.pedidoResponseDTO();

        when(pedidoService.getAllPedidos()).thenReturn(List.of(response));

        mockMvc.perform(requisicaoAutenticada(get("/api/v1/pedidos/admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(response.getId()))
                .andExpect(jsonPath("$[0].usuarioId").value(response.getUsuarioId()))
                .andExpect(jsonPath("$[0].statusPedido").value(response.getStatusPedido().name()))
                .andExpect(jsonPath("$[0].total").value(response.getTotal().doubleValue()));

        verify(pedidoService).getAllPedidos();
    }

    // updateStatusPedido()

    @Test
    void deveRetornar200AoAtualizarStatusDoPedidoComoAdministrador() throws Exception {
        autenticar(UsuarioFixture.usuarioAdmin());
        PedidoUpdateStatusDTO request = PedidoFixture.pedidoUpdateStatusDTO();
        PedidoResponseDTO response = PedidoFixture.pedidoResponseDTO();
        response.setStatusPedido(StatusPedido.APROVADO);

        when(pedidoService.updateStatusPedido(eq(1), any(PedidoUpdateStatusDTO.class))).thenReturn(response);

        mockMvc.perform(requisicaoAutenticada(patch("/api/v1/pedidos/admin/{pedidoId}/status", 1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.statusPedido").value(StatusPedido.APROVADO.name()));

        verify(pedidoService).updateStatusPedido(eq(1), any(PedidoUpdateStatusDTO.class));
    }

    @Test
    void deveRetornar400AoAtualizarStatusSemStatus() throws Exception {
        autenticar(UsuarioFixture.usuarioAdmin());
        PedidoUpdateStatusDTO request = new PedidoUpdateStatusDTO(null);

        mockMvc.perform(requisicaoAutenticada(patch("/api/v1/pedidos/admin/{pedidoId}/status", 1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.status").exists());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void deveRetornar404AoAtualizarStatusDePedidoInexistente() throws Exception {
        autenticar(UsuarioFixture.usuarioAdmin());
        PedidoUpdateStatusDTO request = PedidoFixture.pedidoUpdateStatusDTO();

        doThrow(new RecursoNaoEncontradoException("Pedido não encontrado"))
                .when(pedidoService).updateStatusPedido(eq(99), any(PedidoUpdateStatusDTO.class));

        mockMvc.perform(requisicaoAutenticada(patch("/api/v1/pedidos/admin/{pedidoId}/status", 99))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(pedidoService).updateStatusPedido(eq(99), any(PedidoUpdateStatusDTO.class));
    }

    @Test
    void deveRetornar400QuandoIdDoPedidoForInvalidoAoAtualizarStatus() throws Exception {
        autenticar(UsuarioFixture.usuarioAdmin());
        PedidoUpdateStatusDTO request = PedidoFixture.pedidoUpdateStatusDTO();

        mockMvc.perform(requisicaoAutenticada(patch("/api/v1/pedidos/admin/{pedidoId}/status", "abc"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pedidoService);
    }

    private void autenticar(Usuario usuario) {
        usuarioAutenticado = usuario;
        when(jwtUtil.validarToken(anyString())).thenReturn(true);
        when(jwtUtil.extrairEmail(anyString())).thenReturn(usuarioAutenticado.getEmail());
        when(usuarioRepository.findByEmail(usuarioAutenticado.getEmail()))
                .thenReturn(Optional.of(usuarioAutenticado));
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
