# Arquitetura: Qronis — Monólito Modular

## TL;DR para Agentes

| Atributo | Valor |
|---|---|
| Estilo arquitetural | Monólito Modular com Spring Modulith |
| Módulos | `auth`, `identity`, `project`, `tracker` |
| Código global (não-módulo) | `shared/` |
| Fronteiras impostas por | `QronisModuleDetectionStrategy` + `ApplicationModules.verify()` |
| Comunicação inter-módulo | Exclusivamente via interfaces em `*.api.*` |
| Versões | Spring Boot 3.5.14 · Spring Modulith 1.4.1 · Java 21 |

---

## Stack Técnico

| Camada | Tecnologia | Versão |
|---|---|---|
| Runtime | Java | 21 (LTS) |
| Framework | Spring Boot | **3.5.14** |
| Módulos | Spring Modulith | **1.4.1** |
| Persistência | Spring Data JPA + Hibernate + PostgreSQL | PG 16 |
| Segurança | Spring Security OAuth2 Resource Server (JWT HS256) | — |
| Mapeamento | MapStruct (compile-time) | 1.6.3 |
| Migrações | Flyway | gerenciado pelo BOM |
| Build | Gradle | 9 |
| Testes | JUnit 5 + Mockito + Testcontainers (PostgreSQL) | — |

> **Nota de compatibilidade:** Spring Modulith 1.4.x exige Spring Boot 3.5.x. A versão 1.4.x não é compatível com Spring Boot 4.x — a chamada interna `ConfigDataEnvironmentPostProcessor.applyTo()` mudou de assinatura no Spring Boot 4 e causa `NoSuchMethodError` em runtime. Não faça upgrade do Spring Boot sem verificar a matriz de compatibilidade do Modulith.

---

## Estrutura de Pacotes

```
src/main/java/com/qronis/
│
├── QronisApplication.java           # @SpringBootApplication + @EnableJpaAuditing
│
├── shared/                          # Código global — NÃO é módulo Modulith
│   ├── config/
│   │   └── QronisModuleDetectionStrategy.java   # Estratégia de detecção de módulos
│   └── exception/
│       ├── ErrorResponseDTO.java    # Contrato único de resposta de erro da API
│       └── GlobalExceptionHandler.java          # Fallback para exceções de infra
│
└── modules/                         # 4 bounded contexts — cada subpacote é um módulo
    ├── auth/
    │   ├── api/
    │   │   └── security/
    │   │       └── AuthenticatedUser.java        # Exposto: helper JWT do SecurityContext
    │   ├── application/
    │   │   ├── AuthService.java
    │   │   └── JwtService.java
    │   ├── config/
    │   │   ├── JwtConfig.java
    │   │   ├── JwtProperties.java
    │   │   └── SecurityConfig.java               # SecurityFilterChain + CORS
    │   ├── domain/exception/
    │   │   └── InvalidCredentialsException.java  # Interno: nunca referenciar de fora
    │   └── web/
    │       ├── AuthController.java
    │       ├── AuthExceptionHandler.java
    │       └── dto/
    │
    ├── identity/
    │   ├── api/
    │   │   ├── IdentityFacade.java               # Exposto: provisão de tenant e auth lookup
    │   │   ├── dto/
    │   │   │   ├── IdentityProvisionResult.java  # Exposto
    │   │   │   └── TenantUserAuthDTO.java        # Exposto
    │   │   └── exception/
    │   │       └── UserAlreadyExistsException.java  # Exposto
    │   ├── application/
    │   │   └── IdentityService.java
    │   ├── domain/
    │   │   ├── entity/
    │   │   │   ├── Tenant.java
    │   │   │   ├── TenantUser.java
    │   │   │   ├── TenantUserId.java             # @EmbeddedId (tenant_id, user_id)
    │   │   │   └── User.java
    │   │   └── enums/
    │   │       └── Role.java                     # Interno: usar String em DTOs cross-module
    │   └── infrastructure/persistence/
    │       ├── TenantRepository.java
    │       ├── TenantUserRepository.java
    │       └── UserRepository.java
    │
    ├── project/
    │   ├── api/
    │   │   ├── ProjectFacade.java                # Exposto: validação e lookup de projetos
    │   │   └── exception/
    │   │       └── ProjectNotFoundException.java  # Exposto
    │   ├── application/
    │   │   ├── ProjectMapper.java
    │   │   └── ProjectService.java
    │   ├── domain/entity/
    │   │   └── Project.java
    │   └── infrastructure/persistence/
    │       └── ProjectRepository.java
    │
    └── tracker/
        ├── api/
        │   ├── TrackerFacade.java                # Exposto: agregação de tempo por projeto
        │   └── exception/
        │       ├── ActiveTimerConflictException.java  # Exposto
        │       ├── InvalidTimeBoundsException.java    # Exposto
        │       └── TimeEntryNotFoundException.java    # Exposto
        ├── application/
        │   ├── TimeEntryMapper.java
        │   └── TrackerService.java
        ├── domain/entity/
        │   └── TimeEntry.java
        └── infrastructure/persistence/
            └── TimeEntryRepository.java
```

