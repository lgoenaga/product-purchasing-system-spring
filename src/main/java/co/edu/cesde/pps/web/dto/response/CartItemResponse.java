package co.edu.cesde.pps.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Ítem individual dentro del carrito de compras")
public record CartItemResponse(
        @Schema(description = "ID del ítem en el carrito", example = "201")
        Long id,

        @Schema(description = "ID del producto", example = "5")
        Long productId,

        @Schema(description = "SKU del producto", example = "PROD-001-AZL")
        String sku,

        @Schema(description = "Nombre del producto", example = "Camiseta Azul Talla M")
        String name,

        @Schema(description = "URL de la imagen del producto", example = "https://cdn.example.com/img/camiseta-azul.jpg", nullable = true)
        String image,

        @Schema(description = "Cantidad del producto en el carrito", example = "2")
        Integer quantity,

        @Schema(description = "Precio unitario al momento de agregar al carrito", example = "49900.00")
        BigDecimal unitPrice,

        @Schema(description = "Subtotal del ítem (unitPrice × quantity)", example = "99800.00")
        BigDecimal lineTotal,

        @Schema(description = "true si el producto sigue disponible (activo y en stock)", example = "true")
        Boolean productAvailable,

        @Schema(description = "Stock actual del producto", example = "148")
        Integer productStock,

        @Schema(description = "Fecha en que se agregó el ítem al carrito", example = "2026-04-29T10:05:00")
        LocalDateTime addedAt
) {
}
