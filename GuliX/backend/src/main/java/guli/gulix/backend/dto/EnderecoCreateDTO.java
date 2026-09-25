package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados necessários para criar um endereço")
public class EnderecoCreateDTO {

    @Schema(
            description = "Nome da rua",
            example = "Avenida Paulista",
            maxLength = 150
    )
    @NotBlank(message = "A rua é obrigatória")
    @Size(max = 150, message = "A rua deve possuir no máximo 150 caracteres")
    private String rua;

    @Schema(
            description = "Número do endereço",
            example = "1000",
            maxLength = 20
    )
    @NotBlank(message = "O número é obrigatório")
    @Size(max = 20, message = "O número deve possuir no máximo 20 caracteres")
    private String numero;

    @Schema(
            description = "Nome da cidade",
            example = "São Paulo",
            maxLength = 100
    )
    @NotBlank(message = "A cidade é obrigatória")
    @Size(max = 100, message = "A cidade deve possuir no máximo 100 caracteres")
    private String cidade;

    @Schema(
            description = "Sigla do estado",
            example = "SP",
            minLength = 2,
            maxLength = 2
    )
    @NotBlank(message = "O estado é obrigatório")
    @Size(min = 2, max = 2, message = "O estado deve possuir 2 caracteres")
    private String estado;

    @Schema(
            description = "CEP do endereço",
            example = "01310-100",
            pattern = "\\d{5}-?\\d{3}"
    )
    @NotBlank(message = "O CEP é obrigatório")
    @Pattern(
            regexp = "\\d{5}-?\\d{3}",
            message = "O CEP deve estar no formato 00000-000 ou 00000000"
    )
    private String cep;

}
