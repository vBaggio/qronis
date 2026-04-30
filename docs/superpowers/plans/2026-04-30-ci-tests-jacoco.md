# CI/Tests/JaCoCo Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Elevar cobertura de testes para ≥90%, criar source set `integrationTest` com Testcontainers, e configurar GitHub Actions CI que valida todo PR aberto para `main`.

**Architecture:** Source set `integrationTest` separado em `src/integrationTest/java` com Testcontainers gerenciando PostgreSQL internamente. Unit tests em `src/test` com Mockito. JaCoCo agrega cobertura do task `test`, checa ≥90% excluindo DTOs, gerados por MapStruct, configs e entry points. CI roda dois jobs em paralelo no GitHub Actions.

**Tech Stack:** Spring Boot 3.5.14 · Java 21 · Gradle Kotlin DSL · JaCoCo · Testcontainers PostgreSQL 16 · JUnit 5 · Mockito · MockMvc · GitHub Actions

**Branch:** `refactor/ci-tests-jacoco`

**Spec:** `docs/superpowers/specs/2026-04-30-ci-tests-design.md`

---

## Progresso

| Fase | Descrição | Status |
|---|---|---|
| Fase 1 | Gradle: JaCoCo + source set integrationTest | ✅ Concluída |
| Fase 2 | Migrar testes de integração existentes | ✅ Concluída |
| Fase 3 | Novos unit tests (IdentityService, JwtService) | ✅ Concluída |
| Fase 4 | Novos controller integration tests | ⏳ Em andamento — AbstractControllerIT + AuthControllerIT pendentes |
| Fase 5 | GitHub Actions workflow | ⬜ Pendente |

> Atualize esta tabela trocando ⬜ por ✅ conforme cada fase for concluída.

---

## Mapa de Arquivos

### Criados
| Arquivo | Responsabilidade |
|---|---|
| `src/integrationTest/java/com/qronis/AbstractIntegrationTest.java` | Base Testcontainers para todos os ITs |
| `src/integrationTest/resources/application-test.yml` | Config Spring para testes de integração |
| `src/integrationTest/java/com/qronis/repository/ProjectRepositoryTest.java` | Testes de repositório de projeto |
| `src/integrationTest/java/com/qronis/repository/TimeEntryRepositoryTest.java` | Testes de repositório de time entry |
| `src/integrationTest/java/com/qronis/controller/AbstractControllerIT.java` | Base MockMvc + JWT helper |
| `src/integrationTest/java/com/qronis/controller/AuthControllerIT.java` | ITs do fluxo de auth |
| `src/integrationTest/java/com/qronis/controller/ProjectControllerIT.java` | ITs do CRUD de projetos |
| `src/integrationTest/java/com/qronis/controller/TimeEntryControllerIT.java` | ITs do tracker |
| `src/integrationTest/java/com/qronis/controller/ProjectSummaryControllerIT.java` | IT do endpoint de sumário |
| `src/integrationTest/java/com/qronis/controller/UserControllerIT.java` | IT do endpoint /me |
| `src/test/java/com/qronis/service/IdentityServiceTest.java` | Unit tests de IdentityService |
| `src/test/java/com/qronis/service/JwtServiceTest.java` | Unit tests de JwtService |
| `.github/workflows/CHECK_PULL_REQUEST.yml` | CI do GitHub Actions |

### Modificados
| Arquivo | O que muda |
|---|---|
| `build.gradle.kts` | Plugin jacoco, source set integrationTest, tasks jacocoTestReport e jacocoCoverageVerification |

### Removidos
| Arquivo | Motivo |
|---|---|
| `src/test/java/com/qronis/AbstractIntegrationTest.java` | Movido para integrationTest |
| `src/test/java/com/qronis/repository/ProjectRepositoryTest.java` | Movido para integrationTest |
| `src/test/java/com/qronis/repository/TimeEntryRepositoryTest.java` | Movido para integrationTest |

---

## Fase 1 — Gradle: JaCoCo + Source Set integrationTest

### Task 1: Configurar build.gradle.kts

**Arquivo:** `build.gradle.kts`

- [ ] **Step 1.1: Adicionar plugin JaCoCo e source set integrationTest**

Substitua o conteúdo completo de `build.gradle.kts` por:

