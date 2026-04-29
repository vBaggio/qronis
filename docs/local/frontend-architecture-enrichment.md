# Frontend Architecture Enrichment — Contexto, Diagnóstico e Progresso

**Projeto:** Qronis Frontend  
**Branch:** `refactor/frontend-architecture-enrichment`  
**Iniciado em:** 2026-04-29  
**Status atual:** 🔵 Em andamento — Fase 4 (Refatorar Páginas)

---

## 1. Contexto e Motivação

O frontend do Qronis é uma SPA React 19 com TypeScript estrito, Tailwind CSS v4, shadcn/ui, Axios e React Router 7. A base visual e a filosofia Zen (timer centralizado, UI que desaparece durante foco profundo) estão bem implementadas.

À medida que o codebase cresceu, surgiram os problemas clássicos de escala:

- **Componentes god** acumulando responsabilidades (até 459 linhas)
- **Tipos duplicados** espalhados em múltiplos arquivos sem fonte de verdade
- **Data fetching sem cache** — cada navegação refaz as mesmas requests
- **Memory leaks latentes** — requests sem AbortController que completam após unmount
- **A11y gaps** — botões de ícone sem aria-label, inputs sem label, divs clicáveis para navegação
- **Re-renders desnecessários** — `isHovered` via `useState` em componentes de lista

Esta refatoração resolve todos esses pontos sem mudar a UX ou o design visual. O objetivo é código mais seguro, testável e escalável.

---

## 2. Stack do Frontend

| Tecnologia | Versão | Papel |
|---|---|---|
| React | 19.2.0 | Framework UI |
| TypeScript | 5.9.3 | Type safety (strict mode) |
| Vite | 7.3.1 | Build tool |
| Tailwind CSS | 4.2.0 | Styling |
| shadcn/ui | — | Componentes UI (Radix primitives) |
| React Router | 7.13.1 | Roteamento |
| Axios | 1.13.5 | HTTP client |
| React Hook Form | 7.71.2 | Formulários |
| Zod | 4.3.6 | Validação de schema |
| date-fns | 4.1.0 | Formatação de datas (pt-BR) |
| **@tanstack/react-query** | **a instalar** | **Cache e data fetching** |

---

## 3. Diagnóstico Detalhado

### 3.1 Problemas Críticos (P0 — Memory Leaks / Bugs)

#### P0-A — Requests sem AbortController
**Arquivos afetados:** `History.tsx`, `Projects.tsx`, `ZenTimer.tsx`, `ProjectSelector.tsx`

Quando o usuário navega para outra rota enquanto uma request está em voo, a Promise resolve e tenta chamar `setState` em um componente já desmontado. O React 19 suprime o warning mas o comportamento existe.

```ts
// Exemplo do problema em History.tsx:27
const fetchHistory = async (pageNumber: number, append = false) => {
  // Se o componente desmontar aqui...
  const res = await api.get(`/time-entries?...`);
  setEntries(res.data.content); // ...isso ainda executa
};
```

**Solução:** AbortController por `useEffect`, ou React Query (que gerencia isso internamente).

#### P0-B — Re-renders de hover via useState
**Arquivo:** `TimeEntryRow.tsx:71`

```ts
const [isHovered, setIsHovered] = useState(false);
// onMouseEnter={() => setIsHovered(true)}
// onMouseLeave={() => setIsHovered(false)}
```

Com uma lista de 20 entradas, cada movimento de mouse dispara 1 setState → 1 re-render de todo o componente de 290 linhas. Solução: CSS `group-hover:` (já usado em outros lugares do projeto).

#### P0-C — Código corrompido
**Arquivo:** `Projects.tsx:146`

```ts
setOpen(false);ÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍÍ
setError('Não foi possível criar o projeto. Tente novamente.');
```

Caracteres unicode inválidos após `setOpen(false)`. O `catch` existe mas tem código morto antes do `setError`. Precisa de correção.

---

### 3.2 Problemas de Arquitetura (P1)

