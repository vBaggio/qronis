import React, { useState } from 'react';
import { useAuth } from '../../lib/auth-context';
import { authApi } from '../../lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Clock } from 'lucide-react';

export const Login: React.FC = () => {
    const { login } = useAuth();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e: React.SyntheticEvent) => {
        e.preventDefault();
        setError('');
        setIsLoading(true);

        try {
            // In real scenario, the endpoint would be '/auth/login'
            const response = await authApi.post('/login', { email, password });

            // Assume the backend returns a token field
            if (response.data && response.data.token) {
                await login(response.data.token);
            } else {
                setError('Resposta inválida do servidor.');
            }
        } catch (err: any) {
            setError(err.response?.data?.message || 'E-mail ou senha inválidos.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="flex min-h-screen items-center justify-center bg-zinc-50 px-4 py-12 dark:bg-zinc-950">
            <div className="w-full max-w-md space-y-6">
                <div className="flex flex-col items-center justify-center space-y-2 text-center">
                    <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-emerald-100 dark:bg-emerald-900/30">
                        <Clock className="h-6 w-6 text-emerald-600 dark:text-emerald-400" />
                    </div>
                    <h1 className="text-2xl font-bold tracking-tight text-zinc-900 dark:text-zinc-50">Bem-vindo ao Qronis</h1>
                    <p className="text-sm text-zinc-500 dark:text-zinc-400">
                        Foque no trabalho. Nós cuidamos do tempo.
                    </p>
                </div>

                <Card className="border-0 shadow-xl dark:border dark:border-zinc-800 dark:bg-zinc-900/50">
                    <form onSubmit={handleSubmit}>
                        <CardHeader>
                            <CardTitle>Entrar</CardTitle>
                            <CardDescription>
                                Insira suas credenciais para acessar seu workspace.
                            </CardDescription>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            {error && (
                                <div className="rounded-md bg-red-50 p-3 text-sm text-red-600 dark:bg-red-900/30 dark:text-red-400">
                                    {error}
                                </div>
                            )}
                            <div className="space-y-2">
                                <Label htmlFor="email">E-mail</Label>
                                <Input
                                    id="email"
                                    type="email"
                                    placeholder="Seu e-mail profissional"
                                    required
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    disabled={isLoading}
                                    className="bg-white dark:bg-zinc-950"
                                />
                            </div>
                            <div className="space-y-2">
                                <div className="flex items-center justify-between">
                                    <Label htmlFor="password">Senha</Label>
                                </div>
                                <Input
                                    id="password"
                                    type="password"
                                    required
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    disabled={isLoading}
                                    className="bg-white dark:bg-zinc-950"
                                />
                            </div>
                        </CardContent>
                        <CardFooter className="pt-4">
                            <Button
                                type="submit"
                                className="w-full bg-emerald-600 hover:bg-emerald-700 text-white dark:bg-emerald-600 dark:hover:bg-emerald-700"
                                disabled={isLoading}
                            >
                                {isLoading ? 'Autenticando...' : 'Entrar na Plataforma'}
                            </Button>
                        </CardFooter>
                    </form>
                </Card>
            </div>
        </div>
    );
};
