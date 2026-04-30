import React, { useState } from 'react';
import axios from 'axios';
import { TopNav } from '@/components/layout/TopNav';
import { TimerDisplay } from '@/components/tracker/TimerDisplay';
import { ProjectSelector } from '@/components/tracker/ProjectSelector';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Play, Square } from 'lucide-react';
import { useActiveTimer, useStartTimer, useStopTimer } from '@/hooks/useTimer';
import { MESSAGES } from '@/lib/constants';

export const ZenTimer: React.FC = () => {
    const [taskDescription, setTaskDescription] = useState('');
    const [selectedProjectId, setSelectedProjectId] = useState<string | null>(null);

    const { data: activeTimer, isLoading } = useActiveTimer();
    const startTimer = useStartTimer();
    const stopTimer = useStopTimer();

    const isZenMode = !!activeTimer;
    const startTime = activeTimer?.startTime ?? null;
    const error = startTimer.error || stopTimer.error;

    const handleStart = async () => {
        if (!selectedProjectId) return;
        startTimer.mutate({ projectId: selectedProjectId, description: taskDescription });
    };

    const handleStop = () => {
        stopTimer.mutate(undefined, {
            onSuccess: () => {
                setTaskDescription('');
                setSelectedProjectId(null);
            },
        });
    };

    if (isLoading) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-white dark:bg-zinc-950">
                <div className="h-8 w-8 animate-spin rounded-full border-4 border-emerald-500 border-t-transparent"></div>
            </div>
        );
    }

    const errorMessage = error
        ? (axios.isAxiosError(error) ? error.response?.data?.message : undefined)
            ?? 'Ocorreu um erro. Tente novamente.'
        : null;

    return (
        <div className="flex flex-col min-h-screen bg-white dark:bg-zinc-950 transition-colors duration-500 overflow-hidden">
            <div className={`transition-all duration-700 ease-in-out origin-top ${isZenMode ? 'opacity-0 h-0 overflow-hidden' : 'opacity-100 h-16'}`}>
                <TopNav />
            </div>

            <main className="flex-1 container mx-auto p-4 md:p-8 flex flex-col items-center justify-center">
                <div className="w-full max-w-3xl space-y-8 flex flex-col items-center transition-all duration-700">

                    {errorMessage && (
                        <div className="w-full max-w-md rounded-lg bg-red-50 dark:bg-red-900/30 p-4 text-sm text-red-600 dark:text-red-400 text-center animate-in fade-in duration-300">
                            {errorMessage}
                        </div>
                    )}

                    <TimerDisplay isActive={isZenMode} startTime={startTime} />

                    <div className={`text-xl md:text-3xl text-zinc-600 dark:text-zinc-400 font-medium transition-all duration-700 text-center ${isZenMode ? 'opacity-100 translate-y-0 h-auto' : 'opacity-0 h-0 overflow-hidden -translate-y-4'}`}>
                        {activeTimer?.description || 'Foco Profundo'}
                    </div>

                    <div className={`w-full flex flex-col md:flex-row gap-3 md:gap-4 items-center justify-center transition-all duration-700 ${isZenMode ? 'opacity-0 h-0 overflow-hidden scale-95' : 'opacity-100 scale-100'}`}>
                        <div className="w-full md:w-auto">
                            <ProjectSelector
                                selectedProjectId={selectedProjectId}
                                onSelect={setSelectedProjectId}
                                disabled={isZenMode}
                            />
                        </div>
                        <Input
                            id="taskDescription"
                            name="taskDescription"
                            value={taskDescription}
                            onChange={(e) => setTaskDescription(e.target.value)}
                            onKeyDown={(e) => { if (e.key === 'Enter' && selectedProjectId) handleStart(); }}
                            placeholder={`${MESSAGES.TIMER_PLACEHOLDER}…`}
                            className="w-full md:flex-1 h-14 md:h-16 text-lg md:text-xl px-6 border-transparent focus-visible:ring-1 focus-visible:ring-emerald-500 bg-white dark:bg-zinc-900 text-left shadow-sm rounded-full"
                        />
                        <Button
                            onClick={handleStart}
                            size="lg"
                            className="w-full md:w-auto h-14 md:h-16 px-8 md:px-10 rounded-full bg-emerald-600 hover:bg-emerald-700 text-white text-lg shadow-lg hover:shadow-emerald-500/25 disabled:opacity-50 transition-all flex-shrink-0"
                            disabled={!selectedProjectId || startTimer.isPending}
                        >
                            <Play className="mr-2 h-5 w-5 fill-current" aria-hidden="true" /> Iniciar
                        </Button>
                    </div>

                    <div className={`transition-all duration-700 ${isZenMode ? 'opacity-100 scale-100 pt-8' : 'opacity-0 h-0 pt-0 overflow-hidden scale-95'}`}>
                        <Button
                            onClick={handleStop}
                            size="lg"
                            variant="destructive"
                            aria-label="Parar timer"
                            disabled={stopTimer.isPending}
                            className="h-16 w-16 p-0 rounded-full shadow-lg hover:shadow-red-500/25 hover:-translate-y-1 transition-all"
                        >
                            <Square className="h-6 w-6 fill-current" aria-hidden="true" />
                        </Button>
                    </div>
                </div>
            </main>
        </div>
    );
};