```kotlin
plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.5.14"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.qronis"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

// ─── Source Sets ────────────────────────────────────────────────────────────

sourceSets {
    create("integrationTest") {
        java.srcDir("src/integrationTest/java")
        resources.srcDir("src/integrationTest/resources")
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output
        runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
    }
}

configurations["integrationTestImplementation"]
    .extendsFrom(configurations["testImplementation"])
configurations["integrationTestRuntimeOnly"]
    .extendsFrom(configurations["testRuntimeOnly"])

// ─── Dependency Management ────────────────────────────────────────────────

dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:1.4.1")
    }
}

val mapstructVersion = "1.6.3"

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    implementation("org.springframework.modulith:spring-modulith-core")

    // Database
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")

    // MapStruct & Lombok
    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    // Test (unit)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Test (integration) — Testcontainers
    "integrationTestImplementation"("org.springframework.boot:spring-boot-starter-test")
    "integrationTestImplementation"("org.springframework.security:spring-security-test")
    "integrationTestImplementation"("org.springframework.boot:spring-boot-testcontainers")
    "integrationTestImplementation"(platform("org.testcontainers:testcontainers-bom:1.20.4"))
    "integrationTestImplementation"("org.testcontainers:postgresql")
    "integrationTestImplementation"("org.testcontainers:junit-jupiter")
    "integrationTestRuntimeOnly"("org.junit.platform:junit-platform-launcher")
}

// ─── Compile Options ─────────────────────────────────────────────────────

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf(
        "-parameters",
        "-Amapstruct.defaultComponentModel=spring",
        "-Amapstruct.unmappedTargetPolicy=IGNORE"
    ))
}

// ─── Test Tasks ──────────────────────────────────────────────────────────

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Roda testes de integração com Testcontainers + PostgreSQL"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks.test)
}

// ─── JaCoCo ──────────────────────────────────────────────────────────────

val jacocoExclusions = listOf(
    "**/*DTO*",
    "**/*MapperImpl*",
    "**/*Config*",
    "**/*Properties*",
    "**/QronisApplication*",
    "**/QronisModuleDetectionStrategy*"
)

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = true
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) { exclude(jacocoExclusions) }
        })
    )
}

tasks.jacocoCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) { exclude(jacocoExclusions) }
        })
    )
    violationRules {
        rule {
            limit {
                minimum = "0.90".toBigDecimal()
            }
        }
    }
}
```

- [ ] **Step 1.2: Verificar que o build compila sem erros**

```bash
./gradlew compileJava compileTestJava compileIntegrationTestJava
```

Esperado: `BUILD SUCCESSFUL`

- [ ] **Step 1.3: Commit**

```bash
git add build.gradle.kts
git commit -m "build: adicionar JaCoCo e source set integrationTest"
```

---

## Fase 2 — Migrar Testes de Integração Existentes

### Task 2: Criar estrutura de diretórios e mover AbstractIntegrationTest

- [ ] **Step 2.1: Criar diretórios**

```bash
mkdir -p src/integrationTest/java/com/qronis/repository
mkdir -p src/integrationTest/java/com/qronis/controller
mkdir -p src/integrationTest/resources
```

- [ ] **Step 2.2: Criar `src/integrationTest/java/com/qronis/AbstractIntegrationTest.java`**

```java
package com.qronis;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("qronis_test")
            .withUsername("test")
            .withPassword("test");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

- [ ] **Step 2.3: Criar `src/integrationTest/resources/application-test.yml`**

Copie o conteúdo de `src/test/resources/application-test.yml`:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
    show-sql: true
  flyway:
    enabled: true

jwt:
  secret: qronis-test-secret-com-pelo-menos-32-caracteres
  expiration-hours: 1
  issuer: qronis-test

logging:
  level:
    org.springframework.security: DEBUG
    com.qronis: DEBUG
```

- [ ] **Step 2.4: Criar `src/integrationTest/java/com/qronis/repository/ProjectRepositoryTest.java`**

Copie o conteúdo completo de `src/test/java/com/qronis/repository/ProjectRepositoryTest.java` para o novo caminho. O único ajuste é o import do `AbstractIntegrationTest` — o pacote é o mesmo (`com.qronis`), então nada muda no código.

- [ ] **Step 2.5: Criar `src/integrationTest/java/com/qronis/repository/TimeEntryRepositoryTest.java`**

Copie o conteúdo completo de `src/test/java/com/qronis/repository/TimeEntryRepositoryTest.java` para o novo caminho.

- [ ] **Step 2.6: Remover arquivos antigos de `src/test`**

```bash
rm src/test/java/com/qronis/AbstractIntegrationTest.java
rm src/test/java/com/qronis/repository/ProjectRepositoryTest.java
rm src/test/java/com/qronis/repository/TimeEntryRepositoryTest.java
```

- [ ] **Step 2.7: Verificar que unit tests ainda passam**

