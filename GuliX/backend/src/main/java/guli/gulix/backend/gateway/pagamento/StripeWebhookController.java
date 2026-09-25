package guli.gulix.backend.gateway.pagamento;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks/stripe")
@Tag(
        name = "Stripe Webhook",
        description = "Endpoint utilizado pelo Stripe para notificar eventos relacionados aos pagamentos"
)
public class StripeWebhookController {

    private final String webhookSecret;
    private final StripeWebhookService stripeWebhookService;

    public StripeWebhookController(
            @Value("${stripe.webhook.secret}") String webhookSecret,
            StripeWebhookService stripeWebhookService
    ) {
        this.webhookSecret = webhookSecret;
        this.stripeWebhookService = stripeWebhookService;
    }

    @Operation(
            summary = "Receber webhook do Stripe",
            description = "Recebe eventos enviados pelo Stripe, valida a assinatura da requisição e encaminha o evento para processamento."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Webhook recebido e processado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Assinatura do webhook inválida",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<Void> receberWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) {

        try {

            Event event = Webhook.constructEvent(
                    payload,
                    signature,
                    webhookSecret
            );

            stripeWebhookService.processar(event);

            return ResponseEntity.ok().build();

        } catch (SignatureVerificationException e) {

            return ResponseEntity.badRequest().build();
        }
    }
}