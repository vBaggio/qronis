import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useProject, useProjectSummary, useUpdateProject } from '@/hooks/useProjects';
import { useTimeEntries, useDeleteTimeEntry } from '@/hooks/useTimeEntries';
import type { TimeEntry } from '@/lib/types';
import { TopNav } from '@/components/layout/TopNav';
import { TimeEntryList } from '@/components/history/TimeEntryList';
import { Button } from '@/components/ui/button';
import { Loader2, ArrowLeft, Plus, Pencil } from 'lucide-react';
import { formatDurationSeconds } from '@/lib/time-utils';
import { TimeEntryModal } from '@/components/tracker/TimeEntryModal';
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogDescription,
    DialogFooter,
} from '@/components/ui/dialog';
import { useQueryClient } from '@tanstack/react-query';
import { projectKeys } from '@/hooks/useProjects';

export const ProjectDetails: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [pendingDeleteId, setPendingDeleteId] = useState<string | null>(null);
    const [sortDirection, setSortDirection] = useState<'desc' | 'asc'>('desc');
    const [projectName, setProjectName] = useState('');
    const [isEditingName, setIsEditingName] = useState(false);

    const { data: project, isLoading: isLoadingProject } = useProject(id);
    const { data: summary } = useProjectSummary(id);
    const updateProject = useUpdateProject(id!);
    const deleteTimeEntry = useDeleteTimeEntry();

    const {
        data: entriesData,
        isLoading: isLoadingEntries,
        isFetchingNextPage,
        hasNextPage,
        fetchNextPage,
    } = useTimeEntries({ projectId: id, sort: `startTime,${sortDirection}` });

    const entries = entriesData?.pages.flatMap((p) => p.content) ?? [];

    // Sync local name with fetched project (only when not editing)
    React.useEffect(() => {
        if (project && !isEditingName) {
            setProjectName(project.name);
        }
    }, [project, isEditingName]);

    // Redirect if project not found
    React.useEffect(() => {
        if (!id || id === 'undefined') navigate('/projects');
    }, [id, navigate]);

    const handleNameBlur = () => {
        setIsEditingName(false);
        if (!projectName.trim() || projectName.trim() === project?.name) {
            setProjectName(project?.name ?? '');
            return;
        }
        updateProject.mutate(projectName.trim(), {
            onError: () => setProjectName(project?.name ?? ''),
        });
    };

    const handleNameKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') e.currentTarget.blur();
        if (e.key === 'Escape') {
            setProjectName(project?.name ?? '');
            setIsEditingName(false);
            e.currentTarget.blur();
        }
    };

    const handleDeleteConfirm = () => {
        if (!pendingDeleteId) return;
        deleteTimeEntry.mutate(pendingDeleteId, {
            onSuccess: () => {
                queryClient.invalidateQueries({ queryKey: projectKeys.summary(id!) });
                setPendingDeleteId(null);
            },
            onError: () => setPendingDeleteId(null),
        });
    };

    const handleUpdate = (updatedEntry: TimeEntry) => {
        // Optimistic update: patch entry in cache
        queryClient.setQueriesData(
            { queryKey: ['timeEntries'] },
            (old: { pages: { content: TimeEntry[] }[] } | undefined) => {
                if (!old) return old;
                return {
                    ...old,
                    pages: old.pages.map((page) => ({
                        ...page,
                        content: page.content.map((e) =>
                            e.id === updatedEntry.id ? updatedEntry : e
                        ),
                    })),
                };
            }
        );
    };

    const totalDurationStr = summary ? formatDurationSeconds(summary.totalDurationSeconds) : '--';

    if (isLoadingProject && !project) {
        return (
            <div className="min-h-screen bg-zinc-50 dark:bg-zinc-950 flex flex-col pt-32 items-center">
                <Loader2 className="h-8 w-8 animate-spin text-emerald-500" aria-hidden="true" />
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-white dark:bg-zinc-950 transition-colors duration-500 flex flex-col">
            <TopNav />

            <Dialog open={!!pendingDeleteId} onOpenChange={(open) => { if (!open) setPendingDeleteId(null); }}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Excluir lançamento?</DialogTitle>
                        <DialogDescription>
                            Esta ação não pode ser desfeita. O registro de tempo será removido permanentemente.
                        </DialogDescription>
                    </DialogHeader>
                    <DialogFooter>
                        <Button variant="outline" className="rounded-full" onClick={() => setPendingDeleteId(null)}>
                            Cancelar
                        </Button>
                        <Button
                            onClick={handleDeleteConfirm}
                            disabled={deleteTimeEntry.isPending}
                            className="bg-red-600 hover:bg-red-700 text-white rounded-full"
                        >
                            {deleteTimeEntry.isPending
                                ? <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" />
                                : 'Excluir'}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            <div className="container mx-auto px-4 md:px-8 py-4 max-w-5xl">
                <Button
                    variant="ghost"
                    onClick={() => navigate('/projects')}
                    className="text-zinc-500 hover:text-zinc-900 dark:hover:text-zinc-100 -ml-4"
                >
                    <ArrowLeft className="mr-2 h-4 w-4" aria-hidden="true" />
                    Voltar a Projetos
                </Button>
            </div>

            <main className="container mx-auto px-4 md:px-8 py-4 max-w-5xl flex-1 pb-24">

                <header className="flex flex-col gap-6 mb-12">
                    <div className="flex flex-col md:flex-row md:items-end justify-between gap-6">
                        <div className="flex-1 relative group">
                            <div className="flex items-center gap-3">
                                <input
                                    aria-label="Nome do projeto"
                                    value={projectName}
                                    onChange={(e) => setProjectName(e.target.value)}
                                    onFocus={() => setIsEditingName(true)}
                                    onBlur={handleNameBlur}
                                    onKeyDown={handleNameKeyDown}
                                    className="text-4xl md:text-5xl font-bold tracking-tight text-zinc-900 dark:text-zinc-50 bg-transparent border-none p-0 focus:outline-none focus:ring-0 w-full truncate cursor-text hover:bg-zinc-50 dark:hover:bg-zinc-800/30 rounded-lg px-2 -mx-2 transition-colors"
                                    placeholder="Nome do Projeto…"
                                />
                                {updateProject.isPending && (
                                    <Loader2 className="w-5 h-5 text-zinc-400 animate-spin shrink-0" aria-hidden="true" />
                                )}
                                <Pencil className="w-4 h-4 text-zinc-300 dark:text-zinc-600 opacity-0 group-hover:opacity-100 transition-opacity shrink-0 pointer-events-none" aria-hidden="true" />
                            </div>
                        </div>

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

                <div className="flex items-center justify-end gap-3 border-t border-zinc-100 dark:border-zinc-800/50 pt-6 mb-6">
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setSortDirection((d) => d === 'desc' ? 'asc' : 'desc')}
                        className="bg-zinc-50 dark:bg-zinc-900 rounded-full h-11 px-4 font-medium"
                    >
                        {sortDirection === 'desc' ? '↓ Recentes primeiro' : '↑ Antigos primeiro'}
                    </Button>
                    <Button
                        className="bg-emerald-50/80 text-emerald-700 hover:bg-emerald-100 hover:text-emerald-800 dark:bg-emerald-500/10 dark:text-emerald-400 dark:hover:bg-emerald-500/20 rounded-full h-11 px-4 shadow-sm border border-emerald-200/50 dark:border-emerald-800/50 font-medium"
                        onClick={() => setIsModalOpen(true)}
                    >
                        <Plus className="mr-2 h-4 w-4" aria-hidden="true" /> Adicionar
                    </Button>
                </div>

                <div className="w-full">
                    <TimeEntryList
                        entries={entries}
                        isReadOnly={false}
                        groupByDay={false}
                        showProjectBadge={false}
                        isLoading={isLoadingEntries && entries.length === 0}
                        onDelete={setPendingDeleteId}
                        onUpdate={handleUpdate}
                    />

                    {!isLoadingEntries && hasNextPage && entries.length > 0 && (
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

            {id && (
                <TimeEntryModal
                    isOpen={isModalOpen}
                    onClose={() => setIsModalOpen(false)}
                    projectId={id}
                    onSuccess={() => {
                        queryClient.invalidateQueries({ queryKey: ['timeEntries'] });
                        queryClient.invalidateQueries({ queryKey: projectKeys.summary(id) });
                    }}
                />
            )}
        </div>
    );
};
