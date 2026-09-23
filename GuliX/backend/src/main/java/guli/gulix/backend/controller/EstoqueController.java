package guli.gulix.backend.controller;


import guli.gulix.backend.dto.ErrorResponseDTO;
import guli.gulix.backend.dto.EstoqueRequestDTO;
import guli.gulix.backend.dto.EstoqueRequestInventarioDTO;
import guli.gulix.backend.dto.EstoqueResponseDTO;
import guli.gulix.backend.dto.ValidationErrorResponseDTO;
import guli.gulix.backend.service.EstoqueService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/estoques")
@Tag(
        name = "Estoque",
        description = "Operações relacionadas ao controle de estoque dos produtos."
)
public class EstoqueController {

    private final EstoqueService estoqueService;

    @Operation(
            summary = "Listar estoques",
            description = "Retorna a lista de estoques cadastrados."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estoques listados com sucesso."
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão para realizar esta operação.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<List<EstoqueResponseDTO>> getAllEstoque() {

        return ResponseEntity.ok(estoqueService.getAllEstoque());
    }

    @Operation(
            summary = "Consultar estoque de um produto",
            description = "Retorna as informações de estoque de um produto específico."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estoque encontrado com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Identificador do produto inválido.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Estoque ou produto não encontrado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão para realizar esta operação.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{produtoId}")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<EstoqueResponseDTO> getEstoqueByProdutoId(
            @Parameter(
                    description = "Identificador único do produto.",
                    example = "1"
            )
            @PathVariable("produtoId") Integer produtoId
    ) {
        return ResponseEntity.ok(estoqueService.getEstoqueByProdutoId(produtoId));
    }

    @Operation(
            summary = "Adicionar estoque",
            description = "Adiciona uma determinada quantidade de unidades ao estoque de um produto."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estoque adicionado com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados da movimentação inválidos.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    oneOf = {
                                            ValidationErrorResponseDTO.class,
                                            ErrorResponseDTO.class
                                    }
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto ou estoque não encontrado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão para realizar esta operação.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{produtoId}/entrada")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<EstoqueResponseDTO> adicionarEstoque(
            @Parameter(
                    description = "Identificador único do produto.",
                    example = "1"
            )
            @PathVariable("produtoId") Integer produtoId,
            @Valid @RequestBody EstoqueRequestDTO estoqueRequest
    ) {
        return ResponseEntity.ok(estoqueService.adicionarEstoque(produtoId, estoqueRequest));
    }


    @Operation(
            summary = "Remover estoque",
            description = "Remove uma determinada quantidade de unidades do estoque de um produto."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estoque removido com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados da movimentação inválidos ou quantidade insuficiente em estoque.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    oneOf = {
                                            ValidationErrorResponseDTO.class,
                                            ErrorResponseDTO.class
                                    }
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto ou estoque não encontrado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão para realizar esta operação.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{produtoId}/saida")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<EstoqueResponseDTO> removerEstoque(
            @Parameter(
                    description = "Identificador único do produto.",
                    example = "1"
            )
            @PathVariable("produtoId") Integer produtoId,
            @Valid @RequestBody EstoqueRequestDTO estoqueRequest) {
        return ResponseEntity.ok(estoqueService.removerEstoque(produtoId, estoqueRequest));
    }

    @Operation(
            summary = "Realizar inventário",
            description = "Atualiza o estoque de um produto com base na quantidade identificada durante o inventário."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventário realizado com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados do inventário inválidos.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    oneOf = {
                                            ValidationErrorResponseDTO.class,
                                            ErrorResponseDTO.class
                                    }
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto ou estoque não encontrado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão para realizar esta operação.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{produtoId}/inventario")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<EstoqueResponseDTO> realizarInventario(
            @Parameter(
                    description = "Identificador único do produto.",
                    example = "1"
            )
            @PathVariable("produtoId") Integer produtoId,
            @Valid @RequestBody EstoqueRequestInventarioDTO estoqueRequest) {
        return ResponseEntity.ok(estoqueService.realizarInventario(produtoId, estoqueRequest));
    }

}