package guli.gulix.backend.controller;

import guli.gulix.backend.config.SecurityConfig;
import guli.gulix.backend.dto.UsuarioUpdateDTO;
import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.fixture.UsuarioFixture;
import guli.gulix.backend.security.CustomAccessDeniedHandler;
import guli.gulix.backend.security.CustomAuthenticationEntryPoint;
import guli.gulix.backend.security.JwtFilter;
import guli.gulix.backend.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UsuarioController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        SecurityConfig.class,
                        JwtFilter.class
                }
        )
)
@Import(UsuarioControllerSecurityTest.TestSecurityConfig.class)
class UsuarioControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService usuarioService;


    // getListUsuario()

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAoListarUsuarios() throws Exception {

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void deveRetornar403QuandoUsuarioComPerfilUserTentarListarUsuarios() throws Exception {

        mockMvc.perform(
                get("/api/v1/usuarios")
                        .with(user("usuarioTeste").roles("USER"))
        ).andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }


    // getUsuarioById()

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoConsultarUsuario() throws Exception {

        mockMvc.perform(get("/api/v1/usuarios/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void deveRetornar403QuandoUsuarioTentarConsultarOutroUsuario() throws Exception {

        Usuario usuarioAutenticado = UsuarioFixture.usuario();

        mockMvc.perform(
                get("/api/v1/usuarios/2")
                        .with(authentication(authenticationUsuario(usuarioAutenticado)))
        ).andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }


    // updateUsuarioById()

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAtualizarUsuario() throws Exception {

        UsuarioUpdateDTO request = UsuarioFixture.usuarioUpdateDTO();

        mockMvc.perform(
                put("/api/v1/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isUnauthorized());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void deveRetornar403QuandoUsuarioTentarAtualizarOutroUsuario() throws Exception {

        UsuarioUpdateDTO request = UsuarioFixture.usuarioUpdateDTO();
        Usuario usuarioAutenticado = UsuarioFixture.usuario();

        mockMvc.perform(
                put("/api/v1/usuarios/2")
                        .with(authentication(authenticationUsuario(usuarioAutenticado)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }


    // updatePatchUsuarioById()

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoAtualizarParcialmenteUsuario() throws Exception {

        UsuarioUpdateDTO request = UsuarioFixture.usuarioUpdateDTO();

        mockMvc.perform(
                patch("/api/v1/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isUnauthorized());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void deveRetornar403QuandoUsuarioTentarAtualizarParcialmenteOutroUsuario() throws Exception {

        UsuarioUpdateDTO request = UsuarioFixture.usuarioUpdateDTO();
        Usuario usuarioAutenticado = UsuarioFixture.usuario();

        mockMvc.perform(
                patch("/api/v1/usuarios/2")
                        .with(authentication(authenticationUsuario(usuarioAutenticado)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }


    // deleteUsuarioById()

    @Test
    void deveRetornar401QuandoUsuarioNaoAutenticadoExcluirUsuario() throws Exception {

        mockMvc.perform(delete("/api/v1/usuarios/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void deveRetornar403QuandoUsuarioComPerfilUserTentarExcluirUsuario() throws Exception {

        mockMvc.perform(
                delete("/api/v1/usuarios/1")
                        .with(user("usuarioTeste").roles("USER"))
        ).andExpect(status().isForbidden());

        verifyNoInteractions(usuarioService);
    }


    private UsernamePasswordAuthenticationToken authenticationUsuario(Usuario usuario) {

        return new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                List.of(new SimpleGrantedAuthority(usuario.getRole().name()))
        );
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
