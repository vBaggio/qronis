# Qronis Brand Identity & AI Generation Guide

Este documento contém os parâmetros técnicos, paleta de cores e scripts (prompts) otimizados para geradores de imagem com IA (NanoBanana, Midjourney, DALL-E) na criação dos assets visuais do Qronis.

## 📦 1. Lista de Entregáveis (Assets Necessários)

*   **1. Logo Symbol (Ícone Primário):** Apenas o símbolo gráfico (sem texto). É o que vai no canto esquerdo da nossa `TopNav`, Landing Page e Favicon.
*   **2. Favicon:** Será derivado do *Logo Symbol* e exportado para `.ico` ou `.png` de 32x32px.
*   **3. Open Graph Image (Banner de Compartilhamento):** Uma imagem retangular (1200x630px). Usada como "capa" ao compartilhar o link do sistema (WhatsApp, Discord, LinkedIn).
*   **4. (Opcional) Abstract Hero Graphic:** Arte de fundo sutil para a Landing Page caso desejado no futuro.

---

## 🎨 2. Diretrizes Técnicas (O Design System)

Para que a I.A. obedeça e o resultado não fique datado, siga estas regras-chave:

*   **Estilo Visual:** Minimalismo brutalista, Flat Design, SaaS corporativo moderno.
*   **A "Vibe":** Foco absoluto, "Zen", e fluxo contínuo.
*   **Restrições:** Sem texturas ultra-realistas, sem gradientes cafonas e, principalmente, **sem renderizar imagens colossais de relógios de ponteiros, despertadores físicos ou ampulhetas**.
*   **Fundo:** Force a IA a usar branco sólido (`#FFFFFF`) para o Símbolo para facilitar a remoção depois.

### Paleta de Cores Oficial
Sempre mencione os hexadecimais no prompt para garantir a precisão da IA. As cores oficiais do Front-end em Tailwind são:

*   🟢 **Emerald Principal:** HEX `#059669` | RGB `(5, 150, 105)`
*   🟩 **Emerald Acento:** HEX `#10b981` | RGB `(16, 185, 129)`
*   ⬛ **Zinc Escuro (Background):** HEX `#18181b` | RGB `(24, 24, 27)`
*   ⬜ **Zinc Claro (Background):** HEX `#fafafa` | RGB `(250, 250, 250)`

---

## 🚀 3. Prompts Otimizados para Copiar e Colar

### Prompt 01: Logo Symbol (Ícone)
**Configuração da I.A.:** Aspect Ratio 1:1 (Quadrado), Estilo "Vector" ou "Logo Design".

```text
A minimal and modern logo symbol for a digital productivity and time-tracking SaaS web application called "Qronis". The design must represent "Zen focus" and "time flow" without using literal old clocks or hourglasses. Think of geometric abstract shapes, a sleek continuous minimalist loop, or a highly stylized abstract letter "Q". Flat vector design, crisp edges, premium corporate branding, negative space. Primary color must be Emerald Green (RGB 5 150 105 or HEX #059669). The background must be pure solid white (HEX #FFFFFF) to allow for easy background removal. No text, no typography, graphic symbol only. High resolution, dribbble UI style, clean.
```

### Prompt 02: Open Graph Banner (Capa do Link)
**Configuração da I.A.:** Aspect Ratio 16:9 ou 2:1 (Paisagem/Retangular).

```text
A premium architectural background image for a tech landing page hero section. The theme is "Zen Mode productivity" and "deep focus". Abstract minimal 3D geometric shapes softly floating or interacting with light. The color palette must strictly use Emerald Green tones (HEX #059669 and #10b981) subtly mixed with dark charcoal zinc grays (HEX #18181b). Soft volumetric lighting, extremely clean, lots of negative space on the left side to place text over it later. UI/UX presentation style, modern SaaS background.
```

---

## 🛠️ 4. Pós-produção e Integração (Próximos Passos)

1. Assim que o *Logo Symbol* for gerado com o fundo branco sólido, use uma ferramenta (como Photoshop ou `remove.bg`) para deixar o fundo transparente.
2. Salve o arquivo principal transparente como `logo.png`.
3. Salve o banner como `og-image.jpg` ou `banner.jpg`.
4. Mova esses arquivos visualizados e aprovados para o diretório de arquivos estáticos do Next/Vite em:
   `d:\dev\java\qronis\frontend\public\`
5. Retorne ao código e avise o sistema que os assets estão disponíveis para injeção via `<img src="/logo.png" />`.
