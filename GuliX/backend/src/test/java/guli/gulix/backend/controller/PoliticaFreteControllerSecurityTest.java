package guli.gulix.backend.controller;

import guli.gulix.backend.config.SecurityConfig;
import guli.gulix.backend.dto.PoliticaFreteCreateDTO;
import guli.gulix.backend.security.CustomAccessDeniedHandler;
import guli.gulix.backend.security.CustomAuthenticationEntryPoint;
import guli.gulix.backend.security.JwtFilter;
import guli.gulix.backend.service.PoliticaFreteService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PoliticaFreteController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        SecurityConfig.class,
                        JwtFilter.class
                }
        )
)
@Import(PoliticaFreteControllerSecurityTest.TestSecurityConfig.class)
class PoliticaFreteControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PoliticaFreteService politicaFreteService;

    // getAllPoliticaFrete()

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoListarPoliticasDeFrete() throws Exception {
        mockMvc.perform(get("/api/v1/politicas-frete"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(politicaFreteService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoTentarListarPoliticasDeFrete() throws Exception {
        mockMvc.perform(get("/api/v1/politicas-frete")
                        .with(user("usuarioTeste").roles("USER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(politicaFreteService);
    }

    // getPoliticaFreteById()

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoBuscarPoliticaDeFrete() throws Exception {
        mockMvc.perform(get("/api/v1/politicas-frete/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(politicaFreteService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoTentarBuscarPoliticaDeFrete() throws Exception {
        mockMvc.perform(get("/api/v1/politicas-frete/1")
                        .with(user("usuarioTeste").roles("USER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(politicaFreteService);
    }

    // createNewPoliticaFrete()

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoCriarPoliticaDeFrete() throws Exception {
        PoliticaFreteCreateDTO request = new PoliticaFreteCreateDTO(
                1,
                new java.math.BigDecimal("15.00")
        );

        mockMvc.perform(post("/api/v1/politicas-frete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(politicaFreteService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoTentarCriarPoliticaDeFrete() throws Exception {
        PoliticaFreteCreateDTO request = new PoliticaFreteCreateDTO(
                1,
                new java.math.BigDecimal("15.00")
        );

        mockMvc.perform(post("/api/v1/politicas-frete")
                        .with(user("usuarioTeste").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(politicaFreteService);
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
