package co.edu.cesde.pps.web.dto.response;

import co.edu.cesde.pps.enums.AddressType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dirección de envío o facturación del usuario")
public record AddressResponse(
        @Schema(description = "ID único de la dirección", example = "1")
        Long id,

        @Schema(description = "ID del usuario propietario", example = "1")
        Long userId,

        @Schema(description = "Tipo de dirección: SHIPPING o BILLING", example = "SHIPPING")
        AddressType type,

        @Schema(description = "Línea principal de la dirección", example = "Cra 49 # 7 Sur - 50")
        String line1,

        @Schema(description = "Información adicional (apto, torre, etc.)", example = "Apto 301, Torre A", nullable = true)
        String line2,

        @Schema(description = "Ciudad", example = "Medellín")
        String city,

        @Schema(description = "Departamento / Estado / Provincia", example = "Antioquia")
        String state,

        @Schema(description = "País", example = "Colombia")
        String country,

        @Schema(description = "Código postal", example = "050021")
        String postalCode,

        @Schema(description = "true si es la dirección predeterminada para su tipo", example = "true")
        Boolean isDefault
) {
}
