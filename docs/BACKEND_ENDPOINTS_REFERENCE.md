# BACKEND_ENDPOINTS_REFRENCE - Referencia operativa de endpoints

## Objetivo

Este documento resume, endpoint por endpoint, la API HTTP disponible al cerrar `etapa14`.

Está pensado para el equipo de frontend y QA como referencia rápida del backend funcional.

---

## Convenciones generales

### Base URL
- `/api/v1`

### Autenticación
- Se usa sesión opaca vía header:
  - `Authorization: Bearer <sessionToken>`

### Formato de error estándar

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": [
    {
      "field": "email",
      "message": "must not be blank"
    }
  ],
  "timestamp": "2026-04-05T14:30:00",
  "path": "/api/v1/auth/register"
}
```

### Códigos de error usados
- `VALIDATION_ERROR` → `400`
- `UNAUTHORIZED` → `401`
- `FORBIDDEN` → `403`
- `RESOURCE_NOT_FOUND` → `404`
- `DUPLICATE_RESOURCE` → `409`
- `INSUFFICIENT_STOCK` → `409`
- `INVALID_CART_STATE` → `409`
- `CART_MERGE_ERROR` → `409`
- `INTERNAL_SERVER_ERROR` → `500`

---

# 1. Auth

## 1.1 Crear sesión guest
- **POST** `/api/v1/auth/guest-session`
- **Auth requerida:** no
- **Body:** no
- **Response:** `201 Created`

### Respuesta esperada
- `sessionToken`
- `sessionId`
- `expiresAt`
- `user = null`
- `cart` guest inicial
- el objeto `cart` sigue el mismo contrato de la sección `# 5. Cart`, incluyendo `items[*].image`

---

## 1.2 Registrar usuario
- **POST** `/api/v1/auth/register`
- **Auth requerida:** no
- **Body:** JSON
- **Response:** `201 Created`

### Respuesta esperada
- `sessionToken`
- `sessionId`
- `expiresAt`
- `user`
- `cart`
- el objeto `cart` sigue el mismo contrato de la sección `# 5. Cart`, incluyendo `items[*].image`

### Body
```json
{
  "email": "ada@cesde.edu.co",
  "password": "secret123",
  "firstName": "Ada",
  "lastName": "Lovelace",
  "phone": "3001234567",
  "guestCartId": 10
}
```

### Reglas
- `guestCartId` es opcional
- si llega, se intenta fusionar con el carrito del usuario recién registrado

---

## 1.3 Login
- **POST** `/api/v1/auth/login`
- **Auth requerida:** no
- **Body:** JSON
- **Response:** `200 OK`

### Respuesta esperada
- `sessionToken`
- `sessionId`
- `expiresAt`
- `user`
- `cart`
- el objeto `cart` sigue el mismo contrato de la sección `# 5. Cart`, incluyendo `items[*].image`

### Body
```json
{
  "email": "ada@cesde.edu.co",
  "password": "secret123",
  "guestCartId": 10
}
```

### Reglas
- `guestCartId` es opcional
- si llega, se intenta merge con carrito autenticado

---

## 1.4 Usuario autenticado actual
- **GET** `/api/v1/auth/me`
- **Auth requerida:** sí
- **Body:** no
- **Response:** `200 OK`

### Respuesta
- `id`
- `email`
- `firstName`
- `lastName`
- `fullName`
- `role`
- `phone`
- `status`
- `createdAt`

### Regla
- `fullName` ya es calculado y devuelto por backend; frontend no necesita construirlo
- `role` se expone con el valor exacto de `Role.name`; para integración actual los valores esperados son `ADMIN` y `CUSTOMER`

---

## 1.5 Logout
- **POST** `/api/v1/auth/logout`
- **Auth requerida:** sí
- **Body:** no
- **Response:** `204 No Content`

---

## 1.6 Actualizar perfil del usuario autenticado
- **PUT** `/api/v1/users/me`
- **Auth requerida:** sí
- **Body:** JSON
- **Response:** `200 OK`

### Body
```json
{
  "firstName": "Ada",
  "lastName": "Lovelace",
  "phone": "3001234567"
}
```

### Respuesta
```json
{
  "id": 1,
  "email": "ada@cesde.edu.co",
  "firstName": "Ada",
  "lastName": "Lovelace",
  "fullName": "Ada Lovelace",
  "role": "CUSTOMER",
  "phone": "3001234567",
  "status": "ACTIVE",
  "createdAt": "2026-04-05T14:30:00"
}
```

