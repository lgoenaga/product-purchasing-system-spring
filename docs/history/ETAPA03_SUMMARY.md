# Resumen Etapa 03 - Exceptions y Utilities (Separación de Responsabilidades)

## ✅ Completado - Fecha: 03 de febrero de 2026

### 📦 Estructura del Proyecto

```
src/main/java/co/edu/cesde/pps/
├── enums/           (4 archivos - sin cambios)
│   ├── AddressType.java
│   ├── CartStatus.java
│   ├── Currency.java
│   └── UserStatus.java
│
├── exception/       (7 archivos - NUEVO)
│   ├── BusinessException.java
│   ├── CartMergeException.java
│   ├── DuplicateEntityException.java
│   ├── EntityNotFoundException.java
│   ├── InsufficientStockException.java
│   ├── InvalidCartStateException.java
│   └── ValidationException.java
│
├── model/           (14 archivos - refactorizados)
│   ├── Address.java
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Category.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── OrderStatus.java
│   ├── Payment.java
│   ├── PaymentMethod.java
│   ├── PaymentStatus.java
│   ├── Product.java
│   ├── Role.java
│   ├── User.java
│   └── UserSession.java
│
└── util/            (6 archivos - NUEVO)
    ├── CalculationUtils.java
    ├── Constants.java
    ├── DateTimeUtils.java
    ├── MoneyUtils.java
    ├── StringUtils.java
    └── ValidationUtils.java
```

**Total: 31 archivos Java**
- **7 nuevas clases de excepciones**
- **6 nuevas clases de utilidades**
- **14 entidades refactorizadas**

---

## 🎯 Objetivos de la Etapa 03

Esta etapa prepara el proyecto para la integración futura con JPA/Hibernate mediante:

1. **Separación de responsabilidades**: Entidades como POJOs puros
2. **Manejo centralizado de errores**: Jerarquía de excepciones de negocio
3. **Reutilización de código**: Utilidades compartidas
4. **Preparación para capas superiores**: Service, Repository, DTO (etapas 04-06)

---

## 🔥 Nuevas Excepciones Personalizadas

### Jerarquía de Excepciones

```
RuntimeException
└── BusinessException (abstracta base)
    ├── EntityNotFoundException
    ├── ValidationException
    ├── CartMergeException
    ├── InsufficientStockException
    ├── DuplicateEntityException
    └── InvalidCartStateException
```

### 1️⃣ BusinessException (Base)

**Propósito**: Clase padre para todas las excepciones de negocio del dominio.

```java
throw new BusinessException("General business error");
```

**Constructores**:
- `BusinessException(String message)`
- `BusinessException(String message, Throwable cause)`
- `BusinessException(Throwable cause)`

---

### 2️⃣ EntityNotFoundException

**Propósito**: Entidad solicitada no existe en el sistema.

**Casos de uso**:
- Buscar usuario por ID inexistente
- Buscar producto por SKU inexistente
- Buscar orden por número inexistente

**Ejemplo**:
```java
throw new EntityNotFoundException("User", 123L);
// Mensaje: "User not found with criteria: 123"

throw new EntityNotFoundException("Product", "SKU-12345");
// Mensaje: "Product not found with criteria: SKU-12345"
```

**Propiedades**:
- `entityType`: Tipo de entidad ("User", "Product", etc.)
- `searchCriteria`: Criterio usado (ID, email, SKU, etc.)

---

### 3️⃣ ValidationException

**Propósito**: Violación de reglas de validación de datos.

**Casos de uso**:
- Precio negativo
- Email con formato inválido
- Cantidad fuera de rango
- Campos requeridos nulos

**Ejemplo**:
```java
throw new ValidationException("price", -10.50, "cannot be negative");
// Mensaje: "Validation failed for field 'price' with value '-10.5': cannot be negative"

throw new ValidationException("Email format is invalid");
```

**Propiedades**:
- `fieldName`: Nombre del campo que falló
- `invalidValue`: Valor que causó el error

---

### 4️⃣ CartMergeException

**Propósito**: Error durante fusión de carritos (invitado → registrado).

**Casos de uso**:
- Carritos en estados incompatibles
- Conflictos irresolubles de productos
- Errores de consistencia durante merge

**Ejemplo**:
```java
throw new CartMergeException(cartIdGuest, cartIdUser, "Cannot merge ABANDONED cart");
// Mensaje: "Cart merge failed between guest cart 1 and user cart 2: Cannot merge ABANDONED cart"
```

