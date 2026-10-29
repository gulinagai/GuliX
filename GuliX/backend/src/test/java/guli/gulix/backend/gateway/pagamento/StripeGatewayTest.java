package guli.gulix.backend.gateway.pagamento;

import com.stripe.StripeClient;
import com.stripe.exception.ApiException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.service.CheckoutService;
import com.stripe.service.V1Services;
import com.stripe.service.checkout.SessionService;
import guli.gulix.backend.entity.Pagamento;
import guli.gulix.backend.entity.enums.MetodoPagamento;
import guli.gulix.backend.fixture.PagamentoFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripeGatewayTest {

    @Mock
    private StripeClient stripeClient;

    @Mock
    private V1Services v1Services;

    @Mock
    private CheckoutService checkoutService;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private StripeGateway stripeGateway;

    private Pagamento pagamento;

    @BeforeEach
    void setup() {
        pagamento = PagamentoFixture.pagamento();

        when(stripeClient.v1()).thenReturn(v1Services);
        when(v1Services.checkout()).thenReturn(checkoutService);
        when(checkoutService.sessions()).thenReturn(sessionService);
    }

    // criarCheckout(Pagamento pagamento)

    @Test
    void deveCriarCheckoutNoCartaoComParametrosDaSessao() throws Exception {
        when(sessionService.create(any(SessionCreateParams.class))).thenReturn(sessaoStripe());

        ResultadoCheckout resultado = stripeGateway.criarCheckout(pagamento);

        SessionCreateParams params = capturarParametros();
        SessionCreateParams.LineItem.PriceData priceData = linha(params).getPriceData();

        assertEquals(SessionCreateParams.Mode.PAYMENT, params.getMode());
        assertEquals(
                SessionCreateParams.PaymentMethodType.CARD,
                params.getPaymentMethodTypes().get(0)
        );
        assertEquals(1, params.getPaymentMethodTypes().size());
        assertEquals("http://localhost:8080/pagamento/sucesso", params.getSuccessUrl());
        assertEquals("http://localhost:8080/pagamento/cancelado", params.getCancelUrl());
        assertEquals("1", params.getMetadata().get("pagamento_id"));
        assertEquals("1", params.getPaymentIntentData().getMetadata().get("pagamento_id"));
        assertEquals(1L, linha(params).getQuantity());
        assertEquals("brl", priceData.getCurrency());
        assertEquals(126588L, priceData.getUnitAmount());
        assertEquals("Pedido GuliX", priceData.getProductData().getName());
        assertEquals("https://checkout.stripe.com/c/pay/cs_test_123", resultado.getCheckoutUrl());
        assertEquals("cs_test_123", resultado.getGatewayCheckoutId());
        assertEquals("pi_test_123", resultado.getGatewayPaymentId());
    }

    @Test
    void deveCriarCheckoutNoBoletoUsandoValorFinal() throws Exception {
        pagamento.setMetodoPagamento(MetodoPagamento.BOLETO);
        pagamento.setValorOriginal(new BigDecimal("1265.88"));
        pagamento.setValorFinal(new BigDecimal("1139.29"));
        when(sessionService.create(any(SessionCreateParams.class))).thenReturn(sessaoStripe());

        stripeGateway.criarCheckout(pagamento);

        SessionCreateParams params = capturarParametros();

        assertEquals(
                SessionCreateParams.PaymentMethodType.BOLETO,
                params.getPaymentMethodTypes().get(0)
        );
        assertEquals(113929L, linha(params).getPriceData().getUnitAmount());
    }

    @Test
    void deveConverterValorFinalParaCentavosComArredondamentoHalfUp() throws Exception {
        pagamento.setValorFinal(new BigDecimal("10.125"));
        when(sessionService.create(any(SessionCreateParams.class))).thenReturn(sessaoStripe());

        stripeGateway.criarCheckout(pagamento);

        assertEquals(1013L, linha(capturarParametros()).getPriceData().getUnitAmount());
    }

    @Test
    void deveLancarExcecaoQuandoStripeFalharAoCriarSessao() throws Exception {
        ApiException falhaStripe = new ApiException(
                "falha ao criar sessao",
                "req_123",
                "api_error",
                500,
                null
        );
        when(sessionService.create(any(SessionCreateParams.class))).thenThrow(falhaStripe);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> stripeGateway.criarCheckout(pagamento)
        );

        assertEquals("Erro ao criar Checkout Session no Stripe", exception.getMessage());
        assertSame(falhaStripe, exception.getCause());
        verify(sessionService).create(any(SessionCreateParams.class));
    }

    private Session sessaoStripe() {
        Session session = new Session();
        session.setUrl("https://checkout.stripe.com/c/pay/cs_test_123");
        session.setId("cs_test_123");
        session.setPaymentIntent("pi_test_123");
        return session;
    }

    private SessionCreateParams capturarParametros() throws Exception {
        ArgumentCaptor<SessionCreateParams> captor = ArgumentCaptor.forClass(SessionCreateParams.class);
        verify(sessionService).create(captor.capture());
        return captor.getValue();
    }

    private SessionCreateParams.LineItem linha(SessionCreateParams params) {
        return params.getLineItems().get(0);
    }
}
