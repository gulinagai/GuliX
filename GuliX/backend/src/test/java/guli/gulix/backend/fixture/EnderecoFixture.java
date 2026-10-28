package guli.gulix.backend.fixture;

import guli.gulix.backend.dto.EnderecoCreateDTO;
import guli.gulix.backend.dto.EnderecoResponseDTO;
import guli.gulix.backend.dto.EnderecoUpdateDTO;
import guli.gulix.backend.entity.Endereco;

import java.math.BigDecimal;

public final class EnderecoFixture {

    private EnderecoFixture() {

    }

    public static Endereco endereco() {
        Endereco endereco = new Endereco();

        endereco.setId(1);
        endereco.setUsuario(UsuarioFixture.usuario());
        endereco.setRua("Avenida Paulista");
        endereco.setNumero("1000");
        endereco.setCidade("São Paulo");
        endereco.setEstado("SP");
        endereco.setCep("01310-100");
        endereco.setPrincipal(true);
        endereco.setLatitude(new BigDecimal("-23.550500"));
        endereco.setLongitude(new BigDecimal("-46.633300"));

        return endereco;
    }

    public static Endereco enderecoMapeado() {
        EnderecoCreateDTO dto = enderecoCreateDTO();
        Endereco endereco = new Endereco();

        endereco.setRua(dto.getRua());
        endereco.setNumero(dto.getNumero());
        endereco.setCidade(dto.getCidade());
        endereco.setEstado(dto.getEstado());
        endereco.setCep(dto.getCep());
        endereco.setPrincipal(false);

        return endereco;
    }

    public static EnderecoCreateDTO enderecoCreateDTO() {
        EnderecoCreateDTO dto = new EnderecoCreateDTO();

        dto.setRua("Avenida Paulista");
        dto.setNumero("1000");
        dto.setCidade("São Paulo");
        dto.setEstado("SP");
        dto.setCep("01310-100");

        return dto;
    }

    public static EnderecoUpdateDTO enderecoUpdateDTO() {
        EnderecoUpdateDTO dto = new EnderecoUpdateDTO();

        dto.setRua("Rua Augusta");
        dto.setNumero("200");
        dto.setCidade("São Paulo");
        dto.setEstado("SP");
        dto.setCep("01305-000");

        return dto;
    }

    public static EnderecoResponseDTO enderecoResponseDTO() {
        return new EnderecoResponseDTO(
                1,
                "Avenida Paulista",
                "1000",
                "São Paulo",
                "SP",
                "01310-100"
        );
    }
}
