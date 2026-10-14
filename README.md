# Kafka-consumer

Kora API + consumo de eventos del producer `kundu-api-quarkus`.
Incluye la infraestructura Kafka en Podman (`infra-podman.zip`) y el consumidor `kora-api-consumer`, que guarda en base de datos las invitaciones publicadas en `user-invitation-topic`.

## Contenido

| Elemento | Descripción |
|---|---|
| `infra-podman.zip` | Infraestructura: Zookeeper, Kafka, Schema Registry, Control Center y PostgreSQL (Podman) |
| `kora-api-consumer/` | API consumidora de eventos (Quarkus) |

## Requisitos

- Podman con `podman compose` (en Windows, con la Podman machine iniciada)
- Git Bash (Windows) o una terminal Bash
- Java 21 y Maven 3.9+ (para `kora-api-consumer`)
- `kundu-api-quarkus` en ejecución, que es el producer de los eventos

## Quick start

1. Descomprimir `infra-podman.zip`:
   ```bash
   unzip infra-podman.zip
   cd infra-podman
   ```
2. Levantar la infraestructura (desde la raíz de `infra-podman`):
   ```bash
   bash scripts/start-infrastructure.sh
   ```
3. Verificar que todo esté arriba:
   ```bash
   podman ps
   ```
   - Control Center: http://localhost:9021
   - Schema Registry: http://localhost:8081/subjects
4. Si el script no crea el topic de invitaciones, créalo:
   ```bash
   podman exec kafka-broker-1 kafka-topics --bootstrap-server localhost:19092 --if-not-exists --create --topic user-invitation-topic --partitions 1 --replication-factor 1
   ```
5. Continuar con el consumidor: [kora-api-consumer/README.md](kora-api-consumer/README.md)