**Propiedades**:
- `guestCartId`: ID del carrito de invitado
- `userCartId`: ID del carrito del usuario

---

### 5️⃣ InsufficientStockException

**Propósito**: Stock insuficiente para operación solicitada.

**Casos de uso**:
- Agregar producto al carrito sin stock
- Procesar orden con productos agotados
- Actualizar cantidad superior al disponible

**Ejemplo**:
```java
throw new InsufficientStockException(productId, "SKU-123", 10, 5);
// Mensaje: "Insufficient stock for product SKU-123 (ID: 1). Requested: 10, Available: 5"
```

**Propiedades**:
- `productId`: ID del producto
- `productSku`: SKU del producto
- `requestedQuantity`: Cantidad solicitada
- `availableStock`: Stock disponible

---

### 6️⃣ DuplicateEntityException

**Propósito**: Violación de restricción de unicidad (UNIQUE).

**Casos de uso**:
- Email de usuario duplicado
- SKU de producto duplicado
- Slug de categoría duplicado

**Ejemplo**:
```java
throw new DuplicateEntityException("User", "email", "user@example.com");
// Mensaje: "User already exists with email: user@example.com"
```

**Propiedades**:
- `entityType`: Tipo de entidad
- `fieldName`: Campo único violado
- `duplicateValue`: Valor duplicado

---

### 7️⃣ InvalidCartStateException

**Propósito**: Operación inválida para estado actual del carrito.

**Casos de uso**:
- Agregar items a carrito CONVERTED o ABANDONED
- Hacer checkout de carrito no OPEN
- Modificar carrito ya convertido

**Ejemplo**:
```java
throw new InvalidCartStateException(cartId, CartStatus.CONVERTED, CartStatus.OPEN, "add item");
// Mensaje: "Cannot perform 'add item' on cart 1. Current state: CONVERTED, Required state: OPEN"
```

**Propiedades**:
- `cartId`: ID del carrito
- `currentState`: Estado actual
- `requiredState`: Estado requerido

---

## 🛠️ Clases de Utilidades

### 1️⃣ ValidationUtils

**Propósito**: Validaciones de datos del dominio.

**Métodos principales**:

| Método | Descripción | Lanza |
|--------|-------------|-------|
| `validateNotNull(value, field)` | Valida que no sea nulo | ValidationException |
| `validateNotBlank(value, field)` | Valida String no vacío | ValidationException |
| `validateNotEmpty(collection, field)` | Valida colección no vacía | ValidationException |
| `validatePositive(value, field)` | Valida > 0 (BigDecimal/Integer) | ValidationException |
| `validateNonNegative(value, field)` | Valida >= 0 | ValidationException |
| `validateRange(value, min, max, field)` | Valida rango | ValidationException |
| `validateMinLength(value, min, field)` | Valida longitud mínima | ValidationException |
| `validateMaxLength(value, max, field)` | Valida longitud máxima | ValidationException |
| `validateEmail(email, field)` | Valida formato email | ValidationException |
| `validatePhone(phone, field)` | Valida formato teléfono | ValidationException |
| `validateSku(sku, field)` | Valida formato SKU | ValidationException |
| `validateSlug(slug, field)` | Valida formato slug | ValidationException |

**Ejemplo de uso**:
```java
// En Product.setPrice()
ValidationUtils.validateNonNegative(price, "price");

// En CartItem.setQuantity()
ValidationUtils.validatePositive(quantity, "quantity");

// En User (futuro)
ValidationUtils.validateEmail(email, "email");
ValidationUtils.validateMinLength(password, 8, "password");
```

---

### 2️⃣ MoneyUtils

**Propósito**: Operaciones con valores monetarios usando BigDecimal.

**Características**:
- Escala por defecto: 2 decimales
- Redondeo: HALF_EVEN (bankers rounding)
- Previene errores de precisión

**Métodos principales**:

| Método | Descripción | Retorno |
|--------|-------------|---------|
| `of(double/long/String)` | Crea BigDecimal normalizado | BigDecimal |
| `normalize(amount)` | Normaliza a escala 2 | BigDecimal |
| `add(a, b)` | Suma | BigDecimal |
| `subtract(a, b)` | Resta | BigDecimal |
| `multiply(amount, quantity)` | Multiplica | BigDecimal |
| `divide(amount, divisor)` | Divide | BigDecimal |
| `percentage(amount, percent)` | Calcula porcentaje | BigDecimal |
| `isPositive(amount)` | Verifica > 0 | boolean |
| `isNegative(amount)` | Verifica < 0 | boolean |
| `isZero(amount)` | Verifica == 0 | boolean |
| `max(a, b)` | Retorna mayor | BigDecimal |
| `min(a, b)` | Retorna menor | BigDecimal |
| `formatUSD(amount)` | Formatea en USD | String |
| `formatCOP(amount)` | Formatea en COP | String |
| `formatEUR(amount)` | Formatea en EUR | String |

