package co.edu.cesde.pps.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos para realizar el checkout del carrito")
public record CheckoutRequest(
        @Schema(description = "ID del carrito a convertir en orden (debe estar en estado OPEN con al menos un ítem)", example = "7", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Long cartId,

        @Schema(description = "ID de la dirección de envío del usuario", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Long shippingAddressId,

        @Schema(description = "ID de la dirección de facturación del usuario", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Long billingAddressId
) {
}
