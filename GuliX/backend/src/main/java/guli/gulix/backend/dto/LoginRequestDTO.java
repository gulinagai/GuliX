package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema( description = "Dados necessários para autenticação de um usuário." )
public record LoginRequestDTO(

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email deve ser válido")
        @Schema( description = "Endereço de email utilizado para autenticação.", example = "usuario@email.com" )
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Schema( description = "Senha utilizada para autenticação.", example = "Senha@123" )
        String senha
) {}
