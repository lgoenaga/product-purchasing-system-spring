# Springdoc / Swagger básico con `ProductController`

> Guía paso a paso para iniciar la documentación del API en Spring Boot de la forma más sencilla posible.

---

## Objetivo

Implementar y explicar Swagger de forma básica usando un caso real del proyecto:

- instalar Springdoc en un proyecto Spring Boot
- entender qué aparece en Swagger UI sin casi configuración
- agregar anotaciones básicas sobre `ProductController`
- ver cómo mejora visualmente la documentación en Swagger UI
- usar esto como base para documentar otros controllers después

---

## Punto de partida

Este documento parte de que el proyecto ya tiene:

- Spring Boot 3
- endpoints funcionando
- `ProductController` creado
- la dependencia de Swagger agregada en `pom.xml`

Endpoints de ejemplo:

- `GET /api/v1/products`
- `GET /api/v1/products/{id}`

---

# Paso 1 — Instalar Swagger

## Archivo a modificar

- `pom.xml`

## Dependencia a usar

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.8</version>
</dependency>
```

## ¿Qué hace esta dependencia?

Esta librería se encarga de:

- leer los `@RestController`
- detectar rutas como `@GetMapping`, `@PostMapping`, `@PutMapping`, etc.
- construir el contrato OpenAPI automáticamente
- mostrar una interfaz visual llamada **Swagger UI**

## Scope de la dependencia

No necesita `scope`.

Eso significa que queda con el scope por defecto: **compile**.

---

# Paso 2 — Recargar Maven y reiniciar la aplicación

Después de agregar la dependencia:

1. recargar Maven
2. reiniciar la aplicación

## Comandos sugeridos

```bash
mvn -q -DskipTests compile
mvn spring-boot:run
```

## URLs para verificar

```text
http://localhost:8081/swagger-ui/index.html
http://localhost:8081/swagger-ui.html
http://localhost:8081/v3/api-docs
```

> Si el proyecto está corriendo en otro puerto, reemplazar `8081` por el puerto configurado.

---

# Paso 3 — Ver Swagger antes de agregar anotaciones

## ¿Qué pasa si solo instalamos la dependencia?

Aunque todavía no agreguemos anotaciones OpenAPI, Swagger ya puede mostrar:

- los endpoints detectados
- el verbo HTTP (`GET`, `POST`, etc.)
- la ruta de cada endpoint
- el tipo general de respuesta

## ¿Qué limitaciones tiene esta primera versión?

La UI todavía se ve muy técnica porque:

- el grupo del controller puede salir con un nombre poco amigable
- no hay descripciones claras
- no hay explicaciones de parámetros
- no hay documentación detallada del JSON
- los errores HTTP no quedan bien explicados

---

# Paso 4 — Primeras anotaciones sobre `ProductController`

## Archivo a modificar

- `src/main/java/co/edu/cesde/pps/web/controller/ProductController.java`

## Objetivo de esta fase

Agregar las anotaciones más básicas y visibles para que los estudiantes vean rápidamente cómo mejora Swagger UI.

Las anotaciones usadas son:

- `@Tag`
- `@Operation`
- `@ApiResponses`
- `@ApiResponse`
- `@Parameter`
- `@Content`
- `@Schema`
- `@ArraySchema`

---

# Paso 5 — `@Tag`

## Ejemplo

```java
@Tag(
    name = "Productos",
    description = "Endpoints públicos para consultar el catálogo de productos"
)
```

## Import

```java
import io.swagger.v3.oas.annotations.tags.Tag;
```

## ¿Qué hace?

`@Tag` agrupa los endpoints del controller bajo un nombre amigable dentro de Swagger UI.

## Antes

Swagger puede mostrar algo técnico como:

- `product-controller`

## Después

Swagger muestra algo más claro:

- `Productos`

## Cambio visible en Swagger UI

La documentación se organiza mejor y el grupo del controller queda alineado con el lenguaje del negocio.

---

# Paso 6 — `@Operation`

## Ejemplo en `listProducts()`

```java
@Operation(
    summary = "Listar productos activos",
    description = "Retorna todos los productos activos disponibles en el catálogo"
)
```

## Import

```java
import io.swagger.v3.oas.annotations.Operation;
```

## ¿Qué hace?

`@Operation` documenta un endpoint específico.

### `summary`

Es el título corto del endpoint.

### `description`

Es la explicación más detallada de lo que hace el endpoint.

## Cambio visible en Swagger UI

Antes solo se veía la ruta.

Después también se ve:

- un nombre entendible
- una descripción funcional del endpoint

Esto hace que la UI sea mucho más útil para frontend, QA o para la misma clase.

---

# Paso 7 — `@ApiResponses` y `@ApiResponse`

## Ejemplo en `getProduct()`

```java
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Producto encontrado"
    ),
    @ApiResponse(
        responseCode = "404",
        description = "Producto no encontrado"
    ),
    @ApiResponse(
        responseCode = "500",
        description = "Error interno del servidor"
    )
})
```

## Imports

```java
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
```

## ¿Qué hace cada una?

### `@ApiResponses`

Agrupa todas las respuestas HTTP documentadas de un método.

### `@ApiResponse`

Describe una respuesta específica.

Por ejemplo:

- `200` → éxito
- `404` → recurso no encontrado
- `500` → error interno del servidor

## Cambio visible en Swagger UI

Swagger empieza a mostrar claramente qué respuestas puede devolver el endpoint.

Esto ayuda a explicar:

- qué pasa si todo sale bien
- qué pasa si el ID no existe
- qué pasa si ocurre un error inesperado

---

# Paso 8 — `@Parameter`

## Ejemplo

```java
public ResponseEntity<ProductResponse> getProduct(
        @Parameter(description = "ID del producto a consultar", example = "1")
        @PathVariable Long id) {
    ...
}
```

## Import

```java
import io.swagger.v3.oas.annotations.Parameter;
```

## ¿Qué hace?

`@Parameter` documenta parámetros de entrada.

En este caso documenta el `id` de la URL.

## Cambio visible en Swagger UI

Antes solo aparecía un parámetro llamado `id`.

Después Swagger muestra:

- qué representa ese `id`
- un valor de ejemplo
- mayor claridad al usar **Try it out**

---

# Paso 9 — `@Content`, `@Schema` y `@ArraySchema`

Estas anotaciones ayudan a explicar mejor el cuerpo de la respuesta.

---

## 9.1 — `@Content`

### Ejemplo

```java
content = @Content(
    mediaType = "application/json",
    schema = @Schema(implementation = ProductResponse.class)
)
```

## ¿Qué hace?

Le dice a Swagger:

- qué tipo de contenido devuelve el endpoint
- cuál es el modelo Java que representa ese JSON

---

## 9.2 — `@Schema`

### Ejemplo simple en una respuesta

```java
schema = @Schema(implementation = ProductResponse.class)
```

## Import

```java
import io.swagger.v3.oas.annotations.media.Schema;
```

## ¿Qué hace?

`@Schema` describe un modelo o un campo del modelo.

En este caso le dice a Swagger que la respuesta usa la clase `ProductResponse`.

## Cambio visible en Swagger UI

Swagger puede mostrar mejor la estructura del JSON devuelto.

---

## 9.3 — `@ArraySchema`

### Ejemplo en `listProducts()`

```java
array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))
```

## Import

```java
import io.swagger.v3.oas.annotations.media.ArraySchema;
```

## ¿Qué hace?

`@ArraySchema` se usa cuando la respuesta no es un objeto único sino una **lista**.

En este caso:

- `GET /api/v1/products` retorna `List<ProductResponse>`
- por eso Swagger debe entender que la respuesta es un arreglo JSON

## Cambio visible en Swagger UI

Swagger ya no muestra solo “algo genérico”, sino que indica correctamente que la respuesta es una lista de productos.

---

# Paso 10 — Ejemplo completo del controller documentado

```java
package co.edu.cesde.pps.web.controller;