---

## Spring Modulith: Fronteiras e Como São Impostas

### O Que o Modulith Faz

Spring Modulith é uma biblioteca que **verifica em tempo de teste** (e opcionalmente em startup) se os módulos da aplicação respeitam as fronteiras definidas. Viola a fronteira → `ApplicationModules.verify()` falha com `AssertionError` detalhado.

O teste que executa essa verificação está em `QronisArchitectureTest`:

```java
class QronisArchitectureTest {
    ApplicationModules modules = ApplicationModules.of(QronisApplication.class);

    @Test
    void verifyModulithArchitecture() {
        modules.verify();  // falha se qualquer módulo violar as fronteiras
    }
}
```

### Como o Modulith Descobre os Módulos

O Modulith precisa saber duas coisas: **onde estão os módulos** e **o que cada módulo expõe**. Ambas são respondidas por `QronisModuleDetectionStrategy`:

```java
public class QronisModuleDetectionStrategy implements ApplicationModuleDetectionStrategy {

    @Override
    public Stream<JavaPackage> getModuleBasePackages(JavaPackage basePackage) {
        return basePackage.getDirectSubPackages().stream()
                .filter(pkg -> pkg.getName().endsWith(".modules"))
                .findFirst()
                .map(modulesPkg -> modulesPkg.getDirectSubPackages().stream())
                .orElseGet(() -> basePackage.getDirectSubPackages().stream());
    }

    @Override
    public NamedInterfaces detectNamedInterfaces(JavaPackage basePackage, ApplicationModuleInformation information) {
        return NamedInterfaces.builder(basePackage)
                .recursive()
                .matching("api")
                .build();
    }
}
```

#### `getModuleBasePackages` — onde estão os módulos

O Modulith chama esse método passando o pacote raiz da aplicação (`com.qronis`, derivado de `QronisApplication.class`). O método:

1. Lista os subpacotes diretos de `com.qronis`: `{modules, shared}`
2. Filtra pelo que termina em `.modules` → encontra `com.qronis.modules`
3. Desce um nível e retorna os filhos: `{auth, identity, project, tracker}`
4. **`shared` nunca entra nessa lista** — não é módulo

O `orElseGet` é um fallback de segurança: se por algum motivo o método receber `com.qronis.modules` como base (não a raiz), retorna os filhos diretos diretamente, que já seriam os 4 módulos.

#### `detectNamedInterfaces` — o que cada módulo expõe

Chamado **uma vez por módulo**. O `basePackage` aqui é o pacote raiz de cada módulo individualmente (`com.qronis.modules.tracker`, etc.). O método:

1. Cria um builder com escopo no pacote raiz do módulo
2. `.recursive()` — desce toda a árvore de subpacotes
3. `.matching("api")` — eleva à condição de **NamedInterface** qualquer subpacote cujo nome simples seja `api`

Resultado para `tracker`: `com.qronis.modules.tracker.api` e `com.qronis.modules.tracker.api.exception` tornam-se a interface pública do módulo. Tudo em `tracker.application`, `tracker.domain`, `tracker.infrastructure` permanece **privado**.

A estratégia é registrada em `application.yml`:

```yaml
spring:
  modulith:
    detection-strategy: com.qronis.shared.config.QronisModuleDetectionStrategy
```

### O Papel do `shared/`

`shared/` **não é um módulo Modulith**. O Modulith não aplica nenhuma regra de fronteira a ele.

Do ponto de vista do Modulith, tipos em `shared/` são **código global da aplicação** — qualquer módulo pode importá-los livremente, sem verificação. É equivalente a uma biblioteca interna.

