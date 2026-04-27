# Refatoração: Monólito Modular — Status Final

**Projeto:** Qronis Backend  
**Branch:** `refactor/modular-monolith`  
**Status:** ✅ Concluído  
**Gate final:** 33 tests, 0 failed — BUILD SUCCESSFUL

---

## Objetivo

Refatorar o backend Qronis de uma arquitetura plana (pacotes `controller/`, `service/`, `entity/`) para um **Monólito Modular** organizado por contextos de negócio (*Bounded Contexts*), seguindo princípios de DDD Lite.

---

## Etapas

### 🟢 Etapa 1 — Infraestrutura Transversal (`shared/`)
*(Concluída)*
- [x] Criar `shared/config/SecurityConfig.java`
- [x] Criar `shared/exception/ErrorResponseDTO.java` (contrato único de erro)
- [x] Criar `shared/security/AuthenticatedUser.java`
- [x] Stubs `@Deprecated` nos legados `com.qronis.config.*`
- [x] Validar gate: 33 tests, 1 falha pré-existente (não regressão)

### 🟢 Etapa 2 — Módulo `identity/`
*(Concluída)*
- [x] Mover entidades: `Tenant`, `User`, `TenantUser`, `TenantUserId`, `Role`, `BaseEntity`
- [x] Mover repositórios: `TenantRepository`, `UserRepository`, `TenantUserRepository`
- [x] Stubs `@Deprecated` nos legados sem `@Entity`/`JpaRepository`
- [x] Atualizar imports nos testes de integração
- [x] Validar gate: 33 tests, 1 falha pré-existente (não regressão)

### 🟢 Etapa 3 — Módulo `auth/`
*(Concluída)*
- [x] Mover `AuthService`, `JwtService`, `UserService` → `modules/auth/application/`
- [x] Mover `AuthController`, `UserController` + DTOs → `modules/auth/api/`
- [x] `JwtConfig` e `JwtProperties` → `modules/auth/config/`  _(ADR-016)_
- [x] Criar `InvalidCredentialsException` → `modules/auth/domain/exception/`
- [x] Criar `AuthExceptionHandler` com `basePackages` → `modules/auth/api/`
- [x] Atualizar `AuthServiceTest`: substituir `BadCredentialsException` por `InvalidCredentialsException`
- [x] Validar gate: 33 tests, 1 falha pré-existente (não regressão)

### 🟢 Etapa 4 — Módulo `project/`
*(Concluída)*
- [x] Mover entidade `Project` → `modules/project/domain/entity/`
- [x] Mover `ProjectRepository` → `modules/project/application/repositories/`
- [x] Mover `ProjectService` + `ProjectMapper` → `modules/project/application/`
- [x] Mover `ProjectController` + DTOs → `modules/project/api/`
- [x] Criar `ProjectNotFoundException` → `modules/project/domain/exception/`
- [x] Criar `ProjectExceptionHandler` (`basePackages`) → `modules/project/api/`
- [x] `ProjectController`: migrar para `@AuthenticationPrincipal Jwt` _(ADR-020)_
- [x] Corrigir falha pré-existente: `ORDER BY te.startTime DESC` em `findByUserIdWithProject`
- [x] Validar gate: BUILD SUCCESSFUL (33 tests, 0 failed)

### 🟢 Etapa 5 — Módulo `tracker/`
*(Concluída)*
- [x] Mover entidade `TimeEntry` → `modules/tracker/domain/entity/`
- [x] Mover `TimeEntryRepository` → `modules/tracker/application/repositories/`
- [x] Mover `TimeEntryService` + `TimeEntryMapper` → `modules/tracker/application/`
- [x] Mover `TimeEntryController` + DTOs → `modules/tracker/api/`
- [x] Criar `ActiveTimerConflictException`, `InvalidTimeBoundsException`, `TimeEntryNotFoundException`
- [x] Criar `TrackerExceptionHandler` (`basePackages`) → `modules/tracker/api/`
- [x] `TimeEntryController`: migrar para `@AuthenticationPrincipal Jwt` _(ADR-020)_
- [x] Validar gate: BUILD SUCCESSFUL (33 tests, 0 failed)

### 🟢 Etapa 6 — Limpeza dos Legados
*(Concluída)*
- [x] Deletar `config/`, `controller/`, `dto/`, `entity/`, `exception/`, `mapper/`, `repository/`, `security/`, `service/`
- [x] Migrar `GlobalExceptionHandler` → `shared/exception/` (usa `ErrorResponseDTO` do shared)
- [x] Validar gate: BUILD SUCCESSFUL (33 tests, 0 failed)

### 🟢 Etapa 7 — Documentação
*(Concluída)*
- [x] Atualizar este arquivo com status final
- [x] Criar `docs/architecture.md` com estrutura modular e ADRs
- [x] Commit final na branch `refactor/modular-monolith`

---

## Decisões Relevantes

| ADR | Decisão |
|-----|---------|
| ADR-015 | Adotar Monólito Modular em vez de pacotes planos |
| ADR-016 | `JwtConfig`/`JwtProperties` pertencem ao módulo `auth/` |
| ADR-017 | Exception Handlers com `basePackages` por módulo |
| ADR-018 | Exceções de domínio semânticas por módulo |
| ADR-019 | Services dependem de repositórios (não de services) de outros módulos |
| ADR-020 | `@AuthenticationPrincipal Jwt` nos controllers, sem helper estático |

---

## Bugs Corrigidos no Processo

- `TimeEntryRepositoryTest#findByUserIdWithProject`: query `findByUserIdWithProject` sem `ORDER BY` produzia ordenação não determinística. Corrigido adicionando `ORDER BY te.startTime DESC` à query JPQL.

---

## Resultado Final

**33 testes passando, 0 falhas.**  
Todos os pacotes legados (`com.qronis.controller`, `.service`, `.entity`, `.dto`, `.repository`, `.mapper`, `.exception`, `.config`, `.security`) foram removidos.  
A codebase está estruturada em 4 módulos de negócio + 1 infraestrutura transversal.
