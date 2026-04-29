package co.edu.cesde.pps.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Datos para actualizar un usuario desde el panel de administración")
public record UpdateAdminUserRequest(
        @Schema(description = "Nuevo email del usuario", example = "actualizado@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Email
        String email,

        @Schema(description = "Nombre", example = "Ana", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String firstName,

        @Schema(description = "Apellido", example = "López", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String lastName,

        @Schema(description = "Teléfono (opcional)", example = "+57 310 555 7788", nullable = true)
        String phone,

        @Schema(description = "Nuevo rol: ADMIN o CUSTOMER", example = "ADMIN", allowableValues = {"ADMIN", "CUSTOMER"}, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Pattern(regexp = "(?i)ADMIN|CUSTOMER", message = "role must be ADMIN or CUSTOMER")
        String role,

        @Schema(description = "Nuevo estado: ACTIVE o INACTIVE", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"}, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Pattern(regexp = "(?i)ACTIVE|INACTIVE", message = "status must be ACTIVE or INACTIVE")
        String status
) {
}
