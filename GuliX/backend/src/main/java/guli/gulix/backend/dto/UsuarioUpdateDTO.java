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
@Schema(description = "Dados que podem ser alterados no cadastro de um usuário.")
public class UsuarioUpdateDTO {


    @Size(min = 1, message = "Nome não pode ser vazio")
    @Schema( description = "Novo nome do usuário.", example = "João da Silva" )
    private String nome;


    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$",
            message = "Senha deve conter letra maiúscula, minúscula e número"
    )
    @Schema( description = "Nova senha do usuário. Deve possuir no mínimo 8 caracteres, incluindo letra maiúscula, letra minúscula e número.", example = "NovaSenha123" )
    private String senha;

    @Pattern(
            regexp = "^\\d{10,11}$",
            message = "Telefone deve conter 10 ou 11 dígitos"
    )
    @Schema( description = "Novo número de telefone do usuário, contendo apenas 10 ou 11 dígitos.", example = "11987654321" )
    private String telefone;

}
