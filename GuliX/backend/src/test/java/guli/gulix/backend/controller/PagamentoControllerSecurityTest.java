package guli.gulix.backend.controller;

import guli.gulix.backend.config.CorsConfig;
import guli.gulix.backend.config.SecurityConfig;
import guli.gulix.backend.dto.CondicoesPagamentoResponseDTO;
import guli.gulix.backend.repository.UsuarioRepository;
import guli.gulix.backend.security.CustomAccessDeniedHandler;
import guli.gulix.backend.security.CustomAuthenticationEntryPoint;
import guli.gulix.backend.security.JwtFilter;
import guli.gulix.backend.security.JwtUtil;
import guli.gulix.backend.service.PagamentoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PagamentoController.class)
@Import({
        SecurityConfig.class,
        CorsConfig.class,
        JwtFilter.class,
        CustomAuthenticationEntryPoint.class,
        CustomAccessDeniedHandler.class,
        PagamentoControllerSecurityTest.WebSecurity.class
})
class PagamentoControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PagamentoService pagamentoService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoConsultarPagamento() throws Exception {
        mockMvc.perform(get("/api/v1/pagamentos/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(pagamentoService);
    }

    @Test
    void devePermitirConsultaAnonimaDasCondicoesDeParcelamento() throws Exception {
        when(pagamentoService.getCondicoesParcelamento(1))
                .thenReturn(new CondicoesPagamentoResponseDTO(6, new BigDecimal("833.33")));

        mockMvc.perform(get("/api/v1/pagamentos/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeParcelas").value(6))
                .andExpect(jsonPath("$.valorParcela").value(833.33));

        verify(pagamentoService).getCondicoesParcelamento(1);
    }

    @TestConfiguration
    @EnableWebSecurity
    static class WebSecurity {
    }
}