```
"O tipo referenciado pertence a algum módulo?"
├── Sim → verifica se está em NamedInterface → permite ou bloqueia
└── Não → código global → acesso livre, sem regra aplicada
```

**O que pertence em `shared/`:**

| Tipo | Exemplo | Justificativa |
|---|---|---|
| Contratos de protocolo HTTP | `ErrorResponseDTO` | Não é conceito de negócio de nenhum módulo |
| Handlers de exceção de infra | `GlobalExceptionHandler` | Captura `MethodArgumentNotValidException`, etc. |
| Estratégia de detecção | `QronisModuleDetectionStrategy` | Infraestrutura do próprio Modulith |

**O que NÃO pertence em `shared/`:**

| Tipo | Onde deve estar |
|---|---|
| Exceções de domínio de negócio | `modules/X/api/exception/` ou `modules/X/domain/exception/` |
| Entidades JPA | `modules/X/domain/entity/` |
| Repositórios | `modules/X/infrastructure/persistence/` |
| Configuração específica de módulo | `modules/X/config/` |
| Lógica de negócio | `modules/X/application/` |

`shared/` deve permanecer estritamente transversal. O Modulith não vai reclamar se você colocar lógica de negócio lá — a disciplina é do time.

### Regras de Acoplamento Inter-Módulo

```
┌─────────────────────────────────────────────┐
│  PERMITIDO                                  │
│  Módulo A → tipos em modules.B.api.*        │
│  Qualquer módulo → tipos em shared.*        │
├─────────────────────────────────────────────┤
│  PROIBIDO (verify() falha)                  │
│  Módulo A → tipos em modules.B.application  │
│  Módulo A → tipos em modules.B.domain       │
│  Módulo A → tipos em modules.B.infrastructure│
│  Módulo A → tipos em modules.B.web          │
│  Módulo A → tipos em modules.B.config       │
└─────────────────────────────────────────────┘
```

Toda comunicação cross-module acontece via **Facades** — interfaces em `api/` implementadas internamente pelo módulo:

```java
// tracker chama project via facade pública
// NUNCA: projectRepository.findById(...) direto
projectFacade.validateProjectBelongsToTenant(projectId, tenantId);

// auth chama identity via facade pública
// NUNCA: userRepository.findByEmail(...) direto
identityFacade.getAuthDetailsByEmail(email);
```

### Regra para Tipos no Pacote `api/`

Qualquer tipo colocado em `modules/X/api/` ou `modules/X/api/**` é **automaticamente público** — nenhuma anotação `@NamedInterface` é necessária. A estratégia detecta o pacote pelo nome.

Ao adicionar uma nova Facade, DTO ou Exception ao pacote `api/`, ela já está exposta. Não há nada a fazer além de colocar o arquivo no lugar certo.

Tipos que NÃO devem ir para `api/` mesmo que precisem ser lançados por outros módulos: exceções puramente internas do módulo ficam em `domain/exception/`.

---

## Bounded Contexts e Dependências

```
    auth ──────────────────────────► identity
      │                                  │
      │                                  │ (módulo base,
      │                                  │  sem dependências)
      │
    tracker ──────────────────────► project
      │                                  │
      └──────────────────────────► identity
```

### `identity/` — Identidade & Multi-Tenancy

Gerencia o modelo de usuários, tenants e isolamento multi-tenant. É o módulo **base** — não depende de nenhum outro.

Expõe via `api/`:
- `IdentityFacade` — provisionamento de tenant+usuário, lookup por e-mail, verificação de existência
- `TenantUserAuthDTO` — dados necessários para autenticação
- `IdentityProvisionResult` — resultado do registro
- `UserAlreadyExistsException`

### `auth/` — Autenticação & Autorização

Responsável por registro, login e emissão de JWTs. Toda a configuração de segurança (`SecurityConfig`, `JwtConfig`, `JwtProperties`) vive **dentro deste módulo** — não há configuração de segurança em `shared/`.

Tokens emitidos carregam claims: `sub` (userId), `tenantId`, `email`, `role`.

Depende de: `identity` (via `IdentityFacade`)

Expõe via `api/`:
- `AuthenticatedUser` — helper para extrair dados do JWT do `SecurityContext`

### `project/` — Gestão de Projetos

