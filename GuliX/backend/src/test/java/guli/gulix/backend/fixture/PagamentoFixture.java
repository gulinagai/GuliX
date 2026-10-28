package guli.gulix.backend.fixture;

import guli.gulix.backend.dto.PagamentoResponseDTO;
import guli.gulix.backend.entity.Pagamento;
import guli.gulix.backend.entity.Pedido;
import guli.gulix.backend.entity.enums.GatewayPagamento;
import guli.gulix.backend.entity.enums.MetodoPagamento;
import guli.gulix.backend.entity.enums.StatusPagamento;
import guli.gulix.backend.gateway.pagamento.ResultadoCheckout;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class PagamentoFixture {

    private PagamentoFixture() {

    }

    public static Pagamento pagamento() {
        Pedido pedido = PedidoFixture.pedido();
        Pagamento pagamento = new Pagamento();

        pagamento.setId(1);
        pagamento.setPedido(pedido);
        pagamento.setMetodoPagamento(MetodoPagamento.CARTAO_CREDITO);
        pagamento.setStatusPagamento(StatusPagamento.PENDENTE);
        pagamento.setGateway(GatewayPagamento.STRIPE);
        pagamento.setGatewayCheckoutId("cs_test_123");
        pagamento.setGatewayPaymentId("pi_test_123");
        pagamento.setValorOriginal(pedido.getTotal());
        pagamento.setDesconto(BigDecimal.ZERO);
        pagamento.setPercentualJuros(BigDecimal.ZERO);
        pagamento.setValorJuros(BigDecimal.ZERO);
        pagamento.setValorFinal(pedido.getTotal());
        pagamento.setNumeroParcelas(3);
        pagamento.setValorParcela(new BigDecimal("421.96"));

        return pagamento;
    }

    public static ResultadoCheckout resultadoCheckout() {
        return new ResultadoCheckout(
                "https://checkout.stripe.com/c/pay/cs_test_123",
                "cs_test_123",
                "pi_test_123"
        );
    }

    public static PagamentoResponseDTO pagamentoResponseDTO() {
        PagamentoResponseDTO dto = new PagamentoResponseDTO();

        dto.setId(1);
        dto.setPedidoId(1);
        dto.setMetodoPagamento(MetodoPagamento.CARTAO_CREDITO);
        dto.setStatusPagamento(StatusPagamento.PENDENTE);
        dto.setValorOriginal(new BigDecimal("1265.88"));
        dto.setDesconto(BigDecimal.ZERO);
        dto.setPercentualJuros(BigDecimal.ZERO);
        dto.setValorJuros(BigDecimal.ZERO);
        dto.setValorFinal(new BigDecimal("1265.88"));
        dto.setNumeroParcelas(3);
        dto.setValorParcela(new BigDecimal("421.96"));
        dto.setCheckoutUrl("https://checkout.stripe.com/c/pay/cs_test_123");
        dto.setCriadoEm(LocalDateTime.of(2026, 9, 24, 15, 30));
        dto.setAtualizadoEm(LocalDateTime.of(2026, 9, 24, 16, 10));

        return dto;
    }
}
