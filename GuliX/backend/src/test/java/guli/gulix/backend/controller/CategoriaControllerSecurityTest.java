package guli.gulix.backend.controller;

import guli.gulix.backend.config.SecurityConfig;
import guli.gulix.backend.dto.CategoriaRequestDTO;
import guli.gulix.backend.fixture.CategoriaFixture;
import guli.gulix.backend.security.CustomAccessDeniedHandler;
import guli.gulix.backend.security.CustomAuthenticationEntryPoint;
import guli.gulix.backend.security.JwtFilter;
import guli.gulix.backend.service.CategoriaService;
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
        controllers = CategoriaController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        SecurityConfig.class,
                        JwtFilter.class
                }
        )
)
@Import(CategoriaControllerSecurityTest.TestSecurityConfig.class)
class CategoriaControllerSecurityTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService categoriaService;

    // createNewCategoria()

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoCriarCategoria() throws Exception {
        CategoriaRequestDTO categoriaRequest = CategoriaFixture.categoriaRequestDTO();

        mockMvc.perform(
                post("/api/v1/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaRequest))
        ).andExpect(status().isUnauthorized());

        verifyNoInteractions(categoriaService);
    }

    @Test
    void deveRetornar403QuandoUsuarioNaoAutorizadoAoCriarCategoria() throws Exception {
        CategoriaRequestDTO categoriaRequest = CategoriaFixture.categoriaRequestDTO();

        mockMvc.perform(
                post("/api/v1/categorias")
                        .with(user("usuarioTeste").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaRequest))
        ).andExpect(status().isForbidden());

        verifyNoInteractions(categoriaService);
    }

    // updateCategoriaById()

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoAtualizarCategoria() throws Exception {
        CategoriaRequestDTO categoriaRequest = CategoriaFixture.categoriaRequestDTO();

        mockMvc.perform(
                put("/api/v1/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaRequest))
        ).andExpect(status().isUnauthorized());

        verifyNoInteractions(categoriaService);
    }

    @Test
    void deveRetornar403QuandoUsuarioNaoAutorizadoAoAtualizarCategoria() throws Exception {
        CategoriaRequestDTO categoriaRequest = CategoriaFixture.categoriaRequestDTO();

        mockMvc.perform(
                put("/api/v1/categorias/1")
                        .with(user("usuarioTeste").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaRequest))
        ).andExpect(status().isForbidden());

        verifyNoInteractions(categoriaService);
    }

    // deleteCategoriaById()

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoExcluirCategoria() throws Exception {
        mockMvc.perform(
                delete("/api/v1/categorias/1")
        ).andExpect(status().isUnauthorized());

        verifyNoInteractions(categoriaService);
    }

    @Test
    void deveRetornar403QuandoUsuarioNaoAutorizadoAoExcluirCategoria() throws Exception {
        mockMvc.perform(
                delete("/api/v1/categorias/1")
                        .with(user("usuarioTeste").roles("USER"))
        ).andExpect(status().isForbidden());

        verifyNoInteractions(categoriaService);
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
