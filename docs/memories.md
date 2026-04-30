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

## ADR 006: Versões de Spring Boot e Spring Modulith (Decisão de Compatibilidade)
**Status:** Aceito
**Contexto:** Ao implementar Spring Modulith com detecção automática de pacotes `api/` via `ApplicationModuleDetectionStrategy`, foi necessário avaliar a compatibilidade entre Spring Boot e Spring Modulith. A tentativa de usar Spring Boot 4.x com Modulith 1.4.x resultou em `NoSuchMethodError` em runtime:
- `ConfigDataEnvironmentPostProcessor.applyTo()` existe com assinatura de **1 argumento** no Spring Boot 3.5.x, e foi **removido** no Spring Boot 4.x.
- Modulith 1.4.x é compilado contra o Spring Boot 3.5.x e invoca esse método com 4 argumentos — que tampouco existe no Spring Boot 4.x.
- Modulith 2.x (projetado para Spring Boot 4.x) estava disponível apenas como SNAPSHOT no momento desta decisão.

**Decisão:**
- Manter **Spring Boot 3.5.14** + **Spring Modulith 1.4.1**. Esta é a combinação testada e estável.
- Não usar Spring Boot 4.x até que Modulith 2.x seja GA (Generally Available).
- Flyway deve ser importado via `org.flywaydb:flyway-core` — o artefato `spring-boot-starter-flyway` só existe no Spring Boot 4.x e não no 3.5.x.

**Custo da decisão:** Nenhuma mudança nos arquivos Java foi necessária ao reverter para 3.5.14. A API de Spring Security, JWT e Resource Server é idêntica entre 3.5.x e 4.x.

---

## ADR 007: Detecção Automática de Módulos via QronisModuleDetectionStrategy
**Status:** Aceito
**Contexto:** O Spring Modulith precisa saber (a) onde estão os módulos e (b) o que cada módulo expõe publicamente. A abordagem padrão do Modulith trata cada subpacote direto do pacote raiz da aplicação como um módulo, o que causava o `shared/` ser tratado como módulo com regras de encapsulamento — tornando `ErrorResponseDTO` inacessível de fora.

**Decisão:**
- Implementar `QronisModuleDetectionStrategy` em `com.qronis.shared.config`.
- `getModuleBasePackages()`: procura um subpacote cujo nome termina em `.modules` e retorna **seus** subpacotes como módulos. O `shared/` nunca aparece como módulo — é código de infraestrutura da aplicação root.
- `detectNamedInterfaces()`: usa `NamedInterfaces.builder(basePackage).recursive().matching("api").build()` — qualquer subpacote chamado `api` dentro de um módulo é automaticamente elevado a `NamedInterface`. Zero anotações individuais nas classes.
- A estratégia é registrada em `application.yml`: `spring.modulith.detection-strategy: com.qronis.shared.config.QronisModuleDetectionStrategy`.
- O teste arquitetural usa `ApplicationModules.of(QronisApplication.class)` para garantir que o mesmo `application.yml` seja lido em runtime e em teste.

**Resultado:** Adicionar qualquer tipo ao pacote `modules.X.api` o torna automaticamente público. Não há `package-info.java` nem `@NamedInterface` necessários.

---

## ADR 008: Regra de Ouro do Cronômetro (Frontend)
**Status:** Aceito
**Contexto:** Ao construir o Live Tracker (ZenTimer), usar `setInterval` iterando uma variável de estado (`setSeconds(s => s+1)`) gera dessincronização severa devido a gargalos do event-loop do navegador (abas inativas, processamento pesado em outras páginas).
**Decisão:**
- O cronômetro visual é estritamente derivativo e "stateless" em relação à contagem temporal.
- A matemática adotada é ancorada em carimbo absoluto: `Date.now() - startTime` (onde `startTime` é o ISO Time real retornado pelo backend).
- O React apenas guarda o resultado do delta em segundos no estado para forçar o re-render, mas essa soma nunca é feita de forma recursiva a partir de si mesmo.

---

## ADR 009: Otimização de Espaçamentos Condicionais (Flex Gap vs Space-Y)
**Status:** Aceito
**Contexto:** Nos formulários de autenticação (Login/Register) deparamo-nos com componentes ocultos condicionalmente (mensagens de texto vermelho `{error && ...}`). O uso utilitário do Tailwind `space-y-*` forçava a injeção falha de top-margins baseada em seletores sibling `> * + *`, que não calculam a abstração do React.
**Decisão:**
- Formações de tela com caixas escondíveis/condicionais aboliram categoricamente a utilidade `space-y-*`.
- Adotou-se o modelo inviolável em container pai: `flex flex-col gap-*`.

---

