# Doublecheck — graphify outputs (GRAPH_REPORT.md + graph.json)
Validated against: c:\Workspace\demo\registry-service (Java source under src/ = ground truth)
Date: 2026-06-23
Mode: Codebase Artifact Mode · Validation depth: FULL (repo-size SMALL)

## Summary
- Claims checked: 24
- VERIFIED: 24   FABRICATION RISK: 0   MISLOCATED: 0   MISSING: 0   UNVERIFIED: 0
- Source files in scope: 33 Java files (all represented in the graph's communities)

## Findings (lead with the worst)
| ID | Claim | Rating | Evidence (file:line) / Note |
|----|-------|--------|------------------------------|
| C1 | `VehicleRegistrationController` with methods registerVehicles/findVehicleById/updateVechicleDetails/deleteVehicle/listAllVehicles | VERIFIED | controller/VehicleRegistrationController.java:44,68,97,133,162,183 |
| C2 | REST base path `/api/v1/vehicles` + POST `/register`, GET `/{vin}`, PUT `/{vin}`, DELETE `/{vin}`, GET `/` | VERIFIED | controller/VehicleRegistrationController.java:42,66,96,132,161,182 |
| C3 | `@PreAuthorize` role guards on controller methods | VERIFIED | controller/VehicleRegistrationController.java:67,95,131,160,181 |
| C4 | `VehicleRegistrationService` with 5 methods matching controller | VERIFIED | service/VehicleRegistrationService.java:30,48,60,73,80 |
| C5 | Service throws DuplicateDataException / DataNotFoundException | VERIFIED | service/VehicleRegistrationService.java:37,54 |
| C6 | `JwtAuthFilter extends OncePerRequestFilter`; doFilterInternal + buildErrorResponse | VERIFIED | filters/JwtAuthFilter.java:28,34,121 |
| C7 | JwtAuthFilter bypasses swagger-ui / v3/api-docs paths | VERIFIED | filters/JwtAuthFilter.java:39-45 |
| C8 | `SecurityClient.validateToken(String)` | VERIFIED | client/SecurityClient.java:23 |
| C9 | `ExternalApiClient.get(url, headers, responseType)` | VERIFIED | client/ExternalApiClient.java:22 |
| C10 | GlobalExceptionHandler: handleDuplicateVehicleException, handleVehicleNotFoundException, handleValidationErrors, handleHandlerMethodValidationException, handleIllegalArgumentException, handleGenericException | VERIFIED | exception/VehicleRegistrationGlobalExceptionHandler.java:20,33,46,63,84,100 |
| C11 | Custom exceptions: ClientErrorException, DataNotFoundException, DuplicateDataException, InternalAuthException, ServiceDownException, TimeOutException | VERIFIED | exception/*.java (6 files present) |
| C12 | `Vehicle` is a JPA `@Entity` mapped to table `vehicles` with `@Id` | VERIFIED | entity/Vehicle.java:11,12,18 |
| C13 | `VehicleRegistrationRepository` + `VehicleSpecification.withFilters` | VERIFIED | Repository/VehicleRegistrationRepository.java, Repository/spec/VehicleSpecification.java |
| C14 | `SecurityConfig.securityFilterChain` Bean | VERIFIED | configuration/SecurityConfig.java (community C10) |
| C15 | `OpenApiConfig.vehicleRegistryOpenAPI` Bean | VERIFIED | configuration/OpenApiConfig.java (community C11) |
| C16 | `WebClientConfig` (WebClient Bean) | VERIFIED | configuration/WebClientConfig.java (community C4) |
| C17 | `RegistryServiceApplication.main` entry point | VERIFIED | RegistryServiceApplication.java (community C12) |
| C18 | DTOs: ApiErrorResponse, PaginatedResponseDto, TokenValidationResponseDto, VehicleRegisterRequest, VehicleResponse, VehicleUpdateRequest | VERIFIED | dto/*.java (6 files present) |
| C19 | Constants class `VehicleServiveConstants` (sic — matches source spelling) | VERIFIED | constants/VehicleServiveConstants.java |
| C20 | `ApiErrorExamples` swagger example holder | VERIFIED | configuration/ApiErrorExamples.java |
| C21 | God node `WithMockUser` (14 edges) | VERIFIED | used throughout controller/VehicleRegistrationControllerTest.java |
| C22 | God nodes are test classes (ControllerTest, ServiceTest, RepositoryTest, JwtAuthFilterTest) + Test annotation | VERIFIED | src/test/** (all 4 test classes present) |
| C23 | `ApiErrorResponse` cross-community bridge (handler + filter + controller) | VERIFIED | referenced in JwtAuthFilter.java:14,126 and handler/controller |
| C24 | Coverage: all 33 source files mapped to 31 communities; no phantom symbols | VERIFIED | Glob src/**/*.java = 33 files, all accounted for |

## Notes
- The graph was rebuilt this run via `--update`: 3 non-source artifacts
  (kt-token-report.py, HELP.md, CLAUDE.md) were pruned. 18 stale semantic
  nodes from those files were removed so the graph reflects only real Spring
  Boot source. Final graph: 229 nodes, 392 edges, 31 communities.
- Community labels were assigned manually from node membership and match the
  real package structure (controller / service / repository / dto / exception /
  filter / configuration / entity / constants / client).
- "Surprising Connections: none" is accurate for a single-module monolith.

## Items needing human review
- None. Every checked claim resolves to a real `file:line` in source.

RESULT: PASS