CRUD de projetos com escopo por tenant. Projetos pertencem a um `Tenant` (referenciado por UUID puro — sem `@ManyToOne` cross-module).

Depende de: `identity` (via banco, UUID direto)

Expõe via `api/`:
- `ProjectFacade` — validação de acesso e lookup de nome de projeto
- `ProjectNotFoundException`

### `tracker/` — Rastreamento de Tempo

Timer start/stop, lançamentos manuais e histórico paginado. Também hospeda `ProjectSummaryController` para agregar horas por projeto.

Depende de: `identity` (via banco), `project` (via `ProjectFacade`)

Expõe via `api/`:
- `TrackerFacade` — total de segundos por projeto/usuário
- `ActiveTimerConflictException`, `InvalidTimeBoundsException`, `TimeEntryNotFoundException`

---

## Camadas Internas por Módulo

| Camada | Pacote | Visibilidade Modulith | Responsabilidade |
|---|---|---|---|
| **API** | `api/` | **Pública** — acessível por outros módulos | Facades, DTOs e Exceptions expostos |
| **Web** | `web/` | Privada | Controllers REST, Exception Handlers, DTOs HTTP |
| **Application** | `application/` | Privada | Services (casos de uso), Mappers (MapStruct) |
| **Domain** | `domain/` | Privada | Entidades JPA, Exceções internas, Enums |
| **Infrastructure** | `infrastructure/` | Privada | Repositórios JPA |
| **Config** | `config/` | Privada | Configuração específica do módulo |

Obs.: `web/` não precisa ser acessível de fora porque Controllers são chamados pelo framework, não por outros módulos.

---

## Padrões Importantes

### Exception Handlers por Módulo

Cada módulo tem seu próprio `@RestControllerAdvice` com `basePackages` restrito ao próprio módulo. O `GlobalExceptionHandler` em `shared/` só captura exceções de infraestrutura do framework que nenhum módulo tratou.

```java
@RestControllerAdvice(basePackages = "com.qronis.modules.tracker")
public class TrackerExceptionHandler {
    @ExceptionHandler(ActiveTimerConflictException.class)
    public ResponseEntity<ErrorResponseDTO> handle(ActiveTimerConflictException ex) { ... }
}
```

### Autenticação nos Controllers

Controllers injetam o JWT via `@AuthenticationPrincipal Jwt jwt` — nunca via helper estático. `AuthenticatedUser.fromContext()` existe para casos onde injeção de parâmetro não é prática (filtros, interceptors), mas não é o padrão preferido em controllers.

```java
@GetMapping
public ResponseEntity<?> list(@AuthenticationPrincipal Jwt jwt) {
    UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenantId"));
    UUID userId   = UUID.fromString(jwt.getSubject());
}
```

### Chaves Estrangeiras Cross-Module

Entidades nunca têm `@ManyToOne` apontando para entidades de outro módulo. Referências cross-module são feitas por **UUID puro** no banco. A consistência é garantida por constraint de FK no PostgreSQL (via Flyway), não pelo ORM.

```java
// CORRETO — UUID puro, sem @ManyToOne
@Column(name = "tenant_id", nullable = false)
private UUID tenantId;

// PROIBIDO — cria acoplamento entre módulos no nível JPA
@ManyToOne
private Tenant tenant;
```

### Auditoria JPA

Todas as entidades usam `@CreatedDate` e `@LastModifiedDate` via Spring Data JPA Auditing (habilitado via `@EnableJpaAuditing` em `QronisApplication`). Não há `BaseEntity` compartilhada — cada entidade declara os campos `createdAt` e `updatedAt` diretamente.

---

## Contrato de Erro da API

Todas as respostas de erro seguem `ErrorResponseDTO`:

```json
{
  "status": 404,
  "error": "PROJECT_NOT_FOUND",
  "message": "Projeto não encontrado: <id>",
  "errors": null,
  "timestamp": "2026-04-28T01:00:00Z"
}
```

Em erros de validação, `errors` contém o mapa de campos:

```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Erro de validação nos campos enviados",
  "errors": { "name": "Nome do projeto é obrigatório" },
  "timestamp": "2026-04-28T01:00:00Z"
}
```

O campo `error` sempre usa `UPPER_SNAKE_CASE`. A lista de códigos possíveis está nos handlers de cada módulo.
