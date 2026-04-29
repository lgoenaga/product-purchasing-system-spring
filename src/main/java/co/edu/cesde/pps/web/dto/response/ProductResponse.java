package co.edu.cesde.pps.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Detalle de un producto del catálogo")
public record ProductResponse(
        @Schema(description = "ID único del producto", example = "5")
        Long id,

        @Schema(description = "ID de la categoría a la que pertenece", example = "3")
        Long categoryId,

        @Schema(description = "Nombre de la categoría", example = "Ropa Deportiva")
        String categoryName,

        @Schema(description = "Código SKU único del producto", example = "PROD-001-AZL")
        String sku,

        @Schema(description = "Nombre del producto", example = "Camiseta Azul Talla M")
        String name,

        @Schema(description = "Descripción del producto", example = "Camiseta 100% algodón, color azul marino", nullable = true)
        String description,

        @Schema(description = "URL de la imagen del producto", example = "https://cdn.example.com/img/camiseta-azul.jpg", nullable = true)
        String image,

        @Schema(description = "Precio de venta al público", example = "49900.00")
        BigDecimal price,

        @Schema(description = "Cantidad disponible en stock", example = "150")
        Integer stockQty,

        @Schema(description = "Si false, el producto está oculto en el catálogo público", example = "true")
        Boolean isActive,

        @Schema(description = "true si el producto está activo y tiene stock > 0", example = "true")
        Boolean isAvailable,

        @Schema(description = "Fecha de creación del producto", example = "2026-04-01T09:00:00")
        LocalDateTime createdAt
) {
}