**Ejemplo de uso**:
```java
BigDecimal price = MoneyUtils.of(19.99);
BigDecimal total = MoneyUtils.multiply(price, 3);
BigDecimal tax = MoneyUtils.percentage(total, BigDecimal.valueOf(19));
String formatted = MoneyUtils.formatUSD(total); // "$59.97"
```

---

### 3️⃣ DateTimeUtils

**Propósito**: Operaciones con fechas y tiempos.

**Formateadores**:
- `DEFAULT_FORMATTER`: "dd/MM/yyyy HH:mm:ss"
- `DATE_ONLY_FORMATTER`: "dd/MM/yyyy"
- `ISO_FORMATTER`: ISO 8601

**Métodos principales**:

| Método | Descripción | Retorno |
|--------|-------------|---------|
| `isPast(dateTime)` | Verifica si está en el pasado | boolean |
| `isFuture(dateTime)` | Verifica si está en el futuro | boolean |
| `isExpired(expirationDate)` | Verifica si expiró | boolean |
| `isBetween(date, start, end)` | Verifica si está en rango | boolean |
| `daysBetween(start, end)` | Días de diferencia | long |
| `hoursBetween(start, end)` | Horas de diferencia | long |
| `minutesBetween(start, end)` | Minutos de diferencia | long |
| `addDays(date, days)` | Agrega días | LocalDateTime |
| `addHours(date, hours)` | Agrega horas | LocalDateTime |
| `format(date)` | Formatea fecha | String |
| `formatISO(date)` | Formatea ISO 8601 | String |
| `parse(dateString)` | Parsea fecha | LocalDateTime |
| `parseISO(dateString)` | Parsea ISO 8601 | LocalDateTime |
| `now()` | Fecha actual | LocalDateTime |
| `compare(date1, date2)` | Compara fechas | int |

**Ejemplo de uso**:
```java
if (DateTimeUtils.isExpired(session.getExpiresAt())) {
    throw new InvalidSessionException("Session expired");
}

long hours = DateTimeUtils.hoursBetween(cart.getCreatedAt(), LocalDateTime.now());
if (hours > Constants.CART_ABANDONMENT_HOURS) {
    cart.setStatus(CartStatus.ABANDONED);
}
```

---

### 4️⃣ StringUtils

**Propósito**: Manipulación y transformación de texto.

**Métodos principales**:

| Método | Descripción | Retorno |
|--------|-------------|---------|
| `isBlank(str)` | Verifica si es null/vacío | boolean |
| `isNotBlank(str)` | Verifica si tiene contenido | boolean |
| `slugify(text)` | Genera slug URL-friendly | String |
| `capitalize(str)` | Capitaliza primera letra | String |
| `capitalizeWords(str)` | Capitaliza cada palabra | String |
| `truncate(str, maxLength)` | Trunca a longitud máxima | String |
| `truncateWithEllipsis(str, max)` | Trunca con "..." | String |
| `sanitize(str)` | Remueve caracteres peligrosos | String |
| `normalizeWhitespace(str)` | Normaliza espacios | String |
| `padLeft(str, length, char)` | Padding izquierda | String |
| `padRight(str, length, char)` | Padding derecha | String |
| `defaultIfBlank(str, default)` | Valor por defecto | String |
| `maskExceptLast(str, visible, char)` | Enmascara texto | String |
| `excerpt(text, maxLength)` | Genera extracto | String |

**Ejemplo de uso**:
```java
// Generar slug para categoría
String slug = StringUtils.slugify("Laptops Gaming 15\""); // "laptops-gaming-15"

// Enmascarar tarjeta de crédito
String masked = StringUtils.maskExceptLast("1234567890123456", 4, '*'); // "************3456"

// Extracto de descripción
String preview = StringUtils.excerpt(product.getDescription(), 100);
```

---

### 5️⃣ CalculationUtils

**Propósito**: Cálculos de negocio del dominio.

**Métodos principales**:

