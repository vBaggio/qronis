# Regras Técnicas (Constituição do Projeto)

## 🏗️ Tech Stack Oficial e Versões
O Qronis deve respeitar as versões estipuladas abaixo para garantir estabilidade e previsibilidade.

### Backend
- **Linguagem:** Java 21 (LTS).
- **Framework:** Spring Boot 4.0.3 (arquitetura modularizada).
- **Banco de Dados:** PostgreSQL 17 (na porta local 5434).
- **Ferramental:** Gradle, Flyway (Migrations).
- **Segurança:** Spring Security com OAuth2 Resource Server para validação JWT stateless.

### Frontend
- **Framework Ouro:** React 19 executado via Vite.
- **Linguagem:** TypeScript.
- **Estilização:** Tailwind CSS v4.
- **Componentes:** Shadcn UI.

---

## 🔒 Leis Universais do Backend (Invioláveis)
Estas são as regras invioláveis para o desenvolvimento do lado servidor. 

1. **DTOs Imutáveis:** 
   - O uso de classes tradicionais para DTOs (Data Transfer Objects) é proibido. Toda transferência de dados entre o Controller e o Service deve ocorrer utilizando **Java Records** exclusivamente, finalizados sempre com o sufixo `DTO`.

2. **Mapeamento Pragmático:**
   - O rastreamento manual de dados entre Entidades e Records DTOs não é permitido. O framework **MapStruct** é obrigatório para converter `Entity <-> Record DTO`.

3. **Performance de Consulta (Obrigatório JOIN FETCH):**
   - A síndrome N+1 deve ser erradicada na base. Ao mapear Entidades para DTOs em consultas que necessitam de dados de tabelas relacionadas (como o criador de um projeto, ou o projeto de um time entry), a query no Repository deve utilizar a cláusula `JOIN FETCH` explícita, mantendo o default behavior global como `Lazy Loading`.

4. **Autenticação Direta e Limpa:**
   - O padrão arcaico do Spring de injetar `UserDetailsService` e `AuthenticationManager` não deve ser utilizado.
   - O Login deve ser efetuado utilizando uma query direta no banco buscando em **1 query via JOIN FETCH** a relação `TenantUser → User`.
   - A validação de senha ocorre manualmente na camada Service usando `passwordEncoder.matches()`.

5. **Assinatura Protegida JWT:**
   - Módulo construído sobre o `NimbusJwtEncoder`/`NimbusJwtDecoder`. Tokens são gerados utilizando o algoritmo HMAC (HS256) em conformidade com o Resource Server padrão do Spring `oauth2ResourceServer`.

---

## ⚡ Leis Universais do Frontend (Invioláveis)
Estas são as regras invioláveis para o desenvolvimento das interfaces, especialmente o core do sistema (Live Tracker).

1. **A Regra de Ouro do Cronômetro:**
   - Para evitar bloqueios na thread principal e re-renders em cadeia, a UI deve utilizar um manipulador `setInterval` atachado num hook utilitário.
   - O frontend **NUNCA DEVE SOMAR SEGUNDOS VIA STATE (`setSeconds(s => s+1)`)**.
   - O cálculo matemático inviolável para renderizar o timer é: `[Hora Local do Browser Atual] - [start_time do banco convertido para o fuso local]`. Essa diferença nativa gera a interface visual, de forma imune a gargalos do event loop.
