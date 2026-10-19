package guli.gulix.backend.service;

import guli.gulix.backend.dto.MarcaRequestDTO;
import guli.gulix.backend.dto.MarcaResponseDTO;
import guli.gulix.backend.entity.Marca;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.fixture.MarcaFixture;
import guli.gulix.backend.mapper.MarcaMapper;
import guli.gulix.backend.repository.MarcaRepository;
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
class MarcaServiceImplTest {

    @Mock
    private MarcaRepository marcaRepository;

    @Mock
    private MarcaMapper marcaMapper;

    @InjectMocks
    private MarcaServiceImpl marcaService;

    private Marca marca;
    private MarcaRequestDTO marcaRequestDTO;
    private MarcaResponseDTO marcaResponseDTO;

    @BeforeEach
    void setup() {
        marca = MarcaFixture.marca();
        marcaRequestDTO = MarcaFixture.marcaRequestDTO();
        marcaResponseDTO = MarcaFixture.marcaResponseDTO();
    }

    // getListMarca()

    @Test
    void deveRetornarListaDeMarcasQuandoExistiremMarcas() {
        // Arrange
        when(marcaRepository.findAll()).thenReturn(List.of(marca));
        when(marcaMapper.toDTO(marca)).thenReturn(marcaResponseDTO);

        // Act
        List<MarcaResponseDTO> resultado = marcaService.getListMarca();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(marcaResponseDTO, resultado.get(0));
        verify(marcaRepository).findAll();
        verify(marcaMapper).toDTO(marca);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremMarcas() {
        // Arrange
        when(marcaRepository.findAll()).thenReturn(List.of());

        // Act
        List<MarcaResponseDTO> resultado = marcaService.getListMarca();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(marcaRepository).findAll();
        verifyNoInteractions(marcaMapper);
    }

    // getMarcaById()

    @Test
    void deveRetornarMarcaQuandoIdExistir() {
        // Arrange
        when(marcaRepository.findById(1)).thenReturn(Optional.of(marca));
        when(marcaMapper.toDTO(marca)).thenReturn(marcaResponseDTO);

        // Act
        MarcaResponseDTO resultado = marcaService.getMarcaById(1);

        // Assert
        assertNotNull(resultado);
        assertEquals(marcaResponseDTO, resultado);
        verify(marcaRepository).findById(1);
        verify(marcaMapper).toDTO(marca);
    }

    @Test
    void deveLancarExcecaoQuandoMarcaNaoEncontradaAoBuscarPorId() {
        // Arrange
        when(marcaRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> marcaService.getMarcaById(99)
        );

        // Assert
        assertEquals("Marca com id 99 não encontrado", exception.getMessage());
        verify(marcaRepository).findById(99);
        verifyNoInteractions(marcaMapper);
    }

    // createNewMarca()

    @Test
    void deveRetornarNovaMarca() {
        // Arrange
        Marca novaMarca = MarcaFixture.marca();
        novaMarca.setId(2);

        MarcaResponseDTO respostaEsperada =
                new MarcaResponseDTO(2, marcaRequestDTO.getNome());

        when(marcaMapper.toEntity(marcaRequestDTO)).thenReturn(novaMarca);
        when(marcaRepository.save(novaMarca)).thenReturn(novaMarca);
        when(marcaMapper.toDTO(novaMarca)).thenReturn(respostaEsperada);

        // Act
        MarcaResponseDTO resultado = marcaService.createNewMarca(marcaRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(respostaEsperada, resultado);
        verify(marcaMapper).toEntity(marcaRequestDTO);
        verify(marcaRepository).save(novaMarca);
        verify(marcaMapper).toDTO(novaMarca);
    }

    // updateMarcaById()

    @Test
    void deveRetornarMarcaAtualizadaQuandoIdExistir() {
        // Arrange
        when(marcaRepository.findById(1)).thenReturn(Optional.of(marca));
        when(marcaMapper.toDTO(marca)).thenReturn(marcaResponseDTO);

        // Act
        MarcaResponseDTO resultado = marcaService.updateMarcaById(1, marcaRequestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(marcaResponseDTO, resultado);
        verify(marcaRepository).findById(1);
        verify(marcaMapper).updateFromDto(marcaRequestDTO, marca);
        verify(marcaMapper).toDTO(marca);
        verify(marcaRepository, never()).save(any(Marca.class));
    }

    @Test
    void deveLancarExcecaoQuandoMarcaNaoEncontradaAoAtualizar() {
        // Arrange
        when(marcaRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> marcaService.updateMarcaById(99, marcaRequestDTO)
        );

        // Assert
        assertEquals("Marca com id 99 não encontrado", exception.getMessage());
        verify(marcaRepository).findById(99);
        verifyNoInteractions(marcaMapper);
    }

    // deleteMarcaById()

    @Test
    void deveExcluirMarcaQuandoIdExistir() {
        // Arrange
        when(marcaRepository.findById(1)).thenReturn(Optional.of(marca));

        // Act
        assertDoesNotThrow(() -> marcaService.deleteMarcaById(1));

        // Assert
        verify(marcaRepository).findById(1);
        verify(marcaRepository).delete(marca);
        verifyNoInteractions(marcaMapper);
    }

    @Test
    void deveLancarExcecaoQuandoMarcaNaoEncontradaAoExcluir() {
        // Arrange
        when(marcaRepository.findById(99)).thenReturn(Optional.empty());

        // Act
        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> marcaService.deleteMarcaById(99)
        );

        // Assert
        assertEquals("Marca com id 99 não encontrado", exception.getMessage());
        verify(marcaRepository).findById(99);
        verify(marcaRepository, never()).delete(any(Marca.class));
        verifyNoInteractions(marcaMapper);
    }
}
