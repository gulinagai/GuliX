package guli.gulix.backend.fixture;

import guli.gulix.backend.dto.EmpresaResponseDTO;
import guli.gulix.backend.dto.EmpresaUpdateDTO;
import guli.gulix.backend.entity.Empresa;

import java.math.BigDecimal;

public class EmpresaFixture {

    private EmpresaFixture() {
    }

    public static Empresa empresa() {
        Empresa empresa = new Empresa();

        empresa.setId(1);
        empresa.setRazaoSocial("GuliX Tecnologia LTDA");
        empresa.setNomeFantasia("GuliX");
        empresa.setCnpj("12345678000195");
        empresa.setValorPorKmFrete(new BigDecimal("2.50"));

        return empresa;
    }

    public static EmpresaResponseDTO empresaResponseDTO() {
        return new EmpresaResponseDTO(
                1,
                "GuliX Tecnologia LTDA",
                "GuliX",
                "12345678000195",
                new BigDecimal("2.50")
        );
    }

    public static EmpresaUpdateDTO empresaUpdateDTO() {
        return new EmpresaUpdateDTO(
                "GuliX Tecnologia e Comércio LTDA",
                "GuliX Store",
                "98765432000198",
                new BigDecimal("3.50")
        );
    }
}