package co.edu.cesde.pps.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Sesión activa devuelta tras login, registro o creación de sesión de invitado")
public record AuthSessionResponse(
        @Schema(description = "Token de sesión opaco — incluir en header: `Authorization: Bearer <sessionToken>`", example = "a3f9b2c1-4d7e-4891-b843-2e1f5a234cd8")
        String sessionToken,

        @Schema(description = "ID único de la sesión en la base de datos", example = "101")
        Long sessionId,

        @Schema(description = "Fecha y hora de expiración de la sesión", example = "2026-05-29T22:00:00")
        LocalDateTime expiresAt,

        @Schema(description = "Datos del usuario autenticado (null para sesiones de invitado)", nullable = true)
        UserResponse user,

        @Schema(description = "Carrito activo asociado a la sesión")
        CartResponse cart
) {
}
