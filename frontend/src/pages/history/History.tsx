import React, { useState, useEffect } from 'react';
import { api } from '../../lib/api';
import { TopNav } from '../../components/layout/TopNav';
import { TimeEntryList } from '../../components/history/TimeEntryList';
import type { TimeEntry } from '../../components/history/TimeEntryRow';
import { ProjectSelector } from '../../components/tracker/ProjectSelector';
import { Button } from '@/components/ui/button';
import { Loader2, X } from 'lucide-react';

interface PageResponse {
    content: TimeEntry[];
    last: boolean;
    totalPages: number;
    totalElements: number;
    number: number;
}

export const History: React.FC = () => {
    const [entries, setEntries] = useState<TimeEntry[]>([]);
    const [page, setPage] = useState(0);
    const [isLastPage, setIsLastPage] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [isLoadingMore, setIsLoadingMore] = useState(false);

    const [selectedProjectId, setSelectedProjectId] = useState<string | null>(null);

    const fetchHistory = async (pageNumber: number, append = false) => {
        const loadingSetter = append ? setIsLoadingMore : setIsLoading;
        loadingSetter(true);
        try {
            // Force 20 items per page. Always chronological decrescent.
            const projectQuery = selectedProjectId ? `&projectId=${selectedProjectId}` : '';
            const res = await api.get<PageResponse>(`/time-entries?page=${pageNumber}&size=20&sort=startTime,desc${projectQuery}`);

            if (append) {
                setEntries(prev => [...prev, ...res.data.content]);
            } else {
                setEntries(res.data.content);
            }

            setIsLastPage(res.data.last);
            setPage(res.data.number);
        } catch (error) {
            console.error('Failed to fetch history:', error);
        } finally {
            loadingSetter(false);
        }
    };

    // Initial load or filter change
    useEffect(() => {
        fetchHistory(0, false);
    }, [selectedProjectId]);

    const handleLoadMore = () => {
        if (!isLastPage) {
            fetchHistory(page + 1, true);
        }
    };

    return (
        <div className="min-h-screen bg-white dark:bg-zinc-950 transition-colors duration-500">
            <TopNav />

            <main className="container mx-auto px-4 md:px-8 py-10 max-w-5xl">

                {/* Header Section (Typographic Brutalism) */}
                <header className="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-12">
                    <div>
                        <h1 className="text-4xl md:text-5xl font-bold tracking-tight text-zinc-900 dark:text-zinc-50">
                            Histórico
                        </h1>
                    </div>

                    {/* Filter Strip */}
                    <div className="flex items-center gap-3">
                        {selectedProjectId && (
                            <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => setSelectedProjectId(null)}
                                className="text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100 px-3 h-10 md:h-12 rounded-full hidden sm:flex"
                            >
                                <X className="h-4 w-4 mr-1" /> Limpar
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

                {/* Main List */}
                <div className="w-full">
                    <TimeEntryList
                        entries={entries}
                        isReadOnly={true}
                        groupByDay={true}
                        isLoading={isLoading && page === 0}
                    />

                    {/* Load More Trigger */}
                    {!isLoading && !isLastPage && entries.length > 0 && (
                        <div className="mt-8 flex justify-center">
                            <Button
                                variant="ghost"
                                onClick={handleLoadMore}
                                disabled={isLoadingMore}
                                className="text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100"
                            >
                                {isLoadingMore ? (
                                    <>
                                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                        Carregando...
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
