package guli.gulix.backend.service;

import guli.gulix.backend.dto.EstadoCreateDTO;
import guli.gulix.backend.dto.EstadoResponseDTO;
import guli.gulix.backend.dto.EstadoUpdateDTO;
import guli.gulix.backend.entity.Estado;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.fixture.EstadoFixture;
import guli.gulix.backend.mapper.EstadoMapper;
import guli.gulix.backend.repository.EstadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstadoServiceImplTest {

    @Mock
    private EstadoRepository estadoRepository;

    @Mock
    private EstadoMapper estadoMapper;

    @InjectMocks
    private EstadoServiceImpl estadoService;

    private Estado estado;
    private EstadoCreateDTO estadoCreateDTO;
    private EstadoUpdateDTO estadoUpdateDTO;
    private EstadoResponseDTO estadoResponseDTO;

    @BeforeEach
    void setup() {
        estado = EstadoFixture.estado();
        estadoCreateDTO = EstadoFixture.estadoCreateDTO();
        estadoUpdateDTO = EstadoFixture.estadoUpdateDTO();
        estadoResponseDTO = EstadoFixture.estadoResponseDTO();
    }

    // getAllEstado()

    @Test
    void deveRetornarListaDeEstados() {
        when(estadoRepository.findAll()).thenReturn(List.of(estado));
        when(estadoMapper.toDTO(estado)).thenReturn(estadoResponseDTO);

        List<EstadoResponseDTO> resultado = estadoService.getAllEstado();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(estadoResponseDTO, resultado.get(0));
        verify(estadoRepository).findAll();
        verify(estadoMapper).toDTO(estado);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremEstados() {
        when(estadoRepository.findAll()).thenReturn(List.of());

        List<EstadoResponseDTO> resultado = estadoService.getAllEstado();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(estadoRepository).findAll();
        verifyNoInteractions(estadoMapper);
    }

    // getEstadoById(Integer estadoId)

    @Test
    void deveRetornarEstadoQuandoIdExistir() {
        when(estadoRepository.findById(1)).thenReturn(Optional.of(estado));
        when(estadoMapper.toDTO(estado)).thenReturn(estadoResponseDTO);

        EstadoResponseDTO resultado = estadoService.getEstadoById(1);

        assertNotNull(resultado);
        assertEquals(estadoResponseDTO, resultado);
        verify(estadoRepository).findById(1);
        verify(estadoMapper).toDTO(estado);
    }

    @Test
    void deveLancarExcecaoQuandoEstadoNaoExistirAoBuscarPorId() {
        when(estadoRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> estadoService.getEstadoById(99)
        );

        assertEquals("Estado com id 99 não encontrado", exception.getMessage());
        verify(estadoRepository).findById(99);
        verifyNoInteractions(estadoMapper);
    }

    // createNewEstado(EstadoCreateDTO dto)

    @Test
    void deveCriarEstado() {
        when(estadoMapper.toEntity(estadoCreateDTO)).thenReturn(estado);
        when(estadoRepository.save(estado)).thenReturn(estado);
        when(estadoMapper.toDTO(estado)).thenReturn(estadoResponseDTO);

        EstadoResponseDTO resultado = estadoService.createNewEstado(estadoCreateDTO);

        assertNotNull(resultado);
        assertEquals(estadoResponseDTO, resultado);
        verify(estadoMapper).toEntity(estadoCreateDTO);
        verify(estadoRepository).save(estado);
        verify(estadoMapper).toDTO(estado);
    }

    // updateEstadoById(Integer estadoId, EstadoUpdateDTO dto)

    @Test
    void deveAtualizarEstadoQuandoIdExistir() {
        when(estadoRepository.findById(1)).thenReturn(Optional.of(estado));
        when(estadoMapper.toDTO(estado)).thenReturn(estadoResponseDTO);

        EstadoResponseDTO resultado = estadoService.updateEstadoById(1, estadoUpdateDTO);

        assertNotNull(resultado);
        assertEquals(estadoResponseDTO, resultado);
        verify(estadoRepository).findById(1);
        verify(estadoMapper).updateFromDto(estadoUpdateDTO, estado);
        verify(estadoMapper).toDTO(estado);
        verify(estadoRepository, never()).save(any(Estado.class));
    }

    @Test
    void deveLancarExcecaoQuandoEstadoNaoExistirAoAtualizar() {
        when(estadoRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> estadoService.updateEstadoById(99, estadoUpdateDTO)
        );

        assertEquals("Estado com id 99 não encontrado", exception.getMessage());
        verify(estadoRepository).findById(99);
        verifyNoInteractions(estadoMapper);
    }

    // deleteEstadoById(Integer estadoId)

    @Test
    void deveExcluirEstadoQuandoIdExistir() {
        when(estadoRepository.findById(1)).thenReturn(Optional.of(estado));

        assertDoesNotThrow(() -> estadoService.deleteEstadoById(1));

        verify(estadoRepository).findById(1);
        verify(estadoRepository).delete(estado);
        verifyNoInteractions(estadoMapper);
    }

    @Test
    void deveLancarExcecaoQuandoEstadoNaoExistirAoExcluir() {
        when(estadoRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> estadoService.deleteEstadoById(99)
        );

        assertEquals("Estado com id 99 não encontrado", exception.getMessage());
        verify(estadoRepository).findById(99);
        verify(estadoRepository, never()).delete(any(Estado.class));
        verifyNoInteractions(estadoMapper);
    }
}
