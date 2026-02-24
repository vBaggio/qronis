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

### Fase 4: Autenticação e Layout (Front-end)
Base de interação humana.

- [ ] Arquitetar Axios ou Fetch Client isolado (`/src/lib/api.ts`) que processe transparentemente a expiração e injeção do cabeçalho JWT Bearer.
- [ ] Engenhar Componente Form para `/auth/register` (Nome, Companhia, Email, Senha).
- [ ] Engenhar Componente Form para `/auth/login`.
- [ ] Codificar App Shell Navbar e Sidebar inspirados pelo visual limpo de dashboards financeiras e utilitários modernos (ex: Conta Azul).
- [ ] Rotear o estado de Auth (Proteção de telas e Redireções inteligentes).

---

### Fase 5: Domínio de Negócios Visual (Front-end)
A mágica acontece (Operação de Fluxo Real).

- [ ] Elaborar a página CRUD e Grade de Apresentação visual para listagem inteligente e busca em Projetos da Workspace (Aproveitar `size=20&sort=createdAt`).
- [ ] Construir o coração gravitacional: O **Live Tracker UI (Zen Mode)**. Construir com layout fixed no top ou bottom screen, contendo select para escolher projeto via autocomplete, descritivo, calculo manual matemático rodando fora do State React, e um botão massivo de Play/Stop de reatividade absoluta.
- [ ] Estruturar Tela History. Uma grade infinita que suporte on-blur (foco fora) para disparar instantaneamente na rede requisições `PATCH` visando inputs de Descrições/Projetos e Modais rápidos para editar StartTime e EndTime.
- [ ] Injetar Gráficos Analytics de produtividade atrelados a blocos Recharts interativos no Dashboard Master.
