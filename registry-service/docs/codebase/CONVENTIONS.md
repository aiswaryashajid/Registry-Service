# Coding Conventions

## Core Sections (Required)

### 1) Naming Rules

| Item | Rule | Example | Evidence |
|------|------|---------|----------|
| Files/classes | PascalCase | `VehicleRegistrationService` | service/VehicleRegistrationService.java |
| Methods | camelCase verbs | `registerVehicles`, `findVehicleById` | controller/VehicleRegistrationController.java |
| Types/DTOs | PascalCase, suffixed by role | `VehicleRegisterRequest`, `VehicleResponse`, `PaginatedResponseDto` | dto/** |
| Constants | UPPER_SNAKE_CASE static final | `SORT_CREATED_AT`, `AUTH_ERROR_KEY` | constants/VehicleServiveConstants.java:8,18 |

### 2) Formatting and Linting

- Formatter: none committed (no .editorconfig / spotless config found). `[TODO]` confirm team formatter.
- Linter / static analysis: SonarQube via sonar-maven-plugin (pom.xml:166-170); JaCoCo for coverage (pom.xml:139-164).
- Most relevant enforced rules: coverage report generated in `test` phase; SonarQube ruleset external.
- Run commands: `mvn sonar:sonar`, `mvn jacoco:report`

### 3) Import and Module Conventions

- Import grouping/order: standard Java (java.*, then org.*/com.*); no wildcard imports observed.
- Alias vs relative import policy: N/A (Java packages).
- Public exports/barrel policy: N/A.

### 4) Error and Logging Conventions

- Error strategy by layer:
  - Service throws domain exceptions (`DuplicateDataException` → 409, `DataNotFoundException` → 404) via `orElseThrow`.
  - Controller throws `IllegalArgumentException` for invalid sort field/direction → 400.
  - Clients translate WebClient errors into custom exceptions (`ClientErrorException`, `ServiceDownException`, `TimeOutException`, `InternalAuthException`).
  - All controller-layer exceptions are caught by `@RestControllerAdvice` (`VehicleRegistrationGlobalExceptionHandler`) and rendered as `ApiErrorResponse`.
- Logging style: Lombok `@Slf4j`; `log.debug/info/warn/error` with parameterized messages; `log.error(msg, ex)` for stack traces in the generic handler.
- Sensitive-data redaction: none explicit. Note: `SecurityClient` logs the token-validation response object at INFO (`:::Token Validation Response::: {}`) — see CONCERNS.

### 5) Testing Conventions

- Test file naming/location: `<ClassName>Test` mirrored under `src/test/java/.../<same package>`.
- Mocking strategy norm: Mockito (`@Mock`/`@InjectMocks` + `MockitoExtension`) for unit tests; `@MockBean` for `@WebMvcTest`; `@WithMockUser` for security; AssertJ for assertions.
- Coverage expectation: JaCoCo report generated; no explicit threshold gate configured. `[TODO]` confirm target.

### 6) Evidence

- pom.xml (jacoco/sonar plugins)
- src/main/java/com/vehicle/registry_service/service/VehicleRegistrationService.java (logging + exceptions)
- src/test/java/com/vehicle/registry_service/** (test conventions)

## Extended Sections (Optional)

### Known convention violations to clean up (faithful to source — do NOT silently rename)
- Package `Repository` capitalized (should be lowercase `repository`).
- Class `VehicleServiveConstants` misspelled (Servive → Service).
- Method `updateVechicleDetails` misspelled (Vechicle → Vehicle).
- Bean method `webClinet()` misspelled (WebClientConfig).
- Local variable `authenticatioin` misspelled (JwtAuthFilter).
