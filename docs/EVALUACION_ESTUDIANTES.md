# Banco de Preguntas — Evaluación de Conocimiento de la Aplicación
**Curso:** Backend con Java Spring Boot  
**Fecha:** 2026-05-27  
**Estudiantes:** 15 | **Preguntas por estudiante:** 5 | **Total:** 75

> **Niveles:** 🟢 Básico · 🟡 Intermedio · 🔴 Avanzado  
> **Tipos:** **[EXPLICA]** — comprensión directa · **[QUÉ PASA SI]** — análisis de cambios · **[QUÉ NECESITAS CAMBIAR]** — aplicación práctica

---

## Índice de Estudiantes

| # | Tema |
|---|---|
| [Estudiante 1](#estudiante-1--entidad-user-y-anotaciones-jpa) | Entidad `User` y anotaciones JPA |
| [Estudiante 2](#estudiante-2--entidad-cart-y-estados-del-carrito) | Entidad `Cart` y estados del carrito |
| [Estudiante 3](#estudiante-3--seguridad-bcrypt-y-tokens) | Seguridad — BCrypt y tokens |
| [Estudiante 4](#estudiante-4--flujo-de-login-y-registro) | Flujo de login y registro |
| [Estudiante 5](#estudiante-5--cart-merge) | Cart Merge — fusión de carritos |
| [Estudiante 6](#estudiante-6--manejo-global-de-excepciones) | Manejo global de excepciones |
| [Estudiante 7](#estudiante-7--lombok) | Lombok en los modelos |
| [Estudiante 8](#estudiante-8--cors-y-webconfig) | CORS y `WebConfig` |
| [Estudiante 9](#estudiante-9--dotenvdevelopmentloader) | `DotenvDevelopmentLoader` |
| [Estudiante 10](#estudiante-10--arquitectura-por-capas) | Arquitectura por capas |
| [Estudiante 11](#estudiante-11--controllers-y-apiroutes) | Controllers y `ApiRoutes` |
| [Estudiante 12](#estudiante-12--entidad-order-y-checkout) | Entidad `Order` y checkout |
| [Estudiante 13](#estudiante-13--validationutils) | `ValidationUtils` |
| [Estudiante 14](#estudiante-14--sesiones-guest-vs-authenticated) | Sesiones — Guest vs Authenticated |
| [Estudiante 15](#estudiante-15--transactional) | `@Transactional` |

---

## Estudiante 1 — Entidad `User` y anotaciones JPA

---

### Pregunta 1.1 🟢 [EXPLICA]

**¿Qué hace la anotación `@GeneratedValue(strategy = GenerationType.IDENTITY)` en el campo `userId` de la entidad `User`? ¿Quién es responsable de generar ese valor?**

**Respuesta:**  
Le indica a JPA que el valor del ID lo genera automáticamente la base de datos usando una columna `AUTO_INCREMENT` (en MySQL). El desarrollador **no** asigna el ID manualmente; al hacer `save()`, la BD genera el número y JPA lo refleja en el objeto. Si se cambiara a `GenerationType.SEQUENCE`, JPA usaría una secuencia de BD (no disponible por defecto en MySQL sin configuración adicional).

**Archivo:** `src/main/java/co/edu/cesde/pps/model/User.java` — líneas 55–58

---

### Pregunta 1.2 🟢 [EXPLICA]

**La relación `role` en `User` tiene `fetch = FetchType.LAZY`. ¿Qué significa eso? ¿Cuándo se carga el rol del usuario desde la base de datos?**

**Respuesta:**  
`LAZY` significa que la relación **no se carga** junto con el `User` en el mismo SELECT inicial. El objeto `Role` se carga solo cuando el código accede explícitamente a `user.getRole()` dentro de una transacción activa. Si se intenta acceder fuera de una transacción, JPA lanza `LazyInitializationException`. La alternativa `EAGER` cargaría el rol siempre en el mismo query, incluso cuando no se necesita, aumentando el costo de cada consulta de usuario.

**Archivo:** `src/main/java/co/edu/cesde/pps/model/User.java` — línea 60

---

### Pregunta 1.3 🟡 [QUÉ PASA SI]

**El campo `status` en `User` usa `@Enumerated(EnumType.STRING)`. ¿Qué pasa en la base de datos si cambias eso a `@Enumerated(EnumType.ORDINAL)`? ¿Qué riesgo concreto introduce ese cambio?**

**Respuesta:**  
Con `EnumType.STRING` se guarda el texto `"ACTIVE"`, `"INACTIVE"`, `"BLOCKED"` en la columna. Con `EnumType.ORDINAL` se guardaría `0`, `1`, `2` (el índice de declaración en el enum). El riesgo concreto: si en el futuro se reordena o agrega un valor en `UserStatus` (por ejemplo insertar `PENDING` antes de `ACTIVE`), todos los registros existentes quedarían con el estado incorrecto porque sus números ya no corresponden a los nuevos índices. `EnumType.STRING` es siempre más seguro, legible en BD y resistente a cambios en el enum.

**Archivo:** `src/main/java/co/edu/cesde/pps/model/User.java` — línea 79 | `src/main/java/co/edu/cesde/pps/enums/UserStatus.java`

---

### Pregunta 1.4 🟡 [QUÉ PASA SI]

**El campo `email` en `User` tiene `unique = true` en `@Column`. ¿Qué pasa si dos usuarios intentan registrarse con el mismo correo? ¿Dónde ocurre el error y qué clase lo captura?**

**Respuesta:**  
`unique = true` crea un índice `UNIQUE` en la columna `email` de la tabla `users` en MySQL. Si dos usuarios intentan registrarse con el mismo email, la BD lanza una excepción de violación de constraint. JPA la convierte en `DataIntegrityViolationException` de Spring. El `ApiExceptionHandler` tiene un manejador genérico que la captura y la mapea como error controlado vía `DomainExceptionMapper`. El código de error resultante es `DUPLICATE_RESOURCE` → **HTTP 409 CONFLICT**.

**Archivo:** `src/main/java/co/edu/cesde/pps/model/User.java` — línea 64 | `src/main/java/co/edu/cesde/pps/web/advice/ApiExceptionHandler.java`

---

### Pregunta 1.5 🔴 [EXPLICA]

**¿Por qué `equals()` y `hashCode()` en `User` están implementados usando **solo** `userId` y no otros campos como `email`? ¿Qué problema concreto causaría basar `equals` en `email`?**

**Respuesta:**  
En JPA, dos objetos representan la **misma entidad** si tienen el mismo ID en base de datos, independientemente del estado de sus otros campos. Si `equals` usara `email`, un objeto `User` recién creado (antes de hacer `save()`, cuando `userId` es `null`) podría compararse incorrectamente con otro. Además, si el email se modifica dentro de la sesión de persistencia, el comportamiento en colecciones (`Set`, `HashMap`) sería inconsistente: el objeto entraría en una posición del `Set` y después de cambiar el email ya no sería encontrable en esa misma posición. Usar el ID garantiza identidad estable durante todo el ciclo de vida del objeto.

**Archivo:** `src/main/java/co/edu/cesde/pps/model/User.java` — líneas 114–125

---

## Estudiante 2 — Entidad `Cart` y estados del carrito

---

### Pregunta 2.1 🟢 [EXPLICA]

**¿Cuáles son los tres estados posibles de un `Cart`? Explica en qué situación el sistema asigna cada uno.**

**Respuesta:**  
- **`OPEN`**: el carrito está activo; el usuario puede agregar, quitar o modificar items. Es el estado inicial.  
- **`CONVERTED`**: el carrito fue convertido en una orden al completar el checkout. Ya no se puede modificar.  
- **`ABANDONED`**: el carrito fue descartado. Esto ocurre durante el cart merge: el carrito del invitado se fusiona con el del usuario registrado y el carrito del invitado queda marcado como `ABANDONED`.

**Archivo:** `src/main/java/co/edu/cesde/pps/enums/CartStatus.java` | `src/main/java/co/edu/cesde/pps/model/Cart.java` — comentario líneas 37–42

---

### Pregunta 2.2 🟢 [EXPLICA]

**En la entidad `Cart`, el campo `user` puede ser `null`. ¿Qué significa eso en términos de negocio? ¿Qué comprueba exactamente el método `isGuestCart()`?**

**Respuesta:**  
Un `Cart` con `user = null` pertenece a un **usuario invitado** (no registrado). El sistema permite navegar y agregar productos al carrito sin necesidad de login. El método `isGuestCart()` simplemente retorna `user == null`, identificando si el carrito es de invitado. Un carrito con usuario asignado pertenece a un usuario registrado. Esta diferencia determina también qué estrategia usa `CartApplicationService.resolveCurrentCart()` para encontrar el carrito activo correcto.

**Archivo:** `src/main/java/co/edu/cesde/pps/model/Cart.java` — líneas 96, 124–127

---

### Pregunta 2.3 🟡 [QUÉ PASA SI]

**La relación `items` en `Cart` tiene `cascade = CascadeType.ALL` y `orphanRemoval = true`. ¿Qué sucede exactamente si dentro de una transacción activa llamas a `cart.getItems().clear()`?**

**Respuesta:**  
`orphanRemoval = true` hace que cualquier `CartItem` que se elimine de la colección `items` sea automáticamente borrado de la base de datos por JPA. Si llamas a `cart.getItems().clear()`, JPA detecta que todos los `CartItem` quedaron huérfanos (sin padre) y genera un `DELETE` para cada uno al hacer flush. En combinación con `CascadeType.ALL`, si también se borra el `Cart`, todos sus items se borran en cascada. Es el equivalente a vaciar el carrito completamente a nivel de base de datos, sin necesidad de iterar y borrar item por item manualmente.

**Archivo:** `src/main/java/co/edu/cesde/pps/model/Cart.java` — línea 115

---

### Pregunta 2.4 🟡 [EXPLICA]

**¿Qué hace el método `calculateTotal()` en `Cart`? ¿Por qué delega el cálculo a `CalculationUtils` en vez de hacerlo directamente con un stream en el mismo método?**

**Respuesta:**  
`calculateTotal()` recorre la lista de `items`, obtiene el subtotal de cada `CartItem` (`unitPrice × quantity`) y suma todos los subtotales para obtener el total del carrito. Delega el cálculo a `CalculationUtils` para **centralizar la lógica matemática** en un solo lugar. Si el día de mañana se añaden descuentos, impuestos o redondeo especial, ese cambio se hace en `CalculationUtils` y todos los que lo usan (tanto `Cart` como `Order`) se benefician automáticamente, sin duplicar código ni riesgo de comportamientos distintos entre las dos entidades.

**Archivo:** `src/main/java/co/edu/cesde/pps/model/Cart.java` — líneas 140–145 | `src/main/java/co/edu/cesde/pps/util/CalculationUtils.java`

---

### Pregunta 2.5 🔴 [QUÉ NECESITAS CAMBIAR]

**Si quisieras agregar un nuevo estado `EXPIRED` al carrito (para carritos que llevan más de 48 horas sin actividad), ¿qué archivos tendrías que modificar y en qué orden lógico?**

**Respuesta:**  
1. **`CartStatus.java`** — agregar el valor `EXPIRED` al enum.  
2. **`Cart.java`** — opcionalmente agregar un método helper `isExpired()` para encapsular la lógica.  
3. **El servicio que gestiona carritos (`CartService`)** — implementar la lógica que detecta si `updatedAt` supera el umbral definido en `AppConfig.CART_ABANDONMENT_THRESHOLD_HOURS = 48` y cambia el estado a `EXPIRED`.  
4. **`AppConfig.java`** — ya existe `getCartAbandonmentThresholdHours()` con el valor correcto, no necesita cambios.  
5. **Revisar** cualquier query o método que filtre por estado `OPEN` para decidir si `EXPIRED` debe comportarse igual que `ABANDONED`.

**Archivo:** `src/main/java/co/edu/cesde/pps/enums/CartStatus.java` | `src/main/java/co/edu/cesde/pps/config/AppConfig.java` — línea 22

---

## Estudiante 3 — Seguridad: BCrypt y tokens

---

### Pregunta 3.1 🟢 [EXPLICA]

**¿Qué hace `BCryptPasswordHasher.hash(rawPassword)`? ¿La contraseña se guarda en texto plano en la base de datos?**

**Respuesta:**  
No. `hash()` usa `BCryptPasswordEncoder.encode()` que aplica el algoritmo BCrypt: genera un **salt aleatorio** y produce un hash irreversible de la contraseña. Cada vez que se llama con la misma contraseña produce un resultado diferente (por el salt). La BD guarda el hash en el campo `password_hash` de la tabla `users`, nunca el texto plano. Para verificar el login se usa `matches(rawPassword, hashedPassword)`, que compara la contraseña ingresada contra el hash almacenado sin necesidad de "descifrarlo" (porque no es cifrado reversible).

**Archivo:** `src/main/java/co/edu/cesde/pps/security/BCryptPasswordHasher.java` — líneas 15–22

---

### Pregunta 3.2 🟢 [EXPLICA]

**¿Qué tipo de token genera `UuidSessionTokenGenerator.generateToken()`? ¿Es un JWT? ¿Qué diferencia hay entre este token y un JWT?**

**Respuesta:**  
Genera un **token opaco basado en UUID**: `UUID.randomUUID().toString().replace("-", "")` produce una cadena hexadecimal de 32 caracteres (ej: `a1b2c3d4e5f6...`). **No es un JWT**. Un JWT (JSON Web Token) lleva información codificada en su propio cuerpo (claims: usuario, rol, expiración) y puede validarse sin consultar la BD. Este token opaco no contiene información: para saber a qué sesión y usuario corresponde, el servidor **debe consultar la tabla `user_sessions`** en cada request. El token opaco es más simple pero requiere una consulta a BD por cada llamada autenticada.

**Archivo:** `src/main/java/co/edu/cesde/pps/security/UuidSessionTokenGenerator.java` — líneas 13–15

---

### Pregunta 3.3 🟡 [EXPLICA]

**`PasswordHasher` es una interface. ¿Por qué se usó una interface en lugar de inyectar `BCryptPasswordHasher` directamente en `AuthApplicationService`?**

**Respuesta:**  
Usar la interface `PasswordHasher` desacopla la lógica de negocio (autenticación) de la implementación concreta (BCrypt). `AuthApplicationService` depende de la abstracción, no de BCrypt específicamente. Esto permite:  
1. **Cambiar el algoritmo** de hashing en el futuro (ej: Argon2) creando una nueva implementación de `PasswordHasher` sin tocar `AuthApplicationService`.  
2. **Testear** con una implementación mock que no hace hashing real (retorna el mismo string), acelerando los tests.  

Es el principio de inversión de dependencias (la D de SOLID): las capas de alto nivel no deben depender de las implementaciones concretas.

**Archivo:** `src/main/java/co/edu/cesde/pps/security/PasswordHasher.java` | `src/main/java/co/edu/cesde/pps/application/AuthApplicationService.java` — línea 35

---

### Pregunta 3.4 🟡 [QUÉ PASA SI]

**¿Qué pasa si cambias `new BCryptPasswordEncoder()` a `new BCryptPasswordEncoder(12)` en `BCryptPasswordHasher`? ¿Mejora la seguridad? ¿Hay algún costo?**

**Respuesta:**  
El número `12` es el **factor de costo (strength)** de BCrypt. Por defecto es `10`. Aumentarlo a `12` hace que el hash sea más lento de calcular de forma exponencial (cada punto dobla el tiempo de procesamiento aproximadamente). Esto mejora la seguridad porque dificulta ataques de fuerza bruta: calcular millones de hashes para adivinar contraseñas se vuelve mucho más costoso. El costo concreto: el tiempo de login y registro aumenta de ~100ms a ~400ms por operación. Los hashes existentes en BD **no se ven afectados** porque `matches()` usa el factor embebido en el hash almacenado para verificar correctamente.

**Archivo:** `src/main/java/co/edu/cesde/pps/security/BCryptPasswordHasher.java` — línea 12

---

### Pregunta 3.5 🔴 [QUÉ NECESITAS CAMBIAR]

**Si quisieras cambiar el mecanismo de tokens de UUID a JWT (con expiración embebida), ¿qué archivo crearías, cuál modificarías, y cuál NO deberías tocar en la capa de aplicación?**

**Respuesta:**  
- **Crear:** `JwtSessionTokenGenerator.java` que implemente la interface `SessionTokenGenerator` usando una librería como `jjwt`. Generaría un JWT firmado con la expiración embebida.  
- **Modificar:** `UserSessionService` — la lógica de `requireActiveSession()` debería verificar la firma y expiración del JWT en lugar de hacer consulta a BD para cada request.  
- **NO tocar:** `AuthApplicationService`, `CartApplicationService`, `OrderApplicationService` — todos dependen de `SessionTokenGenerator` (interface) y `UserSessionService` (contrato), no de la implementación concreta. El cambio es transparente para las capas superiores gracias al principio de inversión de dependencias.

**Archivo:** `src/main/java/co/edu/cesde/pps/security/SessionTokenGenerator.java` | `src/main/java/co/edu/cesde/pps/security/UuidSessionTokenGenerator.java`

---

## Estudiante 4 — Flujo de login y registro

---

### Pregunta 4.1 🟢 [EXPLICA]

**En `AuthController`, ¿qué HTTP status code devuelve un registro exitoso? ¿Cómo se logra eso en el código? ¿Por qué no se usa simplemente `@ResponseStatus`?**

**Respuesta:**  
Devuelve `201 CREATED`. Se logra retornando `ResponseEntity.status(HttpStatus.CREATED).body(...)`. No se usa `@ResponseStatus(HttpStatus.CREATED)` a nivel de método porque `register()` retorna `ResponseEntity<AuthSessionResponse>`, lo que da control dinámico sobre el status, headers y body al mismo tiempo. Con `@ResponseStatus` se puede fijar el código pero se pierde la capacidad de construir el `ResponseEntity` completo de forma fluida, incluyendo headers adicionales que se puedan necesitar en el futuro (como `Location`).

**Archivo:** `src/main/java/co/edu/cesde/pps/web/controller/AuthController.java` — líneas 40–43

---

### Pregunta 4.2 🟢 [EXPLICA]

**Describe en orden los pasos que ejecuta el método `login()` de `AuthApplicationService` desde que recibe el `LoginRequest` hasta que retorna la respuesta.**

**Respuesta:**  
1. Valida que el request tenga email válido y password no vacío (`validateLoginRequest`).  
2. Busca el usuario por email en BD (`findUserEntityByEmailOrThrow`). Si no existe, lanza excepción.  
3. Verifica que el `status` del usuario sea `ACTIVE`. Si está inactivo, lanza `AuthenticationException`.  
4. Compara la contraseña ingresada contra el hash almacenado (`passwordHasher.matches`). Si no coincide, lanza `AuthenticationException`.  
5. Crea una nueva sesión autenticada (`createAuthenticatedSession`).  
6. Obtiene el DTO del usuario (`findById`).  
7. Resuelve el carrito: si se envió `guestCartId` hace merge con el carrito del usuario; si no, busca o crea el carrito del usuario.  
8. Construye y retorna `AuthSessionResponse` con sesión + usuario + carrito.

**Archivo:** `src/main/java/co/edu/cesde/pps/application/AuthApplicationService.java` — líneas 78–94

---

### Pregunta 4.3 🟡 [QUÉ PASA SI]

**¿Qué sucede si un usuario intenta hacer login y su `status` es `INACTIVE`? ¿Qué excepción se lanza, qué HTTP status retorna el cliente y cómo llega hasta ahí?**

**Respuesta:**  
En `login()` línea 82, antes de verificar la contraseña se comprueba `user.getStatus() != UserStatus.ACTIVE`. Si el status es `INACTIVE`, se lanza `AuthenticationException("User account is inactive")`. Esta excepción llega al `ApiExceptionHandler.handleAuthentication()`, que la mapea vía `DomainExceptionMapper`. El código de error resultante es `UNAUTHORIZED` y `resolveHttpStatus(UNAUTHORIZED)` retorna `HttpStatus.UNAUTHORIZED` → **HTTP 401**. El body incluye el mensaje de error estructurado en el formato estándar de la API. Importante: la contraseña nunca llega a verificarse si el usuario está inactivo.

**Archivo:** `src/main/java/co/edu/cesde/pps/application/AuthApplicationService.java` — líneas 82–84 | `src/main/java/co/edu/cesde/pps/web/advice/ApiExceptionHandler.java`

---

### Pregunta 4.4 🟡 [EXPLICA]

**¿Qué es `guestCartId` en `LoginRequest` y para qué sirve durante el proceso de login? ¿Es un campo obligatorio?**

**Respuesta:**  
`guestCartId` es el ID del carrito que el usuario tenía como invitado antes de iniciar sesión. **No es obligatorio** (puede ser `null`). Su propósito es preservar los productos que el usuario agregó sin estar logueado: si se envía, `resolveAuthenticatedCart()` llama a `mergeGuestCartToUserCart()`, fusionando el carrito invitado con el del usuario. Si no se envía (`null`), simplemente se busca o crea el carrito del usuario sin hacer merge. Esto garantiza que el usuario no pierda lo que seleccionó antes de autenticarse.

**Archivo:** `src/main/java/co/edu/cesde/pps/application/AuthApplicationService.java` — líneas 106–111

---

### Pregunta 4.5 🔴 [EXPLICA]

**¿Por qué `logout()` en `AuthController` devuelve `ResponseEntity<Void>` con `HttpStatus.NO_CONTENT (204)` en lugar de simplemente `void` o un mensaje de texto?**

**Respuesta:**  
`ResponseEntity<Void>` con `204 NO_CONTENT` es la convención REST estándar para operaciones que se ejecutan correctamente pero **no retornan body**. Usar `void` como tipo de retorno haría que Spring devuelva `200 OK` con body vacío, lo cual es técnicamente incorrecto según REST (200 implica que hay contenido que entregar). `204` comunica explícitamente "éxito, sin contenido que devolver". Usar `ResponseEntity` (en lugar de `@ResponseStatus`) da consistencia con otros endpoints y permite agregar headers en el futuro (ej: `Clear-Site-Data` para limpiar cookies del browser) sin cambiar la firma del método.

**Archivo:** `src/main/java/co/edu/cesde/pps/web/controller/AuthController.java` — líneas 59–63

---

## Estudiante 5 — Cart Merge

---

### Pregunta 5.1 🟢 [EXPLICA]

**¿Qué significa "fusionar un carrito invitado con el carrito de un usuario registrado"? ¿Por qué es necesario ese proceso?**

**Respuesta:**  
Cuando un usuario navega sin loguerse (invitado), agrega productos a un carrito temporal. Al registrarse o iniciar sesión, ese carrito invitado debe unirse al carrito del usuario para que no pierda los productos seleccionados. El proceso toma los `CartItem` del carrito invitado y los transfiere al carrito del usuario: si el mismo producto ya existe en el carrito del usuario se suman las cantidades; si no existe se mueve el item. Finalmente el carrito invitado queda como `ABANDONED`. Sin este proceso, el usuario perdería todo lo que agregó antes de autenticarse.

**Archivo:** `src/main/java/co/edu/cesde/pps/model/Cart.java` — comentario "POLÍTICA DE CART MERGE" líneas 43–67

---

### Pregunta 5.2 🟢 [EXPLICA]

**¿Desde dónde se llama automáticamente a `mergeGuestCartToUserCart()` durante el registro o login? ¿Qué método privado lo orquesta?**

**Respuesta:**  
Lo orquesta el método privado `resolveAuthenticatedCart(Long userId, Long guestCartId)` en `AuthApplicationService` (líneas 106–111). Es llamado tanto desde `register()` como desde `login()` inmediatamente después de que el usuario se autentica. Si `guestCartId != null`, ejecuta el merge llamando a `cartService.mergeGuestCartToUserCart(guestCartId, userId)`. Si `guestCartId == null`, simplemente busca o crea el carrito del usuario sin fusionar nada.

**Archivo:** `src/main/java/co/edu/cesde/pps/application/AuthApplicationService.java` — líneas 106–111

---

### Pregunta 5.3 🟡 [QUÉ PASA SI]

**¿Qué estado queda asignado al carrito del invitado después de completar un merge? ¿Qué impide que ese carrito sea usado de nuevo?**

**Respuesta:**  
Después del merge, el carrito del invitado queda con status `ABANDONED`. La lógica del `CartService` (y `CartApplicationService.resolveCurrentCart()`) solo trabaja con carritos en estado `OPEN`. Al buscar el carrito activo de cualquier sesión, los carritos `ABANDONED` son ignorados completamente. Esto impide que el carrito fusionado sea reutilizado accidentalmente. Si el mismo invitado volviera a navegar, se crearía una nueva sesión y un nuevo carrito vacío en estado `OPEN`.

**Archivo:** `src/main/java/co/edu/cesde/pps/model/Cart.java` — comentario punto 3, línea 59 | `src/main/java/co/edu/cesde/pps/enums/CartStatus.java`

---

### Pregunta 5.4 🟡 [EXPLICA]

**En `CartController`, ¿cuál endpoint permite disparar el merge manualmente después del login? ¿Qué datos debe enviar el cliente en el body y en los headers?**

**Respuesta:**  
El endpoint `POST /api/v1/cart/merge` (mapeado en `CartController` líneas 74–79). El cliente debe enviar:  
- **Header:** `Authorization: Bearer <token>` de la sesión autenticada, para identificar al usuario.  
- **Body:** un `MergeGuestCartRequest` con el campo `guestCartId` (el ID del carrito del invitado a fusionar).  

`CartApplicationService.mergeGuestCart()` verifica que la sesión sea de un usuario autenticado (`requireAuthenticatedUser`), no de un invitado, antes de ejecutar el merge. Si la sesión es de invitado, lanza `AuthenticationException`.

**Archivo:** `src/main/java/co/edu/cesde/pps/web/controller/CartController.java` — líneas 74–79 | `src/main/java/co/edu/cesde/pps/application/CartApplicationService.java` — líneas 68–74

---

### Pregunta 5.5 🔴 [QUÉ PASA SI]

**¿Qué sucede si el mismo producto está en el carrito del invitado (cantidad 2) y en el carrito del usuario registrado (cantidad 3) cuando se hace el merge? ¿Cuál es el resultado final y cómo queda la BD?**

**Respuesta:**  
Según la política documentada en `Cart.java` (punto 2.a del merge): cuando el mismo producto existe en ambos carritos, se **suman las cantidades**. El resultado es que el carrito del usuario queda con ese producto con **cantidad 5** (2 + 3). El `unitPrice` se resuelve según la política interna (conservar el precio más reciente o el del usuario). A nivel de BD:  
- El `CartItem` del usuario queda con `quantity = 5`.  
- El `CartItem` del invitado desaparece (se elimina o queda sin efecto al marcar el carrito como `ABANDONED`).  
- El carrito del invitado pasa a `status = ABANDONED`.  
El usuario continúa con un solo `CartItem` unificado para ese producto.

**Archivo:** `src/main/java/co/edu/cesde/pps/model/Cart.java` — comentario líneas 52–54

---

## Estudiante 6 — Manejo global de excepciones

---

### Pregunta 6.1 🟢 [EXPLICA]

**¿Qué anotación hace que `ApiExceptionHandler` intercepte excepciones de TODOS los controllers sin necesidad de repetir código en cada uno?**

**Respuesta:**  
`@RestControllerAdvice` (línea 22). Es la combinación de `@ControllerAdvice` (que registra la clase como un manejador de excepciones global para todos los controllers del contexto de Spring) más `@ResponseBody` (que convierte automáticamente el objeto de retorno de cada método `@ExceptionHandler` a JSON). Gracias a esto, cualquier excepción no capturada en un controller sube hasta `ApiExceptionHandler`, que decide el status HTTP y el formato de la respuesta de error de forma centralizada y consistente.

**Archivo:** `src/main/java/co/edu/cesde/pps/web/advice/ApiExceptionHandler.java` — línea 22

---

### Pregunta 6.2 🟢 [EXPLICA]

**¿Qué HTTP status code recibe el cliente cuando se lanza una `AuthorizationException`? ¿Por qué es diferente al de `AuthenticationException`?**

**Respuesta:**  
- `AuthenticationException` → **401 UNAUTHORIZED**: el servidor no sabe quién eres (token ausente, inválido o expirado).  
- `AuthorizationException` → **403 FORBIDDEN**: el servidor sabe quién eres pero no tienes permiso para esa operación (usuario autenticado sin el rol necesario).  

La diferencia semántica es importante: 401 invita al cliente a autenticarse; 403 indica que incluso autenticándose no tendría acceso. En `resolveHttpStatus()`: `UNAUTHORIZED` → 401, `FORBIDDEN` → 403. Ejemplo: un usuario normal intentando `POST /api/v1/admin/products` recibe 403.

**Archivo:** `src/main/java/co/edu/cesde/pps/web/advice/ApiExceptionHandler.java` — líneas 100–108

---

### Pregunta 6.3 🟡 [QUÉ PASA SI]

**¿Qué pasa si se agrega una nueva excepción `PaymentFailedException` en la aplicación pero NO se agrega a `ApiErrorCode` ni se maneja en `resolveHttpStatus()`?**

**Respuesta:**  
El `ApiExceptionHandler` tiene un manejador `@ExceptionHandler(Exception.class)` genérico (líneas 87–91) que captura cualquier excepción no manejada específicamente. `PaymentFailedException` caería ahí. Al pasar por `domainExceptionMapper.map(exception)`, si no está mapeada, retornaría `INTERNAL_SERVER_ERROR` como código de error por defecto. `resolveHttpStatus(INTERNAL_SERVER_ERROR)` retorna **HTTP 500**. El cliente recibiría un error 500 genérico en lugar de un código de error específico del pago que le permita entender qué ocurrió y cómo actuar.

**Archivo:** `src/main/java/co/edu/cesde/pps/web/advice/ApiExceptionHandler.java` — líneas 87–108

---

### Pregunta 6.4 🟡 [EXPLICA]

**¿Para qué sirve `MethodArgumentNotValidException`? ¿Qué la lanza y cuándo ocurre en el flujo de un request?**

**Respuesta:**  
La lanza Spring MVC automáticamente cuando un `@RequestBody` anotado con `@Valid` falla las validaciones de Bean Validation (anotaciones como `@NotBlank`, `@Min`, `@Email` en el DTO de request). Por ejemplo, si `LoginRequest` tiene `@NotBlank String email` y el cliente envía `email: ""`, Spring lanza `MethodArgumentNotValidException` **antes de que el código del controller se ejecute**. El `ApiExceptionHandler` la captura en `handleMethodArgumentNotValid()`, extrae los errores por campo y retorna una respuesta `400 BAD_REQUEST` con la lista detallada de campos inválidos y sus mensajes.

**Archivo:** `src/main/java/co/edu/cesde/pps/web/advice/ApiExceptionHandler.java` — líneas 34–47

---

### Pregunta 6.5 🔴 [QUÉ NECESITAS CAMBIAR]

**Actualmente `DUPLICATE_RESOURCE`, `INSUFFICIENT_STOCK`, `INVALID_CART_STATE` y `CART_MERGE_ERROR` devuelven `409 CONFLICT`. Si quisieras que solo `INSUFFICIENT_STOCK` devuelva `422 UNPROCESSABLE_ENTITY`, ¿qué cambias y dónde exactamente?**

**Respuesta:**  
Solo se modifica el método `resolveHttpStatus()` en `ApiExceptionHandler.java`, separando `INSUFFICIENT_STOCK` de la agrupación actual:

```java
// Antes:
case DUPLICATE_RESOURCE, INSUFFICIENT_STOCK, INVALID_CART_STATE, CART_MERGE_ERROR -> HttpStatus.CONFLICT;

// Después:
case DUPLICATE_RESOURCE, INVALID_CART_STATE, CART_MERGE_ERROR -> HttpStatus.CONFLICT;
case INSUFFICIENT_STOCK -> HttpStatus.UNPROCESSABLE_ENTITY;
```

No se necesita cambiar el enum `ApiErrorCode`, ni la excepción `InsufficientStockException`, ni ningún servicio. El cambio queda completamente aislado en el método de resolución de status HTTP.

**Archivo:** `src/main/java/co/edu/cesde/pps/web/advice/ApiExceptionHandler.java` — líneas 100–108

---

## Estudiante 7 — Lombok

---

### Pregunta 7.1 🟢 [EXPLICA]

**¿Qué hace la anotación `@Builder` en la clase `User`? Muestra cómo se construiría un objeto `User` usando el patrón que genera.**

**Respuesta:**  
`@Builder` genera una clase interna `User.UserBuilder` con un método fluido para construir el objeto. En lugar de usar el constructor directamente con todos los parámetros en orden, se encadena cada campo por nombre:

```java
User user = User.builder()
    .email("juan@example.com")
    .firstName("Juan")
    .lastName("Pérez")
    .role(adminRole)
    .passwordHash(hashedPw)
    .build();
```

Ventajas: legibilidad (cada campo tiene nombre explícito), campos opcionales sin definir múltiples constructores, y fácil de extender sin cambiar las firmas existentes.

**Archivo:** `src/main/java/co/edu/cesde/pps/model/User.java` — línea 52

---

### Pregunta 7.2 🟢 [EXPLICA]

**¿Qué diferencia hay entre `@NoArgsConstructor` y `@AllArgsConstructor` en los modelos? ¿Por qué se necesitan ambos en una entidad JPA con `@Builder`?**

**Respuesta:**  
- **`@NoArgsConstructor`**: genera un constructor sin parámetros. **JPA lo requiere obligatoriamente** para poder instanciar las entidades al recuperarlas de la BD mediante reflexión.  
- **`@AllArgsConstructor`**: genera un constructor con todos los campos como parámetros. Lo requiere `@Builder` internamente para construir el objeto una vez finalizados todos los valores en el Builder.  

Sin `@NoArgsConstructor`, JPA lanzaría un error al intentar materializar entidades recuperadas de la BD. Sin `@AllArgsConstructor`, `@Builder` no podría compilar correctamente. Se necesitan los tres en conjunto.

**Archivo:** `src/main/java/co/edu/cesde/pps/model/User.java` — líneas 50–52

---

### Pregunta 7.3 🟡 [QUÉ PASA SI]

**El campo `status` en `User` tiene `@Builder.Default private UserStatus status = UserStatus.ACTIVE`. ¿Qué pasa si quitas `@Builder.Default`? ¿El valor `ACTIVE` se asigna de todas formas cuando se usa el Builder?**

**Respuesta:**  
No. Sin `@Builder.Default`, cuando se usa el Builder sin especificar explícitamente `status`, Lombok inicializa el campo con `null` (para objetos) en lugar del valor declarado en la inicialización del campo. Esto causaría que un usuario creado con el Builder sin especificar `status` tenga `status = null`, violando el `@Column(nullable = false)` al intentar hacer `save()` en BD y lanzando un error de constraint. Sin `@Builder.Default` se tendría que llamar siempre `.status(UserStatus.ACTIVE)` explícitamente en cada uso del Builder.

**Archivo:** `src/main/java/co/edu/cesde/pps/model/User.java` — líneas 81–82

---

### Pregunta 7.4 🟡 [EXPLICA]

**Lombok genera getters y setters para todos los campos en `Product` gracias a `@Getter @Setter`, pero `setPrice()` y `setStockQty()` están definidos manualmente. ¿Por qué? ¿Los sobreescribe Lombok o los respeta?**

**Respuesta:**  
Están definidos manualmente para agregar **validación automática en el setter**: `setPrice()` llama a `ValidationUtils.validateNonNegative(price, "price")` antes de asignar el valor; `setStockQty()` hace lo mismo. Lombok detecta que ya existe un método con esa firma y **no genera uno nuevo** (los respeta, no los sobreescribe). Esto garantiza que nunca se pueda asignar un precio o cantidad negativa a un producto directamente desde cualquier parte del código, sin necesidad de recordar validar manualmente en cada uso.

**Archivo:** `src/main/java/co/edu/cesde/pps/model/Product.java` — líneas 87–95

---

### Pregunta 7.5 🔴 [EXPLICA]

**¿Por qué los `toString()` de los modelos están implementados manualmente si Lombok tiene `@ToString`? ¿Qué problema concreto causaría usar `@ToString` en `User` o `Cart`?**

**Respuesta:**  
Los modelos tienen **relaciones con `FetchType.LAZY`** (`User` → `addresses`, `Cart` → `items`, etc.). Si se usa `@ToString` de Lombok en `User`, al llamar `toString()` Lombok intentaría incluir `addresses` en el string, lo que dispararía la carga lazy (si hay sesión abierta) o lanzaría `LazyInitializationException` (si no hay sesión). Peor aún: si existieran relaciones **bidireccionales** circulares (entidad A tiene B, B tiene A), el `toString()` de Lombok entraría en recursión infinita y causaría `StackOverflowError`. El `toString()` manual solo imprime IDs y conteos de colecciones, nunca navega las relaciones.

**Archivo:** `src/main/java/co/edu/cesde/pps/model/User.java` — líneas 130–142 | `src/main/java/co/edu/cesde/pps/model/Cart.java` — líneas 165–176

---

## Estudiante 8 — CORS y `WebConfig`

---

### Pregunta 8.1 🟢 [EXPLICA]

**¿Qué es CORS y por qué es necesario configurarlo en esta aplicación? ¿Qué pasaría si no se configura?**

**Respuesta:**  
CORS (Cross-Origin Resource Sharing) es un mecanismo de seguridad del navegador que bloquea peticiones HTTP provenientes de un origen diferente al del servidor (distinto protocolo, host o puerto). Esta aplicación tiene el frontend en un origen (ej: `http://localhost:5173`) y el backend en otro (`http://localhost:8080`). Sin configurar CORS, el navegador bloquearía con error todas las peticiones del frontend al backend. Importante: la restricción es **del navegador**, no de la red. Herramientas como Postman o Curl no tienen esa restricción y seguirían funcionando.

**Archivo:** `src/main/java/co/edu/cesde/pps/config/WebConfig.java` — comentario Javadoc líneas 13–35

---

### Pregunta 8.2 🟢 [EXPLICA]

**¿Desde dónde se configura el origen del frontend que la aplicación acepta en producción? ¿Hay que tocar código Java o recompilar para cambiarlo?**

**Respuesta:**  
No. El origen se configura mediante la variable de entorno `CORS_ALLOWED_ORIGINS`, leída por `application.yml` e inyectada en `WebConfig` con `@Value("${app.cors.allowed-origins:...}")`. En producción, se pone el valor en el archivo `.env.prod` del servidor. Cambiar el origen permitido no requiere tocar código Java ni recompilar: solo se actualiza la variable de entorno y se reinicia el contenedor Docker. Esto sigue el principio de la aplicación de 12 factores: la configuración del entorno nunca debe estar en el código.

**Archivo:** `src/main/java/co/edu/cesde/pps/config/WebConfig.java` — líneas 46–47 | `.env.prod.example`

---

### Pregunta 8.3 🟡 [QUÉ PASA SI]

**¿Por qué `allowCredentials(true)` es incompatible con `allowedOrigins("*")`? ¿Qué error ocurriría si se intenta combinar ambas opciones?**

**Respuesta:**  
Es una restricción de seguridad del estándar CORS del W3C. `allowCredentials(true)` le indica al navegador que puede enviar cookies y headers de autenticación entre orígenes. Si esto se combinara con `"*"` (cualquier origen), cualquier sitio malicioso podría hacer peticiones autenticadas en nombre del usuario (ataque CSRF/XSRF potencial). Spring detecta esta combinación insegura y lanza una `IllegalArgumentException` en tiempo de **arranque de la aplicación**: `"When allowCredentials is true, allowedOrigins cannot contain the special value '*'"`. La aplicación no levanta. Se debe especificar siempre orígenes explícitos.

**Archivo:** `src/main/java/co/edu/cesde/pps/config/WebConfig.java` — comentarios líneas 32–34

---

### Pregunta 8.4 🟡 [QUÉ NECESITAS CAMBIAR]

**El frontend está en `localhost:5173` (React + Vite). Si el equipo migra a Next.js en `localhost:3000`, ¿qué archivo cambias? ¿Y si necesitas soportar ambos puertos simultáneamente durante la migración?**

**Respuesta:**  
Solo se modifica el archivo `.env` (desarrollo local), cambiando la variable:
```
CORS_ALLOWED_ORIGINS=http://localhost:3000
```
Para soportar ambos puertos durante la migración:
```
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```
`WebConfig` ya tiene soporte nativo para múltiples orígenes: línea 67 hace `.split(",")` y pasa el array completo a `allowedOrigins(origins)`. **No se recompila** el proyecto. El mismo cambio aplica en `.env.prod` para producción, cambiando al dominio real del servidor Next.js.

**Archivo:** `.env` | `src/main/java/co/edu/cesde/pps/config/WebConfig.java` — línea 67

---

### Pregunta 8.5 🔴 [EXPLICA]

**¿Qué hace `maxAge(3600)` en la configuración CORS? ¿Qué tipo de petición HTTP evita y cómo impacta en el rendimiento del frontend?**

**Respuesta:**  
`maxAge(3600)` indica al navegador que **cachee la respuesta de la petición preflight** (`OPTIONS`) durante 3600 segundos (1 hora). Antes de cualquier request cross-origin con métodos no triviales (`POST`, `PUT`, `DELETE`) o headers personalizados (`Authorization`), el navegador envía automáticamente una petición `OPTIONS` para verificar permisos. Sin `maxAge`, esa verificación ocurriría **antes de cada request** al backend, duplicando el número de llamadas HTTP. Con `maxAge(3600)`, el navegador reutiliza la respuesta cached durante una hora, eliminando virtualmente las peticiones preflight en sesiones normales de usuario y reduciendo la latencia percibida.

**Archivo:** `src/main/java/co/edu/cesde/pps/config/WebConfig.java` — línea 76

---

## Estudiante 9 — `DotenvDevelopmentLoader`

---

### Pregunta 9.1 🟢 [EXPLICA]

**¿Para qué sirve `DotenvDevelopmentLoader` y en qué entorno se usa principalmente? ¿Se utiliza en producción?**

**Respuesta:**  
Lee el archivo `.env` del directorio de trabajo y carga las variables de entorno como `System properties` antes de que Spring Boot arranque. Se usa principalmente en **desarrollo local**, donde el desarrollador no configura variables de entorno a nivel del sistema operativo y las define en el archivo `.env`. En producción (Docker con `env_file:`), las variables vienen como OS environment variables; el loader detecta esto y **no las sobreescribe** (línea 73: `System.getenv(key) != null`). Con `.ignoreIfMissing()`, si no existe el `.env` como en producción, la clase no hace nada sin lanzar errores.

**Archivo:** `src/main/java/co/edu/cesde/pps/config/DotenvDevelopmentLoader.java` — comentario Javadoc + líneas 44–48

---

### Pregunta 9.2 🟢 [QUÉ PASA SI]

**¿Qué hace `ignoreIfMissing()` en la configuración del `Dotenv`? ¿Qué sucedería si lo quitas y el servidor de producción no tiene archivo `.env`?**

**Respuesta:**  
`.ignoreIfMissing()` hace que Dotenv **no lance excepción** si el archivo `.env` no existe en el directorio de trabajo. Si se quita esta opción, al iniciar la aplicación en un servidor de producción (donde no hay `.env`), Dotenv lanzaría una excepción al intentar leer el archivo. En `DotenvDevelopmentLoader.load()` hay un `catch (Exception exception)` general que la capturaría y loguearía como advertencia, permitiendo que la aplicación continúe. Sin embargo, si ese `catch` no existiera, la aplicación fallaría en el arranque únicamente por la ausencia del `.env`, lo cual es un error grave evitable con `ignoreIfMissing()`.

**Archivo:** `src/main/java/co/edu/cesde/pps/config/DotenvDevelopmentLoader.java` — línea 47

---

### Pregunta 9.3 🟡 [QUÉ PASA SI]

**Si la variable `DB_PASSWORD` ya está definida como variable de entorno del sistema operativo (por ejemplo en Docker con `env_file:`), ¿`DotenvDevelopmentLoader` la sobreescribe con el valor del `.env`?**

**Respuesta:**  
No. El método `applyPropertyIfMissing()` verifica explícitamente en la línea 73: `if (System.getenv(key) != null || System.getProperty(key) != null) return 0;`. Si la variable ya existe en el OS o como System property, el loader la **salta completamente** y retorna 0 (no cargada). Esto garantiza que en Docker (donde las variables vienen del `env_file:` como OS env vars), el `.env` local del proyecto no interfiere accidentalmente con los valores de producción. La prioridad es: OS env var / System property **>** `.env` file.

**Archivo:** `src/main/java/co/edu/cesde/pps/config/DotenvDevelopmentLoader.java` — líneas 68–78

---

### Pregunta 9.4 🟡 [EXPLICA]

**¿Por qué `DotenvDevelopmentLoader` tiene un constructor privado con `throw new AssertionError()`? ¿Qué patrón de diseño representa y qué otros archivos del proyecto siguen el mismo patrón?**

**Respuesta:**  
Es el patrón de **clase utilitaria** (utility class). Al tener solo métodos estáticos (`load()`, `applyPropertyIfMissing()`), no tiene sentido crear instancias. El constructor privado previene `new DotenvDevelopmentLoader()`. El `throw new AssertionError()` es una salvaguarda extra: incluso si alguien intenta instanciarla por reflexión (`Constructor.newInstance()`), lanzará un error en tiempo de ejecución. En este proyecto siguen el mismo patrón: `ValidationUtils` (líneas 12–14), `AppConfig` (líneas 54–56) y `ApiRoutes` (constructor privado vacío).

**Archivo:** `src/main/java/co/edu/cesde/pps/config/DotenvDevelopmentLoader.java` — líneas 35–37

---

### Pregunta 9.5 🔴 [QUÉ NECESITAS CAMBIAR]

**Si agregas una nueva variable `STRIPE_API_KEY` para integrar un procesador de pagos, ¿qué debes cambiar en `DotenvDevelopmentLoader` y en qué otros archivos del proyecto para que funcione completo en desarrollo y producción?**

**Respuesta:**  
1. **`DotenvDevelopmentLoader.java`** — agregar `"STRIPE_API_KEY"` a la lista `SUPPORTED_KEYS` (líneas 19–33).  
2. **`.env`** (desarrollo local) — agregar `STRIPE_API_KEY=sk_test_...` con valor de prueba.  
3. **`.env.prod.example`** — agregar `STRIPE_API_KEY=CHANGE_ME` como documentación para el servidor.  
4. **`.env.prod`** en el servidor IONOS — agregar el valor real de producción.  
5. **`application.yml`** — exponer la variable como property de Spring: `stripe.api-key: ${STRIPE_API_KEY}`.  
6. La clase que use la clave la inyecta con `@Value("${stripe.api-key}")`.

**Archivo:** `src/main/java/co/edu/cesde/pps/config/DotenvDevelopmentLoader.java` — líneas 19–33

---

## Estudiante 10 — Arquitectura por capas

---

### Pregunta 10.1 🟢 [EXPLICA]

**¿Cuántas capas tiene esta aplicación y cuáles son, en orden desde el cliente hasta la base de datos?**

**Respuesta:**  
Cinco capas principales:
1. **Web / Controller** (`web/controller/`): recibe la petición HTTP, extrae parámetros y delega.  
2. **Application Service** (`application/`): orquesta los casos de uso coordinando múltiples servicios.  
3. **Service** (`service/`): contiene la lógica de negocio pura de una entidad (validaciones, reglas de dominio).  
4. **Repository** (`repository/`): acceso a datos mediante Spring Data JPA, genera las queries SQL.  
5. **Base de datos** (MySQL): persistencia física de las entidades.  

Los modelos fluyen entre capas; DTOs y Response objects hacen la transformación en las fronteras necesarias.

**Archivo:** paquetes `web/controller/`, `application/`, `service/`, `repository/`

---

### Pregunta 10.2 🟢 [EXPLICA]

**¿Cuál es la diferencia entre un `Service` (ej: `CartService`) y un `ApplicationService` (ej: `CartApplicationService`)? ¿Pueden los controllers llamar directamente a un `Service`?**

**Respuesta:**  
- **`Service`** (ej: `CartService`): lógica de negocio pura de **una sola entidad o agregado**. Sabe cómo agregar un item al carrito, calcular totales, etc. No conoce HTTP ni sesiones de usuario.  
- **`ApplicationService`** (ej: `CartApplicationService`): **orquesta casos de uso** que involucran múltiples servicios. Sabe qué servicio llamar en qué orden para resolver el request del usuario (resolver sesión → encontrar carrito → agregar item).  

Técnicamente los controllers **podrían** llamar directamente a `Service`, pero la arquitectura lo evita deliberadamente: los controllers solo conocen los `ApplicationService`, manteniendo separación de responsabilidades y facilitando la reutilización.

**Archivo:** `src/main/java/co/edu/cesde/pps/application/CartApplicationService.java` | `src/main/java/co/edu/cesde/pps/service/CartService.java`

---

### Pregunta 10.3 🟡 [EXPLICA]

**¿Por qué `CartController` NO llama directamente a `CartService` sino que pasa por `CartApplicationService`? ¿Qué responsabilidad evita asumir el controller?**

**Respuesta:**  
El controller evita asumir **lógica de orquestación**: no debe saber que para agregar un item necesita primero resolver la sesión activa, luego determinar si el carrito es del usuario o del invitado, y finalmente delegar a `CartService`. Eso es responsabilidad del `CartApplicationService`. Si el controller llamara directamente a `CartService`, necesitaría también conocer `UserSessionService`, decidir la estrategia del carrito, etc., violando el principio de responsabilidad única (SRP). El controller solo extrae el token del header y lo pasa; toda la lógica queda en la capa de aplicación.

**Archivo:** `src/main/java/co/edu/cesde/pps/web/controller/CartController.java` | `src/main/java/co/edu/cesde/pps/application/CartApplicationService.java` — líneas 76–81

---

### Pregunta 10.4 🟡 [EXPLICA]

**¿Dónde se verifica que el usuario esté autenticado antes de hacer el checkout? ¿En el controller o en el application service? Describe el flujo completo.**

**Respuesta:**  
La verificación ocurre en `OrderApplicationService.checkout()` (línea 34): `userSessionService.requireAuthenticatedUser(sessionToken)`. El flujo completo es:  
1. El browser envía `POST /api/v1/orders/checkout` con `Authorization: Bearer <token>`.  
2. `OrderController.checkout()` extrae el token del header usando `currentSessionResolver.resolveCurrentToken()`.  
3. Pasa el token a `OrderApplicationService.checkout()`.  
4. El application service llama a `requireAuthenticatedUser(token)`, que verifica: sesión activa + usuario asociado (no invitado). Si falla, lanza `AuthenticationException` → HTTP 401.  
5. Solo si pasa, ejecuta el checkout completo.

**Archivo:** `src/main/java/co/edu/cesde/pps/web/controller/OrderController.java` — líneas 34–42 | `src/main/java/co/edu/cesde/pps/application/OrderApplicationService.java` — línea 34

---

### Pregunta 10.5 🔴 [QUÉ NECESITAS CAMBIAR]

**Si necesitas agregar un nuevo caso de uso "aplicar cupón de descuento al carrito", ¿en qué capa lo implementas? ¿Qué clases crearías o modificarías, en qué orden?**

**Respuesta:**  
1. **Modelo/Entidad** — crear `Coupon.java` con campos `code`, `discountPercent`, `expiresAt`, `isActive`. Agregar `appliedCoupon` (nullable) a `Cart.java`.  
2. **Repository** — crear `CouponRepository.java` con `findByCode(String code)`.  
3. **Service** — crear `CouponService.java` con la lógica: validar que el cupón existe, no expiró y aplica al carrito. Modificar `CartService` para admitir y calcular el descuento.  
4. **ApplicationService** — modificar `CartApplicationService` para orquestar: resolver sesión → encontrar carrito → delegar a `CouponService` + `CartService`.  
5. **Controller** — agregar endpoint `POST /api/v1/cart/coupon` en `CartController`.  
6. **DTOs** — crear `ApplyCouponRequest` con `couponCode`. Actualizar `CartResponse` para incluir `discountApplied` y `finalTotal`.

**Archivo:** paquetes `model/`, `repository/`, `service/`, `application/`, `web/controller/`, `web/dto/`

---

## Estudiante 11 — Controllers y `ApiRoutes`

---

### Pregunta 11.1 🟢 [EXPLICA]

**¿Qué diferencia hay entre `@Controller` y `@RestController`? ¿Por qué todos los controllers de esta aplicación usan `@RestController`?**

**Respuesta:**  
- `@Controller`: Spring espera que el método retorne el nombre de una vista HTML (template) a renderizar con Thymeleaf, FreeMarker, etc.  
- `@RestController`: equivale a `@Controller + @ResponseBody`. Cada método retorna el objeto directamente serializado como JSON en el body HTTP, sin buscar ninguna vista.  

Esta es una **API REST pura** diseñada para ser consumida por un frontend separado (React, Vue, etc.). Nunca sirve HTML. Por eso todos los controllers usan `@RestController`: Spring serializa automáticamente a JSON cualquier objeto que retornen los métodos.

**Archivo:** `src/main/java/co/edu/cesde/pps/web/controller/ProductController.java` — línea 13

---

### Pregunta 11.2 🟢 [EXPLICA]

**¿Cuál es la URL completa para obtener un producto específico por ID según `ApiRoutes`? ¿Qué HTTP method usa? ¿Qué pasa si el producto existe pero `isActive = false`?**

**Respuesta:**  
`GET /api/v1/products/{id}`. Se construye: `ApiRoutes.PRODUCTS = "/api/v1/products"` + `@GetMapping("/{id}")`. Ejemplo: `GET http://localhost:8080/api/v1/products/42`.  

Si el producto existe en BD pero tiene `isActive = false`, el método `getProduct()` en `ProductController` (líneas 38–40) lanza `EntityNotFoundException`, que el `ApiExceptionHandler` mapea a **HTTP 404 NOT FOUND**. Los productos inactivos se tratan como no existentes para el catálogo público, aunque permanezcan en BD.

**Archivo:** `src/main/java/co/edu/cesde/pps/web/controller/ApiRoutes.java` — línea 8 | `src/main/java/co/edu/cesde/pps/web/controller/ProductController.java` — líneas 35–42

---

### Pregunta 11.3 🟡 [EXPLICA]

**¿Por qué se centraliza la definición de rutas en la clase `ApiRoutes` con constantes en lugar de escribir los strings directamente en cada `@RequestMapping`?**

**Respuesta:**  
Centralizar las rutas en `ApiRoutes` evita duplicación y errores tipográficos. Si la ruta cambia de `/api/v1/products` a `/api/v2/products`, solo se modifica `ApiRoutes.PRODUCTS` y todos los controllers y tests que la referencian se actualizan automáticamente al recompilar. Si los strings estuvieran duplicados en múltiples archivos, habría que buscar y cambiar en cada uno con riesgo de dejar alguno sin actualizar y causar inconsistencias difíciles de detectar en runtime. Además, facilita encontrar todos los endpoints de la API en una sola clase.

**Archivo:** `src/main/java/co/edu/cesde/pps/web/controller/ApiRoutes.java`

---

### Pregunta 11.4 🟡 [QUÉ PASA SI]

**¿Qué sucede si se llama a `POST /api/v1/admin/products` sin enviar el header `Authorization`? Describe el flujo completo de la petición hasta la respuesta HTTP.**

**Respuesta:**  
1. `AdminProductController.createProduct()` recibe `authorizationHeader = null` (el parámetro es `required = false`).  
2. Antes de cualquier lógica de negocio llama a `adminAccessGuard.requireAdmin(null)`.  
3. `AdminAccessGuard` llama a `currentSessionResolver.resolveAuthenticatedUser(null)`.  
4. `CurrentSessionResolver` llama a `bearerTokenExtractor.extractRequiredToken(null)`.  
5. `BearerTokenExtractor` detecta `authorizationHeader == null` y lanza `AuthenticationException("Authorization header is required")`.  
6. `ApiExceptionHandler.handleAuthentication()` la captura → código `UNAUTHORIZED` → **HTTP 401** con body estructurado de error.

**Archivo:** `src/main/java/co/edu/cesde/pps/web/controller/AdminProductController.java` — líneas 36–38 | `src/main/java/co/edu/cesde/pps/web/security/BearerTokenExtractor.java` — líneas 11–15

---

### Pregunta 11.5 🔴 [EXPLICA]

**En `ProductController.listProducts()`, el filtro por `categoryId` se aplica en Java con un stream después de traer datos de BD. ¿Qué problema de rendimiento puede causar esto? ¿Cómo lo mejorarías?**

**Respuesta:**  
El problema ocurre especialmente al combinar búsqueda por texto y filtro por categoría: `searchProducts(search)` puede traer cientos de productos de todas las categorías a memoria, y luego el stream filtra por `categoryId` descartando la mayoría. En catálogos grandes (miles de productos) esto es ineficiente porque carga datos innecesarios del servidor de BD, los transfiere por red y los procesa en memoria solo para descartarlos.  

**Mejora:** agregar en el repositorio un método que combine ambos filtros en una sola query SQL:  
```java
List<Product> findByNameContainingAndCategoryId(String name, Long categoryId);
// o con @Query: WHERE p.name LIKE %:search% AND p.category.id = :categoryId
```
Así el filtrado ocurre en BD y solo llegan a la aplicación los registros necesarios.

**Archivo:** `src/main/java/co/edu/cesde/pps/web/controller/ProductController.java` — líneas 27–52

---

## Estudiante 12 — Entidad `Order` y checkout

---

### Pregunta 12.1 🟢 [EXPLICA]

**¿Puede un usuario invitado hacer checkout y crear una orden? ¿Por qué? ¿Dónde están las restricciones en el código?**

**Respuesta:**  
No. Hay dos restricciones:  
1. **Capa de aplicación:** `OrderApplicationService.checkout()` línea 34 llama a `requireAuthenticatedUser(token)`, que verifica que la sesión tenga un usuario asociado. Si es invitado (`user == null`), lanza `AuthenticationException` → **HTTP 401**.  
2. **Modelo/BD:** el campo `user` en `Order.java` tiene `nullable = false` (línea 72), lo que haría imposible guardar una orden sin usuario incluso si se eludiera el primer control.  

El invitado debe registrarse o iniciar sesión antes de poder hacer checkout.

**Archivo:** `src/main/java/co/edu/cesde/pps/application/OrderApplicationService.java` — línea 34 | `src/main/java/co/edu/cesde/pps/model/Order.java` — líneas 19, 72

---

### Pregunta 12.2 🟢 [EXPLICA]

**¿Por qué los campos monetarios de `Order` (`subtotal`, `tax`, `shippingCost`, `total`) usan `BigDecimal` en lugar de `double` o `float`?**

**Respuesta:**  
`double` y `float` usan representación de **punto flotante binario**, que no puede representar exactamente muchos números decimales. Por ejemplo, `0.1 + 0.2` en `double` da `0.30000000000000004`. En operaciones monetarias, esos errores de redondeo se acumulan y causan discrepancias en precios, totales e impuestos. `BigDecimal` usa representación decimal exacta con precisión arbitraria. En `Order.java`, todos los campos tienen `precision = 10, scale = 2`: hasta 10 dígitos totales con exactamente 2 decimales, garantizando cálculos precisos para cualquier monto monetario.

**Archivo:** `src/main/java/co/edu/cesde/pps/model/Order.java` — líneas 87–101

---

### Pregunta 12.3 🟡 [EXPLICA]

**¿Qué HTTP status code devuelve un checkout exitoso y por qué ese código y no `200 OK`?**

**Respuesta:**  
Devuelve **201 CREATED** (`OrderController` línea 41). Se usa `201` porque el checkout **crea un nuevo recurso** (una orden nueva en BD). `200 OK` se usa para operaciones que retornan un recurso existente o confirman una operación sin creación. `201` es el código semántico correcto de REST para "recurso creado exitosamente". Idealmente debería ir acompañado de un header `Location: /api/v1/orders/{id}` con la URL del recurso creado (como indica el estándar REST), aunque esta implementación aún no incluye ese header.

**Archivo:** `src/main/java/co/edu/cesde/pps/web/controller/OrderController.java` — líneas 33–42

---

### Pregunta 12.4 🟡 [EXPLICA]

**¿Para qué sirve `orderNumber` en `Order`? ¿Es lo mismo que `orderId`? ¿Cómo se genera y qué configuraciones lo controlan?**

**Respuesta:**  
No son lo mismo:  
- **`orderId`**: clave primaria interna de la BD (autoincremental), solo para uso técnico interno y relaciones entre tablas.  
- **`orderNumber`**: identificador de negocio visible al usuario, con formato `ORD-XXXX`. Es el que aparece en emails de confirmación, notificaciones y páginas de tracking.  

Se genera en la capa de servicio (`OrderService`) al crear la orden, usando constantes de `AppConfig`: `getOrderNumberPrefix()` = `"ORD-"` y `getOrderNumberLength()` = `12`. Tiene `unique = true` en BD para garantizar que no haya duplicados.

**Archivo:** `src/main/java/co/edu/cesde/pps/model/Order.java` — líneas 68–69 | `src/main/java/co/edu/cesde/pps/config/AppConfig.java` — líneas 32–33

---

### Pregunta 12.5 🔴 [QUÉ PASA SI]

**Si el precio de un producto sube de $100 a $120 después de que el usuario lo agregó al carrito, ¿el precio de la orden final se ve afectado? ¿Por qué? ¿Qué campo del código garantiza esto?**

**Respuesta:**  
No se ve afectado. En `CartItem.java` (línea 73), el campo `unitPrice` almacena el precio **en el momento exacto en que el producto fue agregado al carrito** ("precio congelado"). Cuando el administrador sube el precio del `Product` en BD, el `CartItem` del usuario ya tiene guardado `unitPrice = 100.00`, independientemente de que `product.price` ahora sea `120.00`. Al hacer checkout, la orden se calcula usando `CartItem.unitPrice` (100), no el precio actual del producto. Esta es una decisión deliberada de diseño: el usuario paga lo que vio al agregar el producto, dando consistencia y confianza al proceso de compra.

**Archivo:** `src/main/java/co/edu/cesde/pps/model/CartItem.java` — líneas 23–35, línea 73

---

## Estudiante 13 — `ValidationUtils`

---

### Pregunta 13.1 🟢 [EXPLICA]

**¿Por qué `ValidationUtils` tiene un constructor privado con `throw new AssertionError()`? ¿Qué pasaría si alguien intentara instanciarla con reflexión Java (`Constructor.newInstance()`)?**

**Respuesta:**  
Es una **clase utilitaria** con solo métodos estáticos: no tiene estado interno y no necesita instanciarse. El constructor privado previene `new ValidationUtils()` desde código normal. El `throw new AssertionError()` es una protección adicional contra reflexión: si alguien usa `ValidationUtils.class.getDeclaredConstructors()[0].newInstance()`, Java lanzaría `InvocationTargetException` cuya causa es el `AssertionError`, haciendo imposible la instanciación. Es el mismo patrón que `AppConfig`, `DotenvDevelopmentLoader` y `ApiRoutes` en este proyecto.

**Archivo:** `src/main/java/co/edu/cesde/pps/util/ValidationUtils.java` — líneas 12–14

---

### Pregunta 13.2 🟢 [EXPLICA]

**¿Qué excepción lanza `ValidationUtils.validateNotBlank()` cuando el campo está vacío o es `null`? ¿Cómo llega esa excepción hasta la respuesta HTTP y con qué código?**

**Respuesta:**  
Lanza `ValidationException(fieldName, value, "Field cannot be null or empty")`. El flujo hasta HTTP: `ValidationException` es una excepción de dominio mapeada en `DomainExceptionMapper` al código `VALIDATION_ERROR`. El `ApiExceptionHandler` la captura en el manejador genérico `handleAnyException()` → `resolveHttpStatus(VALIDATION_ERROR)` retorna `HttpStatus.BAD_REQUEST` → **HTTP 400**. La respuesta incluye el nombre del campo que falló y el mensaje de error en el formato estándar de la API.

**Archivo:** `src/main/java/co/edu/cesde/pps/util/ValidationUtils.java` — líneas 21–26 | `src/main/java/co/edu/cesde/pps/web/advice/ApiExceptionHandler.java`

---

### Pregunta 13.3 🟡 [EXPLICA]

**¿Cuál es la diferencia concreta entre `validatePositive()` y `validateNonNegative()` para un `BigDecimal`? Da un ejemplo de dónde falla uno pero no el otro.**

**Respuesta:**  
- `validatePositive(value)`: falla si `value <= 0`. Rechaza `0` y todos los negativos.  
- `validateNonNegative(value)`: falla si `value < 0`. Acepta `0`, solo rechaza negativos.  

**Ejemplo concreto:** `validatePositive(BigDecimal.ZERO)` → lanza `ValidationException("Value must be positive (> 0)")`. `validateNonNegative(BigDecimal.ZERO)` → pasa sin error.  

Aplicación real: el precio de un producto gratuito puede ser `0.00` (válido para `validateNonNegative`), pero la cantidad de un `CartItem` no puede ser `0` porque no tiene sentido tener cero unidades de algo en el carrito (requiere `validatePositive`).

**Archivo:** `src/main/java/co/edu/cesde/pps/util/ValidationUtils.java` — líneas 33–44

---

### Pregunta 13.4 🟡 [EXPLICA]

**¿Por qué `CartItem.setQuantity()` usa `validatePositive` pero `CartItem.setUnitPrice()` usa `validateNonNegative`? ¿Qué caso de negocio justifica esa diferencia?**

**Respuesta:**  
Una **cantidad de 0** en el carrito no tiene lógica de negocio: si la cantidad es cero, el item simplemente no debería existir en el carrito. Por eso se usa `validatePositive` que rechaza el 0. Un **precio de 0.00** sí puede ser válido para productos gratuitos, muestras, bonos promocionales o descuentos del 100%. Por eso se usa `validateNonNegative` que acepta el 0 pero rechaza precios negativos, que nunca tienen sentido en un sistema de ventas. Esta distinción refleja reglas de negocio reales y no es arbitraria.

**Archivo:** `src/main/java/co/edu/cesde/pps/model/CartItem.java` — líneas 81–89

---

### Pregunta 13.5 🔴 [QUÉ NECESITAS CAMBIAR]

**Si el negocio decide que el email solo debe aceptar dominios `.com` y `.co`, ¿qué línea exacta modificarías en `ValidationUtils`? Escribe el nuevo regex y explica el cambio.**

**Respuesta:**  
Se modifica la línea 10, el `EMAIL_PATTERN`:

```java
// Antes — acepta cualquier TLD de 2 o más letras:
private static final Pattern EMAIL_PATTERN =
    Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

// Después — solo acepta .com y .co:
private static final Pattern EMAIL_PATTERN =
    Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.(com|co)$");
```

El cambio: `\\.[A-Za-z]{2,}` (cualquier extensión de 2+ letras) se reemplaza por `\\.(com|co)` (solo esas dos extensiones). El `$` al final garantiza que termine exactamente ahí sin caracteres adicionales. No se necesita cambiar ningún otro archivo. Todos los usos de `validateEmail()` en la aplicación reciben automáticamente la nueva validación.

**Archivo:** `src/main/java/co/edu/cesde/pps/util/ValidationUtils.java` — línea 10

---

## Estudiante 14 — Sesiones: Guest vs Authenticated

---

### Pregunta 14.1 🟢 [EXPLICA]

**¿Cómo distingue el sistema si una `UserSession` pertenece a un invitado o a un usuario registrado? ¿Qué campo en la entidad y qué método helper lo determinan?**

**Respuesta:**  
El campo `user` en `UserSession` es **nullable**. Si `user == null`, es una sesión de invitado. Si `user != null`, pertenece a un usuario registrado. El método helper `isGuestSession()` (línea 63) encapsula esa lógica: `return user == null`. Esto permite al `CartApplicationService.resolveCurrentCart()` (líneas 76–81) decidir qué estrategia usar para encontrar el carrito activo: buscar por `userId` para usuarios registrados, o por `sessionId` para invitados.

**Archivo:** `src/main/java/co/edu/cesde/pps/model/UserSession.java` — líneas 49–50, 63–65

---

### Pregunta 14.2 🟢 [EXPLICA]

**¿Cuánto tiempo dura una sesión de invitado versus una sesión de usuario registrado? ¿Dónde están configurados esos tiempos y cómo se cambiarían?**

**Respuesta:**  
- Sesión de **invitado**: **24 horas** (`GUEST_SESSION_TIMEOUT_HOURS = 24`).  
- Sesión de **usuario registrado**: **168 horas** = 7 días (`USER_SESSION_TIMEOUT_HOURS = 168`).  

Están definidos en `AppConfig.java` (líneas 18–19) como constantes estáticas privadas, accesibles mediante `AppConfig.getGuestSessionTimeoutHours()` y `AppConfig.getUserSessionTimeoutHours()`. Para cambiarlos, solo se modifican esas constantes en `AppConfig` y el `UserSessionService` que las usa al crear sesiones recoge el nuevo valor.

**Archivo:** `src/main/java/co/edu/cesde/pps/config/AppConfig.java` — líneas 18–19

---

### Pregunta 14.3 🟡 [QUÉ PASA SI]

**¿Qué sucede si un invitado intenta hacer checkout directamente sin registrarse? Describe el flujo completo hasta la respuesta HTTP.**

**Respuesta:**  
1. El invitado llama a `POST /api/v1/orders/checkout` con su token de sesión de invitado.  
2. `OrderController` extrae el token del header y lo pasa a `OrderApplicationService.checkout()`.  
3. `userSessionService.requireAuthenticatedUser(token)` carga la sesión desde BD y comprueba `session.isGuestSession()` (user == null). Como es invitado, lanza `AuthenticationException`.  
4. `ApiExceptionHandler.handleAuthentication()` la captura → `UNAUTHORIZED` → **HTTP 401** con mensaje de error.  
5. El cliente recibe 401. El invitado debe primero llamar a `POST /api/v1/auth/register` o `POST /api/v1/auth/login` para obtener una sesión autenticada antes de poder hacer checkout.

**Archivo:** `src/main/java/co/edu/cesde/pps/application/OrderApplicationService.java` — línea 34

---

### Pregunta 14.4 🟡 [EXPLICA]

**En `CartApplicationService.resolveCurrentCart()`, ¿cómo determina el método qué carrito usar? Explica el flujo para sesión de invitado y para sesión de usuario registrado.**

**Respuesta:**  
El método (líneas 76–81):
1. Obtiene la sesión activa con `requireActiveSession(sessionToken)`. Si el token es inválido o expirado, lanza excepción.  
2. Evalúa `session.getUser() != null`:  
   - **Usuario registrado** (`user != null`) → `cartService.findOrCreateOpenCartForUser(session.getUser().getUserId())`: busca el carrito en estado `OPEN` del usuario por su `userId`, o crea uno nuevo si no existe.  
   - **Invitado** (`user == null`) → `cartService.findOrCreateOpenCartForGuestSession(session.getSessionId())`: busca por `sessionId`, o crea uno nuevo.  

Ambos casos garantizan que siempre haya un carrito activo disponible sin que el controller necesite conocer esa lógica.

**Archivo:** `src/main/java/co/edu/cesde/pps/application/CartApplicationService.java` — líneas 76–81

---

### Pregunta 14.5 🔴 [QUÉ PASA SI]

**Si un usuario tiene dos dispositivos con sesiones activas y hace logout en uno, ¿las dos sesiones quedan invalidadas? ¿Por qué? ¿Cómo implementarías "cerrar sesión en todos los dispositivos"?**

**Respuesta:**  
No. `authApplicationService.logout()` llama a `userSessionService.expireSession(sessionToken)`, que invalida **solo la sesión del token específico** recibido en ese logout. La otra sesión (otro dispositivo) sigue activa hasta que expire (`USER_SESSION_TIMEOUT_HOURS = 168h`) o ese dispositivo también haga logout.  

**Para implementar "cerrar sesión en todos los dispositivos":**  
1. En `UserSessionService` agregar: `expireAllSessionsByUserId(Long userId)` → actualiza `expires_at = NOW()` para todas las sesiones activas del usuario.  
2. En `AuthApplicationService` agregar: `logoutAllSessions(String sessionToken)` → resuelve el usuario del token actual y llama al paso 1.  
3. Agregar endpoint en `AuthController`: `DELETE /api/v1/auth/sessions` con `Authorization` header.

**Archivo:** `src/main/java/co/edu/cesde/pps/application/AuthApplicationService.java` — línea 103

---

## Estudiante 15 — `@Transactional`

---

### Pregunta 15.1 🟢 [EXPLICA]

**¿Qué hace `@Transactional(readOnly = true)` a nivel de clase en `AuthApplicationService`? ¿Por qué se pone en la clase y no solo en los métodos que leen?**

**Respuesta:**  
Define que **todos los métodos de la clase** son transaccionales de solo lectura por defecto. `readOnly = true` le indica al ORM y al gestor de transacciones que no habrá escrituras, habilitando optimizaciones: Hibernate deshabilita el mecanismo de dirty-checking (detección de cambios para generar UPDATEs automáticos) y puede usar réplicas de lectura si están configuradas. Se pone a nivel de clase para no repetir `@Transactional(readOnly = true)` en cada método de lectura (aplicando DRY — Don't Repeat Yourself). Los métodos de escritura lo sobreescriben localmente con `@Transactional` sin `readOnly`.

**Archivo:** `src/main/java/co/edu/cesde/pps/application/AuthApplicationService.java` — línea 27

---

### Pregunta 15.2 🟢 [EXPLICA]

**¿Por qué `createGuestSession()` tiene su propia anotación `@Transactional` si la clase ya tiene `@Transactional(readOnly = true)`? ¿Qué pasaría si se quita esa anotación del método?**

**Respuesta:**  
La anotación `@Transactional` en el método (línea 50, sin `readOnly`) **sobreescribe** el comportamiento heredado de la clase para ese método específico. `createGuestSession()` crea una `UserSession` y un `Cart` en BD (operaciones de escritura tipo `INSERT`). Si solo tuviera el `readOnly = true` heredado, Spring configuraría la transacción como de solo lectura. Dependiendo del driver/configuración, el intento de `INSERT` dentro de una transacción `readOnly` lanzaría un error porque la transacción no permite escrituras en BD.

**Archivo:** `src/main/java/co/edu/cesde/pps/application/AuthApplicationService.java` — líneas 50–55

---

### Pregunta 15.3 🟡 [QUÉ PASA SI]

**¿Qué pasa si `register()` lanza una excepción después de crear el usuario en BD pero antes de crear la sesión? ¿El usuario queda guardado sin sesión?**

**Respuesta:**  
No. El método `register()` tiene `@Transactional` (línea 57), que garantiza que todo el método se ejecuta dentro de **una sola transacción**. Si ocurre cualquier excepción en cualquier punto (incluyendo entre el `INSERT` del usuario y el `INSERT` de la sesión), Spring hace **rollback automático** de toda la transacción: el usuario se revierte, la sesión nunca se guardó, y la BD queda exactamente igual que antes de llamar al método. Nunca puede existir un usuario sin sesión inicial como resultado de un registro parcialmente fallido. Esto es fundamental para la consistencia de datos.

**Archivo:** `src/main/java/co/edu/cesde/pps/application/AuthApplicationService.java` — líneas 57–75

---

### Pregunta 15.4 🟡 [QUÉ PASA SI]

**Si quitas `@Transactional` del método `checkout()` en `OrderApplicationService`, ¿qué problema de consistencia puede ocurrir? Da un ejemplo concreto.**

**Respuesta:**  
El checkout implica múltiples operaciones en BD: decrementar stock de productos, marcar el carrito como `CONVERTED`, crear la `Order` y sus `OrderItem`. Sin `@Transactional`, cada operación de escritura se ejecuta en su propia transacción independiente (autocommit). Ejemplo concreto de fallo: el sistema decrementa el stock del producto (commit), pero luego la BD falla al guardar la `Order` (por ejemplo, un error de constraint). Resultado: el stock quedó decrementado pero no hay orden registrada. El producto desapareció del inventario sin que exista una compra. Con `@Transactional`, todo se revierte si cualquier paso falla.

**Archivo:** `src/main/java/co/edu/cesde/pps/application/OrderApplicationService.java` — línea 32

---

### Pregunta 15.5 🔴 [EXPLICA]

**¿Por qué es mejor práctica poner `@Transactional(readOnly = true)` a nivel de clase y `@Transactional` solo en los métodos que escriben, en lugar de poner `@Transactional` en todos los métodos sin distinción?**

**Respuesta:**  
Tiene impacto en rendimiento, seguridad y claridad del código:  
1. **Rendimiento:** `readOnly = true` desactiva el dirty-checking de Hibernate en los métodos de lectura. No se generan snapshots de los objetos cargados para comparar cambios al hacer flush, reduciendo consumo de memoria y CPU.  
2. **Protección contra errores:** si por error alguien modifica un objeto dentro de un método marcado `readOnly`, Hibernate no generará el `UPDATE` (actúa como salvaguarda contra escrituras accidentales en métodos de lectura).  
3. **Optimización de BD:** configuraciones avanzadas pueden redirigir transacciones `readOnly` a réplicas de lectura, distribuyendo la carga del servidor.  
4. **Intencionalidad explícita:** obliga al desarrollador a pensar conscientemente cuándo está escribiendo datos (debe agregar `@Transactional` explícitamente), haciendo el código más autodocumentado y fácil de revisar.

**Archivo:** `src/main/java/co/edu/cesde/pps/application/AuthApplicationService.java` — líneas 27–57 | `src/main/java/co/edu/cesde/pps/application/CartApplicationService.java` — líneas 20–43

---

*Banco de preguntas generado el 2026-05-27. Basado en el código fuente de la aplicación Product Purchasing System — Spring Boot.*