```bash
./gradlew test
```

Esperado: `BUILD SUCCESSFUL` — os 3 service tests e o architecture test passando.

- [ ] **Step 2.8: Verificar que integration tests passam**

```bash
./gradlew integrationTest
```

Esperado: `BUILD SUCCESSFUL` — ProjectRepositoryTest e TimeEntryRepositoryTest passando com Testcontainers.

- [ ] **Step 2.9: Commit**

```bash
git add src/integrationTest src/test
git commit -m "test: migrar testes de integração para source set integrationTest"
```

---

## Fase 3 — Novos Unit Tests

### Task 3: IdentityServiceTest

**Arquivo:** `src/test/java/com/qronis/service/IdentityServiceTest.java`

- [ ] **Step 3.1: Criar `IdentityServiceTest.java`**

```java
package com.qronis.service;

import com.qronis.modules.identity.api.dto.IdentityProvisionResult;
import com.qronis.modules.identity.api.dto.TenantUserAuthDTO;
import com.qronis.modules.identity.api.exception.UserAlreadyExistsException;
import com.qronis.modules.identity.application.IdentityService;
import com.qronis.modules.identity.domain.entity.Tenant;
import com.qronis.modules.identity.domain.entity.TenantUser;
import com.qronis.modules.identity.domain.entity.TenantUserId;
import com.qronis.modules.identity.domain.entity.User;
import com.qronis.modules.identity.domain.enums.Role;
import com.qronis.modules.identity.infrastructure.persistence.TenantRepository;
import com.qronis.modules.identity.infrastructure.persistence.TenantUserRepository;
import com.qronis.modules.identity.infrastructure.persistence.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private TenantUserRepository tenantUserRepository;

    @InjectMocks
    private IdentityService identityService;

    private User user;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        user = new User("vini@email.com", "encoded", "Vinicius");
        user.setId(UUID.randomUUID());
        tenant = new Tenant("Qronis Ltda");
        tenant.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("provisionTenant: deve criar user, tenant, tenantUser e retornar resultado")
    void provisionTenant_success() {
        when(userRepository.existsByEmail("vini@email.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);
        when(tenantUserRepository.save(any(TenantUser.class))).thenReturn(
                new TenantUser(tenant, user, Role.OWNER));

        IdentityProvisionResult result = identityService.provisionTenant(
                "Vinicius", "vini@email.com", "encoded", "Qronis Ltda");

        assertThat(result.userId()).isEqualTo(user.getId());
        assertThat(result.email()).isEqualTo("vini@email.com");
        assertThat(result.tenantId()).isEqualTo(tenant.getId());
        assertThat(result.role()).isEqualTo("OWNER");
        verify(tenantUserRepository).save(any(TenantUser.class));
    }

    @Test
    @DisplayName("provisionTenant: deve lançar UserAlreadyExistsException para email duplicado")
    void provisionTenant_duplicateEmail() {
        when(userRepository.existsByEmail("vini@email.com")).thenReturn(true);

        assertThatThrownBy(() ->
                identityService.provisionTenant("Vinicius", "vini@email.com", "encoded", "Empresa"))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @DisplayName("getAuthDetailsByEmail: deve retornar DTO quando encontrado")
    void getAuthDetailsByEmail_found() {
        TenantUserId id = new TenantUserId(tenant.getId(), user.getId());
        TenantUser tu = new TenantUser(tenant, user, Role.OWNER);
        tu.setId(id);
        when(tenantUserRepository.findByUserEmailWithUser("vini@email.com"))
                .thenReturn(Optional.of(tu));

        Optional<TenantUserAuthDTO> result = identityService.getAuthDetailsByEmail("vini@email.com");

        assertThat(result).isPresent();
        assertThat(result.get().email()).isEqualTo("vini@email.com");
        assertThat(result.get().role()).isEqualTo("OWNER");
    }

    @Test
    @DisplayName("getAuthDetailsByEmail: deve retornar vazio quando email inexistente")
    void getAuthDetailsByEmail_notFound() {
        when(tenantUserRepository.findByUserEmailWithUser("nao@existe.com"))
                .thenReturn(Optional.empty());

        Optional<TenantUserAuthDTO> result = identityService.getAuthDetailsByEmail("nao@existe.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail: deve delegar ao repositório")
    void existsByEmail_delegates() {
        when(userRepository.existsByEmail("vini@email.com")).thenReturn(true);

        assertThat(identityService.existsByEmail("vini@email.com")).isTrue();
    }
}
```

- [ ] **Step 3.2: Rodar e verificar que passa**

