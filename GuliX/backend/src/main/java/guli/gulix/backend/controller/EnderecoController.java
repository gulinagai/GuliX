package guli.gulix.backend.controller;

import guli.gulix.backend.dto.*;
import guli.gulix.backend.entity.Usuario;
import guli.gulix.backend.service.EnderecoService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/enderecos")
@Tag(
        name = "Endereços",
        description = "Endpoints para gerenciamento dos endereços do usuário"
)
public class EnderecoController {

    private final EnderecoService enderecoService;

    @Operation(
            summary = "Listar endereços",
            description = "Retorna todos os endereços cadastrados para o usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Endereços retornados com sucesso"
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
                    description = "Usuário não possui permissão para acessar este recurso",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EnderecoResponseDTO>> getListEnderecos(
            @AuthenticationPrincipal Usuario usuario
            ) {
        return ResponseEntity.ok(enderecoService.getListEnderecos(usuario));
    }

    @Operation(
            summary = "Buscar endereço por ID",
            description = "Retorna um endereço específico do usuário autenticado pelo seu identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Endereço encontrado"
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
                    description = "Usuário não possui permissão para acessar este recurso",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Endereço não encontrado",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{enderecoId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EnderecoResponseDTO> getEnderecoById(
            @Parameter(
                    description = "Identificador do endereço",
                    example = "1"
            )
            @PathVariable("enderecoId") Integer enderecoId,
            @AuthenticationPrincipal Usuario usuario
    ) {
        return ResponseEntity.ok(enderecoService.getEnderecoById(enderecoId, usuario));
    }

    @Operation(
            summary = "Criar endereço",
            description = "Cria um novo endereço para o usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Endereço criado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos",
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
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão para acessar este recurso",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cidade não encontrada ou não cadastrada",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EnderecoResponseDTO> createNewEndereco(
            @Valid @RequestBody EnderecoCreateDTO enderecoRequest,
            @AuthenticationPrincipal Usuario usuario
    ) {

        EnderecoResponseDTO novoEndereco = enderecoService.createNewEndereco(enderecoRequest, usuario);

        HttpHeaders headers = new HttpHeaders();

        headers.add("Location", "/api/v1/enderecos/" + novoEndereco.getId().toString());

        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(novoEndereco);
    }

    @Operation(
            summary = "Excluir endereço",
            description = "Exclui um endereço pertencente ao usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Endereço excluído com sucesso"
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
                    description = "Usuário não possui permissão para acessar este recurso",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Endereço não encontrado",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{enderecoId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteEnderecoById(
            @Parameter(
                    description = "Identificador do endereço",
                    example = "1"
            )
            @PathVariable("enderecoId") Integer enderecoId,
            @AuthenticationPrincipal Usuario usuario
    ) {
        enderecoService.deleteEnderecoById(enderecoId, usuario);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Atualizar endereço",
            description = "Atualiza parcialmente um endereço pertencente ao usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Endereço atualizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos",
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
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão para acessar este recurso",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Endereço não encontrado",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{enderecoId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EnderecoResponseDTO> updateEnderecoById(
            @Parameter(
                    description = "Identificador do endereço",
                    example = "1"
            )
            @PathVariable("enderecoId") Integer enderecoId,
            @Valid @RequestBody EnderecoUpdateDTO enderecoAtualizar,
            @AuthenticationPrincipal Usuario usuario
    ) {

        EnderecoResponseDTO enderecoAtualizado = enderecoService.updateEnderecoById(enderecoId, enderecoAtualizar, usuario);

        return ResponseEntity.ok(enderecoAtualizado);
    }

    @Operation(
            summary = "Definir endereço como principal",
            description = "Define um endereço do usuário autenticado como seu endereço principal."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Endereço definido como principal com sucesso"
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
                    description = "Usuário não possui permissão para acessar este recurso",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Endereço não encontrado",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{enderecoId}/principal")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> updateEnderecoPrincipalById(
            @Parameter(
                    description = "Identificador do endereço",
                    example = "1"
            )
            @PathVariable("enderecoId") Integer enderecoId,
            @AuthenticationPrincipal Usuario usuario
    ) {

        enderecoService.updateEnderecoPrincipalById(enderecoId, usuario);

        return ResponseEntity.noContent().build();
    }
}
