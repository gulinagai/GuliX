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
        marca.setNome("Gabinete");

        return marca;
    }

    public static MarcaRequestDTO categoriaRequestDTO() {
        MarcaRequestDTO marcaRequestDTO = new MarcaRequestDTO();

        marcaRequestDTO.setNome("Gabinete");

        return marcaRequestDTO;
    }

    public static MarcaResponseDTO categoriaResponseDTO() {
        MarcaResponseDTO marcaResponseDTO = new MarcaResponseDTO();

        marcaResponseDTO.setId(1);
        marcaResponseDTO.setNome("Gabinete");

        return marcaResponseDTO;
    }
}
