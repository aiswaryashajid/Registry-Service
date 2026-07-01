# Doublecheck — docs/CODEBASE_MAP.md (cartographer output)
Validated against: c:\Workspace\demo\registry-service (Java source under src/ = ground truth)
Date: 2026-06-23
Mode: Codebase Artifact Mode · Validation depth: FULL (repo-size SMALL)
Method: graphify used as index only; every claim asserted against real source file:line.

## Summary
- Claims checked: 26
- VERIFIED: 26   FABRICATION RISK: 0   MISLOCATED: 0   MISSING: 0   UNVERIFIED: 0

## Findings (lead with the worst)
| ID | Claim | Rating | Evidence (file:line) |
|----|-------|--------|----------------------|
| C1 | Project: Spring Boot 3.5.14, Java 21, artifact registry-service | VERIFIED | pom.xml:9,13,31 |
| C2 | Dependencies: data-jpa, web, security, webflux, springdoc 2.8.16, h2, lombok, devtools | VERIFIED | pom.xml:33-84 |
| C3 | Build: jacoco 0.8.11 excludes SecurityClient.class; sonar plugin | VERIFIED | pom.xml:139-170 |
| C4 | DB: H2 in-memory jdbc:h2:mem:vehicle-db, console /h2-console | VERIFIED | application.properties:5,14 |
| C5 | Base path /api/v1/vehicles | VERIFIED | controller/VehicleRegistrationController.java:42 |
| C6 | POST /register → registerVehicles, hasRole('ADMIN'), 201 | VERIFIED | controller:66,67,68,79 |
| C7 | GET /{vin} → findVehicleById, hasAnyRole('ADMIN','SERVICE','VEHICLE') | VERIFIED | controller:95,96,97 |
| C8 | PUT /{vin} → updateVechicleDetails, hasRole('ADMIN') | VERIFIED | controller:131,132,133 |
| C9 | DELETE /{vin} → deleteVehicle, hasRole('ADMIN'), 204 | VERIFIED | controller:160,161,162,173 |
| C10 | GET / → listAllVehicles, hasAnyRole('ADMIN','SERVICE'), size @Min(1) | VERIFIED | controller:181,182,183,186 |
| C11 | Controller sort validation: allowedSortFields {id,createdAt,vin}, dirs {ASC,DESC} → IllegalArgumentException | VERIFIED | controller:192-203 |
| C12 | Service methods: registerVehicles, findVehicleById, updateVechicleDetails, deleteVehicle, listAllVehicles | VERIFIED | service/VehicleRegistrationService.java:30,48,60,73,80 |
| C13 | Service: existsById→DuplicateDataException; findById.orElseThrow→DataNotFoundException | VERIFIED | service:35-37,52-54 |
| C14 | Service has NO @Transactional | VERIFIED | service/VehicleRegistrationService.java (none present) |
| C15 | Repository extends JpaRepository<Vehicle,String> + JpaSpecificationExecutor | VERIFIED | Repository/VehicleRegistrationRepository.java:7-8 |
| C16 | VehicleSpecification.withFilters: vin equal (exact), model lower LIKE (case-insensitive), ecuVersion LIKE (case-sensitive), AND | VERIFIED | Repository/spec/VehicleSpecification.java:21-34 |
| C17 | Vehicle @Entity @Table("vehicles") @Id vin; fields vin/model/ecuVersion/createdAt/updatedAt; no @Column(unique)/@Version | VERIFIED | entity/Vehicle.java:11-23 |
| C18 | VehicleRegisterRequest: vin @NotBlank+@Pattern(^VIN\d{5}$), model @NotBlank, ecuVersion @NotBlank+@Pattern(^v\d+\.\d+\.\d+$) | VERIFIED | dto/VehicleRegisterRequest.java:16-31 |
| C19 | VehicleUpdateRequest: model @NotBlank, ecuVersion @NotBlank (NO @Pattern) | VERIFIED | dto/VehicleUpdateRequest.java:13-18 |
| C20 | SecurityConfig: csrf disabled, swagger permitAll, /api/v1/** authenticated, anyRequest permitAll, addFilterBefore JwtAuthFilter, @EnableMethodSecurity; filter built with `new` | VERIFIED | configuration/SecurityConfig.java:18,27,29-34 |
| C21 | SecurityClient: hardcoded URL localhost:8088/v1/public/auth/validate; maps WebClientResponseException→ClientErrorException, WebClientRequestException→ServiceDownException, TimeOutException→"Security service Timeout", Exception→InternalAuthException | VERIFIED | client/SecurityClient.java:20,36-50 |
| C22 | ExternalApiClient: 5s Mono timeout, throws TimeOutException | VERIFIED | client/ExternalApiClient.java:35,61 |
| C23 | JwtAuthFilter auth mapping: missing header→401, ClientError→401, ServiceDown→503, TimeOut→504, catch-all→500; bypass swagger/.well-known | VERIFIED | filters/JwtAuthFilter.java:39-45,52,78,88,97,106 |
| C24 | Global handler 6 methods → 409/404/400/400/400/500 | VERIFIED | exception/VehicleRegistrationGlobalExceptionHandler.java:20,33,46,63,84,100 |
| C25 | Client exceptions have no dedicated @ExceptionHandler (fall to generic 500 in MVC) | VERIFIED | only 6 handlers exist; ClientError/ServiceDown/TimeOut/InternalAuth not among them |
| C26 | Gotcha typos faithful to source: `Repository` package, `VehicleServiveConstants`, `updateVechicleDetails`, `webClinet`, `authenticatioin`; SORT_MODEL unused in allowedSortFields | VERIFIED | Repository/*, constants/VehicleServiveConstants.java:3, service:60, WebClientConfig webClinet, JwtAuthFilter:67, controller:192 |

## Notes
- The map correctly captures non-obvious business logic the graph alone does not
  carry: the case-sensitivity asymmetry in VehicleSpecification, the update-vs-register
  validation gap, the auth failure status codes written in the filter vs. the generic
  500 fall-through in the MVC advice, and the SORT_MODEL-but-not-sortable inconsistency.
- All naming typos are reproduced verbatim (not "corrected"), which is the correct
  behavior for a KT artifact — they are the real identifiers a new dev must use.
- Coverage: all 27 main-source classes + bootstrap are represented; no controller,
  service, entity, or endpoint is missing.

## Items needing human review
- None. Every checked claim resolves to a real file:line in source.

RESULT: PASS
