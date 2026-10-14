# kora-api-consumer

Consumidor de eventos de Kundu API. Lee `user-invitation-topic` y guarda las invitaciones nuevas en `tax_invitations`, sin duplicar correos ya registrados como usuarios o invitados.
Expone un único endpoint protegido con el JWT de Kundu API para listar los invitados que aún no se han registrado.

## Requisitos

- Java 21
- Maven 3.9+ (o el wrapper `./mvnw`)
- PostgreSQL con la base `dbo_kundu` ya cargada por Kundu API
- Kafka y Schema Registry en `localhost:19092` y `localhost:8081`, con el topic `user-invitation-topic`
- Kundu API en ejecución (para obtener el token JWT)

## Quick start

1. Ejecutar:
   ```bash
   ./mvnw clean quarkus:dev
   ```
2. Obtener un token en Kundu API (clave de ejemplo: `Kundu2025!`):
   ```bash
   curl -X POST http://localhost:8080/api/auth/authenticate \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"Kundu2025!"}'
   ```
3. Listar los invitados por registrar (reemplaza `<TOKEN>`):
   ```bash
   curl "http://localhost:8183/api/invitations/pending" -H "Authorization: Bearer <TOKEN>"
   ```

API en `http://localhost:8183`. La configuración está en `src/main/resources/application.yml` y se sobrescribe con variables de entorno (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET_KEY`, `KAFKA_BOOTSTRAP_SERVERS`, `SCHEMA_REGISTRY_URL`). `JWT_SECRET_KEY` debe ser la misma que usa Kundu API.