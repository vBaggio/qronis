import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { TopNav } from '../../components/layout/TopNav';
import { TimerDisplay } from '../../components/tracker/TimerDisplay';
import { ProjectSelector } from '../../components/tracker/ProjectSelector';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Play, Square } from 'lucide-react';
import { api } from '../../lib/api';

export const ZenTimer: React.FC = () => {
    const [isZenMode, setIsZenMode] = useState(false);
    const [taskDescription, setTaskDescription] = useState('');
    const [selectedProjectId, setSelectedProjectId] = useState<string | null>(null);
    const [startTime, setStartTime] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        checkActiveTimer();
    }, []);

    const checkActiveTimer = async () => {
        try {
            const res = await api.get('/time-entries/active');
            if (res.data && res.data.id) {
                // Resume active timer from backend
                setIsZenMode(true);
                setTaskDescription(res.data.description || '');
                setSelectedProjectId(res.data.projectId);
                setStartTime(res.data.startTime);
            }
        } catch (error: any) {
            // Se retornar 204 No Content, é porque não tem timer rodando
            if (error.response?.status !== 204) {
                console.error('Falha ao verificar timer ativo:', error);
            }
        } finally {
            setIsLoading(false);
        }
    };

    const handleStart = async () => {
        if (!selectedProjectId) return;
        setError(null);
        try {
            const res = await api.post('/time-entries/start', {
                projectId: selectedProjectId,
                description: taskDescription
            });
            setStartTime(res.data.startTime);
            setIsZenMode(true);
        } catch (err: unknown) {
            if (axios.isAxiosError(err)) {
                setError(err.response?.data?.message || 'Falha ao iniciar o timer.');
            } else {
                setError('Erro inesperado ao iniciar o timer.');
            }
        }
    };

    const handleStop = async () => {
        setError(null);
        try {
            await api.put('/time-entries/stop');
            setIsZenMode(false);
            setStartTime(null);
            setTaskDescription('');
            setSelectedProjectId(null);
        } catch (err: unknown) {
            if (axios.isAxiosError(err)) {
                setError(err.response?.data?.message || 'Falha ao parar o timer.');
            } else {
                setError('Erro inesperado ao parar o timer.');
            }
        }
    };

    if (isLoading) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-zinc-50 dark:bg-zinc-950">
                <div className="h-8 w-8 animate-spin rounded-full border-4 border-emerald-500 border-t-transparent"></div>
            </div>
        );
    }

    return (
        <div className="flex flex-col min-h-screen bg-zinc-50 dark:bg-zinc-950 transition-colors duration-500 overflow-hidden">
            {/* Navbar transitions out when Zen Mode is active */}
            <div className={`transition-all duration-700 ease-in-out origin-top ${isZenMode ? 'opacity-0 h-0 overflow-hidden' : 'opacity-100 h-16'}`}>
                <TopNav />
            </div>

            <main className="flex-1 container mx-auto p-4 md:p-8 flex flex-col items-center justify-center">
                <div className="w-full max-w-3xl space-y-8 flex flex-col items-center transition-all duration-700">

                    {/* Error Banner */}
                    {error && (
                        <div className="w-full max-w-md rounded-lg bg-red-50 dark:bg-red-900/30 p-4 text-sm text-red-600 dark:text-red-400 text-center animate-in fade-in duration-300">
                            {error}
                        </div>
                    )}

                    {/* The Giant Timer */}
                    <TimerDisplay
                        isActive={isZenMode}
                        startTime={startTime}
                    />

                    {/* Display task name as text in Zen mode */}
                    <div className={`text-xl md:text-3xl text-zinc-600 dark:text-zinc-400 font-medium transition-all duration-700 text-center ${isZenMode ? 'opacity-100 translate-y-0 h-auto' : 'opacity-0 h-0 overflow-hidden -translate-y-4'}`}>
                        {taskDescription || 'Foco Profundo'}
                    </div>

                    {/* Start controls & Input (Idle Mode) */}
                    <div className={`w-full flex flex-col sm:flex-row gap-4 items-center justify-center transition-all duration-700 ${isZenMode ? 'opacity-0 h-0 overflow-hidden scale-95' : 'opacity-100 scale-100'}`}>
                        <ProjectSelector
                            selectedProjectId={selectedProjectId}
                            onSelect={setSelectedProjectId}
                            disabled={isZenMode}
                        />
                        <Input
                            id="taskDescription"
                            name="taskDescription"
                            value={taskDescription}
                            onChange={(e) => setTaskDescription(e.target.value)}
                            placeholder="O que você está construindo agora?"
                            className="flex-1 h-14 md:h-16 text-lg md:text-xl px-6 border-transparent focus-visible:ring-1 focus-visible:ring-emerald-500 bg-white dark:bg-zinc-900 text-left shadow-sm rounded-full"
                        />
                        <Button
                            onClick={handleStart}
                            size="lg"
                            className="w-full sm:w-auto h-14 md:h-16 px-8 md:px-10 rounded-full bg-emerald-600 hover:bg-emerald-700 text-white text-lg shadow-lg hover:shadow-emerald-500/25 disabled:opacity-50 transition-all flex-shrink-0"
                            disabled={!selectedProjectId} // Frontend validation for required project
                        >
                            <Play className="mr-2 h-5 w-5 fill-current" /> Iniciar
                        </Button>
                    </div>

                    {/* Stop control (Zen Mode) */}
                    <div className={`transition-all duration-700 ${isZenMode ? 'opacity-100 scale-100 pt-8' : 'opacity-0 h-0 pt-0 overflow-hidden scale-95'}`}>
                        <Button
                            onClick={handleStop}
                            size="lg"
                            variant="destructive"
                            className="h-16 w-16 p-0 rounded-full shadow-lg hover:shadow-red-500/25 hover:-translate-y-1 transition-all"
                        >
                            <Square className="h-6 w-6 fill-current" />
                        </Button>
                    </div>
                </div>
            </main>
        </div>
    );
};
