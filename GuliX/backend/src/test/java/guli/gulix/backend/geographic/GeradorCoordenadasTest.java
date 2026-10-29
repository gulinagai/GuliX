package guli.gulix.backend.geographic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GeradorCoordenadasTest {

    private final GeradorCoordenadas gerador = new GeradorCoordenadas();

    @Test
    void deveGerarCoordenadaDentroDoRaioInformado() {

        double latitudeCentral = -23.5505;
        double longitudeCentral = -46.6333;
        double raioKm = 5;

        Coordenada coordenada = gerador.gerar(
                latitudeCentral,
                longitudeCentral
        );

        double distancia = calcularDistanciaKm(
                latitudeCentral,
                longitudeCentral,
                coordenada.latitude(),
                coordenada.longitude()
        );

        assertTrue(distancia <= raioKm);
    }

    private double calcularDistanciaKm(
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2
    ) {
        double raioTerraKm = 6371.0;

        double deltaLatitude = Math.toRadians(latitude2 - latitude1);
        double deltaLongitude = Math.toRadians(longitude2 - longitude1);

        double a = Math.sin(deltaLatitude / 2) * Math.sin(deltaLatitude / 2)
                + Math.cos(Math.toRadians(latitude1))
                * Math.cos(Math.toRadians(latitude2))
                * Math.sin(deltaLongitude / 2)
                * Math.sin(deltaLongitude / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return raioTerraKm * c;
    }
}