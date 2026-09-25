package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para atualização parcial de um endereço")
public class EnderecoUpdateDTO {

    @Schema(
            description = "Nome da rua",
            example = "Avenida Paulista",
            maxLength = 150
    )
    @Size(max = 150, message = "A rua deve possuir no máximo 150 caracteres")
    @Pattern(
            regexp = ".*\\S.*",
            message = "A rua não pode estar vazia"
    )
    private String rua;

    @Schema(
            description = "Número do endereço",
            example = "1000",
            maxLength = 20
    )
    @Size(max = 20, message = "O número deve possuir no máximo 20 caracteres")
    @Pattern(
            regexp = ".*\\S.*",
            message = "O número não pode estar vazio"
    )
    private String numero;

    @Schema(
            description = "Nome da cidade",
            example = "São Paulo",
            maxLength = 100
    )
    @Size(max = 100, message = "A cidade deve possuir no máximo 100 caracteres")
    @Pattern(
            regexp = ".*\\S.*",
            message = "A cidade não pode estar vazia"
    )
    private String cidade;

    @Schema(
            description = "Sigla do estado",
            example = "SP",
            minLength = 2,
            maxLength = 2
    )
    @Size(min = 2, max = 2, message = "O estado deve possuir 2 caracteres")
    @Pattern(
            regexp = ".*\\S.*",
            message = "O estado não pode estar vazio"
    )
    private String estado;

    @Schema(
            description = "CEP do endereço",
            example = "01310-100",
            pattern = "\\d{5}-?\\d{3}"
    )
    @Pattern(
            regexp = "\\d{5}-?\\d{3}",
            message = "O CEP deve estar no formato 00000-000 ou 00000000"
    )
    private String cep;

}
