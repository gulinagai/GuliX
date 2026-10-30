package guli.gulix.backend.config;

import guli.gulix.backend.controller.ProdutoController;
import guli.gulix.backend.dto.FaixaPrecoResponseDTO;
import guli.gulix.backend.repository.UsuarioRepository;
import guli.gulix.backend.security.CustomAccessDeniedHandler;
import guli.gulix.backend.security.CustomAuthenticationEntryPoint;
import guli.gulix.backend.security.JwtFilter;
import guli.gulix.backend.security.JwtUtil;
import guli.gulix.backend.service.ProdutoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProdutoController.class)
@Import({
        SecurityConfig.class,
        CorsConfig.class,
        JwtFilter.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        CorsConfigTest.WebSecurity.class
})
class CorsConfigTest {

    private static final String ORIGEM_VITE = "http://localhost:5173";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProdutoService produtoService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void deveResponderPreflightDaOrigemDoVite() throws Exception {
        mockMvc.perform(options("/api/v1/produtos/faixa-precos")
                        .header(HttpHeaders.ORIGIN, ORIGEM_VITE)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGEM_VITE))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, org.hamcrest.Matchers.containsString("GET")))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, org.hamcrest.Matchers.containsString("Authorization")));
    }

    @Test
    void deveRecusarPreflightDeOutraOrigem() throws Exception {
        mockMvc.perform(options("/api/v1/produtos/faixa-precos")
                        .header(HttpHeaders.ORIGIN, "http://evil.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void deveManterConsultaPublicaComCabecalhoCors() throws Exception {
        when(produtoService.getFaixaPreco()).thenReturn(
                new FaixaPrecoResponseDTO(new BigDecimal("49.90"), new BigDecimal("5999.90"))
        );

        mockMvc.perform(get("/api/v1/produtos/faixa-precos")
                        .header(HttpHeaders.ORIGIN, ORIGEM_VITE))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGEM_VITE));
    }

    @Test
    void deveManter401EmEndpointProtegidoEExporOErroAoNavegador() throws Exception {
        mockMvc.perform(post("/api/v1/produtos")
                        .header(HttpHeaders.ORIGIN, ORIGEM_VITE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGEM_VITE));
    }

    @TestConfiguration
    @EnableWebSecurity
    static class WebSecurity {
    }
}
