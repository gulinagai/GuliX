package guli.gulix.backend.controller;

import guli.gulix.backend.config.SecurityConfig;
import guli.gulix.backend.dto.EmpresaUpdateDTO;
import guli.gulix.backend.fixture.EmpresaFixture;
import guli.gulix.backend.security.CustomAccessDeniedHandler;
import guli.gulix.backend.security.CustomAuthenticationEntryPoint;
import guli.gulix.backend.security.JwtFilter;
import guli.gulix.backend.service.EmpresaService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = EmpresaController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        SecurityConfig.class,
                        JwtFilter.class
                }
        )
)
@Import(EmpresaControllerSecurityTest.TestSecurityConfig.class)
class EmpresaControllerSecurityTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmpresaService empresaService;

    // updateEmpresa()

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoAtualizarEmpresa() throws Exception {
        EmpresaUpdateDTO empresaUpdateDTO = EmpresaFixture.empresaUpdateDTO();

        mockMvc.perform(
                patch("/api/v1/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaUpdateDTO))
        ).andExpect(status().isUnauthorized());

        verifyNoInteractions(empresaService);
    }

    @Test
    void deveRetornar403QuandoUsuarioNaoAutorizadoAoAtualizarEmpresa() throws Exception {
        EmpresaUpdateDTO empresaUpdateDTO = EmpresaFixture.empresaUpdateDTO();

        mockMvc.perform(
                patch("/api/v1/empresas")
                        .with(user("usuarioTeste").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaUpdateDTO))
        ).andExpect(status().isForbidden());

        verifyNoInteractions(empresaService);
    }

    @Test
    void devePermitirAtualizacaoQuandoUsuarioForAdmin() throws Exception {
        EmpresaUpdateDTO empresaUpdateDTO = EmpresaFixture.empresaUpdateDTO();

        mockMvc.perform(
                patch("/api/v1/empresas")
                        .with(user("adminTeste").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaUpdateDTO))
        ).andExpect(status().isOk());

        verify(empresaService).updateEmpresa(any(EmpresaUpdateDTO.class));
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