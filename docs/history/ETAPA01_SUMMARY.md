# Resumen Etapa 01 - Entidades Básicas (POJOs sin relaciones)

## ✅ Completado - Fecha: 03 de febrero de 2026

### 📦 Estructura del Proyecto

```
src/main/java/co/edu/cesde/pps/
├── enums/           (4 archivos)
│   ├── AddressType.java
│   ├── CartStatus.java
│   ├── Currency.java
│   └── UserStatus.java
│
└── model/           (14 archivos)
    ├── Address.java
    ├── Cart.java
    ├── CartItem.java
    ├── Category.java
    ├── Order.java
    ├── OrderItem.java
    ├── OrderStatus.java
    ├── Payment.java
    ├── PaymentMethod.java
    ├── PaymentStatus.java
    ├── Product.java
    ├── Role.java
    ├── User.java
    └── UserSession.java
```

**Total: 18 archivos Java creados**

---

## 📋 Entidades Implementadas

### 1️⃣ Entidades Catálogo (4)
| Entidad | Descripción | Campos clave |
|---------|-------------|--------------|
| `Role` | Roles de usuario (admin, customer) | roleId, name, description |
| `OrderStatus` | Estados de orden (pending, paid, shipped) | orderStatusId, name |
| `PaymentStatus` | Estados de pago (pending, approved, rejected) | paymentStatusId, name |
| `PaymentMethod` | Métodos de pago (credit_card, bank_transfer) | paymentMethodId, name |

### 2️⃣ Entidades de Usuario (3)
| Entidad | Descripción | Campos clave |
|---------|-------------|--------------|
| `User` | Usuario registrado del sistema | userId, roleId, email (UNIQUE), passwordHash, firstName, lastName, phone, status, createdAt |
| `Address` | Direcciones de envío/facturación | addressId, userId, type, line1, city, state, country, postalCode, isDefault |
| `UserSession` | Sesiones (incluye invitados) | sessionId, userId (NULLABLE), sessionToken (UNIQUE), createdAt, expiresAt |

### 3️⃣ Entidades de Catálogo de Productos (2)
| Entidad | Descripción | Campos clave |
|---------|-------------|--------------|
| `Category` | Categorías con jerarquía | categoryId, parentId (NULLABLE), name, slug (UNIQUE) |
| `Product` | Productos vendibles | productId, categoryId, sku (UNIQUE), name, description, price (BigDecimal), stockQty, isActive, createdAt |

### 4️⃣ Entidades de Carrito (2)
| Entidad | Descripción | Campos clave |
|---------|-------------|--------------|
| `Cart` | Contenedor del carrito | cartId, userId (NULLABLE), sessionId, status, createdAt, updatedAt |
| `CartItem` | Items del carrito | cartItemId, cartId, productId, quantity, unitPrice (BigDecimal), addedAt |

### 5️⃣ Entidades de Órdenes y Pagos (3)
| Entidad | Descripción | Campos clave |
|---------|-------------|--------------|
| `Order` | Orden de compra | orderId, orderNumber (UNIQUE), userId (NOT NULL), orderStatusId, shippingAddressId, billingAddressId, subtotal, tax, shippingCost, total (BigDecimal), createdAt |
| `OrderItem` | Items de la orden | orderItemId, orderId, productId, quantity, unitPrice, lineTotal (BigDecimal) |
| `Payment` | Transacciones de pago | paymentId, orderId, paymentMethodId, paymentStatusId, amount (BigDecimal), currency, providerReference, paidAt |

---

## 🎯 Enumeraciones Implementadas (4)

| Enum | Valores | Uso |
|------|---------|-----|
| `CartStatus` | OPEN, CONVERTED, ABANDONED | Estado del carrito |
| `AddressType` | SHIPPING, BILLING | Tipo de dirección |
| `UserStatus` | ACTIVE, INACTIVE, BLOCKED | Estado del usuario |
| `Currency` | USD, COP, EUR | Moneda del pago |

---

## ✨ Características Implementadas

### ✅ Nomenclatura de IDs específicos
- Todos los IDs usan nombres descriptivos: `roleId`, `userId`, `productId`, etc.
- Facilita mapeo futuro a JPA con `@Column(name="role_id")`

### ✅ Tipos de datos apropiados
- **`LocalDateTime`** para fechas: `createdAt`, `updatedAt`, `expiresAt`, `paidAt`, `addedAt`
- **`BigDecimal`** para valores monetarios: `price`, `amount`, `subtotal`, `tax`, `shippingCost`, `total`, `unitPrice`, `lineTotal`
- Previene errores de redondeo y pérdida de precisión

### ✅ Constructores múltiples
Cada entidad incluye:
1. **Constructor vacío** (requerido para JPA futuro)
2. **Constructor con campos obligatorios**
3. **Constructor completo** (excepto ID y timestamps autogenerados)

### ✅ Validaciones básicas en setters
- Precios y montos `>= 0` (excepto `Payment.amount` que permite negativos para reembolsos)
- Cantidades `> 0`
- Ejemplos:
  ```java
  if (price.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Price cannot be negative");
  }
  ```

### ✅ Métodos equals/hashCode basados en ID
- Implementados con `Objects.equals()` y `Objects.hash()`
- Previenen problemas de comparación en colecciones
- Evitan referencias circulares

### ✅ toString() sin navegación a objetos
- Solo muestran IDs y valores primitivos
- Previenen `StackOverflowError` en debugging
- Ejemplo: muestran `userId` en lugar de objeto `user` completo