#### P1-A — Tipos duplicados sem fonte de verdade
As interfaces `Project`, `TimeEntry`, `PageResponse`, `User` são redefinidas em múltiplos arquivos:

| Interface | Definida em |
|---|---|
| `Project` | `Projects.tsx`, `ProjectDetails.tsx`, `ProjectSelector.tsx` |
| `TimeEntry` | `TimeEntryRow.tsx`, `History.tsx` |
| `PageResponse<T>` / `Page<T>` | `Projects.tsx`, `History.tsx`, `ProjectDetails.tsx` |
| `User` | `auth-context.tsx` |

Solução: `src/lib/types.ts` como único source of truth.

#### P1-B — Lógica de cor duplicada
`ACCENT_COLORS` e `accentColorFor()` são definidos identicamente em `Projects.tsx:59-73` e `TimeEntryRow.tsx:34-46`. Solução: `src/lib/colors.ts`.

#### P1-C — Componentes god

| Arquivo | Linhas | Responsabilidades acumuladas |
|---|---|---|
| `Projects.tsx` | 459 | lista, busca com debounce, paginação, dialog de criação, dialog de exclusão, formatação de data |
| `ProjectDetails.tsx` | 313 | header, lista de entradas, dialog de deleção, modal de criação, sorting, edição inline |
| `TimeEntryRow.tsx` | 290 | exibição, edição de descrição inline (blur-save), dialog de ajuste de horário |

#### P1-D — Sem camada de cache
Toda navegação refaz as mesmas requests. Exemplo: abrir `/projects`, voltar, abrir de novo = 2 requests GET /projects idênticas em ~5 segundos. React Query resolve com `staleTime` configurável.

#### P1-E — Error handling silencioso

```ts
// Projects.tsx:250 — usuario não sabe que falhou
catch { setProjects([]); }

// History.tsx:43 — idem
catch (error) { console.error('Failed to fetch history:', error); }
```

Nenhum dos dois mostra feedback ao usuário.

---

### 3.3 Problemas de A11y / UX (P2) — Web Interface Guidelines

**Baseado na skill `web-design-guidelines` (Vercel Labs):**

| Arquivo | Linha | Problema | Regra |
|---|---|---|---|
| `ZenTimer.tsx` | 151 | Stop button (Square icon) sem `aria-label` | Icon-only buttons need aria-label |
| `ZenTimer.tsx` | 131 | placeholder sem `…` no final | Loading states end with `…` |
| `Projects.tsx` | 362 | `<div onClick>` para navegação | `<button>` for actions, not `<div onClick>` |
| `Projects.tsx` | 158 | Ícone `<Plus>` decorativo sem `aria-hidden="true"` | Decorative icons need aria-hidden |
| `TimeEntryRow.tsx` | 203-210 | `<input>` de descrição sem label associado | Form controls need label or aria-label |
| `TimeEntryRow.tsx` | 252-268 | `<label>` no dialog sem `htmlFor` associado | Labels clickable via htmlFor |
| `History.tsx` | geral | Sem `aria-live="polite"` na região da lista | Async updates need aria-live |

---

### 3.4 Problemas de Performance (P3) — Vercel React Best Practices

| Regra | Arquivo | Descrição |
|---|---|---|
| `rerender-no-inline-components` | `Projects.tsx` | `formatDate()` recriada a cada render |
| `rerender-use-ref-transient-values` | `TimeEntryRow.tsx` | `isHovered` via useState vs ref/CSS |
| `js-cache-function-results` | `Projects.tsx`, `TimeEntryRow.tsx` | `accentColorFor()` recalculada a cada render |
| `rerender-derived-state-no-effect` | `Projects.tsx:229-235` | debounce via `useEffect+setTimeout` → `useDeferredValue` |
| `client-swr-dedup` | todos os pages | sem deduplica­ção de requests |
| `rendering-usetransition-loading` | `Projects.tsx` | `setLoading(true)` manual → `useTransition` |

---

## 4. Plano de Ação

### Fase 1 — Fundação: Tipos, Cores, Constantes
**Objetivo:** Eliminar duplicação sem mudar comportamento. Zero risco de regressão.

