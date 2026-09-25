package guli.gulix.backend.controller;

import guli.gulix.backend.dto.ErrorResponseDTO;
import guli.gulix.backend.dto.PagamentoResponseDTO;
import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.service.PagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/pagamentos")
@Tag(
        name = "Pagamentos",
        description = "Operações relacionadas aos pagamentos dos pedidos"
)
@SecurityRequirement(name = "bearerAuth")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @Operation(
            summary = "Consultar pagamento por ID",
            description = "Retorna os dados de um pagamento específico. Usuários podem consultar apenas pagamentos de seus próprios pedidos, enquanto administradores podem consultar qualquer pagamento."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pagamento retornado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão para consultar o pagamento",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pagamento não encontrado",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping("/{pagamentoId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PagamentoResponseDTO> getPagamentoById(
            @Parameter(
                    description = "Identificador do pagamento",
                    example = "1"
            )
            @PathVariable Integer pagamentoId,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.ok(pagamentoService.getPagamentoById(pagamentoId, usuario));
    }

}