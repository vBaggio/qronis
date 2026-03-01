# AI Prompt: Qronis Zen Concept Art

Este documento registra o prompt exato e as especificações de direção de arte utilizadas para gerar a ilustração principal da Landing Page do Qronis (o `zen_hero`). O objetivo deste registro é garantir que futuras artes (para outras páginas, blogs ou marketing) sigam rigorosamente a mesma linguagem arquitetural e estética.

## O Prompt Oficial (Inglês)
Recomenda-se utilizar este prompt base em ferramentas de geração de imagem como Midjourney, DALL-E 3 ou Stable Diffusion para manter a precisão do estilo:

```text
A minimalist sleek vector line-art illustration of a modern software engineer in a zen meditation pose, levitating slightly above the ground. Floating around them are abstract geometric shapes and a minimalist laptop, representing pure focus, deep work, and zero friction productivity. The art style should be clean, airy, and modern, similar to Notion or Linear branding. Thin black outlines on a pure white background, with only a few subtle accent elements colored in vibrant emerald green. High quality, corporate SaaS aesthetic, no clutter.
```

### Parâmetros Específicos para Midjourney (v6+)
Se optar pelo Midjourney, adicione estas tags ao final do prompt para garantir que a IA não injete 3D, sombras ou arte genérica:

```text
--ar 1:1 --stylize 100 --no shadows, 3d, gradient, realistic --v 6.0
```

---

## Direção de Arte Exigida
Caso um ser humano necessite replicar, expandir ou criar novos assets (ícones, banners) para o ecossistema Qronis, estas são as "regras de ouro" da nossa paleta ilustrativa:

1. **Estilo Visual:** *Line-art* (arte em linha vetorial) estritamente minimalista.
2. **Peso das Linhas:** As linhas devem ser de espessura de "caneta fina" (`stroke-width` leve), mantendo uma geometria precisa e não cartunesca.
3. **Paleta de Cores (A Regra do 90/10):**
   - **90% Monocromático:** Os traços estruturais da gravura devem ser totalmente pretos ou cinza-escuríssimos sobre fundo ausente (transparente ou `#FFFFFF`).
   - **10% Destaque (Spot Color):** Apenas um ou dois elementos minúsculos na tela recebem o "Verde Esmeralda" da marca (`#10B981` ou tons próximos) para indicar vida, energia e foco.
4. **Sem Profundidade Falsa:** 
   - Proibido o uso de degradês internos preenchendo os personagens.
   - Proibido sombras projetadas (`drop shadows`) desenhadas na própria imagem. O arquivo final deve ser *Flat* para que a sombra UI seja desenhada pelo próprio CSS do Tailwind.
5. **A Atmosfera:** Os temas giram em torno de *Deep Work*, controle, calma sob pressão, fluidez e engenharia limpa. Nada de personagens correndo com papéis voando e semblantes desesperados (o anti-SaaS corporativo antigo).
