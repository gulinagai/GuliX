package guli.gulix.backend.service;

import guli.gulix.backend.dto.FilialResponseDTO;
import guli.gulix.backend.dto.FilialUpdateDTO;
import guli.gulix.backend.entity.Filial;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.fixture.FilialFixture;
import guli.gulix.backend.mapper.FilialMapper;
import guli.gulix.backend.repository.FilialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilialServiceImplTest {

    @Mock
    private FilialRepository filialRepository;

    @Mock
    private FilialMapper filialMapper;

    @InjectMocks
    private FilialServiceImpl filialService;

    private Filial filial;
    private FilialUpdateDTO filialUpdateDTO;
    private FilialResponseDTO filialResponseDTO;

    @BeforeEach
    void setup() {
        filial = FilialFixture.filial();
        filialUpdateDTO = FilialFixture.filialUpdateDTO();
        filialResponseDTO = FilialFixture.filialResponseDTO();
    }

    // getFilial()

    @Test
    void deveRetornarFilialCadastrada() {
        // Arrange
        when(filialRepository.findFirstBy()).thenReturn(Optional.of(filial));
        when(filialMapper.toDTO(filial)).thenReturn(filialResponseDTO);

        // Act
        FilialResponseDTO resultado = filialService.getFilial();

        // Assert
        assertNotNull(resultado);
        assertEquals(filialResponseDTO, resultado);

        verify(filialRepository).findFirstBy();
        verify(filialMapper).toDTO(filial);
    }

    @Test
    void deveLancarExcecaoQuandoFilialNaoEncontradaAoBuscar() {
        // Arrange
        when(filialRepository.findFirstBy()).thenReturn(Optional.empty());

        // Act
        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> filialService.getFilial()
        );

        // Assert
        assertEquals("Filial não encontrada", exception.getMessage());

        verify(filialRepository).findFirstBy();
        verifyNoInteractions(filialMapper);
    }

    // updateFilial(FilialUpdateDTO dto)

    @Test
    void deveRetornarFilialAtualizada() {
        // Arrange
        when(filialRepository.findFirstBy()).thenReturn(Optional.of(filial));
        when(filialMapper.toDTO(filial)).thenReturn(filialResponseDTO);

        // Act
        FilialResponseDTO resultado = filialService.updateFilial(filialUpdateDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(filialResponseDTO, resultado);

        verify(filialRepository).findFirstBy();
        verify(filialMapper).updateFromDto(filialUpdateDTO, filial);
        verify(filialMapper).toDTO(filial);
    }

    @Test
    void deveLancarExcecaoQuandoFilialNaoEncontradaAoAtualizar() {
        // Arrange
        when(filialRepository.findFirstBy()).thenReturn(Optional.empty());

        // Act
        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> filialService.updateFilial(filialUpdateDTO)
        );

        // Assert
        assertEquals("Filial não encontrada", exception.getMessage());

        verify(filialRepository).findFirstBy();
        verifyNoInteractions(filialMapper);
    }
}
