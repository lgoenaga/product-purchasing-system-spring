package co.edu.cesde.pps.web.controller;

import co.edu.cesde.pps.application.AuthApplicationService;
import co.edu.cesde.pps.web.dto.request.LoginRequest;
import co.edu.cesde.pps.web.dto.request.RegisterRequest;
import co.edu.cesde.pps.web.dto.response.AuthSessionResponse;
import co.edu.cesde.pps.web.dto.response.UserResponse;
import co.edu.cesde.pps.web.security.CurrentSessionResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Autenticación — sesión de invitado, registro, login y logout")
@RestController
@RequestMapping(ApiRoutes.AUTH)
public class AuthController {

    private final AuthApplicationService authApplicationService;
    private final CurrentSessionResolver currentSessionResolver;

    public AuthController(AuthApplicationService authApplicationService,
                          CurrentSessionResolver currentSessionResolver) {
        this.authApplicationService = authApplicationService;
        this.currentSessionResolver = currentSessionResolver;
    }

    @Operation(
            summary = "Crear sesión de invitado",
            description = """
                    Crea una sesión anónima (guest) sin necesidad de registro.
                    
                    Úsela para permitir que un usuario navegue y agregue productos al carrito
                    antes de registrarse o iniciar sesión. El `sessionToken` devuelto debe
                    enviarse en el header `Authorization: Bearer <sessionToken>` en todos
                    los endpoints que requieran sesión.
                    
                    Al registrarse o hacer login posteriormente, puede fusionar el carrito de
                    invitado usando `POST /api/v1/cart/merge`.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sesión de invitado creada exitosamente",
                    content = @Content(schema = @Schema(implementation = AuthSessionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    @PostMapping("/guest-session")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthSessionResponse createGuestSession() {
        return authApplicationService.createGuestSession();
    }

    @Operation(
            summary = "Registrar nuevo usuario",
            description = """
                    Registra un nuevo usuario CUSTOMER en el sistema y devuelve una sesión activa.
                    
                    Si el usuario tenía un carrito de invitado, puede pasar el `guestCartId`
                    para que los items se fusionen automáticamente al nuevo carrito.
                    
                    La contraseña debe tener entre 8 y 100 caracteres.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado y sesión creada",
                    content = @Content(schema = @Schema(implementation = AuthSessionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de registro inválidos (validación)", content = @Content),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<AuthSessionResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authApplicationService.register(request));
    }

    @Operation(
            summary = "Iniciar sesión",
            description = """
                    Autentica al usuario con email y contraseña, y devuelve un token de sesión.
                    
                    El campo `sessionToken` de la respuesta es el token opaco que debe incluirse
                    en el header `Authorization: Bearer <sessionToken>` para acceder a los
                    endpoints protegidos.
                    
                    Si el usuario tenía un carrito de invitado, puede pasar el `guestCartId`
                    para fusionar sus items en el carrito del usuario autenticado.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso — devuelve sessionToken",
                    content = @Content(schema = @Schema(implementation = AuthSessionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Email o contraseña con formato inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas", content = @Content)
    })
    @PostMapping("/login")
    public AuthSessionResponse login(@Valid @RequestBody LoginRequest request) {
        return authApplicationService.login(request);
    }

    @Operation(
            summary = "Obtener usuario actual",
            description = "Devuelve los datos del usuario dueño del sessionToken enviado en el header Authorization.",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Datos del usuario autenticado",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content)
    })
    @GetMapping("/me")
    public UserResponse getCurrentUser(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        return authApplicationService.getCurrentUser(
                currentSessionResolver.resolveCurrentToken(authorizationHeader)
        );
    }

    @Operation(
            summary = "Cerrar sesión",
            description = "Invalida el sessionToken enviado. Todos los requests posteriores con ese token serán rechazados.",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sesión cerrada exitosamente", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content)
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        authApplicationService.logout(currentSessionResolver.resolveCurrentToken(authorizationHeader));
        return ResponseEntity.noContent().build();
    }
}
