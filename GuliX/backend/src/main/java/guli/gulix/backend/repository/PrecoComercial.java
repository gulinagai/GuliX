package guli.gulix.backend.repository;

import java.math.BigDecimal;

public record PrecoComercial(BigDecimal preco, Boolean destaque, BigDecimal desconto) {
}
