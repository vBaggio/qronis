# Iniciar e Parar a Aplicação Completa (Qronis)

Se você precisa rodar a aplicação inteira para desenvolvimento, você precisará inicializar os três serviços essenciais: **Banco de Dados**, **Backend** e **Frontend**.

## 🚀 Como Iniciar Tudo

Como os serviços rodam em processos separados, a melhor abordagem é abrir **três abas/janelas de terminal** diferentes na raiz do projeto (`qronis`):

### Terminal 1: Banco de Dados
```bash
# Sobe o banco em background (libera o terminal em seguida, mas é bom manter a organização)
docker-compose up -d postgres
```

### Terminal 2: Backend (Spring Boot)
*(Aguarde o banco estar rodando)*
```powershell
# No Windows
.\gradlew bootRun

# No Linux/macOS
./gradlew bootRun
```

### Terminal 3: Frontend (React/Vite)
```bash
cd frontend
npm run dev
```

*(Dica para automação/background: se for rodar o Vite em script ou por agente IA, envolva com a variável `CI=true` para evitar suspensão do terminal. Ex Windows: `$env:CI="true"; npm run dev`)*

Após todos os serviços subirem, acesse `http://localhost:5173` no seu navegador. O frontend vai se comunicar com o backend em `http://localhost:8080`, que por sua vez lê o banco na porta `5434`.

---

## 🛑 Como Parar Tudo

Para encerrar os serviços, consulte o guia dedicado em [stop_all.md](stop_all.md) ou feche os terminais ou utilize os utilitários de encerramento (`stop_services.ps1` e `stop_services.sh`).
