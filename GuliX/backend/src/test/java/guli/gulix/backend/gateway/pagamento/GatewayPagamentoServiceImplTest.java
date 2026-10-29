package guli.gulix.backend.gateway.pagamento;

import guli.gulix.backend.entity.Pagamento;
import guli.gulix.backend.fixture.PagamentoFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GatewayPagamentoServiceImplTest {

    @Mock
    private StripeGateway stripeGateway;

    @InjectMocks
    private GatewayPagamentoServiceImpl gatewayPagamentoService;

    private Pagamento pagamento;
    private ResultadoCheckout resultadoCheckout;

    @BeforeEach
    void setup() {
        pagamento = PagamentoFixture.pagamento();
        resultadoCheckout = PagamentoFixture.resultadoCheckout();
    }

    // criarCheckout(Pagamento pagamento)

    @Test
    void deveDelegarCriacaoDoCheckoutAoStripeGateway() {
        when(stripeGateway.criarCheckout(pagamento)).thenReturn(resultadoCheckout);

        ResultadoCheckout resultado = gatewayPagamentoService.criarCheckout(pagamento);

        assertSame(resultadoCheckout, resultado);
        verify(stripeGateway).criarCheckout(pagamento);
    }
}