## ADR 010: Criação In-Flow de Associações (Project Selector)
**Status:** Aceito
**Contexto:** Como forçar que uma Task pertença a um `Project`, se o usuário não tiver aquele determinado projeto criado ainda? Uma rota de escape para `/projetos/novo` assassinaria completamente o senso prático do Tracker.
**Decisão:**
- O filtro do `<ComboBox>` monitora a String livre.
- Se os resultados para o Array retornarem Length 0 e a query for válida, injecta-se um *Action Button* na lista para "Criar novo projeto [Query]".
- Isso perfura o fluxo, joga um `POST /api/projects` no backend e recupera o ID simultaneamente atrelando ao Timer.

---

## ADR 011: Travas de Viewport em Páginas de Setup (Auth)
**Status:** Aceito
**Contexto:** Nas páginas de Login e Register, em monitores com baixa resolução vertical, o uso de `min-h-screen` com flexbox e paddings internos extensos forçava o scroll — quebrando o "Zen Paradigm".
**Decisão:**
- Arquitetura "App-like Strict": telas de entrada herdam `h-screen overflow-hidden` na tag pai.
- Os paddings flexíveis centrais (`flex-1 min-h-0`) absorvem o espaço restante sem gerar scroll.

---

## ADR 012: Identificação Pragmática de Servidor Offline (Axios)
**Status:** Aceito
**Contexto:** O erro padrão capturado em falhas do backend disparava a lógica genérica de `|| "Credenciais inválidas"` porque o objeto `err.response` retornava nulo quando o backend não estava rodando.
**Decisão:**
- Adotar `if (!err.response && err.request)` para categorizar falhas de handshake como "Servidor indisponível" — separando erros de rede de erros de aplicação.

---

## ADR 013: Agregação de Horas via Native Query PostgreSQL
**Status:** Aceito
**Contexto:** A tela de detalhes do projeto exibe o total de horas investidas. Precisávamos calcular no banco, não no client-side.
**Decisão:**
- Native query no `TimeEntryRepository`: `SELECT COALESCE(SUM(EXTRACT(EPOCH FROM (te.end_time - te.start_time))), 0) FROM time_entry te WHERE te.project_id = CAST(:projectId AS uuid) AND te.created_by = CAST(:userId AS uuid) AND te.end_time IS NOT NULL`.
- Retorna `Long` (total em segundos). O `CAST(:param AS uuid)` é necessário para compatibilidade PostgreSQL + Spring Data JPA.
- **Cuidado:** A query nativa deve usar o nome exato da tabela conforme `@Table(name = "time_entry")` — nunca plurais inventados.

---

## ADR 014: Blindagem de UUID.fromString() nos Controllers
**Status:** Aceito
**Contexto:** O React Router pode despachar IDs inválidos (ex: `"undefined"`) durante transições de rota. `UUID.fromString("undefined")` lança `IllegalArgumentException` não-tratada → 500.
**Decisão:**
- Controllers que recebem `@PathVariable("id") String id` encapsulam `UUID.fromString(id)` em `try-catch(IllegalArgumentException)` → `ResponseStatusException(BAD_REQUEST)`.

---

## ADR 015: @RequestParam Requer `name` Explícito (Spring Boot / Gradle)
**Status:** Aceito
**Contexto:** `@RequestParam(required = false) String name` causava 400 em todas as chamadas porque o Gradle não preserva nomes de argumentos no bytecode sem a flag `-parameters`.
**Decisão:**
- **Regra inviolável:** Todo `@RequestParam`, `@PathVariable` e `@RequestHeader` deve declarar o atributo `name` explicitamente: `@RequestParam(name = "name", required = false) String name`.
- Nota: o `build.gradle.kts` já possui `-parameters` em `compileArgs` (adicionado posteriormente). A declaração explícita permanece como reforço defensivo.

---

## ADR 016: Vulnerabilidade N+1 no endpoint PATCH de TimeEntry (Conhecida)
**Status:** Adiado / Mapeado
**Contexto:** `TimeEntryService.patch()` usa `findById` (lazy). O MapStruct, ao converter para `TimeEntryResponseDTO`, precisa de `projectName`, o que dispara uma query implícita N+1.
**Tentativa de Correção:** Criado `findByIdAndCreatedByIdWithProject` com JOIN FETCH, mas a alteração quebrou vários mocks de testes unitários.
**Decisão Atual:** `git restore` para estado funcional. A N+1 é aceitável (1 projeto, não paginação em lote). Corrigir apenas quando a suíte E2E de Controller estiver madura.

---

## ADR 017: Plano de Evolução UI/UX em 3 Camadas (Grade de Projetos)
**Status:** Aceito
**Contexto:** Após a entrega funcional da Grade de Projetos, foi realizada análise profunda de UI/UX para elevar o frontend de "funcional" para "produto de primeira linha".
**Decisão:**
- **Camada 1 (Quick Wins):** Dialog de exclusão customizado, busca live com debounce, accent colors, coesão visual `rounded-full`, hover com elevação e stagger animation.
- **Camada 2 (Identidade Visual):** Toast notifications via `sonner`, skeleton loading, empty state humanizado, refinamento TopNav.
- **Camada 3 (Infraestrutura):** Lazy loading de rotas, Error Boundary global, acessibilidade (a11y), preparação i18n.
- **Filosofia:** Todas as decisões respeitam o DNA Zen/Minimalista do Qronis.