| Método | Descripción | Retorno |
|--------|-------------|---------|
| `calculateCartItemSubtotal(price, qty)` | Subtotal item carrito | BigDecimal |
| `calculateOrderItemLineTotal(price, qty)` | Total línea orden | BigDecimal |
| `calculateCartTotal(itemSubtotals)` | Total carrito | BigDecimal |
| `calculateOrderTotal(sub, tax, ship)` | Total orden | BigDecimal |
| `calculateOrderSubtotal(lineTotals)` | Subtotal orden | BigDecimal |
| `calculateTax(subtotal, taxRate)` | Impuestos | BigDecimal |
| `calculateShippingCost(subtotal, zone)` | Costo envío | BigDecimal |
| `calculateDiscount(subtotal, percent)` | Descuento | BigDecimal |
| `applyDiscount(subtotal, discount)` | Aplica descuento | BigDecimal |
| `calculateWeightedAveragePrice(...)` | Precio promedio ponderado | BigDecimal |
| `hasEnoughStock(available, requested)` | Verifica stock | boolean |
| `calculateNewStock(current, sold)` | Nuevo stock | Integer |
| `calculatePercentageOfTotal(value, total)` | Porcentaje | BigDecimal |

**Ejemplo de uso**:
```java
// En CartItem.calculateSubtotal()
return CalculationUtils.calculateCartItemSubtotal(unitPrice, quantity);

// En Cart.calculateTotal()
List<BigDecimal> subtotals = items.stream()
    .map(CartItem::calculateSubtotal)
    .collect(Collectors.toList());
return CalculationUtils.calculateCartTotal(subtotals);

// En Order.calculateTotal()
return CalculationUtils.calculateOrderTotal(subtotal, tax, shippingCost);
```

---

### 6️⃣ Constants

**Propósito**: Constantes centralizadas del sistema.

**Categorías de constantes**:

#### Sesiones
- `DEFAULT_SESSION_EXPIRATION_HOURS = 24`
- `USER_SESSION_EXPIRATION_HOURS = 168` (7 días)

#### Carritos
- `CART_ABANDONMENT_HOURS = 48`
- `MAX_CART_ITEM_QUANTITY = 99`
- `MIN_CART_ITEM_QUANTITY = 1`

#### Productos
- `LOW_STOCK_THRESHOLD = 10`
- `MIN_STOCK_FOR_SALE = 1`
- `MIN_PRODUCT_PRICE = 0.01`
- `MAX_PRODUCT_PRICE = 999999.99`

#### Órdenes
- `DEFAULT_TAX_RATE = 19` (%)
- `FREE_SHIPPING_THRESHOLD = 100.00`
- `BASE_SHIPPING_COST = 5.00`
- `MIN_ORDER_AMOUNT = 1.00`
- `MAX_ORDER_AMOUNT = 999999.99`

#### Usuarios
- `MIN_PASSWORD_LENGTH = 8`
- `MAX_PASSWORD_LENGTH = 100`
- `MIN_NAME_LENGTH = 2`
- `MAX_NAME_LENGTH = 100`
- `MAX_EMAIL_LENGTH = 255`
- `MAX_ADDRESSES_PER_USER = 10`

#### Paginación
- `DEFAULT_PAGE_SIZE = 20`
- `MAX_PAGE_SIZE = 100`

#### Pagos
- `PAYMENT_TIMEOUT_MINUTES = 30`
- `MAX_PAYMENT_RETRIES = 3`

#### Formatos
- `ORDER_NUMBER_PREFIX = "ORD-"`
- `ORDER_NUMBER_UNIQUE_LENGTH = 6`
- `SKU_PREFIX = "SKU-"`

#### Mensajes de Error
- `ERROR_NULL_VALUE`, `ERROR_NEGATIVE_VALUE`, etc.

#### Monedas
- `DEFAULT_CURRENCY = "USD"`
- `SECONDARY_CURRENCY = "COP"`

#### Roles y Estados
- `ROLE_ADMIN = "ADMIN"`
- `ROLE_CUSTOMER = "CUSTOMER"`
- `DEFAULT_ROLE_ID = 2L`
- `ORDER_STATUS_PENDING_ID = 1L`
- `PAYMENT_STATUS_PENDING_ID = 1L`
- `PAYMENT_METHOD_CREDIT_CARD_ID = 1L`

**Ejemplo de uso**:
```java
if (product.getStockQty() < Constants.LOW_STOCK_THRESHOLD) {
    sendLowStockAlert(product);
}

if (orderSubtotal.compareTo(Constants.FREE_SHIPPING_THRESHOLD) >= 0) {
    shippingCost = BigDecimal.ZERO;
}
```

---

## 🔄 Refactorización de Entidades del Modelo

