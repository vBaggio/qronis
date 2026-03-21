import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '@/lib/api';
import { TopNav } from '@/components/layout/TopNav';
import { TimeEntryList } from '@/components/history/TimeEntryList';
import type { TimeEntry } from '@/components/history/TimeEntryRow';
import { Button } from '@/components/ui/button';
import { Loader2, ArrowLeft, Plus, Pencil } from 'lucide-react';
import { formatSmartDuration } from '@/lib/time-utils';
import { TimeEntryModal } from '@/components/tracker/TimeEntryModal';
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogDescription,
    DialogFooter,
} from '@/components/ui/dialog';

interface PageResponse {
    content: TimeEntry[];
    last: boolean;
    totalPages: number;
    totalElements: number;
    number: number;
}

interface ProjectSummary {
    projectId: string;
    totalDurationSeconds: number;
}

interface Project {
    id: string;
    name: string;
}

export const ProjectDetails: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    // Core Data
    const [project, setProject] = useState<Project | null>(null);
    const [summary, setSummary] = useState<ProjectSummary | null>(null);
    const [entries, setEntries] = useState<TimeEntry[]>([]);

    // UI State
    const [isLoading, setIsLoading] = useState(true);
    const [isLoadingMore, setIsLoadingMore] = useState(false);
    const [page, setPage] = useState(0);
    const [isLastPage, setIsLastPage] = useState(false);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [pendingDeleteId, setPendingDeleteId] = useState<string | null>(null);

    // Title Inline Edit
    const [projectName, setProjectName] = useState('');
    const [isSavingName, setIsSavingName] = useState(false);

    // Filters
    const [sortDirection, setSortDirection] = useState<'desc' | 'asc'>('desc');

    const fetchProjectData = useCallback(async () => {
        if (!id || id === 'undefined') return;
        try {
            const [projRes, sumRes] = await Promise.all([
                api.get<Project>(`/projects/${id}`),
                api.get<ProjectSummary>(`/projects/${id}/summary`)
            ]);
            setProject(projRes.data);
            setProjectName(projRes.data.name);
            setSummary(sumRes.data);
        } catch (error) {
            console.error('Failed to load project details', error);
            navigate('/projects');
        }
    }, [id, navigate]);

    const fetchEntries = useCallback(async (pageNumber: number, append = false) => {
        if (!id || id === 'undefined') return;
        const loadingSetter = append ? setIsLoadingMore : setIsLoading;
        if (!append) loadingSetter(true);

        try {
            const res = await api.get<PageResponse>(`/time-entries?page=${pageNumber}&size=20&sort=startTime,${sortDirection}&projectId=${id}`);

            if (append) {
                setEntries(prev => [...prev, ...res.data.content]);
            } else {
                setEntries(res.data.content);
            }
            setIsLastPage(res.data.last);
            setPage(res.data.number);
        } catch (error) {
            console.error('Failed to load project entries', error);
        } finally {
            loadingSetter(false);
        }
    }, [id, sortDirection]);

    useEffect(() => {
        fetchProjectData();
    }, [fetchProjectData]);

    useEffect(() => {
        fetchEntries(0, false);
    }, [fetchEntries, sortDirection]);

    const handleLoadMore = () => {
        if (!isLastPage) {
            fetchEntries(page + 1, true);
        }
    };

    const handleDeleteRequest = (entryId: string) => {
        setPendingDeleteId(entryId);
    };

    const handleDeleteConfirm = async () => {
        if (!pendingDeleteId) return;
        try {
            await api.delete(`/time-entries/${pendingDeleteId}`);
            setEntries(prev => prev.filter(e => e.id !== pendingDeleteId));
            // Firing a background refresh for the summary without blocking
            api.get<ProjectSummary>(`/projects/${id}/summary`).then(res => setSummary(res.data));
        } catch (error) {
            console.error('Failed to delete time entry:', error);
            fetchEntries(0, false);
        } finally {
            setPendingDeleteId(null);
        }
    };

    const handleUpdate = (updatedEntry: TimeEntry) => {
        setEntries(prev => prev.map(e => e.id === updatedEntry.id ? updatedEntry : e));
    };

    const toggleSort = () => {
        setSortDirection(prev => prev === 'desc' ? 'asc' : 'desc');
    };

    const handleNameBlur = async () => {
        if (!projectName.trim() || projectName.trim() === project?.name) return;
        setIsSavingName(true);
        try {
            const res = await api.put<Project>(`/projects/${id}`, { name: projectName.trim() });
            setProject(res.data);
            setProjectName(res.data.name);
        } catch (error) {
            console.error('Failed to update project name', error);
            setProjectName(project?.name || '');
        } finally {
            setIsSavingName(false);
        }
    };

    const handleNameKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') e.currentTarget.blur();
        if (e.key === 'Escape') {
            setProjectName(project?.name || '');
            e.currentTarget.blur();
        }
    };

    // Calculate intelligent string for total duration from total seconds
    const totalDurationStr = summary
        ? formatSmartDuration(new Date().toISOString(), new Date(Date.now() + summary.totalDurationSeconds * 1000).toISOString()).value
        : '--';

    if (isLoading && !project) {
        return (
            <div className="min-h-screen bg-zinc-50 dark:bg-zinc-950 flex flex-col pt-32 items-center">
                <Loader2 className="h-8 w-8 animate-spin text-emerald-500" />
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-white dark:bg-zinc-950 transition-colors duration-500 flex flex-col">
            <TopNav />

            {/* Delete Confirmation Dialog */}
            <Dialog open={!!pendingDeleteId} onOpenChange={(open: boolean) => { if (!open) setPendingDeleteId(null); }}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Excluir lançamento?</DialogTitle>
                        <DialogDescription>
                            Esta ação não pode ser desfeita. O registro de tempo será removido permanentemente.
                        </DialogDescription>
                    </DialogHeader>
                    <DialogFooter>
                        <Button variant="outline" className="rounded-full" onClick={() => setPendingDeleteId(null)}>Cancelar</Button>
                        <Button
                            onClick={handleDeleteConfirm}
                            className="bg-red-600 hover:bg-red-700 text-white rounded-full"
                        >
                            Excluir
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* Sub-Nav actions row */}
            <div className="container mx-auto px-4 md:px-8 py-4 max-w-5xl">
                <Button
                    variant="ghost"
                    onClick={() => navigate('/projects')}
                    className="text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100 -ml-4"
                >
                    <ArrowLeft className="mr-2 h-4 w-4" />
                    Voltar a Projetos
                </Button>
            </div>

            <main className="container mx-auto px-4 md:px-8 py-4 max-w-5xl flex-1 pb-24">

                {/* Analytics Header (Mini-Dash) */}
                <header className="flex flex-col gap-6 mb-12">
                    <div className="flex flex-col md:flex-row md:items-end justify-between gap-6">
                        <div className="flex-1 relative group">
                            <div className="flex items-center gap-3">
                                <input
                                    value={projectName}
                                    onChange={(e) => setProjectName(e.target.value)}
                                    onBlur={handleNameBlur}
                                    onKeyDown={handleNameKeyDown}
                                    className="text-4xl md:text-5xl font-bold tracking-tight text-zinc-900 dark:text-zinc-50 bg-transparent border-none p-0 focus:outline-none focus:ring-0 w-full truncate cursor-text hover:bg-zinc-50 dark:hover:bg-zinc-800/30 rounded-lg px-2 -mx-2 transition-colors"
                                    placeholder="Nome do Projeto..."
                                />
                                {isSavingName && <Loader2 className="w-5 h-5 text-zinc-400 animate-spin shrink-0" />}
                                <Pencil className="w-4 h-4 text-zinc-300 dark:text-zinc-600 opacity-0 group-hover:opacity-100 transition-opacity shrink-0 pointer-events-none" />
                            </div>
                        </div>

                        {/* Total Invested — Inline Metric */}
                        <div className="shrink-0 flex flex-col items-end">
                            <span className="text-sm font-medium text-zinc-400 dark:text-zinc-500">
                                Total investido
                            </span>
                            <span className="text-3xl font-bold text-emerald-600 dark:text-emerald-500 tabular-nums">
                                {totalDurationStr}
                            </span>
                        </div>
                    </div>
                </header>

                {/* Action Toolbar — separator + controls without heading */}
                <div className="flex items-center justify-end gap-3 border-t border-zinc-100 dark:border-zinc-800/50 pt-6 mb-6">
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={toggleSort}
                        className="bg-zinc-50 dark:bg-zinc-900 rounded-full h-11 px-4 font-medium"
                    >
                        {sortDirection === 'desc' ? '↓ Recentes primeiro' : '↑ Antigos primeiro'}
                    </Button>
                    <Button
                        className="bg-emerald-50/80 text-emerald-700 hover:bg-emerald-100 hover:text-emerald-800 dark:bg-emerald-500/10 dark:text-emerald-400 dark:hover:bg-emerald-500/20 rounded-full h-11 px-4 shadow-sm border border-emerald-200/50 dark:border-emerald-800/50 font-medium"
                        onClick={() => setIsModalOpen(true)}
                    >
                        <Plus className="mr-2 h-4 w-4" /> Adicionar
                    </Button>
                </div>

                {/* Flat List Component mapped for Full Control */}
                <div className="w-full">
                    <TimeEntryList
                        entries={entries}
                        isReadOnly={false}
                        groupByDay={false}  // Crucial: Flat list to respect ASC/DESC sorting
                        showProjectBadge={false}
                        isLoading={isLoading && page === 0}
                        onDelete={handleDeleteRequest}
                        onUpdate={handleUpdate}
                    />

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

            {/* Launch Manual Entry Modal */}
            {id && (
                <TimeEntryModal
                    isOpen={isModalOpen}
                    onClose={() => setIsModalOpen(false)}
                    projectId={id}
                    onSuccess={() => {
                        // Refresh entries and summary
                        fetchEntries(0, false);
                        api.get<ProjectSummary>(`/projects/${id}/summary`).then(res => setSummary(res.data));
                    }}
                />
            )}
        </div>
    );
};
