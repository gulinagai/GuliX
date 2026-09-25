package guli.gulix.backend.dto;

import guli.gulix.backend.entity.enums.MetodoPagamento;
import guli.gulix.backend.entity.enums.StatusPagamento;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados completos de um pagamento")
public class PagamentoResponseDTO {

    @Schema(
            description = "Identificador do pagamento",
            example = "1"
    )
    private Integer id;

    @Schema(
            description = "Identificador do pedido associado ao pagamento",
            example = "10"
    )
    private Integer pedidoId;

    @Schema(
            description = "Método de pagamento utilizado",
            example = "CARTAO_CREDITO"
    )
    private MetodoPagamento metodoPagamento;

    @Schema(
            description = "Status atual do pagamento",
            example = "PENDENTE"
    )
    private StatusPagamento statusPagamento;

    @Schema(
            description = "Valor original do pedido antes da aplicação de descontos e juros",
            example = "525.70"
    )
    private BigDecimal valorOriginal;

    @Schema(
            description = "Valor do desconto aplicado ao pagamento",
            example = "52.57"
    )
    private BigDecimal desconto;

    @Schema(
            description = "Percentual de juros aplicado ao pagamento",
            example = "2.00"
    )
    private BigDecimal percentualJuros;

    @Schema(
            description = "Valor dos juros aplicados ao pagamento",
            example = "9.46"
    )
    private BigDecimal valorJuros;

    @Schema(
            description = "Valor final do pagamento após descontos e juros",
            example = "482.59"
    )
    private BigDecimal valorFinal;

    @Schema(
            description = "Número de parcelas do pagamento. Aplicável ao cartão de crédito",
            example = "12"
    )
    private Integer numeroParcelas;

    @Schema(
            description = "Valor de cada parcela do pagamento",
            example = "40.22"
    )
    private BigDecimal valorParcela;

    @Schema(
            description = "URL para acessar o checkout do gateway de pagamento",
            example = "https://checkout.stripe.com/c/pay/cs_test_123"
    )
    private String checkoutUrl;

    @Schema(
            description = "Data e hora em que o pagamento foi criado",
            example = "2026-09-24T15:30:00"
    )
    private LocalDateTime criadoEm;

    @Schema(
            description = "Data e hora da última atualização do pagamento",
            example = "2026-09-24T16:10:00"
    )
    private LocalDateTime atualizadoEm;

}
