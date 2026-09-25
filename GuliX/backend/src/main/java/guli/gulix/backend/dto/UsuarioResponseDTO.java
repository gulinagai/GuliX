package guli.gulix.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados retornados pela API após o cadastro ou consulta de um usuário.")
public class UsuarioResponseDTO {

    @Schema(
            description = "Identificador único do usuário.",
            example = "1"
    )
    private Integer id;

    @Schema(
            description = "Nome completo do usuário.",
            example = "João da Silva"
    )
    private String nome;

    @Schema(
            description = "Endereço de email do usuário.",
            example = "joao.silva@email.com"
    )
    private String email;

    @Schema(
            description = "Número de telefone do usuário.",
            example = "11987654321"
    )
    private String telefone;

    @Schema(
            description = "Data e hora em que o usuário foi cadastrado.",
            example = "2026-09-13T11:30:00"
    )
    private LocalDateTime criadoEm;

    @Schema(
            description = "Data e hora da última atualização dos dados do usuário.",
            example = "2026-09-13T12:15:00"
    )
    private LocalDateTime atualizadoEm;

}
