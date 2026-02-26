# Iniciar o Banco de Dados (Docker)

O projeto usa Docker Compose para garantir consistência no ambiente de desenvolvimento do PostgreSQL.
O banco será exposto localmente na porta `5434` para evitar conflitos.

## Pré-requisitos
- Docker Engine e Docker Compose instalados e em execução.

## Como iniciar

1. Abra um terminal na raiz do projeto (`qronis`).
2. Digite o seguinte comando para subir o container do banco em background (detached mode):
   ```bash
   docker-compose up -d postgres
   ```
3. Aguarde alguns instantes até que o log confirme que o banco inicializou. Você pode checar o status executando:
   ```bash
   docker ps
   ```

Seu Spring Boot agora conectará automaticamente no próximo boot.
