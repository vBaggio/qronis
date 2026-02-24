# Architecture Decision Records (ADRs) & Memória de Longo Prazo

Este documento registra decisões arquiteturais cruciais para que o contexto de **porquê** algo foi feito não se perca no tempo. Baseado na arquitetura de Spec-Driven Development, estas memórias evitam a repetição de erros (lessons learned).

---

## ADR 001: Modelagem de Chaves Primárias (UUIDs)
**Status:** Aceito
**Contexto:** Necessitávamos de um identificador que garantisse segurança contra enumeração (IDOR) e facilitasse a fusão de bancos de dados no futuro, comum em cenários SaaS.
**Decisão:**
- O uso de `UUID` (geração v4 via Hibernate) é **obrigatório** como chave primária (`PK`) para todas as tabelas principais (`Tenant`, `User`, `Project`, `TimeEntry`).
- **Exceção Estratégica:** A tabela pivot `tenant_user` não possui UUID próprio. Sua PK é a chave composta das Foreign Keys `(tenant_id, user_id)` representada pela classe embutida `@EmbeddedId TenantUserId`.

---

## ADR 002: Proteção no Banco contra Múltiplos Timers Ativos
**Status:** Aceito
**Contexto:** O domínio exige que um usuário não possa ter mais de uma tarefa simultânea rodando (onde `end_time` é nulo). Depender apenas de validações lógicas na camada de serviço (Racing Conditions) era arriscado.
**Decisão:**
- A regra foi descida para o nível do banco de dados (Hard Constraint).
- Criado um **Partial Unique Index** no PostgreSQL (`idx_time_entry_active_per_user`).
- Definição: `CREATE UNIQUE INDEX ... ON time_entry (user_id) WHERE end_time IS NULL`.
- Isso garante de forma matemática a integridade do dado: o banco rejeitará qualquer tentativa paralela de iniciar um segundo cronômetro.

---

## ADR 003: Simplificação Extrema do Login (1 Query Policy)
**Status:** Aceito
**Contexto:** O Spring Security por padrão incentiva o injetamento de `UserDetailsService`, o que frequentemente gera queries desnecessárias (buscando o usuário e depois sub-queries para as roles/tenants).
**Decisão:**
- Contornar o padrão engessado do framework usando uma consulta centralizada `findByEmailWithTenant` usando `JOIN FETCH`.
- O Login agora acontece usando o `AuthService` manualmente: busca a relação `TenantUser → User` (1 roundtrip ao banco) e valida o hash bcrypt diretamente (`passwordEncoder.matches()`).

---

## ADR 004: JWT como Portador de Claims Integradas
**Status:** Aceito
**Contexto:** Após a requisição inicial, precisávamos evitar idas constantes ao banco para checar o nome do usuário, seu tenant ativo e sua permissão (Role).
**Decisão:**
- O JWT gerado (`JwtService.generateToken(...)`) não contém apenas o ID (Subject).
- Ele é enriquecido com Claims customizadas: `email`, `name`, `tenantId` e `role`.
- O `AuthenticatedUser` converte esse payload diretamente no SecurityContext, permitindo que os Controllers filtrem os repositórios (Zero Db Queries na validação de sessão).

---

## ADR 005: Deleção Lógica vs Física (Hard Delete para MVP)
**Status:** Aceito
**Contexto:** No desenvolvimento da infraestrutura inicial, discutiu-se a necessidade de um Soft Delete (`is_deleted` flag).
**Decisão:**
- Para manter a tração e agilidade do MVP livre de complexidades em exclusões em cascata e filtros de WHERE globais (Hibernate `@SQLRestriction`), optou-se pela exclusão física (Hard Delete).
- Registros apagados no `/api/time-entries/{id}` somem definitivamente. O mesmo ocorre ao deletar Projetos.

---

## ADR 006: Compatibilidade com Spring Boot 4
**Status:** Aceito
**Contexto:** O upgrade prematuro para o recém-lançado Spring Boot 4 quebrou o ecossistema padrão de testes que usava flyway nativo (`flyway-core`).
**Decisão:**
- Como o projeto migrou a autoconfiguração do flyway para um wrapper, fomos forçados a importar o módulo `spring-boot-starter-flyway` explícito no `build.gradle.kts` e ajustar a parametrização do `ddl-auto` no profile de testes com Testcontainers.
