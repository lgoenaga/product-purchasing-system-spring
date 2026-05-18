# Resumen Etapa 04 - DTO y Config

## ✅ Completado - Fecha: 03 de febrero de 2026

### 📦 Estructura del Proyecto

```
src/main/java/co/edu/cesde/pps/
├── config/          (2 archivos - NUEVO)
│   ├── AppConfig.java
│   └── DatabaseConfig.java
│
├── dto/             (8 archivos - NUEVO)
│   ├── AddressDTO.java
│   ├── CartDTO.java
│   ├── CartItemDTO.java
│   ├── CategoryDTO.java
│   ├── OrderDTO.java
│   ├── OrderItemDTO.java
│   ├── ProductDTO.java
│   └── UserDTO.java
│
├── enums/           (4 archivos - sin cambios)
├── exception/       (7 archivos - sin cambios)
├── model/           (14 archivos - sin cambios)
└── util/            (6 archivos - sin cambios)
```

**Total: 41 archivos Java**
- **8 DTOs nuevos**
- **2 clases de configuración nuevas**
- **31 archivos previos**

---

## 🎯 Objetivos de la Etapa 04

Esta etapa prepara el proyecto para la implementación de la capa de servicios (Etapa 05) mediante:

1. **DTOs (Data Transfer Objects)**: Separar modelo de dominio de la API
2. **Configuración centralizada**: AppConfig y DatabaseConfig
3. **Preparación para JPA**: DatabaseConfig listo para Hibernate (Etapa 06)

**NOTA**: El sistema de logging (SLF4J + Logback) se agregará en la **Etapa 06** junto con Hibernate/JPA/MySQL.

---

## 📋 DTOs Implementados (8 clases)

### ¿Qué son los DTOs?

Los **Data Transfer Objects** son objetos diseñados específicamente para transferir datos entre capas de la aplicación. Se diferencian de las entidades del modelo en:

- ✅ **No exponen datos sensibles** (ej: passwordHash)
- ✅ **Estructura optimizada** para cada caso de uso
- ✅ **Previenen lazy loading exceptions** de Hibernate
- ✅ **Desacoplan la API** del modelo de dominio
- ✅ **Incluyen campos calculados** y formateados

---

### 1️⃣ UserDTO

**Propósito**: Transferencia de datos de usuario sin información sensible.

**Campos**:
```java
- userId: Long
- roleName: String (en lugar de objeto Role)
- email: String
- firstName: String
- lastName: String
- phone: String
- status: UserStatus
- createdAt: LocalDateTime
- fullName: String (calculado)
- addressesCount: Integer (agregado)
```

**Casos de uso**:
- Respuestas de API (no expone `passwordHash`)
- Registro de usuarios
- Actualización de perfil
- Listados de usuarios

**Ventaja principal**: No expone `passwordHash` ni objetos anidados complejos.

---

### 2️⃣ AddressDTO

**Propósito**: Transferencia de direcciones de envío y facturación.

**Campos**:
```java
- addressId: Long
- userId: Long
- type: AddressType (SHIPPING/BILLING)
- line1, line2: String
- city, state, country, postalCode: String
- isDefault: Boolean
```

**Método helper**:
```java
getFullAddress() // Retorna dirección completa formateada
```

**Casos de uso**:
- Agregar/actualizar dirección
- Mostrar direcciones en perfil
- Selección de dirección en checkout

---

### 3️⃣ ProductDTO

**Propósito**: Datos de producto para catálogo y detalles.

**Campos**:
```java
- productId: Long
- categoryId: Long
- categoryName: String (desnormalizado)
- sku, name, description: String
- price: BigDecimal
- stockQty: Integer
- isActive: Boolean
- isAvailable: Boolean (calculado: isActive && stockQty > 0)
- createdAt: LocalDateTime
- priceFormatted: String (para UI)
```

**Casos de uso**:
- Listados de productos en catálogo
- Detalles de producto
- Búsqueda de productos
- Respuestas de API

