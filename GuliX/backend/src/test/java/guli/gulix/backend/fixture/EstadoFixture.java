package guli.gulix.backend.fixture;

import guli.gulix.backend.dto.EstadoCreateDTO;
import guli.gulix.backend.dto.EstadoResponseDTO;
import guli.gulix.backend.dto.EstadoUpdateDTO;
import guli.gulix.backend.entity.Estado;

public class EstadoFixture {

    private EstadoFixture() {
    }

    public static Estado estado() {
        Estado estado = new Estado();

        estado.setId(1);
        estado.setNome("São Paulo");
        estado.setSigla("SP");

        return estado;
    }

    public static EstadoCreateDTO estadoCreateDTO() {
        return new EstadoCreateDTO(
                "São Paulo",
                "SP"
        );
    }

    public static EstadoResponseDTO estadoResponseDTO() {
        return new EstadoResponseDTO(
                1,
                "São Paulo",
                "SP"
        );
    }

    public static EstadoUpdateDTO estadoUpdateDTO() {
        return new EstadoUpdateDTO(
                "Rio de Janeiro",
                "RJ"
        );
    }
}