- [x] **1.1** Criar `src/lib/types.ts` — centralizar `Project`, `TimeEntry`, `PageResponse<T>`, `User`
- [x] **1.2** Criar `src/lib/colors.ts` — `ACCENT_COLORS`, `accentColorFor()`, `accentBgFor()`
- [x] **1.3** Criar `src/lib/constants.ts` — `PAGE_SIZE`, `MESSAGES`
- [x] **1.4** Atualizar todos os imports nos arquivos consumidores
- [x] **1.5** Corrigir código corrompido em `Projects.tsx` (catch block ausente + caracteres inválidos)
- [x] **1.6** Mover `formatDate` para fora do componente (evitar recriação a cada render)

**Gate:** `npm run build` sem erros TypeScript.

---

### Fase 2 — Instalar e Configurar React Query
**Objetivo:** Adicionar a infraestrutura de cache antes de refatorar os componentes.

- [x] **2.1** `npm install @tanstack/react-query @tanstack/react-query-devtools`
- [x] **2.2** Configurar `QueryClient` com `staleTime: 30_000`, `retry: 1`
- [x] **2.3** Envolver `<App>` com `<QueryClientProvider>` em `src/main.tsx`
- [x] **2.4** Adicionar `<ReactQueryDevtools>` (visível apenas em dev)

**Gate:** App continua funcionando identicamente (nenhum componente usa React Query ainda).

---

### Fase 3 — Custom Hooks com React Query
**Objetivo:** Criar a camada de data fetching reutilizável.

- [x] **3.1** Criar `src/hooks/useProjects.ts`
  - `useProjects({ page, search })` → `useQuery` paginado
  - `useProject(id)`, `useProjectSummary(id)` → `useQuery`
  - `useCreateProject()`, `useUpdateProject(id)`, `useDeleteProject()` → `useMutation`

- [x] **3.2** Criar `src/hooks/useTimeEntries.ts`
  - `useTimeEntries({ projectId?, sort? })` → `useInfiniteQuery`
  - `usePatchTimeEntry()`, `useDeleteTimeEntry()` → `useMutation` + invalidate

- [x] **3.3** Criar `src/hooks/useTimer.ts`
  - `useActiveTimer()` → `useQuery` com `staleTime: 5s` e `retry: false`
  - `useStartTimer()` → `useMutation` + `setQueryData` otimista
  - `useStopTimer()` → `useMutation` + invalidate timeEntries

**Gate:** Hooks criados, importáveis, TypeScript compila.

---

### Fase 4 — Refatorar Páginas
**Objetivo:** Substituir fetch manual pelos hooks; corrigir error handling e a11y nas páginas.

- [ ] **4.1** `ZenTimer.tsx`
  - Substituir `checkActiveTimer` + estados manuais por `useActiveTimer()`
  - Substituir `handleStart/handleStop` por `useStartTimer/useStopTimer`
  - Corrigir: `aria-label="Parar timer"` no Stop button
  - Corrigir: placeholder com `…`

- [ ] **4.2** `Projects.tsx`
  - Substituir fetch manual por `useProjects({ page, search: debouncedSearch })`
  - Substituir `useEffect+setTimeout` por `useDeferredValue(searchQuery)`
  - Substituir `handleDeleteConfirm` por `useDeleteProject()`
  - Corrigir: `<div onClick>` → `<button>` com `role` e `onKeyDown`
  - Corrigir: mostrar erro ao usuário (não silencioso)
  - Mover `formatDate` para fora do componente

- [ ] **4.3** `History.tsx`
  - Substituir `fetchHistory` por `useTimeEntries` com `useInfiniteQuery`
  - Adicionar `aria-live="polite"` na região da lista
  - Mostrar erro ao usuário

- [ ] **4.4** `ProjectDetails.tsx`
  - Substituir fetches por `useProject(id)` + `useTimeEntries({ projectId: id })`
  - Mesmas correções de error handling

**Gate:** Todas as rotas funcionam, React Query DevTools mostra cache hits ao navegar de volta.

---

