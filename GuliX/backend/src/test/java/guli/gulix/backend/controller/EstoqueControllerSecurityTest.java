package guli.gulix.backend.controller;

import guli.gulix.backend.config.SecurityConfig;
import guli.gulix.backend.dto.EstoqueRequestDTO;
import guli.gulix.backend.dto.EstoqueRequestInventarioDTO;
import guli.gulix.backend.fixture.EstoqueFixture;
import guli.gulix.backend.security.CustomAccessDeniedHandler;
import guli.gulix.backend.security.CustomAuthenticationEntryPoint;
import guli.gulix.backend.security.JwtFilter;
import guli.gulix.backend.service.EstoqueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = EstoqueController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        SecurityConfig.class,
                        JwtFilter.class
                }
        )
)
@Import(EstoqueControllerSecurityTest.TestSecurityConfig.class)
class EstoqueControllerSecurityTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstoqueService estoqueService;

    // GET /api/v1/estoques

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoListarEstoques() throws Exception {
        mockMvc.perform(get("/api/v1/estoques"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(estoqueService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoAoListarEstoques() throws Exception {
        mockMvc.perform(get("/api/v1/estoques")
                        .with(user("usuarioTeste").roles("USER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(estoqueService);
    }

    // GET /api/v1/estoques/{produtoId}

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoConsultarEstoque() throws Exception {
        mockMvc.perform(get("/api/v1/estoques/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(estoqueService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoAoConsultarEstoque() throws Exception {
        mockMvc.perform(get("/api/v1/estoques/1")
                        .with(user("usuarioTeste").roles("USER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(estoqueService);
    }

    // PUT /api/v1/estoques/{produtoId}/entrada

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoAdicionarEstoque() throws Exception {
        EstoqueRequestDTO request = EstoqueFixture.estoqueRequestDTO();

        mockMvc.perform(put("/api/v1/estoques/1/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(estoqueService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoAoAdicionarEstoque() throws Exception {
        EstoqueRequestDTO request = EstoqueFixture.estoqueRequestDTO();

        mockMvc.perform(put("/api/v1/estoques/1/entrada")
                        .with(user("usuarioTeste").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(estoqueService);
    }

    // PUT /api/v1/estoques/{produtoId}/saida

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoRemoverEstoque() throws Exception {
        EstoqueRequestDTO request = EstoqueFixture.estoqueRequestDTO();

        mockMvc.perform(put("/api/v1/estoques/1/saida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(estoqueService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoAoRemoverEstoque() throws Exception {
        EstoqueRequestDTO request = EstoqueFixture.estoqueRequestDTO();

        mockMvc.perform(put("/api/v1/estoques/1/saida")
                        .with(user("usuarioTeste").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(estoqueService);
    }

    // PUT /api/v1/estoques/{produtoId}/inventario

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoRealizarInventario() throws Exception {
        EstoqueRequestInventarioDTO request = new EstoqueRequestInventarioDTO(10);

        mockMvc.perform(put("/api/v1/estoques/1/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(estoqueService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoAoRealizarInventario() throws Exception {
        EstoqueRequestInventarioDTO request = new EstoqueRequestInventarioDTO(10);

        mockMvc.perform(put("/api/v1/estoques/1/inventario")
                        .with(user("usuarioTeste").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(estoqueService);
    }

    @TestConfiguration
    @EnableWebSecurity
    @EnableMethodSecurity
    static class TestSecurityConfig {

        @Bean
        CustomAuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
            return new CustomAuthenticationEntryPoint(objectMapper);
        }

        @Bean
        CustomAccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
            return new CustomAccessDeniedHandler(objectMapper);
        }

        @Bean
        SecurityFilterChain testSecurityFilterChain(
                HttpSecurity http,
                CustomAuthenticationEntryPoint authenticationEntryPoint,
                CustomAccessDeniedHandler accessDeniedHandler
        ) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .exceptionHandling(exception -> exception
                            .authenticationEntryPoint(authenticationEntryPoint)
                            .accessDeniedHandler(accessDeniedHandler)
                    )
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().authenticated()
                    )
                    .build();
        }
    }
}
