package guli.gulix.backend.controller;

import guli.gulix.backend.config.SecurityConfig;
import guli.gulix.backend.dto.CidadeCreateDTO;
import guli.gulix.backend.dto.CidadeUpdateDTO;
import guli.gulix.backend.fixture.CidadeFixture;
import guli.gulix.backend.security.CustomAccessDeniedHandler;
import guli.gulix.backend.security.CustomAuthenticationEntryPoint;
import guli.gulix.backend.security.JwtFilter;
import guli.gulix.backend.service.CidadeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CidadeController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        SecurityConfig.class,
                        JwtFilter.class
                }
        )
)
@Import(CidadeControllerSecurityTest.TestSecurityConfig.class)
class CidadeControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CidadeService cidadeService;

    // createNewCidade()

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoCriarCidade() throws Exception {
        CidadeCreateDTO request = CidadeFixture.cidadeCreateDTO();

        mockMvc.perform(post("/api/v1/cidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(cidadeService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoTentarCriarCidade() throws Exception {
        CidadeCreateDTO request = CidadeFixture.cidadeCreateDTO();

        mockMvc.perform(post("/api/v1/cidades")
                        .with(user("usuarioTeste").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(cidadeService);
    }

    // updateCidadeById()

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoAtualizarCidade() throws Exception {
        CidadeUpdateDTO request = CidadeFixture.cidadeUpdateDTO();

        mockMvc.perform(patch("/api/v1/cidades/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(cidadeService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoTentarAtualizarCidade() throws Exception {
        CidadeUpdateDTO request = CidadeFixture.cidadeUpdateDTO();

        mockMvc.perform(patch("/api/v1/cidades/1")
                        .with(user("usuarioTeste").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(cidadeService);
    }

    // deleteCidadeById()

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoExcluirCidade() throws Exception {
        mockMvc.perform(delete("/api/v1/cidades/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(cidadeService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoTentarExcluirCidade() throws Exception {
        mockMvc.perform(delete("/api/v1/cidades/1")
                        .with(user("usuarioTeste").roles("USER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(cidadeService);
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