### Fase 5 — Componentes: Extrair, Corrigir A11y
**Objetivo:** Quebrar componentes god; eliminar re-renders desnecessários.

- [ ] **5.1** `TimeEntryRow.tsx`
  - Substituir `useState(isHovered)` por CSS `group-hover:` (eliminar 40 re-renders por hover)
  - Adicionar `aria-label="Descrição do registro"` no input inline
  - Adicionar `htmlFor` nos `<label>` do dialog de horário
  - Extrair `<TimeEditDialog>` → `src/components/history/TimeEditDialog.tsx`

- [ ] **5.2** Criar `src/components/error/ErrorBoundary.tsx`
  - Componente class `ErrorBoundary` com fallback UI
  - Envolver em `App.tsx` → `<ErrorBoundary><AuthProvider>...</AuthProvider></ErrorBoundary>`

- [ ] **5.3** `ProjectSelector.tsx`
  - Avaliar: extrair variante `<ProjectSelectorCompact>` em vez de `size="compact"` prop
  - Ou usar CVA (class-variance-authority, já disponível via shadcn)

**Gate:** Lighthouse a11y score melhora; re-renders de hover eliminados (verificar com React DevTools Profiler).

---

### Fase 6 — Limpeza Final
**Objetivo:** Remover resíduos; garantir consistência total.

- [ ] **6.1** Remover `import axios from 'axios'` direto de `ZenTimer.tsx` (só usar `api`)
- [ ] **6.2** Garantir `catch (err: unknown)` em todos os handlers (`TimeEntryModal.tsx` usa `any`)
- [ ] **6.3** Verificar inputs de auth: `autocomplete="email"`, `autocomplete="current-password"`
- [ ] **6.4** Adicionar `aria-hidden="true"` em ícones decorativos onde faltam
- [ ] **6.5** Remover código morto identificado no diagnóstico

**Gate final:** `npm run build` limpo + auditoria manual em todas as rotas.

---

## 5. Estrutura de Arquivos Resultante

```
src/
├── lib/
│   ├── api.ts              (existente — sem alteração)
│   ├── auth-context.tsx    (existente — remover tipo User local)
│   ├── time-utils.ts       (existente — sem alteração)
│   ├── utils.ts            (existente — sem alteração)
│   ├── types.ts            ← NOVO: fonte de verdade para interfaces
│   ├── colors.ts           ← NOVO: ACCENT_COLORS, accentColorFor, accentBgFor
│   └── constants.ts        ← NOVO: PAGE_SIZE, MESSAGES
│
├── hooks/
│   ├── useProjects.ts      ← NOVO
│   ├── useTimeEntries.ts   ← NOVO
│   └── useTimer.ts         ← NOVO
│
├── components/
│   ├── error/
│   │   └── ErrorBoundary.tsx   ← NOVO
│   ├── history/
│   │   ├── TimeEntryList.tsx   (existente)
│   │   ├── TimeEntryRow.tsx    (refatorado — remove isHovered state, a11y)
│   │   └── TimeEditDialog.tsx  ← NOVO (extraído de TimeEntryRow)
│   ├── layout/             (sem alteração)
│   ├── tracker/            (sem alteração estrutural)
│   └── ui/                 (sem alteração)
│
└── pages/
    ├── tracker/ZenTimer.tsx    (refatorado)
    ├── projects/Projects.tsx   (refatorado)
    ├── projects/ProjectDetails.tsx (refatorado)
    └── history/History.tsx     (refatorado)
```

---

## 6. Decisões Arquiteturais

| ADR | Decisão | Motivo |
|---|---|---|
| ADR-FE-001 | Adotar React Query para data fetching | Cache automático, deduplica­ção, invalidação pós-mutação, AbortController interno |
| ADR-FE-002 | `src/lib/types.ts` como único source of truth para interfaces | Eliminar divergências silenciosas entre arquivos |
| ADR-FE-003 | `src/lib/colors.ts` para lógica de accent colors | DRY — mesma função em 2 arquivos |
| ADR-FE-004 | `useDeferredValue` em vez de `useEffect+setTimeout` para debounce | Padrão React moderno, sem timer manual |
| ADR-FE-005 | CSS `group-hover:` em vez de `useState(isHovered)` | Eliminar re-renders desnecessários em listas |
| ADR-FE-006 | ErrorBoundary envolvendo `<AuthProvider>` | Evitar crash total da app por erro não tratado |
| ADR-FE-007 | Extrair `<TimeEditDialog>` de `TimeEntryRow` | Single Responsibility — componente de 290L com 3 responsabilidades |

