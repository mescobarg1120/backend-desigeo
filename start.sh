#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOGS_DIR="$SCRIPT_DIR/logs"
PIDS_FILE="$SCRIPT_DIR/.service-pids"

mkdir -p "$LOGS_DIR"
> "$PIDS_FILE"

SERVICES=(
  "api-gateway"
  "desigeo-auth-service"
  "gestion-de-usuarios"
  "gestion-de-reportes"
  "desigeo-support-service"
  "desigeo-ai-agent-service"
  "desigeo-notification-service"
  "desigeo-analytics-service"
  "desigeo-report-service"
)

echo "Iniciando microservicios..."

for SERVICE in "${SERVICES[@]}"; do
  SERVICE_DIR="$SCRIPT_DIR/$SERVICE"
  if [ ! -d "$SERVICE_DIR" ]; then
    echo "  [SKIP] $SERVICE — directorio no encontrado"
    continue
  fi

  LOG_FILE="$LOGS_DIR/$SERVICE.log"
  cd "$SERVICE_DIR"
  ./mvnw spring-boot:run > "$LOG_FILE" 2>&1 &
  PID=$!
  echo "$SERVICE:$PID" >> "$PIDS_FILE"
  echo "  [OK] $SERVICE (PID $PID) → logs/$SERVICE.log"
done

echo ""
echo "Todos los servicios iniciados. Para detenerlos: ./stop.sh"
echo "Para ver logs en tiempo real: tail -f logs/<servicio>.log"
