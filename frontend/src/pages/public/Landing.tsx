import React from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Clock, Keyboard, BarChart3, ShieldCheck, Code, Globe } from 'lucide-react';
import { Button } from '@/components/ui/button';

export const Landing: React.FC = () => {
  return (
    <div className="flex min-h-screen flex-col bg-zinc-50 dark:bg-zinc-950 font-sans selection:bg-emerald-200 dark:selection:bg-emerald-900/40">
      {/* Header */}
      <header className="sticky top-0 z-50 w-full border-b border-zinc-200/50 bg-white/70 backdrop-blur-xl dark:border-zinc-800/50 dark:bg-zinc-950/70">
        <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-6">
          <div className="flex items-center">
            <img src="/qronis_ext.svg" alt="Qronis" className="h-9 md:h-10 w-auto object-contain drop-shadow-sm" />
          </div>
          <div className="flex items-center gap-6">
            <Link to="/login" className="text-sm font-semibold text-zinc-600 hover:text-zinc-900 dark:text-zinc-400 dark:hover:text-zinc-50 transition-colors">
              Entrar
            </Link>
            <Button asChild className="rounded-full bg-emerald-600 px-6 font-semibold hover:bg-emerald-700 text-white shadow-md shadow-emerald-500/20 transition-all hover:scale-105 active:scale-95">
              <Link to="/register">Iniciar Grátis</Link>
            </Button>
          </div>
        </div>
      </header>

      <main className="flex-1 w-full overflow-hidden">
        {/* ── 1. Split-Screen Hero ── */}
        <section className="relative mx-auto max-w-7xl px-6 py-4 md:py-8 flex flex-col lg:flex-row items-center gap-12 lg:gap-8">
          {/* Background Accents (Subtle Gradients) */}
          <div className="absolute top-0 -left-1/4 h-[500px] w-[500px] rounded-full bg-emerald-400/10 blur-[100px] dark:bg-emerald-900/20 -z-10" />

          {/* Left Copy: Vexatious Pain vs Zen Solution */}
          <div className="flex-1 space-y-8 text-center lg:text-left z-10 animate-in fade-in slide-in-from-bottom-8 duration-1000">
            {/* Elite Trust Builder Badge */}
            <div className="inline-flex items-center gap-2 rounded-full border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900 px-4 py-1.5 text-xs font-semibold text-zinc-600 dark:text-zinc-400 shadow-sm mx-auto lg:mx-0">
              <span className="flex h-2 w-2 rounded-full bg-emerald-500 animate-pulse" />
              Lançamento Qronis 1.0
            </div>

            <h1 className="text-5xl lg:text-7xl font-extrabold tracking-tight text-zinc-900 dark:text-zinc-50 leading-[1.1]">
              Rastreamento de tempo.<br />
              <span className="text-emerald-600 dark:text-emerald-500">Zero atrito.</span>
            </h1>

            <p className="max-w-xl text-lg lg:text-xl text-zinc-600 dark:text-zinc-400 leading-relaxed mx-auto lg:mx-0 font-medium">
              Projetado para engenheiros e freelancers de elite que odeiam preencher planilhas no fim do dia. Pressione Play, entre no Zen Mode e vamos com tudo.
            </p>

            <div className="flex flex-col sm:flex-row items-center gap-4 pt-4 justify-center lg:justify-start">
              <Button asChild size="lg" className="h-14 px-8 text-lg bg-emerald-600 hover:bg-emerald-700 text-white rounded-full shadow-xl shadow-emerald-500/25 transition-all hover:ring-4 ring-emerald-500/20 active:scale-95 w-full sm:w-auto">
                <Link to="/register">
                  Começar agora
                  <ArrowRight className="ml-2 h-5 w-5" />
                </Link>
              </Button>
            </div>

            {/* Social Proof Avatars */}
            <div className="pt-8 flex flex-col sm:flex-row items-center gap-4 justify-center lg:justify-start text-sm text-zinc-500 font-medium border-t border-zinc-100 dark:border-zinc-800/60 mt-8 pt-8 lg:mt-12 lg:pt-12">
              <div className="flex -space-x-3">
                {[1, 2, 3, 4].map((i) => (
                  <img key={i} className="inline-block h-10 w-10 rounded-full ring-2 ring-white dark:ring-zinc-950 shadow-sm" src={`https://i.pravatar.cc/100?img=${i + 10}`} alt={`User ${i}`} />
                ))}
                <div className="flex items-center justify-center h-10 w-10 rounded-full border-2 border-white dark:border-zinc-950 bg-zinc-100 dark:bg-zinc-800 text-xs font-bold text-zinc-600 dark:text-zinc-400 z-10 shadow-sm">
                  +500
                </div>
              </div>
              <p>Engenheiros recuperando o foco neste exato minuto.</p>
            </div>
          </div>

          {/* Right Art: Concept Vector Illustration (Transparent Background) */}
          <div className="flex-1 relative w-full max-w-lg lg:max-w-none animate-in fade-in slide-in-from-right-8 duration-1000 delay-200 flex items-center justify-center p-8">
            <div className="absolute inset-0 bg-emerald-400/5 dark:bg-emerald-900/10 blur-[120px] rounded-full -z-10" />
            <img
              src="/zen_hero_transparent.png"
              alt="Conceito Zen do Qronis"
              className="w-full h-full max-h-[500px] object-contain filter drop-shadow-2xl transition-transform duration-700 ease-out hover:scale-[1.3] -mt-8 lg:-mt-16 scale-110 lg:scale-125 origin-center"
            />
          </div>
        </section>

        {/* ── 2. Asymmetric Bento Grid (Features) ── */}
        <section className="bg-white dark:bg-zinc-900 border-y border-zinc-100 dark:border-zinc-800 py-16 lg:py-24">
          <div className="mx-auto max-w-6xl px-6">
            <div className="text-center mb-10 max-w-2xl mx-auto space-y-4">
              <h2 className="text-3xl font-bold tracking-tight text-zinc-900 dark:text-zinc-50">Tudo que você precisa. Nada mais.</h2>
              <p className="text-zinc-500 dark:text-zinc-400 text-lg">Criamos um ecossistema projetado estritamente para não interferir na sua produtividade.</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 auto-rows-[minmax(180px,auto)]">
              {/* Bento Item 1: Zen Mode (Featured / Full Width) */}
              <div className="md:col-span-3 group relative overflow-hidden rounded-[2rem] bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 p-8 md:p-12 hover:shadow-2xl hover:shadow-emerald-500/10 transition-all duration-500 flex flex-col md:flex-row items-center gap-12">
                <div className="absolute top-0 right-0 p-12 opacity-5 group-hover:opacity-10 transition-opacity pointer-events-none">
                  <Clock className="w-96 h-96 text-emerald-600 -translate-y-1/4 translate-x-1/4" />
                </div>

                <div className="relative z-10 flex-1 space-y-6">
                  <div className="inline-flex h-14 w-14 items-center justify-center rounded-2xl bg-white dark:bg-zinc-900 shadow-md border border-zinc-100 dark:border-zinc-800 mb-2 group-hover:scale-110 transition-transform">
                    <ShieldCheck className="h-7 w-7 text-emerald-600" />
                  </div>
                  <h3 className="text-3xl md:text-4xl font-extrabold tracking-tight text-zinc-900 dark:text-zinc-50">Zen Mode Nativo</h3>
                  <p className="text-zinc-500 dark:text-zinc-400 text-lg md:text-xl leading-relaxed font-medium max-w-lg">
                    Ao iniciar sua tarefa, toda a interface desaparece. Resta apenas o timer limpo no centro da tela. Respire, entre no fluxo e codifique sem ser interrompido pelas abas.
                  </p>
                </div>

                {/* Epic GIF Showcase */}
                <div className="relative z-10 flex-[1.5] w-full mt-8 md:mt-0 perspective-1000">
                  <div className="rounded-2xl bg-white dark:bg-zinc-900 border border-zinc-200/80 dark:border-zinc-800/80 p-2 shadow-2xl overflow-hidden bg-gradient-to-b from-zinc-50 to-zinc-100 dark:from-zinc-900 dark:to-zinc-950 ring-1 ring-black/5 dark:ring-white/5 transform transition-transform duration-700 hover:scale-[1.02]">
                    <div className="absolute inset-0 bg-emerald-500/5 mix-blend-overlay pointer-events-none z-10"></div>
                    <img src="/zen-mode-action.gif" alt="Zen Mode em Ação" className="w-full h-auto object-cover object-top rounded-xl opacity-95 transition-opacity duration-500 hover:opacity-100 shadow-sm relative z-0" />
                  </div>
                </div>
              </div>

              {/* Bento Item 2: Edição Inline (Wide) */}
              <div className="md:col-span-2 group relative overflow-hidden rounded-[2rem] bg-zinc-50 dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 p-8 md:p-10 hover:shadow-xl transition-all duration-300 flex flex-col justify-center">
                <div className="relative z-10 flex flex-col md:flex-row items-center gap-8">
                  <div className="flex-1 space-y-4 text-center md:text-left">
                    <div className="inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-white dark:bg-zinc-900 shadow-sm border border-zinc-100 dark:border-zinc-800 mb-2 group-hover:scale-110 transition-transform">
                      <Keyboard className="h-6 w-6 text-emerald-600" />
                    </div>
                    <h3 className="text-xl font-bold text-zinc-900 dark:text-zinc-50">Edição Direta e Rápida</h3>
                    <p className="text-zinc-500 dark:text-zinc-400 font-medium">Sem sub-menus, modais chatos ou botões de salvar infinitos. Digite, dê Enter, done.</p>
                  </div>
                  <div className="flex-1 w-full bg-white dark:bg-zinc-900 rounded-xl p-4 border border-zinc-200 dark:border-zinc-800 shadow-inner">
                    <div className="flex items-center gap-3 p-2 bg-zinc-50 dark:bg-zinc-950 rounded border border-emerald-500/30 ring-2 ring-emerald-500/10">
                      <span className="w-2 h-2 rounded-full bg-emerald-500"></span>
                      <span className="font-mono text-sm text-zinc-900 dark:text-zinc-50">Reescrevendo a API...</span>
                      <span className="ml-auto w-1 h-4 bg-emerald-500 animate-pulse"></span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Bento Item 3: Insights (Small/Square) */}
              <div className="md:col-span-1 group relative overflow-hidden rounded-[2rem] bg-emerald-600 border border-emerald-500 p-8 md:p-10 hover:shadow-xl hover:shadow-emerald-500/20 transition-all duration-300">
                <div className="relative z-10 flex flex-col h-full justify-between">
                  <div className="space-y-4">
                    <div className="inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-emerald-700/50 shadow-sm border border-emerald-500/50 mb-2 text-white group-hover:scale-110 transition-transform">
                      <BarChart3 className="h-6 w-6" />
                    </div>
                    <h3 className="text-xl font-bold text-white">Insights Brutais</h3>
                    <p className="text-emerald-100 font-medium max-w-sm">Métricas reais para faturamento de projetos. Transparência nua e crua da sua alocação.</p>
                  </div>
                  <div className="mt-8 flex items-end gap-2 h-20 opacity-80">
                    <div className="w-1/6 bg-white/40 h-[40%] rounded-t-sm transition-all group-hover:h-[60%]"></div>
                    <div className="w-1/6 bg-white/60 h-[70%] rounded-t-sm transition-all group-hover:h-[80%]"></div>
                    <div className="w-1/6 bg-white/30 h-[30%] rounded-t-sm transition-all group-hover:h-[50%]"></div>
                    <div className="w-1/6 bg-white border border-white h-[90%] rounded-t-sm relative shadow-[0_0_15px_rgba(255,255,255,0.5)]"><span className="absolute -top-6 left-1/2 -translate-x-1/2 text-xs font-bold text-white uppercase hidden group-hover:block transition-all">$</span></div>
                    <div className="w-1/6 bg-white/50 h-[50%] rounded-t-sm transition-all group-hover:h-[40%]"></div>
                    <div className="w-1/6 bg-white/70 h-[80%] rounded-t-sm transition-all group-hover:h-[95%]"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* ── 3. Philosophy & Architecture (Trust Builder) ── */}
        <section className="bg-zinc-50 dark:bg-zinc-950 py-24">
          <div className="mx-auto max-w-4xl px-6 text-center space-y-8">
            <div className="inline-flex items-center justify-center p-3 bg-zinc-100 dark:bg-zinc-900 rounded-full border border-zinc-200 dark:border-zinc-800 mb-4">
              <Code className="h-6 w-6 text-zinc-500" />
            </div>
            <h2 className="text-3xl font-bold tracking-tight text-zinc-900 dark:text-zinc-50">Arquitetura de Grau Enterprise</h2>
            <p className="text-lg text-zinc-500 dark:text-zinc-400 font-medium max-w-2xl mx-auto leading-relaxed">
              O Qronis não é mais uma ferramenta no-code frágil. Somos esculpidos à mão sobre uma fundação robusta de <b>Java 21 LTS</b> e <b>PostgreSQL</b> no backend, acrisolados com um frontend imaculado em <b>React 19</b>. Sem falsas promessas, entregamos performance pura.
            </p>
            <div className="pt-8 grid grid-cols-2 md:grid-cols-4 gap-4 max-w-2xl mx-auto opacity-70 grayscale hover:grayscale-0 transition-all duration-500">
              {/* Fictional/Tech badges to boost perception */}
              <div className="flex flex-col items-center gap-2 p-4"><div className="font-bold text-zinc-800 dark:text-zinc-200">JWT</div><div className="text-xs text-zinc-400">Security</div></div>
              <div className="flex flex-col items-center gap-2 p-4"><div className="font-bold text-zinc-800 dark:text-zinc-200">OAuth2</div><div className="text-xs text-zinc-400">Resource Server</div></div>
              <div className="flex flex-col items-center gap-2 p-4"><div className="font-bold text-zinc-800 dark:text-zinc-200">OKLCH</div><div className="text-xs text-zinc-400">Color System</div></div>
              <div className="flex flex-col items-center gap-2 p-4"><div className="font-bold text-emerald-600 dark:text-emerald-500">SSOT</div><div className="text-xs text-zinc-400">Single Source</div></div>
            </div>
          </div>
        </section>
      </main>

      {/* ── 4. Elevated SaaS Footer ── */}
      <footer className="border-t border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-950 py-12 lg:py-16">
        <div className="mx-auto max-w-6xl px-6 grid grid-cols-1 md:grid-cols-4 gap-8">
          <div className="md:col-span-1 space-y-4">
            <img src="/qronis_ext.svg" alt="Qronis" className="h-8 w-auto object-contain grayscale opacity-80" />
            <p className="text-sm text-zinc-500 dark:text-zinc-400 leading-relaxed max-w-xs">
              O padrão ouro em rastreamento de tempo para profissionais de alto desempenho.
            </p>
          </div>

          <div className="space-y-4">
            <h4 className="text-sm font-semibold text-zinc-900 dark:text-zinc-50 uppercase tracking-wider">Produto</h4>
            <ul className="space-y-2 text-sm text-zinc-500 dark:text-zinc-400">
              <li><Link to="/register" className="hover:text-emerald-600 transition-colors">Testar Grátis</Link></li>
              <li><Link to="/login" className="hover:text-emerald-600 transition-colors">Login</Link></li>
              <li><a href="#" className="hover:text-emerald-600 transition-colors">Preços</a></li>
            </ul>
          </div>

          <div className="space-y-4">
            <h4 className="text-sm font-semibold text-zinc-900 dark:text-zinc-50 uppercase tracking-wider">Recursos</h4>
            <ul className="space-y-2 text-sm text-zinc-500 dark:text-zinc-400">
              <li><a href="#" className="hover:text-emerald-600 transition-colors">API & Docs</a></li>
              <li><a href="#" className="hover:text-emerald-600 transition-colors">Open Source</a></li>
              <li className="flex items-center gap-2"><div className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse"></div><a href="#" className="hover:text-emerald-600 transition-colors">Status do Sistema</a></li>
            </ul>
          </div>

          <div className="space-y-4">
            <h4 className="text-sm font-semibold text-zinc-900 dark:text-zinc-50 uppercase tracking-wider">Legal</h4>
            <ul className="space-y-2 text-sm text-zinc-500 dark:text-zinc-400">
              <li><a href="#" className="hover:text-emerald-600 transition-colors">Privacidade</a></li>
              <li><a href="#" className="hover:text-emerald-600 transition-colors">Termos de Serviço</a></li>
              <li><a href="#" className="hover:text-emerald-600 transition-colors flex items-center gap-1"><Globe className="h-3 w-3" /> PT-BR</a></li>
            </ul>
          </div>
        </div>

        <div className="mx-auto max-w-6xl px-6 pt-12 mt-12 border-t border-zinc-100 dark:border-zinc-800/60 flex flex-col sm:flex-row items-center justify-between text-xs text-zinc-400">
          <p>&copy; {new Date().getFullYear()} Qronis. Todos os direitos reservados. Foco absoluto.</p>
        </div>
      </footer>
    </div>
  );
};
