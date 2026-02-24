# Tarefas de Engenharia (Estado da Arte)

As Fases 1, 2 e 3 de fundação back-end e segurança foram dadas como estritamente entregues (100% testadas e blindadas). A prioridade agora foca na cobertura maciça de testes e na entrega da UI.

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
