package guli.gulix.backend.service;

import guli.gulix.backend.entity.Empresa;
import guli.gulix.backend.entity.Endereco;
import guli.gulix.backend.entity.Filial;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.fixture.EmpresaFixture;
import guli.gulix.backend.fixture.EnderecoFixture;
import guli.gulix.backend.fixture.EstadoFixture;
import guli.gulix.backend.fixture.FilialFixture;
import guli.gulix.backend.gateway.openrouteservice.OpenRouteServiceGateway;
import guli.gulix.backend.gateway.openrouteservice.dto.Rota;
import guli.gulix.backend.geographic.Coordenada;
import guli.gulix.backend.repository.EmpresaRepository;
import guli.gulix.backend.repository.FilialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculoFreteServiceImplTest {

    @Mock
    private FilialRepository filialRepository;

    @Mock
    private OpenRouteServiceGateway openRouteServiceGateway;

    @Mock
    private PoliticaFreteService politicaFreteService;

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private CalculoFreteServiceImpl calculoFreteService;

    private Filial filial;
    private Empresa empresa;
    private Coordenada destino;
    private String siglaEstado;

    @BeforeEach
    void setup() {
        filial = FilialFixture.filial();
        empresa = EmpresaFixture.empresa();
        Endereco enderecoDestino = EnderecoFixture.endereco();
        destino = new Coordenada(
                enderecoDestino.getLatitude().doubleValue(),
                enderecoDestino.getLongitude().doubleValue()
        );
        siglaEstado = EstadoFixture.estado().getSigla();
    }

    // calcularFrete(Coordenada coordenadaDestino, String siglaEstado)

    @Test
    void deveCalcularFreteComValorBaseECustoPorQuilometro() {
        when(filialRepository.findFirstBy()).thenReturn(Optional.of(filial));
        when(openRouteServiceGateway.calcularRota(any(Coordenada.class), eq(destino)))
                .thenReturn(new Rota(new BigDecimal("2500"), new BigDecimal("600")));
        when(empresaRepository.findFirstBy()).thenReturn(Optional.of(empresa));
        when(politicaFreteService.getValorBasePorSiglaEstado(siglaEstado))
                .thenReturn(new BigDecimal("15.00"));

        BigDecimal resultado = calculoFreteService.calcularFrete(destino, siglaEstado);

        ArgumentCaptor<Coordenada> origemCaptor = ArgumentCaptor.forClass(Coordenada.class);
        ArgumentCaptor<Coordenada> destinoCaptor = ArgumentCaptor.forClass(Coordenada.class);
        verify(openRouteServiceGateway).calcularRota(origemCaptor.capture(), destinoCaptor.capture());
        assertEquals(filial.getEndereco().getLatitude().doubleValue(), origemCaptor.getValue().latitude(), 0.0);
        assertEquals(filial.getEndereco().getLongitude().doubleValue(), origemCaptor.getValue().longitude(), 0.0);
        assertSame(destino, destinoCaptor.getValue());
        verify(politicaFreteService).getValorBasePorSiglaEstado("SP");
        verify(filialRepository).findFirstBy();
        verify(empresaRepository).findFirstBy();
        assertEquals(0, new BigDecimal("21.25").compareTo(resultado));
    }

    @Test
    void deveArredondarDistanciaEmQuilometrosComHalfUp() {
        when(filialRepository.findFirstBy()).thenReturn(Optional.of(filial));
        when(openRouteServiceGateway.calcularRota(any(Coordenada.class), eq(destino)))
                .thenReturn(new Rota(new BigDecimal("1234.5"), new BigDecimal("300")));
        when(empresaRepository.findFirstBy()).thenReturn(Optional.of(empresa));
        when(politicaFreteService.getValorBasePorSiglaEstado(siglaEstado))
                .thenReturn(new BigDecimal("15.00"));

        BigDecimal resultado = calculoFreteService.calcularFrete(destino, siglaEstado);

        assertEquals(0, new BigDecimal("18.0875").compareTo(resultado));
    }

    @Test
    void deveLancarExcecaoQuandoFilialNaoExistir() {
        when(filialRepository.findFirstBy()).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> calculoFreteService.calcularFrete(destino, siglaEstado)
        );

        assertEquals("Filial não encontrada", exception.getMessage());
        verify(filialRepository).findFirstBy();
        verifyNoInteractions(openRouteServiceGateway, politicaFreteService, empresaRepository);
    }

    @Test
    void deveLancarExcecaoQuandoEmpresaNaoExistir() {
        when(filialRepository.findFirstBy()).thenReturn(Optional.of(filial));
        when(openRouteServiceGateway.calcularRota(any(Coordenada.class), eq(destino)))
                .thenReturn(new Rota(new BigDecimal("2500"), new BigDecimal("600")));
        when(empresaRepository.findFirstBy()).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> calculoFreteService.calcularFrete(destino, siglaEstado)
        );

        assertEquals("Empresa não encontrada", exception.getMessage());
        verify(openRouteServiceGateway).calcularRota(any(Coordenada.class), eq(destino));
        verify(politicaFreteService, never()).getValorBasePorSiglaEstado(anyString());
    }

    @Test
    void devePropagarExcecaoQuandoGatewayFalharAoCalcularRota() {
        when(filialRepository.findFirstBy()).thenReturn(Optional.of(filial));
        when(openRouteServiceGateway.calcularRota(any(Coordenada.class), eq(destino)))
                .thenThrow(new RestClientException("falha ao consultar o OpenRouteService"));

        RestClientException exception = assertThrows(
                RestClientException.class,
                () -> calculoFreteService.calcularFrete(destino, siglaEstado)
        );

        assertEquals("falha ao consultar o OpenRouteService", exception.getMessage());
        verifyNoInteractions(empresaRepository, politicaFreteService);
    }

    @Test
    void devePropagarExcecaoQuandoNaoHouverPoliticaAtiva() {
        when(filialRepository.findFirstBy()).thenReturn(Optional.of(filial));
        when(openRouteServiceGateway.calcularRota(any(Coordenada.class), eq(destino)))
                .thenReturn(new Rota(new BigDecimal("2500"), new BigDecimal("600")));
        when(empresaRepository.findFirstBy()).thenReturn(Optional.of(empresa));
        when(politicaFreteService.getValorBasePorSiglaEstado(siglaEstado))
                .thenThrow(new RecursoNaoEncontradoException("Política de frete não encontrada"));

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> calculoFreteService.calcularFrete(destino, siglaEstado)
        );

        assertEquals("Política de frete não encontrada", exception.getMessage());
        verify(politicaFreteService).getValorBasePorSiglaEstado("SP");
    }
}
