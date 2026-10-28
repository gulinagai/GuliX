package guli.gulix.backend.controller;

import guli.gulix.backend.config.SecurityConfig;
import guli.gulix.backend.dto.EnderecoCreateDTO;
import guli.gulix.backend.dto.EnderecoUpdateDTO;
import guli.gulix.backend.fixture.EnderecoFixture;
import guli.gulix.backend.security.CustomAccessDeniedHandler;
import guli.gulix.backend.security.CustomAuthenticationEntryPoint;
import guli.gulix.backend.security.JwtFilter;
import guli.gulix.backend.service.EnderecoService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = EnderecoController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        SecurityConfig.class,
                        JwtFilter.class
                }
        )
)
@Import(EnderecoControllerSecurityTest.TestSecurityConfig.class)
class EnderecoControllerSecurityTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnderecoService enderecoService;

    // GET /api/v1/enderecos

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoListarEnderecos() throws Exception {
        mockMvc.perform(get("/api/v1/enderecos"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(enderecoService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoAoListarEnderecos() throws Exception {
        mockMvc.perform(get("/api/v1/enderecos")
                        .with(user("adminTeste").roles("ADMIN")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(enderecoService);
    }

    // GET /api/v1/enderecos/{enderecoId}

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoConsultarEndereco() throws Exception {
        mockMvc.perform(get("/api/v1/enderecos/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(enderecoService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoAoConsultarEndereco() throws Exception {
        mockMvc.perform(get("/api/v1/enderecos/1")
                        .with(user("adminTeste").roles("ADMIN")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(enderecoService);
    }

    // POST /api/v1/enderecos

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoCriarEndereco() throws Exception {
        EnderecoCreateDTO request = EnderecoFixture.enderecoCreateDTO();

        mockMvc.perform(post("/api/v1/enderecos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(enderecoService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoAoCriarEndereco() throws Exception {
        EnderecoCreateDTO request = EnderecoFixture.enderecoCreateDTO();

        mockMvc.perform(post("/api/v1/enderecos")
                        .with(user("adminTeste").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(enderecoService);
    }

    // DELETE /api/v1/enderecos/{enderecoId}

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoExcluirEndereco() throws Exception {
        mockMvc.perform(delete("/api/v1/enderecos/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(enderecoService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoAoExcluirEndereco() throws Exception {
        mockMvc.perform(delete("/api/v1/enderecos/1")
                        .with(user("adminTeste").roles("ADMIN")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(enderecoService);
    }

    // PATCH /api/v1/enderecos/{enderecoId}

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoAtualizarEndereco() throws Exception {
        EnderecoUpdateDTO request = EnderecoFixture.enderecoUpdateDTO();

        mockMvc.perform(patch("/api/v1/enderecos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(enderecoService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoAoAtualizarEndereco() throws Exception {
        EnderecoUpdateDTO request = EnderecoFixture.enderecoUpdateDTO();

        mockMvc.perform(patch("/api/v1/enderecos/1")
                        .with(user("adminTeste").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(enderecoService);
    }

    // PATCH /api/v1/enderecos/{enderecoId}/principal

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoDefinirPrincipal() throws Exception {
        mockMvc.perform(patch("/api/v1/enderecos/1/principal"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(enderecoService);
    }

    @Test
    void deveRetornar403QuandoUsuarioSemPermissaoAoDefinirPrincipal() throws Exception {
        mockMvc.perform(patch("/api/v1/enderecos/1/principal")
                        .with(user("adminTeste").roles("ADMIN")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(enderecoService);
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
