package co.edu.cesde.pps.web.controller;

import co.edu.cesde.pps.application.CatalogApplicationService;
import co.edu.cesde.pps.exception.EntityNotFoundException;
import co.edu.cesde.pps.web.dto.response.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Products", description = "Catálogo de productos — consulta pública (no requiere token)")
@RestController
@RequestMapping(ApiRoutes.PRODUCTS)
public class ProductController {

    private final CatalogApplicationService catalogApplicationService;

    public ProductController(CatalogApplicationService catalogApplicationService) {
        this.catalogApplicationService = catalogApplicationService;
    }

    @Operation(
            summary = "Listar productos",
            description = """
                    Devuelve la lista de productos activos del catálogo.
                    
                    Parámetros opcionales de filtrado:
                    - `search` — busca por nombre o SKU (búsqueda parcial).
                    - `categoryId` — filtra por categoría específica.
                    - `activeOnly` — si `true` (por defecto), solo devuelve productos activos
                      y con stock disponible.
                    
                    Los parámetros `search` y `categoryId` son mutuamente excluyentes;
                    `search` tiene prioridad si ambos están presentes.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de productos",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))))
    })
    @GetMapping
    public List<ProductResponse> listProducts(
            @Parameter(description = "Texto de búsqueda (nombre o SKU, parcial)")
            @RequestParam(required = false) String search,
            @Parameter(description = "ID de categoría para filtrar")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Si es true (defecto), retorna solo productos activos con stock")
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        List<ProductResponse> products = resolveBaseProducts(search, categoryId);

        return products.stream()
                .filter(product -> categoryId == null || categoryId.equals(product.categoryId()))
                .filter(product -> !activeOnly || Boolean.TRUE.equals(product.isActive()))
                .toList();
    }

    @Operation(
            summary = "Obtener producto por ID",
            description = "Retorna el detalle de un producto activo. Devuelve 404 si el producto no existe o está inactivo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle del producto",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado o inactivo", content = @Content)
    })
    @GetMapping("/{id}")
    public ProductResponse getProduct(
            @Parameter(description = "ID del producto", required = true, example = "1")
            @PathVariable Long id) {
        ProductResponse response = catalogApplicationService.getProduct(id);
        if (!Boolean.TRUE.equals(response.isActive())) {
            throw new EntityNotFoundException("Product", id);
        }
        return response;
    }

    private List<ProductResponse> resolveBaseProducts(String search, Long categoryId) {
        if (search != null && !search.isBlank()) {
            return catalogApplicationService.searchProducts(search);
        }
        if (categoryId != null) {
            return catalogApplicationService.listProductsByCategory(categoryId);
        }
        return catalogApplicationService.listProducts(false);
    }
}
