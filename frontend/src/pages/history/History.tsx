import React, { useState } from 'react';
import { TopNav } from '../../components/layout/TopNav';
import { TimeEntryList } from '../../components/history/TimeEntryList';
import { ProjectSelector } from '../../components/tracker/ProjectSelector';
import { Button } from '@/components/ui/button';
import { Loader2, X } from 'lucide-react';
import { useTimeEntries } from '../../hooks/useTimeEntries';

export const History: React.FC = () => {
    const [selectedProjectId, setSelectedProjectId] = useState<string | null>(null);

    const {
        data,
        isLoading,
        isFetchingNextPage,
        hasNextPage,
        fetchNextPage,
        isError,
    } = useTimeEntries({ projectId: selectedProjectId });

    const entries = data?.pages.flatMap((p) => p.content) ?? [];

    return (
        <div className="min-h-screen bg-white dark:bg-zinc-950 transition-colors duration-500">
            <TopNav />

            <main className="container mx-auto px-4 md:px-8 py-10 max-w-5xl">

                <header className="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-12">
                    <div>
                        <h1 className="text-4xl md:text-5xl font-bold tracking-tight text-zinc-900 dark:text-zinc-50">
                            Histórico
                        </h1>
                    </div>

                    <div className="flex items-center gap-3">
                        {selectedProjectId && (
                            <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => setSelectedProjectId(null)}
                                className="text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100 px-3 h-10 md:h-12 rounded-full hidden sm:flex"
                            >
                                <X className="h-4 w-4 mr-1" aria-hidden="true" /> Limpar
                            </Button>
                        )}
                        <ProjectSelector
                            selectedProjectId={selectedProjectId}
                            onSelect={setSelectedProjectId}
                            allowCreate={false}
                            size="compact"
                        />
                    </div>
                </header>

                {isError && (
                    <div className="mb-6 rounded-lg bg-red-50 dark:bg-red-900/30 p-4 text-sm text-red-600 dark:text-red-400">
                        Falha ao carregar o histórico. Tente recarregar a página.
                    </div>
                )}

                <div className="w-full" aria-live="polite" aria-atomic="false">
                    <TimeEntryList
                        entries={entries}
                        isReadOnly={true}
                        groupByDay={true}
                        isLoading={isLoading}
                    />

                    {!isLoading && hasNextPage && entries.length > 0 && (
                        <div className="mt-8 flex justify-center">
                            <Button
                                variant="ghost"
                                onClick={() => fetchNextPage()}
                                disabled={isFetchingNextPage}
                                className="text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100"
                            >
                                {isFetchingNextPage ? (
                                    <>
                                        <Loader2 className="mr-2 h-4 w-4 animate-spin" aria-hidden="true" />
                                        Carregando…
                                    </>
                                ) : (
                                    'Carregar mais'
                                )}
                            </Button>
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
};
