package co.edu.cesde.pps.web.dto.request;

import co.edu.cesde.pps.enums.AddressType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos para crear o actualizar una dirección")
public record AddressUpsertRequest(
        @Schema(description = "Tipo de dirección: SHIPPING (envío) o BILLING (facturación)", example = "SHIPPING", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        AddressType type,

        @Schema(description = "Línea principal de la dirección (calle, número)", example = "Cra 49 # 7 Sur - 50", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String line1,

        @Schema(description = "Información adicional (apto, torre, oficina — opcional)", example = "Apto 301, Torre A", nullable = true)
        String line2,

        @Schema(description = "Ciudad", example = "Medellín", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String city,

        @Schema(description = "Departamento / Estado / Provincia", example = "Antioquia", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String state,

        @Schema(description = "País (código ISO o nombre)", example = "Colombia", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String country,

        @Schema(description = "Código postal", example = "050021", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String postalCode,

        @Schema(description = "Si true, esta dirección se convierte en la predeterminada para su tipo", example = "true", nullable = true)
        Boolean isDefault
) {
}