```bash
./gradlew test --tests "com.qronis.service.IdentityServiceTest"
```

Esperado: 5 testes passando.

- [ ] **Step 3.3: Commit**

```bash
git add src/test/java/com/qronis/service/IdentityServiceTest.java
git commit -m "test: adicionar IdentityServiceTest"
```

---

### Task 4: JwtServiceTest

**Arquivo:** `src/test/java/com/qronis/service/JwtServiceTest.java`

- [ ] **Step 4.1: Criar `JwtServiceTest.java`**

```java
package com.qronis.service;

import com.qronis.modules.auth.application.JwtService;
import com.qronis.modules.auth.config.JwtProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock private JwtEncoder jwtEncoder;
    @Mock private JwtProperties jwtProperties;

    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        when(jwtProperties.getIssuer()).thenReturn("qronis-test");
        when(jwtProperties.getExpirationHours()).thenReturn(1L);
    }

    @Test
    @DisplayName("generateToken: deve chamar encoder e retornar token")
    void generateToken_callsEncoderAndReturnsToken() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("mocked-jwt-token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        String token = jwtService.generateToken(
                UUID.randomUUID(), "Vinicius", "vini@email.com",
                UUID.randomUUID(), "OWNER");

        assertThat(token).isEqualTo("mocked-jwt-token");
        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    @Test
    @DisplayName("generateToken: deve incluir claims corretos nos parâmetros de encode")
    void generateToken_includesCorrectClaims() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenAnswer(invocation -> {
            JwtEncoderParameters params = invocation.getArgument(0);
            assertThat(params.getClaims().getSubject()).isEqualTo(userId.toString());
            assertThat(params.getClaims().getClaim("email")).isEqualTo("vini@email.com");
            assertThat(params.getClaims().getClaim("tenantId")).isEqualTo(tenantId.toString());
            assertThat(params.getClaims().getClaim("role")).isEqualTo("OWNER");
            assertThat(params.getClaims().getIssuer().toString()).isEqualTo("qronis-test");
            return jwt;
        });

        jwtService.generateToken(userId, "Vinicius", "vini@email.com", tenantId, "OWNER");
    }
}
```

- [ ] **Step 4.2: Rodar e verificar que passa**

```bash
./gradlew test --tests "com.qronis.service.JwtServiceTest"
```

Esperado: 2 testes passando.

- [ ] **Step 4.3: Rodar suite completa de unit tests**

```bash
./gradlew test
```

Esperado: todos os 7 arquivos de unit test passando (AuthServiceTest, ProjectServiceTest, TrackerServiceTest, IdentityServiceTest, JwtServiceTest, QronisArchitectureTest + os 2 novos).

- [ ] **Step 4.4: Commit**

```bash
git add src/test/java/com/qronis/service/JwtServiceTest.java
git commit -m "test: adicionar JwtServiceTest"
```

---

## Fase 4 — Controller Integration Tests

### Task 5: AbstractControllerIT

**Arquivo:** `src/integrationTest/java/com/qronis/controller/AbstractControllerIT.java`

Esta classe base injeta MockMvc e provê um helper que registra um usuário e retorna o JWT para ser usado nos testes.

- [ ] **Step 5.1: Criar `AbstractControllerIT.java`**

```java
package com.qronis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qronis.AbstractIntegrationTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public abstract class AbstractControllerIT extends AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String registerAndGetToken(String email, String password) throws Exception {
        String uniqueName = "User-" + UUID.randomUUID().toString().substring(0, 8);
        String companyName = "Company-" + UUID.randomUUID().toString().substring(0, 8);

        String body = objectMapper.writeValueAsString(Map.of(
                "name", uniqueName,
                "email", email,
                "password", password,
                "companyName", companyName
        ));

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        Map<?, ?> response = objectMapper.readValue(
                result.getResponse().getContentAsString(), Map.class);
        return (String) response.get("token");
    }

    protected String bearerHeader(String token) {
        return "Bearer " + token;
    }
}
```

- [ ] **Step 5.2: Commit**

```bash
git add src/integrationTest/java/com/qronis/controller/AbstractControllerIT.java
git commit -m "test(integration): adicionar AbstractControllerIT com helper JWT"
```

---

### Task 6: AuthControllerIT

**Arquivo:** `src/integrationTest/java/com/qronis/controller/AuthControllerIT.java`

- [ ] **Step 6.1: Criar `AuthControllerIT.java`**

