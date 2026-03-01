# Regras Técnicas (Constituição do Projeto)

## 🏗️ Tech Stack Oficial e Versões
O Qronis deve respeitar as versões estipuladas abaixo para garantir estabilidade e previsibilidade.

### Backend
- **Linguagem:** Java 21 (LTS).
- **Framework:** Spring Boot 4.0.3 (arquitetura modularizada).
- **Banco de Dados:** PostgreSQL 16 (na porta local 5434).
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

## 🎨 O Paradigma "Zen" (Identidade Visual e UX)
O Qronis deve transparecer calma, foco e acabamento premium. Para manter a coesão do design system nas próximas telas, siga estas heurísticas visuais:

1. **Minimalismo e Redução de Carga Cognitiva:**
   - Evite poluição visual. Esconda elementos secundários e mantenha o foco absoluto na ação primária da página.
   - Ações contextuais de listas/tabelas devem ser escondidas em **Dropdown Menus** (ícone `MoreVertical` `...`). Nunca exponha botões de "Editar" ou "Excluir" abertamente ocupando espaço na tabela, e remova o cabeçalho textual "Ações" das tabelas.
   - Elimine botões redundantes. Por exemplo: se o botão "Novo Projeto" já está gigante no centro da tela em um *Empty State*, oculte-o temporariamente do Header da página para evitar duplicidade de CTAs.

2. **Tipografia Premium:**
   - **Títulos Customizados:** Títulos de página (`h1`) devem usar `text-2xl font-semibold tracking-tight text-zinc-900`. O tracking (espaçamento negativo) dita um tom mais moderno.
   - **Cabeçalhos Mudos:** Cabeçalhos de tabela (`TableHeader`) não devem usar `uppercase`. Devem adotar *Sentence Case* (minúscula) com `text-sm font-medium text-zinc-500` para transmitir leveza.

3. **Cores, Botões e Coesão Orgânica:**
   - O tom primário de fundo é neutro (`zinc`). A cor de destaque é o `emerald`.
   - Modere o uso de botões sólidos (`bg-emerald-600`). Reserve-os apenas para as ações essenciais e afirmativas da view (como "Iniciar" timer, ou confirmar submit modal).
   - Para gatilhos (triggers) ou ações de navegação (como "Novo Projeto" no header), prefira botões modulares do tipo **Ghost/Soft** (`bg-emerald-50 text-emerald-700 hover:bg-emerald-100`). Eles dão utilidade sem "gritar" pela atenção do usuário.
   - Inputs de busca e controles principais (como os do Tracker) devem usar cantos perfeitamente arredondados (`rounded-full`) para gerar formas contínuas e orgânicas, nunca caixas rústicas quadradas.

4. **Anatomia de Tabelas e Listagens (Fim do CRUD corporativo):**
   - **Sem Bordas Agressivas:** Nunca use bordas laterais espessas (`border-l-4`) para denotar cor/categoria. Em vez disso, incorpore o indicador de cor de forma sutil através de um "Color Pill" esférico minúsculo (badge) renderizado na mesma célula textual com `gap`.
   - **Respiro Visceral:** O respiro vertical das linhas (padding) deve ser luxuoso (ex: `py-5`). Tabelas apertadas reduzem a sensação de produto premium.
   - **Estados Vazios Acionáveis:** O *Empty State* de qualquer listagem deve sempre centralizar um grande botão de ação (Lei de Fitts), transformando um dead-end em uma via de valor.

5. **Responsividade Inteligente (Mobile First Real):**
   - Em telas móveis (`sm` ou menores), evite o "efeito caixote" gerado pelo clássico empilhamento vertical `flex-col` estrito com larguras variadas em formulários ou controles de barra.
   - Elementos primários (ex: Seletor de Relacionamento) e Inputs textuais devem fluir ocupando de forma orgânica e previsível a largura da tela (`w-full`), alinhados logicamente para não parecerem "montados às pressas".
