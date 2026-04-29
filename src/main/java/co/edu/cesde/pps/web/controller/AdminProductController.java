package co.edu.cesde.pps.web.controller;

import co.edu.cesde.pps.application.CatalogApplicationService;
import co.edu.cesde.pps.web.dto.request.ProductUpsertRequest;
import co.edu.cesde.pps.web.dto.response.ProductResponse;
import co.edu.cesde.pps.web.security.AdminAccessGuard;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin — Products", description = "Administración de productos — requiere sessionToken con rol ADMIN")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping(ApiRoutes.ADMIN_PRODUCTS)
public class AdminProductController {

    private final CatalogApplicationService catalogApplicationService;
    private final AdminAccessGuard adminAccessGuard;

    public AdminProductController(CatalogApplicationService catalogApplicationService,
                                  AdminAccessGuard adminAccessGuard) {
        this.catalogApplicationService = catalogApplicationService;
        this.adminAccessGuard = adminAccessGuard;
    }

    @Operation(
            summary = "Crear producto",
            description = """
                    Crea un nuevo producto en el catálogo. **Requiere rol ADMIN.**
                    
                    El `sku` debe ser único. La `categoryId` debe corresponder a una categoría existente.
                    Si `isActive` es `false`, el producto no aparecerá en el catálogo público.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o SKU duplicado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody ProductUpsertRequest request) {
        adminAccessGuard.requireAdmin(authorizationHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogApplicationService.createProduct(request));
    }

    @Operation(
            summary = "Actualizar producto",
            description = "Reemplaza todos los campos del producto. **Requiere rol ADMIN.** El `sku` debe seguir siendo único."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o SKU duplicado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Parameter(description = "ID del producto a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ProductUpsertRequest request) {
        adminAccessGuard.requireAdmin(authorizationHeader);
        return catalogApplicationService.updateProduct(id, request);
    }

    @Operation(
            summary = "Eliminar producto",
            description = """
                    Elimina (o desactiva) un producto del catálogo. **Requiere rol ADMIN.**
                    
                    Si el producto tiene órdenes asociadas, puede realizar una eliminación lógica
                    (marcarlo como inactivo) en lugar de físico.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Parameter(description = "ID del producto a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        adminAccessGuard.requireAdmin(authorizationHeader);
        catalogApplicationService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
