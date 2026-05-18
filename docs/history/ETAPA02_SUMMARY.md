# Resumen Etapa 02 - Relaciones entre Entidades y Métodos de Negocio

## ✅ Completado - Fecha: 03 de febrero de 2026

### 📦 Transformaciones Realizadas

**Total: 14 entidades transformadas** desde POJOs con IDs Long a modelo orientado a objetos completo con relaciones bidireccionales.

---

## 🔄 Entidades Transformadas

### 1️⃣ Entidades de Usuario (3)

#### **User**
- ❌ `Long roleId` → ✅ `Role role`
- ✅ Agregado: `List<Address> addresses`
- ✅ Métodos: `addAddress()`, `removeAddress()`, `getDefaultAddress()`, `getFullName()`

#### **Address**
- ❌ `Long userId` → ✅ `User user`
- ✅ Relación bidireccional con User

#### **UserSession**
- ❌ `Long userId` → ✅ `User user` (nullable para invitados)
- ✅ Métodos mantienen lógica de invitado

---

### 2️⃣ Entidades de Catálogo de Productos (2)

#### **Category**
- ❌ `Long parentId` → ✅ `Category parent` (auto-referencia)
- ✅ Agregado: `List<Category> subcategories`
- ✅ Agregado: `List<Product> products`
- ✅ Métodos: `addSubcategory()`, `removeSubcategory()`, `isRootCategory()`

#### **Product**
- ❌ `Long categoryId` → ✅ `Category category`
- ✅ Relación bidireccional con Category

---

### 3️⃣ Entidades de Carrito (2)

#### **Cart**
- ❌ `Long userId` → ✅ `User user` (nullable para invitados)
- ❌ `Long sessionId` → ✅ `UserSession session`
- ✅ Agregado: `List<CartItem> items`
- ✅ Métodos: `addItem()`, `removeItem()`, `calculateTotal()`, `touch()`, `isGuestCart()`, `isOpen()`

#### **CartItem**
- ❌ `Long cartId` → ✅ `Cart cart`
- ❌ `Long productId` → ✅ `Product product`
- ✅ Relación bidireccional con Cart

---

### 4️⃣ Entidades de Órdenes y Pagos (5)

#### **Order**
- ❌ `Long userId` → ✅ `User user` (NOT NULL)
- ❌ `Long orderStatusId` → ✅ `OrderStatus orderStatus`
- ❌ `Long shippingAddressId` → ✅ `Address shippingAddress`
- ❌ `Long billingAddressId` → ✅ `Address billingAddress`
- ✅ Agregado: `List<OrderItem> items`
- ✅ Métodos: `addItem()`, `removeItem()`, `calculateTotal()`

#### **OrderItem**
- ❌ `Long orderId` → ✅ `Order order`
- ❌ `Long productId` → ✅ `Product product`
- ✅ Relación bidireccional con Order

#### **Payment**
- ❌ `Long orderId` → ✅ `Order order`
- ❌ `Long paymentMethodId` → ✅ `PaymentMethod paymentMethod`
- ❌ `Long paymentStatusId` → ✅ `PaymentStatus paymentStatus`
- ✅ Métodos mantienen lógica de estado

#### **OrderStatus, PaymentStatus, PaymentMethod, Role**
- ✅ Sin cambios (entidades catálogo simples)

---

## 🎯 Métodos de Negocio Implementados

### Métodos de Gestión Bidireccional

Mantienen consistencia automática en relaciones 1:N:

#### **User**
```java
void addAddress(Address address)           // Agrega dirección y establece user.addresses ↔ address.user
void removeAddress(Address address)        // Remueve dirección y limpia referencia
Address getDefaultAddress()                // Busca dirección con isDefault = true
String getFullName()                       // Concatena firstName + lastName
```

#### **Category**
```java
void addSubcategory(Category subcategory)  // Agrega subcategoría y establece parent
void removeSubcategory(Category subcategory) // Remueve y limpia parent
boolean isRootCategory()                   // Verifica si parent == null
```

