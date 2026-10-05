package guli.gulix.backend.fixture;

import guli.gulix.backend.dto.EstoqueRequestDTO;
import guli.gulix.backend.dto.EstoqueResponseDTO;
import guli.gulix.backend.entity.Estoque;

public class EstoqueFixture {
    
    private EstoqueFixture() {
    }

    public static Estoque estoque() {
        Estoque estoque = new Estoque();

        estoque.setId(1);
        estoque.setProduto(ProdutoFixture.produto());
        estoque.setEstoqueTotal(10);
        estoque.setEstoqueReservado(0);

        return estoque;
    }

    public static EstoqueRequestDTO estoqueRequestDTO() {
        return new EstoqueRequestDTO(20);
    }

    public static EstoqueResponseDTO estoqueResponseDTO() {
        EstoqueResponseDTO estoqueResponseDTO = new EstoqueResponseDTO();

        estoqueResponseDTO.setProdutoId(1);
        estoqueResponseDTO.setEstoqueTotal(10);
        estoqueResponseDTO.setEstoqueReservado(2);
        estoqueResponseDTO.setEstoqueDisponivel(8);

        return estoqueResponseDTO;
    }
    
}
