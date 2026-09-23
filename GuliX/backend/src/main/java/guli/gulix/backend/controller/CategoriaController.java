package guli.gulix.backend.controller;


import guli.gulix.backend.dto.CategoriaRequestDTO;
import guli.gulix.backend.dto.CategoriaResponseDTO;
import guli.gulix.backend.dto.ErrorResponseDTO;
import guli.gulix.backend.dto.ValidationErrorResponseDTO;
import guli.gulix.backend.service.CategoriaService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/categorias")
@Tag(
        name = "Categorias",
        description = "Endpoints para gerenciamento de categorias."
)
public class CategoriaController {
    private final CategoriaService categoriaService;

    @Operation(
            summary = "Listar categorias",
            description = "Retorna uma lista de todas as categorias cadastradas."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categorias encontradas com sucesso."
            )
    })
    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> getListCategoria() {

        List<CategoriaResponseDTO> response = categoriaService.getListCategoria();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Buscar categoria por ID",
            description = "Retorna os dados de uma categoria específica a partir do seu identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoria encontrada com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "O identificador da categoria é inválido.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoria não encontrada.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping("/{categoriaId}")
    public ResponseEntity<CategoriaResponseDTO> getCategoriaById(
            @Parameter(
                    description = "Identificador único da categoria.",
                    example = "1"
            )
            @PathVariable("categoriaId") Integer categoriaId
    ) {
        return ResponseEntity.ok(categoriaService.getCategoriaById(categoriaId));
    }


    @Operation(
            summary = "Cadastrar categoria",
            description = "Cadastra uma nova categoria no sistema. Esta operação requer autenticação com perfil ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Categoria cadastrada com sucesso."
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
    public ResponseEntity<CategoriaResponseDTO> createNewCategoria(@Valid @RequestBody CategoriaRequestDTO categoriaRequest) {

        CategoriaResponseDTO response = categoriaService.createNewCategoria(categoriaRequest);

        HttpHeaders headers = new HttpHeaders();

        headers.add("Location", "/api/v1/categorias/" + response.getId().toString());

        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(response);

    }


    @Operation(
            summary = "Atualizar categoria",
            description = "Atualiza completamente os dados de uma categoria. Esta operação requer autenticação com perfil ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoria atualizada com sucesso."
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
                    description = "Categoria não encontrada.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{categoriaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaResponseDTO> updateCategoriaById(
            @Parameter(
                    description = "Identificador único da categoria.",
                    example = "1"
            )
            @PathVariable("categoriaId") Integer categoriaId,
            @Valid @RequestBody CategoriaRequestDTO categoriaAtualizar) {

        CategoriaResponseDTO response = categoriaService.updateCategoriaById(categoriaId, categoriaAtualizar);

        return ResponseEntity.ok(response);

    }

    @Operation(
            summary = "Excluir categoria",
            description = "Exclui uma categoria do sistema a partir do seu identificador. Esta operação requer autenticação com perfil ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Categoria excluída com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "O identificador da categoria é inválido.",
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
                    description = "Categoria não encontrada.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{categoriaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategoriaById(
            @Parameter(
                    description = "Identificador único da categoria.",
                    example = "1"
            )
            @PathVariable Integer categoriaId
    ) {

        categoriaService.deleteCategoriaById(categoriaId);

        return ResponseEntity.noContent().build();

    }
}
