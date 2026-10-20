package guli.gulix.backend.service;

import guli.gulix.backend.dto.EmpresaResponseDTO;
import guli.gulix.backend.dto.EmpresaUpdateDTO;
import guli.gulix.backend.entity.Empresa;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.fixture.EmpresaFixture;
import guli.gulix.backend.mapper.EmpresaMapper;
import guli.gulix.backend.repository.EmpresaRepository;
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
public class EmpresaServiceImplTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private EmpresaMapper empresaMapper;

    @InjectMocks
    private EmpresaServiceImpl empresaService;

    private Empresa empresa;
    private EmpresaUpdateDTO empresaUpdateDTO;
    private EmpresaResponseDTO empresaResponseDTO;

    @BeforeEach
    void setup() {
        empresa = EmpresaFixture.empresa();
        empresaUpdateDTO = EmpresaFixture.empresaUpdateDTO();
        empresaResponseDTO = EmpresaFixture.empresaResponseDTO();
    }


    // getEmpresa()

    @Test
    void deveRetornarEmpresaCadastrada() {

        // Arrange

        when(empresaRepository.findFirstBy()).thenReturn(Optional.of(empresa));
        when(empresaMapper.toDTO(empresa)).thenReturn(empresaResponseDTO);

        // Act

        EmpresaResponseDTO resultado = empresaService.getEmpresa();

        // Assert

        assertNotNull(resultado);
        assertEquals(empresaResponseDTO, resultado);

        verify(empresaRepository).findFirstBy();
        verify(empresaMapper).toDTO(empresa);
    }


    @Test
    void deveLancarExcecaoQuandoEmpresaNaoEncontradaAoBuscar() {

        // Arrange

        when(empresaRepository.findFirstBy()).thenReturn(Optional.empty());

        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> empresaService.getEmpresa()
        );

        // Assert

        assertEquals("Empresa não encontrada", exception.getMessage());

        verify(empresaRepository).findFirstBy();
        verifyNoInteractions(empresaMapper);
    }


    // updateEmpresa(EmpresaUpdateDTO empresaUpdateDTO)

    // 1. happy path

    @Test
    void deveRetornarEmpresaAtualizada() {

        // Arrange

        EmpresaResponseDTO empresaAtualizadaDTO = new EmpresaResponseDTO(
                empresa.getId(),
                empresaUpdateDTO.razaoSocial(),
                empresaUpdateDTO.nomeFantasia(),
                empresaUpdateDTO.cnpj(),
                empresaUpdateDTO.valorPorKmFrete()
        );

        when(empresaRepository.findFirstBy()).thenReturn(Optional.of(empresa));
        when(empresaMapper.toDTO(empresa)).thenReturn(empresaAtualizadaDTO);

        // Act

        EmpresaResponseDTO resultado = empresaService.updateEmpresa(empresaUpdateDTO);

        // Assert

        assertNotNull(resultado);
        assertEquals(empresaAtualizadaDTO, resultado);

        verify(empresaRepository).findFirstBy();
        verify(empresaMapper).updateFromDto(empresaUpdateDTO, empresa);
        verify(empresaMapper).toDTO(empresa);
    }


    // 2. Empresa não encontrada

    @Test
    void deveLancarExcecaoQuandoEmpresaNaoEncontradaAoAtualizar() {

        // Arrange

        when(empresaRepository.findFirstBy()).thenReturn(Optional.empty());

        // Act

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> empresaService.updateEmpresa(empresaUpdateDTO)
        );

        // Assert

        assertEquals("Empresa não encontrada", exception.getMessage());

        verify(empresaRepository).findFirstBy();
        verifyNoInteractions(empresaMapper);
    }

}
