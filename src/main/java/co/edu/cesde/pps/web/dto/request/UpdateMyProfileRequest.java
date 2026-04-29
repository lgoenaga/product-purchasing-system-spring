package co.edu.cesde.pps.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Nuevos datos del perfil del usuario autenticado")
public record UpdateMyProfileRequest(
        @Schema(description = "Nuevo nombre", example = "Carlos", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String firstName,

        @Schema(description = "Nuevo apellido", example = "Ramírez", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String lastName,

        @Schema(description = "Nuevo teléfono (opcional)", example = "+57 314 987 6543", nullable = true)
        String phone
) {
}
