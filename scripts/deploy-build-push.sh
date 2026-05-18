#!/usr/bin/env bash
# ============================================
# deploy-build-push.sh
# Construye la imagen Docker y la sube a Docker Hub
# ============================================
# Uso:
#   ./scripts/deploy-build-push.sh           → usa version "latest"
#   ./scripts/deploy-build-push.sh 1.0.1     → usa version especificada
# ============================================

set -euo pipefail

DOCKER_USER="lgoenaga"
IMAGE_NAME="backend-wisegrade"
FULL_IMAGE="${DOCKER_USER}/${IMAGE_NAME}"
VERSION="${1:-latest}"

echo ""
echo "============================================"
echo "  BUILD & PUSH — ${FULL_IMAGE}"
echo "  Versión: ${VERSION}"
echo "============================================"
echo ""

# ── 1. Verificar que Docker está corriendo ──────────────────
if ! docker info > /dev/null 2>&1; then
  echo "❌ Docker no está corriendo. Inicia Docker Desktop o el daemon."
  exit 1
fi

# ── 2. Verificar login en Docker Hub ───────────────────────
echo "🔐 Verificando sesión en Docker Hub..."
if ! docker info 2>/dev/null | grep -q "Username"; then
  echo "⚠️  No hay sesión activa. Iniciando login..."
  docker login
fi

# ── 3. Construir imagen ─────────────────────────────────────
echo ""
echo "🔨 Construyendo imagen ${FULL_IMAGE}:${VERSION}..."
docker build \
  --tag "${FULL_IMAGE}:${VERSION}" \
  --tag "${FULL_IMAGE}:latest" \
  --file Dockerfile \
  .

echo ""
echo "✅ Imagen construida:"
docker image inspect "${FULL_IMAGE}:${VERSION}" \
  --format "   Tamaño: {{.Size | printf \"%.0f\"}} bytes   |   ID: {{.Id | printf \"%.12s\"}}"

# ── 4. Subir a Docker Hub ───────────────────────────────────
echo ""
echo "🚀 Subiendo ${FULL_IMAGE}:${VERSION} a Docker Hub..."
docker push "${FULL_IMAGE}:${VERSION}"

if [ "${VERSION}" != "latest" ]; then
  echo "🚀 Subiendo ${FULL_IMAGE}:latest..."
  docker push "${FULL_IMAGE}:latest"
fi

echo ""
echo "============================================"
echo "  ✅ COMPLETADO"
echo "  Imagen disponible en:"
echo "  https://hub.docker.com/r/${DOCKER_USER}/${IMAGE_NAME}"
echo ""
echo "  En el servidor IONOS:"
echo "  docker compose -f docker-compose.prod.yml pull"
echo "  docker compose -f docker-compose.prod.yml up -d"
echo "============================================"
echo ""

