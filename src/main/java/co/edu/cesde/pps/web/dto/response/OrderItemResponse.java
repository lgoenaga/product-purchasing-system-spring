package co.edu.cesde.pps.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Ítem individual dentro de una orden de compra")
public record OrderItemResponse(
        @Schema(description = "ID del ítem de orden", example = "301")
        Long id,

        @Schema(description = "ID del producto", example = "5")
        Long productId,

        @Schema(description = "SKU del producto al momento de la compra", example = "PROD-001-AZL")
        String sku,

        @Schema(description = "Nombre del producto al momento de la compra", example = "Camiseta Azul Talla M")
        String productName,

        @Schema(description = "URL de la imagen del producto", example = "https://cdn.example.com/img/camiseta-azul.jpg", nullable = true)
        String image,

        @Schema(description = "Cantidad comprada", example = "2")
        Integer quantity,

        @Schema(description = "Precio unitario al momento de la compra", example = "49900.00")
        BigDecimal unitPrice,

        @Schema(description = "Subtotal del ítem (unitPrice × quantity)", example = "99800.00")
        BigDecimal lineTotal
) {
}