---

## ADR-FE-001: React Query como Camada de Data Fetching
**Status:** Aceito
**Contexto:** As páginas gerenciavam cada uma seu próprio estado de loading, error e paginação com `useEffect` + Axios direto. Isso produzia: requests sem AbortController (memory leak ao desmontar), zero cache entre navegações, erro silencioso swallowado em catch blocks, e duplicação de lógica de paginação.
**Decisão:**
- Adotar `@tanstack/react-query` como única camada de data fetching client-side.
- `QueryClient` configurado com `staleTime: 30s`, `retry: 1`. Timer ativo com `staleTime: 5s`.
- Cada domínio tem seus hooks em `src/hooks/` (`useProjects`, `useTimeEntries`, `useTimer`).
- Query keys hierárquicas por domínio (`projectKeys`, `timeEntryKeys`, `timerKeys`) permitem invalidação precisa.
- AbortController integrado via `signal` do React Query em todas as `queryFn` — eliminando o memory leak de P0.

---

## ADR-FE-002: Fonte de Verdade Única para Tipos TypeScript
**Status:** Aceito
**Contexto:** `Project`, `TimeEntry`, `PageResponse<T>` e `User` eram redefinidos em múltiplos arquivos (`Projects.tsx`, `History.tsx`, `TimeEntryRow.tsx`, `auth-context.tsx`, `ProjectSelector.tsx`). Divergências silenciosas entre as definições eram possíveis.
**Decisão:**
- Criar `src/lib/types.ts` como único source of truth para todas as interfaces de domínio.
- Arquivos consumidores importam com `import type { ... } from '@/lib/types'`.
- Tipos re-exportados onde necessário (`export type { TimeEntry }` em `TimeEntryRow.tsx`) para não quebrar consumers existentes.

---

## ADR-FE-003: Centralização de Lógica de Accent Colors
**Status:** Aceito
**Contexto:** `ACCENT_COLORS`, `accentColorFor()` e `accentBgFor()` estavam definidos identicamente em `Projects.tsx` e `TimeEntryRow.tsx`. Qualquer mudança na paleta exigia edição em dois lugares.
**Decisão:**
- Criar `src/lib/colors.ts` com as três exports. Todos os consumers importam dali.

---

## ADR-FE-004: useDeferredValue em vez de useEffect+setTimeout para Debounce
**Status:** Aceito
**Contexto:** `Projects.tsx` implementava debounce de busca com `useEffect(() => { const t = setTimeout(..., 400); return () => clearTimeout(t); }, [searchQuery])`. Isso cria um timer manual e um estado intermediário (`debouncedSearch`) que exige reset manual ao mudar de página.
**Decisão:**
- Substituir por `useDeferredValue(searchQuery)` — padrão React moderno que delega o debounce ao scheduler do React sem timers manuais.
- `isSearchPending = searchQuery !== deferredSearch` provê o indicador de loading sem estado adicional.

---

## ADR-FE-005: CSS group-hover em vez de useState para Hover em Listas
**Status:** Aceito
**Contexto:** `TimeEntryRow.tsx` usava `const [isHovered, setIsHovered] = useState(false)` com `onMouseEnter/Leave`. Em listas de 20 entradas, cada movimento de mouse disparava setState → re-render do componente inteiro (290 linhas). ~40 re-renders por scroll.
**Decisão:**
- Remover o estado completamente. Usar `className="group"` no container e `group-hover:opacity-100` nos filhos — comportamento idêntico, custo zero para o React.

---

## ADR-FE-006: ErrorBoundary Global
**Status:** Aceito
**Contexto:** Qualquer erro não tratado em um render React desmontava silenciosamente a árvore inteira, deixando o usuário com tela branca sem feedback.
**Decisão:**
- Criar `src/components/error/ErrorBoundary.tsx` (React class component, única forma de capturar erros de render).
- Envolve toda a app em `App.tsx` (fora de `BrowserRouter` para capturar inclusive erros de roteamento).
- Fallback: mensagem amigável em PT-BR + botão "Recarregar".

---

## ADR-FE-007: Extração de TimeEditDialog de TimeEntryRow
**Status:** Aceito
**Contexto:** `TimeEntryRow.tsx` tinha 290 linhas com três responsabilidades distintas: (1) renderização da linha, (2) edição de descrição inline com blur-save, (3) dialog de ajuste de horário com seus próprios estados.
**Decisão:**
- Extrair o dialog para `src/components/history/TimeEditDialog.tsx` — componente puro de apresentação que recebe props de estado e callbacks.
- `TimeEntryRow` passa `open`, `startTime`, `endTime`, `onSave`, `isSaving` via props. Zero estado interno de dialog em `TimeEntryRow`.
- Labels do dialog passam a ter `htmlFor` associado (`edit-start-time`, `edit-end-time`) — corrigindo gap de a11y.
