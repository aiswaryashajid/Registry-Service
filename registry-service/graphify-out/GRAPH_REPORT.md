# Graph Report - .  (2026-06-23)

## Corpus Check
- 33 files · ~0 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 229 nodes · 392 edges · 31 communities (11 shown, 20 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 37 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Custom Exceptions|Custom Exceptions]]
- [[_COMMUNITY_JWT Auth Filter Tests|JWT Auth Filter Tests]]
- [[_COMMUNITY_Controller Tests|Controller Tests]]
- [[_COMMUNITY_Vehicle REST Controller|Vehicle REST Controller]]
- [[_COMMUNITY_External API Client|External API Client]]
- [[_COMMUNITY_Repository & Specification|Repository & Specification]]
- [[_COMMUNITY_Global Exception Handler|Global Exception Handler]]
- [[_COMMUNITY_Service Layer Tests|Service Layer Tests]]
- [[_COMMUNITY_Vehicle Service Layer|Vehicle Service Layer]]
- [[_COMMUNITY_JWT Auth Filter|JWT Auth Filter]]
- [[_COMMUNITY_Security Config|Security Config]]
- [[_COMMUNITY_OpenAPI Config|OpenAPI Config]]
- [[_COMMUNITY_Application Entry Point|Application Entry Point]]
- [[_COMMUNITY_Application Context Test|Application Context Test]]
- [[_COMMUNITY_API Error Examples|API Error Examples]]
- [[_COMMUNITY_Service Constants|Service Constants]]
- [[_COMMUNITY_ApiErrorResponse DTO|ApiErrorResponse DTO]]
- [[_COMMUNITY_Paginated Response DTO|Paginated Response DTO]]
- [[_COMMUNITY_Token Validation DTO|Token Validation DTO]]
- [[_COMMUNITY_Vehicle Register Request DTO|Vehicle Register Request DTO]]
- [[_COMMUNITY_Vehicle Response DTO|Vehicle Response DTO]]
- [[_COMMUNITY_Vehicle Update Request DTO|Vehicle Update Request DTO]]
- [[_COMMUNITY_Vehicle Entity|Vehicle Entity]]
- [[_COMMUNITY_Maven Build|Maven Build]]
- [[_COMMUNITY_OCI  Boot Plugin|OCI / Boot Plugin]]
- [[_COMMUNITY_Spring MVC Stack|Spring MVC Stack]]
- [[_COMMUNITY_Spring Web  OpenAPI|Spring Web / OpenAPI]]
- [[_COMMUNITY_Vehicle Repository|Vehicle Repository]]
- [[_COMMUNITY_Package Naming|Package Naming]]
- [[_COMMUNITY_Spring Boot DevTools|Spring Boot DevTools]]
- [[_COMMUNITY_Spring Data JPA|Spring Data JPA]]

## God Nodes (most connected - your core abstractions)
1. `VehicleRegistrationControllerTest` - 14 edges
2. `Test` - 14 edges
3. `WithMockUser` - 14 edges
4. `VehicleRegistrationServiceTest` - 12 edges
5. `Test` - 12 edges
6. `JwtAuthFilterTest` - 10 edges
7. `ApiErrorResponse` - 9 edges
8. `Test` - 9 edges
9. `VehicleRegistrationRepositoryTest` - 8 edges
10. `Test` - 8 edges

## Surprising Connections (you probably didn't know these)
- None detected - all connections are within the same source files.

## Import Cycles
- None detected.

## Communities (31 total, 20 thin omitted)

### Community 0 - "Custom Exceptions"
Cohesion: 0.08
Nodes (13): ClientErrorException, DataNotFoundException, DuplicateDataException, InternalAuthException, ServiceDownException, TimeOutException, RuntimeException, String (+5 more)

### Community 1 - "JWT Auth Filter Tests"
Cohesion: 0.16
Nodes (9): SecurityClient, DummyController, JwtAuthFilterTest, String, BeforeEach, GetMapping, String, Test (+1 more)

### Community 2 - "Controller Tests"
Cohesion: 0.25
Nodes (6): Class, VehicleRegistrationControllerTest, String, Test, T, WithMockUser

### Community 3 - "Vehicle REST Controller"
Cohesion: 0.26
Nodes (15): VehicleRegistrationController, DeleteMapping, Operation, PaginatedResponseDto, PostMapping, PreAuthorize, PutMapping, GetMapping (+7 more)

### Community 4 - "External API Client"
Cohesion: 0.17
Nodes (8): ExternalApiClient, ExternalApiClientTest, WebClientConfig, HttpHeaders, Bean, BeforeEach, Test, WebClient

### Community 5 - "Repository & Specification"
Cohesion: 0.18
Nodes (7): Page, VehicleRegistrationRepositoryTest, VehicleSpecification, Specification, String, Vehicle, Test

### Community 6 - "Global Exception Handler"
Cohesion: 0.29
Nodes (9): ApiErrorResponse, DataNotFoundException, Exception, VehicleRegistrationGlobalExceptionHandler, ExceptionHandler, HandlerMethodValidationException, IllegalArgumentException, MethodArgumentNotValidException (+1 more)

### Community 8 - "Vehicle Service Layer"
Cohesion: 0.33
Nodes (6): DuplicateDataException, VehicleRegistrationService, String, Vehicle, VehicleRegisterRequest, VehicleUpdateRequest

### Community 9 - "JWT Auth Filter"
Cohesion: 0.31
Nodes (7): FilterChain, JwtAuthFilter, HttpServletResponse, OncePerRequestFilter, Override, HttpServletRequest, String

### Community 10 - "Security Config"
Cohesion: 0.53
Nodes (4): SecurityConfig, HttpSecurity, SecurityFilterChain, Bean

### Community 11 - "OpenAPI Config"
Cohesion: 0.60
Nodes (3): OpenApiConfig, OpenAPI, Bean

## Knowledge Gaps
- **35 isolated node(s):** `String`, `VehicleRegistrationRepository`, `String`, `String`, `Class` (+30 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **20 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Page` connect `Repository & Specification` to `Vehicle Service Layer`, `Controller Tests`, `Vehicle REST Controller`, `Service Layer Tests`?**
  _High betweenness centrality (0.092) - this node is a cross-community bridge._
- **Why does `ApiErrorResponse` connect `Global Exception Handler` to `JWT Auth Filter`, `Vehicle REST Controller`?**
  _High betweenness centrality (0.082) - this node is a cross-community bridge._
- **Why does `TokenValidationResponseDto` connect `JWT Auth Filter Tests` to `JWT Auth Filter`?**
  _High betweenness centrality (0.051) - this node is a cross-community bridge._
- **What connects `String`, `VehicleRegistrationRepository`, `String` to the rest of the system?**
  _37 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Custom Exceptions` be split into smaller, more focused modules?**
  _Cohesion score 0.08 - nodes in this community are weakly interconnected._