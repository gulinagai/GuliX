package guli.gulix.backend.service;

import guli.gulix.backend.dto.UsuarioAdminResponseDTO;
import guli.gulix.backend.dto.UsuarioCreateDTO;
import guli.gulix.backend.dto.UsuarioResponseDTO;
import guli.gulix.backend.dto.UsuarioUpdateDTO;
import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.entity.enums.Role;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.fixture.UsuarioFixture;
import guli.gulix.backend.mapper.UsuarioMapper;
import guli.gulix.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuario;
    private UsuarioCreateDTO usuarioCreateDTO;
    private UsuarioUpdateDTO usuarioUpdateDTO;
    private UsuarioResponseDTO usuarioResponseDTO;
    private UsuarioAdminResponseDTO usuarioAdminResponseDTO;

    @BeforeEach
    void setup() {
        usuario = UsuarioFixture.usuario();
        usuarioCreateDTO = UsuarioFixture.usuarioCreateDTO();
        usuarioUpdateDTO = UsuarioFixture.usuarioUpdateDTO();
        usuarioResponseDTO = UsuarioFixture.usuarioResponseDTO();
        usuarioAdminResponseDTO = UsuarioFixture.usuarioAdminResponseDTO();
    }

    // getListUsuario()

    @Test
    void deveRetornarListaDeUsuarios() {

        // Arrange

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(usuarioMapper.toDTOAdmin(usuario)).thenReturn(usuarioAdminResponseDTO);

        // Act

        List<UsuarioAdminResponseDTO> resultado = usuarioService.getListUsuario();

        // Assert

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(usuarioAdminResponseDTO, resultado.get(0));

        verify(usuarioRepository).findAll();
        verify(usuarioMapper).toDTOAdmin(usuario);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremUsuarios() {

        // Arrange

        when(usuarioRepository.findAll()).thenReturn(List.of());

        // Act

        List<UsuarioAdminResponseDTO> resultado = usuarioService.getListUsuario();

        // Assert

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(usuarioRepository).findAll();
        verifyNoInteractions(usuarioMapper);
    }

    // getUsuarioById(Integer usuarioId)

    @Test
    void deveRetornarUsuarioQuandoIdExistir() {

        // Arrange

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toDTO(usuario)).thenReturn(usuarioResponseDTO);

        // Act

        UsuarioResponseDTO resultado = usuarioService.getUsuarioById(1);

        // Assert

        assertNotNull(resultado);
        assertEquals(usuarioResponseDTO, resultado);

        verify(usuarioRepository).findById(1);
        verify(usuarioMapper).toDTO(usuario);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoForEncontradoAoBuscarPorId() {

        // Arrange

        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> usuarioService.getUsuarioById(99)
        );

        // Assert

        assertEquals("Usuário com id99 não encontrado", exception.getMessage());

        verify(usuarioRepository).findById(99);
        verifyNoInteractions(usuarioMapper);
    }

    // createNewUsuario(UsuarioCreateDTO usuarioRequest)

    @Test
    void deveCriarUsuarioComSenhaCriptografadaEPerfilUsuario() {

        // Arrange

        String senhaCriptografada = "$2a$10$hashDeTeste";

        usuario.setRole(Role.ROLE_ADMIN);

        when(usuarioMapper.toEntity(usuarioCreateDTO)).thenReturn(usuario);
        when(passwordEncoder.encode(usuarioCreateDTO.getSenha()))
                .thenReturn(senhaCriptografada);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.toDTO(usuario)).thenReturn(usuarioResponseDTO);

        // Act

        UsuarioResponseDTO resultado = usuarioService.createNewUsuario(usuarioCreateDTO);

        // Assert

        assertNotNull(resultado);
        assertEquals(usuarioResponseDTO, resultado);
        assertEquals(senhaCriptografada, usuario.getSenhaHash());
        assertEquals(Role.ROLE_USER, usuario.getRole());

        verify(usuarioMapper).toEntity(usuarioCreateDTO);
        verify(passwordEncoder).encode(usuarioCreateDTO.getSenha());
        verify(usuarioRepository).save(usuario);
        verify(usuarioMapper).toDTO(usuario);
    }

    // updateUsuarioById(Integer usuarioId, UsuarioUpdateDTO usuarioAtualizar)

    @Test
    void deveAtualizarUsuarioExistente() {

        // Arrange

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toDTO(usuario)).thenReturn(usuarioResponseDTO);

        // Act

        UsuarioResponseDTO resultado = usuarioService.updateUsuarioById(
                1,
                usuarioUpdateDTO
        );

        // Assert

        assertNotNull(resultado);
        assertEquals(usuarioResponseDTO, resultado);

        verify(usuarioRepository).findById(1);
        verify(usuarioMapper).updateEntityfromDTO(usuarioUpdateDTO, usuario);
        verify(passwordEncoder).encode(usuarioUpdateDTO.getSenha());
        verify(usuarioMapper).toDTO(usuario);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExistirAoAtualizar() {

        // Arrange

        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> usuarioService.updateUsuarioById(99, usuarioUpdateDTO)
        );

        // Assert

        assertEquals("Usuário com id99 não encontrado", exception.getMessage());

        verify(usuarioRepository).findById(99);
        verifyNoInteractions(usuarioMapper, passwordEncoder);
    }

    @Test
    void naoDeveCriptografarSenhaQuandoSenhaForNulaNaAtualizacao() {

        // Arrange

        UsuarioUpdateDTO request = new UsuarioUpdateDTO(
                "Usuario Atualizado",
                null,
                "11912345678"
        );

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toDTO(usuario)).thenReturn(usuarioResponseDTO);

        // Act

        UsuarioResponseDTO resultado = usuarioService.updateUsuarioById(1, request);

        // Assert

        assertNotNull(resultado);

        verify(usuarioRepository).findById(1);
        verify(usuarioMapper).updateEntityfromDTO(request, usuario);
        verify(usuarioMapper).toDTO(usuario);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void naoDeveCriptografarSenhaQuandoSenhaEstiverEmBrancoNaAtualizacao() {

        // Arrange

        UsuarioUpdateDTO request = new UsuarioUpdateDTO(
                "Usuario Atualizado",
                "   ",
                "11912345678"
        );

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toDTO(usuario)).thenReturn(usuarioResponseDTO);

        // Act

        UsuarioResponseDTO resultado = usuarioService.updateUsuarioById(1, request);

        // Assert

        assertNotNull(resultado);

        verify(usuarioRepository).findById(1);
        verify(usuarioMapper).updateEntityfromDTO(request, usuario);
        verify(usuarioMapper).toDTO(usuario);
        verifyNoInteractions(passwordEncoder);
    }

    // updatePatchUsuarioById(Integer usuarioId, UsuarioUpdateDTO usuarioAtualizar)

    @Test
    void deveAtualizarUsuarioParcialmente() {

        // Arrange

        UsuarioUpdateDTO request = new UsuarioUpdateDTO(
                "Nome Atualizado",
                null,
                "11912345678"
        );

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toDTO(usuario)).thenReturn(usuarioResponseDTO);

        // Act

        UsuarioResponseDTO resultado = usuarioService.updatePatchUsuarioById(1, request);

        // Assert

        assertNotNull(resultado);
        assertEquals(usuarioResponseDTO, resultado);

        verify(usuarioRepository).findById(1);
        verify(usuarioMapper).updateEntityfromDTO(request, usuario);
        verify(usuarioMapper).toDTO(usuario);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deveCriptografarNovaSenhaAoAtualizarParcialmente() {

        // Arrange

        String novaSenha = "NovaSenha123";
        String senhaCriptografada = "$2a$10$novoHash";

        UsuarioUpdateDTO request = new UsuarioUpdateDTO(
                null,
                novaSenha,
                null
        );

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode(novaSenha)).thenReturn(senhaCriptografada);
        when(usuarioMapper.toDTO(usuario)).thenReturn(usuarioResponseDTO);

        // Act

        UsuarioResponseDTO resultado = usuarioService.updatePatchUsuarioById(1, request);

        // Assert

        assertNotNull(resultado);
        assertEquals(senhaCriptografada, usuario.getSenhaHash());

        verify(usuarioRepository).findById(1);
        verify(usuarioMapper).updateEntityfromDTO(request, usuario);
        verify(passwordEncoder).encode(novaSenha);
        verify(usuarioMapper).toDTO(usuario);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExistirAoAtualizarParcialmente() {

        // Arrange

        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> usuarioService.updatePatchUsuarioById(99, usuarioUpdateDTO)
        );

        // Assert

        assertEquals("Usuário com id 99 não encontrado", exception.getMessage());

        verify(usuarioRepository).findById(99);
        verifyNoInteractions(usuarioMapper, passwordEncoder);
    }

    // deleteUsuarioById(Integer usuarioId)

    @Test
    void deveExcluirUsuarioQuandoIdExistir() {

        // Arrange

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

        // Act

        assertDoesNotThrow(() -> usuarioService.deleteUsuarioById(1));

        // Assert

        verify(usuarioRepository).findById(1);
        verify(usuarioRepository).delete(usuario);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExistirAoExcluir() {

        // Arrange

        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> usuarioService.deleteUsuarioById(99)
        );

        // Assert

        assertEquals("Usuário com id 99 não encontrado", exception.getMessage());

        verify(usuarioRepository).findById(99);
        verify(usuarioRepository, never()).delete(any(Usuario.class));
    }
}
