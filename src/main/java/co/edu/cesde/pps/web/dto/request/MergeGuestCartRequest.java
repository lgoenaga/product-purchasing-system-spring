package co.edu.cesde.pps.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos para fusionar el carrito de invitado al carrito del usuario autenticado")
public record MergeGuestCartRequest(
        @Schema(description = "ID del carrito de invitado (campo `id` del carrito devuelto durante la sesión guest)", example = "15", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Long guestCartId
) {
}