### Errores esperados
- `400 VALIDATION_ERROR`
- `401 UNAUTHORIZED`

---

## 1.7 Cambiar contraseña del usuario autenticado
- **PUT** `/api/v1/users/me/password`
- **Auth requerida:** sí
- **Body:** JSON
- **Response:** `204 No Content`

### Body
```json
{
  "currentPassword": "secret123",
  "newPassword": "secret456"
}
```

### Reglas
- `currentPassword` debe coincidir con la contraseña actual del usuario autenticado
- `newPassword` debe tener mínimo 8 caracteres
- el cambio de contraseña no invalida automáticamente la sesión actual en `etapa14`

### Errores esperados
- `400 VALIDATION_ERROR`
- `401 UNAUTHORIZED`

---

# 2. Categories

## 2.1 Listar categorías
- **GET** `/api/v1/categories`
- **Auth requerida:** no
- **Response:** `200 OK`

## 2.2 Árbol de categorías
- **GET** `/api/v1/categories/tree`
- **Auth requerida:** no
- **Response:** `200 OK`

### Regla
- responde una lista de categorías raíz
- cada nodo usa el mismo shape recursivo de `CategoryResponse`
- `subcategories` puede venir anidado para representar la jerarquía completa

### Shape real de ejemplo
```json
[
  {
    "id": 1,
    "parentId": null,
    "parentName": null,
    "name": "Electronics",
    "slug": "electronics",
    "isRoot": true,
    "subcategoriesCount": 2,
    "productsCount": 0,
    "subcategories": [
      {
        "id": 2,
        "parentId": 1,
        "parentName": "Electronics",
        "name": "Computers",
        "slug": "computers",
        "isRoot": false,
        "subcategoriesCount": 0,
        "productsCount": 1,
        "subcategories": []
      }
    ]
  }
]
```

## 2.3 Obtener categoría por ID
- **GET** `/api/v1/categories/{id}`
- **Auth requerida:** no
- **Response:** `200 OK`

## 2.4 Obtener subcategorías
- **GET** `/api/v1/categories/{id}/subcategories`
- **Auth requerida:** no
- **Response:** `200 OK`

### Shape de categoría
```json
{
  "id": 1,
  "parentId": null,
  "parentName": null,
  "name": "Perifericos",
  "slug": "perifericos",
  "isRoot": true,
  "subcategoriesCount": 1,
  "productsCount": 2,
  "subcategories": []
}
```

---

# 3. Products

## 3.1 Listar productos públicos
- **GET** `/api/v1/products`
- **Auth requerida:** no
- **Response:** `200 OK`

### Query params soportados
- `search` opcional
- `categoryId` opcional
- `activeOnly` opcional, default `true`

### Reglas
- solo el detalle público expone productos activos
- si `search` viene informado, se filtra por nombre
- si `categoryId` viene informado, se filtra por categoría
- `activeOnly=true` excluye productos inactivos
- `image` representa la URL pública de la imagen principal del producto y puede venir en `null` si el producto no la tiene cargada

## 3.2 Obtener producto por ID
- **GET** `/api/v1/products/{id}`
- **Auth requerida:** no
- **Response:** `200 OK`
- **Si el producto está inactivo:** responde `404`

### Shape de producto
```json
{
  "id": 101,
  "categoryId": 1,
  "categoryName": "Perifericos",
  "sku": "MOU-001",
  "name": "Mouse Gamer",
  "description": "Mouse Gamer descripcion de prueba",
  "image": "https://images.unsplash.com/photo-1511467687858-23d96c32e4ae?auto=format&fit=crop&w=1200&q=80",
  "price": 89.90,
  "stockQty": 20,
  "isActive": true,
  "isAvailable": true,
  "createdAt": "2026-04-05T14:30:00"
}
```

---

# 4. Addresses

## 4.1 Listar direcciones del usuario autenticado
- **GET** `/api/v1/users/me/addresses`
- **Auth requerida:** sí
- **Response:** `200 OK`

## 4.2 Obtener dirección por ID
- **GET** `/api/v1/users/me/addresses/{id}`
- **Auth requerida:** sí
- **Response:** `200 OK`

## 4.3 Crear dirección
- **POST** `/api/v1/users/me/addresses`
- **Auth requerida:** sí
- **Body:** JSON
- **Response:** `201 Created`