### ✅ Métodos helper útiles
| Clase | Método | Descripción |
|-------|--------|-------------|
| `UserSession` | `isGuestSession()` | Verifica si userId == null |
| `UserSession` | `isExpired()` | Verifica si expiró la sesión |
| `Category` | `isRootCategory()` | Verifica si parentId == null |
| `Product` | `isAvailable()` | Verifica isActive && stockQty > 0 |
| `Cart` | `isGuestCart()` | Verifica si userId == null |
| `Cart` | `isOpen()` | Verifica si status == OPEN |
| `Cart` | `touch()` | Actualiza updatedAt |
| `CartItem` | `calculateSubtotal()` | unitPrice * quantity |
| `Order` | `calculateTotal()` | subtotal + tax + shippingCost |
| `OrderItem` | `calculateLineTotal()` | unitPrice * quantity |
| `Payment` | `isPaid()` | Verifica si paidAt != null |
| `Payment` | `isRefund()` | Verifica si amount < 0 |

---

## 📝 Documentación Especial

### 🔄 Política de Cart Merge
La clase `Cart` incluye **documentación Javadoc extensa** (60+ líneas) explicando:
- Escenario del merge (invitado se registra)
- Proceso detallado del merge (6 pasos)
- Resolución de conflictos de cantidad y precio
- Marcado de carrito invitado como ABANDONED
- Referencia a `documents_external/er_model_documentation.md` - Sección 5

### 📦 Restricciones UNIQUE documentadas
- `CartItem`: Javadoc documenta UNIQUE(cartId, productId)
- `OrderItem`: Javadoc documenta UNIQUE(orderId, productId)
- Implementación SQL será en Etapa 3 con anotaciones JPA

---

## 🎨 Características de Diseño

### Sin relaciones entre objetos (solo IDs)
- Todos los campos FK son `Long`: `roleId`, `userId`, `categoryId`, etc.
- **NO** hay objetos relacionados en esta etapa: `Role role`, `User user`, etc.
- Las relaciones se establecerán en **Etapa 02**

### Preparación para serialización
- `toString()` implementado sin navegación evita referencias circulares
- `equals()/hashCode()` basados solo en ID evitan bucles infinitos
- Preparado para futura serialización JSON en APIs REST

### Congelación de precios
- `CartItem.unitPrice`: precio al agregar al carrito
- `OrderItem.unitPrice`: precio histórico al comprar
- Mantiene consistencia si los precios cambian

---

## 🔧 Compilación

```bash
mvn clean compile
```

**Resultado:** ✅ BUILD SUCCESS (18 source files compiled)

---

## 📦 Commits realizados

```bash
git log --oneline
```

1. `feat: add 4 enums - CartStatus, AddressType, UserStatus, Currency`
2. `feat: add 4 catalog entities - Role, OrderStatus, PaymentStatus, PaymentMethod`
3. `feat: add 3 user entities - User, Address, UserSession with validation`
4. `feat: add 2 product catalog entities - Category with hierarchy, Product with BigDecimal price`
5. `feat: add 2 cart entities - Cart with merge policy documentation, CartItem with frozen price`
6. `feat: add 3 order/payment entities - Order with totals, OrderItem, Payment with multiple currencies`

---

## 🌿 Estado de Git

- **Rama actual:** `etapa01`
- **Commits:** 6 commits granulares
- **Estado:** Pusheado a GitHub en `origin/etapa01`
- **Repositorio:** `https://github.com/lgoenaga/product-purchasing-system`

---

## 🚀 Próximos Pasos (Etapa 02)

La **Etapa 02** implementará:

1. ✅ Crear rama `etapa02` desde `etapa01`
2. ✅ Cambiar campos `Long xxxId` por objetos del dominio
   - Ejemplo: `Long roleId` → `Role role`
3. ✅ Agregar colecciones bidireccionales
   - `User.addresses` → `List<Address>`
   - `Cart.items` → `List<CartItem>`
   - `Order.items` → `List<OrderItem>`
   - `Category.subcategories` → `List<Category>`
4. ✅ Inicializar colecciones en constructor vacío
5. ✅ Implementar métodos de negocio
   - `Cart.addItem()`, `Cart.removeItem()`, `Cart.calculateTotal()`
   - `Order.addItem()`, `Order.calculateTotal()`
   - `User.addAddress()`, `User.getDefaultAddress()`
   - `Category.addSubcategory()`
6. ✅ Implementar métodos helper bidireccionales
   - Mantener consistencia en ambos lados de la relación
7. ✅ Validaciones avanzadas en setters

---

## 📚 Referencias

- **Modelo E-R completo:** [`documents_external/er_model_documentation.md`](../documents_external/er_model_documentation.md)
- **Diagrama visual:** [`documents_external/modelo_er_store.png`](../documents_external/modelo_er_store.png)
- **README del proyecto:** [`README.md`](../README.md)

---

## ✅ Checklist Etapa 01

- [x] Estructura de paquetes `co.edu.cesde.pps.model` y `.enums`
- [x] 4 enumeraciones implementadas
- [x] 4 entidades catálogo implementadas
- [x] 3 entidades de usuario implementadas
- [x] 2 entidades de catálogo de productos implementadas
- [x] 2 entidades de carrito implementadas
- [x] 3 entidades de órdenes/pagos implementadas
- [x] Constructores vacío/obligatorio/completo en todas las entidades
- [x] Getters/Setters en todas las entidades
- [x] equals/hashCode basados en ID
- [x] toString() sin navegación a objetos
- [x] Validaciones básicas en setters críticos
- [x] Métodos helper útiles
- [x] Documentación Javadoc completa
- [x] Política de Cart Merge documentada
- [x] Compilación exitosa
- [x] Commits granulares
- [x] Push a GitHub

---

**Etapa 01 completada exitosamente** ✨

Autor: Luis Goenaga  
Proyecto: Product Purchasing System  
Institución: CESDE - Backend II  
Fecha: 03 de febrero de 2026
