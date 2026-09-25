package guli.gulix.backend.dto;

import guli.gulix.backend.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados de um usuário retornados em operações administrativas.")
public class UsuarioAdminResponseDTO {

    @Schema( description = "Identificador único do usuário.", example = "1" )
    private Integer id;

    @Schema( description = "Nome completo do usuário.", example = "João da Silva" )
    private String nome;

    @Schema( description = "Endereço de email do usuário.", example = "joao.silva@email.com" )
    private String email;

    @Schema( description = "Número de telefone do usuário.", example = "11987654321" )
    private String telefone;

    @Schema( description = "Perfil de acesso do usuário na aplicação.", example = "ADMIN" )
    private Role role;

    @Schema( description = "Data e hora em que o usuário foi cadastrado.", example = "2026-09-13T11:30:00" )
    private LocalDateTime criadoEm;

    @Schema( description = "Data e hora da última atualização dos dados do usuário.", example = "2026-09-13T12:15:00" )
    private LocalDateTime atualizadoEm;

}
