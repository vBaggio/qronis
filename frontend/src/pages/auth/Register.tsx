import React, { useState } from 'react';
import axios from 'axios';
import { useAuth } from '../../lib/auth-context';
import { authApi } from '../../lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardFooter } from '@/components/ui/card';
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
                setError(err.response?.data?.message || 'Falha ao criar a conta. Verifique os dados.');
            } else {
                setError('Erro inesperado. Tente novamente.');
            }
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="flex min-h-screen items-center justify-center bg-zinc-50 px-4 py-12 dark:bg-zinc-950">
            <div className="w-full max-w-md space-y-6">
                <div className="flex flex-col items-center justify-center space-y-2 text-center">
                    <Link to="/" className="flex h-16 items-center justify-center mb-2 transition-transform hover:scale-105">
                        <img src="/qronis_ext.svg" alt="Qronis Logo" className="h-12 w-auto object-contain drop-shadow-sm" />
                    </Link>
                    <h1 className="text-2xl font-bold tracking-tight text-zinc-900 dark:text-zinc-50">Crie seu Workspace</h1>
                    <p className="text-sm text-zinc-500 dark:text-zinc-400">
                        Alguns dados básicos e você estará pronto para o Zen Mode.
                    </p>
                </div>

                <Card className="border-0 shadow-xl dark:border dark:border-zinc-800 dark:bg-zinc-900/50">
                    <form onSubmit={handleSubmit}>
                        <CardContent className="flex flex-col gap-4 p-6 pt-2">
                            {error && (
                                <div className="rounded-md bg-red-50 p-3 text-sm text-red-600 dark:bg-red-900/30 dark:text-red-400">
                                    {error}
                                </div>
                            )}

                            <div className="space-y-2">
                                <Label htmlFor="name">Seu Nome</Label>
                                <Input
                                    id="name"
                                    placeholder="Ex: Alan Turing"
                                    required
                                    value={formData.name}
                                    onChange={handleChange}
                                    disabled={isLoading}
                                    className="bg-white dark:bg-zinc-950"
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="companyName">Nome da Empresa (Workspace)</Label>
                                <Input
                                    id="companyName"
                                    placeholder="Sua Consultoria ou Nome Pessoal"
                                    required
                                    value={formData.companyName}
                                    onChange={handleChange}
                                    disabled={isLoading}
                                    className="bg-white dark:bg-zinc-950"
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="email">E-mail Profissional</Label>
                                <Input
                                    id="email"
                                    type="email"
                                    placeholder="alan@turing.com"
                                    required
                                    value={formData.email}
                                    onChange={handleChange}
                                    disabled={isLoading}
                                    className="bg-white dark:bg-zinc-950"
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="password">Senha Forte</Label>
                                <Input
                                    id="password"
                                    type="password"
                                    required
                                    value={formData.password}
                                    onChange={handleChange}
                                    disabled={isLoading}
                                    className="bg-white dark:bg-zinc-950"
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="confirmPassword">Confirmar Senha</Label>
                                <Input
                                    id="confirmPassword"
                                    type="password"
                                    required
                                    value={formData.confirmPassword}
                                    onChange={handleChange}
                                    disabled={isLoading}
                                    className="bg-white dark:bg-zinc-950"
                                />
                            </div>
                        </CardContent>
                        <CardFooter className="flex flex-col space-y-4 pt-4">
                            <Button
                                type="submit"
                                className="w-full bg-emerald-600 hover:bg-emerald-700 text-white dark:bg-emerald-600 dark:hover:bg-emerald-700"
                                disabled={isLoading}
                            >
                                {isLoading ? 'Criando Conta...' : 'Começar Agora'}
                            </Button>
                            <div className="text-center text-sm text-zinc-500 dark:text-zinc-400">
                                Já tem uma conta? <Link to="/login" className="font-semibold text-emerald-600 hover:underline dark:text-emerald-400">Entrar</Link>
                            </div>
                        </CardFooter>
                    </form>
                </Card>
            </div>
        </div>
    );
};
