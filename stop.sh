#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PIDS_FILE="$SCRIPT_DIR/.service-pids"

if [ ! -f "$PIDS_FILE" ]; then
  echo "No se encontró .service-pids. ¿Corriste start.sh?"
  exit 1
fi

echo "Deteniendo microservicios..."

while IFS=: read -r SERVICE PID; do
  if kill -0 "$PID" 2>/dev/null; then
    kill "$PID"
    echo "  [OK] $SERVICE (PID $PID) detenido"
  else
    echo "  [SKIP] $SERVICE (PID $PID) ya no estaba corriendo"
  fi
done < "$PIDS_FILE"

rm -f "$PIDS_FILE"
echo "Listo."