```java
package com.qronis.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIT extends AbstractControllerIT {

    @Test
    @DisplayName("POST /auth/register: deve retornar 200 com token JWT")
    void register_success() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Vinicius",
                "email", "vini@test.com",
                "password", "senha123",
                "companyName", "Qronis Ltda"
        ));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/register: deve retornar 409 para email duplicado")
    void register_duplicateEmail() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "Vinicius",
                "email", "duplicado@test.com",
                "password", "senha123",
                "companyName", "Empresa A"
        ));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/register: deve retornar 400 para campos inválidos")
    void register_invalidFields() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "name", "",
                "email", "nao-e-email",
                "password", "123",
                "companyName", ""
        ));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/login: deve retornar 200 com token JWT")
    void login_success() throws Exception {
        String registerBody = objectMapper.writeValueAsString(Map.of(
                "name", "Login User",
                "email", "login@test.com",
                "password", "senha123",
                "companyName", "Empresa Login"
        ));
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk());

        String loginBody = objectMapper.writeValueAsString(Map.of(
                "email", "login@test.com",
                "password", "senha123"
        ));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/login: deve retornar 401 para senha incorreta")
    void login_wrongPassword() throws Exception {
        String registerBody = objectMapper.writeValueAsString(Map.of(
                "name", "Wrong Pass User",
                "email", "wrongpass@test.com",
                "password", "correta123",
                "companyName", "Empresa WP"
        ));
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk());

        String loginBody = objectMapper.writeValueAsString(Map.of(
                "email", "wrongpass@test.com",
                "password", "errada123"
        ));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/login: deve retornar 401 para email inexistente")
    void login_emailNotFound() throws Exception {
        String loginBody = objectMapper.writeValueAsString(Map.of(
                "email", "inexistente@test.com",
                "password", "qualquer123"
        ));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized());
    }
}
```

- [ ] **Step 6.2: Rodar e verificar**

```bash
./gradlew integrationTest --tests "com.qronis.controller.AuthControllerIT"
```

Esperado: 5 testes passando.

- [ ] **Step 6.3: Commit**

```bash
git add src/integrationTest/java/com/qronis/controller/AuthControllerIT.java
git commit -m "test(integration): adicionar AuthControllerIT"
```

---

### Task 7: ProjectControllerIT

**Arquivo:** `src/integrationTest/java/com/qronis/controller/ProjectControllerIT.java`

- [ ] **Step 7.1: Criar `ProjectControllerIT.java`**

```java
package com.qronis.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProjectControllerIT extends AbstractControllerIT {

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        String email = "proj-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        token = registerAndGetToken(email, "senha123");
    }

    @Test
    @DisplayName("GET /api/projects: deve retornar 401 sem token")
    void list_unauthorized() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/projects: deve retornar página vazia para tenant sem projetos")
    void list_emptyPage() throws Exception {
        mockMvc.perform(get("/api/projects")
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("POST /api/projects: deve criar projeto e retornar 201")
    void create_success() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", "Projeto Teste"));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Projeto Teste"));
    }

    @Test
    @DisplayName("POST /api/projects: deve retornar 400 para nome em branco")
    void create_blankName() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", ""));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("DELETE /api/projects/{id}: deve retornar 204")
    void delete_success() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", "Para Deletar"));
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated())
                .andReturn();

        Map<?, ?> created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), Map.class);
        String projectId = (String) created.get("id");

        mockMvc.perform(delete("/api/projects/" + projectId)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/projects/{id}: deve retornar 404 para projeto inexistente")
    void delete_notFound() throws Exception {
        mockMvc.perform(delete("/api/projects/" + UUID.randomUUID())
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("PROJECT_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/projects/{id}: deve retornar 404 para projeto de outro tenant")
    void getById_otherTenantIsolation() throws Exception {
        // Cria projeto com tenant A
        String body = objectMapper.writeValueAsString(Map.of("name", "Projeto Tenant A"));
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated())
                .andReturn();
        Map<?, ?> created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), Map.class);
        String projectId = (String) created.get("id");

        // Tenant B tenta acessar
        String tokenB = registerAndGetToken(
                "tenantb-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com", "senha123");

        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", bearerHeader(tokenB)))
                .andExpect(status().isNotFound());
    }
}
```

- [ ] **Step 7.2: Rodar e verificar**

```bash
./gradlew integrationTest --tests "com.qronis.controller.ProjectControllerIT"
```

Esperado: 6 testes passando.

- [ ] **Step 7.3: Commit**

```bash
git add src/integrationTest/java/com/qronis/controller/ProjectControllerIT.java
git commit -m "test(integration): adicionar ProjectControllerIT"
```

---

### Task 8: TimeEntryControllerIT

