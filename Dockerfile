# ============================================
# Dockerfile — product-purchasing-system
# ============================================
# Build multi-stage:
#   Stage 1 (builder): compila el JAR con Maven
#   Stage 2 (runtime): imagen mínima JRE Alpine
# ============================================

# ---------- BUILD STAGE ----------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Descargar dependencias primero (aprovecha cache de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:21-jre-alpine

# Usuario seguro no-root
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

RUN chown -R spring:spring /app

USER spring

EXPOSE 8080

# -Djava.security.egd evita bloqueos de entropía en Alpine
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

