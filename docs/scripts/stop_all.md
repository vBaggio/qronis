# Parar Toda a Aplicação Local

Se você precisar parar todo o ambiente (Frontend, Backend e Banco de Dados), você pode usar os scripts utilitários que fecham as portas locais ou encerrar manualmente.

## ⚡ Método Automático (Recomendado para "Orfãos")

Se você fechou os terminais mas os processos continuaram rodando "presos" no background (ex: porta 8080 ou 5173 ocupada), use os scripts de shutdown:

### No Windows (PowerShell)
```powershell
pwsh -File docs/scripts/stop_services.ps1
docker-compose stop postgres
```

### No Linux/Mac (Bash)
```bash
bash docs/scripts/stop_services.sh
docker-compose stop postgres
```

## 🛑 Método Manual Tradicional

Se você ainda tem os terminais originais abertos (conforme descrito em [start_all.md](start_all.md)):

1. **Frontend:** No terminal do Vite, pressione `Ctrl + C`.
2. **Backend:** No terminal do Spring Boot, pressione `Ctrl + C`.
3. **Banco de Dados:** Em qualquer terminal na raiz, execute:
   ```bash
   docker-compose stop postgres
   ```
   *(Ou `docker-compose down` se quiser limpar a rede/containers descartáveis).*
