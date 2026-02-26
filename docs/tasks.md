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
- [x] Rotear o estado de Auth (Proteção de telas via React Router / Guarda de Rotas).
- [x] Codificar o App Shell (Layout Base): Adotar Navbar superior simples e limpa `[Logo] | [Timer] [Projetos] | [Perfil]`, abandonando sidebars complexas para focar no conteúdo central.

---

### Fase 5: Domínio de Negócios Visual (Front-end)
A mágica acontece (Operação de Fluxo Real). Foco na legibilidade e "Zen Mode".

- [ ] Construir o **Live Tracker UI (Zen Mode)**: O coração gravitacional da aplicação. Timer GIGANTE e centralizado na tela (foco em topografia moderna/bold) + Botões maciços de Play/Stop (Verde/Vermelho) + ComboBox simples (`shadcn/ui`) para selecionar o Projeto. Tudo sem distrações laterais.
- [ ] Elaborar a Grade de Projetos: Tela simples consumindo o endpoint de listagem/busca usando a base do componente Table (`shadcn/ui`).
- [ ] Estruturar Tela History (Abaixo do Timer): Uma grade infinita suportando *on-blur* para disparar instantaneamente `PATCH`. Combinação de inputs sem borda nas células para entregar a edição inline sem botões de "Salvar".
- [ ] Injetar Gráficos Analytics de produtividade atrelados a blocos Recharts interativos no Dashboard Master.
