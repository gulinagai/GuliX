package guli.gulix.backend.service;

import guli.gulix.backend.dto.PoliticaFreteCreateDTO;
import guli.gulix.backend.dto.PoliticaFreteResponseDTO;
import guli.gulix.backend.entity.Estado;
import guli.gulix.backend.entity.PoliticaFrete;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.fixture.EstadoFixture;
import guli.gulix.backend.fixture.PoliticaFreteFixture;
import guli.gulix.backend.mapper.PoliticaFreteMapper;
import guli.gulix.backend.repository.EstadoRepository;
import guli.gulix.backend.repository.PoliticaFreteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PoliticaFreteServiceImplTest {

    @Mock
    private PoliticaFreteRepository politicaFreteRepository;

    @Mock
    private PoliticaFreteMapper politicaFreteMapper;

    @Mock
    private EstadoRepository estadoRepository;

    @InjectMocks
    private PoliticaFreteServiceImpl politicaFreteService;

    private PoliticaFrete politicaFrete;
    private Estado estado;
    private PoliticaFreteCreateDTO createDTO;
    private PoliticaFreteResponseDTO responseDTO;

    @BeforeEach
    void setup() {
        politicaFrete = PoliticaFreteFixture.politicaFrete();
        estado = EstadoFixture.estado();
        createDTO = PoliticaFreteFixture.politicaFreteCreateDTO();
        responseDTO = PoliticaFreteFixture.politicaFreteResponseDTO();
    }

    // getAllPoliticaFrete()

    @Test
    void deveRetornarListaDePoliticasDeFrete() {
        when(politicaFreteRepository.findAll()).thenReturn(List.of(politicaFrete));
        when(politicaFreteMapper.toDTO(politicaFrete)).thenReturn(responseDTO);

        List<PoliticaFreteResponseDTO> resultado = politicaFreteService.getAllPoliticaFrete();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(responseDTO, resultado.get(0));
        verify(politicaFreteRepository).findAll();
        verify(politicaFreteMapper).toDTO(politicaFrete);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremPoliticasDeFrete() {
        when(politicaFreteRepository.findAll()).thenReturn(List.of());

        List<PoliticaFreteResponseDTO> resultado = politicaFreteService.getAllPoliticaFrete();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(politicaFreteRepository).findAll();
        verifyNoInteractions(politicaFreteMapper);
    }

    // getPoliticaFreteById(Integer politicaFreteId)

    @Test
    void deveRetornarPoliticaDeFreteQuandoIdExistir() {
        when(politicaFreteRepository.findById(1)).thenReturn(Optional.of(politicaFrete));
        when(politicaFreteMapper.toDTO(politicaFrete)).thenReturn(responseDTO);

        PoliticaFreteResponseDTO resultado = politicaFreteService.getPoliticaFreteById(1);

        assertNotNull(resultado);
        assertEquals(responseDTO, resultado);
        verify(politicaFreteRepository).findById(1);
        verify(politicaFreteMapper).toDTO(politicaFrete);
    }

    @Test
    void deveLancarExcecaoQuandoPoliticaDeFreteNaoExistirAoBuscarPorId() {
        when(politicaFreteRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> politicaFreteService.getPoliticaFreteById(99)
        );

        assertEquals("PoliticaFrete com id 99 não encontrado", exception.getMessage());
        verify(politicaFreteRepository).findById(99);
        verifyNoInteractions(politicaFreteMapper);
    }

    // createNewPoliticaFrete(PoliticaFreteCreateDTO dto)

    @Test
    void deveCriarPoliticaDeFreteAtivaQuandoEstadoExistirENaoHouverPoliticaAtiva() {
        when(estadoRepository.findById(createDTO.estadoId())).thenReturn(Optional.of(estado));
        when(politicaFreteMapper.toEntity(createDTO)).thenReturn(politicaFrete);
        when(politicaFreteRepository.findByEstadoIdAndAtivoTrue(createDTO.estadoId()))
                .thenReturn(Optional.empty());
        when(politicaFreteRepository.save(politicaFrete)).thenReturn(politicaFrete);
        when(politicaFreteMapper.toDTO(politicaFrete)).thenReturn(responseDTO);

        PoliticaFreteResponseDTO resultado = politicaFreteService.createNewPoliticaFrete(createDTO);

        assertNotNull(resultado);
        assertEquals(responseDTO, resultado);
        assertTrue(politicaFrete.getAtivo());
        assertEquals(estado, politicaFrete.getEstado());
        verify(estadoRepository).findById(createDTO.estadoId());
        verify(politicaFreteMapper).toEntity(createDTO);
        verify(politicaFreteRepository).findByEstadoIdAndAtivoTrue(createDTO.estadoId());
        verify(politicaFreteRepository).save(politicaFrete);
        verify(politicaFreteMapper).toDTO(politicaFrete);
    }

    @Test
    void deveDesativarPoliticaAnteriorAoCriarNovaPoliticaParaMesmoEstado() {
        PoliticaFrete politicaAnterior = PoliticaFreteFixture.politicaFrete();
        politicaAnterior.setId(2);
        politicaAnterior.setAtivo(true);

        when(estadoRepository.findById(createDTO.estadoId())).thenReturn(Optional.of(estado));
        when(politicaFreteMapper.toEntity(createDTO)).thenReturn(politicaFrete);
        when(politicaFreteRepository.findByEstadoIdAndAtivoTrue(createDTO.estadoId()))
                .thenReturn(Optional.of(politicaAnterior));
        when(politicaFreteRepository.save(politicaFrete)).thenReturn(politicaFrete);
        when(politicaFreteMapper.toDTO(politicaFrete)).thenReturn(responseDTO);

        PoliticaFreteResponseDTO resultado = politicaFreteService.createNewPoliticaFrete(createDTO);

        assertNotNull(resultado);
        assertFalse(politicaAnterior.getAtivo());
        assertTrue(politicaFrete.getAtivo());
        verify(politicaFreteRepository).findByEstadoIdAndAtivoTrue(createDTO.estadoId());
        verify(politicaFreteRepository).save(politicaFrete);
    }

    @Test
    void deveLancarExcecaoQuandoEstadoNaoExistirAoCriarPoliticaDeFrete() {
        when(estadoRepository.findById(createDTO.estadoId())).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> politicaFreteService.createNewPoliticaFrete(createDTO)
        );

        assertEquals(
                "Estado com id " + createDTO.estadoId() + " não encontrado",
                exception.getMessage()
        );
        verify(estadoRepository).findById(createDTO.estadoId());
        verifyNoInteractions(politicaFreteMapper);
        verify(politicaFreteRepository, never()).save(any(PoliticaFrete.class));
    }

    // getValorBasePorSiglaEstado(String siglaEstado)

    @Test
    void deveRetornarValorBaseQuandoHouverPoliticaAtivaParaSigla() {
        when(politicaFreteRepository.findByEstadoSiglaAndAtivoTrue("SP"))
                .thenReturn(Optional.of(politicaFrete));

        BigDecimal resultado = politicaFreteService.getValorBasePorSiglaEstado("SP");

        assertEquals(0, new BigDecimal("15.00").compareTo(resultado));
        verify(politicaFreteRepository).findByEstadoSiglaAndAtivoTrue("SP");
    }

    @Test
    void deveLancarExcecaoQuandoNaoHouverPoliticaAtivaParaSigla() {
        when(politicaFreteRepository.findByEstadoSiglaAndAtivoTrue("RJ"))
                .thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> politicaFreteService.getValorBasePorSiglaEstado("RJ")
        );

        assertEquals("Política de frete não encontrada", exception.getMessage());
        verify(politicaFreteRepository).findByEstadoSiglaAndAtivoTrue("RJ");
    }
}
