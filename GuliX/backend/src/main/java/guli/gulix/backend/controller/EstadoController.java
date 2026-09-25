package guli.gulix.backend.controller;

import guli.gulix.backend.dto.ErrorResponseDTO;
import guli.gulix.backend.dto.EstadoCreateDTO;
import guli.gulix.backend.dto.EstadoResponseDTO;
import guli.gulix.backend.dto.EstadoUpdateDTO;
import guli.gulix.backend.dto.ValidationErrorResponseDTO;
import guli.gulix.backend.service.EstadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
@RequestMapping("/api/v1/estados")
@Tag(
        name = "Estados",
        description = "Endpoints para consulta e gerenciamento de estados."
)
public class EstadoController {

    private final EstadoService estadoService;

    @Operation(
            summary = "Listar estados",
            description = "Retorna uma lista contendo todos os estados cadastrados."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estados encontrados com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = EstadoResponseDTO.class)
                            )
                    )
            )
    })
    @GetMapping
    ResponseEntity<List<EstadoResponseDTO>> getAllEstado() {

        return ResponseEntity.ok().body(estadoService.getAllEstado());
    }

    @Operation(
            summary = "Buscar estado por ID",
            description = "Retorna os dados de um estado a partir do seu identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado encontrado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EstadoResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Estado não encontrado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping("/{estadoId}")
    ResponseEntity<EstadoResponseDTO> getEstadoById(
            @Parameter(
                    description = "Identificador único do estado.",
                    example = "1"
            )
            @PathVariable("estadoId") Integer estadoId
    ) {

        return ResponseEntity.ok().body(estadoService.getEstadoById(estadoId));
    }


    @Operation(
            summary = "Cadastrar estado",
            description = "Cadastra um novo estado. Operação permitida somente para usuários com perfil ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Estado cadastrado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EstadoResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "A requisição possui dados inválidos ou está malformada.",
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
                    description = "Usuário não autenticado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário autenticado, mas sem permissão para realizar esta operação.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    ResponseEntity<EstadoResponseDTO> createNewEstado(@Valid @RequestBody EstadoCreateDTO dto) {

        EstadoResponseDTO novoEstado = estadoService.createNewEstado(dto);

        HttpHeaders headers = new HttpHeaders();

        headers.add("Location", "/api/v1/estados/" + novoEstado.id().toString());

        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(novoEstado);
    }


    @Operation(
            summary = "Atualizar estado",
            description = "Atualiza parcialmente os dados de um estado. Operação permitida somente para usuários com perfil ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado atualizado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EstadoResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "A requisição possui dados inválidos ou está malformada.",
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
                    description = "Usuário não autenticado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário autenticado, mas sem permissão para realizar esta operação.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Estado não encontrado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{estadoId}")
    ResponseEntity<EstadoResponseDTO> updateEstadoById(
            @Parameter(
                    description = "Identificador único do estado.",
                    example = "1"
            )
            @PathVariable Integer estadoId,
            @Valid @RequestBody EstadoUpdateDTO dto
    ) {

        return ResponseEntity.ok().body(estadoService.updateEstadoById(estadoId, dto));
    }


    @Operation(
            summary = "Excluir estado",
            description = "Exclui um estado pelo seu identificador. Operação permitida somente para usuários com perfil ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Estado excluído com sucesso."
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
                    description = "Usuário autenticado, mas sem permissão para realizar esta operação.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Estado não encontrado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{estadoId}")
    ResponseEntity<Void> deleteEstadoById(
            @Parameter(
                    description = "Identificador único do estado.",
                    example = "1"
            )
            @PathVariable Integer estadoId
    ) {

        estadoService.deleteEstadoById(estadoId);

        return ResponseEntity.noContent().build();
    }


}
