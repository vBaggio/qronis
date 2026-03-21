# Qronis

Qronis é um SaaS de Time Tracker projetado para oferecer um fluxo sem atritos, permitindo o registro automático e fluido de tempo em projetos. A ferramenta foca no "Deep Work", minimizando interrupções com uma interface extremamente rápida e reativa.

## Tecnologias Utilizadas

### Frontend
- React 19
- TypeScript
- Vite
- Tailwind CSS v4
- Shadcn UI

### Backend
- Java 21 (LTS)
- Spring Boot 4.0.3
- PostgreSQL 16
- Gradle e Flyway (Migrations)
- Spring Security (OAuth2 JWT)

## Arquitetura e Estrutura

O sistema opera em um modelo SPA (Single Page Application) acoplado a uma REST API totalmente stateless.

### Frontend
A interface é guiada pelo minimalismo e redução de carga cognitiva:
- Interface mutável: elementos secundários desaparecem quando o cronômetro é iniciado.
- As atualizações de histórico e edição de tarefas ocorrem em tempo real via eventos "on blur", sem a necessidade de botões de salvamento explícitos.
- O cronômetro do Tracker não utiliza estados renderizados em massa (como setSeconds). A diferença é calculada nativamente entre a data UTC inicial do servidor e o relógio local do navegador, evitando problemas com o event loop.

### Backend
O núcleo de serviços segue regras de domínio imutáveis:
- Soberania do UTC: todas as datas e registros de tempo são calculados, transferidos e persistidos estritamente em fuso UTC.
- Exclusividade de Timer: o sistema valida e permite apenas uma tarefa em andamento por usuário de cada vez.
- Isolamento de Tenant: todos os projetos e registros são agrupados com isolamento lógico de Tenant, mesmo com infraestrutura single-database.
- Performance e Modelagem: mapeamentos são protegidos contra problemas N+1 usando JOIN FETCH explicitamente. A comunicação ocorre exclusivamente através de Java Records imutáveis convertidos com MapStruct.
- Global Exception Handler: nenhum stacktrace ou erro nativo é exposto. Falhas são uniformizadas através de um Data Transfer Object de erro padrão em JSON.

## Instruções para Execução Local

### Pré-requisitos
- Docker e Docker Compose
- Java 21+ instalado
- Node.js 22 ou superior

### 1. Iniciar Banco de Dados
A infraestrutura local de dados é suportada via container PostgreSQL. Na raiz do projeto, execute:
```bash
docker-compose up -d
```

### 2. Iniciar Backend (API)
Ainda na raiz do projeto, inicie o servidor Spring Boot. As migrações Flyway irão sincronizar o schema automaticamente durante a inicialização:
```bash
./gradlew bootRun
```

### 3. Iniciar Frontend (UI)
Abra um novo terminal acessando a pasta específica do frontend para instalar dependências e rodar o servidor de desenvolvimento:
```bash
cd frontend
npm install
npm run dev
```

Acesso da aplicação frontend estará disponível por padrão na porta orientada pelo Vite (geralmente localizável em `http://localhost:5173`). O backend estará operando e aceitando rotas de integração primárias.
