# Testing Patterns

## Core Sections (Required)

### 1) Test Stack and Commands

- Primary test framework: JUnit 5 (Jupiter), via spring-boot-starter-test
- Assertion/mocking tools: Mockito (`@Mock`, `@InjectMocks`, `@MockBean`), AssertJ (`assertThat`, `assertThatThrownBy`), Spring `MockMvc`, spring-security-test (`@WithMockUser`)
- Commands:

```bash
mvn test                 # run all tests
mvn -Dtest=ClassName test  # run a single test class
mvn jacoco:report        # coverage (target/site/jacoco)
```

### 2) Test Layout

- Test file placement: mirrored package under `src/test/java/com/vehicle/registry_service/`
- Naming convention: `<ClassName>Test`; methods are scenario-named (e.g. `registerVehicle_duplicateVin_returns409`, `getVehicle_notFound_returns404`)
- Setup files: per-class `@BeforeEach` setup where needed (e.g. ExternalApiClientTest, JwtAuthFilterTest); no shared test resources directory.

### 3) Test Scope Matrix

| Scope | Covered? | Typical target | Notes |
|-------|----------|----------------|-------|
| Unit | yes | Service, Filter, Client | ServiceTest (`@ExtendWith(MockitoExtension)`), JwtAuthFilterTest, ExternalApiClientTest |
| Web slice | yes | Controller | `@WebMvcTest(VehicleRegistrationController.class)` + `@AutoConfigureMockMvc(addFilters=false)` + `@MockBean` service |
| Data/JPA slice | yes | Repository + Specification | `@DataJpaTest` (VehicleRegistrationRepositoryTest) against H2 |
| Context load | yes | Application bootstrap | `@SpringBootTest` (RegistryServiceApplicationTests.contextLoads) |
| Full E2E | no | — | no end-to-end / external-service integration tests |

Test classes (6): VehicleRegistrationControllerTest, VehicleRegistrationServiceTest, VehicleRegistrationRepositoryTest, JwtAuthFilterTest, ExternalApiClientTest, RegistryServiceApplicationTests.

### 4) Mocking and Isolation Strategy

- Main mocking approach:
  - Service tests mock the repository (`@Mock` + `@InjectMocks`).
  - Controller tests mock the service (`@MockBean`) and disable security filters (`addFilters=false`), using `@WithMockUser(roles="ADMIN")` for role context.
  - Client tests mock WebClient interactions to simulate success/timeout/4xx/service-down.
- Isolation guarantees: Mockito resets per test method; `@DataJpaTest` rolls back each test transaction; H2 is in-memory.
- Common failure mode: controller tests bypass the real `JwtAuthFilter` (addFilters=false), so auth-filter behavior is only covered by JwtAuthFilterTest, not end-to-end.

### 5) Coverage and Quality Signals

- Coverage tool + threshold: JaCoCo 0.8.11; no enforced threshold gate. `SecurityClient.class` is **excluded** from coverage (pom.xml:144-148).
- Current reported coverage: `[TODO]` run `mvn jacoco:report` to obtain.
- Known gaps/flaky areas: SecurityClient excluded from coverage; no E2E; auth filter not exercised through the controller slice.

### 6) Evidence

- src/test/java/com/vehicle/registry_service/controller/VehicleRegistrationControllerTest.java:33-50
- src/test/java/com/vehicle/registry_service/service/VehicleRegistrationServiceTest.java:31-38
- src/test/java/com/vehicle/registry_service/Repository/VehicleRegistrationRepositoryTest.java (@DataJpaTest)
- pom.xml:139-164 (JaCoCo)
