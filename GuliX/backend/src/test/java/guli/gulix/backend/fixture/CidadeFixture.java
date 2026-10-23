package guli.gulix.backend.fixture;

import guli.gulix.backend.dto.CidadeCreateDTO;
import guli.gulix.backend.dto.CidadeResponseDTO;
import guli.gulix.backend.dto.CidadeUpdateDTO;
import guli.gulix.backend.entity.Cidade;

import java.math.BigDecimal;

public class CidadeFixture {

    private CidadeFixture() {
    }

    public static Cidade cidade() {
        Cidade cidade = new Cidade();

        cidade.setId(1);
        cidade.setNome("São Paulo");
        cidade.setEstado(EstadoFixture.estado());
        cidade.setLatitudeCentral(new BigDecimal("-23.550520"));
        cidade.setLongitudeCentral(new BigDecimal("-46.633308"));

        return cidade;
    }

    public static CidadeCreateDTO cidadeCreateDTO() {
        return new CidadeCreateDTO(
                "São Paulo",
                1,
                new BigDecimal("-23.550520"),
                new BigDecimal("-46.633308")
        );
    }

    public static CidadeResponseDTO cidadeResponseDTO() {
        return new CidadeResponseDTO(
                1,
                "São Paulo",
                1,
                new BigDecimal("-23.550520"),
                new BigDecimal("-46.633308")
        );
    }

    public static CidadeUpdateDTO cidadeUpdateDTO() {
        return new CidadeUpdateDTO(
                "Campinas",
                1,
                new BigDecimal("-22.905560"),
                new BigDecimal("-47.060830")
        );
    }
}
