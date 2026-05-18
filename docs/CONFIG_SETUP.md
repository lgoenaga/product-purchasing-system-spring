# Configuración del ambiente - Spring Boot + `.env`

## 📋 Alcance de esta guía

Esta guía documenta únicamente la configuración que sigue vigente en el proyecto:

- Spring Boot
- `application.yml`
- carga local de `.env`
- variables de entorno para DDL y logging
- uso de `APP_ENVIRONMENT` como apoyo de configuración

### Ya no hacen parte del flujo activo
- `DatabaseConfig.java`
- `JpaConfig.java`
- `TransactionManager.java`
- `src/main/resources/META-INF/persistence.xml`

---

## ✅ Supuestos previos

La conectividad base ya fue validada previamente, por lo tanto esta guía **no repite**:

- instalación del motor MySQL
- creación manual de base de datos
- validación del usuario de conexión
- pruebas del motor o del esquema

Esta documentación parte de que el ambiente de base de datos ya existe y ya fue probado.

---

## 🔧 Flujo vigente de configuración

### 1. Crear el archivo local `.env`

```bash
cp .env.example .env
```

### 2. Ajustar únicamente los valores reales del ambiente

El archivo `.env.example` quedó orientado a despliegue/configuración controlada.  
El archivo `.env` local es donde se asignan los valores reales del ambiente.

Variables relevantes:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `DB_DDL_AUTO`
- `DB_SHOW_SQL`
- `DB_POOL_SIZE`
- `APP_ENVIRONMENT`
- `LOG_LEVEL`
- `LOG_SQL_LEVEL`
- `LOG_SQL_BIND_LEVEL`

---

## 🚀 Cómo se carga `.env`

La aplicación carga `.env` al iniciar mediante:

- la dependencia `java-dotenv`
- la clase `DotenvDevelopmentLoader`
- el arranque desde `PpsApplication`

### Orden de prioridad
Si una variable ya existe como:

- variable de entorno real del sistema, o
- `system property`

entonces **no se sobrescribe** con el valor del archivo `.env`.

Esto permite:

- usar `.env` en desarrollo local
- usar variables reales del servidor en otros ambientes

---

## ⚙️ Configuración activa del proyecto

La configuración activa está en:

- `src/main/resources/application.yml`

Spring Boot resuelve desde allí:

- datasource
- JPA / Hibernate
- Hikari pool
- logging

### Fragmento vigente

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:pps_db}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: ${DB_USER:user_pps}
    password: ${DB_PASSWORD:}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: ${DB_POOL_SIZE:10}
  jpa:
    hibernate:
      ddl-auto: ${DB_DDL_AUTO:update}
    open-in-view: false
    show-sql: ${DB_SHOW_SQL:true}

logging:
  level:
    co.edu.cesde.pps: ${LOG_LEVEL:DEBUG}
    org.hibernate.SQL: ${LOG_SQL_LEVEL:DEBUG}
    org.hibernate.orm.jdbc.bind: ${LOG_SQL_BIND_LEVEL:TRACE}
```

---

## 🗂️ Scripts SQL disponibles

Se mantienen como referencia operativa:

- `src/main/resources/sql/schema.sql`
- `src/main/resources/sql/data.sql`

Estos archivos pueden seguir usándose como apoyo del proyecto, pero la guía actual ya no documenta la creación inicial del motor/base/usuario.

---

## 🌎 Variables recomendadas por ambiente

### Producción
```dotenv
DB_DDL_AUTO=none
DB_SHOW_SQL=false
APP_ENVIRONMENT=production
LOG_LEVEL=INFO
LOG_SQL_LEVEL=WARN
LOG_SQL_BIND_LEVEL=OFF
```

### Desarrollo controlado
```dotenv
DB_DDL_AUTO=update
DB_SHOW_SQL=true
APP_ENVIRONMENT=development
LOG_LEVEL=DEBUG
LOG_SQL_LEVEL=DEBUG
LOG_SQL_BIND_LEVEL=TRACE
```

---

## 🐛 Troubleshooting vigente

### `.env` no se refleja
Validar:
- que `.env` exista en la raíz del proyecto
- que `PpsApplication` invoque `DotenvDevelopmentLoader.load()`
- que la variable no exista ya en el entorno con otro valor

### La aplicación no arranca
Validar:

```bash
mvn clean compile
```

Y revisar:
- valores obligatorios del datasource
- disponibilidad del ambiente de base de datos
- configuración de `DB_DDL_AUTO`

### El logging no coincide con lo esperado
Revisar:
- `LOG_LEVEL`
- `LOG_SQL_LEVEL`
- `LOG_SQL_BIND_LEVEL`
- `APP_ENVIRONMENT`

---

## 📝 Notas importantes

1. `application.yml` es la fuente principal de configuración.
2. `.env` se usa como apoyo práctico para desarrollo local.
3. `APP_ENVIRONMENT` permanece como variable auxiliar de configuración.
4. `DatabaseConfig`, `JpaConfig` y `TransactionManager` quedan solo como histórico.
5. `persistence.xml` ya no debe volver al classpath activo.

---

## ✅ Checklist

- [ ] `.env` creado desde `.env.example`
- [ ] valores reales del ambiente configurados
- [ ] `application.yml` usado como fuente principal
- [ ] `.env` cargado por `DotenvDevelopmentLoader`
- [ ] `APP_ENVIRONMENT` definido según el ambiente
- [ ] `DB_DDL_AUTO` definido correctamente
- [ ] logging ajustado con `LOG_LEVEL`, `LOG_SQL_LEVEL` y `LOG_SQL_BIND_LEVEL`
- [ ] `DatabaseConfig`, `JpaConfig` y `TransactionManager` sin uso activo
- [ ] `persistence.xml` fuera del flujo activo
