# Contexto do Produto (O Porquê e o Quê)

## 📌 Problema de Negócio
No mercado atual, sistemas de rastreamento de tempo costumam ser excessivamente complexos, exigindo múltiplos cliques e preenchimentos extensos apenas para iniciar o registro do trabalho. Essa fricção desencoraja o uso contínuo por profissionais que buscam praticidade e foco.

## 👥 Público-Alvo
Desenvolvedores, freelancers, consultores e equipes que necessitam de um rastreamento de tempo invisível, fluido e livre de interrupções, permitindo foco exclusivo na execução do trabalho.

## 🚀 Visão do Produto
O **Qronis** é um SaaS de Time Tracker projetado para oferecer um fluxo sem atritos ("Zen Mode"). O objetivo central é fornecer a interface mais rápida e reativa possível para registrar tempo em projetos, minimizando o esforço cognitivo exigido pela ferramenta. 

A filosofia central do produto é: **iniciar fácil, registrar automaticamente**.

## ⚖️ Regras de Negócio Críticas
O sistema é governado pelas seguintes diretrizes imutáveis de domínio:

1. **Soberania do UTC:** 
   - Todas as datas e horas de início e fim das tarefas (`start_time`, `end_time`) são obrigatoriamente calculadas, persistidas no banco e trafegadas pela API em fuso `UTC`. A conversão de fuso horário ocorre estritamente na camada de exibição visual do usuário.

2. **Exclusividade de Foco (1 Timer Ativo):** 
   - O domínio permite uma e apenas uma tarefa em andamento por usuário simultaneamente. Se um `end_time` estiver nulo, nenhum novo registro de tempo pode ser iniciado até que a tarefa atual seja pausada ou concluída.

3. **Fluidez UI (Salvamento Inline):**
   - Não existe botão "Salvar" para edições de tarefas (Time Entries). Qualquer alteração nas descrições ou janelas de tempo registradas no histórico ocorre em tempo real no evento de perda de foco do input (`on blur`).

4. **Isolamento de Tenant:**
   - Todos os projetos e registros gerados são estritamente isolados pelo `Tenant` ao qual o usuário pertence. Informações de um espaço de trabalho jamais cruzam fronteiras, mesmo que o sistema seja single-database.
