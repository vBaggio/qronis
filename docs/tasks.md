# Tarefas de Engenharia (Estado da Arte)

As Fases 1, 2 e 3 de fundação back-end e segurança foram dadas como estritamente entregues (100% testadas e blindadas). A prioridade agora foca na cobertura maciça de testes e na entrega da UI.

### Fase 1: Setup da Infraestrutura e Esqueletos
- [x] Criar arquivos `.gitignore` abrangentes para ignorar `node_modules`, `build`, `.gradle`, arquivos de configuração de IDE e `.env`.
- [x] Pasta /context no gitignore
- [x] Inicializar projeto Gradle para Spring Boot (Java 25, Web, Data JPA, Security, PostgreSQL, Validation, Flyway, MapStruct). Configurar o `annotationProcessor` do MapStruct.
- [x] Configurar `application.yml` com conexão ao banco na porta 5434, chaves JWT com fallback e `ddl-auto=validate`.
- [x] Inicializar projeto React com Vite + TypeScript na pasta `/frontend`.
- [x] Configurar Tailwind CSS e inicializar Shadcn UI no React.

### Fase 2: Banco de Dados, Segurança e Modelagem (Back-end)
- [x] Criar script inicial de migração do Flyway (`V1__create_initial_schema.sql`) para as tabelas: `tenant`, `users`, `tenant_user`, `project`, `time_entry`.
- [x] Criar entidades JPA mapeando as tabelas acima.
- [x] Implementar configuração de Segurança (Spring Security) com `SecurityFilterChain` separando rotas públicas e protegidas.
- [x] Implementar geração e validação de JWT (`NimbusJwtDecoder`).

### Fase 3: Core API e Validações (Back-end)
- [x] Criar DTOs usando **Java Records**.
- [x] Criar interfaces de mapeamento com **MapStruct** para converter Entity <-> Record DTO.
- [x] Configurar um `@RestControllerAdvice` para capturar e formatar exceções globais e erros do Jakarta Validation.
- [x] Implementar endpoints CRUD para `Project` (usando validações como `@NotBlank`).
- [x] Implementar endpoint para iniciar timer (`POST /api/time-entries/start`).
- [x] Implementar endpoint para parar timer (`PUT /api/time-entries/stop`).
- [x] Implementar endpoint de listagem de histórico e captura da tarefa ativa.
- [x] Implementar criação manual de time entry (`POST /api/time-entries`).
- [x] Implementar edição parcial inline (`PATCH /api/time-entries/{id}`).
- [x] Implementar exclusão de time entry (`DELETE /api/time-entries/{id}`).
- [x] Implementar listagem de entries por projeto (`GET /api/projects/{id}/time-entries`).
- [x] Atualizar Postman collection com os novos endpoints.

### Fase 3.5: Automação e Malha de Qualificação (Back-end)
Esta fase blinda regressões para futuros refactorings.

**Infraestrutura Core**
- [x] Configurar Testcontainers (PostgreSQL) garantindo ambiente Docker perfeitamente isolado nos testes de integração.
- [x] Ajustar Spring Profiles (`application-test.yml`) para anular dependência de hard-databases no workflow.
- [x] Implementar e sanear conflitos com as autoconfigurações do Spring Boot 4 (+ Flyway modules).

**Teste em Pilares**
- [x] Testes Unitários de Services via Mockito (blindar isolamentos de Tenant e Timer Activo Único).
- [x] Testes de Integração via `@DataJpaTest`/`@SpringBootTest` garantindo performance das queries de JOIN FETCH.
- [ ] Construir o teste supremo de fluxo (`QronisIntegrationTest`) abrangendo o flow real com WebMock: Cadastro -> Puxa Perfil (`/me`) -> Gera e gerencia Projetos -> Brinca com Timers (Live Action + Manual + Patchs).
- [ ] Extender os Controller Tests para todas as APIs injetando segurança e claims customizados via MockMvc.

---

### Fase 4: Autenticação e Layout (Front-end Pragmático MVP)
Base de interação humana focada em simplicidade e entrega rápida.

- [x] Instalar dependências base: Inicializar CLI do `shadcn/ui`, instalar Tailwind plugins e definir tema neutro (Zinc/Slate) com acentuação Emerald.
- [x] Arquitetar Axios Client isolado (`/src/lib/api.ts`) processando JWT Bearer e interceptors de expiração.
- [x] Engenhar Telas de Autenticação (`/auth/register` e `/auth/login`) usando os componentes Form e Input do `shadcn/ui`.
- [x] Engenhar **Landing Page** (`/`): Tela inicial limpa com introdução ao produto e call-to-actions claros para Autenticação.
- [x] Rotear o estado de Auth (Proteção de telas via React Router / Guarda de Rotas).
- [x] Codificar o App Shell (Layout Base): Adotar Navbar superior simples e limpa `[Logo] | [Timer] [Projetos] | [Perfil]`, abandonando sidebars complexas para focar no conteúdo central.

