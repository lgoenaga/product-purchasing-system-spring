# Plan de Depuración y Despliegue en Producción — IONOS
**Fecha:** 2026-05-18  
**Estado:** ✅ Revisado — Listo para implementar

---

## Respuestas a las preguntas del plan anterior

| Pregunta | Respuesta | Impacto |
|---|---|---|
| ¿Hay reverse proxy? | ✅ Nginx ya configurado en el VPS | Puerto 8080 se expone, nginx proxea |
| ¿CORS con frontend en la misma red? | Frontend en `app-network`, acceso browser vía nginx | `CORS_ALLOWED_ORIGINS` = dominio público nginx |
| ¿Build en servidor o registry? | ✅ Build directo en servidor IONOS | `docker compose up --build` en el servidor |
| ¿Spring Actuator? | ❌ No está instalado, no es obligatorio | Cambiar healthcheck a endpoint real de la API |

### ¿Spring Actuator es obligatorio?
**No.** Spring Actuator es un módulo opcional que agrega endpoints como `/actuator/health`, `/actuator/info`, etc. para monitoreo. Sin él, el healthcheck del docker-compose usa directamente un endpoint real de la API. En este proyecto usaremos `GET /api/v1/categories` que es público, liviano y sin autenticación.

**¿Qué cambia sin Actuator?**
- Solo afecta el healthcheck del contenedor (comando docker interno, no visible externamente)
- La aplicación funciona exactamente igual
- Se puede agregar en el futuro sin cambios de configuración

### ¿Afecta algo en el local?

**Impacto mínimo:**

| Cambio | Efecto en dev local |
|---|---|
| `docker-compose.yml` → `docker-compose.dev.yml` | Hay que usar `-f docker-compose.dev.yml` al levantar MySQL local |
| Puerto default yml `8081` → `8080` | **Ninguno** — el `.env` local tiene `SERVER_PORT=8081` que sigue teniendo prioridad |
| Nuevos archivos `docker-compose.prod.yml`, `Dockerfile` | Ninguno, son archivos nuevos |
| `.env.prod` nuevo | Ninguno, solo se usa en producción |

**En resumen:** el único cambio que requiere adaptación es usar `docker compose -f docker-compose.dev.yml up -d` en lugar de `docker compose up -d` para el MySQL local.

---

## 1. Arquitectura Final en IONOS

```
Internet
    │
    ▼
┌─────────────────────────────────────────────────────────┐
│  SERVIDOR IONOS                                         │
│                                                         │
│  [Nginx — ya configurado] ← HTTPS/HTTP                  │
│     └── proxy_pass → backend-wisegrade:8080             │
│                                                         │
│  Docker Network: app-network (external, ya creada)      │
│                                                         │
│  ┌──────────────────┐    ┌──────────────────┐           │
│  │ backend-wisegrade│    │    mysql-db       │           │
│  │  Puerto: 8080    │───▶│ (ya existe,       │           │
│  │  (nueva imagen)  │    │  datos migrados)  │           │
│  └──────────────────┘    └──────────────────┘           │
│                                                         │
│  [frontend-container]  (también en app-network)         │
│                                                         │
└─────────────────────────────────────────────────────────┘

NOTA CORS: el browser accede via dominio nginx (URL pública)
→ CORS_ALLOWED_ORIGINS debe ser ese dominio, NO una IP interna Docker
```

**Ya existe y no cambia:**
- Contenedor `mysql-db` con datos migrados
- Red Docker `app-network` (external)
- Nginx configurado en el VPS
- Credenciales BD: `app_user` / `appL@gp2O26.` sobre BD `wisegrade`

**Nota sobre `java-dotenv`:** El `DotenvDevelopmentLoader` ya está diseñado correctamente — solo carga desde `.env` si la variable NO existe como OS env var. En Docker, las vars del `env_file:` son OS env vars → el loader las ignora automáticamente. No hay conflicto en producción. ✅

---

## 2. Archivos a Crear

### `Dockerfile`
```dockerfile
# ---------- BUILD STAGE ----------
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
RUN chown -R spring:spring /app

USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
```

- `21-jre-alpine` → solo JRE (no JDK completo), imagen más liviana
- `-Djava.security.egd=file:/dev/./urandom` → evita bloqueos de entropía en Alpine

