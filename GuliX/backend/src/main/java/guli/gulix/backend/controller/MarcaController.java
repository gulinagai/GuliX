package guli.gulix.backend.controller;




import guli.gulix.backend.dto.ErrorResponseDTO;
import guli.gulix.backend.dto.MarcaRequestDTO;
import guli.gulix.backend.dto.MarcaResponseDTO;
import guli.gulix.backend.dto.ValidationErrorResponseDTO;
import guli.gulix.backend.service.MarcaService;
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
@RequestMapping("/api/v1/marcas")
@Tag(
        name = "Marcas",
        description = "Endpoints para gerenciamento de marcas."
)
public class MarcaController {

    private final MarcaService marcaService;

    @Operation(
            summary = "Listar marcas",
            description = "Retorna uma lista de todas as marcas cadastradas."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Marcas encontradas com sucesso."
            )
    })
    @GetMapping
    public ResponseEntity<List<MarcaResponseDTO>> getListMarca() {

        List<MarcaResponseDTO> response = marcaService.getListMarca();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Buscar marca por ID",
            description = "Retorna os dados de uma marca específica a partir do seu identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Marca encontrada com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "O identificador da marca é inválido.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Marca não encontrada.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping("/{marcaId}")
    public ResponseEntity<MarcaResponseDTO> getMarcaById(
            @Parameter(
                    description = "Identificador único da marca.",
                    example = "1"
            )
            @PathVariable("marcaId") Integer marcaId) {
        return ResponseEntity.ok(marcaService.getMarcaById(marcaId));
    }

    @Operation(
            summary = "Cadastrar marca",
            description = "Cadastra uma nova marca no sistema. Esta operação requer autenticação com perfil ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Marca cadastrada com sucesso."
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
    public ResponseEntity<MarcaResponseDTO> createNewMarca(@Valid @RequestBody MarcaRequestDTO marcaRequest) {

        MarcaResponseDTO response = marcaService.createNewMarca(marcaRequest);

        HttpHeaders headers = new HttpHeaders();

        headers.add("Location", "/api/v1/marcas/" + response.getId().toString());

        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(response);
    }

    @Operation(
            summary = "Atualizar marca",
            description = "Atualiza completamente os dados de uma marca. Esta operação requer autenticação com perfil ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Marca atualizada com sucesso."
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
                    description = "Marca não encontrada.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{marcaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MarcaResponseDTO> updateMarcaById(
            @Parameter(
                    description = "Identificador único da marca.",
                    example = "1"
            )
            @PathVariable("marcaId") Integer marcaId,
            @Valid @RequestBody MarcaRequestDTO marcaAtualizar) {

        MarcaResponseDTO response = marcaService.updateMarcaById(marcaId, marcaAtualizar);

        return ResponseEntity.ok(response);

    }

    @Operation(
            summary = "Excluir marca",
            description = "Exclui uma marca do sistema a partir do seu identificador. Esta operação requer autenticação com perfil ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Marca excluída com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "O identificador da marca é inválido.",
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
                    description = "Marca não encontrada.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{marcaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMarcaById(
            @Parameter(
                    description = "Identificador único da marca.",
                    example = "1"
            )
            @PathVariable Integer marcaId
    ) {

        marcaService.deleteMarcaById(marcaId);

        return ResponseEntity.noContent().build();

    }

}