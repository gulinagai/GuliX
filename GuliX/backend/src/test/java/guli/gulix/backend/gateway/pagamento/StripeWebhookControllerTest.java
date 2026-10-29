package guli.gulix.backend.gateway.pagamento;

import com.stripe.model.Event;
import guli.gulix.backend.exception.RecursoNaoEncontradoException;
import guli.gulix.backend.repository.UsuarioRepository;
import guli.gulix.backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StripeWebhookController.class)
@TestPropertySource(properties = "stripe.webhook.secret=whsec_teste_gulix")
class StripeWebhookControllerTest {

    private static final String WEBHOOK_SECRET = "whsec_teste_gulix";
    private static final String PAYLOAD = """
            {"id":"evt_test_123","object":"event","type":"checkout.session.completed","data":{"object":{"id":"cs_test_123","object":"checkout.session"}}}""";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StripeWebhookService stripeWebhookService;

    // Dependências utilizadas pelo filtro JWT carregado no contexto MVC.
    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    // POST /webhooks/stripe

    @Test
    void deveRetornar200QuandoAssinaturaForValida() throws Exception {
        mockMvc.perform(post("/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", assinar(PAYLOAD))
                        .content(PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        ArgumentCaptor<Event> eventoCaptor = ArgumentCaptor.forClass(Event.class);
        verify(stripeWebhookService).processar(eventoCaptor.capture());
        assertEquals("checkout.session.completed", eventoCaptor.getValue().getType());
    }

    @Test
    void deveRetornar200QuandoEventoConstruidoNaoForTratadoPeloController() throws Exception {
        String payload = PAYLOAD.replace("checkout.session.completed", "charge.refunded");

        mockMvc.perform(post("/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", assinar(payload))
                        .content(payload))
                .andExpect(status().isOk());

        ArgumentCaptor<Event> eventoCaptor = ArgumentCaptor.forClass(Event.class);
        verify(stripeWebhookService).processar(eventoCaptor.capture());
        assertEquals("charge.refunded", eventoCaptor.getValue().getType());
    }

    @Test
    void deveRetornar400QuandoAssinaturaForInvalida() throws Exception {
        mockMvc.perform(post("/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", "t=1,v1=assinatura-invalida")
                        .content(PAYLOAD))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));

        verifyNoInteractions(stripeWebhookService);
    }

    @Test
    void deveRetornar400QuandoPayloadNaoCorresponderAAssinatura() throws Exception {
        String assinatura = assinar(PAYLOAD);

        mockMvc.perform(post("/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", assinatura)
                        .content(PAYLOAD.replace("cs_test_123", "cs_test_alterado")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));

        verifyNoInteractions(stripeWebhookService);
    }

    @Test
    void deveRetornar400QuandoCorpoEstiverAusente() throws Exception {
        mockMvc.perform(post("/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", assinar(PAYLOAD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("O corpo da requisição é inválido ou está em formato incorreto"));

        verifyNoInteractions(stripeWebhookService);
    }

    @Test
    void deveRetornar500QuandoAssinaturaNaoForInformada() throws Exception {
        mockMvc.perform(post("/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Ocorreu um erro inesperado ao processar a requisição"));

        verifyNoInteractions(stripeWebhookService);
    }

    @Test
    void deveRetornar500QuandoPayloadAssinadoNaoForUmEvento() throws Exception {
        String payload = "nao-e-json";

        mockMvc.perform(post("/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", assinar(payload))
                        .content(payload))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));

        verifyNoInteractions(stripeWebhookService);
    }

    @Test
    void deveRetornar404QuandoProcessamentoNaoEncontrarPagamento() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Pagamento não encontrado."))
                .when(stripeWebhookService).processar(any(Event.class));

        mockMvc.perform(post("/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", assinar(PAYLOAD))
                        .content(PAYLOAD))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Pagamento não encontrado."));

        verify(stripeWebhookService).processar(any(Event.class));
    }

    @Test
    void deveRetornar500QuandoProcessamentoFalhar() throws Exception {
        doThrow(new IllegalStateException("Não foi possível desserializar a Checkout Session."))
                .when(stripeWebhookService).processar(any(Event.class));

        mockMvc.perform(post("/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", assinar(PAYLOAD))
                        .content(PAYLOAD))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Ocorreu um erro inesperado ao processar a requisição"));
    }

    private String assinar(String payload) throws Exception {
        long timestamp = Instant.now().getEpochSecond();
        String assinatura = com.stripe.net.Webhook.Util.computeHmacSha256(
                WEBHOOK_SECRET,
                timestamp + "." + payload
        );
        return "t=" + timestamp + ",v1=" + assinatura;
    }
}
