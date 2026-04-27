# Arquitetura: Monólito Modular — Qronis

## Visão Geral

O backend do Qronis está organizado como um **Monólito Modular** — uma única unidade de deploy com separação interna clara por contextos de negócio (*Bounded Contexts*). Esta arquitetura equilibra a simplicidade operacional do monolito com a organização e isolamento típicos de microserviços.

A abordagem situa-se no espectro do **DDD Lite**: utilizamos o vocabulário e os princípios de Domain-Driven Design (contextos delimitados, exceções de domínio semânticas, camadas de aplicação) sem implementar padrões táticos complexos como Aggregates, Domain Events ou Event Sourcing.

---

## Estrutura de Pacotes

```
src/main/java/com/qronis/
├── QronisApplication.java
│
├── shared/                          # Infraestrutura transversal
│   ├── config/
│   │   └── SecurityConfig.java      # Configuração Spring Security + JWT
│   ├── exception/
│   │   ├── ErrorResponseDTO.java    # Contrato único de resposta de erro
│   │   └── GlobalExceptionHandler.java  # Fallback global
│   └── security/
│       └── AuthenticatedUser.java   # Helper (mantido por compatibilidade)
│
└── modules/
    ├── auth/                        # Contexto: Autenticação & Autorização
    │   ├── api/
    │   │   ├── AuthController.java
    │   │   ├── AuthExceptionHandler.java
    │   │   ├── UserController.java
    │   │   └── dto/
    │   ├── application/
    │   │   ├── AuthService.java
    │   │   ├── JwtService.java
    │   │   └── UserService.java
    │   ├── config/
    │   │   ├── JwtConfig.java
    │   │   └── JwtProperties.java
    │   └── domain/exception/
    │       └── InvalidCredentialsException.java
    │
    ├── identity/                    # Contexto: Identidade & Multi-Tenancy
    │   ├── application/repositories/
    │   │   ├── TenantRepository.java
    │   │   ├── TenantUserRepository.java
    │   │   └── UserRepository.java
    │   └── domain/
    │       ├── entity/
    │       │   ├── BaseEntity.java
    │       │   ├── Tenant.java
    │       │   ├── TenantUser.java
    │       │   ├── TenantUserId.java
    │       │   └── User.java
    │       └── enums/
    │           └── Role.java
    │
    ├── project/                     # Contexto: Gestão de Projetos
    │   ├── api/
    │   │   ├── ProjectController.java
    │   │   ├── ProjectExceptionHandler.java
    │   │   └── dto/
    │   ├── application/
    │   │   ├── ProjectMapper.java
    │   │   ├── ProjectService.java
    │   │   └── repositories/
    │   │       └── ProjectRepository.java
    │   └── domain/
    │       ├── entity/Project.java
    │       └── exception/
    │           └── ProjectNotFoundException.java
    │
    └── tracker/                     # Contexto: Rastreamento de Tempo
        ├── api/
        │   ├── TimeEntryController.java
        │   ├── TrackerExceptionHandler.java
        │   └── dto/
        ├── application/
        │   ├── TimeEntryMapper.java
        │   ├── TimeEntryService.java
        │   └── repositories/
        │       └── TimeEntryRepository.java
        └── domain/
            ├── entity/TimeEntry.java
            └── exception/
                ├── ActiveTimerConflictException.java
                ├── InvalidTimeBoundsException.java
                └── TimeEntryNotFoundException.java
```

---

## Camadas por Módulo

Cada módulo segue a mesma estrutura de três camadas:

| Camada | Pacote | Responsabilidade |
|--------|--------|-----------------|
| **API** | `api/` | Controllers REST, DTOs de entrada/saída, Exception Handlers |
| **Application** | `application/` | Services (casos de uso), Mappers (MapStruct), Repositórios (interfaces JPA) |
| **Domain** | `domain/` | Entidades JPA, Exceções de domínio semânticas, Enums |

---

## Bounded Contexts

### `identity/` — Identidade & Multi-Tenancy
Gerencia o modelo de usuários e o isolamento multi-tenant. Não expõe controllers próprios — suas entidades (`Tenant`, `User`, `TenantUser`) são usadas pelos outros módulos via repositório.

