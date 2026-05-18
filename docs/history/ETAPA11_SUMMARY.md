# ETAPA 11 - Implementación de Capa de Aplicación y Preparación de Etapa12

## Objetivo

Implementar en `etapa11` la base técnica previa a la exposición HTTP real, dejando `etapa12` lista para agregar controllers sin rehacer lógica ni volver a mezclar DTOs de servicio con contrato web.

En esta etapa se decidió explícitamente **no crear aún**:

- `@RestController`
- `@RestControllerAdvice`
- uso operativo de Bean Validation en requests HTTP

Esos elementos quedan reservados para `etapa12`.

---

## Punto de Partida

`etapa10` ya tenía:

- Spring Boot + Spring Data JPA
- servicios de negocio funcionales
- checkout persistido
- carrito guest/usuario a nivel dominio

Pero faltaba una capa intermedia reusable para que los futuros controllers fueran delgados.

---

## Qué se implementó realmente en etapa11

### 1) Documentación estructural de la etapa

Se mantuvieron y consolidaron estos documentos:

- `documents_external/ETAPA11_BRECHAS_DOMINIO_ACTUAL.md`
- `documents_external/ETAPA11_CONTRATO_API_COMPLETO.md`
- `documents_external/ETAPA11_ESTRATEGIA_DTOS_REQUEST_RESPONSE.md`
- `ETAPA11_SUMMARY.md`

Además, se actualizó `README.md` para enlazar la documentación nueva y se ajustó `.gitignore` para permitir versionar únicamente los markdowns nuevos de `etapa11` dentro de `documents_external`.

### 2) Base de autenticación y sesión opaca

Se agregó soporte real para auth/sesión sin controllers:

- dependencia `spring-security-crypto` en `pom.xml`
- `PasswordHasher`
- `BCryptPasswordHasher`
- `SessionTokenGenerator`
- `UuidSessionTokenGenerator`
- `AuthenticationException`
- `UserSessionService`

Con esto ya existe una base para:

- crear sesiones guest
- crear sesiones autenticadas
- resolver sesión por token
- expirar/logout de sesión
- verificar usuario autenticado desde token

### 3) Repositorios y services ajustados para etapa12

Se extendieron piezas existentes para soportar la futura capa HTTP:

- `UserSessionRepository`
  - búsqueda por token
  - verificación de token único
  - consulta de sesiones activas por usuario

- `CartRepository`
  - búsqueda de carrito abierto por sesión

- `UserService`
  - búsqueda de usuario entity por email

- `CartService`
  - buscar carrito abierto por sesión
  - buscar o crear carrito abierto de usuario
  - buscar o crear carrito guest por sesión

- `AddressService`
  - consulta/actualización de dirección validando pertenencia al usuario

- `OrderService`
  - consulta de orden validando pertenencia al usuario

- `OrderMapper`
  - ahora incluye direcciones de envío y facturación en `OrderDTO`

### 4) DTOs web separados de los DTOs actuales

Se implementó la primera base de DTOs web en código Java, pero **sin controllers todavía**.

#### Requests

- `RegisterRequest`
- `LoginRequest`
- `AddressUpsertRequest`
- `AddCartItemRequest`
- `UpdateCartItemQuantityRequest`
- `MergeGuestCartRequest`
- `CheckoutRequest`
- `CategoryUpsertRequest`
- `ProductUpsertRequest`

#### Responses

- `AuthSessionResponse`
- `UserResponse`
- `AddressResponse`
- `CategoryResponse`
- `ProductResponse`
- `CartItemResponse`
- `CartSummaryResponse`
- `CartResponse`
- `OrderItemResponse`
- `OrderTotalsResponse`
- `OrderResponse`

#### Error DTOs

- `ApiErrorCode`
- `ApiFieldErrorResponse`
- `ApiErrorResponse`

### 5) Mappers web y capa de aplicación

Se agregó una capa intermedia para que `etapa12` solo deba conectar HTTP con casos de uso ya preparados.

#### Mappers

- `WebRequestMapper`
- `WebResponseMapper`

#### Application Services

- `AuthApplicationService`
- `CatalogApplicationService`
- `CartApplicationService`
- `AddressApplicationService`
- `OrderApplicationService`

Con esto ya quedaron encapsulados casos de uso para:

- crear sesión guest
- registrar usuario con hash real
- login con password hasheado
- obtener usuario actual por token
- logout
- resolver carrito actual por token
- manejar carrito guest y autenticado
- CRUD funcional de direcciones desde token
- checkout y consulta de órdenes del usuario autenticado
- consultas y operaciones de catálogo listas para controllers futuros

### 6) Estrategia de error preparada para etapa12

Se dejó lista la base reutilizable para el futuro `@RestControllerAdvice`:

- `DomainExceptionMapper`
- `ErrorResponseFactory`

Todavía no existe advice HTTP, pero el mapeo excepción → código público de API ya está centralizado.

---

## Qué NO se implementa en etapa11

### Fuera de alcance por decisión de etapa

- controllers REST
- `ResponseEntity`
- `@RequestMapping`
- `@RestControllerAdvice` operativo
- `@Valid` en endpoints
- Bean Validation ejecutándose desde MVC
- status codes HTTP reales

Esto queda reservado para `etapa12`.

---

## Qué deja listo etapa11 para etapa12

Al cerrar esta implementación, `etapa12` ya puede enfocarse solo en conectar HTTP porque ahora ya existen:

1. request DTOs separados de response DTOs
2. error DTOs públicos
3. mapeo centralizado de excepciones
4. auth con token opaco y sesiones persistidas
5. capa de aplicación reusable para controllers delgados
6. consultas seguras por ownership de usuario en direcciones y órdenes

---

## Validación ejecutada

### Compilación

```bash
mvn -q -DskipTests compile
```

### Pruebas

```bash
mvn -q test
```

### Cobertura funcional verificada

- creación de sesión guest
- creación de carrito guest desde token
- registro de usuario con hash BCrypt
- merge de carrito guest → usuario
- login con password hasheado
- gestión de direcciones desde capa de aplicación
- checkout desde capa de aplicación
- consulta de órdenes del usuario autenticado
- construcción normalizada de errores para la futura capa HTTP
- prueba MySQL marcada como opt-in para no romper la suite general sin infraestructura externa

---

## Commits granulares recomendados

1. `feat(auth-session): add password hashing and opaque session services`
2. `refactor(service): expose ownership and session-ready operations for etapa12`
3. `feat(application): add web dto contracts and application services without controllers`
4. `feat(error): prepare api error mapping for future rest advice`
5. `test(application): add etapa11 integration coverage and make mysql connectivity opt-in`
6. `docs(etapa11): update summary and stage11 references after implementation`

---

## Próximo Paso Recomendado

### ETAPA 12

Implementar la capa HTTP real apoyándose en lo construido en `etapa11`:

1. controllers por módulo
2. `@RestControllerAdvice`
3. `@Valid` y Bean Validation en requests
4. mapeo de status HTTP
5. pruebas de integración de endpoints

La meta es que los controllers de `etapa12` sean principalmente un adaptador delgado hacia los `ApplicationService` ya creados.

---

**Fecha:** 5 de abril de 2026  
**Rama:** `etapa11`  
**Estado:** ✅ Implementada la base técnica de etapa11 sin controllers; etapa12 preparada para capa HTTP