#### **Cart**
```java
void addItem(CartItem item)                // Agrega item y establece cart ↔ item.cart
void removeItem(CartItem item)             // Remueve item y limpia cart
BigDecimal calculateTotal()                // Suma subtotales de todos los items
void touch()                               // Actualiza updatedAt
boolean isGuestCart()                      // Verifica si user == null
boolean isOpen()                           // Verifica si status == OPEN
```

#### **CartItem**
```java
BigDecimal calculateSubtotal()             // unitPrice * quantity
```

#### **Order**
```java
void addItem(OrderItem item)               // Agrega item y establece order ↔ item.order
void removeItem(OrderItem item)            // Remueve item y limpia order
BigDecimal calculateTotal()                // subtotal + tax + shippingCost
```

#### **OrderItem**
```java
BigDecimal calculateLineTotal()            // unitPrice * quantity (recalcula al cambiar precio/cantidad)
```

---

## ✨ Características Implementadas

### ✅ Relaciones Bidireccionales
Todas las relaciones 1:N están correctamente configuradas:
- `User` ↔ `Address`
- `Category` ↔ `Category` (auto-referencia)
- `Category` ↔ `Product`
- `Cart` ↔ `CartItem`
- `Order` ↔ `OrderItem`

### ✅ Inicialización de Colecciones
Todas las colecciones se inicializan en constructor vacío como `new ArrayList<>()` para evitar `NullPointerException`.

### ✅ Métodos Helper Bidireccionales
Los métodos `add*()` y `remove*()` mantienen consistencia automática:
- Al agregar item a colección, establecen la referencia inversa
- Al remover item, limpian la referencia inversa
- Verifican duplicados antes de agregar

### ✅ toString() Defensivo
- No navegan a objetos relacionados (previene `StackOverflowError`)
- Muestran solo IDs: `user.getUserId()` en lugar de `user`
- Muestran tamaños de colecciones: `items.size()` en lugar de iterar

### ✅ Validaciones en Setters
Mantienen las validaciones de Etapa 01:
- Precios/montos `>= 0` (excepto `Payment.amount` que permite negativos para reembolsos)
- Cantidades `> 0`
- Recalculan automáticamente totales al cambiar valores

---

## 📊 Comparación Etapa 01 vs Etapa 02

| Aspecto | Etapa 01 | Etapa 02 |
|---------|----------|----------|
| **Relaciones** | IDs Long (`roleId`, `userId`) | Objetos (`Role role`, `User user`) |
| **Colecciones** | ❌ No existen | ✅ `List<Address>`, `List<CartItem>`, etc. |
| **Bidireccionalidad** | ❌ No gestionada | ✅ Automática con métodos helper |
| **Métodos de negocio** | ❌ Solo helper básicos | ✅ `calculateTotal()`, `addItem()`, etc. |
| **Navegación** | ❌ No posible | ✅ `cart.getItems()`, `user.getAddresses()` |
| **Tipo de modelo** | POJOs simples | Modelo OO completo |

---

## 🔧 Compilación

```bash
mvn clean compile
```

**Resultado:** ✅ BUILD SUCCESS

---

## 📦 Commits realizados

```bash
git log --oneline etapa02
```

1. `refactor: transform User, Address, UserSession, Category, Product to use object references (WIP)`
2. `refactor: transform Cart, CartItem, Order, OrderItem, Payment to use object references with bidirectional relationships`
3. `feat: add business methods and bidirectional helpers to all entities with collections`

---

## 🌿 Estado de Git

- **Rama actual:** `etapa02`
- **Rama base:** `etapa01`
- **Commits:** 3 commits incrementales
- **Estado:** Listo para push a GitHub
- **Repositorio:** `https://github.com/lgoenaga/product-purchasing-system`

---

## 🚀 Siguientes Pasos (Etapa 03)

La **Etapa 03** agregará soporte completo para JPA/Hibernate:

