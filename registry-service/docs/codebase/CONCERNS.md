# Codebase Concerns

## Core Sections (Required)

### 1) Security Risks

| Risk | Impact | Evidence |
|------|--------|----------|
| Hardcoded external auth URL `http://localhost:8088/...` | Auth fails in any non-local environment; cannot rotate/point per env | client/SecurityClient.java:20 |
| `.well-known` bypass uses `contains()` not `startsWith()` | A crafted path like `/api/v1/.well-known/x` skips JWT auth | filters/JwtAuthFilter.java:41 |
| `anyRequest().permitAll()` catch-all | Any path outside `/api/v1/**` is unauthenticated | configuration/SecurityConfig.java:33 |
| Token-validation response logged at INFO | Possible leakage of `sub`/`role` into logs | client/SecurityClient.java:33 |
| DB creds in plaintext (`sa`/empty) | Acceptable for in-memory H2 only; not for prod | application.properties:16-17 |

### 2) Tech Debt / Correctness

| Item | Impact | Evidence |
|------|--------|----------|
| Client exceptions have no dedicated MVC `@ExceptionHandler` | ClientError/ServiceDown/TimeOut/InternalAuth map to generic 500 inside MVC; only filter stage yields 401/503/504 | exception/VehicleRegistrationGlobalExceptionHandler.java (6 handlers only) |
| Update validation gap | `VehicleUpdateRequest.ecuVersion` lacks `@Pattern` that register enforces → malformed version persists via PUT | dto/VehicleUpdateRequest.java:16-18 |
| VIN uniqueness app-level only | No `@Column(unique=true)` / `@Version` → race condition under concurrent registration | entity/Vehicle.java:18-19 |
| No `@Transactional` in service | Multi-step ops rely on Spring Data per-method tx; no service-level atomicity boundary | service/VehicleRegistrationService.java |
| `SORT_MODEL` constant unused | `"model"` is not in controller allowedSortFields → cannot sort by model despite the constant | controller/VehicleRegistrationController.java:192, constants/VehicleServiveConstants.java:10 |
| Filter-vs-LIKE inconsistency | `model` filter case-insensitive, `ecuVersion` filter case-sensitive | Repository/spec/VehicleSpecification.java:25-32 |
| Developer TODO comments | `// make as a seperate method`, `// use springUtil.copy property` unaddressed | service/VehicleRegistrationService.java:34,40 |

### 3) Reliability / Performance

| Item | Impact | Evidence |
|------|--------|----------|
| No HTTP/TCP-level timeout on WebClient | Only a 5s Mono timeout; a TCP-connect hang may not be bounded | configuration/WebClientConfig.java, client/ExternalApiClient.java:35 |
| No retry / circuit breaker on auth call | Transient auth-service blips fail requests outright | client/ExternalApiClient.java |
| `LIKE '%...%'` filters | Full table scans on `model`/`ecuVersion` at scale | Repository/spec/VehicleSpecification.java:27,31 |
| Blocking `.block()` on WebClient | Fine in servlet (Tomcat) container; would not suit a reactive stack | client/ExternalApiClient.java |

### 4) Naming Defects (cosmetic, but public surface)

- `Repository` package capitalized; `VehicleServiveConstants`, `updateVechicleDetails`, `webClinet()`, `authenticatioin` all misspelled. They are the real identifiers — must be used as-is, not "corrected" in code references.

### 5) Observability Gaps

- No metrics/tracing (no Micrometer/OpenTelemetry). Logging only.
- `SecurityClient` is excluded from JaCoCo coverage (pom.xml:144-148).

### 6) Test-Only / Coverage Gaps (not production debt)

- No end-to-end tests; controller slice disables the real auth filter (`addFilters=false`).
- No enforced coverage threshold.

### 7) Evidence

- src/main/java/com/vehicle/registry_service/{client,filters,configuration,service,Repository,entity,dto}/**
- src/main/resources/application.properties
- pom.xml
- docs/CODEBASE_MAP.md (Gotchas), graphify-out/GRAPH_REPORT.md

## Items requiring team intent ([ASK USER])
1. What is the production auth-service URL, and how should it be externalized (env var / config server)?
2. What production database replaces in-memory H2?
3. Should client-layer exceptions (timeout/service-down) surface distinct status codes from MVC controllers, or is the filter-stage handling sufficient?
4. Is there a target test-coverage threshold to enforce in CI?
