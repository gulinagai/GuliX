package guli.gulix.backend.service;

import guli.gulix.backend.entity.Produto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PrecoPromocionalService {

    private static final BigDecimal CEM = new BigDecimal("100");
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    public BigDecimal percentualEfetivo(Produto produto) {
        if (produto == null || !Boolean.TRUE.equals(produto.getDestaque())) {
            return ZERO;
        }

        BigDecimal desconto = produto.getDesconto();

        if (desconto == null
                || desconto.compareTo(BigDecimal.ZERO) <= 0
                || desconto.compareTo(CEM) > 0) {
            return ZERO;
        }

        return desconto.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal precoUnitario(Produto produto) {
        BigDecimal percentual = percentualEfetivo(produto);

        if (percentual.compareTo(BigDecimal.ZERO) == 0) {
            return produto.getPreco().setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal fator = BigDecimal.ONE.subtract(
                percentual.divide(CEM, 4, RoundingMode.HALF_UP)
        );

        return produto.getPreco()
                .multiply(fator)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
