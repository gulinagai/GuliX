package guli.gulix.backend.controller;


import guli.gulix.backend.dto.ErrorResponseDTO;
import guli.gulix.backend.dto.FilialResponseDTO;
import guli.gulix.backend.dto.FilialUpdateDTO;
import guli.gulix.backend.dto.ValidationErrorResponseDTO;
import guli.gulix.backend.service.FilialService;
import io.swagger.v3.oas.annotations.Operation;
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


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/filiais")
@Tag(
        name = "Filiais",
        description = "Operações relacionadas ao gerenciamento da filial."
)
public class FilialController {

    private final FilialService filialService;

    @Operation(
            summary = "Consultar filial",
            description = "Retorna os dados da filial atualmente cadastrada no sistema."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Filial encontrada.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FilialResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Filial não encontrada.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @GetMapping
    ResponseEntity<FilialResponseDTO> getFilial() {

        return ResponseEntity.ok().body(filialService.getFilial());
    }

    @Operation(
            summary = "Atualizar filial",
            description = "Atualiza os dados da filial atualmente cadastrada no sistema. Somente os campos informados serão alterados."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Filial atualizada com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FilialResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de atualização inválidos.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidationErrorResponseDTO.class)
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
                    description = "Usuário não possui permissão para atualizar a filial.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Filial não encontrada.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping
    ResponseEntity<FilialResponseDTO> updateFilial(@Valid @RequestBody FilialUpdateDTO dto) {

        return ResponseEntity.ok().body(filialService.updateFilial(dto));
    }

}