### Cambios Aplicados

#### ❌ Removido de TODAS las entidades:
1. **Métodos bidireccionales**:
   - `addItem()`, `removeItem()` (Cart, Order)
   - `addAddress()`, `removeAddress()` (User)
   - `addSubcategory()`, `removeSubcategory()` (Category)
   
2. **Métodos con efectos secundarios**:
   - `touch()` en Cart (actualizar updatedAt)

3. **Lógica de cálculo inline**:
   - Loops, streams, operaciones BigDecimal directas
   - Toda lógica movida a `CalculationUtils`

4. **Validaciones con IllegalArgumentException**:
   - Reemplazadas por `ValidationUtils` + `ValidationException`

#### ✅ Mantenido en entidades:
1. **Métodos de consulta sin efectos secundarios**:
   - `isGuestCart()`, `isOpen()` (Cart)
   - `getFullName()`, `getDefaultAddress()` (User)
   - `isRootCategory()` (Category)
   - `isAvailable()` (Product)
   - `isPaid()`, `isRefund()` (Payment)

2. **Getters y Setters estándar**
3. **Constructores múltiples**
4. **equals(), hashCode(), toString()`**

---

### Comparación: Antes vs Después

#### Ejemplo: Cart.java

**Antes (Etapa 02)**:
```java
public void addItem(CartItem item) {
    if (item != null && !this.items.contains(item)) {
        this.items.add(item);
        item.setCart(this);
        this.touch();
    }
}

public void touch() {
    this.updatedAt = LocalDateTime.now();
}

public BigDecimal calculateTotal() {
    return items.stream()
            .map(CartItem::calculateSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
}
```

**Después (Etapa 03)**:
```java
// Métodos bidireccionales removidos - se moverán a CartService en etapa 05

// Método de consulta con lógica delegada
public BigDecimal calculateTotal() {
    List<BigDecimal> subtotals = items.stream()
        .map(CartItem::calculateSubtotal)
        .collect(Collectors.toList());
    return CalculationUtils.calculateCartTotal(subtotals);
}
```

---

#### Ejemplo: Product.java

**Antes (Etapa 02)**:
```java
public void setPrice(BigDecimal price) {
    if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
        throw new IllegalArgumentException("Price cannot be negative");
    }
    this.price = price;
}
```

**Después (Etapa 03)**:
```java
public void setPrice(BigDecimal price) {
    ValidationUtils.validateNonNegative(price, "price");
    this.price = price;
}
```

---

#### Ejemplo: CartItem.java

**Antes (Etapa 02)**:
```java
public void setQuantity(Integer quantity) {
    if (quantity != null && quantity <= 0) {
        throw new IllegalArgumentException("Quantity must be greater than 0");
    }
    this.quantity = quantity;
}

public BigDecimal calculateSubtotal() {
    if (unitPrice == null || quantity == null) {
        return BigDecimal.ZERO;
    }
    return unitPrice.multiply(new BigDecimal(quantity));
}
```

**Después (Etapa 03)**:
```java
public void setQuantity(Integer quantity) {
    ValidationUtils.validatePositive(quantity, "quantity");
    this.quantity = quantity;
}

public BigDecimal calculateSubtotal() {
    return CalculationUtils.calculateCartItemSubtotal(unitPrice, quantity);
}
```

---

## 📊 Resumen de Transformaciones

| Aspecto | Etapa 02 | Etapa 03 |
|---------|----------|----------|
| **Validaciones** | `IllegalArgumentException` inline | `ValidationUtils` + `ValidationException` |
| **Cálculos** | Lógica inline en entidades | Delegado a `CalculationUtils` |
| **Métodos bidireccionales** | En entidades | ❌ Removidos (se moverán a service layer) |
| **Manejo de errores** | Excepciones genéricas | Jerarquía de excepciones específicas |
| **Constantes** | Valores mágicos | Centralizadas en `Constants` |
| **Operaciones monetarias** | BigDecimal directo | `MoneyUtils` con escala consistente |
| **Operaciones de fechas** | LocalDateTime directo | `DateTimeUtils` con helpers |
| **Manipulación de texto** | String directo | `StringUtils` con utilidades |
| **Tipo de entidades** | POJOs con lógica | POJOs puros (solo datos) |

---

## 🎓 Beneficios de la Refactorización

### 1️⃣ Separación de Responsabilidades (SRP)
- **Entidades**: Solo estructura de datos (getters, setters, constructores)
- **Validaciones**: Centralizadas en `ValidationUtils`
- **Cálculos**: Centralizados en `CalculationUtils`
- **Gestión bidireccional**: Se moverá a Service Layer (etapa 05)

### 2️⃣ Reutilización de Código (DRY)
- Validaciones reutilizables en múltiples entidades
- Cálculos compartidos entre Cart y Order
- Operaciones monetarias consistentes en todo el sistema
- Formateadores y parsers de fechas unificados

### 3️⃣ Mantenibilidad
- Cambiar lógica de cálculo en un solo lugar
- Agregar nuevas validaciones sin tocar entidades
- Actualizar constantes centralizadamente
- Modificar formatos sin dispersión

### 4️⃣ Testabilidad
- Clases de utilidades fáciles de testear (métodos estáticos)
- Entidades simples de instanciar para tests
- Excepciones con contexto claro para assertions
- Mocking más simple en futuras capas

### 5️⃣ Preparación para JPA
- Entidades limpias listas para anotaciones JPA
- Sin lógica que interfiera con proxies de Hibernate
- Métodos de consulta seguros para lazy loading
- Estructura compatible con EntityManager

### 6️⃣ Mensajes de Error Descriptivos
```java
// Antes
throw new IllegalArgumentException("Price cannot be negative");