**Arquivo:** `src/integrationTest/java/com/qronis/controller/TimeEntryControllerIT.java`

- [ ] **Step 8.1: Criar `TimeEntryControllerIT.java`**

```java
package com.qronis.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TimeEntryControllerIT extends AbstractControllerIT {

    private String token;
    private String projectId;

    @BeforeEach
    void setUp() throws Exception {
        String email = "tracker-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        token = registerAndGetToken(email, "senha123");

        // Cria projeto para usar nos testes
        String body = objectMapper.writeValueAsString(Map.of("name", "Projeto Tracker"));
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andReturn();
        Map<?, ?> created = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        projectId = (String) created.get("id");
    }

    @Test
    @DisplayName("POST /api/time-entries/start: deve iniciar timer e retornar 201")
    void start_success() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "projectId", projectId,
                "description", "Trabalhando em feature"
        ));

        mockMvc.perform(post("/api/time-entries/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.endTime").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/time-entries/start: deve retornar 409 quando timer já está ativo")
    void start_conflictWhenAlreadyActive() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "projectId", projectId,
                "description", "Primeiro timer"
        ));

        mockMvc.perform(post("/api/time-entries/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/time-entries/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ACTIVE_TIMER_CONFLICT"));
    }

    @Test
    @DisplayName("PUT /api/time-entries/stop: deve parar timer ativo e retornar endTime preenchido")
    void stop_success() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "projectId", projectId,
                "description", "Timer para parar"
        ));
        mockMvc.perform(post("/api/time-entries/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/time-entries/stop")
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endTime").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/time-entries: deve criar entry manual com startTime e endTime")
    void create_manualEntry() throws Exception {
        Instant start = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant end = Instant.now().minus(1, ChronoUnit.HOURS);

        String body = objectMapper.writeValueAsString(Map.of(
                "projectId", projectId,
                "description", "Entry manual",
                "startTime", start.toString(),
                "endTime", end.toString()
        ));

        mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.startTime").isNotEmpty())
                .andExpect(jsonPath("$.endTime").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/time-entries: deve retornar 400 para bounds inválidos (start >= end)")
    void create_invalidBounds() throws Exception {
        Instant now = Instant.now();
        String body = objectMapper.writeValueAsString(Map.of(
                "projectId", projectId,
                "description", "Bounds inválidos",
                "startTime", now.toString(),
                "endTime", now.minus(1, ChronoUnit.HOURS).toString()
        ));

        mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/time-entries/{id}: deve atualizar description")
    void patch_description() throws Exception {
        Instant start = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant end = Instant.now().minus(1, ChronoUnit.HOURS);
        String createBody = objectMapper.writeValueAsString(Map.of(
                "projectId", projectId,
                "description", "Original",
                "startTime", start.toString(),
                "endTime", end.toString()
        ));
        MvcResult createResult = mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody)
                        .header("Authorization", bearerHeader(token)))
                .andReturn();
        Map<?, ?> created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), Map.class);
        String entryId = (String) created.get("id");

        String patchBody = objectMapper.writeValueAsString(Map.of("description", "Atualizado"));

        mockMvc.perform(patch("/api/time-entries/" + entryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchBody)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Atualizado"));
    }

    @Test
    @DisplayName("DELETE /api/time-entries/{id}: deve retornar 204")
    void delete_success() throws Exception {
        Instant start = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant end = Instant.now().minus(1, ChronoUnit.HOURS);
        String createBody = objectMapper.writeValueAsString(Map.of(
                "projectId", projectId,
                "description", "Para deletar",
                "startTime", start.toString(),
                "endTime", end.toString()
        ));
        MvcResult createResult = mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody)
                        .header("Authorization", bearerHeader(token)))
                .andReturn();
        Map<?, ?> created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), Map.class);
        String entryId = (String) created.get("id");

        mockMvc.perform(delete("/api/time-entries/" + entryId)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/time-entries/{id}: deve retornar 404 para entry inexistente")
    void delete_notFound() throws Exception {
        mockMvc.perform(delete("/api/time-entries/" + UUID.randomUUID())
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("TIME_ENTRY_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/time-entries: deve retornar histórico paginado")
    void history_paged() throws Exception {
        Instant start = Instant.now().minus(3, ChronoUnit.HOURS);
        Instant end = Instant.now().minus(2, ChronoUnit.HOURS);
        String createBody = objectMapper.writeValueAsString(Map.of(
                "projectId", projectId,
                "description", "Entry histórico",
                "startTime", start.toString(),
                "endTime", end.toString()
        ));
        mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/time-entries")
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }
}
```