**Ventaja**: Incluye `categoryName` para evitar joins en consultas simples.

---

### 4️⃣ CategoryDTO

**Propósito**: Categorías jerárquicas con subcategorías.

**Campos**:
```java
- categoryId: Long
- parentId: Long
- parentName: String
- name, slug: String
- isRoot: Boolean (calculado: parentId == null)
- subcategoriesCount: Integer
- productsCount: Integer
- subcategories: List<CategoryDTO> (jerarquía)
```

**Casos de uso**:
- Menú de navegación de categorías
- Árbol de categorías jerárquico
- Filtros de búsqueda
- Breadcrumbs de navegación

**Ventaja**: Soporta estructura jerárquica sin lazy loading issues.

---

### 5️⃣ CartDTO

**Propósito**: Carrito de compras con sus items.

**Campos**:
```java
- cartId: Long
- userId: Long (nullable para invitados)
- userEmail: String
- status: CartStatus
- isGuest: Boolean (calculado: userId == null)
- createdAt, updatedAt: LocalDateTime
- items: List<CartItemDTO>
- itemsCount: Integer (agregado)
- total: BigDecimal (calculado)
- totalFormatted: String (para UI)
```

**Casos de uso**:
- Mostrar carrito actual del usuario
- Actualizar carrito
- Respuestas de API del carrito
- Histórico de carritos

---

### 6️⃣ CartItemDTO

**Propósito**: Items individuales del carrito con detalles de producto.

**Campos**:
```java
- cartItemId: Long
- cartId, productId: Long
- productName, productSku: String
- productImageUrl: String
- quantity: Integer
- unitPrice: BigDecimal
- subtotal: BigDecimal (calculado)
- unitPriceFormatted, subtotalFormatted: String
- addedAt: LocalDateTime
- productAvailable: Boolean (stock actual)
- productStock: Integer (stock actual)
```

**Casos de uso**:
- Mostrar items en el carrito
- Agregar/actualizar items
- Validar disponibilidad antes de checkout

**Ventaja**: Incluye información de disponibilidad actual del producto.

---

### 7️⃣ OrderDTO

**Propósito**: Orden completa con todos sus detalles.

**Campos**:
```java
- orderId: Long
- orderNumber: String
- userId: Long
- userEmail, userFullName: String
- orderStatusName: String
- shippingAddress, billingAddress: AddressDTO (anidados)
- items: List<OrderItemDTO>
- itemsCount: Integer
- subtotal, tax, shippingCost, total: BigDecimal
- subtotalFormatted, taxFormatted, shippingCostFormatted, totalFormatted: String
- createdAt: LocalDateTime
```

**Casos de uso**:
- Mostrar detalles de orden
- Listado de órdenes del usuario
- Tracking de orden
- Facturas y recibos

**Ventaja**: Incluye direcciones completas y valores formateados para UI.

---

### 8️⃣ OrderItemDTO

**Propósito**: Items de la orden con precios históricos.

**Campos**:
```java
- orderItemId: Long
- orderId, productId: Long
- productName, productSku: String
- productImageUrl: String
- quantity: Integer
- unitPrice: BigDecimal (precio histórico)
- lineTotal: BigDecimal
- unitPriceFormatted, lineTotalFormatted: String
```

**Casos de uso**:
- Mostrar items en detalles de orden
- Histórico de compras
- Facturas y recibos

**Ventaja**: Preserva precios históricos al momento de la compra.

---

## ⚙️ Clases de Configuración (2 clases)

### 1️⃣ AppConfig

**Propósito**: Configuración general de la aplicación.

#### Configuraciones incluidas:

**Sesiones**:
```java
SESSION_TIMEOUT_MINUTES = 30
GUEST_SESSION_TIMEOUT_HOURS = 24
USER_SESSION_TIMEOUT_HOURS = 168 // 7 días
```

**Carritos**:
```java
CART_ABANDONMENT_THRESHOLD_HOURS = 48
MAX_ITEMS_PER_CART = 50
MAX_QUANTITY_PER_ITEM = 99
```

