package co.edu.cesde.pps.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Datos para crear o actualizar una categoría")
public record CategoryUpsertRequest(
        @Schema(description = "ID de la categoría padre (null para categoría raíz)", example = "1", nullable = true)
        Long parentId,

        @Schema(description = "Nombre de la categoría", example = "Ropa Deportiva", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String name,

        @Schema(description = "Slug URL-friendly (opcional, se genera automáticamente si no se envía)", example = "ropa-deportiva", nullable = true)
        String slug
) {
}
