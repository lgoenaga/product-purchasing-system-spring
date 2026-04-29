package co.edu.cesde.pps.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Totales calculados de la orden de compra")
public record OrderTotalsResponse(
        @Schema(description = "Subtotal antes de impuestos y envío", example = "99800.00")
        BigDecimal subtotal,

        @Schema(description = "Monto de impuestos aplicados", example = "18962.00")
        BigDecimal tax,

        @Schema(description = "Costo de envío", example = "10000.00")
        BigDecimal shipping,

        @Schema(description = "Total final de la orden (subtotal + tax + shipping)", example = "128762.00")
        BigDecimal total
) {
}