---

### Fase 5: Domínio de Negócios Visual (Front-end)
A mágica acontece (Operação de Fluxo Real). Foco na legibilidade e "Zen Mode".

- [x] Construir o **Live Tracker UI (Zen Mode)**: O coração gravitacional da aplicação. Timer GIGANTE e centralizado na tela (foco em topografia moderna/bold). Botões maciços de Play/Stop (Verde/Vermelho) + ComboBox simples (`shadcn/ui`) para selecionar o Projeto.
  - *Feature "Escape Hatch":* Quando *Idle* (parado), manter navegação (`TopNav`) visível para fuga. Quando *Active* (rodando), ocultar todo ruído visual da tela.
- [x] Elaborar a Grade de Projetos: Tela consumindo o endpoint de listagem paginada usando componente Table (`shadcn/ui`). Cores de "Accent" determinísticas por UUID do projeto. Dialog de criação inline e exclusão com confirmação.

#### Fase 5.1: Polimento da Grade de Projetos (Quick Wins)
- [x] Dialog de confirmação de exclusão customizado (substituir `window.confirm` por Dialog shadcn com ícone, nome do projeto em destaque e botão destructive).
- [x] Busca live com debounce 400ms (remover botão "Buscar", filtro via query param `?name=` no backend).
- [x] Accent colors via `border-l-4` na row (trocar dot + badge por borda lateral colorida, estilo Linear/Notion).
- [x] Coesão visual `rounded-full` nos inputs e botão "Novo Projeto" (herança do ZenTimer).
- [x] Hover row com elevação sutil (`shadow-sm` + `translate-y-[1px]`) e stagger animation (fade-in escalonado por row).

## 6. Live Tracker Mobile Polish
- [x] Reproject `ZenTimer.tsx` and `ProjectSelector.tsx` form controls to avoid the "boxy" stacked look on small screens.

## 7. Additional UX Refinements
- [x] Hide the global "Novo Projeto" button in `/projects` when the empty state CTA is visible.
- [x] Move row actions to a Dropdown Menu (`...` vertical) and remove the "Ações" table header.

#### Fase 5.2: Evolução de Identidade Visual e UX Premium (O Paradigma "Zen")
- [ ] Refatorar Navbar (`TopNav.tsx`) restringindo largura para `max-w-5xl` para unificar alinhamento visual com as páginas.
- [ ] Header de `/projects`: Unificar Título, Busca ("pill-shape") e Botão "Novo Projeto" (Estilo Ghost/Soft) num layout orgânico e responsivo.
- [ ] Refinar tipografia ("Projetos" com `font-semibold tracking-tight` e "TableHeader" em *sentence case* discreto).
- [ ] Redesenhar indicador de cor da tabela: remover `border-l-4` bruta e usar *Pill/Badge* circular sutil interno.
- [ ] Minimalismo Tabela: Excluir coluna "Criado por" e aumentar `padding-y` das linhas (respiro / luxo visual).
- [ ] Empty State Ativo: Injetar botão "Novo Projeto" centralizado na tela vazia (Lei de Fitts).
- [ ] Reestruturar GRID/FLEX Mobile do `/tracker`: impedir o empilhamento em caixotes ("boxy") do selector de projetos e campos adjacentes.
- [ ] Integrar Toast Notifications (`sonner`) para feedback não-intrusivo de criação/exclusão/erros.
- [ ] Skeleton Loading na Table (substituir spinner por rows fantasma para melhorar CLS).
- [ ] Tipografia premium: avaliar adição de fonte Inter via Google Fonts.
- [ ] Dark Mode Toggle na TopNav (Sun/Moon) com persistência em `localStorage`.

#### Fase 5.3: Infraestrutura de Qualidade Frontend
- [ ] Lazy Loading de rotas via `React.lazy()` + `Suspense` no `App.tsx`.
- [ ] Error Boundary global com fallback visual amigável.
- [ ] Acessibilidade (a11y): `focus-visible` rings, `aria-labels` em botões de ação, keyboard navigation.
- [ ] Preparação i18n: extrair strings hardcoded para constantes (futuro `react-i18next`).

---

- [ ] Estruturar Tela History: Uma grade infinita suportando *on-blur* para disparar instantaneamente `PATCH`. Combinação de inputs sem borda nas células para entregar a edição inline.
  - *Feature Formatação Dinâmica:* Duração inteligente formatando `< 1m` (segundos), `< 1h` (min/seg), `> 1h` (horas/min) para limpar a tabela ao máximo.
- [ ] Injetar Gráficos Analytics de produtividade atrelados a blocos Recharts interativos no Dashboard Master.
