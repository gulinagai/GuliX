package guli.gulix.backend.dto;

import guli.gulix.backend.entity.enums.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados necessários para atualizar o status de um pedido")
public class PedidoUpdateStatusDTO {

    @Schema(
            description = "Novo status do pedido",
            example = "APROVADO"
    )
    @NotNull(message = "O status do pedido é obrigatório")
    private StatusPedido status;
}
