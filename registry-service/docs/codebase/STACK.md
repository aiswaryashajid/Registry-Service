# Technology Stack

## Core Sections (Required)

### 1) Runtime Summary

| Area | Value | Evidence |
|------|-------|----------|
| Primary language | Java | pom.xml, src/main/java/** |
| Runtime + version | Java 21 | pom.xml:31 (`<java.version>21</java.version>`) |
| Package manager | Maven (spring-boot-starter-parent) | pom.xml:6-11 |
| Module/build system | Maven; Spring Boot 3.5.14 | pom.xml:9 |

### 2) Production Frameworks and Dependencies

| Dependency | Version | Role in system | Evidence |
|------------|---------|----------------|----------|
| spring-boot-starter-web | managed (3.5.14) | Spring MVC REST layer | pom.xml:38-41 |
| spring-boot-starter-data-jpa | managed | JPA/Hibernate persistence | pom.xml:34-37 |
| spring-boot-starter-security | managed | Authn/authz, method security | pom.xml:64-72 |
| spring-boot-starter-webflux | managed | WebClient (used as a blocking HTTP client) | pom.xml:81-84 |
| springdoc-openapi-starter-webmvc-ui | 2.8.16 | Swagger UI / OpenAPI 3 docs | pom.xml:42-46 |
| h2 | managed (runtime) | In-memory database | pom.xml:54-58 |
| lombok | managed (optional) | Boilerplate (@Data, @Slf4j, @RequiredArgsConstructor, @Builder) | pom.xml:59-63 |
| spring-boot-devtools | managed (runtime) | Dev hot reload | pom.xml:48-53 |

### 3) Development Toolchain

| Tool | Purpose | Evidence |
|------|---------|----------|
| spring-boot-maven-plugin | Build / repackage (lombok excluded) | pom.xml:91-102 |
| maven-compiler-plugin | Compile + lombok annotation processor | pom.xml:103-138 |
| jacoco-maven-plugin 0.8.11 | Test coverage (excludes SecurityClient.class) | pom.xml:139-164 |
| sonar-maven-plugin 3.8.0.2131 | Static analysis / SonarQube | pom.xml:166-170 |
| spring-boot-starter-test | JUnit 5, Mockito, AssertJ | pom.xml:64-68 |
| spring-security-test | @WithMockUser security testing | pom.xml:74-78 |

### 4) Key Commands

```bash
mvn clean install        # build + run tests
mvn spring-boot:run      # run the application
mvn test                 # run tests
mvn jacoco:report        # coverage report (bound to test phase)
mvn sonar:sonar          # SonarQube analysis
```

### 5) Environment and Config

- Config sources: src/main/resources/application.properties
- Required env vars: none committed (no .env, no `@Value` bindings). The external auth URL is hardcoded — see CONCERNS.
- Deployment/runtime constraints: in-memory H2 (data not persisted across restarts); default server port 8080 (not overridden); H2 console at `/h2-console`. External auth service expected at `http://localhost:8088`. `[ASK USER]` what is the production auth endpoint and DB?

### 6) Evidence

- pom.xml
- src/main/resources/application.properties
- src/main/java/com/vehicle/registry_service/client/SecurityClient.java:20
