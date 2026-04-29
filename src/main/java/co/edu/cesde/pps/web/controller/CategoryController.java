package co.edu.cesde.pps.web.controller;

import co.edu.cesde.pps.application.CatalogApplicationService;
import co.edu.cesde.pps.web.dto.response.CategoryResponse;
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
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Categories", description = "Categorías del catálogo — consulta pública (no requiere token)")
@RestController
@RequestMapping(ApiRoutes.CATEGORIES)
public class CategoryController {

    private final CatalogApplicationService catalogApplicationService;

    public CategoryController(CatalogApplicationService catalogApplicationService) {
        this.catalogApplicationService = catalogApplicationService;
    }

    @Operation(
            summary = "Listar todas las categorías",
            description = "Retorna la lista plana de todas las categorías activas, sin jerarquía."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de categorías",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryResponse.class))))
    })
    @GetMapping
    public List<CategoryResponse> listCategories() {
        return catalogApplicationService.listCategories();
    }

    @Operation(
            summary = "Árbol de categorías",
            description = """
                    Retorna las categorías raíz (sin padre) con sus subcategorías anidadas.
                    
                    Útil para renderizar menús de navegación con múltiples niveles.
                    El campo `subcategories` de cada elemento puede contener más niveles de anidamiento.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Árbol jerárquico de categorías",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryResponse.class))))
    })
    @GetMapping("/tree")
    public List<CategoryResponse> listCategoryTree() {
        return catalogApplicationService.listCategoryTree();
    }

    @Operation(
            summary = "Obtener categoría por ID",
            description = "Retorna el detalle de una categoría específica incluyendo conteo de subcategorías y productos."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle de la categoría",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public CategoryResponse getCategory(
            @Parameter(description = "ID de la categoría", required = true, example = "1")
            @PathVariable Long id) {
        return catalogApplicationService.getCategory(id);
    }

    @Operation(
            summary = "Listar subcategorías de una categoría",
            description = "Retorna la lista de subcategorías directas (hijas) de la categoría indicada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de subcategorías",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Categoría padre no encontrada", content = @Content)
    })
    @GetMapping("/{id}/subcategories")
    public List<CategoryResponse> listSubcategories(
            @Parameter(description = "ID de la categoría padre", required = true, example = "1")
            @PathVariable Long id) {
        return catalogApplicationService.listSubcategories(id);
    }
}
