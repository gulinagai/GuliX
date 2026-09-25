package guli.gulix.backend.controller;

import guli.gulix.backend.dto.*;
import guli.gulix.backend.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/produtos")
@Tag(
        name = "Produtos",
        description = "Endpoints para consulta e gerenciamento de produtos."
)
public class ProdutoController {
    private final ProdutoService produtoService;


    @Operation(
            summary = "Listar produtos",
            description = "Retorna uma lista paginada de produtos, permitindo filtrar os resultados por nome, categoria e marca."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Produtos encontrados com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parâmetros de consulta inválidos.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<Page<ProdutoResponseDTO>> getAllProduto(
            @Parameter(
                    description = "Nome ou parte do nome do produto utilizado como filtro.",
                    example = "RX 6650"
            )
            @RequestParam(required = false) String nome,

            @Parameter(
                    description = "Identificador da categoria utilizada como filtro.",
                    example = "3"
            )
            @RequestParam(required = false) Integer categoriaId,

            @Parameter(
                    description = "Identificador da marca utilizada como filtro.",
                    example = "5"
            )
            @RequestParam(required = false) Integer marcaId,


            Pageable pageable
    ) {
        return ResponseEntity.ok(produtoService.getAllProduto(nome, categoriaId, marcaId, pageable));
    }

    @Operation(
            summary = "Buscar produto por ID",
            description = "Retorna os dados de um produto específico a partir do seu identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Produto encontrado com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "O identificador do produto é inválido.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto não encontrado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping("/{produtoId}")
    public ResponseEntity<ProdutoResponseDTO> getProdutoById(
            @Parameter(
                    description = "Identificador único do produto.",
                    example = "1"
            )
            @PathVariable("produtoId") Integer produtoId) {
        return ResponseEntity.ok(produtoService.getProdutoById(produtoId));
    }

    @Operation(
            summary = "Cadastrar produto",
            description = "Cadastra um novo produto no sistema. Esta operação requer autenticação com perfil ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Produto cadastrado com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Os dados enviados são inválidos.",
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
                    responseCode = "401",
                    description = "Credenciais inválidas ou autenticação não realizada.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "O usuário autenticado não possui permissão de administrador.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProdutoResponseDTO> createNewProduto(@Valid @RequestBody ProdutoCreateDTO produtoRequest) {

        ProdutoResponseDTO novoProduto = produtoService.createNewProduto(produtoRequest);

        HttpHeaders headers = new HttpHeaders();

        headers.add("Location", "/api/v1/produtos/" + novoProduto.getId().toString());

        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(novoProduto);
    }

    @Operation(
            summary = "Excluir produto",
            description = "Exclui um produto do sistema a partir do seu identificador. Esta operação requer autenticação com perfil ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Produto excluído com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "O identificador do produto é inválido.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais inválidas ou autenticação não realizada.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "O usuário autenticado não possui permissão de administrador.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto não encontrado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{produtoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProdutoById(
            @Parameter(
                    description = "Identificador único do produto.",
                    example = "1"
            )
            @PathVariable("produtoId") Integer produtoId) {
        produtoService.deleteProdutoById(produtoId);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Atualizar produto",
            description = "Atualiza completamente os dados de um produto. Esta operação requer autenticação com perfil ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Produto atualizado com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Os dados enviados são inválidos.",
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
                    responseCode = "401",
                    description = "Credenciais inválidas ou autenticação não realizada.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "O usuário autenticado não possui permissão de administrador.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto não encontrado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{produtoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProdutoResponseDTO> updateProdutoById(
            @Parameter(
                    description = "Identificador único do produto.",
                    example = "1"
            )
            @PathVariable("produtoId") Integer produtoId,
            @Valid @RequestBody ProdutoUpdateDTO produtoAtualizar) {

        ProdutoResponseDTO produtoAtualizado = produtoService.updateProdutoById(produtoId, produtoAtualizar);

        return ResponseEntity.ok(produtoAtualizado);
    }

    @Operation(
            summary = "Atualizar parcialmente um produto",
            description = "Atualiza parcialmente os dados de um produto. Apenas os campos enviados na requisição serão alterados. Esta operação requer autenticação com perfil ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Produto atualizado com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Os dados enviados são inválidos.",
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
                    responseCode = "401",
                    description = "Credenciais inválidas ou autenticação não realizada.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "O usuário autenticado não possui permissão de administrador.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto não encontrado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{produtoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProdutoResponseDTO> updatePartialProdutoById(
            @Parameter(
                    description = "Identificador único do produto.",
                    example = "1"
            )
            @PathVariable("produtoId") Integer produtoId,
            @Valid @RequestBody ProdutoPatchDTO produtoAtualizar) {

        ProdutoResponseDTO produtoAtualizado = produtoService.updatePartialProdutoById(produtoId, produtoAtualizar);

       return ResponseEntity.ok(produtoAtualizado);
    }

}
