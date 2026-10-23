package guli.gulix.backend.service;

import guli.gulix.backend.dto.CidadeCreateDTO;
import guli.gulix.backend.dto.CidadeResponseDTO;
import guli.gulix.backend.dto.CidadeUpdateDTO;
import guli.gulix.backend.entity.Cidade;
import guli.gulix.backend.entity.Estado;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.fixture.CidadeFixture;
import guli.gulix.backend.fixture.EstadoFixture;
import guli.gulix.backend.mapper.CidadeMapper;
import guli.gulix.backend.repository.CidadeRepository;
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
class CidadeServiceImplTest {

    @Mock
    private CidadeRepository cidadeRepository;

    @Mock
    private CidadeMapper cidadeMapper;

    @Mock
    private EstadoRepository estadoRepository;

    @InjectMocks
    private CidadeServiceImpl cidadeService;

    private Cidade cidade;
    private Estado estado;
    private CidadeCreateDTO cidadeCreateDTO;
    private CidadeUpdateDTO cidadeUpdateDTO;
    private CidadeResponseDTO cidadeResponseDTO;

    @BeforeEach
    void setup() {
        cidade = CidadeFixture.cidade();
        estado = EstadoFixture.estado();
        cidadeCreateDTO = CidadeFixture.cidadeCreateDTO();
        cidadeUpdateDTO = CidadeFixture.cidadeUpdateDTO();
        cidadeResponseDTO = CidadeFixture.cidadeResponseDTO();
    }

    // getAllCidade()

    @Test
    void deveRetornarListaDeCidades() {
        when(cidadeRepository.findAll()).thenReturn(List.of(cidade));
        when(cidadeMapper.toDTO(cidade)).thenReturn(cidadeResponseDTO);

        List<CidadeResponseDTO> resultado = cidadeService.getAllCidade();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(cidadeResponseDTO, resultado.get(0));
        verify(cidadeRepository).findAll();
        verify(cidadeMapper).toDTO(cidade);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremCidades() {
        when(cidadeRepository.findAll()).thenReturn(List.of());

        List<CidadeResponseDTO> resultado = cidadeService.getAllCidade();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(cidadeRepository).findAll();
        verifyNoInteractions(cidadeMapper);
    }

    // getCidadeById(Integer cidadeId)

    @Test
    void deveRetornarCidadeQuandoIdExistir() {
        when(cidadeRepository.findById(1)).thenReturn(Optional.of(cidade));
        when(cidadeMapper.toDTO(cidade)).thenReturn(cidadeResponseDTO);

        CidadeResponseDTO resultado = cidadeService.getCidadeById(1);

        assertNotNull(resultado);
        assertEquals(cidadeResponseDTO, resultado);
        verify(cidadeRepository).findById(1);
        verify(cidadeMapper).toDTO(cidade);
    }

    @Test
    void deveLancarExcecaoQuandoCidadeNaoExistirAoBuscarPorId() {
        when(cidadeRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> cidadeService.getCidadeById(99)
        );

        assertEquals("Cidade com id 99 não encontrado", exception.getMessage());
        verify(cidadeRepository).findById(99);
        verifyNoInteractions(cidadeMapper);
    }

    // createNewCidade(CidadeCreateDTO dto)

    @Test
    void deveCriarCidadeQuandoEstadoExistir() {
        when(cidadeMapper.toEntity(cidadeCreateDTO)).thenReturn(cidade);
        when(estadoRepository.findById(cidadeCreateDTO.estadoId())).thenReturn(Optional.of(estado));
        when(cidadeRepository.save(cidade)).thenReturn(cidade);
        when(cidadeMapper.toDTO(cidade)).thenReturn(cidadeResponseDTO);

        CidadeResponseDTO resultado = cidadeService.createNewCidade(cidadeCreateDTO);

        assertNotNull(resultado);
        assertEquals(cidadeResponseDTO, resultado);
        assertEquals(estado, cidade.getEstado());
        verify(cidadeMapper).toEntity(cidadeCreateDTO);
        verify(estadoRepository).findById(cidadeCreateDTO.estadoId());
        verify(cidadeRepository).save(cidade);
        verify(cidadeMapper).toDTO(cidade);
    }

    @Test
    void deveLancarExcecaoQuandoEstadoNaoExistirAoCriarCidade() {
        when(cidadeMapper.toEntity(cidadeCreateDTO)).thenReturn(cidade);
        when(estadoRepository.findById(cidadeCreateDTO.estadoId())).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> cidadeService.createNewCidade(cidadeCreateDTO)
        );

        assertEquals(
                "Estado com id " + cidadeCreateDTO.estadoId() + " não encontrado",
                exception.getMessage()
        );
        verify(cidadeMapper).toEntity(cidadeCreateDTO);
        verify(estadoRepository).findById(cidadeCreateDTO.estadoId());
        verify(cidadeRepository, never()).save(any(Cidade.class));
        verify(cidadeMapper, never()).toDTO(any(Cidade.class));
    }

