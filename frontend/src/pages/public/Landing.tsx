import React from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Focus, Layers, LineChart } from 'lucide-react';
import { Button } from '@/components/ui/button';

export const Landing: React.FC = () => {
  return (
    <div className="flex min-h-screen flex-col bg-zinc-50 dark:bg-zinc-950">
      {/* Simple Header */}
      <header className="w-full max-w-5xl mx-auto flex h-16 items-center justify-between px-4 pt-4 md:pt-8 pb-4">
        <div className="flex items-center">
          <img src="/qronis_ext.svg" alt="Qronis" className="h-10 md:h-11 w-auto object-contain drop-shadow-md" />
        </div>
        <div className="flex items-center gap-4">
          <Link to="/login" className="text-sm font-medium text-zinc-600 hover:text-zinc-900 dark:text-zinc-400 dark:hover:text-zinc-50">
            Entrar
          </Link>
          <Button asChild className="bg-emerald-600 hover:bg-emerald-700 text-white">
            <Link to="/register">Criar Conta</Link>
          </Button>
        </div>
      </header>

      {/* Hero Section */}
      <main className="flex flex-1 flex-col items-center justify-center px-4 text-center">
        <div className="space-y-6 max-w-4xl py-20">
          <h1 className="text-5xl md:text-7xl font-extrabold tracking-tight text-zinc-900 dark:text-zinc-50 mb-8 animate-in fade-in slide-in-from-bottom-4 duration-1000">
            Foque no que importa.<br />
            <span className="text-emerald-600 dark:text-emerald-500">Nós cuidamos do tempo.</span>
          </h1>
          <p className="mx-auto max-w-2xl text-lg md:text-xl text-zinc-500 dark:text-zinc-400 leading-relaxed animate-in fade-in slide-in-from-bottom-4 duration-1000 delay-150">
            O time tracker brutalmente simples projetado para engenheiros e freelancers.
            Sem distrações, sem configurações infinitas. Apenas "Zen Mode" e produtividade absoluta.
          </p>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-4 pt-8 animate-in fade-in slide-in-from-bottom-4 duration-1000 delay-300">
            <Button asChild size="lg" className="h-14 px-8 text-lg bg-emerald-600 hover:bg-emerald-700 text-white rounded-full shadow-lg hover:shadow-emerald-500/25 transition-all hover:scale-105">
              <Link to="/register">
                Comece gratuitamente
                <ArrowRight className="ml-2 h-5 w-5" />
              </Link>
            </Button>
            <Button asChild size="lg" variant="ghost" className="h-14 px-8 text-lg rounded-full text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-50 hover:bg-zinc-100 dark:hover:bg-zinc-900 transition-colors">
              <Link to="/login">
                Já tenho uma conta
              </Link>
            </Button>
          </div>

          {/* Features Section */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 pt-24 animate-in fade-in slide-in-from-bottom-8 duration-1000 delay-500 text-left">

            {/* Feature 1 */}
            <div className="group rounded-2xl bg-white dark:bg-zinc-900/50 p-6 border border-zinc-100 dark:border-zinc-800 shadow-sm hover:shadow-md transition-all hover:-translate-y-1">
              <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-emerald-50 dark:bg-emerald-900/20 mb-4 group-hover:scale-110 transition-transform">
                <Focus className="h-6 w-6 text-emerald-600 dark:text-emerald-400" />
              </div>
              <h3 className="text-xl font-semibold text-zinc-900 dark:text-zinc-50 mb-2">Zen Mode Nativo</h3>
              <p className="text-zinc-500 dark:text-zinc-400 leading-relaxed">
                Ao iniciar uma tarefa, a interface inteira desaparece. Resta apenas o timer para você alcançar o estado de fluxo contínuo.
              </p>
            </div>

            {/* Feature 2 */}
            <div className="group rounded-2xl bg-white dark:bg-zinc-900/50 p-6 border border-zinc-100 dark:border-zinc-800 shadow-sm hover:shadow-md transition-all hover:-translate-y-1">
              <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-emerald-50 dark:bg-emerald-900/20 mb-4 group-hover:scale-110 transition-transform">
                <Layers className="h-6 w-6 text-emerald-600 dark:text-emerald-400" />
              </div>
              <h3 className="text-xl font-semibold text-zinc-900 dark:text-zinc-50 mb-2">Edição Inline</h3>
              <p className="text-zinc-500 dark:text-zinc-400 leading-relaxed">
                Esqueça formulários complexos. Ajuste segundos, minutos ou horas de qualquer projeto digitando diretamente na grade.
              </p>
            </div>

            {/* Feature 3 */}
            <div className="group rounded-2xl bg-white dark:bg-zinc-900/50 p-6 border border-zinc-100 dark:border-zinc-800 shadow-sm hover:shadow-md transition-all hover:-translate-y-1">
              <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-emerald-50 dark:bg-emerald-900/20 mb-4 group-hover:scale-110 transition-transform">
                <LineChart className="h-6 w-6 text-emerald-600 dark:text-emerald-400" />
              </div>
              <h3 className="text-xl font-semibold text-zinc-900 dark:text-zinc-50 mb-2">Insights Brutais</h3>
              <p className="text-zinc-500 dark:text-zinc-400 leading-relaxed">
                Métricas e gráficos gerados automaticamente baseados na alocação de tempo dos seus projetos e times reais.
              </p>
            </div>

          </div>
        </div>
      </main>

      {/* Footer minimalista */}
      <footer className="py-6 text-center text-sm text-zinc-500 dark:text-zinc-400">
        &copy; {new Date().getFullYear()} Qronis. Time tracking in Zen Mode.
      </footer>
    </div>
  );
};
