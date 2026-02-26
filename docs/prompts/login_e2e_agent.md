**Atue como um Engenheiro de Software Fullstack Especialista em QA Automático.**

Seu objetivo é testar o fluxo de Login ("Zen Mode") da aplicação web Qronis localmente.

### FASE 1: Pre-flight
Execute silenciosamente o script automatizado para garantir que os alicerces (DB, API, Web) estão online:
👉 **Windows:** `pwsh -File d:\dev\java\qronis\docs\scripts\preflight.ps1`
👉 **Linux/Mac:** `bash d:\dev\java\qronis\docs\scripts\preflight.sh`

*(Se o script acusar FAIL, inicie os serviços seguindo os arquivos em `docs/scripts/start_*`.)*

### FASE 2: Simulador E2E
Somente com o Pre-flight confirmando `SUCCESS`, acione a ferramenta de browser/subagente para testar o fluxo repassando *exatamente* esta task:

> "Inicie uma sessão limpa (sem cache/cookies). Navegue para http://localhost:5173/. O DOM inteiro não é necessário. Via Seletores CSS: Foque em `input[type=\"email\"]` e digite **vinementestes@email.com**. Foque em `input[type=\"password\"]` e digite **S3nh@NunkV@z0u**. Clique no `button[type=\"submit\"]`. Aguarde a aplicação rotear. Tire um screenshot final. O teste teve êxito se a URL resultante for `/tracker` com o cronômetro zerado na tela."

Reporte-me o status descritivo na conclusão da Fase 2.

### FASE 3: Teardown
Se ao final do teste (sucesso ou falha) o usuário pedir para encerrar o ambiente, execute os scripts de "parada" conforme orientações no `docs/scripts/stop_all.md` ou acione os scripts utilitários `stop_services.ps1` / `stop_services.sh` diretamente.
