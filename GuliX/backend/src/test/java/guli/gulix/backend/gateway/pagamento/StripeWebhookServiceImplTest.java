package guli.gulix.backend.gateway.pagamento;

import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import guli.gulix.backend.entity.ItemPedido;
import guli.gulix.backend.entity.Pagamento;
import guli.gulix.backend.entity.Pedido;
import guli.gulix.backend.entity.Produto;
import guli.gulix.backend.entity.enums.StatusPagamento;
import guli.gulix.backend.entity.enums.StatusPedido;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.fixture.PagamentoFixture;
import guli.gulix.backend.fixture.PedidoFixture;
import guli.gulix.backend.fixture.ProdutoFixture;
import guli.gulix.backend.repository.PagamentoRepository;
import guli.gulix.backend.service.EstoqueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripeWebhookServiceImplTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private EstoqueService estoqueService;

    @InjectMocks
    private StripeWebhookServiceImpl stripeWebhookService;

    private Pagamento pagamento;

    @BeforeEach
    void setup() {
        pagamento = PagamentoFixture.pagamento();
        pagamento.setPedido(PedidoFixture.pedidoComItem());
        pagamento.setStatusPagamento(StatusPagamento.PENDENTE);
    }

    // processar(Event event) — checkout.session.completed

    @Test
    void deveValidarCheckoutQuandoSessaoCorresponderAoPagamento() {
        when(pagamentoRepository.findById(1)).thenReturn(Optional.of(pagamento));

        stripeWebhookService.processar(evento(
                "checkout.session.completed",
                sessao("cs_test_123", "1")
        ));

        assertEquals(StatusPagamento.PENDENTE, pagamento.getStatusPagamento());
        assertEquals(StatusPedido.PENDENTE, pagamento.getPedido().getStatusPedido());
        assertEquals("cs_test_123", pagamento.getGatewayCheckoutId());
        verify(pagamentoRepository).findById(1);
        verify(pagamentoRepository, never()).save(any());
        verifyNoInteractions(estoqueService);
    }

    @Test
    void deveLancarExcecaoQuandoCheckoutNaoCorresponderAoPagamento() {
        when(pagamentoRepository.findById(1)).thenReturn(Optional.of(pagamento));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> stripeWebhookService.processar(evento(
                        "checkout.session.completed",
                        sessao("cs_test_outro", "1")
                ))
        );

        assertEquals("A Checkout Session não corresponde ao pagamento.", exception.getMessage());
        assertEquals(StatusPagamento.PENDENTE, pagamento.getStatusPagamento());
        verifyNoInteractions(estoqueService);
    }

    @Test
    void deveLancarExcecaoQuandoCheckoutNaoInformarPagamento() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> stripeWebhookService.processar(evento(
                        "checkout.session.completed",
                        sessao("cs_test_123", null)
                ))
        );

        assertEquals("Pagamento não informado na metadata da Checkout Session.", exception.getMessage());
        verifyNoInteractions(pagamentoRepository, estoqueService);
    }

    @Test
    void deveLancarExcecaoQuandoPagamentoDoCheckoutNaoExistir() {
        when(pagamentoRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> stripeWebhookService.processar(evento(
                        "checkout.session.completed",
                        sessao("cs_test_123", "99")
                ))
        );

        assertEquals("Pagamento não encontrado.", exception.getMessage());
        verify(pagamentoRepository).findById(99);
        verifyNoInteractions(estoqueService);
    }

    @Test
    void deveLancarExcecaoQuandoCheckoutNaoPuderSerDesserializado() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> stripeWebhookService.processar(evento("checkout.session.completed", null))
        );

        assertEquals("Não foi possível desserializar a Checkout Session.", exception.getMessage());
        verifyNoInteractions(pagamentoRepository, estoqueService);
    }

    // processar(Event event) — payment_intent.succeeded

    @Test
    void deveConfirmarPagamentoAprovarPedidoEBaixarEstoque() {
        Pedido pedido = pagamento.getPedido();
        ItemPedido segundoItem = PedidoFixture.itemPedido();
        Produto segundoProduto = ProdutoFixture.produto();
        segundoProduto.setId(2);
        segundoItem.setProduto(segundoProduto);
        segundoItem.setQuantidade(1);
        pedido.getItens().add(segundoItem);
        when(pagamentoRepository.findById(1)).thenReturn(Optional.of(pagamento));

        stripeWebhookService.processar(evento(
                "payment_intent.succeeded",
                paymentIntent("pi_test_sucesso", "1")
        ));

        assertEquals(StatusPagamento.CONFIRMADO, pagamento.getStatusPagamento());
        assertEquals("pi_test_sucesso", pagamento.getGatewayPaymentId());
        assertEquals(StatusPedido.APROVADO, pedido.getStatusPedido());
        verify(pagamentoRepository).findById(1);
        verify(pagamentoRepository, never()).save(any());
        verify(estoqueService).setQuantidadeReservada(1, 2, "subtracao");
        verify(estoqueService).setQuantidadeTotal(1, 2, "subtracao");
        verify(estoqueService).setQuantidadeReservada(2, 1, "subtracao");
        verify(estoqueService).setQuantidadeTotal(2, 1, "subtracao");
    }

    @Test
    void deveIgnorarPaymentIntentQuandoPagamentoJaEstiverConfirmado() {
        pagamento.setStatusPagamento(StatusPagamento.CONFIRMADO);
        pagamento.setGatewayPaymentId("pi_test_123");
        when(pagamentoRepository.findById(1)).thenReturn(Optional.of(pagamento));

        stripeWebhookService.processar(evento(
                "payment_intent.succeeded",
                paymentIntent("pi_test_novo", "1")
        ));

        assertEquals(StatusPagamento.CONFIRMADO, pagamento.getStatusPagamento());
        assertEquals("pi_test_123", pagamento.getGatewayPaymentId());
        assertEquals(StatusPedido.PENDENTE, pagamento.getPedido().getStatusPedido());
        verify(pagamentoRepository).findById(1);
        verifyNoInteractions(estoqueService);
    }

    @Test
    void deveLancarExcecaoQuandoPagamentoDoPaymentIntentNaoExistir() {
        when(pagamentoRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> stripeWebhookService.processar(evento(
                        "payment_intent.succeeded",
                        paymentIntent("pi_test_sucesso", "99")
                ))
        );

        assertEquals("Pagamento com id 99 não encontrado.", exception.getMessage());
        verify(pagamentoRepository).findById(99);
        verifyNoInteractions(estoqueService);
    }

    @Test
    void deveLancarExcecaoQuandoPaymentIntentNaoInformarPagamento() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> stripeWebhookService.processar(evento(
                        "payment_intent.succeeded",
                        paymentIntent("pi_test_sucesso", null)
                ))
        );

        assertEquals("Pagamento não informado na metadata do PaymentIntent.", exception.getMessage());
        verifyNoInteractions(pagamentoRepository, estoqueService);
    }

    @Test
    void deveLancarExcecaoQuandoPaymentIntentNaoPuderSerDesserializado() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> stripeWebhookService.processar(evento("payment_intent.succeeded", null))
        );

        assertEquals("Não foi possível desserializar o PaymentIntent.", exception.getMessage());
        verifyNoInteractions(pagamentoRepository, estoqueService);
    }

    @Test
    void deveLancarExcecaoQuandoIdDoPagamentoForInvalido() {
        NumberFormatException exception = assertThrows(
                NumberFormatException.class,
                () -> stripeWebhookService.processar(evento(
                        "payment_intent.succeeded",
                        paymentIntent("pi_test_sucesso", "abc")
                ))
        );

        assertEquals("For input string: \"abc\"", exception.getMessage());
        verifyNoInteractions(pagamentoRepository, estoqueService);
    }

    @Test
    void devePropagarExcecaoQuandoBaixaDeEstoqueFalhar() {
        when(pagamentoRepository.findById(1)).thenReturn(Optional.of(pagamento));
        doThrow(new RecursoNaoEncontradoException("Estoque do produto de id 1 não encontrado"))
                .when(estoqueService)
                .setQuantidadeReservada(1, 2, "subtracao");

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> stripeWebhookService.processar(evento(
                        "payment_intent.succeeded",
                        paymentIntent("pi_test_sucesso", "1")
                ))
        );

        assertEquals("Estoque do produto de id 1 não encontrado", exception.getMessage());
        verify(estoqueService, never()).setQuantidadeTotal(anyInt(), anyInt(), anyString());
    }

    // processar(Event event) — payment_intent.payment_failed

    @Test
    void deveRecusarPagamentoSemAlterarPedidoNemEstoque() {
        when(pagamentoRepository.findById(1)).thenReturn(Optional.of(pagamento));

        stripeWebhookService.processar(evento(
                "payment_intent.payment_failed",
                paymentIntent("pi_test_falha", "1")
        ));

        assertEquals(StatusPagamento.RECUSADO, pagamento.getStatusPagamento());
        assertEquals("pi_test_falha", pagamento.getGatewayPaymentId());
        assertEquals(StatusPedido.PENDENTE, pagamento.getPedido().getStatusPedido());
        verify(pagamentoRepository).findById(1);
        verifyNoInteractions(estoqueService);
    }

    @Test
    void deveLancarExcecaoQuandoPagamentoRecusadoNaoExistir() {
        when(pagamentoRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> stripeWebhookService.processar(evento(
                        "payment_intent.payment_failed",
                        paymentIntent("pi_test_falha", "99")
                ))
        );

        assertEquals("Pagamento com id 99 não encontrado.", exception.getMessage());
        verifyNoInteractions(estoqueService);
    }

    // processar(Event event) — evento não tratado

    @Test
    void deveIgnorarEventoNaoTratado() {
        Event event = mock(Event.class);
        when(event.getType()).thenReturn("charge.refunded");

        assertDoesNotThrow(() -> stripeWebhookService.processar(event));

        verifyNoInteractions(pagamentoRepository, estoqueService);
    }

    private Event evento(String tipo, StripeObject objeto) {
        Event event = mock(Event.class);
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        when(event.getType()).thenReturn(tipo);
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.ofNullable(objeto));
        return event;
    }

    private Session sessao(String sessionId, String pagamentoId) {
        Session session = new Session();
        session.setId(sessionId);
        session.setMetadata(metadata(pagamentoId));
        return session;
    }

    private PaymentIntent paymentIntent(String paymentIntentId, String pagamentoId) {
        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setId(paymentIntentId);
        paymentIntent.setMetadata(metadata(pagamentoId));
        return paymentIntent;
    }

    private Map<String, String> metadata(String pagamentoId) {
        Map<String, String> metadata = new HashMap<>();
        if (pagamentoId != null) {
            metadata.put("pagamento_id", pagamentoId);
        }
        return metadata;
    }
}
