package guli.gulix.backend.controller;

import guli.gulix.backend.dto.*;
import guli.gulix.backend.service.CidadeService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cidades")
@Tag(
        name = "Cidades",
        description = "Operações relacionadas ao gerenciamento de cidades."
)
public class CidadeController {

    private final CidadeService cidadeService;

    @Operation(
            summary = "Lista todas as cidades",
            description = "Retorna uma lista contendo todas as cidades cadastradas."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cidades encontradas com sucesso."
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping
    ResponseEntity<List<CidadeResponseDTO>> getAllCidade() {

        return ResponseEntity.ok().body(cidadeService.getAllCidade());
    }

    @Operation(
            summary = "Busca uma cidade por ID",
            description = "Retorna os dados de uma cidade a partir do seu identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cidade encontrada com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID da cidade inválido.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cidade não encontrada.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping("/{cidadeId}")
    ResponseEntity<CidadeResponseDTO> getCidadeById(
            @Parameter(
                    description = "Identificador da cidade.",
                    example = "1"
            )
            @PathVariable("cidadeId") Integer cidadeId
    ) {

        return ResponseEntity.ok().body(cidadeService.getCidadeById(cidadeId));
    }

    @Operation(
            summary = "Cria uma nova cidade",
            description = "Cadastra uma nova cidade. Esta operação requer privilégios de administrador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Cidade criada com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados da cidade inválidos.",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Estado informado não foi encontrado.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão de administrador.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    ResponseEntity<CidadeResponseDTO> createNewCidade(@Valid @RequestBody CidadeCreateDTO dto) {

        CidadeResponseDTO novaCidade = cidadeService.createNewCidade(dto);

        HttpHeaders headers = new HttpHeaders();

        headers.add("Location", "/api/v1/cidades/" + novaCidade.id().toString());

        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(novaCidade);
    }

    @Operation(
            summary = "Atualiza uma cidade",
            description = "Atualiza parcialmente os dados de uma cidade. Apenas os campos informados serão alterados. Esta operação requer privilégios de administrador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cidade atualizada com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados da cidade inválidos.",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão de administrador.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cidade não encontrada.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{cidadeId}")
    ResponseEntity<CidadeResponseDTO> updateCidadeById(
            @Parameter(
                    description = "Identificador da cidade.",
                    example = "1"
            )
            @PathVariable Integer cidadeId,
            @Valid @RequestBody CidadeUpdateDTO dto) {

        return ResponseEntity.ok().body(cidadeService.updateCidadeById(cidadeId, dto));
    }


    @Operation(
            summary = "Exclui uma cidade",
            description = "Remove uma cidade pelo seu identificador. Esta operação requer privilégios de administrador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Cidade excluída com sucesso."
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão de administrador.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cidade não encontrada.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{cidadeId}")
    ResponseEntity<Void> deleteCidadeById(
            @Parameter(
                    description = "Identificador da cidade.",
                    example = "1"
            )
            @PathVariable Integer cidadeId
    ) {

        cidadeService.deleteCidadeById(cidadeId);

        return ResponseEntity.noContent().build();
    }


}
