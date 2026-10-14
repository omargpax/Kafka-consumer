#!/bin/bash

set -e

echo ""
echo "############################################################"
echo "Deteniendo infraestructura"
echo "############################################################"

podman compose \
    -f compose/01-kafka-server.yml \
    -f compose/02-postgres.yml \
    -p votes-infrastructure \
    down

echo ""
echo "############################################################"
echo "Eliminando red votes-net"
echo "############################################################"

if podman network exists votes-net; then
    podman network rm votes-net
fi

echo ""
echo "############################################################"
echo "Infraestructura eliminada correctamente"
echo "############################################################"