package guli.gulix.backend.fixture;

import guli.gulix.backend.dto.ProdutoCreateDTO;
import guli.gulix.backend.dto.ProdutoPatchDTO;
import guli.gulix.backend.dto.ProdutoResponseDTO;
import guli.gulix.backend.dto.ProdutoUpdateDTO;
import guli.gulix.backend.entity.Produto;

import java.math.BigDecimal;

public final class ProdutoFixture {

    private ProdutoFixture() {

    }


    public static Produto produto() {
        Produto produto = new Produto();

        produto.setId(1);
        produto.setNome("Gabinete Gamer Teste");
        produto.setResumo("RGB, Mid-Tower, Lateral de Vidro, Preto");
        produto.setPreco(new BigDecimal("619.99"));
        produto.setImagemURL("gabinete-gamer-x-teste-rgb-mid-tower-lateral-de-vidro-preto.jpg");
        produto.setCategoria(CategoriaFixture.categoria());
        produto.setMarca(MarcaFixture.marca());
        produto.setDestaque(true);
        produto.setDesconto(new BigDecimal("10.00"));

        return produto;
    }

    public static ProdutoCreateDTO produtoCreateDTO() {
        ProdutoCreateDTO dto = new ProdutoCreateDTO();


        dto.setNome("Gabinete Gamer Teste");
        dto.setResumo("RGB, Mid-Tower, Lateral de Vidro, Preto");
        dto.setPreco(new BigDecimal("619.99"));
        dto.setImagemURL("gabinete-gamer-x-teste-rgb-mid-tower-lateral-de-vidro-preto.jpg");
        dto.setCategoriaId(1);
        dto.setMarcaId(1);
        dto.setDestaque(true);
        dto.setDesconto(new BigDecimal("10.00"));

        return dto;
    }


    public static ProdutoUpdateDTO produtoUpdateDTO() {
        ProdutoUpdateDTO dto = new ProdutoUpdateDTO();

        dto.setNome("Novo Gabinete Gamer Teste");
        dto.setResumo("Nova Descrição");
        dto.setPreco(new BigDecimal("619.99"));
        dto.setImagemURL("gabinete-gamer-x-teste-rgb-mid-tower-lateral-de-vidro-preto.jpg");
        dto.setCategoriaId(1);
        dto.setMarcaId(1);
        dto.setDestaque(true);
        dto.setDesconto(new BigDecimal("10.00"));

        return dto;
    }

    public static ProdutoPatchDTO produtoUpdatePartialDTO() {
        ProdutoPatchDTO dto = new ProdutoPatchDTO();

        dto.setNome("Gabinete Gaming Teste");
        dto.setImagemURL("gabinete-gaming-teste-rgb-mid-tower-lateral-de-vidro-preto.jpg");
        dto.setCategoriaId(1);
        dto.setMarcaId(1);
        dto.setDestaque(false);

        return dto;
    }

    public static ProdutoResponseDTO produtoResponseDTO() {
        ProdutoResponseDTO dto = new ProdutoResponseDTO();

        dto.setId(1);
        dto.setNome("Gabinete Gamer Teste");
        dto.setResumo("RGB, Mid-Tower, Lateral de Vidro, Preto");
        dto.setPreco(new BigDecimal("619.99"));
        dto.setEstoque(EstoqueFixture.estoqueResponseDTO());
        dto.setImagemURL("gabinete-gamer-x-teste-rgb-mid-tower-lateral-de-vidro-preto.jpg");
        dto.setCategoriaId(1);
        dto.setMarcaId(1);
        dto.setDestaque(true);
        dto.setDesconto(new BigDecimal("10.00"));

        return dto;
    }

}