- [ ] **Step 8.2: Rodar e verificar**

```bash
./gradlew integrationTest --tests "com.qronis.controller.TimeEntryControllerIT"
```

Esperado: 9 testes passando.

- [ ] **Step 8.3: Commit**

```bash
git add src/integrationTest/java/com/qronis/controller/TimeEntryControllerIT.java
git commit -m "test(integration): adicionar TimeEntryControllerIT"
```

---

### Task 9: ProjectSummaryControllerIT + UserControllerIT

**Arquivos:**
- `src/integrationTest/java/com/qronis/controller/ProjectSummaryControllerIT.java`
- `src/integrationTest/java/com/qronis/controller/UserControllerIT.java`

- [ ] **Step 9.1: Criar `ProjectSummaryControllerIT.java`**

```java
package com.qronis.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProjectSummaryControllerIT extends AbstractControllerIT {

    private String token;
    private String projectId;

    @BeforeEach
    void setUp() throws Exception {
        String email = "summary-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        token = registerAndGetToken(email, "senha123");

        String body = objectMapper.writeValueAsString(Map.of("name", "Projeto Summary"));
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", bearerHeader(token)))
                .andReturn();
        Map<?, ?> created = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        projectId = (String) created.get("id");
    }

    @Test
    @DisplayName("GET /api/projects/{id}/summary: deve retornar 0 segundos para projeto sem entries")
    void summary_noEntries() throws Exception {
        mockMvc.perform(get("/api/projects/" + projectId + "/summary")
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSeconds").value(0));
    }

    @Test
    @DisplayName("GET /api/projects/{id}/summary: deve retornar totalSeconds acumulado")
    void summary_withEntries() throws Exception {
        Instant start = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant end = Instant.now().minus(1, ChronoUnit.HOURS);
        String createBody = objectMapper.writeValueAsString(Map.of(
                "projectId", projectId,
                "description", "1 hora",
                "startTime", start.toString(),
                "endTime", end.toString()
        ));
        mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody)
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/projects/" + projectId + "/summary")
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSeconds").isNumber());
    }

    @Test
    @DisplayName("GET /api/projects/{id}/summary: deve retornar 404 para projeto de outro tenant")
    void summary_otherTenantForbidden() throws Exception {
        String tokenB = registerAndGetToken(
                "summaryb-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com", "senha123");

        mockMvc.perform(get("/api/projects/" + projectId + "/summary")
                        .header("Authorization", bearerHeader(tokenB)))
                .andExpect(status().isNotFound());
    }
}
```

- [ ] **Step 9.2: Criar `UserControllerIT.java`**

```java
package com.qronis.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerIT extends AbstractControllerIT {

    @Test
    @DisplayName("GET /api/users/me: deve retornar dados do usuário autenticado")
    void me_success() throws Exception {
        String email = "me-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
        String token = registerAndGetToken(email, "senha123");

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.tenantId").isNotEmpty())
                .andExpect(jsonPath("$.role").value("OWNER"));
    }

    @Test
    @DisplayName("GET /api/users/me: deve retornar 401 sem token")
    void me_unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }
}
```

- [ ] **Step 9.3: Rodar todos os integration tests**

```bash
./gradlew integrationTest
```

Esperado: todos os controller ITs + repository tests passando.

- [ ] **Step 9.4: Rodar verificação de cobertura**

```bash
./gradlew test jacocoTestReport jacocoCoverageVerification
```

Esperado: `BUILD SUCCESSFUL` com cobertura ≥90%. Se falhar, verifique o relatório em `build/reports/jacoco/test/html/index.html` para identificar classes descobertas e adicionar os casos de teste faltantes.

- [ ] **Step 9.5: Commit**

```bash
git add src/integrationTest/java/com/qronis/controller/ProjectSummaryControllerIT.java \
        src/integrationTest/java/com/qronis/controller/UserControllerIT.java
git commit -m "test(integration): adicionar ProjectSummaryControllerIT e UserControllerIT"
```

---

## Fase 5 — GitHub Actions CI Workflow

### Task 10: Criar workflow CHECK_PULL_REQUEST.yml

**Arquivo:** `.github/workflows/CHECK_PULL_REQUEST.yml`

- [ ] **Step 10.1: Criar diretório e arquivo do workflow**

```bash
mkdir -p .github/workflows
```

- [ ] **Step 10.2: Criar `.github/workflows/CHECK_PULL_REQUEST.yml`**

