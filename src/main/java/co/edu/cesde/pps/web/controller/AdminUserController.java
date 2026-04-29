package co.edu.cesde.pps.web.controller;

import co.edu.cesde.pps.application.AdminUserApplicationService;
import co.edu.cesde.pps.web.dto.request.CreateAdminUserRequest;
import co.edu.cesde.pps.web.dto.request.UpdateAdminUserRequest;
import co.edu.cesde.pps.web.dto.response.UserResponse;
import co.edu.cesde.pps.web.security.AdminAccessGuard;
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
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin — Users", description = "Administración de usuarios — requiere sessionToken con rol ADMIN")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping(ApiRoutes.ADMIN_USERS)
public class AdminUserController {

    private final AdminUserApplicationService adminUserApplicationService;
    private final AdminAccessGuard adminAccessGuard;

    public AdminUserController(AdminUserApplicationService adminUserApplicationService,
                               AdminAccessGuard adminAccessGuard) {
        this.adminUserApplicationService = adminUserApplicationService;
        this.adminAccessGuard = adminAccessGuard;
    }

    @Operation(
            summary = "Crear usuario",
            description = """
                    Crea un nuevo usuario desde el panel de administración. **Requiere rol ADMIN.**
                    
                    A diferencia del registro público (`/auth/register`), permite asignar
                    un rol específico (`ADMIN` o `CUSTOMER`) y un estado (`ACTIVE` o `INACTIVE`).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o email duplicado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN", content = @Content)
    })
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody CreateAdminUserRequest request) {
        adminAccessGuard.requireAdmin(authorizationHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(adminUserApplicationService.createUser(request));
    }

    @Operation(
            summary = "Listar todos los usuarios",
            description = "Devuelve la lista completa de usuarios del sistema. **Requiere rol ADMIN.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuarios",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN", content = @Content)
    })
    @GetMapping
    public List<UserResponse> listUsers(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        adminAccessGuard.requireAdmin(authorizationHeader);
        return adminUserApplicationService.listUsers();
    }

    @Operation(
            summary = "Obtener usuario por ID",
            description = "Devuelve el detalle de un usuario específico. **Requiere rol ADMIN.**"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle del usuario",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public UserResponse getUser(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathVariable Long id) {
        adminAccessGuard.requireAdmin(authorizationHeader);
        return adminUserApplicationService.getUser(id);
    }

    @Operation(
            summary = "Actualizar usuario",
            description = """
                    Actualiza los datos, rol y estado de un usuario. **Requiere rol ADMIN.**
                    
                    Permite cambiar el email, nombre, rol (`ADMIN`/`CUSTOMER`) y
                    estado (`ACTIVE`/`INACTIVE`). No modifica la contraseña.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o email duplicado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    public UserResponse updateUser(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Parameter(description = "ID del usuario a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UpdateAdminUserRequest request) {
        adminAccessGuard.requireAdmin(authorizationHeader);
        return adminUserApplicationService.updateUser(id, request);
    }

    @Operation(
            summary = "Eliminar usuario",
            description = "Elimina un usuario del sistema. **Requiere rol ADMIN.** Esta acción puede ser irreversible."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Parameter(description = "ID del usuario a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        adminAccessGuard.requireAdmin(authorizationHeader);
        adminUserApplicationService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
