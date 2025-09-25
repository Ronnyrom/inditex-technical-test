# Asset Manager

Servicio Spring Boot para gestionar assets (imágenes, vídeos): subida de archivos y listado con filtros.

## Stack
- Java21, Spring Boot, Gradle
- Resilience4j (CircuitBreaker, Retry)
- Virtual Threads
- OpenAPI 3.0 (`src/main/resources/openapi.yml`)
- Spring security
## Arquitectura
- Enfoque por puertos y adaptadores (hexagonal)
- Subida asíncrona con resiliencia y fallback que marca el asset como `FAILED` si el almacenamiento falla.

## API
- POST `/api/mgmt/1/assets/actions/upload` → 202 Accepted. Body: `filename`, `encodedFile` (base64), `contentType`.
- GET `/api/mgmt/1/assets/` → 200 OK. Filtros: `uploadDateStart`, `uploadDateEnd`, `filename`, `filetype`, `sortDirection`.
- GET `/auth/login` → 200 OK. Body: `username`, `password`. Respuesta: JWT.'

La especificación completa está en `src/main/resources/openapi.yml`.

## Ejecución con Docker

Puedes iniciar el servicio y sus dependencias usando Docker Compose:

```bash
docker-compose up

```
## Funcionalidad WebFlux

El proyecto cuenta con una rama adicional llamada `feature/webflux` donde está implementada la funcionalidad usando Spring WebFlux.
