# Iniciar o Frontend (React + Vite)

Este guia rápido descreve como iniciar o servidor de desenvolvimento do frontend.

## Pré-requisitos
- Node.js instalado (v18+)
- Dependências instaladas (se for a primeira vez, execute `npm install` na pasta `frontend`)

## Como iniciar

1. Abra um terminal.
2. Navegue até o diretório do frontend:
   ```bash
   cd frontend
   ```
3. Execute o comando de inicialização local:
   ```bash
   npm run dev
   ```

> 💡 **Dica para Execução em Background / Agentes de IA:**
> Se você precisar rodar o Vite em background num terminal automatizado sem que ele suspenda aguardando interações do usuário, defina a variável `CI=true`.
> Exemplo Windows: `$env:CI="true"; npm run dev`
> Exemplo Mac/Linux: `CI=true npm run dev`

O console exibirá o endereço local e a porta onde a aplicação Vite está rodando (normalmente `http://localhost:5173`).