**Productos**:
```java
LOW_STOCK_THRESHOLD = 10
PRODUCTS_PER_PAGE = 20
MAX_PRODUCTS_PER_PAGE = 100
```

**Órdenes**:
```java
ORDER_NUMBER_PREFIX = "ORD-"
ORDER_NUMBER_LENGTH = 12
```

**Paginación**:
```java
DEFAULT_PAGE_SIZE = 20
MAX_PAGE_SIZE = 100
```

**Impuestos y Envío**:
```java
DEFAULT_TAX_RATE = 19.0 // 19% para Colombia
FREE_SHIPPING_THRESHOLD = 100.0
BASE_SHIPPING_COST = 5.0
```

**Seguridad**:
```java
MIN_PASSWORD_LENGTH = 8
MAX_PASSWORD_LENGTH = 100
MAX_LOGIN_ATTEMPTS = 5
LOCKOUT_DURATION_MINUTES = 15
```

#### Métodos útiles:

```java
getEnvironment() // "development", "staging", "production"
isProduction()   // true si está en producción
isDevelopment()  // true si está en desarrollo
getApplicationName() // "Product Purchasing System"
getVersion()     // "1.0-SNAPSHOT"
```

**Uso de variables de entorno**:
```bash
export APP_ENVIRONMENT=production
```

---

### 2️⃣ DatabaseConfig

**Propósito**: Configuración de conexión a MySQL preparada para Hibernate/JPA (Etapa 06).

#### Variables de entorno soportadas:

```bash
DB_HOST=localhost          # Host del servidor MySQL
DB_PORT=3306              # Puerto de MySQL
DB_NAME=pps_db            # Nombre de la base de datos
DB_USER=root              # Usuario de MySQL
DB_PASSWORD=secret        # Contraseña de MySQL
DB_POOL_SIZE=10           # Tamaño del pool de conexiones
DB_DDL_AUTO=update        # Estrategia Hibernate (create/update/validate/none)
DB_SHOW_SQL=true          # Mostrar SQL en logs
```

#### Propiedades de conexión:

```java
// Valores por defecto (desarrollo)
DEFAULT_DB_HOST = "localhost"
DEFAULT_DB_PORT = "3306"
DEFAULT_DB_NAME = "pps_db"
DEFAULT_DB_USER = "root"
DEFAULT_DB_PASSWORD = ""
```

#### Propiedades de Hibernate/JPA:

```java
DEFAULT_DIALECT = "org.hibernate.dialect.MySQL8Dialect"
DEFAULT_DDL_AUTO = "update"
DEFAULT_SHOW_SQL = true
DEFAULT_FORMAT_SQL = true
DEFAULT_USE_SQL_COMMENTS = true
```

#### Propiedades del pool de conexiones:

```java
DEFAULT_POOL_SIZE = 10
DEFAULT_MIN_POOL_SIZE = 5
DEFAULT_MAX_POOL_SIZE = 20
DEFAULT_IDLE_TIMEOUT = 300000 // 5 minutos
```

#### Métodos principales:

```java
getDbHost()          // Host desde env o default
getDbPort()          // Puerto desde env o default
getDbName()          // Nombre BD desde env o default
getDbUser()          // Usuario desde env o default
getDbPassword()      // Contraseña desde env o default
getJdbcUrl()         // URL JDBC completa construida
getHibernateDialect() // Dialecto de Hibernate para MySQL8
getHibernateDdlAuto() // Estrategia DDL (none en producción)
isShowSql()          // false en producción, true en dev
getPoolSize()        // Tamaño del pool desde env o default
getDriverClassName() // "com.mysql.cj.jdbc.Driver"
getConfigSummary()   // String con resumen de configuración
```

#### Ejemplo de JDBC URL generada:

