package co.edu.cesde.pps.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Categoría del catálogo de productos")
public record CategoryResponse(
        @Schema(description = "ID único de la categoría", example = "2")
        Long id,

        @Schema(description = "ID de la categoría padre (null si es categoría raíz)", example = "1", nullable = true)
        Long parentId,

        @Schema(description = "Nombre de la categoría padre (null si es raíz)", example = "Ropa", nullable = true)
        String parentName,

        @Schema(description = "Nombre de la categoría", example = "Ropa Deportiva")
        String name,

        @Schema(description = "Slug URL-friendly de la categoría", example = "ropa-deportiva")
        String slug,

        @Schema(description = "true si es una categoría raíz (sin padre)", example = "false")
        Boolean isRoot,

        @Schema(description = "Número de subcategorías directas", example = "3")
        Integer subcategoriesCount,

        @Schema(description = "Número de productos asignados a esta categoría", example = "25")
        Integer productsCount,

        @Schema(description = "Lista de subcategorías directas (solo en respuesta de árbol)", nullable = true)
        List<CategoryResponse> subcategories
) {
}