// Después
throw new ValidationException("price", -10.50, "cannot be negative");
// Mensaje: "Validation failed for field 'price' with value '-10.5': cannot be negative"
```

### 7️⃣ Manejo de Errores Específico
```java
try {
    productService.addToCart(productId, quantity);
} catch (EntityNotFoundException e) {
    return ResponseEntity.notFound().build();
} catch (InsufficientStockException e) {
    return ResponseEntity.status(409).body(e.getMessage());
} catch (ValidationException e) {
    return ResponseEntity.badRequest().body(e.getMessage());
}
```

---

## 🔧 Compilación

```bash
mvn clean compile
```

**Resultado:** ✅ BUILD SUCCESS

**Warnings**: Solo métodos no usados (normal, se usarán en futuras etapas)

---

## 📦 Commits Realizados

```bash
git log --oneline etapa03
```

1. `feat: add custom exception hierarchy for business logic`
2. `feat: add utility classes for common operations`
3. `refactor: clean model entities and delegate logic to utilities`

---

## 🌿 Estado de Git

- **Rama actual:** `etapa03`
- **Rama base:** `etapa02`
- **Commits:** 3 commits incrementales
- **Estado:** Listo para push a GitHub
- **Repositorio:** `https://github.com/lgoenaga/product-purchasing-system`

---

## 🚀 Roadmap de Próximas Etapas

### 📌 Etapa 04 - DTO, Config y Logging
- **DTO (Data Transfer Objects)**: Clases para transferencia de datos
  - `UserDTO`, `ProductDTO`, `CartDTO`, `OrderDTO`
  - Separación entre modelo de dominio y API
  - Prevenir exposición de datos sensibles
  
- **Config**: Configuración del proyecto
  - `DatabaseConfig`: Propiedades de conexión BD
  - `AppConfig`: Configuración general de la aplicación
  - Variables de entorno para diferentes ambientes
  
- **Logging**: Sistema de logging
  - SLF4J API + Logback implementation
  - Configuración `logback.xml`
  - Logs en services y controllers

### 📌 Etapa 05 - Service Layer
- **Implementar capa de servicios**:
  - `UserService`: Gestión de usuarios y direcciones
  - `CartService`: Lógica de carrito (addItem, removeItem, merge)
  - `OrderService`: Proceso de checkout y gestión de órdenes
  - `ProductService`: Gestión de productos y stock
  - `CategoryService`: Gestión de categorías jerárquicas
  - `PaymentService`: Procesamiento de pagos

- **Transacciones**: Preparar anotaciones `@Transactional`
- **Lógica de negocio compleja**: 
  - Algoritmo de Cart Merge
  - Conversión Cart → Order
  - Gestión de stock con concurrencia
  
- **Uso de excepciones personalizadas** en toda la lógica

### 📌 Etapa 06 - Repository + JPA + Hibernate + MySQL
- **Anotaciones JPA en entidades**:
  - `@Entity`, `@Table`, `@Id`, `@GeneratedValue`
  - `@Column`, `@ManyToOne`, `@OneToMany`
  - `@Enumerated`, `@Temporal`
  - Estrategias Lazy/Eager loading
  
- **Interfaces de repositorio**:
  - Extender `JpaRepository<Entity, ID>`
  - Métodos de consulta personalizados
  - Query methods y `@Query`
  
