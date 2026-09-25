package guli.gulix.backend.controller;

import guli.gulix.backend.dto.ErrorResponseDTO;
import guli.gulix.backend.dto.PedidoCreateDTO;
import guli.gulix.backend.dto.PedidoResponseDTO;
import guli.gulix.backend.dto.PedidoUpdateStatusDTO;
import guli.gulix.backend.dto.ValidationErrorResponseDTO;
import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(
        name = "Pedidos",
        description = "Operações relacionadas aos pedidos dos usuários"
)
@SecurityRequirement(name = "bearerAuth")
public class PedidoController {

    private final PedidoService pedidoService;

    @Operation(
            summary = "Listar pedidos do usuário",
            description = "Retorna todos os pedidos pertencentes ao usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedidos retornados com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão para acessar os pedidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PedidoResponseDTO>> getListPedidos(
            @AuthenticationPrincipal Usuario usuario
            ) {
        return ResponseEntity.ok(pedidoService.getListPedidos(usuario));
    }


    @Operation(
            summary = "Consultar pedido por ID",
            description = "Retorna os dados de um pedido específico. Usuários podem consultar apenas seus próprios pedidos, enquanto administradores podem consultar qualquer pedido."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido retornado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão para consultar o pedido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @GetMapping("/{pedidoId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PedidoResponseDTO> getPedidoById(
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(
                    description = "Identificador do pedido",
                    example = "1"
            )
            @PathVariable("pedidoId") Integer pedidoId
    ) {
        return ResponseEntity.ok(pedidoService.getPedidoById(usuario, pedidoId));
    }


    @Operation(
            summary = "Criar pedido",
            description = "Cria um novo pedido utilizando o carrinho do usuário autenticado e o endereço de entrega informado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Pedido criado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou estoque indisponível",
                    content = @Content(
                            schema = @Schema(
                                    oneOf = {
                                            ValidationErrorResponseDTO.class,
                                            ErrorResponseDTO.class
                                    }
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão para criar pedidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Carrinho ou endereço não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PedidoResponseDTO> createNewPedido(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody PedidoCreateDTO pedidoCreate
            ) {

        PedidoResponseDTO response = pedidoService.createNewPedido(usuario, pedidoCreate);

        HttpHeaders headers = new HttpHeaders();

        headers.add("Location", "/api/v1/pedidos/" + response.getId().toString());

        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(response);
    }

    @Operation(
            summary = "Cancelar pedido",
            description = "Cancela um pedido pertencente ao usuário autenticado. Somente pedidos com status PENDENTE podem ser cancelados."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido cancelado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Pedido não pode ser cancelado no estado atual",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão para cancelar o pedido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @PatchMapping("/{pedidoId}/cancelar")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PedidoResponseDTO> cancelPedido(
            @Parameter(
                    description = "Identificador do pedido",
                    example = "1"
            )
            @PathVariable("pedidoId") Integer pedidoId,
            @AuthenticationPrincipal Usuario usuario
    ) {
        PedidoResponseDTO response = pedidoService.cancelPedido(pedidoId, usuario);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Listar todos os pedidos",
            description = "Retorna todos os pedidos cadastrados no sistema. Disponível exclusivamente para administradores."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedidos retornados com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão de administrador",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PedidoResponseDTO>> getAllPedidos() {
        return ResponseEntity.ok(pedidoService.getAllPedidos());
    }

    @Operation(
            summary = "Atualizar status do pedido",
            description = "Atualiza o status de um pedido. Disponível exclusivamente para administradores."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Status do pedido atualizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou status não informado",
                    content = @Content(
                            schema = @Schema(
                                    oneOf = {
                                            ValidationErrorResponseDTO.class,
                                            ErrorResponseDTO.class
                                    }
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão de administrador",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pedido não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @PatchMapping("/admin/{pedidoId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PedidoResponseDTO> updateStatusPedido(
            @Parameter(
                    description = "Identificador do pedido",
                    example = "1"
            )
            @PathVariable("pedidoId") Integer pedidoId,
            @Valid @RequestBody PedidoUpdateStatusDTO dto
            ) {
        return ResponseEntity.ok(pedidoService.updateStatusPedido(pedidoId, dto));
    }
}
