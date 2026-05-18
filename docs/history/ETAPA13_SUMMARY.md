# ETAPA 13 - Autorización básica por rol ADMIN/CUSTOMER

## Objetivo

Agregar una capa mínima pero consistente de autorización para endpoints administrativos sobre la base de `etapa12`, sin introducir todavía Spring Security completo.

La meta de esta etapa es que el backend ya pueda distinguir entre:

- usuario no autenticado → `401 Unauthorized`
- usuario autenticado sin privilegios admin → `403 Forbidden`
- usuario autenticado con rol `ADMIN` → acceso permitido

---

## Punto de partida

`etapa12` ya dejó implementado:

- sesión opaca por `sessionToken`
- resolución de sesión actual
- resolución de usuario autenticado
- controllers REST por módulo
- `ApiExceptionHandler`
- contrato de errores HTTP consistente
- endpoints admin funcionales, pero aún sin control real por rol

---

## Qué implementa etapa13

### 1. Contrato de error `403 Forbidden`
Se agrega soporte explícito para denegación de acceso por rol:

- `AuthorizationException`
- `ApiErrorCode.FORBIDDEN`
- mapeo en `DomainExceptionMapper`
- respuesta HTTP `403` desde `ApiExceptionHandler`

### 2. Guard reusable para acceso admin
Se agrega:

- `AdminAccessGuard`

Este componente reutiliza la sesión actual y valida que el usuario autenticado tenga rol `ADMIN`.

### 3. Protección de endpoints admin
Se actualiza:

- `AdminProductController`

para exigir:

- token válido
- usuario autenticado
- rol `ADMIN`

### 4. Pruebas HTTP de autorización
Se agregan pruebas para cubrir:

- `401` sin token
- `401` con sesión guest
- `403` con usuario `CUSTOMER`
- `201` exitoso con usuario `ADMIN`

Además se ajustan las pruebas HTTP heredadas de `etapa12` para invocar endpoints admin con sesión administrativa válida.

---

## Resultado funcional de etapa13

Al cerrar esta etapa:

- los endpoints públicos siguen igual
- los endpoints autenticados de usuario siguen igual
- los endpoints admin ya no quedan expuestos a cualquier consumidor
- la API responde `403 Forbidden` cuando un `CUSTOMER` intenta operar como admin

---

## Archivos principales modificados

### Seguridad / autorización
- `src/main/java/co/edu/cesde/pps/exception/AuthorizationException.java`
- `src/main/java/co/edu/cesde/pps/web/security/AdminAccessGuard.java`
- `src/main/java/co/edu/cesde/pps/web/controller/AdminProductController.java`

### Contrato de error
- `src/main/java/co/edu/cesde/pps/web/dto/error/ApiErrorCode.java`
- `src/main/java/co/edu/cesde/pps/web/error/DomainExceptionMapper.java`
- `src/main/java/co/edu/cesde/pps/web/advice/ApiExceptionHandler.java`

### Testing
- `src/test/java/co/edu/cesde/pps/Etapa12HttpIntegrationTest.java`
- `src/test/java/co/edu/cesde/pps/Etapa13AdminAuthorizationIntegrationTest.java`

### Documentación
- `BACKEND_ENDPOINTS_REFRENCE.md`
- `README.md`
- `ETAPA13_SUMMARY.md`

---

## Criterio de terminado

`etapa13` se considera terminada cuando:

- existe una excepción explícita para autorización
- existe un `ApiErrorCode` para `FORBIDDEN`
- el advice global responde `403`
- los endpoints admin exigen rol `ADMIN`
- las pruebas distinguen correctamente `401`, `403` y acceso exitoso admin
- la documentación pública quedó actualizada

---

## Qué sigue pendiente después de etapa13

Esta etapa no busca cerrar seguridad avanzada completa. Aún pueden quedar como backlog:

1. Spring Security completa
2. autorización más fina por permisos y recursos
3. auditoría de acciones administrativas
4. refresh/revocación avanzada de sesiones
5. CORS y hardening final
6. OpenAPI / Swagger

---

## Commits granulares sugeridos

1. `feat(authz): add forbidden error contract`
2. `feat(authz): add admin access guard`
3. `feat(admin): protect admin product endpoints by role`
4. `test(authz): cover admin unauthorized forbidden and success flows`
5. `docs(etapa13): add authorization summary and student guides`

---

## Validación recomendada

```bash
mvn -q -DskipTests compile
mvn -q -Dtest=Etapa12HttpIntegrationTest,Etapa13AdminAuthorizationIntegrationTest test
mvn -q test
```

---

**Fecha:** 5 de abril de 2026  
**Rama objetivo:** `etapa13`  
**Estado:** ✅ Implementación de autorización básica por rol

