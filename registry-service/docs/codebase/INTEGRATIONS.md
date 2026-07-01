# External Integrations

## Core Sections (Required)

### 1) Integration Inventory

| System | Type | Purpose | Auth model | Criticality | Evidence |
|--------|------|---------|------------|-------------|----------|
| External Auth/Token service | HTTP API (GET) | Validate Bearer JWT, return sub + role | Forwards inbound `Authorization` header | High (every /api/v1 request depends on it) | client/SecurityClient.java:20-34 |
| H2 database | Embedded SQL DB | Persist `vehicles` records | username `sa`, empty password (in-memory) | High | application.properties:14-17 |
| SpringDoc OpenAPI / Swagger UI | Library/UI | API docs + bearerAuth scheme | n/a (docs) | Low | configuration/OpenApiConfig.java |

### 2) Data Stores

| Store | Role | Access layer | Key risk | Evidence |
|-------|------|--------------|----------|----------|
| H2 (in-memory `vehicle-db`) | primary datastore | VehicleRegistrationRepository + VehicleSpecification | data lost on restart; not production-grade | application.properties:14, Repository/** |

### 3) Secrets and Credentials Handling

- Credential sources: none externalized. DB creds (`sa`/empty) are in application.properties; auth URL hardcoded in source.
- Hardcoding checks: **hardcoded** auth endpoint `http://localhost:8088/v1/public/auth/validate` (SecurityClient.java:20). No secrets manager / env binding.
- Rotation or lifecycle notes: unknown. `[ASK USER]` how are auth-service URL and any API credentials managed per environment?

### 4) Reliability and Failure Behavior

- Retry/backoff: none (single blocking call, no retry).
- Timeout policy: 5s Reactor Mono timeout in `ExternalApiClient.get()` (ExternalApiClient.java:35). No HTTP/TCP-level (Netty) timeout on the WebClient bean (WebClientConfig).
- Circuit-breaker/fallback: none. Failures surface as mapped exceptions:
  - 4xx/5xx from auth → `ClientErrorException` → filter returns 401.
  - network down → `ServiceDownException` → filter returns 503.
  - timeout → `TimeOutException` → filter returns 504.
  - unexpected → `InternalAuthException`/Exception → 500.

### 5) Observability for Integrations

- Logging around external calls: yes — `SecurityClient` logs start/response; `ExternalApiClient` logs error status/body and network errors. `JwtAuthFilter` logs auth outcomes.
- Metrics/tracing: none (no Micrometer/Sleuth/OpenTelemetry dependency).
- Missing visibility gaps: no metrics/tracing; response logging may leak token-validation payload (sub/role) at INFO level.

### 6) Evidence

- src/main/java/com/vehicle/registry_service/client/SecurityClient.java
- src/main/java/com/vehicle/registry_service/client/ExternalApiClient.java
- src/main/java/com/vehicle/registry_service/configuration/WebClientConfig.java
- src/main/resources/application.properties
