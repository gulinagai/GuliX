package guli.gulix.backend.service;

import guli.gulix.backend.entity.Produto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class PrecoPromocionalServiceTest {

    private final PrecoPromocionalService precoPromocionalService = new PrecoPromocionalService();

    @Test
    void deveCalcularPrecoPromocionalDeProdutoEmDestaque() {
        Produto produto = produto("1000.00", true, "15.00");
        BigDecimal precoOriginal = produto.getPreco();

        assertEquals(new BigDecimal("15.00"), precoPromocionalService.percentualEfetivo(produto));
        assertEquals(new BigDecimal("850.00"), precoPromocionalService.precoUnitario(produto));
        assertSame(precoOriginal, produto.getPreco());
    }

    @Test
    void deveManterPrecoOriginalQuandoDestaqueNaoTiverDesconto() {
        Produto produto = produto("1000.00", true, "0.00");

        assertEquals(new BigDecimal("0.00"), precoPromocionalService.percentualEfetivo(produto));
        assertEquals(new BigDecimal("1000.00"), precoPromocionalService.precoUnitario(produto));
        assertSame(produto.getPreco(), precoPromocionalService.precoUnitario(produto));
    }

    @Test
    void deveIgnorarDescontoQuandoProdutoNaoEstiverEmDestaque() {
        Produto produto = produto("1000.00", false, "15.00");
        BigDecimal precoOriginal = produto.getPreco();

        assertEquals(new BigDecimal("0.00"), precoPromocionalService.percentualEfetivo(produto));
        assertEquals(new BigDecimal("1000.00"), precoPromocionalService.precoUnitario(produto));
        assertEquals(new BigDecimal("15.00"), produto.getDesconto());
        assertSame(precoOriginal, produto.getPreco());
    }

    @Test
    void deveIgnorarDescontoQuandoDestaqueEstiverNulo() {
        Produto produto = produto("200.00", null, "10.00");

        assertEquals(new BigDecimal("0.00"), precoPromocionalService.percentualEfetivo(produto));
        assertEquals(new BigDecimal("200.00"), precoPromocionalService.precoUnitario(produto));
    }

    @Test
    void deveArredondarPrecoPromocionalComDuasCasas() {
        Produto produto = produto("10.01", true, "15.00");

        assertEquals(new BigDecimal("8.51"), precoPromocionalService.precoUnitario(produto));
        assertEquals(new BigDecimal("10.01"), produto.getPreco());
    }

    private Produto produto(String preco, Boolean destaque, String desconto) {
        Produto produto = new Produto();
        produto.setPreco(new BigDecimal(preco));
        produto.setDestaque(destaque);
        produto.setDesconto(desconto == null ? null : new BigDecimal(desconto));
        return produto;
    }
}
