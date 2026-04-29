package co.edu.cesde.pps.web.controller;

import co.edu.cesde.pps.application.CartApplicationService;
import co.edu.cesde.pps.web.dto.request.AddCartItemRequest;
import co.edu.cesde.pps.web.dto.request.MergeGuestCartRequest;
import co.edu.cesde.pps.web.dto.request.UpdateCartItemQuantityRequest;
import co.edu.cesde.pps.web.dto.response.CartResponse;
import co.edu.cesde.pps.web.security.CurrentSessionResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cart", description = "Carrito de compras — requiere sessionToken (guest o autenticado)")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping(ApiRoutes.CART)
public class CartController {

    private final CartApplicationService cartApplicationService;
    private final CurrentSessionResolver currentSessionResolver;

    public CartController(CartApplicationService cartApplicationService,
                          CurrentSessionResolver currentSessionResolver) {
        this.cartApplicationService = cartApplicationService;
        this.currentSessionResolver = currentSessionResolver;
    }

    @Operation(
            summary = "Ver mi carrito",
            description = "Devuelve el carrito activo asociado al sessionToken. Incluye items, cantidades y totales."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrito activo del usuario o invitado",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content)
    })
    @GetMapping("/me")
    public CartResponse getCurrentCart(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        return cartApplicationService.getCurrentCart(currentSessionResolver.resolveCurrentToken(authorizationHeader));
    }

    @Operation(
            summary = "Agregar ítem al carrito",
            description = """
                    Agrega un producto al carrito o incrementa su cantidad si ya existe.
                    
                    Valida que el producto exista, esté activo y tenga stock suficiente.
                    Devuelve el carrito completo actualizado.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrito actualizado con el nuevo ítem",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o stock insuficiente", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    })
    @PostMapping("/items")
    public CartResponse addItem(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody AddCartItemRequest request) {
        return cartApplicationService.addItem(currentSessionResolver.resolveCurrentToken(authorizationHeader), request);
    }

    @Operation(
            summary = "Actualizar cantidad de un ítem",
            description = "Cambia la cantidad de un producto específico en el carrito. La cantidad debe ser >= 1."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrito actualizado",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "400", description = "Cantidad inválida o stock insuficiente", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado en el carrito", content = @Content)
    })
    @PatchMapping("/items/{productId}")
    public CartResponse updateItemQuantity(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Parameter(description = "ID del producto a actualizar", required = true, example = "5")
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemQuantityRequest request) {
        return cartApplicationService.updateItemQuantity(
                currentSessionResolver.resolveCurrentToken(authorizationHeader),
                productId,
                request
        );
    }

    @Operation(
            summary = "Eliminar ítem del carrito",
            description = "Elimina completamente un producto del carrito, independientemente de la cantidad."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrito actualizado sin el ítem eliminado",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado en el carrito", content = @Content)
    })
    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Parameter(description = "ID del producto a eliminar", required = true, example = "5")
            @PathVariable Long productId) {
        return cartApplicationService.removeItem(currentSessionResolver.resolveCurrentToken(authorizationHeader), productId);
    }

    @Operation(
            summary = "Vaciar carrito",
            description = "Elimina todos los ítems del carrito dejándolo vacío. No elimina el carrito en sí."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Carrito vaciado exitosamente", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content)
    })
    @DeleteMapping("/items")
    public ResponseEntity<Void> clearCurrentCart(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        cartApplicationService.clearCurrentCart(currentSessionResolver.resolveCurrentToken(authorizationHeader));
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Fusionar carrito de invitado",
            description = """
                    Fusiona los ítems del carrito de invitado al carrito del usuario autenticado.
                    
                    Usar después del login o registro si el usuario agregó productos como invitado.
                    El `guestCartId` es el `id` del carrito devuelto por `GET /api/v1/cart/me`
                    durante la sesión de invitado.
                    
                    Los ítems existentes en el carrito del usuario se respetan; los ítems del
                    carrito invitado se suman o agregan.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrito fusionado exitosamente",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "400", description = "El carrito invitado no existe o ya fue fusionado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content)
    })
    @PostMapping("/merge")
    public CartResponse mergeGuestCart(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody MergeGuestCartRequest request) {
        return cartApplicationService.mergeGuestCart(currentSessionResolver.resolveCurrentToken(authorizationHeader), request);
    }
}
