# Codebase Structure

## Core Sections (Required)

### 1) Top-Level Map

| Path | Purpose | Evidence |
|------|---------|----------|
| src/main/java/com/vehicle/registry_service/ | Application source (base package) | tree |
| src/main/resources/application.properties | Runtime config (H2, logging) | application.properties |
| src/test/java/com/vehicle/registry_service/ | Test suite (6 test classes) | tree |
| pom.xml | Maven build + dependencies | pom.xml |
| docs/ | Generated KT documentation | docs/ |

### 2) Entry Points

- Main runtime entry: `RegistryServiceApplication.main()` (`@SpringBootApplication`) — RegistryServiceApplication.java
- Secondary entry points (worker/cli/jobs): NONE
- How entry is selected: Spring Boot auto-configuration + component scanning of `com.vehicle.registry_service`

### 3) Module Boundaries (package layout)

| Boundary | What belongs here | What must not be here |
|----------|-------------------|------------------------|
| controller/ | REST endpoints, request/response mapping, @PreAuthorize | business logic, persistence |
| service/ | Business rules, orchestration | HTTP/web concerns, SQL |
| Repository/ (+ spec/) | Spring Data JPA repo + dynamic Specifications | business rules |
| entity/ | JPA @Entity (Vehicle → table `vehicles`) | DTO/transport concerns |
| dto/ | Request/response DTOs + ApiErrorResponse | persistence annotations |
| exception/ | Custom exceptions + @RestControllerAdvice handler | controller logic |
| filters/ | JwtAuthFilter (OncePerRequestFilter) | business logic |
| configuration/ | SecurityConfig, WebClientConfig, OpenApiConfig, ApiErrorExamples | runtime business logic |
| client/ | Outbound HTTP (SecurityClient, ExternalApiClient) | controller/service logic |
| constants/ | App-wide string constants | logic |

### 4) Naming and Organization Rules

- File naming pattern: PascalCase Java classes (e.g. `VehicleRegistrationController`); tests are `<ClassName>Test` (e.g. `VehicleRegistrationServiceTest`).
- Directory organization pattern: by layer/technical concern (controller, service, repository, dto, …), not by feature — single feature (Vehicle) so layer-organization is effective.
- Import/path conventions: standard Java package imports; no aliasing.
- Note (faithful to source): the persistence package is `Repository` with a **capital R** (non-standard), and `constants/VehicleServiveConstants` is misspelled. These are the real identifiers.

### 5) Evidence

- src/main/java/com/vehicle/registry_service/ (directory tree)
- src/main/java/com/vehicle/registry_service/RegistryServiceApplication.java
- docs/CODEBASE_MAP.md (Directory Structure section)
