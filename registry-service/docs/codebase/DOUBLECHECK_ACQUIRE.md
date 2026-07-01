# Doublecheck — docs/codebase/ (acquire-codebase-knowledge: 7 docs)
Validated against: c:\Workspace\demo\registry-service (Java source + pom.xml + application.properties = ground truth)
Date: 2026-06-23
Mode: Codebase Artifact Mode · Validation depth: FULL (repo-size SMALL)
Method: graphify/map used as index; every claim asserted against real source file:line. [TODO]/[ASK USER] markers treated as acceptable gaps, not fabrications.

## Summary
- Claims checked: 30
- VERIFIED: 30   FABRICATION RISK: 0   MISLOCATED: 0   MISSING: 0   UNVERIFIED: 0
- Acceptable open markers (not defects): 4 [ASK USER] + a few [TODO] (auth URL/prod DB/coverage threshold/formatter)

## Findings (lead with the worst)
| ID | Doc | Claim | Rating | Evidence (file:line) |
|----|-----|-------|--------|----------------------|
| C1 | STACK | Java 21 | VERIFIED | pom.xml:31 |
| C2 | STACK | Spring Boot 3.5.14 | VERIFIED | pom.xml:9 |
| C3 | STACK | springdoc-openapi 2.8.16 | VERIFIED | pom.xml:43-45 |
| C4 | STACK | jacoco 0.8.11 | VERIFIED | pom.xml:140-142 |
| C5 | STACK | sonar-maven-plugin 3.8.0.2131 | VERIFIED | pom.xml:167-169 |
| C6 | STACK | deps: web, data-jpa, security, webflux, h2, lombok, devtools | VERIFIED | pom.xml:33-84 |
| C7 | STACK | H2 in-memory vehicle-db; console /h2-console; default port 8080 (not set) | VERIFIED | application.properties:5-6,14; no server.port |
| C8 | STRUCTURE | Base package com.vehicle.registry_service; entry RegistryServiceApplication.main | VERIFIED | RegistryServiceApplication.java |
| C9 | STRUCTURE | Packages: controller/service/Repository(+spec)/entity/dto/exception/filters/configuration/client/constants | VERIFIED | src/main/java/** tree |
| C10 | STRUCTURE | Repository package capitalized; VehicleServiveConstants misspelled | VERIFIED | Repository/, constants/VehicleServiveConstants.java:3 |
| C11 | ARCH | Layered Controller→Service→Repository→H2 + filter/clients cross-cutting | VERIFIED | controller/service/Repository/filters/client |
| C12 | ARCH | Repository+Specification pattern | VERIFIED | Repository/spec/VehicleSpecification.java:16 |
| C13 | ARCH | Centralized @RestControllerAdvice | VERIFIED | exception/VehicleRegistrationGlobalExceptionHandler.java:16 |
| C14 | ARCH | No @Transactional in service | VERIFIED | service/VehicleRegistrationService.java (none) |
| C15 | CONV | Lombok @Slf4j logging w/ parameterized messages | VERIFIED | service:21,32 etc |
| C16 | CONV | Domain exceptions via orElseThrow; IllegalArgumentException for bad sort | VERIFIED | service:52-54; controller:198,202 |
| C17 | CONV | Naming defects: updateVechicleDetails, webClinet, authenticatioin | VERIFIED | service:60; WebClientConfig webClinet(); JwtAuthFilter:67 |
| C18 | INTEG | External auth GET http://localhost:8088/v1/public/auth/validate (hardcoded) | VERIFIED | client/SecurityClient.java:20 |
| C19 | INTEG | 5s Mono timeout; no HTTP-level timeout on WebClient bean | VERIFIED | client/ExternalApiClient.java:35; configuration/WebClientConfig.java |
| C20 | INTEG | Failure mapping: 4xx/5xx→ClientErrorException, network→ServiceDown, timeout→TimeOut, else→InternalAuth | VERIFIED | client/SecurityClient.java:36-50 |
| C21 | INTEG | No retry/circuit breaker; no metrics/tracing deps | VERIFIED | client/**; pom.xml (no micrometer/otel) |
| C22 | INTEG | H2 creds sa/empty | VERIFIED | application.properties:16-17 |
| C23 | TEST | JUnit 5 + Mockito + AssertJ via starter-test; spring-security-test | VERIFIED | pom.xml:64-78; ServiceTest imports |
| C24 | TEST | ControllerTest @WebMvcTest + @AutoConfigureMockMvc(addFilters=false) + @MockBean + @WithMockUser | VERIFIED | controller/VehicleRegistrationControllerTest.java:33-50 |
| C25 | TEST | ServiceTest @ExtendWith(MockitoExtension) + @Mock/@InjectMocks | VERIFIED | service/VehicleRegistrationServiceTest.java:31-38 |
| C26 | TEST | RepositoryTest @DataJpaTest | VERIFIED | Repository/VehicleRegistrationRepositoryTest.java |
| C27 | TEST | RegistryServiceApplicationTests @SpringBootTest | VERIFIED | RegistryServiceApplicationTests.java |
| C28 | TEST | 6 test classes; no E2E | VERIFIED | src/test/** (6 files) |
| C29 | TEST/CONCERNS | JaCoCo excludes SecurityClient.class | VERIFIED | pom.xml:144-148 |
| C30 | CONCERNS | SecurityClient logs validation response at INFO; .well-known uses contains(); anyRequest permitAll; update ecuVersion lacks @Pattern; model LIKE case-insensitive vs ecuVersion case-sensitive; dev TODO comments | VERIFIED | SecurityClient.java:33; JwtAuthFilter.java:41; SecurityConfig.java:33; VehicleUpdateRequest.java:16-18; VehicleSpecification.java:25-32; service:34,40 |

## Notes
- All version numbers match pom.xml exactly — no fabricated/drifted versions.
- Test-framework claims confirmed against actual annotations in the test sources.
- [ASK USER]/[TODO] markers (prod auth URL, prod DB, coverage threshold, formatter) are
  legitimate intent-dependent gaps and are correctly flagged rather than guessed.
- Docs correctly separate test-only gaps (no E2E, no coverage gate) from production debt.

## Items needing human review
- None at the verification level. The 4 [ASK USER] questions in CONCERNS are for the team to answer (intent), not accuracy defects.

RESULT: PASS
