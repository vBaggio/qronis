# Rodar a Malha de Testes (Back-end)

A malha de testes assegura a integridade de regressões do backend simulando casos em bases PostgreSQL providas em tempo-real através do **Testcontainers**.

## Como executar

Pelo terminal, na raiz do projeto (`qronis`):

**No Windows (PowerShell/CMD):**
```powershell
.\gradlew test
```

**No Linux/macOS:**
```bash
./gradlew test
```

## Relatórios de Cobertura e Resultados
O arquivo de log consolidado contendo a saída detalhada e resultados em formato HTML pode estar localizado em:
`build/reports/tests/test/index.html`

> 💡 **Nota do Testcontainers:**
A primeira execução do Testcontainers pode levar mais tempo enquanto a imagem do banco é baixada do Docker Hub para suportar os testes.