- **Configuración Hibernate**:
  - `persistence.xml` o Spring Data JPA
  - Conexión a MySQL
  - Propiedades: dialect, ddl-auto, show-sql
  
- **Dependencias Maven**:
  - Hibernate Core 6.4.x
  - MySQL Connector 8.3.x
  - Lombok 1.18.x
  - Bean Validation API 3.0.x
  
- **Scripts de base de datos**:
  - Schema DDL
  - Data seed con catálogos

---

## 📚 Documentación

- **README.md** - Descripción del proyecto y cómo usar ramas
- **ETAPA01_SUMMARY.md** - Resumen de entidades básicas (POJOs)
- **ETAPA02_SUMMARY.md** - Resumen de relaciones y métodos de negocio
- **ETAPA03_SUMMARY.md** - Este documento (exceptions y utilities)
- **documents_external/er_model_documentation.md** - Modelo E-R completo

---

## ✅ Checklist Etapa 03

- [x] Crear rama `etapa03` desde `etapa02`
- [x] Crear jerarquía de excepciones personalizadas (7 clases)
- [x] Crear clases de utilidades (6 clases)
- [x] Refactorizar validaciones de entidades con `ValidationUtils`
- [x] Delegar cálculos a `CalculationUtils`
- [x] Remover métodos bidireccionales de entidades
- [x] Remover métodos con efectos secundarios
- [x] Mantener solo métodos de consulta en entidades
- [x] Actualizar documentación en entidades
- [x] Compilar sin errores: `mvn clean compile`
- [x] Realizar 3 commits incrementales
- [x] Documentar en `ETAPA03_SUMMARY.md`
- [ ] Push a GitHub (pendiente comando del usuario)

---

## 🎯 Lecciones Aprendidas

### ✅ Buenas Prácticas Aplicadas

1. **Single Responsibility Principle (SRP)**
   - Cada clase tiene una única responsabilidad
   - Entidades solo datos, Utils solo operaciones

2. **Don't Repeat Yourself (DRY)**
   - Código reutilizable centralizado
   - Sin duplicación de lógica de validación

3. **Fail Fast**
   - Validaciones tempranas con excepciones descriptivas
   - Errores detectados lo antes posible

4. **Separation of Concerns**
   - Modelo separado de lógica de negocio
   - Preparado para arquitectura en capas

5. **Consistent Error Handling**
   - Jerarquía clara de excepciones
   - Mensajes descriptivos con contexto

6. **Money Handling Best Practices**
   - BigDecimal para precisión
   - Escala y redondeo consistentes
   - Operaciones seguras

7. **Defensive Programming**
   - Validaciones exhaustivas
   - Manejo de nulls explícito
   - Prevención de estados inválidos

---

## 🔍 Ejemplos de Uso Completos

### Ejemplo 1: Validación de Producto

```java
// En ProductService (futuro - etapa 05)
public void createProduct(ProductDTO dto) {
    // Validaciones
    ValidationUtils.validateNotBlank(dto.getName(), "name");
    ValidationUtils.validateSku(dto.getSku(), "sku");
    ValidationUtils.validateNonNegative(dto.getPrice(), "price");
    ValidationUtils.validateNonNegative(dto.getStockQty(), "stockQty");
    
    // Verificar duplicado
    if (productRepository.existsBySku(dto.getSku())) {
        throw new DuplicateEntityException("Product", "sku", dto.getSku());
    }
    
    // Crear producto
    Product product = new Product(
        category,
        dto.getSku(),
        dto.getName(),
        dto.getDescription(),
        dto.getPrice(),
        dto.getStockQty(),
        true
    );
    
    productRepository.save(product);
}
```

### Ejemplo 2: Agregar Item al Carrito

