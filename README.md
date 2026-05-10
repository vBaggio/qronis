# Qronis

Qronis é um SaaS de Time Tracker projetado para oferecer um fluxo sem atritos, permitindo o registro automático e fluido de tempo em projetos. A ferramenta foca no "Deep Work", minimizando interrupções com uma interface extremamente rápida e reativa.

## Tecnologias Utilizadas

### Backend
- Java 21 (LTS)
- Spring Boot **3.5.14** + Spring Modulith **1.4.1**
- PostgreSQL 16
- Gradle e Flyway (Migrations)
- Spring Security (OAuth2 Resource Server / JWT HS256)
- MapStruct + Lombok

### Frontend
- React 19 + TypeScript 5 (strict)
- Vite 7
- Tailwind CSS v4 + Shadcn UI
- React Router 7
- TanStack React Query 5 — cache, deduplicação e data fetching
- React Hook Form + Zod — formulários e validação
- Axios — HTTP client com interceptors JWT e auth expiry
- date-fns (pt-BR) — formatação de datas

## Arquitetura e Estrutura

O sistema opera em um modelo SPA (Single Page Application) acoplado a uma REST API totalmente stateless.

### Backend — Monólito Modular
O núcleo de serviços é um **Monólito Modular** validado em teste pelo Spring Modulith (`QronisArchitectureTest`).

Módulos em `src/main/java/com/qronis/modules/`:
- `identity` — usuários, tenants, provisão de identidade
- `project` — projetos e seus aggregates
- `tracker` — time entries, timers ativos, histórico

Regras de encapsulamento:
- Cada módulo expõe apenas o que está em seu subpacote `api/` (ex: `modules.tracker.api.TrackerFacade`).
- Comunicação entre módulos ocorre exclusivamente via Facades no pacote `api/`.
- O pacote `shared/` contém código transversal (não é um módulo Modulith — é acessível globalmente).

Outras regras de domínio:
- **Soberania do UTC:** datas persistidas e trafegadas exclusivamente em UTC.
- **Exclusividade de Timer:** apenas 1 timer ativo por usuário (garantido por Partial Unique Index no PostgreSQL).
- **Isolamento de Tenant:** projetos e registros isolados logicamente por tenant.
- **DTOs imutáveis:** Java Records com sufixo `DTO`, mapeados via MapStruct.
- **Global Exception Handler:** nenhum stacktrace exposto. Erros padronizados em `ErrorResponseDTO`.

### Frontend
- Interface mutável: elementos secundários desaparecem quando o cronômetro é iniciado.
- Atualizações de histórico via eventos "on blur" — sem botão de salvar explícito.
- Cronômetro calculado como `Date.now() - startTime` (UTC do servidor), imune a gargalos do event loop.

## Documentação

| Arquivo | Conteúdo |
|---|---|
| `docs/architecture.md` | Arquitetura técnica detalhada, Spring Modulith, pacotes, padrões |
| `docs/rules.md` | Regras invioláveis do projeto (backend, frontend, modularização) |
| `docs/memories.md` | ADRs — decisões arquiteturais e o porquê de cada uma |
| `docs/context.md` | Contexto do produto, regras de negócio, público-alvo |
| `docs/api.md` | Contrato da API REST (endpoints, payloads, respostas) |

## Execução Local

### Pré-requisitos
- Docker e Docker Compose
- Java 21+
- Node.js 22+

### 1. Banco de Dados
```bash
docker compose up -d
```

### 2. Backend (API)
```bash
./gradlew bootRun
```

As migrações Flyway sincronizam o schema automaticamente na inicialização.

### 3. Frontend (UI)
```bash
cd frontend
npm install
npm run dev
```

Frontend disponível em `http://localhost:5173`. Backend na porta `8080`.

### Testes Arquiteturais
```bash
./gradlew test --tests "com.qronis.QronisArchitectureTest"
```