```
jdbc:mysql://localhost:3306/pps_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

#### Configuración por ambiente:

- **Development**: `DDL_AUTO=update`, `SHOW_SQL=true`
- **Production**: `DDL_AUTO=none`, `SHOW_SQL=false`

---

## 📊 Comparación: Etapa 03 vs Etapa 04

| Aspecto | Etapa 03 | Etapa 04 |
|---------|----------|----------|
| **Estructura** | Exception + Util + Model | + DTO + Config |
| **Transferencia de datos** | ❌ Usar entidades directamente | ✅ DTOs especializados |
| **Configuración** | ❌ Valores dispersos | ✅ Centralizada en Config |
| **Preparación JPA** | ❌ No configurado | ✅ DatabaseConfig listo |
| **Separación API/Dominio** | ❌ No separado | ✅ DTOs desacoplan |
| **Seguridad** | ⚠️ Podría exponer passwordHash | ✅ DTOs no exponen datos sensibles |
| **Lazy Loading** | ⚠️ Posibles excepciones | ✅ DTOs previenen |

---

## 🎯 Beneficios de la Etapa 04

### 1️⃣ Separación de Responsabilidades

**Model (Entidades)**:
- Representan estructura de BD
- Anotaciones JPA (futuro)
- Relaciones entre entidades
- Lógica de dominio mínima

**DTO**:
- Representan datos de API
- Estructura optimizada por caso de uso
- Sin relaciones complejas
- Campos calculados y formateados

### 2️⃣ Seguridad

```java
// ❌ MALO: Exponer entidad directamente
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    return userService.findById(id); // Expone passwordHash!
}

// ✅ BUENO: Usar DTO
@GetMapping("/users/{id}")
public UserDTO getUser(@PathVariable Long id) {
    return userService.findUserDTO(id); // No expone passwordHash
}
```

### 3️⃣ Prevención de Lazy Loading Exceptions

```java
// ❌ MALO: Lazy loading puede fallar
@GetMapping("/carts/{id}")
public Cart getCart(@PathVariable Long id) {
    Cart cart = cartService.findById(id);
    cart.getItems().size(); // LazyInitializationException!
    return cart;
}

// ✅ BUENO: DTO sin lazy loading
@GetMapping("/carts/{id}")
public CartDTO getCart(@PathVariable Long id) {
    return cartService.findCartDTO(id); // Items ya cargados
}
```

### 4️⃣ Optimización de Consultas

```java
// DTO permite proyecciones optimizadas
@Query("SELECT new co.edu.cesde.pps.dto.ProductDTO(" +
       "p.productId, p.category.categoryId, p.category.name, " +
       "p.sku, p.name, p.description, p.price, p.stockQty, " +
       "p.isActive, p.createdAt) " +
       "FROM Product p WHERE p.isActive = true")
List<ProductDTO> findAllActiveProducts();
```

### 5️⃣ Flexibilidad de API

```java
// Diferentes DTOs para diferentes casos de uso
ProductSummaryDTO    // Lista: id, name, price, thumbnail
ProductDetailDTO     // Detalle: todos los campos + reviews + related
ProductAdminDTO      // Admin: incluye campos internos
```

### 6️⃣ Configuración Centralizada

```java
// ❌ MALO: Valores mágicos dispersos
if (sessionAge > 24 * 60) { // ¿Qué significa 24 * 60?
    expireSession();
}

// ✅ BUENO: Usar constantes de configuración
if (sessionAge > AppConfig.getGuestSessionTimeoutHours() * 60) {
    expireSession();
}
```

### 7️⃣ Preparación para Múltiples Ambientes

```bash
# Desarrollo
export APP_ENVIRONMENT=development
export DB_HOST=localhost
export DB_NAME=pps_dev

# Staging
export APP_ENVIRONMENT=staging
export DB_HOST=staging.mysql.example.com
export DB_NAME=pps_staging

