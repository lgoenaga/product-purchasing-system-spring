package co.edu.cesde.pps.web.controller;

import co.edu.cesde.pps.application.UserProfileApplicationService;
import co.edu.cesde.pps.web.dto.request.ChangeMyPasswordRequest;
import co.edu.cesde.pps.web.dto.request.UpdateMyProfileRequest;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Profile", description = "Perfil del usuario autenticado — datos personales y contraseña")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping(ApiRoutes.USER_PROFILE)
public class UserProfileController {

    private final UserProfileApplicationService userProfileApplicationService;
    private final CurrentSessionResolver currentSessionResolver;

    public UserProfileController(UserProfileApplicationService userProfileApplicationService,
                                 CurrentSessionResolver currentSessionResolver) {
        this.userProfileApplicationService = userProfileApplicationService;
        this.currentSessionResolver = currentSessionResolver;
    }

    @Operation(
            summary = "Actualizar mi perfil",
            description = "Actualiza el nombre, apellido y teléfono del usuario autenticado. No modifica email ni contraseña."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content)
    })
    @PutMapping
    public UserResponse updateMyProfile(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody UpdateMyProfileRequest request) {
        return userProfileApplicationService.updateMyProfile(
                currentSessionResolver.resolveCurrentToken(authorizationHeader),
                request
        );
    }

    @Operation(
            summary = "Cambiar mi contraseña",
            description = """
                    Cambia la contraseña del usuario autenticado.
                    
                    Requiere la contraseña actual para confirmar la identidad.
                    La nueva contraseña debe tener entre 8 y 100 caracteres.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contraseña cambiada exitosamente", content = @Content),
            @ApiResponse(responseCode = "400", description = "Contraseña actual incorrecta o nueva contraseña inválida", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content)
    })
    @PutMapping("/password")
    public ResponseEntity<Void> changeMyPassword(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody ChangeMyPasswordRequest request) {
        userProfileApplicationService.changeMyPassword(
                currentSessionResolver.resolveCurrentToken(authorizationHeader),
                request
        );
        return ResponseEntity.noContent().build();
    }
}
