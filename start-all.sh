#!/bin/bash

# Script para levantar todos los microservicios de DESIGEO
# Uso: ./start-all.sh

ROOT_DIR=$(pwd)

start_service() {
  local service=$1
  echo "🚀 Iniciando $service..."
  cd "$ROOT_DIR/$service"
  export $(cat .env | xargs) 2>/dev/null
  ./mvnw spring-boot:run &
  cd "$ROOT_DIR"
}

start_service "api-gateway"
start_service "desigeo-auth-service"
start_service "gestion-de-usuarios"

echo "✅ Todos los servicios iniciados"
echo "Para detenerlos usa: kill \$(lsof -ti :8080,8081,8087)"