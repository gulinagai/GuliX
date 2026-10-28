package guli.gulix.backend.controller;

import guli.gulix.backend.config.SecurityConfig;
import guli.gulix.backend.dto.PedidoCreateDTO;
import guli.gulix.backend.dto.PedidoUpdateStatusDTO;
import guli.gulix.backend.fixture.PedidoFixture;
import guli.gulix.backend.security.CustomAccessDeniedHandler;
import guli.gulix.backend.security.CustomAuthenticationEntryPoint;
import guli.gulix.backend.security.JwtFilter;
import guli.gulix.backend.service.PedidoService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PedidoController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        SecurityConfig.class,
                        JwtFilter.class
                }
        )
)
@Import(PedidoControllerSecurityTest.TestSecurityConfig.class)
class PedidoControllerSecurityTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    // GET /api/v1/pedidos

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoListarPedidos() throws Exception {
        mockMvc.perform(get("/api/v1/pedidos"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoAoListarPedidos() throws Exception {
        mockMvc.perform(get("/api/v1/pedidos")
                        .with(user("adminTeste").roles("ADMIN")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(pedidoService);
    }

    // GET /api/v1/pedidos/{pedidoId}

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoConsultarPedido() throws Exception {
        mockMvc.perform(get("/api/v1/pedidos/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(pedidoService);
    }

    // POST /api/v1/pedidos

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoCriarPedido() throws Exception {
        PedidoCreateDTO request = PedidoFixture.pedidoCreateDTO();

        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoAoCriarPedido() throws Exception {
        PedidoCreateDTO request = PedidoFixture.pedidoCreateDTO();

        mockMvc.perform(post("/api/v1/pedidos")
                        .with(user("adminTeste").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(pedidoService);
    }

    // PATCH /api/v1/pedidos/{pedidoId}/cancelar

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoCancelarPedido() throws Exception {
        mockMvc.perform(patch("/api/v1/pedidos/1/cancelar"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoAoCancelarPedido() throws Exception {
        mockMvc.perform(patch("/api/v1/pedidos/1/cancelar")
                        .with(user("adminTeste").roles("ADMIN")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(pedidoService);
    }

    // GET /api/v1/pedidos/admin

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoListarTodosOsPedidos() throws Exception {
        mockMvc.perform(get("/api/v1/pedidos/admin"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoAoListarTodosOsPedidos() throws Exception {
        mockMvc.perform(get("/api/v1/pedidos/admin")
                        .with(user("usuarioTeste").roles("USER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(pedidoService);
    }

    // PATCH /api/v1/pedidos/admin/{pedidoId}/status

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoAtualizarStatus() throws Exception {
        PedidoUpdateStatusDTO request = PedidoFixture.pedidoUpdateStatusDTO();

        mockMvc.perform(patch("/api/v1/pedidos/admin/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoAoAtualizarStatus() throws Exception {
        PedidoUpdateStatusDTO request = PedidoFixture.pedidoUpdateStatusDTO();

        mockMvc.perform(patch("/api/v1/pedidos/admin/1/status")
                        .with(user("usuarioTeste").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(pedidoService);
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
