package guli.gulix.backend.controller;

import guli.gulix.backend.dto.PagamentoResponseDTO;
import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.exception.RegraNegocioException;
import guli.gulix.backend.fixture.PagamentoFixture;
import guli.gulix.backend.fixture.UsuarioFixture;
import guli.gulix.backend.repository.UsuarioRepository;
import guli.gulix.backend.security.JwtUtil;
import guli.gulix.backend.service.PagamentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PagamentoController.class)
@Import(PagamentoControllerTest.AuthenticationPrincipalTestConfig.class)
class PagamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PagamentoService pagamentoService;

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

    // getPagamentoById()

    @Test
    void deveRetornar200AoConsultarPagamentoPorId() throws Exception {
        PagamentoResponseDTO response = PagamentoFixture.pagamentoResponseDTO();

        when(pagamentoService.getPagamentoById(1, usuarioAutenticado)).thenReturn(response);

        mockMvc.perform(requisicaoAutenticada(get("/api/v1/pagamentos/{pagamentoId}", 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.pedidoId").value(response.getPedidoId()))
                .andExpect(jsonPath("$.metodoPagamento").value(response.getMetodoPagamento().name()))
                .andExpect(jsonPath("$.statusPagamento").value(response.getStatusPagamento().name()))
                .andExpect(jsonPath("$.valorOriginal").value(response.getValorOriginal().doubleValue()))
                .andExpect(jsonPath("$.desconto").value(response.getDesconto().doubleValue()))
                .andExpect(jsonPath("$.valorJuros").value(response.getValorJuros().doubleValue()))
                .andExpect(jsonPath("$.valorFinal").value(response.getValorFinal().doubleValue()))
                .andExpect(jsonPath("$.numeroParcelas").value(response.getNumeroParcelas()))
                .andExpect(jsonPath("$.valorParcela").value(response.getValorParcela().doubleValue()))
                .andExpect(jsonPath("$.checkoutUrl").value(response.getCheckoutUrl()));

        verify(pagamentoService).getPagamentoById(1, usuarioAutenticado);
    }

    @Test
    void deveRetornar200AoConsultarPagamentoComoAdministrador() throws Exception {
        autenticar(UsuarioFixture.usuarioAdmin());
        PagamentoResponseDTO response = PagamentoFixture.pagamentoResponseDTO();

        when(pagamentoService.getPagamentoById(1, usuarioAutenticado)).thenReturn(response);

        mockMvc.perform(requisicaoAutenticada(get("/api/v1/pagamentos/{pagamentoId}", 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.checkoutUrl").value(response.getCheckoutUrl()));

        verify(pagamentoService).getPagamentoById(1, usuarioAutenticado);
    }

    @Test
    void deveRetornar404AoConsultarPagamentoInexistente() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Pagamento não encontrado"))
                .when(pagamentoService).getPagamentoById(99, usuarioAutenticado);

        mockMvc.perform(requisicaoAutenticada(get("/api/v1/pagamentos/{pagamentoId}", 99)))
                .andExpect(status().isNotFound());

        verify(pagamentoService).getPagamentoById(99, usuarioAutenticado);
    }

    @Test
    void deveRetornar400AoConsultarPagamentoDeOutroUsuario() throws Exception {
        doThrow(new RegraNegocioException("Acesso negado"))
                .when(pagamentoService).getPagamentoById(1, usuarioAutenticado);

        mockMvc.perform(requisicaoAutenticada(get("/api/v1/pagamentos/{pagamentoId}", 1)))
                .andExpect(status().isBadRequest());

        verify(pagamentoService).getPagamentoById(1, usuarioAutenticado);
    }

    @Test
    void deveRetornar400QuandoIdDoPagamentoForInvalido() throws Exception {
        mockMvc.perform(requisicaoAutenticada(get("/api/v1/pagamentos/{pagamentoId}", "abc")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pagamentoService);
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
