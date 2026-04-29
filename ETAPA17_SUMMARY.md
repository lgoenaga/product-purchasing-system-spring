# ETAPA 17 - Documentación interactiva con Springdoc OpenAPI / Swagger UI

## Objetivo

Integrar documentación interactiva de la API REST usando **Springdoc OpenAPI 2.x**
(compatible con Spring Boot 3.x / Java 21), habilitada exclusivamente en el perfil
de desarrollo (`dev`).

La meta de `etapa17` es que cualquier desarrollador, estudiante o integrante del
equipo pueda:

- Explorar todos los endpoints desde un navegador sin herramientas externas
- Probar requests directamente desde la UI con autenticación Bearer
- Consultar el contrato HTTP completo (schemas, validaciones, ejemplos) en un solo lugar
- Acceder al JSON OpenAPI estándar para generación de clientes o colecciones

---

## Qué implementa etapa17

### 1. Dependencia Springdoc OpenAPI

Se agrega `springdoc-openapi-starter-webmvc-ui:2.8.8` al `pom.xml`. Esta librería
genera automáticamente la especificación OpenAPI 3.x a partir de los controladores
y DTOs, y sirve la Swagger UI sin configuración adicional de servidor.

### 2. Configuración OpenAPI centralizada (`OpenApiConfig.java`)

Se crea la clase de configuración con:

- `@OpenAPIDefinition` — título, versión, descripción completa del proyecto y servidor de desarrollo
- `@SecurityScheme` — esquema `BearerAuth` de tipo HTTP con formato `SessionToken` (no JWT)
- `@Profile("dev")` — la configuración permanece **inactiva en producción**
- Bean `OpenAPI` — define el orden de aparición de los 9 grupos (tags) en la UI

### 3. Esquema de seguridad: Bearer SessionToken (no JWT)

El sistema usa un token de sesión opaco almacenado en la tabla `user_sessions`.

El token **no es un JWT**:

- No se puede decodificar en cliente
- No tiene claims internos
- Su validez se verifica contra la base de datos en cada request
- Es generado por el backend al registrarse, hacer login o crear sesión de invitado

El flujo de autenticación en Swagger UI es:

1. Ejecutar `POST /api/v1/auth/login` (o `/guest-session`) en la UI
2. Copiar el valor del campo `sessionToken` de la respuesta
3. Clic en **Authorize 🔓** → pegar el token → confirmar
4. Todos los endpoints protegidos lo incluirán automáticamente en el header `Authorization: Bearer <token>`

### 4. Configuración por perfil (`application.yml` y `application-dev.yml`)

- **`application.yml`** — Springdoc deshabilitado por defecto (`enabled: false`) para no exponer la documentación en producción
- **`application-dev.yml`** — activa Swagger UI, configura ruta `/swagger-ui.html`, ordena endpoints por método HTTP y habilita `persist-authorization`

### 5. Ajuste CORS (`WebConfig.java`)

El mapping CORS se separa en tres secciones con reglas distintas:

- `/api/**` — CORS con credenciales, orígenes desde `.env` (comportamiento anterior)
- `/swagger-ui/**` — CORS abierto sin credenciales (permite acceso a la UI)
- `/v3/api-docs/**` — CORS abierto sin credenciales (permite que la UI consuma el JSON)

Además se agrega `localhost:8081` a los orígenes permitidos en `application-dev.yml`
para que **Try it out** no sea bloqueado por el navegador.

### 6. Anotaciones detalladas en los 9 controladores

Cada controlador recibe:

- `@Tag(name, description)` — nombre y descripción del grupo en la UI
- `@SecurityRequirement(name = "BearerAuth")` — a nivel de clase para controladores protegidos
- `@Operation(summary, description)` — descripción detallada por endpoint con reglas de negocio
- `@ApiResponse` — respuestas documentadas por código HTTP (200, 201, 204, 400, 401, 403, 404)
- `@Parameter(hidden = true)` — oculta el header `Authorization` de la UI (lo gestiona el botón Authorize)

### 7. Anotaciones `@Schema` en todos los DTOs

Todos los records de request y response reciben `@Schema` con:

- `description` — propósito semántico del campo
- `example` — valor de ejemplo realista
- `requiredMode` — indica si el campo es obligatorio
- `nullable` — marca campos opcionales
- `allowableValues` — valores permitidos para campos enum-like (ej: `ADMIN | CUSTOMER`)

---

## Grupos de endpoints documentados