## 4.4 Actualizar dirección
- **PUT** `/api/v1/users/me/addresses/{id}`
- **Auth requerida:** sí
- **Body:** JSON
- **Response:** `200 OK`

## 4.5 Marcar dirección como default
- **PATCH** `/api/v1/users/me/addresses/{id}/default`
- **Auth requerida:** sí
- **Body:** no
- **Response:** `200 OK`

### Respuesta
```json
{
  "id": 101,
  "userId": 1,
  "type": "BILLING",
  "line1": "Carrera 20 #50-10",
  "line2": null,
  "city": "Medellin",
  "state": "Antioquia",
  "country": "Colombia",
  "postalCode": "050001",
  "isDefault": true
}
```

### Errores esperados
- `400 VALIDATION_ERROR` si la dirección no pertenece al usuario autenticado
- `401 UNAUTHORIZED`
- `404 RESOURCE_NOT_FOUND`

## 4.6 Eliminar dirección
- **DELETE** `/api/v1/users/me/addresses/{id}`
- **Auth requerida:** sí
- **Body:** no
- **Response:** `204 No Content`

### Body para crear/actualizar
```json
{
  "type": "SHIPPING",
  "line1": "Calle 10 #20-30",
  "line2": null,
  "city": "Medellin",
  "state": "Antioquia",
  "country": "Colombia",
  "postalCode": "050001",
  "isDefault": true
}
```

---

# 5. Cart

## 5.1 Obtener carrito actual
- **GET** `/api/v1/cart/me`
- **Auth requerida:** sí, guest o autenticado
- **Response:** `200 OK`

## 5.2 Agregar item
- **POST** `/api/v1/cart/items`
- **Auth requerida:** sí, guest o autenticado
- **Body:** JSON
- **Response:** `200 OK`

## 5.3 Actualizar cantidad de item
- **PATCH** `/api/v1/cart/items/{productId}`
- **Auth requerida:** sí, guest o autenticado
- **Body:** JSON
- **Response:** `200 OK`

## 5.4 Remover item
- **DELETE** `/api/v1/cart/items/{productId}`
- **Auth requerida:** sí, guest o autenticado
- **Response:** `200 OK`

## 5.5 Limpiar carrito actual
- **DELETE** `/api/v1/cart/items`
- **Auth requerida:** sí, guest o autenticado
- **Response:** `204 No Content`

## 5.6 Fusionar carrito guest con autenticado
- **POST** `/api/v1/cart/merge`
- **Auth requerida:** sí, usuario autenticado
- **Body:** JSON
- **Response:** `200 OK`

### Body agregar item
```json
{
  "productId": 101,
  "quantity": 2
}
```

### Body actualizar cantidad
```json
{
  "quantity": 3
}
```

### Body merge
```json
{
  "guestCartId": 10
}
```

### Comportamiento cuando `guestCartId` no es válido
- si el carrito no existe: `404 RESOURCE_NOT_FOUND`
- si el carrito existe pero no está en estado `OPEN`: `409 INVALID_CART_STATE`
- si el carrito existe pero ya tiene usuario asignado: `409 CART_MERGE_ERROR`
- si el merge supera el stock disponible: `409 INSUFFICIENT_STOCK`

### Shape de carrito
- cada item expone `image` con la imagen principal actual del producto relacionado
- `image` puede venir en `null` si el producto no tiene imagen cargada

```json
{
  "id": 22,
  "userId": 1,
  "userEmail": "ada@cesde.edu.co",
  "status": "OPEN",
  "isGuest": false,
  "createdAt": "2026-04-05T14:30:00",
  "updatedAt": "2026-04-05T14:35:00",
  "items": [
    {
      "id": 100,
      "productId": 101,
      "sku": "MOU-001",
      "name": "Mouse Gamer",
      "image": "https://images.unsplash.com/photo-1511467687858-23d96c32e4ae?auto=format&fit=crop&w=1200&q=80",
      "quantity": 2,
      "unitPrice": 89.90,
      "lineTotal": 179.80,
      "productAvailable": true,
      "productStock": 20,
      "addedAt": "2026-04-05T14:31:00"
    }
  ],
  "summary": {
    "itemsCount": 2,
    "subtotal": 179.80,
    "tax": 0,
    "shipping": 0,
    "total": 179.80
  }
}
```

---

# 6. Orders

## 6.1 Checkout
- **POST** `/api/v1/orders/checkout`
- **Auth requerida:** sí
- **Body:** JSON
- **Response:** `201 Created`

