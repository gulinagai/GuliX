package guli.gulix.backend.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema( description = "Dados necessários para realizar o cadastro de um novo usuário." )
public class UsuarioCreateDTO {

    @NotBlank(message = "Nome é Obrigatório")
    @Schema( description = "Nome completo do usuário.", example = "João da Silva" )
    private String nome;

    @NotBlank(message = "Email é Obrigatório")
    @Email(message = "Email inválido")
    @Schema( description = "Endereço de email utilizado pelo usuário.", example = "joao.silva@email.com" )
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$",
            message = "Senha deve conter letra maiúscula, minúscula e número"
    )
    @Schema( description = "Senha utilizada para autenticação. Deve possuir no mínimo 8 caracteres, incluindo letra maiúscula, letra minúscula e número.", example = "Senha@123" )
    private String senha;


    @NotBlank(message = "Telefone é Obrigatório")
    @Pattern(
            regexp = "^\\d{10,11}$",
            message = "Telefone deve conter 10 ou 11 dígitos"
    )
    @Schema( description = "Número de telefone do usuário contendo apenas dígitos. Deve conter 10 ou 11 dígitos", example = "11987654321" )
    private String telefone;

}