    // updateCidadeById(Integer cidadeId, CidadeUpdateDTO dto)

    @Test
    void deveAtualizarCidadeQuandoIdExistirSemAlterarEstado() {
        CidadeUpdateDTO dto = new CidadeUpdateDTO(
                "Campinas",
                null,
                cidadeUpdateDTO.latitudeCentral(),
                cidadeUpdateDTO.longitudeCentral()
        );

        when(cidadeRepository.findById(1)).thenReturn(Optional.of(cidade));
        when(cidadeMapper.toDTO(cidade)).thenReturn(cidadeResponseDTO);

        CidadeResponseDTO resultado = cidadeService.updateCidadeById(1, dto);

        assertNotNull(resultado);
        assertEquals(cidadeResponseDTO, resultado);
        verify(cidadeRepository).findById(1);
        verify(cidadeMapper).updateFromDto(dto, cidade);
        verify(cidadeMapper).toDTO(cidade);
        verify(estadoRepository, never()).findById(anyInt());
        verify(cidadeRepository, never()).save(any(Cidade.class));
    }

    @Test
    void deveAtualizarEstadoDaCidadeQuandoNovoEstadoExistir() {
        Estado novoEstado = new Estado();
        novoEstado.setId(2);
        novoEstado.setNome("Rio de Janeiro");
        novoEstado.setSigla("RJ");

        CidadeUpdateDTO dto = new CidadeUpdateDTO(
                "Campinas",
                2,
                cidadeUpdateDTO.latitudeCentral(),
                cidadeUpdateDTO.longitudeCentral()
        );

        when(cidadeRepository.findById(1)).thenReturn(Optional.of(cidade));
        when(estadoRepository.findById(2)).thenReturn(Optional.of(novoEstado));
        when(cidadeMapper.toDTO(cidade)).thenReturn(cidadeResponseDTO);

        CidadeResponseDTO resultado = cidadeService.updateCidadeById(1, dto);

        assertNotNull(resultado);
        assertEquals(cidadeResponseDTO, resultado);
        assertEquals(novoEstado, cidade.getEstado());
        verify(cidadeRepository).findById(1);
        verify(estadoRepository).findById(2);
        verify(cidadeMapper).updateFromDto(dto, cidade);
        verify(cidadeMapper).toDTO(cidade);
    }

    @Test
    void deveLancarExcecaoQuandoCidadeNaoExistirAoAtualizar() {
        when(cidadeRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> cidadeService.updateCidadeById(99, cidadeUpdateDTO)
        );

        assertEquals("Cidade com id 99 não encontrado", exception.getMessage());
        verify(cidadeRepository).findById(99);
        verifyNoInteractions(cidadeMapper);
        verifyNoInteractions(estadoRepository);
    }

    @Test
    void deveLancarExcecaoQuandoEstadoNaoExistirAoAtualizarCidade() {
        CidadeUpdateDTO dto = new CidadeUpdateDTO(
                "Campinas",
                99,
                cidadeUpdateDTO.latitudeCentral(),
                cidadeUpdateDTO.longitudeCentral()
        );

        when(cidadeRepository.findById(1)).thenReturn(Optional.of(cidade));
        when(estadoRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> cidadeService.updateCidadeById(1, dto)
        );

        assertEquals("Estado com id 99 não encontrado", exception.getMessage());
        verify(cidadeRepository).findById(1);
        verify(estadoRepository).findById(99);
        verify(cidadeMapper, never()).updateFromDto(any(CidadeUpdateDTO.class), any(Cidade.class));
        verify(cidadeMapper, never()).toDTO(any(Cidade.class));
    }

    // deleteCidadeById(Integer cidadeId)

    @Test
    void deveExcluirCidadeQuandoIdExistir() {
        when(cidadeRepository.findById(1)).thenReturn(Optional.of(cidade));

        assertDoesNotThrow(() -> cidadeService.deleteCidadeById(1));

        verify(cidadeRepository).findById(1);
        verify(cidadeRepository).delete(cidade);
        verifyNoInteractions(cidadeMapper);
        verifyNoInteractions(estadoRepository);
    }

    @Test
    void deveLancarExcecaoQuandoCidadeNaoExistirAoExcluir() {
        when(cidadeRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> cidadeService.deleteCidadeById(99)
        );

        assertEquals("Cidade com id 99 não encontrado", exception.getMessage());
        verify(cidadeRepository).findById(99);
        verify(cidadeRepository, never()).delete(any(Cidade.class));
        verifyNoInteractions(cidadeMapper);
        verifyNoInteractions(estadoRepository);
    }
}
