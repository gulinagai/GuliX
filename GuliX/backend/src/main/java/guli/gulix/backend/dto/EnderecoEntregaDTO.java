package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados do endereço utilizado para entrega")
public class EnderecoEntregaDTO {

    @Schema(
            description = "Nome da rua",
            example = "Avenida Paulista"
    )
    private String rua;

    @Schema(
            description = "Número do endereço",
            example = "1000"
    )
    private String numero;

    @Schema(
            description = "Nome da cidade",
            example = "São Paulo"
    )
    private String cidade;

    @Schema(
            description = "Sigla do estado",
            example = "SP"
    )
    private String estado;

    @Schema(
            description = "CEP do endereço",
            example = "01310-100"
    )
    private String cep;
}
