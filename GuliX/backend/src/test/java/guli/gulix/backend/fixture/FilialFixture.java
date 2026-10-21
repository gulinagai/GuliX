package guli.gulix.backend.fixture;

import guli.gulix.backend.dto.EnderecoFilialDTO;
import guli.gulix.backend.dto.FilialResponseDTO;
import guli.gulix.backend.dto.FilialUpdateDTO;
import guli.gulix.backend.entity.EnderecoFilial;
import guli.gulix.backend.entity.Filial;

import java.math.BigDecimal;

public class FilialFixture {

    private FilialFixture() {
    }

    public static Filial filial() {
        Filial filial = new Filial();

        filial.setId(1);
        filial.setEmpresa(EmpresaFixture.empresa());
        filial.setNome("Filial São Paulo");
        filial.setEndereco(enderecoFilial());
        filial.setAtivo(true);

        return filial;
    }

    public static EnderecoFilial enderecoFilial() {
        return new EnderecoFilial(
                "Avenida Paulista",
                "1000",
                "São Paulo",
                "SP",
                "01310100",
                new BigDecimal("-23.5616840"),
                new BigDecimal("-46.6559810")
        );
    }

    public static EnderecoFilialDTO enderecoFilialDTO() {
        return new EnderecoFilialDTO(
                "Avenida Paulista",
                "1000",
                "São Paulo",
                "SP",
                "01310100",
                new BigDecimal("-23.5616840"),
                new BigDecimal("-46.6559810")
        );
    }

    public static FilialResponseDTO filialResponseDTO() {
        return new FilialResponseDTO(
                1,
                "Filial São Paulo",
                enderecoFilialDTO(),
                true
        );
    }

    public static FilialUpdateDTO filialUpdateDTO() {
        return new FilialUpdateDTO(
                "Filial Rio de Janeiro",
                new EnderecoFilialDTO(
                        "Avenida Atlântica",
                        "500",
                        "Rio de Janeiro",
                        "RJ",
                        "22021001",
                        new BigDecimal("-22.9711770"),
                        new BigDecimal("-43.1825430")
                ),
                false
        );
    }
}