## 6.2 Listar órdenes del usuario
- **GET** `/api/v1/orders/me`
- **Auth requerida:** sí
- **Response:** `200 OK`

### Regla
- si el usuario no tiene órdenes, responde arreglo vacío `[]`
- no responde `null`
- no responde `404`

## 6.3 Obtener detalle de orden
- **GET** `/api/v1/orders/{id}`
- **Auth requerida:** sí
- **Response:** `200 OK`

### Regla de items de orden
- cada item expone `image` con la imagen principal actual del producto relacionado
- `image` puede venir en `null` si el producto no tiene imagen cargada

### Body checkout
```json
{
  "cartId": 22,
  "shippingAddressId": 100,
  "billingAddressId": 101
}
```

### Shape de orden
```json
{
  "id": 5001,
  "orderNumber": "ORD-20260405-123456",
  "userId": 1,
  "userEmail": "ada@cesde.edu.co",
  "userFullName": "Ada Lovelace",
  "status": "PENDING",
  "shippingAddress": {
    "id": 100,
    "userId": 1,
    "type": "SHIPPING",
    "line1": "Calle 10 #20-30",
    "line2": null,
    "city": "Medellin",
    "state": "Antioquia",
    "country": "Colombia",
    "postalCode": "050001",
    "isDefault": true
  },
  "billingAddress": {
    "id": 101,
    "userId": 1,
    "type": "BILLING",
    "line1": "Carrera 20 #50-10",
    "line2": null,
    "city": "Medellin",
    "state": "Antioquia",
    "country": "Colombia",
    "postalCode": "050001",
    "isDefault": true
  },
  "items": [
    {
      "id": 1,
      "productId": 101,
      "sku": "MOU-001",
      "productName": "Mouse Gamer",
      "image": "https://images.unsplash.com/photo-1511467687858-23d96c32e4ae?auto=format&fit=crop&w=1200&q=80",
      "quantity": 2,
      "unitPrice": 89.90,
      "lineTotal": 179.80
    }
  ],
  "totals": {
    "subtotal": 179.80,
    "tax": 34.16,
    "shipping": 0,
    "total": 213.96
  },
  "createdAt": "2026-04-05T14:45:00"
}
```

---

# 7. Admin Products

> En `etapa13`, estos endpoints requieren usuario autenticado con rol `ADMIN`.

## 7.1 Crear producto
- **POST** `/api/v1/admin/products`
- **Auth requerida:** sí
- **Rol requerido:** `ADMIN`
- **Body:** JSON
- **Response:** `201 Created`
- **Errores relevantes:** `401 Unauthorized`, `403 Forbidden`

## 7.2 Actualizar producto
- **PUT** `/api/v1/admin/products/{id}`
- **Auth requerida:** sí
- **Rol requerido:** `ADMIN`
- **Body:** JSON
- **Response:** `200 OK`
- **Errores relevantes:** `401 Unauthorized`, `403 Forbidden`

## 7.3 Eliminar producto
- **DELETE** `/api/v1/admin/products/{id}`
- **Auth requerida:** sí
- **Rol requerido:** `ADMIN`
- **Body:** no
- **Response:** `204 No Content`
- **Errores relevantes:** `401 Unauthorized`, `403 Forbidden`

### Body crear/actualizar producto
```json
{
  "categoryId": 1,
  "sku": "MOU-001",
  "name": "Mouse Gamer",
  "description": "Mouse Gamer descripcion de prueba",
  "image": "https://images.unsplash.com/photo-1511467687858-23d96c32e4ae?auto=format&fit=crop&w=1200&q=80",
  "price": 89.90,
  "stockQty": 20,
  "isActive": true
}
```

### Reglas crear/actualizar producto
- `image` es opcional
- cuando se informa `image`, el backend la persiste y la devuelve en `POST`, `PUT`, `GET /api/v1/products` y `GET /api/v1/products/{id}`

---

# 8. Admin Users

> En `etapa15`, estos endpoints requieren usuario autenticado con rol `ADMIN`.

## 8.1 Crear usuario
- **POST** `/api/v1/admin/users`
- **Auth requerida:** sí
- **Rol requerido:** `ADMIN`
- **Body:** JSON
- **Response:** `201 Created`
- **Errores relevantes:** `400 Validation Error`, `401 Unauthorized`, `403 Forbidden`, `409 Duplicate Resource`

