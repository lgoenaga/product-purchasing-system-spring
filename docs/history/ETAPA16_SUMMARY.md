# ETAPA 16 - Soporte de imágenes en productos

## Objetivo

Agregar soporte backend real para `image` en productos, de forma que frontend pueda consumir y administrar la imagen principal del producto desde el contrato oficial del API, sin depender de normalizaciones temporales en cliente ni de cargas manuales directas sobre la base de datos.

La meta de `etapa16` es cerrar este alcance de punta a punta:

- persistencia
- requests y responses
- catálogo público
- CRUD administrativo de productos
- carrito y responses auth con carrito
- respuestas de órdenes y checkout
- seed demo
- documentación
- pruebas

---

## Qué implementa etapa16

### 1. Campo `image` persistido en productos
Se agrega el atributo `image` al modelo `Product` y a la definición SQL de referencia.

Regla aplicada:

- `image` es opcional
- si llega vacío o en blanco, se normaliza a `null`
- si viene informado, se persiste como URL de imagen principal del producto

### 2. Contrato público actualizado para catálogo
Los endpoints públicos ahora devuelven `image` en el shape de producto:

- `GET /api/v1/products`
- `GET /api/v1/products/{id}`

Esto permite que frontend renderice tarjetas, listados y detalle de producto con una imagen oficial servida por el backend.

### 3. Contrato admin actualizado para productos
Los endpoints administrativos ahora aceptan y devuelven `image`:

- `POST /api/v1/admin/products`
- `PUT /api/v1/admin/products/{id}`

Con esto, el dashboard admin ya puede:

- crear productos con imagen
- editar o reemplazar la imagen principal
- recuperar el valor persistido luego de crear o actualizar

### 4. Imagen disponible en carrito y auth
Las respuestas que exponen `CartResponse` ahora devuelven `image` dentro de cada item del carrito:

- `GET /api/v1/cart/me`
- `POST /api/v1/cart/items`
- `PATCH /api/v1/cart/items/{productId}`
- `DELETE /api/v1/cart/items/{productId}`
- `POST /api/v1/cart/merge`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/guest-session`

Con esto, frontend puede renderizar correctamente carrito, mini-cart y los estados autenticados iniciales sin consultas adicionales al catálogo para resolver imágenes.

### 5. Imagen disponible en checkout e historial de órdenes
Los endpoints de órdenes ahora devuelven `image` dentro de cada item:

- `POST /api/v1/orders/checkout`
- `GET /api/v1/orders/me`
- `GET /api/v1/orders/{id}`

Con esto, frontend puede renderizar `order-confirmation`, historial y detalle de compra sin consultas adicionales al catálogo para resolver la imagen del producto.

### 6. Mapeo completo entre capas
`image` queda propagado en:

- entidad JPA
- DTO de servicio
- request DTO web
- response DTO web
- mapper entity <-> DTO
- mapper web request/response

Esto evita inconsistencias entre persistencia, lógica de negocio y contrato HTTP.

### 7. Seed demo con URLs reales
El perfil `demo` ahora carga productos con imágenes públicas reales para facilitar:

- validación visual desde frontend
- demos funcionales
- smoke tests manuales
- revisión de catálogo sin datos vacíos
- validación visual del carrito y auth bootstrap
- validación visual de order-confirmation e historial

### 8. Pruebas de integración ajustadas
Se amplían pruebas para cubrir:

- creación de producto con `image`
- respuesta pública con `image`
- actualización admin de `image`
- presencia de `image` en seed demo
- exposición de `image` en catálogo demo
- exposición de `image` en cart y auth responses con carrito
- exposición de `image` en checkout y consultas de órdenes

---

## Archivos principales incorporados o modificados

### Dominio / persistencia
- `src/main/java/co/edu/cesde/pps/model/Product.java`
- `src/main/resources/sql/schema.sql`
- `src/main/resources/sql/data.sql`

### DTOs y mappers
- `src/main/java/co/edu/cesde/pps/dto/ProductDTO.java`
- `src/main/java/co/edu/cesde/pps/dto/CartItemDTO.java`
- `src/main/java/co/edu/cesde/pps/dto/OrderItemDTO.java`
- `src/main/java/co/edu/cesde/pps/mapper/CartMapper.java`
- `src/main/java/co/edu/cesde/pps/mapper/OrderMapper.java`
- `src/main/java/co/edu/cesde/pps/web/dto/response/CartItemResponse.java`
- `src/main/java/co/edu/cesde/pps/mapper/ProductMapper.java`
- `src/main/java/co/edu/cesde/pps/web/dto/request/ProductUpsertRequest.java`
- `src/main/java/co/edu/cesde/pps/web/dto/response/OrderItemResponse.java`
- `src/main/java/co/edu/cesde/pps/web/dto/response/ProductResponse.java`
- `src/main/java/co/edu/cesde/pps/web/mapper/WebRequestMapper.java`
- `src/main/java/co/edu/cesde/pps/web/mapper/WebResponseMapper.java`

### Servicio / seed
- `src/main/java/co/edu/cesde/pps/service/ProductService.java`
- `src/main/java/co/edu/cesde/pps/config/demo/DemoDataSeeder.java`

### Testing
- `src/test/java/co/edu/cesde/pps/Etapa11ApplicationLayerIntegrationTest.java`
- `src/test/java/co/edu/cesde/pps/Etapa12HttpIntegrationTest.java`
- `src/test/java/co/edu/cesde/pps/Etapa13AdminAuthorizationIntegrationTest.java`
- `src/test/java/co/edu/cesde/pps/DemoProfileSeedIntegrationTest.java`

### Documentación
- `BACKEND_ENDPOINTS_REFRENCE.md`
- `ETAPA16_SUMMARY.md`
- `README.md`

---

## Criterio de terminado

`etapa16` se considera terminada cuando:

- `Product` persiste `image`
- `GET /api/v1/products` devuelve `image`
- `GET /api/v1/products/{id}` devuelve `image`
- `POST /api/v1/admin/products` acepta y devuelve `image`
- `PUT /api/v1/admin/products/{id}` acepta y devuelve `image`
- `GET /api/v1/cart/me` devuelve `image` por item
- las responses auth con `cart` devuelven `image` por item
- `POST /api/v1/orders/checkout` devuelve `image` por item
- `GET /api/v1/orders/me` devuelve `image` por item
- `GET /api/v1/orders/{id}` devuelve `image` por item
- el perfil `demo` siembra productos con imágenes
- frontend tiene contrato oficial actualizado
- existe documentación nueva propia de etapa16
- existe handoff operativo local en `documents_external/`

---

## Validación ejecutada

```bash
mvn -q -DskipTests compile
mvn -q -Dtest=Etapa11ApplicationLayerIntegrationTest,Etapa12HttpIntegrationTest,Etapa13AdminAuthorizationIntegrationTest,DemoProfileSeedIntegrationTest test
mvn -q test
```

---

## Nota operativa de base de datos

Según la configuración actual del proyecto, `application.yml` usa:

- `ddl-auto: update`

Por tanto, en entornos donde no se sobrescriba ese valor, Hibernate puede crear la nueva columna automáticamente.

Si un entorno usa otra estrategia de DDL, la columna requerida es:

- `image VARCHAR(1000)` en la tabla `products`

---

**Fecha:** 5 de abril de 2026  
**Rama objetivo:** `etapa16`  
**Estado:** ✅ Soporte de imágenes en productos implementado, probado y documentado

