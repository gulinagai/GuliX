package guli.gulix.backend.fixture;

import guli.gulix.backend.dto.CarrinhoResponseDTO;
import guli.gulix.backend.dto.ItemCarrinhoRequestDTO;
import guli.gulix.backend.dto.ItemCarrinhoResponseDTO;
import guli.gulix.backend.dto.ItemCarrinhoUpdateDTO;
import guli.gulix.backend.entity.Carrinho;
import guli.gulix.backend.entity.ItemCarrinho;
import guli.gulix.backend.entity.Produto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class CarrinhoFixture {

    private CarrinhoFixture() {

    }

    public static Carrinho carrinho() {
        Carrinho carrinho = new Carrinho();

        carrinho.setId(1);
        carrinho.setUsuario(UsuarioFixture.usuario());
        carrinho.setItens(new ArrayList<>());

        return carrinho;
    }

    public static Carrinho carrinhoComItem() {
        Carrinho carrinho = carrinho();
        ItemCarrinho item = itemCarrinho();
        item.setCarrinho(carrinho);
        carrinho.getItens().add(item);

        return carrinho;
    }

    public static ItemCarrinho itemCarrinho() {
        ItemCarrinho item = new ItemCarrinho();
        Produto produto = ProdutoFixture.produto();

        item.setId(1L);
        item.setProduto(produto);
        item.setQuantidade(2);

        return item;
    }

    public static ItemCarrinhoRequestDTO itemCarrinhoRequestDTO() {
        ItemCarrinhoRequestDTO dto = new ItemCarrinhoRequestDTO();

        dto.setProdutoId(1);
        dto.setQuantidade(2);

        return dto;
    }

    public static ItemCarrinhoUpdateDTO itemCarrinhoUpdateDTO() {
        ItemCarrinhoUpdateDTO dto = new ItemCarrinhoUpdateDTO();

        dto.setQuantidade(3);

        return dto;
    }

    public static ItemCarrinhoResponseDTO itemCarrinhoResponseDTO() {
        return new ItemCarrinhoResponseDTO(
                1,
                1,
                "Gabinete Gamer Teste",
                new BigDecimal("619.99"),
                2
        );
    }

    public static CarrinhoResponseDTO carrinhoResponseDTO() {
        return new CarrinhoResponseDTO(
                List.of(itemCarrinhoResponseDTO()),
                new BigDecimal("1239.98")
        );
    }

    public static CarrinhoResponseDTO carrinhoResponseDTOVazio() {
        return new CarrinhoResponseDTO(
                List.of(),
                BigDecimal.ZERO
        );
    }
}
