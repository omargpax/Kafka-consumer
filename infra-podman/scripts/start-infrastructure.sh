#!/bin/bash

set -e

echo ""
echo "############################################################"
echo "Creando red Podman: votes-net"
echo "############################################################"

podman network exists votes-net || podman network create votes-net

echo ""
echo "############################################################"
echo "Descargando imagenes sin verificar certificados"
echo "############################################################"

podman pull --tls-verify=false docker.io/bitnami/postgresql:latest
podman pull --tls-verify=false docker.io/confluentinc/cp-zookeeper:7.7.1
podman pull --tls-verify=false docker.io/confluentinc/cp-kafka:7.7.1
podman pull --tls-verify=false docker.io/confluentinc/cp-schema-registry:7.7.1
podman pull --tls-verify=false docker.io/confluentinc/cp-enterprise-control-center:7.7.1

echo ""
echo "############################################################"
echo "Levantando infraestructura Kafka + PostgreSQL"
echo "############################################################"

podman compose \
    -p votes-infrastructure \
    -f compose/01-kafka-server.yml \
    -f compose/02-postgres.yml \
    up -d

echo ""
echo "############################################################"
echo "Esperando Kafka..."
echo "############################################################"

sleep 25

echo ""
echo "############################################################"
echo "Creando topics Kafka"
echo "############################################################"

podman exec kafka-broker-1 \
kafka-topics \
--bootstrap-server localhost:19092 \
--if-not-exists \
--create \
--topic votes-topic \
--partitions 1 \
--replication-factor 1

podman exec kafka-broker-1 \
kafka-topics \
--bootstrap-server localhost:19092 \
--if-not-exists \
--create \
--topic votes-confirmed-topic \
--partitions 1 \
--replication-factor 1

echo ""
echo "############################################################"
echo "Infraestructura iniciada correctamente"
echo "############################################################"