### Body crear usuario
```json
{
  "email": "new.user@cesde.edu.co",
  "password": "secret123",
  "firstName": "Nuevo",
  "lastName": "Usuario",
  "phone": "3001234567",
  "role": "CUSTOMER",
  "status": "ACTIVE"
}
```

### Reglas crear usuario
- `password` es obligatoria solo en creación
- `role` acepta `ADMIN` o `CUSTOMER`
- `status` acepta `ACTIVE` o `INACTIVE`
- `email` debe ser único

## 8.2 Listar usuarios
- **GET** `/api/v1/admin/users`
- **Auth requerida:** sí
- **Rol requerido:** `ADMIN`
- **Body:** no
- **Response:** `200 OK`
- **Errores relevantes:** `401 Unauthorized`, `403 Forbidden`

### Response listar usuarios
```json
[
  {
    "id": 1,
    "email": "admin.demo@pps.com",
    "firstName": "Admin",
    "lastName": "Demo",
    "fullName": "Admin Demo",
    "role": "ADMIN",
    "phone": "3000000001",
    "status": "ACTIVE",
    "createdAt": "2026-04-05T14:30:00"
  },
  {
    "id": 2,
    "email": "customer.demo@pps.com",
    "firstName": "Customer",
    "lastName": "Demo",
    "fullName": "Customer Demo",
    "role": "CUSTOMER",
    "phone": "3000000002",
    "status": "INACTIVE",
    "createdAt": "2026-04-05T14:35:00"
  }
]
```

## 8.3 Obtener usuario por ID
- **GET** `/api/v1/admin/users/{id}`
- **Auth requerida:** sí
- **Rol requerido:** `ADMIN`
- **Body:** no
- **Response:** `200 OK`
- **Errores relevantes:** `401 Unauthorized`, `403 Forbidden`, `404 Resource Not Found`

## 8.4 Actualizar usuario
- **PUT** `/api/v1/admin/users/{id}`
- **Auth requerida:** sí
- **Rol requerido:** `ADMIN`
- **Body:** JSON
- **Response:** `200 OK`
- **Errores relevantes:** `400 Validation Error`, `401 Unauthorized`, `403 Forbidden`, `404 Resource Not Found`, `409 Duplicate Resource`

### Body actualizar usuario
```json
{
  "email": "updated.user@cesde.edu.co",
  "firstName": "Usuario",
  "lastName": "Actualizado",
  "phone": "3017654321",
  "role": "ADMIN",
  "status": "ACTIVE"
}
```

### Reglas actualizar usuario
- este CRUD no cambia contraseña
- `role` acepta `ADMIN` o `CUSTOMER`
- `status` acepta `ACTIVE` o `INACTIVE`
- si se cambia `email`, debe seguir siendo único

## 8.5 Eliminar usuario
- **DELETE** `/api/v1/admin/users/{id}`
- **Auth requerida:** sí
- **Rol requerido:** `ADMIN`
- **Body:** no
- **Response:** `204 No Content`
- **Errores relevantes:** `401 Unauthorized`, `403 Forbidden`, `404 Resource Not Found`

### Regla eliminar usuario
- el delete es **baja lógica**: el usuario no se borra físicamente
- el backend cambia `status` a `INACTIVE`
- un usuario `INACTIVE` no puede iniciar sesión

### Shape de usuario admin
```json
{
  "id": 3,
  "email": "updated.user@cesde.edu.co",
  "firstName": "Usuario",
  "lastName": "Actualizado",
  "fullName": "Usuario Actualizado",
  "role": "ADMIN",
  "phone": "3017654321",
  "status": "ACTIVE",
  "createdAt": "2026-04-05T15:00:00"
}
```

---

## Flujos principales ya soportados

1. crear sesión guest
2. consultar carrito guest
3. agregar y actualizar items
4. registrar usuario usando `guestCartId`
5. consultar `auth/me`
6. crear y administrar direcciones
7. fusionar carrito guest adicional
8. hacer checkout
9. listar órdenes
10. consultar detalle de orden
11. consultar catálogo público
12. crear/editar/desactivar productos
13. crear/listar/editar/desactivar usuarios desde admin

---

## Pendientes fuera de alcance de etapa12

1. endurecimiento avanzado de autorización y permisos finos por recurso
2. Spring Security completa
3. CORS y hardening final
4. OpenAPI / Swagger
5. pagos reales
6. refresh token / revocación avanzada


