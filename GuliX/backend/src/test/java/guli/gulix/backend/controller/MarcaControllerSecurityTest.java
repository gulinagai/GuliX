package guli.gulix.backend.controller;

import guli.gulix.backend.config.SecurityConfig;
import guli.gulix.backend.dto.MarcaRequestDTO;
import guli.gulix.backend.fixture.MarcaFixture;
import guli.gulix.backend.security.CustomAccessDeniedHandler;
import guli.gulix.backend.security.CustomAuthenticationEntryPoint;
import guli.gulix.backend.security.JwtFilter;
import guli.gulix.backend.service.MarcaService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = MarcaController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        SecurityConfig.class,
                        JwtFilter.class
                }
        )
)
@Import(MarcaControllerSecurityTest.TestSecurityConfig.class)
class MarcaControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MarcaService marcaService;

    // createNewMarca()

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoCriarMarca() throws Exception {
        MarcaRequestDTO request = MarcaFixture.marcaRequestDTO();

        mockMvc.perform(post("/api/v1/marcas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(marcaService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoTentarCriarMarca() throws Exception {
        MarcaRequestDTO request = MarcaFixture.marcaRequestDTO();

        mockMvc.perform(post("/api/v1/marcas")
                        .with(user("usuarioTeste").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(marcaService);
    }

    // updateMarcaById()

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoAtualizarMarca() throws Exception {
        MarcaRequestDTO request = MarcaFixture.marcaRequestDTO();

        mockMvc.perform(put("/api/v1/marcas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(marcaService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoTentarAtualizarMarca() throws Exception {
        MarcaRequestDTO request = MarcaFixture.marcaRequestDTO();

        mockMvc.perform(put("/api/v1/marcas/1")
                        .with(user("usuarioTeste").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(marcaService);
    }

    // deleteMarcaById()

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoExcluirMarca() throws Exception {
        mockMvc.perform(delete("/api/v1/marcas/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(marcaService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoTentarExcluirMarca() throws Exception {
        mockMvc.perform(delete("/api/v1/marcas/1")
                        .with(user("usuarioTeste").roles("USER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(marcaService);
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
