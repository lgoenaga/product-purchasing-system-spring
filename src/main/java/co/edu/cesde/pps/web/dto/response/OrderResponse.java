package co.edu.cesde.pps.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Detalle de una orden de compra")
public record OrderResponse(
        @Schema(description = "ID único de la orden", example = "10")
        Long id,

        @Schema(description = "Número de orden legible (ej: ORD-20260429-00010)", example = "ORD-20260429-00010")
        String orderNumber,

        @Schema(description = "ID del usuario que realizó la orden", example = "1")
        Long userId,

        @Schema(description = "Email del usuario", example = "juan@example.com")
        String userEmail,

        @Schema(description = "Nombre completo del usuario", example = "Juan Pérez")
        String userFullName,

        @Schema(description = "Estado de la orden: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED", example = "PENDING")
        String status,

        @Schema(description = "Dirección de envío seleccionada")
        AddressResponse shippingAddress,

        @Schema(description = "Dirección de facturación seleccionada")
        AddressResponse billingAddress,

        @Schema(description = "Lista de productos incluidos en la orden")
        List<OrderItemResponse> items,

        @Schema(description = "Totales de la orden")
        OrderTotalsResponse totals,

        @Schema(description = "Fecha y hora de creación de la orden", example = "2026-04-29T11:00:00")
        LocalDateTime createdAt
) {
}