# Producción
export APP_ENVIRONMENT=production
export DB_HOST=prod.mysql.example.com
export DB_NAME=pps_prod
export DB_DDL_AUTO=none
export DB_SHOW_SQL=false
```

---

## 💡 Patrones Aplicados

### 1️⃣ DTO Pattern

**Problema**: Las entidades de dominio no son ideales para API.

**Solución**: DTOs específicos para cada caso de uso.

**Beneficios**:
- Desacoplamiento
- Seguridad
- Optimización
- Flexibilidad

### 2️⃣ Configuration Pattern

**Problema**: Valores dispersos en el código.

**Solución**: Clases de configuración centralizadas.

**Beneficios**:
- Mantenibilidad
- Configuración por ambiente
- Documentación clara
- Reutilización

### 3️⃣ Environment-based Configuration

**Problema**: Misma configuración para todos los ambientes.

**Solución**: Variables de entorno + valores por defecto.

**Beneficios**:
- Flexibilidad
- Seguridad (passwords no en código)
- Despliegue simplificado
- CI/CD friendly

---

## 🔧 Compilación

```bash
mvn clean compile
```

**Resultado:** ✅ BUILD SUCCESS

**Warnings**: Solo métodos no usados (normal, se usarán en Etapa 05)

---

## 📦 Commits Realizados

```bash
git log --oneline etapa04
```

1. `feat: add DTO classes for data transfer`
2. `feat: add configuration classes for application settings`
3. `docs: add ETAPA04_SUMMARY documentation`

---

## 🌿 Estado de Git

- **Rama actual:** `etapa04`
- **Rama base:** `etapa03`
- **Commits:** 3 commits
- **Estado:** Listo para push a GitHub
- **Repositorio:** `https://github.com/lgoenaga/product-purchasing-system`

---

## 🚀 Roadmap de Próximas Etapas

### 📌 Etapa 05 - Service Layer

**Objetivo**: Implementar la capa de servicios con lógica de negocio compleja.

**Servicios a crear**:

1. **UserService**
   - Registro de usuarios
   - Autenticación
   - Gestión de direcciones (addAddress, removeAddress)
   - Actualización de perfil
   - Conversión Entity ↔ DTO

2. **CartService**
   - Crear carrito para invitado/usuario
   - Agregar/actualizar/remover items (lógica bidireccional)
   - Calcular totales
   - **Algoritmo de Cart Merge** (invitado → registrado)
   - touch() para actualizar updatedAt
   - Validar disponibilidad de productos
   - Conversión Entity ↔ DTO

3. **OrderService**
   - Proceso de checkout (Cart → Order)
   - Generar número de orden único
   - Calcular totales (subtotal, tax, shipping)
   - Validar direcciones
   - Actualizar stock de productos
   - Cambiar estado de carrito a CONVERTED
   - Conversión Entity ↔ DTO

4. **ProductService**
   - CRUD de productos
   - Búsqueda y filtrado
   - Verificar disponibilidad
   - Actualizar stock
   - Conversión Entity ↔ DTO

5. **CategoryService**
   - CRUD de categorías
   - Gestión de jerarquía (addSubcategory, removeSubcategory)
   - Construcción de árbol de categorías
   - Conversión Entity ↔ DTO

**Características**:
- Uso de excepciones personalizadas (Etapa 03)
- Uso de utilities (ValidationUtils, CalculationUtils, etc.)
- Uso de configuración (AppConfig)
- Conversión Entity ↔ DTO
- Lógica de negocio compleja centralizada
- Preparación para `@Transactional` (Etapa 06)

---

### 📌 Etapa 06 - Repository + JPA + Hibernate + MySQL + Logging

**Objetivo**: Integrar persistencia completa con base de datos.

**Componentes**:

1. **Anotaciones JPA en entidades**
   - `@Entity`, `@Table`, `@Id`, `@GeneratedValue`
   - `@Column`, `@ManyToOne`, `@OneToMany`
   - `@Enumerated`, `@JoinColumn`
   - Estrategias Lazy/Eager loading

2. **Interfaces de repositorio**
   - Extender `JpaRepository<Entity, ID>`
   - Query methods
   - `@Query` personalizados
   - Proyecciones a DTOs

