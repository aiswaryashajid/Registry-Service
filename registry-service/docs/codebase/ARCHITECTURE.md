# Architecture

## Core Sections (Required)

### 1) Architectural Style

- Primary style: Classic layered Spring Boot REST service (Controller → Service → Repository → DB), with a security filter and outbound HTTP clients as cross-cutting concerns.
- Why this classification: packages are organized by technical layer; `VehicleRegistrationController` delegates to `VehicleRegistrationService`, which uses `VehicleRegistrationRepository`/`VehicleSpecification` over a JPA `Vehicle` entity. Evidence: controller/service/Repository/entity packages.
- Primary constraints: (1) stateless request authentication delegated to an external auth service via a per-request filter; (2) all errors funneled through one `@RestControllerAdvice`; (3) dynamic queries via JPA Specification rather than custom query methods.

### 2) System Flow

```text
HTTP request
  -> JwtAuthFilter (validate Bearer token via SecurityClient -> ExternalApiClient -> external auth svc)
  -> Spring Security chain (/api/v1/** authenticated) + @PreAuthorize role check
  -> VehicleRegistrationController (validate @RequestBody / params)
  -> VehicleRegistrationService (business rules, exceptions)
  -> VehicleRegistrationRepository (+ VehicleSpecification) -> H2 (vehicles table)
  -> response mapped to VehicleResponse / PaginatedResponseDto
  (any exception -> VehicleRegistrationGlobalExceptionHandler -> ApiErrorResponse JSON)
```

### 3) Layer/Module Responsibilities

| Layer or module | Owns | Must not own | Evidence |
|-----------------|------|--------------|----------|
| Controller | endpoint routing, validation trigger (@Valid), role guards, DTO mapping | persistence, business rules | controller/VehicleRegistrationController.java |
| Service | business rules (duplicate check, fetch-or-throw, sort), orchestration | HTTP, SQL strings | service/VehicleRegistrationService.java |
| Repository + spec | CRUD + dynamic filtering | business decisions | Repository/VehicleRegistrationRepository.java, Repository/spec/VehicleSpecification.java |
| Entity | DB mapping for `vehicles` | transport shape | entity/Vehicle.java |
| Filter | per-request token validation, SecurityContext population | business logic | filters/JwtAuthFilter.java |
| Clients | outbound HTTP + exception translation | controller/service logic | client/SecurityClient.java, client/ExternalApiClient.java |
| Exception advice | exception → HTTP status + ApiErrorResponse | throwing domain exceptions | exception/VehicleRegistrationGlobalExceptionHandler.java |

### 4) Reused Patterns

| Pattern | Where found | Why it exists |
|---------|-------------|---------------|
| Layered architecture | whole codebase | separation of concerns |
| Repository + Specification | Repository/spec/VehicleSpecification.java | dynamic, optional filters without query explosion |
| Centralized exception handling (advice) | exception/VehicleRegistrationGlobalExceptionHandler.java | uniform error envelope |
| Adapter/wrapper around WebClient | client/ExternalApiClient.java | reusable blocking GET with timeout + error mapping |
| Filter-based authentication | filters/JwtAuthFilter.java | delegate token validation to external service |
| DTO + builder | dto/ApiErrorResponse.java | decouple transport from entity |
| Utility class (private ctor) | VehicleSpecification, VehicleServiveConstants, ApiErrorExamples | static-only helpers |

### 5) Known Architectural Risks

- External auth URL is hardcoded to `localhost:8088` (SecurityClient.java:20) — not environment-aware; breaks outside local.
- Client-layer exceptions (ClientError/ServiceDown/TimeOut/InternalAuth) have **no dedicated MVC `@ExceptionHandler`** — inside the MVC layer they fall through to the generic 500; only `JwtAuthFilter` maps them to 401/503/504 during the filter stage.
- No `@Transactional` boundaries in the service; relies on Spring Data per-method transactions.
- VIN uniqueness enforced only in application code (no DB unique constraint, no `@Version`) → race risk under concurrency.
- `anyRequest().permitAll()` catch-all (SecurityConfig.java:33) leaves non-`/api/v1/**` paths open.

### 6) Evidence

- src/main/java/com/vehicle/registry_service/RegistryServiceApplication.java
- src/main/java/com/vehicle/registry_service/{controller,service,Repository,filters,client,exception}/**
- graphify-out/GRAPH_REPORT.md (communities, god nodes), docs/CODEBASE_MAP.md (feature flows)
