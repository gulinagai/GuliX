package guli.gulix.backend.fixture;

import guli.gulix.backend.dto.PoliticaFreteCreateDTO;
import guli.gulix.backend.dto.PoliticaFreteResponseDTO;
import guli.gulix.backend.entity.PoliticaFrete;

import java.math.BigDecimal;

public class PoliticaFreteFixture {

    private PoliticaFreteFixture() {
    }

    public static PoliticaFrete politicaFrete() {
        PoliticaFrete politicaFrete = new PoliticaFrete();

        politicaFrete.setId(1);
        politicaFrete.setEstado(EstadoFixture.estado());
        politicaFrete.setValorBase(new BigDecimal("15.00"));
        politicaFrete.setAtivo(true);

        return politicaFrete;
    }

    public static PoliticaFreteCreateDTO politicaFreteCreateDTO() {
        return new PoliticaFreteCreateDTO(
                1,
                new BigDecimal("15.00")
        );
    }

    public static PoliticaFreteResponseDTO politicaFreteResponseDTO() {
        return new PoliticaFreteResponseDTO(
                1,
                1,
                "SP",
                new BigDecimal("15.00"),
                true
        );
    }
}
