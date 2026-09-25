package guli.gulix.backend.controller;

import guli.gulix.backend.dto.CarrinhoResponseDTO;
import guli.gulix.backend.dto.ErrorResponseDTO;
import guli.gulix.backend.dto.ItemCarrinhoRequestDTO;
import guli.gulix.backend.dto.ItemCarrinhoUpdateDTO;
import guli.gulix.backend.dto.ValidationErrorResponseDTO;
import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.service.CarrinhoService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/carrinho")
@Tag(
        name = "Carrinho",
        description = "Operações relacionadas ao carrinho de compras do usuário"
)
@SecurityRequirement(name = "bearerAuth")
public class CarrinhoController {

    private final CarrinhoService carrinhoService;

    @Operation(
            summary = "Consultar carrinho",
            description = "Retorna o carrinho de compras do usuário autenticado, incluindo seus itens e o valor total."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Carrinho retornado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão para acessar o carrinho",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CarrinhoResponseDTO> buscarCarrinho(
            @AuthenticationPrincipal Usuario usuario
    ) {

        CarrinhoResponseDTO response = carrinhoService.buscarCarrinho(usuario.getId());

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Adicionar item ao carrinho",
            description = "Adiciona um produto ao carrinho do usuário autenticado. Caso o produto já esteja presente, sua quantidade é incrementada."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Item adicionado ao carrinho com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou quantidade solicitada indisponível em estoque",
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
                    description = "Usuário não possui permissão para adicionar itens ao carrinho",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @PostMapping("/itens")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> adicionarItem(
            @AuthenticationPrincipal  Usuario usuario,
            @Valid @RequestBody ItemCarrinhoRequestDTO dto
            ) {
        carrinhoService.adicionarItem(usuario.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Atualizar quantidade do item",
            description = "Atualiza a quantidade de um item existente no carrinho do usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Quantidade atualizada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou quantidade solicitada indisponível em estoque",
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
                    description = "Usuário não possui permissão para atualizar o carrinho",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Carrinho ou item não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @PutMapping("/itens/{itemId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> atualizarQuantidade (
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(
                    description = "Identificador do item do carrinho",
                    example = "1"
            )
            @PathVariable Integer itemId,
            @Valid @RequestBody ItemCarrinhoUpdateDTO dto
    ) {
        carrinhoService.atualizarQuantidade(usuario.getId(), itemId, dto);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Remover item do carrinho",
            description = "Remove um item do carrinho do usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Item removido com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão para remover itens do carrinho",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Carrinho ou item não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @DeleteMapping("/itens/{itemId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> removerItem (
            @AuthenticationPrincipal Usuario usuario,
            @Parameter(
                    description = "Identificador do item do carrinho",
                    example = "1"
            )
            @PathVariable Integer itemId
    ) {
        carrinhoService.removerItem(usuario.getId(), itemId);
        return ResponseEntity.noContent().build();
    }
}