### 1. Anotaciones JPA
- `@Entity`, `@Table(name="...")`
- `@Id`, `@GeneratedValue(strategy=GenerationType.IDENTITY)`
- `@Column(name="...", unique=true, nullable=false)`
- `@ManyToOne(fetch=FetchType.LAZY)`, `@JoinColumn(name="...")`
- `@OneToMany(mappedBy="...", cascade=CascadeType.ALL, orphanRemoval=true)`
- `@Enumerated(EnumType.STRING)`
- `@Temporal(TemporalType.TIMESTAMP)` o uso nativo de `LocalDateTime`
- `@Table(uniqueConstraints=@UniqueConstraint(columnNames={...}))`

### 2. Estrategias Lazy/Eager Loading
- **Relaciones N:1** (`@ManyToOne`): `FetchType.LAZY` por defecto
  - Excepto catálogos pequeños (Role, OrderStatus) que pueden ser EAGER
- **Relaciones 1:N** (`@OneToMany`): Siempre `LAZY`
  - Usar fetch joins cuando se necesiten: `JOIN FETCH`
  - Configurar `@EntityGraph` para cargas complejas
- **Problema N+1**: Documentar soluciones (batch fetching, DTO projections)

### 3. Serialización JSON (Jackson)
- `@JsonManagedReference` en lado propietario (`User.addresses`)
- `@JsonBackReference` en lado inverso (`Address.user`)
- Alternativa: `@JsonIgnore` en lado inverso
- `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})` para proxies

### 4. Dependencias Maven
```xml
<!-- Hibernate Core -->
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>6.4.x</version>
</dependency>

<!-- MySQL Connector -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.x</version>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.x</version>
</dependency>

<!-- Bean Validation -->
<dependency>
    <groupId>jakarta.validation</groupId>
    <artifactId>jakarta.validation-api</artifactId>
    <version>3.0.x</version>
</dependency>
```

### 5. Configuración Database
- Variables de entorno: `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `persistence.xml` o `application.properties` (Spring Boot)
- Scripts de inicialización: `data.sql` con catálogos

### 6. Implementación Cart Merge
- Capa de servicio con `@Transactional`
- Algoritmo completo según sección 5 de `er_model_documentation.md`

---

## 📚 Documentación

- **README.md** - Descripción del proyecto y estructura de ramas
- **ETAPA01_SUMMARY.md** - Resumen de entidades básicas
- **ETAPA02_SUMMARY.md** - Este documento (relaciones y métodos)
- **documents_external/er_model_documentation.md** - Modelo E-R completo
- **Javadoc** - Documentación inline en todas las clases

---

## ✅ Checklist Etapa 02

- [x] Crear rama `etapa02` desde `etapa01`
- [x] Transformar User, Address, UserSession (3 entidades)
- [x] Transformar Category, Product (2 entidades)
- [x] Transformar Cart, CartItem (2 entidades)
- [x] Transformar Order, OrderItem, Payment (3 entidades + 3 catálogos)
- [x] Cambiar todos los `Long xxxId` por objetos del dominio
- [x] Agregar colecciones `List<>` en relaciones 1:N
- [x] Inicializar colecciones en constructores vacíos
- [x] Implementar métodos `add*()` bidireccionales
- [x] Implementar métodos `remove*()` bidireccionales
- [x] Implementar métodos `calculate*()` de negocio
- [x] Actualizar todos los `toString()` para mostrar solo IDs
- [x] Mantener `equals()/hashCode()` basados en ID
- [x] Mantener validaciones en setters
- [x] Compilación exitosa sin errores
- [x] Commits granulares por grupos de entidades
- [x] Documentación completa de cambios

---

## 🎊 ¡Etapa 02 Completada Exitosamente!

El modelo E-R ahora es un **modelo orientado a objetos completo** con:
- ✅ Relaciones bidireccionales funcionales
- ✅ Navegación entre objetos (ej: `user.getAddresses()`)
- ✅ Métodos de negocio implementados
- ✅ Gestión automática de consistencia bidireccional
- ✅ Prevención de referencias circulares
- ✅ Código limpio y mantenible

**Preparado para Etapa 03: JPA/Hibernate + MySQL** 🚀

---

Autor: Luis Goenaga  
Proyecto: Product Purchasing System  
Institución: CESDE - Backend II  
Fecha: 03 de febrero de 2026