```java
// En CartService (futuro - etapa 05)
public void addItemToCart(Long cartId, Long productId, Integer quantity) {
    // Buscar entidades
    Cart cart = cartRepository.findById(cartId)
        .orElseThrow(() -> new EntityNotFoundException("Cart", cartId));
    
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new EntityNotFoundException("Product", productId));
    
    // Validar estado del carrito
    if (cart.getStatus() != CartStatus.OPEN) {
        throw new InvalidCartStateException(
            cartId, cart.getStatus(), CartStatus.OPEN, "add item"
        );
    }
    
    // Validar disponibilidad del producto
    if (!product.getIsActive()) {
        throw new ValidationException("Product is not active");
    }
    
    // Validar stock
    if (!CalculationUtils.hasEnoughStock(product.getStockQty(), quantity)) {
        throw new InsufficientStockException(
            productId, product.getSku(), quantity, product.getStockQty()
        );
    }
    
    // Buscar item existente o crear nuevo
    CartItem item = cart.getItems().stream()
        .filter(i -> i.getProduct().getProductId().equals(productId))
        .findFirst()
        .orElse(null);
    
    if (item != null) {
        // Actualizar cantidad
        int newQuantity = item.getQuantity() + quantity;
        if (!CalculationUtils.hasEnoughStock(product.getStockQty(), newQuantity)) {
            throw new InsufficientStockException(
                productId, product.getSku(), newQuantity, product.getStockQty()
            );
        }
        item.setQuantity(newQuantity);
    } else {
        // Crear nuevo item
        item = new CartItem(cart, product, quantity, product.getPrice());
        cart.getItems().add(item);
        item.setCart(cart);
    }
    
    // Actualizar timestamp
    cart.setUpdatedAt(LocalDateTime.now());
    
    cartRepository.save(cart);
}
```

### Ejemplo 3: Calcular Total de Orden

```java
// En OrderService (futuro - etapa 05)
public BigDecimal calculateOrderTotals(Order order) {
    // Calcular subtotal
    List<BigDecimal> lineTotals = order.getItems().stream()
        .map(OrderItem::calculateLineTotal)
        .collect(Collectors.toList());
    
    BigDecimal subtotal = CalculationUtils.calculateOrderSubtotal(lineTotals);
    order.setSubtotal(subtotal);
    
    // Calcular impuestos
    BigDecimal tax = CalculationUtils.calculateTax(
        subtotal, Constants.DEFAULT_TAX_RATE
    );
    order.setTax(tax);
    
    // Calcular costo de envío
    BigDecimal shippingCost = CalculationUtils.calculateShippingCost(
        subtotal, shippingZone
    );
    order.setShippingCost(shippingCost);
    
    // Calcular total
    BigDecimal total = CalculationUtils.calculateOrderTotal(
        subtotal, tax, shippingCost
    );
    order.setTotal(total);
    
    return total;
}
```

### Ejemplo 4: Validar y Formatear Datos de Usuario

```java
// En UserService (futuro - etapa 05)
public void registerUser(UserRegistrationDTO dto) {
    // Validaciones
    ValidationUtils.validateEmail(dto.getEmail(), "email");
    ValidationUtils.validateMinLength(dto.getPassword(), 
        Constants.MIN_PASSWORD_LENGTH, "password");
    ValidationUtils.validateNotBlank(dto.getFirstName(), "firstName");
    ValidationUtils.validateNotBlank(dto.getLastName(), "lastName");
    
    if (dto.getPhone() != null) {
        ValidationUtils.validatePhone(dto.getPhone(), "phone");
    }
    
    // Verificar email duplicado
    if (userRepository.existsByEmail(dto.getEmail())) {
        throw new DuplicateEntityException("User", "email", dto.getEmail());
    }
    
    // Normalizar datos
    String normalizedEmail = dto.getEmail().toLowerCase().trim();
    String firstName = StringUtils.capitalizeWords(dto.getFirstName().trim());
    String lastName = StringUtils.capitalizeWords(dto.getLastName().trim());
    
    // Hash de contraseña (usando librería de seguridad)
    String passwordHash = passwordEncoder.encode(dto.getPassword());
    
    // Crear usuario
    User user = new User(
        defaultRole,
        normalizedEmail,
        passwordHash,
        firstName,
        lastName
    );
    
    if (dto.getPhone() != null) {
        user.setPhone(dto.getPhone().trim());
    }
    
    userRepository.save(user);
}
```

---

## 🎉 Conclusión

La **Etapa 03** ha transformado el proyecto de un modelo con lógica dispersa a una arquitectura limpia y organizada:

- ✅ **Excepciones personalizadas** para manejo robusto de errores
- ✅ **Utilidades reutilizables** para operaciones comunes
- ✅ **Entidades limpias** preparadas para JPA
- ✅ **Separación de responsabilidades** clara
- ✅ **Base sólida** para capas Service y Repository

El proyecto está ahora **listo para las etapas 04-06**, donde se implementarán:
- DTOs y configuración
- Capa de servicios con lógica de negocio compleja
- Persistencia con JPA/Hibernate/MySQL

---

**Autor:** Luis Goenaga  
**Proyecto:** Product Purchasing System - Backend II  
**Institución:** CESDE  
**Año:** 2026
