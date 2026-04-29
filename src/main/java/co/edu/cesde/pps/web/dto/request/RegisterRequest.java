package co.edu.cesde.pps.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar un nuevo usuario CUSTOMER")
public record RegisterRequest(
        @Schema(description = "Email único del usuario", example = "maria@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Email
        String email,

        @Schema(description = "Contraseña (mínimo 8, máximo 100 caracteres)", example = "SecurePass99!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(min = 8, max = 100)
        String password,

        @Schema(description = "Nombre del usuario", example = "María", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String firstName,

        @Schema(description = "Apellido del usuario", example = "García", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String lastName,

        @Schema(description = "Número de teléfono (opcional)", example = "+57 300 123 4567", nullable = true)
        String phone,

        @Schema(description = "ID del carrito de invitado a fusionar tras el registro (opcional)", example = "42", nullable = true)
        Long guestCartId
) {
}
