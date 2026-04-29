package co.edu.cesde.pps.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Carrito de compras activo")
public record CartResponse(
        @Schema(description = "ID único del carrito", example = "42")
        Long id,

        @Schema(description = "ID del usuario propietario (null para carritos de invitado)", example = "1", nullable = true)
        Long userId,

        @Schema(description = "Email del usuario propietario (null para invitados)", example = "juan@example.com", nullable = true)
        String userEmail,

        @Schema(description = "Estado del carrito: OPEN, CHECKED_OUT o ABANDONED", example = "OPEN")
        String status,

        @Schema(description = "true si el carrito pertenece a una sesión de invitado", example = "false")
        Boolean isGuest,

        @Schema(description = "Fecha de creación del carrito", example = "2026-04-29T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "Fecha de última actualización del carrito", example = "2026-04-29T10:45:00")
        LocalDateTime updatedAt,

        @Schema(description = "Lista de ítems en el carrito")
        List<CartItemResponse> items,

        @Schema(description = "Resumen de totales del carrito")
        CartSummaryResponse summary
) {
}
