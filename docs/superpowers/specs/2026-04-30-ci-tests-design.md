# Design: CI/CD, Cobertura de Testes e Qualidade — Qronis

**Data:** 2026-04-30  
**Status:** Aprovado

---

## Visão Geral

Elevar a maturidade de qualidade do Qronis com três pilares:

1. **Cobertura ≥ 90%** via JaCoCo, medida sobre código de lógica real (excluindo gerados e infra)
2. **Testes de integração** em source set separado com Testcontainers + PostgreSQL real
3. **CI automático** no GitHub Actions que valida todo PR aberto para `main`

---

## Arquitetura de Testes

### Source Sets

```
src/
├── test/java/com/qronis/              ← unit tests (JUnit 5 + Mockito, sem Spring context)
│   ├── QronisArchitectureTest         (existente — Spring Modulith verify)
│   ├── service/
│   │   ├── AuthServiceTest            (existente)
│   │   ├── ProjectServiceTest         (existente)
│   │   ├── TrackerServiceTest         (existente)
│   │   ├── IdentityServiceTest        (novo)
│   │   └── JwtServiceTest             (novo)
│
└── integrationTest/java/com/qronis/   ← novo source set
    ├── AbstractIntegrationTest        (movido de src/test)
    ├── repository/
    │   ├── ProjectRepositoryTest      (movido de src/test)
    │   └── TimeEntryRepositoryTest    (movido de src/test)
    └── controller/
        ├── AuthControllerIT           (novo)
        ├── ProjectControllerIT        (novo)
        ├── TimeEntryControllerIT      (novo)
        ├── ProjectSummaryControllerIT (novo)
        └── UserControllerIT           (novo)
```

### Estratégia por camada

| Camada | Abordagem | Source set |
|---|---|---|
| Services (AuthService, IdentityService, ProjectService, TrackerService) | Mockito puro | `src/test` |
| JwtService | Mockito puro | `src/test` |
| Arquitetura Modulith | `ApplicationModules.verify()` | `src/test` |
| Repositories (Project, TimeEntry) | `@SpringBootTest` + Testcontainers | `src/integrationTest` |
| Controllers + Exception Handlers + Mappers + Facades | MockMvc + `@SpringBootTest` + Testcontainers | `src/integrationTest` |

Os controller ITs cobrem por transitividade: ExceptionHandlers, Mappers (MapStruct), Facades e entidades JPA.

---

## Cobertura JaCoCo

### Configuração no Gradle

- Plugin `jacoco` adicionado
- Task `jacocoTestReport` executa após `test`
- Task `jacocoCoverageVerification` checa minimum ≥ 90% em linha
- Relatório XML gerado em `build/reports/jacoco/test/jacocoTestReport.xml`

### Exclusões da métrica

Estas classes são excluídas da contagem de cobertura por serem geradas, de configuração ou sem lógica testável:

| Padrão | Motivo |
|---|---|
| `**/*DTO.*` | Java Records de transferência — sem lógica |
| `**/*MapperImpl.*` | Gerado pelo MapStruct em compile-time |
| `**/*Config.*` | Configuração Spring (SecurityConfig, JwtConfig) |
| `**/*Properties.*` | Binding de properties (JwtProperties) |
| `**/QronisApplication.*` | Entry point da aplicação |
| `**/QronisModuleDetectionStrategy.*` | Infraestrutura do Modulith |

---

## Gradle (`build.gradle.kts`)

### Source set `integrationTest`

```kotlin
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
```

### Task `integrationTest`

```kotlin
val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Roda testes de integração com Testcontainers + PostgreSQL"
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks.test)
}
```

### JaCoCo

```kotlin
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = true
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/*DTO.*",
                    "**/*MapperImpl.*",
                    "**/*Config.*",
                    "**/*Properties.*",
                    "**/QronisApplication.*",
                    "**/QronisModuleDetectionStrategy.*"
                )
            }
        })
    )
}
```

---

## Cenários de Teste por Controller IT

### `AuthControllerIT`
- `POST /api/auth/register` — 200 com JWT
- `POST /api/auth/register` — 409 email duplicado
- `POST /api/auth/login` — 200 com JWT
- `POST /api/auth/login` — 401 senha incorreta
- `POST /api/auth/login` — 401 email inexistente
- `POST /api/auth/register` — 400 campos inválidos (validação Bean Validation)

### `ProjectControllerIT`
- `GET /api/projects` — listagem paginada com JWT válido
- `GET /api/projects` — 401 sem token
- `POST /api/projects` — 201 projeto criado
- `POST /api/projects` — 400 nome em branco
- `DELETE /api/projects/{id}` — 204 ok
- `DELETE /api/projects/{id}` — 404 projeto inexistente
- Isolamento de tenant: projeto do tenant B retorna 404 para usuário do tenant A

### `TimeEntryControllerIT`
- `POST /api/time-entries` — start timer (endTime null)
- `POST /api/time-entries` — 409 timer já ativo (ActiveTimerConflictException)
- `PATCH /api/time-entries/{id}` — stop timer (endTime preenchido)
- `PATCH /api/time-entries/{id}` — patch description
- `PATCH /api/time-entries/{id}` — patch projectId com validação de tenant
- `PATCH /api/time-entries/{id}` — 400 bounds inválidos (start >= end)
- `GET /api/time-entries` — histórico paginado
- `DELETE /api/time-entries/{id}` — 204 ok
- `DELETE /api/time-entries/{id}` — 404 entry inexistente

### `ProjectSummaryControllerIT`
- `GET /api/projects/summary` — retorna lista com totalSeconds por projeto

### `UserControllerIT`
- `GET /api/users/me` — 200 com dados do usuário autenticado
- `GET /api/users/me` — 401 sem token

---

## GitHub Actions Workflow

**Arquivo:** `.github/workflows/CHECK_PULL_REQUEST.yml`  
**Trigger:** `pull_request` → branches: `[main]`

### Job 1: `unit-tests`

```yaml
- uses: actions/checkout@v4
- uses: actions/setup-java@v4 (Java 21, temurin)
- run: ./gradlew test jacocoTestReport
- run: verificação de cobertura ≥ 90% via script bash no XML do JaCoCo
- uses: actions/upload-artifact@v4 (badge de cobertura SVG)
```

### Job 2: `integration-tests`

```yaml
- uses: actions/checkout@v4
- uses: actions/setup-java@v4 (Java 21, temurin)
- run: ./gradlew integrationTest
```

Os dois jobs rodam **em paralelo**. Testcontainers gerencia o PostgreSQL internamente no job de integration — sem `services: postgres` no workflow. `ubuntu-latest` tem Docker disponível nativamente.

---

## Convenções de Nomenclatura

- Unit tests: sufixo `Test` (ex: `AuthServiceTest`)
- Integration tests: sufixo `IT` para controllers (ex: `AuthControllerIT`), sufixo `Test` para repositories (ex: `ProjectRepositoryTest`)
- Todos os testes usam `@DisplayName` em português para legibilidade nos relatórios

---

## O que NÃO está no escopo

- Testes de frontend (React/Vitest)
- Testes E2E (Playwright/Selenium)
- Mutation testing (PIT)
- Performance/load testing
