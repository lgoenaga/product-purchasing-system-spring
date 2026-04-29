package co.edu.cesde.pps.web.controller;

import co.edu.cesde.pps.application.OrderApplicationService;
import co.edu.cesde.pps.web.dto.request.CheckoutRequest;
import co.edu.cesde.pps.web.dto.response.OrderResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Orders", description = "Órdenes de compra — requiere sessionToken de usuario autenticado")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping(ApiRoutes.ORDERS)
public class OrderController {

    private final OrderApplicationService orderApplicationService;
    private final CurrentSessionResolver currentSessionResolver;

    public OrderController(OrderApplicationService orderApplicationService,
                           CurrentSessionResolver currentSessionResolver) {
        this.orderApplicationService = orderApplicationService;
        this.currentSessionResolver = currentSessionResolver;
    }

    @Operation(
            summary = "Realizar checkout",
            description = """
                    Convierte un carrito en una orden de compra.
                    
                    Requisitos:
                    - El usuario debe estar autenticado (no aplica para invitados).
                    - El `cartId` debe corresponder a un carrito OPEN del usuario con al menos un ítem.
                    - Las direcciones `shippingAddressId` y `billingAddressId` deben pertenecer al usuario.
                    - Haber stock suficiente para todos los productos del carrito.
                    
                    Al completarse, el carrito pasa a estado CHECKED_OUT y se descuenta el stock.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Orden creada exitosamente",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, carrito vacío o sin stock", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Carrito o dirección no encontrada", content = @Content)
    })
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody CheckoutRequest request) {
        OrderResponse response = orderApplicationService.checkout(
                currentSessionResolver.resolveCurrentToken(authorizationHeader),
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Listar mis órdenes",
            description = "Devuelve todas las órdenes de compra realizadas por el usuario autenticado, ordenadas por fecha descendente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de órdenes del usuario",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content)
    })
    @GetMapping("/me")
    public List<OrderResponse> listMyOrders(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        return orderApplicationService.listMyOrders(currentSessionResolver.resolveCurrentToken(authorizationHeader));
    }

    @Operation(
            summary = "Obtener mi orden por ID",
            description = "Devuelve el detalle de una orden específica. Solo el propietario de la orden puede consultarla."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle de la orden",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada o no pertenece al usuario", content = @Content)
    })
    @GetMapping("/{id}")
    public OrderResponse getMyOrder(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Parameter(description = "ID de la orden", required = true, example = "10")
            @PathVariable Long id) {
        return orderApplicationService.getMyOrder(currentSessionResolver.resolveCurrentToken(authorizationHeader), id);
    }
}
