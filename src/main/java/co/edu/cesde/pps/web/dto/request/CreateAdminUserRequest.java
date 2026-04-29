package co.edu.cesde.pps.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear un usuario desde el panel de administración")
public record CreateAdminUserRequest(
        @Schema(description = "Email único del nuevo usuario", example = "nuevo@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Email
        String email,

        @Schema(description = "Contraseña temporal (mínimo 8, máximo 100 caracteres)", example = "TempPass123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(min = 8, max = 100)
        String password,

        @Schema(description = "Nombre", example = "Luis", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String firstName,

        @Schema(description = "Apellido", example = "Pérez", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String lastName,

        @Schema(description = "Teléfono (opcional)", example = "+57 320 111 2233", nullable = true)
        String phone,

        @Schema(description = "Rol del usuario: ADMIN o CUSTOMER", example = "CUSTOMER", allowableValues = {"ADMIN", "CUSTOMER"}, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Pattern(regexp = "(?i)ADMIN|CUSTOMER", message = "role must be ADMIN or CUSTOMER")
        String role,

        @Schema(description = "Estado del usuario: ACTIVE o INACTIVE", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"}, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Pattern(regexp = "(?i)ACTIVE|INACTIVE", message = "status must be ACTIVE or INACTIVE")
        String status
) {
}
