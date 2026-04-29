package co.edu.cesde.pps.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear o actualizar un producto")
public record ProductUpsertRequest(
        @Schema(description = "ID de la categoría a la que pertenece el producto", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Long categoryId,

        @Schema(description = "Código SKU único del producto", example = "PROD-001-AZL", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String sku,

        @Schema(description = "Nombre del producto", example = "Camiseta Azul Talla M", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String name,

        @Schema(description = "Descripción detallada del producto", example = "Camiseta 100% algodón, color azul marino", nullable = true)
        String description,

        @Schema(description = "URL de la imagen del producto (máx. 1000 caracteres)", example = "https://cdn.example.com/img/camiseta-azul.jpg", nullable = true)
        @Size(max = 1000)
        String image,

        @Schema(description = "Precio de venta al público (>= 0)", example = "49900.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @PositiveOrZero
        BigDecimal price,

        @Schema(description = "Cantidad en stock (>= 0)", example = "150", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @PositiveOrZero
        Integer stockQty,

        @Schema(description = "Si false, el producto no aparece en el catálogo público", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Boolean isActive
) {
}
