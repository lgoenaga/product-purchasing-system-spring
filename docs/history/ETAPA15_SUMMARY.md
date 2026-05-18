# ETAPA 15 - CRUD administrativo de usuarios

## Objetivo

Agregar el módulo mínimo de administración de usuarios que necesita frontend para construir el dashboard admin de usuarios sobre endpoints oficiales del backend.

La meta de `etapa15` es cerrar un CRUD administrativo completo, protegido por rol `ADMIN`, sin romper la arquitectura vigente ni mezclar este alcance con el autoservicio del usuario autenticado.

---

## Qué implementa etapa15

### 1. Namespace admin para usuarios
Se agrega la ruta base:

- `GET /api/v1/admin/users`
- `POST /api/v1/admin/users`
- `GET /api/v1/admin/users/{id}`
- `PUT /api/v1/admin/users/{id}`
- `DELETE /api/v1/admin/users/{id}`

Todos estos endpoints requieren:

- sesión autenticada
- rol `ADMIN`

### 2. CRUD completo para dashboard admin
El backend queda listo para:

- crear usuarios con rol y estado inicial
- listar usuarios para tablas administrativas
- consultar detalle por ID
- actualizar datos, rol y estado
- desactivar usuarios mediante baja lógica

### 3. Requests específicos de admin
Se agregan dos request DTOs nuevos:

#### `CreateAdminUserRequest`
Incluye:

- `email`
- `password`
- `firstName`
- `lastName`
- `phone`
- `role`
- `status`

#### `UpdateAdminUserRequest`
Incluye:

- `email`
- `firstName`
- `lastName`
- `phone`
- `role`
- `status`

Regla de diseño:

- `create` sí incluye contraseña inicial
- `update` no cambia contraseña dentro de este CRUD

### 4. Roles y estados expuestos en etapa15
Para el contrato administrativo documentado en esta etapa:

- roles permitidos: `ADMIN`, `CUSTOMER`
- estados permitidos: `ACTIVE`, `INACTIVE`

Aunque `BLOCKED` existe en el enum del dominio, no se expone como estado editable en este CRUD base de etapa15.

### 5. Baja lógica de usuarios
El endpoint de eliminación no borra físicamente el registro.

Se mantiene el patrón:

- `DELETE /api/v1/admin/users/{id}`
- cambia `status` a `INACTIVE`

Además, se alinea autenticación para que un usuario `INACTIVE` no pueda iniciar sesión.

### 6. Pruebas de integración
Se agregan pruebas para cubrir:

- `401` sin token
- `403` para customer intentando usar endpoints admin
- creación de usuario por admin
- listado de usuarios
- consulta de usuario por id
- actualización de usuario
- eliminación lógica
- rechazo de login cuando el usuario queda `INACTIVE`
- `409` por email duplicado
- `400` por estado inválido fuera del contrato

---

## Archivos principales incorporados o modificados

### Web / rutas
- `src/main/java/co/edu/cesde/pps/web/controller/ApiRoutes.java`
- `src/main/java/co/edu/cesde/pps/web/controller/AdminUserController.java`

### Application
- `src/main/java/co/edu/cesde/pps/application/AdminUserApplicationService.java`
- `src/main/java/co/edu/cesde/pps/application/AuthApplicationService.java`

### Request DTOs
- `src/main/java/co/edu/cesde/pps/web/dto/request/CreateAdminUserRequest.java`
- `src/main/java/co/edu/cesde/pps/web/dto/request/UpdateAdminUserRequest.java`

### Servicio de usuario
- `src/main/java/co/edu/cesde/pps/service/UserService.java`

### Testing
- `src/test/java/co/edu/cesde/pps/Etapa15AdminUsersIntegrationTest.java`

### Documentación
- `BACKEND_ENDPOINTS_REFRENCE.md`
- `ETAPA15_SUMMARY.md`
- `README.md`

---

## Criterio de terminado

`etapa15` se considera terminada cuando:

- existe el namespace `/api/v1/admin/users`
- create/list/get/update/delete requieren rol `ADMIN`
- create acepta contraseña inicial
- update no cambia contraseña
- delete hace baja lógica con `INACTIVE`
- frontend tiene contrato oficial actualizado en la referencia de endpoints
- existe documentación nueva propia de etapa15
- existe handoff operativo local en `documents_external/`

---

## Validación recomendada

```bash
mvn -q -DskipTests compile
mvn -q -Dtest=Etapa13AdminAuthorizationIntegrationTest,Etapa15AdminUsersIntegrationTest test
mvn -q test
```

---

**Fecha:** 5 de abril de 2026  
**Rama objetivo:** `etapa15`  
**Estado:** ✅ CRUD administrativo de usuarios implementado y documentado

