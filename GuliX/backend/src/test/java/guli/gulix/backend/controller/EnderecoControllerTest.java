package guli.gulix.backend.controller;

import guli.gulix.backend.dto.EnderecoCreateDTO;
import guli.gulix.backend.dto.EnderecoResponseDTO;
import guli.gulix.backend.dto.EnderecoUpdateDTO;
import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.exception.RegraNegocioException;
import guli.gulix.backend.fixture.EnderecoFixture;
import guli.gulix.backend.fixture.UsuarioFixture;
import guli.gulix.backend.repository.UsuarioRepository;
import guli.gulix.backend.security.JwtUtil;
import guli.gulix.backend.service.EnderecoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnderecoController.class)
@Import(EnderecoControllerTest.AuthenticationPrincipalTestConfig.class)
class EnderecoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EnderecoService enderecoService;

    // Dependências utilizadas pelo filtro JWT carregado no contexto MVC.
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    private Usuario usuarioAutenticado;

    @BeforeEach
    void setup() {
        autenticar(UsuarioFixture.usuario());
    }

    // getListEnderecos()

    @Test
    void deveRetornar200AoListarEnderecosDoUsuario() throws Exception {
        EnderecoResponseDTO response = EnderecoFixture.enderecoResponseDTO();

        when(enderecoService.getListEnderecos(usuarioAutenticado)).thenReturn(List.of(response));

        mockMvc.perform(requisicaoAutenticada(get("/api/v1/enderecos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(response.getId()))
                .andExpect(jsonPath("$[0].rua").value(response.getRua()))
                .andExpect(jsonPath("$[0].numero").value(response.getNumero()))
                .andExpect(jsonPath("$[0].cidade").value(response.getCidade()))
                .andExpect(jsonPath("$[0].estado").value(response.getEstado()))
                .andExpect(jsonPath("$[0].cep").value(response.getCep()));

        verify(enderecoService).getListEnderecos(usuarioAutenticado);
    }

    @Test
    void deveRetornar200ComListaVaziaQuandoUsuarioNaoPossuirEnderecos() throws Exception {
        when(enderecoService.getListEnderecos(usuarioAutenticado)).thenReturn(List.of());

        mockMvc.perform(requisicaoAutenticada(get("/api/v1/enderecos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(enderecoService).getListEnderecos(usuarioAutenticado);
    }

    // getEnderecoById()

    @Test
    void deveRetornar200AoConsultarEnderecoPorId() throws Exception {
        EnderecoResponseDTO response = EnderecoFixture.enderecoResponseDTO();

        when(enderecoService.getEnderecoById(1, usuarioAutenticado)).thenReturn(response);

        mockMvc.perform(requisicaoAutenticada(get("/api/v1/enderecos/{enderecoId}", 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.rua").value(response.getRua()))
                .andExpect(jsonPath("$.numero").value(response.getNumero()))
                .andExpect(jsonPath("$.cidade").value(response.getCidade()))
                .andExpect(jsonPath("$.estado").value(response.getEstado()))
                .andExpect(jsonPath("$.cep").value(response.getCep()));

        verify(enderecoService).getEnderecoById(1, usuarioAutenticado);
    }

    @Test
    void deveRetornar404AoConsultarEnderecoInexistente() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Endereço com id 99 não encontrado"))
                .when(enderecoService).getEnderecoById(99, usuarioAutenticado);

        mockMvc.perform(requisicaoAutenticada(get("/api/v1/enderecos/{enderecoId}", 99)))
                .andExpect(status().isNotFound());

        verify(enderecoService).getEnderecoById(99, usuarioAutenticado);
    }

    @Test
    void deveRetornar400AoConsultarEnderecoDeOutroUsuario() throws Exception {
        doThrow(new RegraNegocioException("Acesso negado"))
                .when(enderecoService).getEnderecoById(1, usuarioAutenticado);

        mockMvc.perform(requisicaoAutenticada(get("/api/v1/enderecos/{enderecoId}", 1)))
                .andExpect(status().isBadRequest());

        verify(enderecoService).getEnderecoById(1, usuarioAutenticado);
    }

    @Test
    void deveRetornar400QuandoIdDoEnderecoForInvalido() throws Exception {
        mockMvc.perform(requisicaoAutenticada(get("/api/v1/enderecos/{enderecoId}", "abc")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(enderecoService);
    }

    // createNewEndereco()

    @Test
    void deveRetornar201AoCriarEndereco() throws Exception {
        EnderecoCreateDTO request = EnderecoFixture.enderecoCreateDTO();
        EnderecoResponseDTO response = EnderecoFixture.enderecoResponseDTO();

        when(enderecoService.createNewEndereco(any(EnderecoCreateDTO.class), eq(usuarioAutenticado)))
                .thenReturn(response);

        mockMvc.perform(requisicaoAutenticada(post("/api/v1/enderecos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/enderecos/" + response.getId()))
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.rua").value(response.getRua()))
                .andExpect(jsonPath("$.cidade").value(response.getCidade()))
                .andExpect(jsonPath("$.cep").value(response.getCep()));

        verify(enderecoService).createNewEndereco(any(EnderecoCreateDTO.class), eq(usuarioAutenticado));
    }

    @Test
    void deveRetornar400AoCriarEnderecoComRuaEmBranco() throws Exception {
        EnderecoCreateDTO request = new EnderecoCreateDTO(" ", "1000", "São Paulo", "SP", "01310-100");

        mockMvc.perform(requisicaoAutenticada(post("/api/v1/enderecos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.rua").exists());

        verifyNoInteractions(enderecoService);
    }

    @Test
    void deveRetornar400AoCriarEnderecoComNumeroEmBranco() throws Exception {
        EnderecoCreateDTO request = new EnderecoCreateDTO("Avenida Paulista", " ", "São Paulo", "SP", "01310-100");

        mockMvc.perform(requisicaoAutenticada(post("/api/v1/enderecos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.numero").exists());

        verifyNoInteractions(enderecoService);
    }

    @Test
    void deveRetornar400AoCriarEnderecoComCidadeEmBranco() throws Exception {
        EnderecoCreateDTO request = new EnderecoCreateDTO("Avenida Paulista", "1000", " ", "SP", "01310-100");

        mockMvc.perform(requisicaoAutenticada(post("/api/v1/enderecos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.cidade").exists());

        verifyNoInteractions(enderecoService);
    }

    @Test
    void deveRetornar400AoCriarEnderecoComEstadoInvalido() throws Exception {
        EnderecoCreateDTO request = new EnderecoCreateDTO("Avenida Paulista", "1000", "São Paulo", "S", "01310-100");

        mockMvc.perform(requisicaoAutenticada(post("/api/v1/enderecos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.estado").exists());

        verifyNoInteractions(enderecoService);
    }

    @Test
    void deveRetornar400AoCriarEnderecoComCepInvalido() throws Exception {
        EnderecoCreateDTO request = new EnderecoCreateDTO("Avenida Paulista", "1000", "São Paulo", "SP", "0131");

        mockMvc.perform(requisicaoAutenticada(post("/api/v1/enderecos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.cep").exists());

        verifyNoInteractions(enderecoService);
    }

    @Test
    void deveRetornar404AoCriarEnderecoQuandoCidadeNaoExistir() throws Exception {
        EnderecoCreateDTO request = EnderecoFixture.enderecoCreateDTO();

        doThrow(new RecursoNaoEncontradoException("Cidade não encontrada ou não cadastrada"))
                .when(enderecoService).createNewEndereco(any(EnderecoCreateDTO.class), eq(usuarioAutenticado));

        mockMvc.perform(requisicaoAutenticada(post("/api/v1/enderecos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(enderecoService).createNewEndereco(any(EnderecoCreateDTO.class), eq(usuarioAutenticado));
    }

    // deleteEnderecoById()

    @Test
    void deveRetornar204AoExcluirEndereco() throws Exception {
        mockMvc.perform(requisicaoAutenticada(delete("/api/v1/enderecos/{enderecoId}", 1)))
                .andExpect(status().isNoContent());

        verify(enderecoService).deleteEnderecoById(1, usuarioAutenticado);
    }

    @Test
    void deveRetornar404AoExcluirEnderecoInexistente() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Endereço com id 99 não encontrado"))
                .when(enderecoService).deleteEnderecoById(99, usuarioAutenticado);

        mockMvc.perform(requisicaoAutenticada(delete("/api/v1/enderecos/{enderecoId}", 99)))
                .andExpect(status().isNotFound());

        verify(enderecoService).deleteEnderecoById(99, usuarioAutenticado);
    }

    @Test
    void deveRetornar400AoExcluirEnderecoDeOutroUsuario() throws Exception {
        doThrow(new RegraNegocioException("Acesso negado"))
                .when(enderecoService).deleteEnderecoById(1, usuarioAutenticado);

        mockMvc.perform(requisicaoAutenticada(delete("/api/v1/enderecos/{enderecoId}", 1)))
                .andExpect(status().isBadRequest());

        verify(enderecoService).deleteEnderecoById(1, usuarioAutenticado);
    }

    @Test
    void deveRetornar400QuandoIdDoEnderecoForInvalidoAoExcluir() throws Exception {
        mockMvc.perform(requisicaoAutenticada(delete("/api/v1/enderecos/{enderecoId}", "abc")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(enderecoService);
    }

    // updateEnderecoById()

    @Test
    void deveRetornar200AoAtualizarEndereco() throws Exception {
        EnderecoUpdateDTO request = EnderecoFixture.enderecoUpdateDTO();
        EnderecoResponseDTO response = EnderecoFixture.enderecoResponseDTO();

        when(enderecoService.updateEnderecoById(eq(1), any(EnderecoUpdateDTO.class), eq(usuarioAutenticado)))
                .thenReturn(response);

        mockMvc.perform(requisicaoAutenticada(patch("/api/v1/enderecos/{enderecoId}", 1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.rua").value(response.getRua()))
                .andExpect(jsonPath("$.cidade").value(response.getCidade()))
                .andExpect(jsonPath("$.cep").value(response.getCep()));

        verify(enderecoService).updateEnderecoById(eq(1), any(EnderecoUpdateDTO.class), eq(usuarioAutenticado));
    }

    @Test
    void deveRetornar400AoAtualizarEnderecoComRuaEmBranco() throws Exception {
        EnderecoUpdateDTO request = new EnderecoUpdateDTO(" ", null, null, null, null);

        mockMvc.perform(requisicaoAutenticada(patch("/api/v1/enderecos/{enderecoId}", 1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.rua").exists());

        verifyNoInteractions(enderecoService);
    }

    @Test
    void deveRetornar400AoAtualizarEnderecoComEstadoInvalido() throws Exception {
        EnderecoUpdateDTO request = new EnderecoUpdateDTO(null, null, null, "S", null);

        mockMvc.perform(requisicaoAutenticada(patch("/api/v1/enderecos/{enderecoId}", 1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.estado").exists());

        verifyNoInteractions(enderecoService);
    }

    @Test
    void deveRetornar400AoAtualizarEnderecoComCepInvalido() throws Exception {
        EnderecoUpdateDTO request = new EnderecoUpdateDTO(null, null, null, null, "123");

        mockMvc.perform(requisicaoAutenticada(patch("/api/v1/enderecos/{enderecoId}", 1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.cep").exists());

        verifyNoInteractions(enderecoService);
    }

    @Test
    void deveRetornar404AoAtualizarEnderecoInexistente() throws Exception {
        EnderecoUpdateDTO request = EnderecoFixture.enderecoUpdateDTO();

        doThrow(new RecursoNaoEncontradoException("Endereço com id 99 não encontrado"))
                .when(enderecoService).updateEnderecoById(eq(99), any(EnderecoUpdateDTO.class), eq(usuarioAutenticado));

        mockMvc.perform(requisicaoAutenticada(patch("/api/v1/enderecos/{enderecoId}", 99))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(enderecoService).updateEnderecoById(eq(99), any(EnderecoUpdateDTO.class), eq(usuarioAutenticado));
    }

    @Test
    void deveRetornar400AoAtualizarEnderecoDeOutroUsuario() throws Exception {
        EnderecoUpdateDTO request = EnderecoFixture.enderecoUpdateDTO();

        doThrow(new RegraNegocioException("Acesso negado"))
                .when(enderecoService).updateEnderecoById(eq(1), any(EnderecoUpdateDTO.class), eq(usuarioAutenticado));

        mockMvc.perform(requisicaoAutenticada(patch("/api/v1/enderecos/{enderecoId}", 1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(enderecoService).updateEnderecoById(eq(1), any(EnderecoUpdateDTO.class), eq(usuarioAutenticado));
    }

    @Test
    void deveRetornar400QuandoIdDoEnderecoForInvalidoAoAtualizar() throws Exception {
        EnderecoUpdateDTO request = EnderecoFixture.enderecoUpdateDTO();

        mockMvc.perform(requisicaoAutenticada(patch("/api/v1/enderecos/{enderecoId}", "abc"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(enderecoService);
    }

    // updateEnderecoPrincipalById()

    @Test
    void deveRetornar204AoDefinirEnderecoComoPrincipal() throws Exception {
        mockMvc.perform(requisicaoAutenticada(patch("/api/v1/enderecos/{enderecoId}/principal", 1)))
                .andExpect(status().isNoContent());

        verify(enderecoService).updateEnderecoPrincipalById(1, usuarioAutenticado);
    }

    @Test
    void deveRetornar404AoDefinirPrincipalEmEnderecoInexistente() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Endereço com id 99 não encontrado"))
                .when(enderecoService).updateEnderecoPrincipalById(99, usuarioAutenticado);

        mockMvc.perform(requisicaoAutenticada(patch("/api/v1/enderecos/{enderecoId}/principal", 99)))
                .andExpect(status().isNotFound());

        verify(enderecoService).updateEnderecoPrincipalById(99, usuarioAutenticado);
    }

    @Test
    void deveRetornar400AoDefinirPrincipalEmEnderecoDeOutroUsuario() throws Exception {
        doThrow(new RegraNegocioException("Acesso negado"))
                .when(enderecoService).updateEnderecoPrincipalById(1, usuarioAutenticado);

        mockMvc.perform(requisicaoAutenticada(patch("/api/v1/enderecos/{enderecoId}/principal", 1)))
                .andExpect(status().isBadRequest());

        verify(enderecoService).updateEnderecoPrincipalById(1, usuarioAutenticado);
    }

    @Test
    void deveRetornar400QuandoIdDoEnderecoForInvalidoAoDefinirPrincipal() throws Exception {
        mockMvc.perform(requisicaoAutenticada(patch("/api/v1/enderecos/{enderecoId}/principal", "abc")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(enderecoService);
    }

    private void autenticar(Usuario usuario) {
        usuarioAutenticado = usuario;
        when(jwtUtil.validarToken(anyString())).thenReturn(true);
        when(jwtUtil.extrairEmail(anyString())).thenReturn(usuarioAutenticado.getEmail());
        when(usuarioRepository.findByEmail(usuarioAutenticado.getEmail()))
                .thenReturn(Optional.of(usuarioAutenticado));
    }

    private MockHttpServletRequestBuilder requisicaoAutenticada(MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer token-teste");
    }

    @TestConfiguration
    static class AuthenticationPrincipalTestConfig implements WebMvcConfigurer {

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new AuthenticationPrincipalArgumentResolver());
        }
    }
}