**Dependências de saída:** nenhuma (módulo base)

### `auth/` — Autenticação & Autorização
Responsável por registro, login e emissão de JWTs. Produz tokens com claims `sub` (userId), `tenantId`, `email` e `role`.

**Dependências de saída:** `identity/` (repositórios de User e Tenant)

### `project/` — Gestão de Projetos
CRUD de projetos com escopo por tenant. Inclui sumarização de horas por projeto.

**Dependências de saída:** `identity/` (entidades), `tracker/` (repositório TimeEntry para sumário)

### `tracker/` — Rastreamento de Tempo
Timer start/stop, lançamentos manuais e histórico paginado de entradas de tempo.

**Dependências de saída:** `identity/` (User), `project/` (ProjectService para validação de tenant)

---

## Princípios de Isolamento

### Exception Handlers por Módulo
Cada módulo tem seu próprio `@RestControllerAdvice` com `basePackages` restrito:

```java
@RestControllerAdvice(basePackages = "com.qronis.modules.project")
public class ProjectExceptionHandler { ... }
```

Isso garante que cada módulo gerencie suas próprias exceções de domínio sem vazar para outros contextos.

### Exceções de Domínio Semânticas
Cada módulo define suas próprias exceções ao invés de usar genéricas:

| Módulo | Exceção | HTTP |
|--------|---------|------|
| `auth` | `InvalidCredentialsException` | 401 |
| `project` | `ProjectNotFoundException` | 404 |
| `tracker` | `TimeEntryNotFoundException` | 404 |
| `tracker` | `ActiveTimerConflictException` | 409 |
| `tracker` | `InvalidTimeBoundsException` | 400 |

### Autenticação via `@AuthenticationPrincipal Jwt`
Todos os controllers injetam o JWT diretamente, sem dependência de um utilitário estático:

```java
@GetMapping
public ResponseEntity<?> list(@AuthenticationPrincipal Jwt jwt) {
    UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenantId"));
    UUID userId   = UUID.fromString(jwt.getSubject());
    ...
}
```

---

## Contrato de Erro da API

Todas as respostas de erro seguem o contrato de `ErrorResponseDTO`:

```json
{
  "status": 404,
  "error": "PROJECT_NOT_FOUND",
  "message": "Projeto não encontrado: <id>",
  "errors": null,
  "timestamp": "2025-04-27T01:00:00Z"
}
```

Em erros de validação (`400 VALIDATION_ERROR`), o campo `errors` contém um mapa de campos inválidos:

```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Erro de validação nos campos enviados",
  "errors": { "name": "Nome do projeto é obrigatório" },
  "timestamp": "2025-04-27T01:00:00Z"
}
```

---

## Decisões Arquiteturais (ADRs)

| ADR | Decisão |
|-----|---------|
| ADR-015 | Adotar Monólito Modular com Bounded Contexts em vez de pacotes planos |
| ADR-016 | `JwtConfig` e `JwtProperties` pertencem ao módulo `auth/` (não a `shared/`) |
| ADR-017 | Exception Handlers com `basePackages` por módulo em vez de handler global único |
| ADR-018 | Exceções de domínio semânticas por módulo em vez de `BusinessException`/`ResourceNotFoundException` genéricos |
| ADR-019 | Services dependem de repositórios de outros módulos diretamente (não de services), para evitar acoplamento circular |
| ADR-020 | `@AuthenticationPrincipal Jwt` injetado nos controllers em vez de `AuthenticatedUser.fromContext()` estático |

---

## Stack Técnico

- **Runtime:** Java 21, Spring Boot 3.x
- **Persistência:** Spring Data JPA + Hibernate, PostgreSQL
- **Segurança:** Spring Security OAuth2 Resource Server (JWT Bearer)
- **Mapeamento:** MapStruct (geração em compile-time)
- **Testes:** JUnit 5, Mockito, Spring Boot Test, Testcontainers (PostgreSQL)
- **Build:** Gradle 9