3. **Dependencias Maven**
   - Hibernate Core 6.4.x
   - MySQL Connector 8.3.x
   - SLF4J + Logback (logging)
   - Bean Validation API

4. **Configuración**
   - `persistence.xml` o Spring Data JPA
   - Uso de `DatabaseConfig` creado en Etapa 04
   - Scripts SQL de inicialización

5. **Logging**
   - SLF4J API + Logback Classic
   - `logback.xml` configuration
   - Logs en services y repositories
   - Diferentes niveles por ambiente

---

## ✅ Checklist Etapa 04

- [x] Crear rama `etapa04` desde `etapa03`
- [x] Crear 8 clases DTO
- [x] Crear 2 clases de configuración (AppConfig, DatabaseConfig)
- [x] Compilar sin errores: `mvn clean compile`
- [x] Realizar 3 commits incrementales
- [x] Documentar en `ETAPA04_SUMMARY.md`
- [ ] Push a GitHub (pendiente)

---

## 🎓 Lecciones Aprendidas

### ✅ Buenas Prácticas Aplicadas

1. **DTO Pattern**
   - Desacopla API de modelo de dominio
   - Mejora seguridad
   - Optimiza consultas

2. **Configuration Centralization**
   - Valores en un solo lugar
   - Fácil mantenimiento
   - Configuración por ambiente

3. **Environment Variables**
   - No hardcodear passwords
   - Flexible entre ambientes
   - CI/CD friendly

4. **Separation of Concerns**
   - DTOs para transferencia
   - Entities para dominio
   - Config para configuración

5. **Prepared for Future**
   - DatabaseConfig listo para JPA
   - DTOs listos para Service Layer
   - Config listo para diferentes ambientes

---

## 📝 Ejemplos de Uso Futuros

### Conversión Entity → DTO (Etapa 05)

```java
// En UserService
public UserDTO toDTO(User user) {
    UserDTO dto = new UserDTO(
        user.getUserId(),
        user.getRole().getName(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.getPhone(),
        user.getStatus(),
        user.getCreatedAt()
    );
    dto.setAddressesCount(user.getAddresses().size());
    return dto;
}
```

### Uso de AppConfig (Etapa 05)

```java
// En CartService
public void validateCartItemQuantity(Integer quantity) {
    ValidationUtils.validateRange(
        quantity, 
        1, 
        AppConfig.getMaxQuantityPerItem(),
        "quantity"
    );
}
```

### Uso de DatabaseConfig (Etapa 06)

```java
// En persistence.xml o application.properties
properties.put("javax.persistence.jdbc.url", DatabaseConfig.getJdbcUrl());
properties.put("javax.persistence.jdbc.user", DatabaseConfig.getDbUser());
properties.put("javax.persistence.jdbc.password", DatabaseConfig.getDbPassword());
properties.put("hibernate.dialect", DatabaseConfig.getHibernateDialect());
properties.put("hibernate.hbm2ddl.auto", DatabaseConfig.getHibernateDdlAuto());
properties.put("hibernate.show_sql", DatabaseConfig.isShowSql());
```

---

## 🎉 Conclusión

La **Etapa 04** ha preparado exitosamente el proyecto para la implementación de servicios y persistencia:

- ✅ **8 DTOs** para transferencia de datos segura y optimizada
- ✅ **2 Clases de configuración** centralizadas y flexibles
- ✅ **Preparación para JPA** con DatabaseConfig completo
- ✅ **Separación clara** entre modelo y API
- ✅ **Base sólida** para Service Layer (Etapa 05)

El proyecto está ahora **listo para la Etapa 05** (Service Layer), donde implementaremos toda la lógica de negocio compleja usando las excepciones, utilidades, DTOs y configuración creados hasta ahora.

---

**Autor:** Luis Goenaga  
**Proyecto:** Product Purchasing System - Backend II  
**Institución:** CESDE  
**Año:** 2026
