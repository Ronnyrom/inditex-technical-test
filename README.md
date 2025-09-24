# Asset Manager

Servicio Spring Boot para gestionar assets (imágenes, vídeos): subida de archivos y listado con filtros.

## Stack
- Java, Spring Boot, Gradle
- Resilience4j (CircuitBreaker, Retry)
- Virtual Threads (Executor)
- OpenAPI 3.0 (`src/main/resources/openapi.yml`)
## Arquitectura
- Enfoque por puertos y adaptadores (hexagonal): `AssetRepositoryPort`, `StoragePort`.
- Subida asíncrona con resiliencia y fallback que marca el asset como `FAILED` si el almacenamiento falla.

## API
- POST `/api/mgmt/1/assets/actions/upload` → 202 Accepted. Body: `filename`, `encodedFile` (base64), `contentType`.
- GET `/api/mgmt/1/assets/` → 200 OK. Filtros: `uploadDateStart`, `uploadDateEnd`, `filename`, `filetype`, `sortDirection`.

La especificación completa está en `src/main/resources/openapi.yml`.
