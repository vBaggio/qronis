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

---

## ADR 007: Vulnerabilidade N+1 no endpoint PATCH de TimeEntry (Conhecida)
**Status:** Adiado / Mapeado
**Contexto:** Durante a revisão profunda da base de código para geração das documentações de arquitetura, foi identificado um ponto cego nas Regras Estritas de mapeamento prático. O método `TimeEntryService.patch()` utiliza o método padrão do JpaRepository (`findById`), que carrega a entidade `TimeEntry` de forma estrita (Lazy Loader ativado). No entanto, o retorno deste service é convertido para um `TimeEntryResponseDTO` pelo MapStruct no `TimeEntryController`.
- Como o DTO exige os campos `projectId` e `projectName`, e o `Project` não foi carregado via `JOIN FETCH` na origem da busca pelo ID, o Hibernate dispara uma **Query N+1** de forma implícita durante a conversão do MapStruct.
**Tentativa de Correção:**
- Foi tentada a criação de um método `findByIdAndCreatedByIdWithProject` abusando de `JOIN FETCH` diretamente no `TimeEntryRepository` e substituí-lo na service.
- **Motivo do Rollback:** A alteração na camada de persistência introduziu quebras colaterais em vários testes unitários sensíveis (`TimeEntryServiceTest`), requerendo a reescrita massiva de Mocks e injeções de dependência que não estavam no escopo do momento.
**Decisão Atual:**
- O código-fonte Java sofreu `git restore` e voltou ao estado funcional com cobertura 100%. A query N+1 nesse fluxo específico é aceitável por ora (dado o impacto controlado de apenas buscar 1 projeto e não realizar paginação em lote do DTO individual). A refatoração foi anotada para ser corrigida apenas quando as suítes E2E de Controller estiverem totalmente maduras e seguras.

---

## ADR 008: Regra de Ouro do Cronômetro (Frontend)
**Status:** Aceito
**Contexto:** Ao construir o Live Tracker (ZenTimer), usar `setInterval` iterando uma variável de estado (`setSeconds(s => s+1)`) gera dessincronização severa devido a gargalos do event-loop do navegador (abas inativas, processamento pesado em outras páginas).
**Decisão:**
- O cronômetro visual é estritamente derivativo e "stateless" em relação à contagem temporal.
- A matemática adotada é ancorada em carimbo absoluto: `Date.now() - startTime` (onde `startTime` é o ISO Time real retornado pelo backend).
- O React apenas guarda o resultado do delta em segundos no estado para forçar o re-render, mas essa soma nunca é feita de forma recursiva a partir de si mesmo. As abas podem dormir ou processar lags sem afetar a verdade do cronômetro.

---

## ADR 009: Otimização de Espaçamentos Condicionais (Flex Gap vs Space-Y)
**Status:** Aceito
**Contexto:** Nos formulários de autenticação (Login/Register) deparamo-nos com componentes ocultos condicionalmente (mensagens de texto vermelho `{error && ...}`). O uso utilitário do Tailwind `space-y-*` forçava a injeção falha de top-margins baseada em seletores sibling `> * + *`, que não calculam a abstração do React.
**Decisão:**
- Formações de tela com caixas escondíveis/condicionais aboliram categoricamente a utilidade `space-y-*`.
- Adotou-se o modelo inviolável em container pai: `flex flex-col gap-*`. O Gap lida inerentemente com a ausência de componentes no DOM e distribui os elásticos perfeitamente, sem "margins" vazadas.

---

## ADR 010: Criação In-Flow de Associações (Project Selector)
**Status:** Aceito
**Contexto:** Como forçar que uma Task pertença a um `Project`, se o usuário não tiver aquele determinado projeto criado ainda? Uma rota de escape para `/projetos/novo` assassinaria completamente o senso prático do Tracker.
**Decisão:**
- Abordagem unificada: O filtro do `<ComboBox>` monitora a String livre.
- Se os resultados para o Array retornarem Length 0 e a query for válida, injecta-se um *Action Button* transparente na lista para "Criar novo projeto [Query]".
- Isso perfura o fluxo, joga um `POST /api/projects` no backend e recupera o ID simultaneamente atrelando ao Timer. O Foco não se quebra.

---

## ADR 011: Plano de Evolução UI/UX em 3 Camadas (Grade de Projetos)
**Status:** Aceito
**Contexto:** Após a entrega funcional da Grade de Projetos (`/projects`), foi realizada uma análise profunda de UI/UX cruzando agentes (`frontend-specialist`, `performance-optimizer`), skills (`react-best-practices`, `web-design-guidelines`, `performance-profiling`, `i18n-localization`, `frontend-design`) e toda a documentação do produto para elevar o frontend de "funcional" para "produto de primeira linha".
**Decisão:**
- **Camada 1 (Quick Wins):** Dialog de exclusão customizado, busca live com debounce + filtro backend por nome, accent colors via `border-l-4` (estilo Linear/Notion), coesão visual `rounded-full`, hover com elevação e stagger animation nas rows.
- **Camada 2 (Identidade Visual):** Toast notifications via `sonner`, skeleton loading, empty state humanizado, refinamento TopNav, tipografia Inter, dark mode toggle.
- **Camada 3 (Infraestrutura):** Lazy loading de rotas (`React.lazy`), Error Boundary global, acessibilidade (a11y), preparação estrutural i18n.
- **Filosofia:** Todas as decisões respeitam o DNA Zen/Minimalista do Qronis — polimento, não adição de complexidade.

---

## ADR 012: @RequestParam Requer `name` Explícito (Spring Boot / Gradle)
**Status:** Aceito
**Contexto:** Ao adicionar o parâmetro de busca `?name=` ao endpoint `GET /api/projects`, o método do Controller foi anotado com `@RequestParam(required = false) String name`. O backend passou a retornar **400 Bad Request** em todas as chamadas ao endpoint — inclusive sem o parâmetro `name` — com a mensagem: *"Name for argument of type [java.lang.String] not specified, and parameter name information not available via reflection. Ensure that the compiler uses the '-parameters' flag."*
**Causa Raiz:** O compilador Java, por padrão no Gradle sem configuração adicional, não preserva os nomes dos argumentos de método no bytecode. O Spring tenta inferir o nome do `@RequestParam` via reflection, mas falha sem a flag `-parameters`. Isso quebrou todo o endpoint `GET /api/projects` (o frontend recebia 400 e limpava a lista para `[]` no `catch` block).
**Decisão:**
- **Regra inviolável:** Todo `@RequestParam`, `@PathVariable` e `@RequestHeader` no projeto **deve** declarar o atributo `name` (ou `value`) de forma explícita.
- Correto: `@RequestParam(name = "name", required = false) String name`
- Incorreto: `@RequestParam(required = false) String name`
- Alternativa sistêmica (não adotada por ora): adicionar `compileJava { options.compilerArgs << "-parameters" }` no `build.gradle.kts`.
