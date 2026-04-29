package co.edu.cesde.pps.web.dto.response;

import co.edu.cesde.pps.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Datos públicos de un usuario")
public record UserResponse(
        @Schema(description = "ID único del usuario", example = "1")
        Long id,

        @Schema(description = "Email del usuario", example = "juan@example.com")
        String email,

        @Schema(description = "Nombre", example = "Juan")
        String firstName,

        @Schema(description = "Apellido", example = "Pérez")
        String lastName,

        @Schema(description = "Nombre completo (firstName + lastName)", example = "Juan Pérez")
        String fullName,

        @Schema(description = "Rol asignado: ADMIN o CUSTOMER", example = "CUSTOMER")
        String role,

        @Schema(description = "Teléfono de contacto", example = "+57 300 123 4567", nullable = true)
        String phone,

        @Schema(description = "Estado de la cuenta: ACTIVE o INACTIVE", example = "ACTIVE")
        UserStatus status,

        @Schema(description = "Fecha de creación del usuario", example = "2026-04-29T10:30:00")
        LocalDateTime createdAt
) {
}