import co.edu.cesde.pps.application.CatalogApplicationService;
import co.edu.cesde.pps.exception.EntityNotFoundException;
import co.edu.cesde.pps.web.dto.response.ProductResponse;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Productos",
        description = "Endpoints públicos para consultar el catálogo de productos"
)
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final CatalogApplicationService catalogApplicationService;

    public ProductController(CatalogApplicationService catalogApplicationService) {
        this.catalogApplicationService = catalogApplicationService;
    }

    @Operation(
            summary = "Listar productos activos",
            description = "Retorna todos los productos activos disponibles en el catálogo"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de productos obtenida correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content
            )
    })
    @GetMapping
    public ResponseEntity<List<ProductResponse>> listProducts() {
        try {
            List<ProductResponse> products = catalogApplicationService.listProducts(true);
            return ResponseEntity.ok(products);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @Operation(
            summary = "Obtener producto por ID",
            description = "Retorna el detalle de un producto específico. Si no existe, responde 404"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "ID del producto a consultar", example = "1")
            @PathVariable Long id) {
        try {
            ProductResponse product = catalogApplicationService.getProduct(id);

            return ResponseEntity.ok(product);

        } catch (EntityNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}
```

---

# Paso 11 — ¿Qué se ve mejor en Swagger UI después de estas anotaciones?

## Sin anotaciones

Swagger muestra información mínima:

- rutas
- verbos HTTP
- nombres técnicos

## Con anotaciones básicas

Swagger empieza a mostrar:

- grupo `Productos`
- títulos claros por endpoint
- descripción de cada operación
- parámetros explicados
- respuestas `200`, `404`, `500`
- mejor interpretación del tipo de respuesta

---

# Paso 12 — Resumen rápido de cada anotación

| Anotación | ¿Dónde se usa? | ¿Para qué sirve? |
|---|---|---|
| `@Tag` | clase | agrupa endpoints con nombre amigable |
| `@Operation` | método | pone título y descripción del endpoint |
| `@ApiResponses` | método | agrupa respuestas HTTP documentadas |
| `@ApiResponse` | método | describe una respuesta específica |
| `@Parameter` | parámetro | documenta entradas como `id` |
| `@Content` | respuesta | define el tipo de contenido devuelto |
| `@Schema` | respuesta/modelo/campo | describe un objeto o propiedad |
| `@ArraySchema` | respuesta | documenta listas o arreglos JSON |

---

# Paso 13 — Qué sigue después de esta versión básica

Una vez entendido este primer nivel, los siguientes pasos recomendados son:

1. documentar `ProductResponse` con `@Schema` en cada campo
2. documentar también `CategoryController`
3. agregar documentación de request DTOs
4. documentar respuestas de error con body real
5. agregar configuración global del API (`OpenApiConfig`)
6. documentar endpoints protegidos con autenticación

---

# Verificación final

## Compilar

```bash
mvn -q -DskipTests compile
```

## Ejecutar

```bash
mvn spring-boot:run
```

## Abrir Swagger UI

```text
http://localhost:8081/swagger-ui/index.html
```

o

```text
http://localhost:8081/swagger-ui.html
```

## Revisar visualmente

Verificar que en la UI:

- aparece el grupo `Productos`
- `GET /api/v1/products` tiene título y descripción
- `GET /api/v1/products/{id}` muestra parámetro `id` con ejemplo
- aparecen respuestas `200`, `404` y `500`
- Swagger interpreta correctamente que un endpoint devuelve una lista y el otro un objeto

---

**Estado esperado:** Swagger básico instalado y primeras anotaciones aplicadas sobre `ProductController`.  
**Fecha:** 29 de abril de 2026

