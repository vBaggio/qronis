# Verificar o Status dos Serviços (Qronis)

Se você precisa saber quais partes da aplicação estão rodando atualmente ou identificar o motivo de uma porta estar ocupada, siga os comandos abaixo de acordo com seu ambiente.

## 1. Status do Banco de Dados (Docker)

Para checar se o PostgreSQL está executando no Docker:

```bash
docker ps
```
*Procure pela imagem `postgres:16-alpine` na lista, o status deve indicar "Up".*

## 2. Status do Backend e Frontend

O backend (Spring Boot / Java) e o frontend (Vite / Node.js) rodam como processos nativos no seu sistema operacional.

### No Windows (PowerShell/CMD):

Para listar os processos do **Java** (Backend) e **Node** (Frontend) ativos:
```powershell
Get-Process java, node -ErrorAction SilentlyContinue
```
Se o comando retornar uma tabela, significa que os serviços estão em execução.

Para descobrir o que está rodando em uma porta específica (ex: qual serviço travou a 8080):
```powershell
netstat -ano | findstr :8080
```
*O último número na linha será o PID (Process ID) que está segurando a porta. Você pode encerá-lo no Gerenciador de Tarefas ou com `Stop-Process -Id <PID>`.*

### No Linux/macOS:

Para listar os processos na memória:
```bash
ps aux | grep -E "java|node"
```

Para descobrir o processo atrelado a uma porta específica (ex: 8080 ou 5173):
```bash
lsof -i :8080
# ou
lsof -i :5173
```
*O comando mostrará o nome e o PID do serviço rodando nas portas da aplicação.*

## 3. Verificação Automatizada (Agentes de IA e Scripts)

Se você é um agente de Inteligência Artificial ou está automatizando o fluxo CI/CD, evitar comandos de leitura interativos (`netstat` complexo ou leitura longa de processos) economiza tokens e tempo. 

Em vez de listar processos manualmente, execute os **scripts de Pre-flight** localizados na pasta `docs/scripts/` para validar a disponibilidade do Banco (5434), API (8080) e Frontend (5173).

👉 **Windows (PowerShell):** `pwsh -File docs/scripts/preflight.ps1`
👉 **Linux/macOS (Bash):** `bash docs/scripts/preflight.sh`

*(O script reportará `SUCCESS` se todos os serviços estiverem operacionais, ou listará a porta que estiver offline para depuração imediata).*
