package guli.gulix.backend.controller;

import guli.gulix.backend.dto.*;
import guli.gulix.backend.service.AuthService;
import guli.gulix.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
@Tag(
        name = "Autenticação",
        description = "Endpoints relacionados à autenticação e gerenciamento de tokens."
)
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    @Operation(
            summary = "Autenticar usuário",
            description = "Autentica um usuário utilizando suas credenciais e retorna os tokens de acesso."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário autenticado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "A requisição possui um body inválido ou malformado",
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
                    description = "Credenciais inválidas",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }



    @Operation(
            summary = "Cadastrar usuário",
            description = "Realiza o cadastro de um novo usuário na aplicação."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuário cadastrado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UsuarioResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "A requisição possui um body inválido ou malformado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    oneOf = {
                                            ValidationErrorResponseDTO.class,
                                            ErrorResponseDTO.class
                                    }
                            )
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> createNewUsuario(@Valid @RequestBody UsuarioCreateDTO usuarioRequest) {

        UsuarioResponseDTO response = usuarioService.createNewUsuario(usuarioRequest);

        HttpHeaders headers = new HttpHeaders();

        headers.add("Location", "/api/v1/usuarios/" + response.getId().toString());

        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(response);
    }

    @Operation(
            summary = "Renovar tokens de autenticação",
            description = "Gera um novo token de acesso e um novo refresh token utilizando um refresh token válido."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tokens renovados com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "A requisição possui um body inválido ou malformado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "O refresh token é inválido, expirado ou foi revogado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(
            @Valid @RequestBody RefreshTokenRequestDTO dto
            ) {
        return ResponseEntity.ok(authService.refresh(dto));
    }

    @Operation(
            summary = "Encerrar sessão",
            description = "Revoga o refresh token informado, invalidando a sessão associada."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Sessão encerrada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "A requisição possui um body inválido ou malformado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "O refresh token é inválido, expirado ou foi revogado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequestDTO dto
    ) {
        authService.logout(dto);

        return ResponseEntity.noContent().build();
    }
}
