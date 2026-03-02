import React, { useState } from 'react';
import axios from 'axios';
import { useAuth } from '../../lib/auth-context';
import { authApi } from '../../lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Link } from 'react-router-dom';

export const Register: React.FC = () => {
    const { login } = useAuth();

    const [formData, setFormData] = useState({
        name: '',
        email: '',
        password: '',
        confirmPassword: '',
        companyName: ''
    });
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData(prev => ({ ...prev, [e.target.id]: e.target.value }));
    };

    const handleSubmit = async (e: React.SyntheticEvent) => {
        e.preventDefault();
        setError('');
        setIsLoading(true);

        if (formData.password !== formData.confirmPassword) {
            setError('As senhas não coincidem. Tente novamente.');
            setIsLoading(false);
            return;
        }

        try {
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            const { confirmPassword, ...apiPayload } = formData;
            const response = await authApi.post('/register', apiPayload);

            if (response.data?.token) {
                await login(response.data.token);
            } else {
                setError('Conta criada, mas o token não foi retornado. Tente fazer login.');
            }
        } catch (err: unknown) {
            if (axios.isAxiosError(err)) {
                if (!err.response && err.request) {
                    setError('Servidor indisponível. Verifique sua conexão ou tente novamente mais tarde.');
                } else {
                    setError(err.response?.data?.message || 'Falha ao criar a conta. Verifique os dados.');
                }
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
                        Construa a base<br />para o seu fluxo.
                    </h2>
                    <p className="text-lg text-zinc-500 dark:text-zinc-400 font-medium leading-relaxed">
                        Crie seu workspace em segundos. Deixe para trás ferramentas complexas e comece a rastrear tempo de forma nativa e sem intromissões.
                    </p>
                </div>
            </div>

            {/* Lado Direito: Área de Ação (Registro) */}
            <div className="flex-1 flex flex-col justify-center items-center py-4 px-6 md:px-12 relative overflow-y-auto min-h-0">
                {/* Efeito Mobile: Logo para telas menores */}
                <div className="lg:hidden absolute top-6 left-6">
                    <Link to="/" className="inline-block transition-transform hover:scale-105 active:scale-95">
                        <img src="/qronis_ext.svg" alt="Qronis Logo" className="h-8 w-auto object-contain drop-shadow-sm" />
                    </Link>
                </div>

                <div className="w-full max-w-[400px] animate-in fade-in slide-in-from-bottom-8 duration-700 mt-12 lg:mt-0 pb-4">
                    <div className="mb-6 text-center lg:text-left">
                        <h1 className="text-3xl font-bold tracking-tight text-zinc-900 dark:text-zinc-50 mb-2">Crie seu Workspace</h1>
                        <p className="text-zinc-500 dark:text-zinc-400 text-sm font-medium">Alguns dados básicos e você estará pronto para o Zen Mode.</p>
                    </div>

                    <form onSubmit={handleSubmit} className="space-y-4">
                        {error && (
                            <div className="rounded-xl bg-red-50 p-4 text-sm font-medium text-red-600 dark:bg-red-900/20 dark:text-red-400 border border-red-100 dark:border-red-900/50 flex items-start gap-3 animate-in fade-in zoom-in-95 duration-300">
                                <span className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-red-100 dark:bg-red-900/50 text-red-600 dark:text-red-400">!</span>
                                {error}
                            </div>
                        )}

                        <div className="space-y-4">
                            <div className="space-y-2">
                                <Label htmlFor="name" className="text-zinc-700 dark:text-zinc-300 font-semibold">Seu Nome</Label>
                                <Input
                                    id="name"
                                    placeholder="Ex: Alan Turing"
                                    required
                                    value={formData.name}
                                    onChange={handleChange}
                                    disabled={isLoading}
                                    className="h-12 bg-zinc-50 dark:bg-zinc-900/50 border-zinc-200 dark:border-zinc-800 focus-visible:ring-emerald-500/30 text-base"
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="companyName" className="text-zinc-700 dark:text-zinc-300 font-semibold">Nome da Empresa (Workspace)</Label>
                                <Input
                                    id="companyName"
                                    placeholder="Sua Consultoria ou Nome Pessoal"
                                    required
                                    value={formData.companyName}
                                    onChange={handleChange}
                                    disabled={isLoading}
                                    className="h-12 bg-zinc-50 dark:bg-zinc-900/50 border-zinc-200 dark:border-zinc-800 focus-visible:ring-emerald-500/30 text-base"
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="email" className="text-zinc-700 dark:text-zinc-300 font-semibold">E-mail Profissional</Label>
                                <Input
                                    id="email"
                                    type="email"
                                    placeholder="alan@turing.com"
                                    required
                                    value={formData.email}
                                    onChange={handleChange}
                                    disabled={isLoading}
                                    className="h-12 bg-zinc-50 dark:bg-zinc-900/50 border-zinc-200 dark:border-zinc-800 focus-visible:ring-emerald-500/30 text-base"
                                />
                            </div>

                            <div className="space-y-4">
                                <div className="space-y-2">
                                    <Label htmlFor="password" className="text-zinc-700 dark:text-zinc-300 font-semibold">Senha Forte</Label>
                                    <Input
                                        id="password"
                                        type="password"
                                        required
                                        value={formData.password}
                                        onChange={handleChange}
                                        disabled={isLoading}
                                        className="h-12 bg-zinc-50 dark:bg-zinc-900/50 border-zinc-200 dark:border-zinc-800 focus-visible:ring-emerald-500/30 text-base"
                                    />
                                </div>
                                <div className="space-y-2">
                                    <Label htmlFor="confirmPassword" className="text-zinc-700 dark:text-zinc-300 font-semibold">Confirmar Senha</Label>
                                    <Input
                                        id="confirmPassword"
                                        type="password"
                                        required
                                        value={formData.confirmPassword}
                                        onChange={handleChange}
                                        disabled={isLoading}
                                        className="h-12 bg-zinc-50 dark:bg-zinc-900/50 border-zinc-200 dark:border-zinc-800 focus-visible:ring-emerald-500/30 text-base"
                                    />
                                </div>
                            </div>
                        </div>

                        <Button
                            type="submit"
                            className="w-full h-12 text-base font-semibold bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl shadow-lg shadow-emerald-500/20 transition-all hover:ring-4 ring-emerald-500/20 active:scale-95 mt-4"
                            disabled={isLoading}
                        >
                            {isLoading ? 'Criando Workspace...' : 'Próximo Passo'}
                        </Button>
                    </form>

                    <p className="mt-8 text-center text-sm text-zinc-500 dark:text-zinc-400 font-medium pb-8 lg:pb-0">
                        Já tem uma conta?{' '}
                        <Link to="/login" className="text-emerald-600 dark:text-emerald-500 underline underline-offset-4 hover:text-emerald-700 dark:hover:text-emerald-400 transition-colors">
                            Acessar o sistema
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
};
