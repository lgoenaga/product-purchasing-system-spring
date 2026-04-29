package co.edu.cesde.pps.web.controller;

import co.edu.cesde.pps.application.AddressApplicationService;
import co.edu.cesde.pps.web.dto.request.AddressUpsertRequest;
import co.edu.cesde.pps.web.dto.response.AddressResponse;
import co.edu.cesde.pps.web.security.CurrentSessionResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Addresses", description = "Direcciones del usuario autenticado — envío y facturación")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping(ApiRoutes.USER_ADDRESSES)
public class AddressController {

    private final AddressApplicationService addressApplicationService;
    private final CurrentSessionResolver currentSessionResolver;

    public AddressController(AddressApplicationService addressApplicationService,
                             CurrentSessionResolver currentSessionResolver) {
        this.addressApplicationService = addressApplicationService;
        this.currentSessionResolver = currentSessionResolver;
    }

    @Operation(
            summary = "Listar mis direcciones",
            description = "Devuelve todas las direcciones registradas por el usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de direcciones del usuario",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AddressResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content)
    })
    @GetMapping
    public List<AddressResponse> listMyAddresses(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        return addressApplicationService.listMyAddresses(currentSessionResolver.resolveCurrentToken(authorizationHeader));
    }

    @Operation(
            summary = "Obtener dirección por ID",
            description = "Devuelve el detalle de una dirección específica del usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle de la dirección",
                    content = @Content(schema = @Schema(implementation = AddressResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dirección no encontrada o no pertenece al usuario", content = @Content)
    })
    @GetMapping("/{id}")
    public AddressResponse getMyAddress(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Parameter(description = "ID de la dirección", required = true, example = "1")
            @PathVariable Long id) {
        return addressApplicationService.getMyAddress(currentSessionResolver.resolveCurrentToken(authorizationHeader), id);
    }

    @Operation(
            summary = "Agregar dirección",
            description = """
                    Crea una nueva dirección para el usuario autenticado.
                    
                    El campo `type` acepta los valores `SHIPPING` (envío) o `BILLING` (facturación).
                    Si `isDefault` es `true`, esta dirección pasa a ser la predeterminada para
                    su tipo y se quita el flag de la dirección anterior.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Dirección creada exitosamente",
                    content = @Content(schema = @Schema(implementation = AddressResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content)
    })
    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody AddressUpsertRequest request) {
        AddressResponse response = addressApplicationService.addAddress(
                currentSessionResolver.resolveCurrentToken(authorizationHeader), request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Actualizar dirección",
            description = "Reemplaza todos los campos de la dirección indicada. Los campos no enviados quedan en null."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dirección actualizada",
                    content = @Content(schema = @Schema(implementation = AddressResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dirección no encontrada o no pertenece al usuario", content = @Content)
    })
    @PutMapping("/{id}")
    public AddressResponse updateAddress(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Parameter(description = "ID de la dirección a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody AddressUpsertRequest request) {
        return addressApplicationService.updateAddress(currentSessionResolver.resolveCurrentToken(authorizationHeader), id, request);
    }

    @Operation(
            summary = "Establecer dirección predeterminada",
            description = "Marca la dirección indicada como predeterminada para su tipo (SHIPPING o BILLING). Quita el flag de la anterior."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dirección marcada como predeterminada",
                    content = @Content(schema = @Schema(implementation = AddressResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dirección no encontrada o no pertenece al usuario", content = @Content)
    })
    @PatchMapping("/{id}/default")
    public AddressResponse setDefaultAddress(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Parameter(description = "ID de la dirección a marcar como predeterminada", required = true, example = "1")
            @PathVariable Long id) {
        return addressApplicationService.setDefaultAddress(currentSessionResolver.resolveCurrentToken(authorizationHeader), id);
    }

    @Operation(
            summary = "Eliminar dirección",
            description = "Elimina la dirección del usuario. No se puede eliminar una dirección que esté referenciada en una orden existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Dirección eliminada exitosamente", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dirección no encontrada o no pertenece al usuario", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Parameter(description = "ID de la dirección a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        addressApplicationService.deleteAddress(currentSessionResolver.resolveCurrentToken(authorizationHeader), id);
        return ResponseEntity.noContent().build();
    }
}
