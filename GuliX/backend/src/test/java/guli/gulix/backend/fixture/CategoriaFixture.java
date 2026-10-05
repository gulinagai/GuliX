package guli.gulix.backend.fixture;

import guli.gulix.backend.dto.CategoriaRequestDTO;
import guli.gulix.backend.dto.CategoriaResponseDTO;
import guli.gulix.backend.entity.Categoria;

public class CategoriaFixture {

    private CategoriaFixture() {

    }

    public static Categoria categoria() {
        Categoria categoria = new Categoria();

        categoria.setId(1);
        categoria.setNome("Gabinete");

        return categoria;
    }

    public static CategoriaRequestDTO categoriaRequestDTO() {
        CategoriaRequestDTO categoriaRequestDTO = new CategoriaRequestDTO();

        categoriaRequestDTO.setNome("Gabinete");

        return categoriaRequestDTO;
    }

    public static CategoriaResponseDTO categoriaResponseDTO() {
        CategoriaResponseDTO categoriaResponseDTO = new CategoriaResponseDTO();

        categoriaResponseDTO.setId(1);
        categoriaResponseDTO.setNome("Gabinete");

        return categoriaResponseDTO;
    }
}