---

### `docker-compose.prod.yml`
```yaml
services:
  backend-wisegrade:
    build: .
    container_name: backend-wisegrade
    env_file:
      - .env.prod
    ports:
      - "8080:8080"
    networks:
      - app-network
    restart: always
    healthcheck:
      # Sin Spring Actuator → usar endpoint público real de la API
      test: ["CMD-SHELL", "wget -qO- http://localhost:8080/api/v1/categories || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

networks:
  app-network:
    external: true
```

- Healthcheck usa `GET /api/v1/categories` (endpoint público, sin auth, sin Actuator)
- `restart: always` → se reinicia automáticamente si cae

---

### `.env.prod.example` ← va a Git
```dotenv
# ============================================
# Variables de Entorno — PRODUCCIÓN (PLANTILLA)
# ============================================
# Copiar como .env.prod en el servidor y completar valores reales.
# ESTE ARCHIVO va a Git — sin contraseñas.
# ============================================

DB_HOST=mysql-db
DB_PORT=3306
DB_NAME=wisegrade
DB_USER=CHANGE_ME
DB_PASSWORD=CHANGE_ME

DB_DDL_AUTO=none
DB_SHOW_SQL=false
DB_POOL_SIZE=10

SERVER_PORT=8080
APP_ENVIRONMENT=production

# Dominio público que expone nginx (lo que ve el browser)
# Ejemplo: https://wisegrade.com  ó  https://api.wisegrade.com
CORS_ALLOWED_ORIGINS=https://tu-dominio.com

LOG_LEVEL=INFO
LOG_SQL_LEVEL=WARN
LOG_SQL_BIND_LEVEL=OFF
```

---

### `.env.prod` ← SOLO EN EL SERVIDOR, nunca a Git
```dotenv
DB_HOST=mysql-db
DB_PORT=3306
DB_NAME=wisegrade
DB_USER=app_user
DB_PASSWORD=appL@gp2O26.

DB_DDL_AUTO=none
DB_SHOW_SQL=false
DB_POOL_SIZE=10

SERVER_PORT=8080
APP_ENVIRONMENT=production

# ⚠️ Reemplazar con el dominio real que sirve nginx
CORS_ALLOWED_ORIGINS=https://tu-dominio.com

LOG_LEVEL=INFO
LOG_SQL_LEVEL=WARN
LOG_SQL_BIND_LEVEL=OFF
```

---

## 3. Archivos a Modificar

### `application.yml` — cambio mínimo
```yaml
server:
  port: ${SERVER_PORT:8080}   # era 8081 → ahora 8080 como default de producción
```
> El `.env` local tiene `SERVER_PORT=8081` → dev local sigue usando 8081, sin tocar nada. ✅

---

### `.gitignore` — agregar entradas
```gitignore
# Entornos de producción (NUNCA a Git)
.env.prod
.env.dev.docker

# Documentación histórica de etapas
docs/history/

# Conservar plantillas
!.env.prod.example
```

---

### `docker-compose.yml` → renombrar a `docker-compose.dev.yml`
Comentario interno actualizado:
```yaml
# Arrancar MySQL local: docker compose -f docker-compose.dev.yml up -d
# Detener:             docker compose -f docker-compose.dev.yml down
```

---

## 4. Reorganización — Limpieza del raíz

```
Crear: docs/history/

git mv ETAPA01_SUMMARY.md  docs/history/
git mv ETAPA02_SUMMARY.md  docs/history/
git mv ETAPA03_SUMMARY.md  docs/history/
git mv ETAPA04_SUMMARY.md  docs/history/
git mv ETAPA05_SUMMARY.md  docs/history/
git mv ETAPA06_SUMMARY.md  docs/history/
git mv ETAPA07_SUMMARY.md  docs/history/
git mv ETAPA08_SUMMARY.md  docs/history/
git mv ETAPA09_SUMMARY.md  docs/history/
git mv ETAPA10_SUMMARY.md  docs/history/
git mv ETAPA11_SUMMARY.md  docs/history/
git mv ETAPA12_SUMMARY.md  docs/history/
git mv ETAPA13_SUMMARY.md  docs/history/
git mv ETAPA14_SUMMARY.md  docs/history/
git mv ETAPA15_SUMMARY.md  docs/history/
git mv ETAPA16_SUMMARY.md  docs/history/
git mv PLAN-UNIFICACION-MYSQL.md       docs/history/
git mv BACKEND_ENDPOINTS_REFRENCE.md   docs/BACKEND_ENDPOINTS_REFERENCE.md  ← corregir typo
git mv CONFIG_SETUP.md                 docs/CONFIG_SETUP.md
```

