package guli.gulix.backend.dto;

import guli.gulix.backend.entity.enums.MetodoPagamento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados necessários para criar um novo pedido")
public class PedidoCreateDTO {

    @Schema(
            description = "Identificador do endereço de entrega do pedido",
            example = "5",
            minimum = "1"
    )
    @NotNull(message = "O endereço é obrigatório")
    @Positive(message = "O ID do endereço deve ser positivo")
    private Integer enderecoId;

    @Schema(
            description = "Método de pagamento utilizado no pedido",
            example = "CARTAO_CREDITO"
    )
    @NotNull(message = "O método de pagamento é obrigatório")
    private MetodoPagamento metodoPagamento;

    @Schema(
            description = "Número de parcelas para o pagamento",
            example = "3",
            minimum = "1",
            maximum = "48"
    )
    @Positive(message = "O número de parcelas deve ser maior que zero")
    @Max(48)
    private Integer numeroParcelas;
}


