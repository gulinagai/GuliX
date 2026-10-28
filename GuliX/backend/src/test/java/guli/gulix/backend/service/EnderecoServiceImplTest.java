package guli.gulix.backend.service;

import guli.gulix.backend.dto.EnderecoCreateDTO;
import guli.gulix.backend.dto.EnderecoResponseDTO;
import guli.gulix.backend.dto.EnderecoUpdateDTO;
import guli.gulix.backend.entity.Cidade;
import guli.gulix.backend.entity.Endereco;
import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.exception.RegraNegocioException;
import guli.gulix.backend.fixture.CidadeFixture;
import guli.gulix.backend.fixture.EnderecoFixture;
import guli.gulix.backend.fixture.UsuarioFixture;
import guli.gulix.backend.gateway.openrouteservice.OpenRouteServiceGateway;
import guli.gulix.backend.geographic.Coordenada;
import guli.gulix.backend.mapper.EnderecoMapper;
import guli.gulix.backend.repository.CidadeRepository;
import guli.gulix.backend.repository.EnderecoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnderecoServiceImplTest {

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private EnderecoMapper enderecoMapper;

    @Mock
    private CidadeRepository cidadeRepository;

    @Mock
    private OpenRouteServiceGateway openRouteServiceGateway;

    @InjectMocks
    private EnderecoServiceImpl enderecoService;

    private Usuario usuario;
    private Endereco endereco;
    private EnderecoResponseDTO enderecoResponseDTO;
    private EnderecoCreateDTO enderecoCreateDTO;
    private EnderecoUpdateDTO enderecoUpdateDTO;

    @BeforeEach
    void setup() {
        usuario = UsuarioFixture.usuario();
        endereco = EnderecoFixture.endereco();
        endereco.setUsuario(usuario);
        enderecoResponseDTO = EnderecoFixture.enderecoResponseDTO();
        enderecoCreateDTO = EnderecoFixture.enderecoCreateDTO();
        enderecoUpdateDTO = EnderecoFixture.enderecoUpdateDTO();
    }

    // getListEnderecos(Usuario usuarioLogado)

    @Test
    void deveRetornarEnderecosDoUsuario() {
        when(enderecoRepository.findByUsuarioId(usuario.getId())).thenReturn(List.of(endereco));
        when(enderecoMapper.toDTO(endereco)).thenReturn(enderecoResponseDTO);

        List<EnderecoResponseDTO> resultado = enderecoService.getListEnderecos(usuario);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertSame(enderecoResponseDTO, resultado.get(0));
        verify(enderecoRepository).findByUsuarioId(usuario.getId());
        verify(enderecoMapper).toDTO(endereco);
    }

    @Test
    void deveRetornarListaVaziaQuandoUsuarioNaoPossuirEnderecos() {
        when(enderecoRepository.findByUsuarioId(usuario.getId())).thenReturn(List.of());

        List<EnderecoResponseDTO> resultado = enderecoService.getListEnderecos(usuario);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(enderecoRepository).findByUsuarioId(usuario.getId());
        verifyNoInteractions(enderecoMapper);
    }

    // getEnderecoById(Integer enderecoId, Usuario usuarioLogado)

    @Test
    void deveRetornarEnderecoQuandoUsuarioForODono() {
        when(enderecoRepository.findById(1)).thenReturn(Optional.of(endereco));
        when(enderecoMapper.toDTO(endereco)).thenReturn(enderecoResponseDTO);

        EnderecoResponseDTO resultado = enderecoService.getEnderecoById(1, usuario);

        assertSame(enderecoResponseDTO, resultado);
        verify(enderecoRepository).findById(1);
        verify(enderecoMapper).toDTO(endereco);
    }

    @Test
    void deveLancarExcecaoQuandoEnderecoNaoExistirAoBuscarPorId() {
        when(enderecoRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> enderecoService.getEnderecoById(99, usuario)
        );

        assertEquals("Endereço com id 99 não encontrado", exception.getMessage());
        verify(enderecoRepository).findById(99);
        verifyNoInteractions(enderecoMapper);
    }

    @Test
    void deveLancarExcecaoAoBuscarEnderecoDeOutroUsuario() {
        Usuario outroUsuario = UsuarioFixture.usuarioAdmin();
        when(enderecoRepository.findById(1)).thenReturn(Optional.of(endereco));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> enderecoService.getEnderecoById(1, outroUsuario)
        );

        assertEquals("Acesso negado", exception.getMessage());
        verify(enderecoRepository).findById(1);
        verifyNoInteractions(enderecoMapper);
    }

    // createNewEndereco(EnderecoCreateDTO enderecoRequest, Usuario usuario)

    @Test
    void deveCriarEnderecoComoPrincipalQuandoUsuarioAindaNaoPossuirEndereco() {
        Endereco enderecoMapeado = EnderecoFixture.enderecoMapeado();
        Cidade cidade = CidadeFixture.cidade();
        Coordenada coordenadaSnap = new Coordenada(-23.551, -46.634);

        when(enderecoMapper.toEntity(enderecoCreateDTO)).thenReturn(enderecoMapeado);
        when(enderecoRepository.findByUsuarioId(usuario.getId())).thenReturn(List.of());
        when(cidadeRepository.findByNome(enderecoCreateDTO.getCidade())).thenReturn(Optional.of(cidade));
        when(openRouteServiceGateway.snap(any(Coordenada.class))).thenReturn(coordenadaSnap);
        when(enderecoRepository.save(any(Endereco.class))).thenAnswer(invocation -> {
            Endereco salvo = invocation.getArgument(0);
            salvo.setId(1);
            return salvo;
        });
        when(enderecoMapper.toDTO(any(Endereco.class))).thenReturn(enderecoResponseDTO);

        EnderecoResponseDTO resultado = enderecoService.createNewEndereco(enderecoCreateDTO, usuario);

        ArgumentCaptor<Endereco> enderecoCaptor = ArgumentCaptor.forClass(Endereco.class);
        verify(enderecoRepository).save(enderecoCaptor.capture());
        Endereco enderecoSalvo = enderecoCaptor.getValue();

        assertEquals(1, enderecoSalvo.getId());
        assertSame(usuario, enderecoSalvo.getUsuario());
        assertTrue(enderecoSalvo.isPrincipal());
        assertEquals(enderecoCreateDTO.getRua(), enderecoSalvo.getRua());
        assertEquals(enderecoCreateDTO.getNumero(), enderecoSalvo.getNumero());
        assertEquals(enderecoCreateDTO.getCidade(), enderecoSalvo.getCidade());
        assertEquals(enderecoCreateDTO.getEstado(), enderecoSalvo.getEstado());
        assertEquals(enderecoCreateDTO.getCep(), enderecoSalvo.getCep());
        assertEquals(0, BigDecimal.valueOf(coordenadaSnap.latitude()).compareTo(enderecoSalvo.getLatitude()));
        assertEquals(0, BigDecimal.valueOf(coordenadaSnap.longitude()).compareTo(enderecoSalvo.getLongitude()));
        verify(cidadeRepository).findByNome(enderecoCreateDTO.getCidade());
        verify(openRouteServiceGateway).snap(any(Coordenada.class));
        verify(enderecoMapper).toDTO(enderecoSalvo);
        assertSame(enderecoResponseDTO, resultado);
    }

    @Test
    void deveCriarEnderecoSemDefinirComoPrincipalQuandoUsuarioJaPossuirEndereco() {
        Endereco enderecoExistente = EnderecoFixture.endereco();
        enderecoExistente.setPrincipal(true);
        Endereco enderecoMapeado = EnderecoFixture.enderecoMapeado();
        Cidade cidade = CidadeFixture.cidade();
        Coordenada coordenadaSnap = new Coordenada(-23.551, -46.634);

        when(enderecoMapper.toEntity(enderecoCreateDTO)).thenReturn(enderecoMapeado);
        when(enderecoRepository.findByUsuarioId(usuario.getId())).thenReturn(List.of(enderecoExistente));
        when(cidadeRepository.findByNome(enderecoCreateDTO.getCidade())).thenReturn(Optional.of(cidade));
        when(openRouteServiceGateway.snap(any(Coordenada.class))).thenReturn(coordenadaSnap);
        when(enderecoRepository.save(any(Endereco.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(enderecoMapper.toDTO(any(Endereco.class))).thenReturn(enderecoResponseDTO);

        enderecoService.createNewEndereco(enderecoCreateDTO, usuario);

        ArgumentCaptor<Endereco> enderecoCaptor = ArgumentCaptor.forClass(Endereco.class);
        verify(enderecoRepository).save(enderecoCaptor.capture());
        assertFalse(enderecoCaptor.getValue().isPrincipal());
        assertTrue(enderecoExistente.isPrincipal());
        verify(openRouteServiceGateway).snap(any(Coordenada.class));
    }

    @Test
    void deveLancarExcecaoAoCriarEnderecoQuandoCidadeNaoExistir() {
        Endereco enderecoMapeado = EnderecoFixture.enderecoMapeado();
        when(enderecoMapper.toEntity(enderecoCreateDTO)).thenReturn(enderecoMapeado);
        when(enderecoRepository.findByUsuarioId(usuario.getId())).thenReturn(List.of());
        when(cidadeRepository.findByNome(enderecoCreateDTO.getCidade())).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> enderecoService.createNewEndereco(enderecoCreateDTO, usuario)
        );

        assertEquals("Cidade não encontrada ou não cadastrada", exception.getMessage());
        verify(enderecoMapper).toEntity(enderecoCreateDTO);
        verify(enderecoRepository).findByUsuarioId(usuario.getId());
        verify(cidadeRepository).findByNome(enderecoCreateDTO.getCidade());
        verify(enderecoRepository, never()).save(any(Endereco.class));
        verify(enderecoMapper, never()).toDTO(any(Endereco.class));
        verifyNoInteractions(openRouteServiceGateway);
    }

    // deleteEnderecoById(Integer enderecoId, Usuario usuarioLogado)

    @Test
    void deveExcluirEnderecoQuandoUsuarioForODono() {
        when(enderecoRepository.findById(1)).thenReturn(Optional.of(endereco));

        assertDoesNotThrow(() -> enderecoService.deleteEnderecoById(1, usuario));

        verify(enderecoRepository).findById(1);
        verify(enderecoRepository).delete(endereco);
        verifyNoInteractions(enderecoMapper);
    }

    @Test
    void deveLancarExcecaoQuandoEnderecoNaoExistirAoExcluir() {
        when(enderecoRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> enderecoService.deleteEnderecoById(99, usuario)
        );

        assertEquals("Endereço com id 99 não encontrado", exception.getMessage());
        verify(enderecoRepository).findById(99);
        verify(enderecoRepository, never()).delete(any(Endereco.class));
        verifyNoInteractions(enderecoMapper);
    }

    @Test
    void deveLancarExcecaoAoExcluirEnderecoDeOutroUsuario() {
        Usuario outroUsuario = UsuarioFixture.usuarioAdmin();
        when(enderecoRepository.findById(1)).thenReturn(Optional.of(endereco));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> enderecoService.deleteEnderecoById(1, outroUsuario)
        );

        assertEquals("Acesso negado", exception.getMessage());
        verify(enderecoRepository).findById(1);
        verify(enderecoRepository, never()).delete(any(Endereco.class));
        verifyNoInteractions(enderecoMapper);
    }

    // updateEnderecoById(Integer enderecoId, EnderecoUpdateDTO enderecotualizar, Usuario usuarioLogado)

    @Test
    void deveAtualizarEnderecoQuandoUsuarioForODono() {
        when(enderecoRepository.findById(1)).thenReturn(Optional.of(endereco));
        when(enderecoMapper.toDTO(endereco)).thenReturn(enderecoResponseDTO);

        EnderecoResponseDTO resultado = enderecoService.updateEnderecoById(1, enderecoUpdateDTO, usuario);

        assertSame(enderecoResponseDTO, resultado);
        verify(enderecoRepository).findById(1);
        verify(enderecoMapper).updateFromDto(enderecoUpdateDTO, endereco);
        verify(enderecoMapper).toDTO(endereco);
        verify(enderecoRepository, never()).save(any(Endereco.class));
        verifyNoInteractions(cidadeRepository, openRouteServiceGateway);
    }

    @Test
    void deveLancarExcecaoQuandoEnderecoNaoExistirAoAtualizar() {
        when(enderecoRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> enderecoService.updateEnderecoById(99, enderecoUpdateDTO, usuario)
        );

        assertEquals("Endereço com id 99 não encontrado", exception.getMessage());
        verify(enderecoRepository).findById(99);
        verifyNoInteractions(enderecoMapper);
    }

    @Test
    void deveLancarExcecaoAoAtualizarEnderecoDeOutroUsuario() {
        Usuario outroUsuario = UsuarioFixture.usuarioAdmin();
        when(enderecoRepository.findById(1)).thenReturn(Optional.of(endereco));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> enderecoService.updateEnderecoById(1, enderecoUpdateDTO, outroUsuario)
        );

        assertEquals("Acesso negado", exception.getMessage());
        verify(enderecoRepository).findById(1);
        verify(enderecoMapper, never()).updateFromDto(any(EnderecoUpdateDTO.class), any(Endereco.class));
        verify(enderecoRepository, never()).save(any(Endereco.class));
    }

    // updateEnderecoPrincipalById(Integer enderecoId, Usuario usuarioLogado)

    @Test
    void deveDefinirEnderecoComoPrincipalERemoverPrincipalDosDemais() {
        Endereco principalAtual = EnderecoFixture.endereco();
        principalAtual.setId(2);
        principalAtual.setUsuario(usuario);
        principalAtual.setPrincipal(true);
        endereco.setPrincipal(false);

        when(enderecoRepository.findById(1)).thenReturn(Optional.of(endereco));
        when(enderecoRepository.findByUsuarioId(usuario.getId())).thenReturn(List.of(endereco, principalAtual));

        enderecoService.updateEnderecoPrincipalById(1, usuario);

        assertTrue(endereco.isPrincipal());
        assertFalse(principalAtual.isPrincipal());
        verify(enderecoRepository).findById(1);
        verify(enderecoRepository).findByUsuarioId(usuario.getId());
        verify(enderecoRepository, never()).save(any(Endereco.class));
        verifyNoInteractions(enderecoMapper, cidadeRepository, openRouteServiceGateway);
    }

    @Test
    void deveLancarExcecaoQuandoEnderecoNaoExistirAoDefinirPrincipal() {
        when(enderecoRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> enderecoService.updateEnderecoPrincipalById(99, usuario)
        );

        assertEquals("Endereço com id 99 não encontrado", exception.getMessage());
        verify(enderecoRepository).findById(99);
        verify(enderecoRepository, never()).findByUsuarioId(any(Integer.class));
        verifyNoInteractions(enderecoMapper);
    }

    @Test
    void deveLancarExcecaoAoDefinirPrincipalEmEnderecoDeOutroUsuario() {
        Usuario outroUsuario = UsuarioFixture.usuarioAdmin();
        when(enderecoRepository.findById(1)).thenReturn(Optional.of(endereco));

        RegraNegocioException exception = assertThrows(
                RegraNegocioException.class,
                () -> enderecoService.updateEnderecoPrincipalById(1, outroUsuario)
        );

        assertEquals("Acesso negado", exception.getMessage());
        assertTrue(endereco.isPrincipal());
        verify(enderecoRepository).findById(1);
        verify(enderecoRepository, never()).findByUsuarioId(any(Integer.class));
        verifyNoInteractions(enderecoMapper);
    }
}
