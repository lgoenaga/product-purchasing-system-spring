# Product Purchasing System — Backend API

Sistema de tienda online con API REST en Spring Boot. Proyecto educativo Backend II — CESDE 2026.

## 📋 Descripción

Sistema de compras que expone una API REST con:
- Gestión de usuarios, roles y autenticación
- Catálogo de productos y categorías jerárquicas
- Carrito de compras (invitados y usuarios registrados)
- Merge automático de carrito al registrarse
- Gestión de órdenes y pagos
- Módulo administrativo (usuarios y productos)

## 🛠️ Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.5 |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | MySQL 8 |
| Build | Maven 3.9 |
| Contenedores | Docker + Docker Compose |
| Seguridad | Spring Security Crypto (BCrypt) |

## 🗂️ Modelo E-R

14 entidades: `User`, `Role`, `Address`, `UserSession`, `Category`, `Product`, `Cart`, `CartItem`, `Order`, `OrderItem`, `OrderStatus`, `Payment`, `PaymentStatus`, `PaymentMethod`.

Documentación completa: `documents_external/er_model_documentation.md` (solo local, no versionado).

---

## 🖥️ Desarrollo Local

### Requisitos
- Java 21
- Maven 3.9+
- Docker + Docker Compose

### 1. Configurar variables de entorno
```bash
cp .env.example .env
# Editar .env con los valores de tu entorno local
```

### 2. Levantar MySQL local
```bash
cp .env.docker.example .env.dev.docker
# Editar .env.dev.docker con usuario y contraseña deseados
docker compose -f docker-compose.dev.yml up -d
```

### 3. Ejecutar la aplicación
```bash
mvn spring-boot:run
# La API queda disponible en http://localhost:8081
```

### Perfil demo (datos de prueba)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

**Credenciales demo:**
- Admin: `admin.demo@pps.com` / `Admin12345*`
- Customer: `customer.demo@pps.com` / `Customer12345*`

---

## 🚀 Despliegue en Producción (IONOS)

Ver [`PLAN-DEPLOY-PRODUCCION-IONOS.md`](PLAN-DEPLOY-PRODUCCION-IONOS.md) para el plan completo.

### Resumen rápido en el servidor
```bash
# 1. Clonar / actualizar
git pull origin main

# 2. Crear archivo de entorno (solo primera vez)
cp .env.prod.example .env.prod
nano .env.prod   # completar con valores reales

# 3. Construir y levantar
docker compose -f docker-compose.prod.yml up -d --build

# 4. Verificar
docker logs backend-wisegrade --tail 50 -f
```

---

## 📚 Documentación

| Archivo | Descripción |
|---|---|
| [`docs/BACKEND_ENDPOINTS_REFERENCE.md`](docs/BACKEND_ENDPOINTS_REFERENCE.md) | Referencia completa de endpoints REST |
| [`docs/CONFIG_SETUP.md`](docs/CONFIG_SETUP.md) | Guía de configuración |
| [`PLAN-DEPLOY-PRODUCCION-IONOS.md`](PLAN-DEPLOY-PRODUCCION-IONOS.md) | Plan de despliegue producción |
| `.env.example` | Plantilla variables de entorno desarrollo |
| `.env.prod.example` | Plantilla variables de entorno producción |

---

## 👤 Autor

Luis Goenaga — Proyecto educativo Backend II — CESDE 2026