`docs/history/` queda en `.gitignore` → archivos quedan localmente pero no se publican en Git.

---

## 5. Estructura Final del Proyecto

```
product-purchasing-system-spring/
├── src/
├── docs/
│   ├── history/                    ← en .gitignore (ETAPA*)
│   ├── BACKEND_ENDPOINTS_REFERENCE.md
│   └── CONFIG_SETUP.md
├── documents_external/             ← ya en .gitignore
├── .env                            ← dev local, en .gitignore
├── .env.example                    ← plantilla dev, en Git
├── .env.prod                       ← SOLO EN SERVIDOR, en .gitignore
├── .env.prod.example               ← plantilla producción, en Git ✅ nuevo
├── .env.dev.docker                 ← (renombrado), en .gitignore
├── .env.docker.example             ← plantilla MySQL dev, en Git
├── .gitignore
├── docker-compose.dev.yml          ← dev local (levanta MySQL) ← renombrado
├── docker-compose.prod.yml         ← producción IONOS ✅ nuevo
├── Dockerfile                      ✅ nuevo
├── pom.xml
├── README.md
└── PLAN-DEPLOY-PRODUCCION-IONOS.md
```

---

## 6. Procedimiento de Despliegue en IONOS

### En máquina local — preparar y subir código
```bash
# 1. Verificar que compila
mvn clean package -DskipTests

# 2. Commit y push
git add .
git commit -m "chore: docker producción IONOS + reorganización docs"
git push origin main
```

### En el servidor IONOS
```bash
# 1. Ir al directorio del proyecto (o clonar si es primera vez)
cd /ruta/del/proyecto
git pull origin main

# 2. Crear .env.prod (solo la primera vez)
cp .env.prod.example .env.prod
nano .env.prod
# → completar CORS_ALLOWED_ORIGINS con el dominio real de nginx

# 3. Verificar prerrequisitos
docker network ls | grep app-network
docker ps | grep mysql-db

# 4. Construir y levantar (~3–5 min primera vez por build Maven)
docker compose -f docker-compose.prod.yml up -d --build

# 5. Verificar
docker ps | grep backend-wisegrade
docker logs backend-wisegrade --tail 100 -f
```

### Actualizar después de cambios en el código
```bash
git pull origin main
docker compose -f docker-compose.prod.yml up -d --build
# Docker usa el layer cache del pom.xml → solo recompila si cambió el código
```

---

## 7. Seguridad

| Item | Estado |
|---|---|
| `.env.prod` fuera de Git | ✅ en .gitignore |
| Usuario no-root en contenedor | ✅ `spring:spring` |
| `DB_DDL_AUTO=none` en producción | ✅ |
| SQL no visible en logs producción | ✅ `DB_SHOW_SQL=false` |
| HTTPS via nginx | ✅ ya configurado |
| Contraseña BD robusta | ✅ `appL@gp2O26.` |
| Spring Actuator expuesto | ✅ No instalado = no expuesto |

---

## 8. Pendiente — Solo falta confirmar

> **⚠️ Necesario antes de iniciar implementación:**
>
> **¿Cuál es el dominio real del frontend (URL que ve el browser vía nginx)?**  
> Por ejemplo: `https://wisegrade.com` o `https://app.wisegrade.com`
>
> Este valor va en `CORS_ALLOWED_ORIGINS` del `.env.prod`.  
> Sin esto la API bloqueará todas las peticiones del frontend en producción.

---

## 9. Pendientes Futuros

- CI/CD automático (GitHub Actions → build → deploy en servidor)
- Eliminar `java-dotenv` del jar de producción (perfil Maven `provided`)
- Spring Boot Actuator para healthchecks y métricas avanzadas
- Logs persistentes del contenedor mediante volumen o servicio externo

---

*Implementación lista para iniciar tras confirmar el dominio del punto 8.*

