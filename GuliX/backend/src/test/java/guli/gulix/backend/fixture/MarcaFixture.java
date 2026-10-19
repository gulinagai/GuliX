package guli.gulix.backend.fixture;

import guli.gulix.backend.dto.MarcaRequestDTO;
import guli.gulix.backend.dto.MarcaResponseDTO;
import guli.gulix.backend.entity.Marca;

public class MarcaFixture {

    private MarcaFixture() {

    }

    public static Marca marca() {
        Marca marca = new Marca();

        marca.setId(1);
        marca.setNome("AMD");

        return marca;
    }

    public static MarcaRequestDTO marcaRequestDTO() {
        MarcaRequestDTO marcaRequestDTO = new MarcaRequestDTO();

        marcaRequestDTO.setNome("AMD");

        return marcaRequestDTO;
    }

    public static MarcaResponseDTO marcaResponseDTO() {
        MarcaResponseDTO marcaResponseDTO = new MarcaResponseDTO();

        marcaResponseDTO.setId(1);
        marcaResponseDTO.setNome("AMD");

        return marcaResponseDTO;
    }
}
