# Iniciar o Backend (Spring Boot)

Este guia descreve como iniciar o servidor backend usando o wrapper do Gradle (`gradlew`).

## Pré-requisitos
- Java 25+ (ou a versão especificada pelo projeto)
- O banco de dados PostgreSQL estar em execução na porta `5434` (veja `start_database.md`)

## Como iniciar

1. Abra um terminal na raiz do projeto (onde está localizado o arquivo `build.gradle.kts` e `gradlew`).
2. Execute o comando para rodar o Spring Boot:
   
   **No Windows (PowerShell/CMD):**
   ```powershell
   .\gradlew bootRun
   ```

   **No Linux/macOS:**
   ```bash
   ./gradlew bootRun
   ```

O backend iniciará na porta padrão `8080` com as validações automáticas do Flyway sendo efetuadas.
