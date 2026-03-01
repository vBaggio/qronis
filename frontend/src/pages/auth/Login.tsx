import React, { useState } from 'react';
import axios from 'axios';
import { useAuth } from '../../lib/auth-context';
import { authApi } from '../../lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Link, useLocation, useNavigate } from 'react-router-dom';

export const Login: React.FC = () => {
    const { login } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e: React.SyntheticEvent) => {
        e.preventDefault();
        setError('');
        setIsLoading(true);

        try {
            const response = await authApi.post('/login', { email, password });

            if (response.data?.token) {
                await login(response.data.token);
                const from = (location.state as { from?: { pathname: string } })?.from?.pathname || '/tracker';
                navigate(from, { replace: true });
            } else {
                setError('Resposta inválida do servidor.');
            }
        } catch (err: unknown) {
            if (axios.isAxiosError(err)) {
                setError(err.response?.data?.message || 'E-mail ou senha inválidos.');
            } else {
                setError('Erro inesperado. Tente novamente.');
            }
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="flex h-screen w-full bg-white dark:bg-zinc-950 font-sans selection:bg-emerald-200 dark:selection:bg-emerald-900/40 overflow-hidden">
            {/* Lado Esquerdo: Canvas Interativo / Identidade da Marca (Desktop Only) */}
            <div className="hidden lg:flex lg:w-1/2 relative flex-col justify-between h-full bg-zinc-50 dark:bg-zinc-950 overflow-hidden text-zinc-900 dark:text-zinc-50 p-8 lg:p-12 border-r border-zinc-200 dark:border-zinc-800/50">
                {/* Efeito de Luz / Fundo */}
                <div className="absolute top-0 left-0 w-full h-[500px] bg-emerald-500/10 dark:bg-emerald-900/20 blur-[120px] rounded-full -translate-x-1/2 -translate-y-1/2" />

                <div className="relative z-10 w-full flex justify-between items-start shrink-0">
                    <Link to="/" className="inline-block transition-transform hover:scale-105 active:scale-95">
                        <img src="/qronis_ext.svg" alt="Qronis Logo" className="h-10 w-auto object-contain dark:brightness-0 dark:invert dark:opacity-90" />
                    </Link>
                </div>

                {/* Arte Minimalista Dinâmica (Respeita Telas Menores) */}
                <div className="relative z-0 flex-1 flex items-center justify-center min-h-0 py-4 opacity-80 dark:opacity-20 pointer-events-none mix-blend-multiply dark:mix-blend-screen transition-transform duration-1000 hover:scale-105 hover:opacity-100 w-full lg:px-12">
                    <img src="/auth_portal_art.png" alt="Concept Art" className="w-full max-w-[400px] h-full object-contain filter dark:grayscale" />
                </div>

                <div className="relative z-10 max-w-md space-y-6 pb-4">
                    <h2 className="text-4xl font-extrabold tracking-tight leading-[1.1]">
                        Estado de fluxo.<br />Acesso liberado.
                    </h2>
                    <p className="text-lg text-zinc-500 dark:text-zinc-400 font-medium leading-relaxed">
                        A arquitetura do desenvolvedor moderno. Nós cuidamos do tempo, das métricas e do faturamento para que você possa focar exclusivamente em resolver o impossível.
                    </p>
                </div>
            </div>

            {/* Lado Direito: Área de Ação (Login) */}
            <div className="flex-1 flex flex-col justify-center items-center py-6 px-6 md:px-12 relative overflow-y-auto">
                {/* Efeito Mobile: Logo para telas menores */}
                <div className="lg:hidden absolute top-6 left-6">
                    <Link to="/" className="inline-block transition-transform hover:scale-105 active:scale-95">
                        <img src="/qronis_ext.svg" alt="Qronis Logo" className="h-8 w-auto object-contain drop-shadow-sm" />
                    </Link>
                </div>

                <div className="w-full max-w-[400px] animate-in fade-in slide-in-from-bottom-8 duration-700">
                    <div className="mb-10 text-center lg:text-left">
                        <h1 className="text-3xl font-bold tracking-tight text-zinc-900 dark:text-zinc-50 mb-2">Bem-vindo de volta</h1>
                        <p className="text-zinc-500 dark:text-zinc-400 text-sm font-medium">Insira suas credenciais corporativas para continuar.</p>
                    </div>

                    <form onSubmit={handleSubmit} className="space-y-6">
                        {error && (
                            <div className="rounded-xl bg-red-50 p-4 text-sm font-medium text-red-600 dark:bg-red-900/20 dark:text-red-400 border border-red-100 dark:border-red-900/50 flex items-start gap-3 animate-in fade-in zoom-in-95 duration-300">
                                <span className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-red-100 dark:bg-red-900/50 text-red-600 dark:text-red-400">!</span>
                                {error}
                            </div>
                        )}

                        <div className="space-y-4">
                            <div className="space-y-2">
                                <Label htmlFor="email" className="text-zinc-700 dark:text-zinc-300 font-semibold">E-mail</Label>
                                <Input
                                    id="email"
                                    type="email"
                                    placeholder="alan@turing.com"
                                    required
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    disabled={isLoading}
                                    className="h-12 bg-zinc-50 dark:bg-zinc-900/50 border-zinc-200 dark:border-zinc-800 focus-visible:ring-emerald-500/30 text-base"
                                />
                            </div>
                            <div className="space-y-2">
                                <div className="flex items-center justify-between">
                                    <Label htmlFor="password" className="text-zinc-700 dark:text-zinc-300 font-semibold">Senha</Label>
                                    <Link to="#" className="text-xs font-medium text-emerald-600 dark:text-emerald-500 hover:text-emerald-700 dark:hover:text-emerald-400 transition-colors">
                                        Esqueceu a senha?
                                    </Link>
                                </div>
                                <Input
                                    id="password"
                                    type="password"
                                    required
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    disabled={isLoading}
                                    className="h-12 bg-zinc-50 dark:bg-zinc-900/50 border-zinc-200 dark:border-zinc-800 focus-visible:ring-emerald-500/30 text-base"
                                />
                            </div>
                        </div>

                        <Button
                            type="submit"
                            className="w-full h-12 text-base font-semibold bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl shadow-lg shadow-emerald-500/20 transition-all hover:ring-4 ring-emerald-500/20 active:scale-95"
                            disabled={isLoading}
                        >
                            {isLoading ? 'Autenticando...' : 'Entrar na Plataforma'}
                        </Button>
                    </form>

                    <p className="mt-8 text-center text-sm text-zinc-500 dark:text-zinc-400 font-medium">
                        Não tem uma conta?{' '}
                        <Link to="/register" className="text-emerald-600 dark:text-emerald-500 underline underline-offset-4 hover:text-emerald-700 dark:hover:text-emerald-400 transition-colors">
                            Crie seu Workspace grátis
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
};
