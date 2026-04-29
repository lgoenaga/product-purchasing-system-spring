package co.edu.cesde.pps.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Ítem a agregar al carrito")
public record AddCartItemRequest(
        @Schema(description = "ID del producto", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Long productId,

        @Schema(description = "Cantidad a agregar (mínimo 1)", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Positive
        Integer quantity
) {
}