```yaml
name: CI

on:
  pull_request:
    branches: [ main ]

jobs:
  unit-tests:
    name: Unit Tests + Coverage
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: gradle

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run unit tests with coverage
        run: ./gradlew test jacocoTestReport

      - name: Check minimum coverage (90%)
        run: |
          REPORT="build/reports/jacoco/test/jacocoTestReport.xml"
          if [ ! -f "$REPORT" ]; then
            echo "Jacoco report not found!"
            exit 1
          fi
          LINE_COUNTER=$(grep -o '<counter type="LINE" missed="[0-9]*" covered="[0-9]*"/>' "$REPORT" | tail -1)
          MISSED=$(echo "$LINE_COUNTER" | sed -n 's/.*missed="\([0-9]*\)".*/\1/p')
          COVERED=$(echo "$LINE_COUNTER" | sed -n 's/.*covered="\([0-9]*\)".*/\1/p')
          if [ -z "$MISSED" ] || [ -z "$COVERED" ]; then
            echo "Could not parse coverage data!"
            exit 1
          fi
          TOTAL=$((MISSED + COVERED))
          if [ "$TOTAL" -eq 0 ]; then
            echo "No lines found for coverage!"
            exit 1
          fi
          PERCENT=$(echo "scale=2; $COVERED*100/$TOTAL" | bc)
          echo "Coverage: $PERCENT% (Covered: $COVERED, Missed: $MISSED, Total: $TOTAL)"
          RESULT=$(echo "$PERCENT >= 90" | bc)
          if [ "$RESULT" -eq 1 ]; then
            echo "Coverage check passed."
          else
            echo "Coverage $PERCENT% is below minimum 90%." && exit 1
          fi

      - name: Generate coverage badge
        run: |
          COVERAGE=$(grep -oP 'line-rate="\K[0-9.]+(?=")' build/reports/jacoco/test/jacocoTestReport.xml | head -1)
          PERCENT=$(printf "%.2f" $(echo "$COVERAGE * 100" | bc -l))
          COLOR=red
          if (( $(echo "$PERCENT >= 90" | bc -l) )); then COLOR=brightgreen
          elif (( $(echo "$PERCENT >= 80" | bc -l) )); then COLOR=yellow; fi
          echo "Coverage: $PERCENT% ($COLOR)"
          curl -s "https://img.shields.io/badge/Coverage-${PERCENT}%25-${COLOR}.svg" -o coverage-badge.svg

      - name: Upload coverage badge
        uses: actions/upload-artifact@v4
        with:
          name: coverage-badge
          path: coverage-badge.svg

      - name: Upload JaCoCo HTML report
        uses: actions/upload-artifact@v4
        with:
          name: jacoco-report
          path: build/reports/jacoco/test/html/

  integration-tests:
    name: Integration Tests
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: gradle

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run integration tests
        run: ./gradlew integrationTest
```

- [ ] **Step 10.3: Verificar que o arquivo YAML é válido (opcional, se tiver yamllint)**

```bash
python3 -c "import yaml; yaml.safe_load(open('.github/workflows/CHECK_PULL_REQUEST.yml'))" && echo "YAML válido"
```

Esperado: `YAML válido`

- [ ] **Step 10.4: Commit**

```bash
git add .github/workflows/CHECK_PULL_REQUEST.yml
git commit -m "ci: adicionar workflow CHECK_PULL_REQUEST com unit tests e coverage check"
```

---

## Fase Final — Push e Abertura de PR

- [ ] **Step 11.1: Rodar suite completa local antes do push**

```bash
./gradlew test jacocoTestReport jacocoCoverageVerification integrationTest
```

Esperado: `BUILD SUCCESSFUL` em todos os tasks.

- [ ] **Step 11.2: Atualizar tabela de progresso no início deste arquivo**

Trocar todos os ⬜ por ✅ nas fases concluídas.

- [ ] **Step 11.3: Push e abertura do PR**

```bash
git push -u origin refactor/ci-tests-jacoco
gh pr create \
  --title "feat: CI/JaCoCo/testes de integração — cobertura ≥90%" \
  --body "Implementa o plano definido em docs/superpowers/specs/2026-04-30-ci-tests-design.md" \
  --base main
```

Esperado: PR criado e o workflow `CHECK_PULL_REQUEST` disparado automaticamente no GitHub Actions.

---

## Referências

- Spec: `docs/superpowers/specs/2026-04-30-ci-tests-design.md`
- Branch: `refactor/ci-tests-jacoco`
- Relatório JaCoCo local: `build/reports/jacoco/test/html/index.html`
- Workflow CI: `.github/workflows/CHECK_PULL_REQUEST.yml`
