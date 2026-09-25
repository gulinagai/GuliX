package guli.gulix.backend.controller;

import guli.gulix.backend.dto.ErrorResponseDTO;
import guli.gulix.backend.dto.PoliticaFreteCreateDTO;
import guli.gulix.backend.dto.PoliticaFreteResponseDTO;
import guli.gulix.backend.dto.ValidationErrorResponseDTO;
import guli.gulix.backend.service.PoliticaFreteService;
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
@RequestMapping("/api/v1/politicas-frete")
@Tag(name = "Políticas de Frete", description = "Endpoints para gerenciamento das políticas de frete")
public class PoliticaFreteController {

    private final PoliticaFreteService politicaFreteService;


    @Operation(
            summary = "Listar políticas de frete",
            description = "Retorna todas as políticas de frete cadastradas, incluindo políticas ativas e históricas."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Políticas de frete retornadas com sucesso"
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
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    ResponseEntity<List<PoliticaFreteResponseDTO>> getAllPoliticaFrete() {

        return ResponseEntity.ok().body(politicaFreteService.getAllPoliticaFrete());
    }

    @Operation(
            summary = "Buscar política de frete por ID",
            description = "Retorna uma política de frete específica pelo seu identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Política de frete encontrada"
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
                    description = "Política de frete não encontrada",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{politicaFreteId}")
    ResponseEntity<PoliticaFreteResponseDTO> getPoliticaFreteById(
            @Parameter(
                    description = "Identificador da política de frete",
                    example = "1"
            )
            @PathVariable("politicaFreteId") Integer politicaFreteId
    ) {

        return ResponseEntity.ok().body(politicaFreteService.getPoliticaFreteById(politicaFreteId));
    }

    @Operation(
            summary = "Criar política de frete",
            description = "Cria uma nova política de frete para um estado. A nova política será ativada e, caso exista uma política ativa para o mesmo estado, ela será automaticamente desativada."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Política de frete criada com sucesso"
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
                    description = "Estado não encontrado",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    ResponseEntity<PoliticaFreteResponseDTO> createNewPoliticaFrete(@Valid @RequestBody PoliticaFreteCreateDTO dto) {

        PoliticaFreteResponseDTO novaPoliticaFrete = politicaFreteService.createNewPoliticaFrete(dto);

        HttpHeaders headers = new HttpHeaders();

        headers.add("Location", "/api/v1/politicas-frete/" + novaPoliticaFrete.id().toString());

        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(novaPoliticaFrete);
    }

}