---

## 7. Progresso

### ✅ Concluído
- **Fase 1 — Fundação** (commit `15a5a72`)
  - `src/lib/types.ts` criado — fonte de verdade para todas as interfaces
  - `src/lib/colors.ts` criado — `accentColorFor`, `accentBgFor`, `ACCENT_COLORS`
  - `src/lib/constants.ts` criado — `PAGE_SIZE`, `MESSAGES`
  - Todos os consumers atualizados: `Projects.tsx`, `History.tsx`, `ProjectDetails.tsx`, `TimeEntryRow.tsx`, `ProjectSelector.tsx`, `auth-context.tsx`
  - Bug crítico corrigido: `Projects.tsx` handleCreate com código corrompido e catch ausente
  - `formatDate` movida para escopo de módulo (fora do componente)
  - Gate: `npm run build` — ✅ 0 erros TypeScript

- **Fase 2 — React Query** (commit `35e14a1`)
  - `@tanstack/react-query` + `@tanstack/react-query-devtools` instalados
  - `QueryClient` configurado: `staleTime: 30s`, `retry: 1`
  - `QueryClientProvider` envolvendo toda a app em `main.tsx`
  - `ReactQueryDevtools` disponível em dev (botão flutuante no canto)
  - Gate: `npm run build` — ✅ 0 erros TypeScript

- **Fase 3 — Custom Hooks** (commit `5a5f07b`)
  - `src/hooks/useProjects.ts`: 6 hooks (list, detail, summary, create, update, delete)
  - `src/hooks/useTimeEntries.ts`: 3 hooks (infinite list, patch, delete)
  - `src/hooks/useTimer.ts`: 3 hooks (active, start, stop)
  - AbortController integrado via `signal` do React Query em todas as `queryFn`
  - Query keys estruturados hierarquicamente por domínio para invalidação precisa
  - Gate: `npm run build` — ✅ 0 erros TypeScript

### 🔵 Em Andamento
- Fase 4 — Refatorar Páginas

### ⏳ Pendente
- Fase 2 — React Query
- Fase 3 — Custom Hooks
- Fase 4 — Refatorar Páginas
- Fase 5 — Componentes e A11y
- Fase 6 — Limpeza Final

---

## 8. Como Usar Este Documento (Para Agentes de IA)

Este arquivo é o **contexto primário** para qualquer agente que continuar esta refatoração.

**Fluxo esperado:**
1. Ler este arquivo completamente antes de qualquer ação
2. Identificar a fase atual na seção "Progresso"
3. Executar apenas as tasks da fase atual (não pular fases)
4. Atualizar os checkboxes `[ ]` → `[x]` conforme cada task é completada
5. Atualizar a seção "Progresso" ao completar uma fase inteira
6. Registrar qualquer decisão nova na seção "Decisões Arquiteturais" (ADR-FE-NNN)

**Regras invioláveis do frontend (de `docs/rules.md`):**
- Timer calculado como `Date.now() - startTime` — NUNCA incrementando estado via interval
- Datas armazenadas/transmitidas em UTC; conversão só na camada de exibição
- Design tokens: emerald primário, zinc neutro, `rounded-full` nos botões principais
- Zen UI: elementos secundários colapsam quando timer ativo

**Arquivos de referência:**
- `docs/context.md` — visão do produto e regras de negócio
- `docs/rules.md` — regras invioláveis de frontend e backend
- `docs/api.md` — contrato REST completo com exemplos de payload
- `docs/architecture.md` — estrutura do backend (módulos, facades, boundaries)
