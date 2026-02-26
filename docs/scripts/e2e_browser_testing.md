# Testes de Interface (E2E) com AI: Guia de Otimização

Esta documentação provê as melhores práticas para que ferramentas automatizadas e agentes de IA testem a interface web e fluxos como o de Autenticação (Login) eficientemente, economizando **energia/tokens**, diminuindo lentidão e evitando falsos negativos e falhas de parser no DOM.

## 0. Pre-Flight Checks (Verificação de Status)

Antes de instanciar o browser subagent, é uma regra mandante que o agente de IA execute o script de *pre-flight* localmente. Isso evita gastar tokens com subagentes falhando por recusas de conexão (`ECONNREFUSED`).

**Scripts prontos para automação (avaliam portas 5434, 8080 e 5173 e retornam um status verde SUCCESS):**
- **Windows (PowerShell):** `pwsh -File docs/scripts/preflight.ps1`
- **Linux/macOS:** `bash docs/scripts/preflight.sh`

## 1. Inicialização Resiliente de Serviços

Para que um Agente AI prepare os containers e portas sem sofrer com *tty/interactive hangups* (ex.: o terminal bloqueia tentando mostrar QRCodes, prompts ou esperando input para logs):

- **PostgreSQL:** Sempre inicie no modo *detached*: `docker-compose up -d postgres`.
- **Spring Boot Backend:** Rodar normalmente com `./gradlew bootRun` em background.
- **React/Vite Frontend:** ⚠️ É mandatório que execuções não interativas ou background tasks do agente declarem a variável `CI=true`. O Vite, por padrão na v7, suspende caso perca o TTY, travando testes em processo paralelo.
  - Windows PowerShell: `$env:CI="true"; npm run dev`
  - macOS/Linux: `CI=true npm run dev`

## 2. Abordagem de Economia de Tokens no Browser Subagent

A maior "fuga" (vazamento) de tokens e latência durante testes de web-apps ocorre porque o sub-agente AI tipicamente aciona o método que extrai a árvore do DOM inteira para entender a página e calcular onde clicar.

**Estratégia Obrigatória para Testes:**
Ao invés de pedir: *"Entre em /login e logue"*, instrua o seu *Task Prompt* no subagente a não buscar a página, injetando diretamente as credenciais utilizando coordenadas de Seletores CSS conhecidos.

### Seletores Relevantes (Página de Login)
- **Email:** `input[type="email"]`
- **Senha:** `input[type="password"]`
- **Submit:** `button[type="submit"]`

### Modelo Otimizado (Copie/Cole como Prompt do Subagente)

Em todo novo teste envolvendo login via UI, utilize este prompt:

> "Navegue para http://localhost:5173/. Não é necessário extrair o DOM inteiro da página para explorar botões. Encontre o elemento 'input[type=\"email\"]', foque nele e digite o email. Encontre 'input[type=\"password\"]' e digite a senha correspondente fornecida na task original. Clique no 'button[type=\"submit\"]'. Aguarde o redirecionamento; verifique o sucesso simplesmente conferindo se a URL resultante é '/tracker'. Retorne o print ou status ao usuário."

Seguindo as regras estruturadas acima, validaremos features E2E gastando 1/3 do budget de Tokens standard.
