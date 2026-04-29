package co.edu.cesde.pps.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Totales calculados del carrito de compras")
public record CartSummaryResponse(
        @Schema(description = "Número total de ítems en el carrito", example = "3")
        Integer itemsCount,

        @Schema(description = "Subtotal antes de impuestos y envío", example = "149700.00")
        BigDecimal subtotal,

        @Schema(description = "Monto de impuestos aplicados", example = "28443.00")
        BigDecimal tax,

        @Schema(description = "Costo de envío", example = "10000.00")
        BigDecimal shipping,

        @Schema(description = "Total final a pagar (subtotal + tax + shipping)", example = "188143.00")
        BigDecimal total
) {
}