| Tag | Ruta base | Seguridad |
|-----|-----------|-----------|
| Auth | `/api/v1/auth` | Público (excepto `/me` y `/logout`) |
| Products | `/api/v1/products` | Público |
| Categories | `/api/v1/categories` | Público |
| Cart | `/api/v1/cart` | Bearer (guest o autenticado) |
| Orders | `/api/v1/orders` | Bearer (usuario autenticado) |
| User Profile | `/api/v1/users/me` | Bearer (usuario autenticado) |
| Addresses | `/api/v1/users/me/addresses` | Bearer (usuario autenticado) |
| Admin — Products | `/api/v1/admin/products` | Bearer + rol ADMIN |
| Admin — Users | `/api/v1/admin/users` | Bearer + rol ADMIN |

---

## Archivos principales incorporados o modificados

### Configuración

- `pom.xml`
- `src/main/resources/application.yml`
- `src/main/resources/application-dev.yml` ← **NUEVO**
- `src/main/java/co/edu/cesde/pps/config/OpenApiConfig.java` ← **NUEVO**
- `src/main/java/co/edu/cesde/pps/config/WebConfig.java`

### Controladores (anotados con OpenAPI)

- `src/main/java/co/edu/cesde/pps/web/controller/AuthController.java`
- `src/main/java/co/edu/cesde/pps/web/controller/ProductController.java`
- `src/main/java/co/edu/cesde/pps/web/controller/CategoryController.java`
- `src/main/java/co/edu/cesde/pps/web/controller/CartController.java`
- `src/main/java/co/edu/cesde/pps/web/controller/OrderController.java`
- `src/main/java/co/edu/cesde/pps/web/controller/UserProfileController.java`
- `src/main/java/co/edu/cesde/pps/web/controller/AddressController.java`
- `src/main/java/co/edu/cesde/pps/web/controller/AdminProductController.java`
- `src/main/java/co/edu/cesde/pps/web/controller/AdminUserController.java`

### DTOs de request (13 archivos con `@Schema`)

- `LoginRequest.java`, `RegisterRequest.java`, `ProductUpsertRequest.java`
- `AddCartItemRequest.java`, `UpdateCartItemQuantityRequest.java`, `MergeGuestCartRequest.java`
- `CheckoutRequest.java`, `AddressUpsertRequest.java`, `UpdateMyProfileRequest.java`
- `ChangeMyPasswordRequest.java`, `CreateAdminUserRequest.java`, `UpdateAdminUserRequest.java`
- `CategoryUpsertRequest.java`

### DTOs de response (11 archivos con `@Schema`)

- `AuthSessionResponse.java`, `UserResponse.java`, `ProductResponse.java`
- `CategoryResponse.java`, `CartResponse.java`, `CartItemResponse.java`
- `CartSummaryResponse.java`, `OrderResponse.java`, `OrderItemResponse.java`
- `OrderTotalsResponse.java`, `AddressResponse.java`

### Documentación

- `ETAPA17_SUMMARY.md`
- `documents_external/ETAPA17_ARCHIVOS_MODIFICADOS_Estudiantes.md`
- `documents_external/ETAPA17_PASO_A_PASO_ESTUDIANTES.md`

---

## Criterio de terminado

`etapa17` se considera terminada cuando:

- la dependencia `springdoc-openapi-starter-webmvc-ui` está declarada en `pom.xml`
- `mvn compile` pasa sin errores
- `OpenApiConfig.java` existe y está anotado con `@Profile("dev")`
- Swagger UI es inaccesible sin el perfil `dev` activo
- Swagger UI es accesible en `http://localhost:8081/swagger-ui.html` con `SPRING_PROFILES_ACTIVE=dev`
- el JSON OpenAPI está disponible en `http://localhost:8081/v3/api-docs`
- el botón **Authorize 🔓** acepta el `sessionToken` de `/auth/login`
- todos los endpoints protegidos retornan respuesta correcta al usar el token desde la UI
- los 9 controladores tienen `@Tag` + `@Operation` + `@ApiResponse` por método
- todos los DTOs de request tienen `@Schema` con `description` y `example`
- todos los DTOs de response tienen `@Schema` con `description` y `example`
- CORS no bloquea `/swagger-ui/**` ni `/v3/api-docs/**`

---

## Validación ejecutada

```bash
mvn -q -DskipTests compile

# Activar perfil dev en .env:
# SPRING_PROFILES_ACTIVE=dev

# Iniciar la aplicación y navegar a:
# http://localhost:8081/swagger-ui.html
```

---

## URLs de referencia (perfil dev)

| Recurso | URL |
|---------|-----|
| Swagger UI | `http://localhost:8081/swagger-ui.html` |
| JSON OpenAPI | `http://localhost:8081/v3/api-docs` |

---

**Fecha:** 29 de abril de 2026  
**Rama objetivo:** `etapa17`  
**Estado:** ✅ Swagger UI implementado, documentado y habilitado exclusivamente para perfil dev
