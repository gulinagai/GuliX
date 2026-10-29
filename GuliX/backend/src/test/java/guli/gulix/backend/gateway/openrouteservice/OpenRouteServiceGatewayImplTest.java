package guli.gulix.backend.gateway.openrouteservice;

import guli.gulix.backend.gateway.openrouteservice.dto.DirectionsRequestDTO;
import guli.gulix.backend.gateway.openrouteservice.dto.DirectionsResponseDTO;
import guli.gulix.backend.gateway.openrouteservice.dto.Rota;
import guli.gulix.backend.gateway.openrouteservice.dto.SnapRequestDTO;
import guli.gulix.backend.gateway.openrouteservice.dto.SnapResponseDTO;
import guli.gulix.backend.geographic.Coordenada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenRouteServiceGatewayImplTest {

    private static final String API_KEY = "chave-teste";

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private OpenRouteServiceGatewayImpl gateway;

    private Coordenada origem;
    private Coordenada destino;

    @BeforeEach
    void setup() {
        gateway = new OpenRouteServiceGatewayImpl(restClient, API_KEY);
        origem = new Coordenada(-23.561684, -46.655981);
        destino = new Coordenada(-23.5505, -46.6333);

        when(restClient.post()).thenReturn(requestSpec);
        when(requestSpec.uri(anyString(), any(Object[].class))).thenReturn(requestSpec);
        when(requestSpec.header(anyString(), any(String[].class))).thenReturn(requestSpec);
        doReturn(requestSpec).when(requestSpec).body(any(Object.class));
    }

    // calcularRota(Coordenada origem, Coordenada destino)

    @Test
    void deveCalcularRotaComCoordenadasLongitudeLatitudeEMapearDistancia() {
        DirectionsResponseDTO resposta = new DirectionsResponseDTO(List.of(
                new DirectionsResponseDTO.RouteDTO(
                        new DirectionsResponseDTO.SummaryDTO(12500.0, 845.0)
                )
        ));
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(DirectionsResponseDTO.class)).thenReturn(resposta);

        Rota rota = gateway.calcularRota(origem, destino);

        verify(requestSpec).uri("/openrouteservice/v2/directions/driving-car/json");
        verify(requestSpec).header("Authorization", API_KEY);

        ArgumentCaptor<DirectionsRequestDTO> corpoCaptor = ArgumentCaptor.forClass(DirectionsRequestDTO.class);
        verify(requestSpec).body(corpoCaptor.capture());
        assertEquals(
                List.of(
                        List.of(origem.longitude(), origem.latitude()),
                        List.of(destino.longitude(), destino.latitude())
                ),
                corpoCaptor.getValue().coordinates()
        );
        assertEquals(0, new BigDecimal("12500").compareTo(rota.distanciaMetros()));
        assertEquals(0, new BigDecimal("845").compareTo(rota.duracaoSegundos()));
    }

    @Test
    void devePropagarExcecaoQuandoFalharAoCalcularRota() {
        when(requestSpec.retrieve()).thenThrow(new RestClientException("falha ao consultar rota"));

        RestClientException exception = assertThrows(
                RestClientException.class,
                () -> gateway.calcularRota(origem, destino)
        );

        assertEquals("falha ao consultar rota", exception.getMessage());
        verifyNoInteractions(responseSpec);
    }

    // snap(Coordenada coordenada)

    @Test
    void deveAproximarCoordenadaComRaioEReordenarLocalizacao() {
        SnapResponseDTO resposta = new SnapResponseDTO(List.of(
                new SnapResponseDTO.LocationDTO(List.of(-46.64, -23.56))
        ));
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(SnapResponseDTO.class)).thenReturn(resposta);

        Coordenada resultado = gateway.snap(destino);

        verify(requestSpec).uri("/openrouteservice/v2/snap/driving-car/json");
        verify(requestSpec).header("Authorization", API_KEY);

        ArgumentCaptor<SnapRequestDTO> corpoCaptor = ArgumentCaptor.forClass(SnapRequestDTO.class);
        verify(requestSpec).body(corpoCaptor.capture());
        assertEquals(5000, corpoCaptor.getValue().radius());
        assertEquals(
                List.of(List.of(destino.longitude(), destino.latitude())),
                corpoCaptor.getValue().locations()
        );
        assertEquals(-23.56, resultado.latitude(), 0.0);
        assertEquals(-46.64, resultado.longitude(), 0.0);
    }

    @Test
    void devePropagarExcecaoQuandoFalharAoAproximarCoordenada() {
        when(requestSpec.retrieve()).thenThrow(new RestClientException("falha ao aproximar coordenada"));

        RestClientException exception = assertThrows(
                RestClientException.class,
                () -> gateway.snap(destino)
        );

        assertEquals("falha ao aproximar coordenada", exception.getMessage());
        verifyNoInteractions(responseSpec);
    }
}
