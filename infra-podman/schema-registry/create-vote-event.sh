#!/bin/bash

# Configura esto según tu entorno
SCHEMA_REGISTRY_URL="http://localhost:8081"
TOPIC_NAME="votes-topic"
SUBJECT_NAME="${TOPIC_NAME}-value"
SCHEMA_FILE="VoteEvent.avsc"

# Verifica que el archivo exista
if [ ! -f "$SCHEMA_FILE" ]; then
  echo "❌ Error: No se encontró el archivo $SCHEMA_FILE"
  exit 1
fi

echo "📤 Registrando esquema para '$SUBJECT_NAME' en $SCHEMA_REGISTRY_URL..."

# Envía el schema al registry
curl -X POST "${SCHEMA_REGISTRY_URL}/subjects/${SUBJECT_NAME}/versions" \
     -H "Content-Type: application/vnd.schemaregistry.v1+json" \
     -d @"$SCHEMA_FILE"

echo -e "\n✅ Registro completado (si no hubo errores arriba)."
