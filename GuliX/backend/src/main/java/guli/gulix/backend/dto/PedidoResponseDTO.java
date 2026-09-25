package guli.gulix.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import guli.gulix.backend.entity.enums.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados completos de um pedido")
public class PedidoResponseDTO {

    @Schema(
            description = "Identificador do pedido",
            example = "1"
    )
    private Integer id;

    @Schema(
            description = "Identificador do usuário que realizou o pedido",
            example = "10"
    )
    private Integer usuarioId;

    @Schema(
            description = "Status atual do pedido",
            example = "PENDENTE"
    )
    private StatusPedido statusPedido;

    @Schema(
            description = "Endereço de entrega utilizado no pedido"
    )
    private EnderecoEntregaDTO enderecoEntrega;

    @Schema(
            description = "Lista de itens pertencentes ao pedido"
    )
    private List<ItemPedidoResponseDTO> itens;

    @Schema(
            description = "Dados do pagamento associado ao pedido"
    )
    private PagamentoResponseDTO pagamento;

    @Schema(
            description = "Valor do frete do pedido",
            example = "25.90"
    )
    @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "0.00")
    private BigDecimal valorFrete;

    @Schema(
            description = "Valor total dos produtos antes do frete",
            example = "499.80"
    )
    @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "0.00")
    private BigDecimal subtotal;

    @Schema(
            description = "Valor total do pedido, incluindo produtos e frete",
            example = "525.70"
    )
    @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "0.00")
    private BigDecimal total;

    @Schema(
            description = "Data e hora em que o pedido foi criado",
            example = "2026-09-24T15:30:00"
    )
    private LocalDateTime criadoEm;

    @Schema(
            description = "Data e hora da última atualização do pedido",
            example = "2026-09-24T16:10:00"
    )
    private LocalDateTime atualizadoEm;

}
