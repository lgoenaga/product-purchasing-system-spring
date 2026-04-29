package co.edu.cesde.pps.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para cambiar la contraseña del usuario autenticado")
public record ChangeMyPasswordRequest(
        @Schema(description = "Contraseña actual del usuario (para confirmar identidad)", example = "OldPass123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String currentPassword,

        @Schema(description = "Nueva contraseña (mínimo 8, máximo 100 caracteres)", example = "NewSecure99!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(min = 8, max = 100)
        String newPassword
) {
}
