# ETAPA 12 - Controllers REST, Advice Global y Bean Validation

## Objetivo

Convertir la base técnica creada en `etapa11` en una API HTTP real, agregando:

- controllers REST
- manejo global de errores HTTP
- Bean Validation operativo
- pruebas de integración por endpoint

En esta etapa se conecta la capa web con los `ApplicationService` ya existentes.

---

## Punto de partida

`etapa11` ya dejó implementado:

- `ApplicationService` por módulo
- DTOs web `request/response/error`
- auth por `sessionToken`
- `DomainExceptionMapper`
- `ErrorResponseFactory`
- mappers web
- pruebas de integración de capa de aplicación

Gracias a esto, `etapa12` no debe volver a mover lógica de negocio al controller.

---

## Alcance de implementación esperado

### Infraestructura HTTP
- `ApiRoutes`
- `BearerTokenExtractor`
- `CurrentSessionResolver`
- `ApiExceptionHandler`

### Controllers
- `AuthController`
- `AddressController`
- `CartController`
- `OrderController`
- `CategoryController`
- `ProductController`
- `AdminProductController`

### Validación
- anotaciones Bean Validation sobre request DTOs
- uso de `@Valid` en endpoints

### Testing
- pruebas HTTP con `MockMvc`
- validación de status codes
- validación de payloads de error y éxito

---

## Flujo principal que debe quedar funcional

1. crear sesión guest
2. consultar carrito guest
3. agregar items al carrito
4. registrar o hacer login usando `guestCartId`
5. consultar `auth/me`
6. crear direcciones
7. hacer checkout
8. consultar órdenes del usuario
9. ver detalle de orden

---

## Error handling esperado

El advice global debe responder de forma consistente:

- `400 Bad Request`
- `401 Unauthorized`
- `404 Not Found`
- `409 Conflict`
- `500 Internal Server Error`

usando:

- `DomainExceptionMapper`
- `ErrorResponseFactory`
- `ApiErrorResponse`

---

## Criterio de terminado de etapa12

`etapa12` se considera terminada cuando:

- existen controllers por módulo
- todos delegan a `ApplicationService`
- funciona `@RestControllerAdvice`
- funciona Bean Validation
- el contrato HTTP se cumple
- hay pruebas de integración HTTP
- el flujo principal funciona de punta a punta
- la rama está lista para merge a `main`

---

## Qué deja resuelto etapa12

Si se implementa todo el alcance previsto, el backend queda funcional para integración real con frontend en:

- auth básica por sesión
- catálogo
- direcciones
- carrito
- merge guest
- checkout
- consulta de órdenes
- errores HTTP consistentes

---

## Qué puede quedar pendiente después de etapa12

Aunque la API quede funcional, pueden seguir pendientes mejoras como:

1. autorización robusta por roles
2. dashboard admin agregado
3. pasarela de pagos real
4. OpenAPI / Swagger
5. CORS y hardening
6. pruebas E2E completas
7. seguridad avanzada con Spring Security completa

---

## Commits granulares recomendados

1. `feat(http): add base web infrastructure for etapa12`
2. `feat(validation): enable bean validation on etapa12 request dto`
3. `feat(auth): add auth controller endpoints`
4. `feat(address): add authenticated address endpoints`
5. `feat(cart): add cart endpoints for guest and authenticated flows`
6. `feat(order): add checkout and order query endpoints`
7. `feat(catalog): add category and product public endpoints`
8. `feat(admin): add admin product endpoints`
9. `test(http): add etapa12 endpoint integration coverage`
10. `docs(etapa12): add summary and student implementation guide`

---

## Validación antes de merge

### Compilación
```bash
mvn -q -DskipTests compile
```

### Pruebas
```bash
mvn -q test
```

### Revisión de estado
```bash
git status
```

---

## Estrategia de ramas y merge

### Crear rama
```bash
git checkout etapa11
git pull origin etapa11
git checkout -b etapa12
```

### Publicar rama
```bash
git push -u origin etapa12
```

### Merge final
- merge recomendado: `etapa12 -> main`
- solo después de validación completa y sin errores

---

**Fecha:** 5 de abril de 2026  
**Rama objetivo:** `etapa12`  
**Estado:** ⏳ Plan operativo listo; implementación pendiente

