package co.edu.cesde.pps.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Nueva cantidad para un ítem del carrito")
public record UpdateCartItemQuantityRequest(
        @Schema(description = "Nueva cantidad del producto (mínimo 1)", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Positive
        Integer quantity
) {
}
