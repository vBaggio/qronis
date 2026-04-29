import React, { useState, useDeferredValue } from 'react';
import { useNavigate } from 'react-router-dom';
import { accentColorFor } from '@/lib/colors';
import type { Project } from '@/lib/types';
import { useProjects, useDeleteProject, useCreateProject } from '@/hooks/useProjects';
import { TopNav } from '@/components/layout/TopNav';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from '@/components/ui/dialog';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import {
    Briefcase,
    Plus,
    Search,
    ChevronLeft,
    ChevronRight,
    Loader2,
    Trash2,
    FolderOpen,
    AlertTriangle,
    MoreVertical,
} from 'lucide-react';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';

// ─── Utilities ────────────────────────────────────────────────────────────────

const formatDate = (iso: string) => {
    try {
        return format(new Date(iso), "dd 'de' MMM 'de' yyyy", { locale: ptBR });
    } catch {
        return iso;
    }
};

// ─── Delete Confirm Dialog ────────────────────────────────────────────────────

interface DeleteConfirmDialogProps {
    project: Project | null;
    onConfirm: () => void;
    onCancel: () => void;
    loading: boolean;
}

const DeleteConfirmDialog: React.FC<DeleteConfirmDialogProps> = ({
    project, onConfirm, onCancel, loading,
}) => (
    <Dialog open={!!project} onOpenChange={(o) => { if (!o) onCancel(); }}>
        <DialogContent className="sm:max-w-sm">
            <DialogHeader>
                <DialogTitle className="flex items-center gap-2.5 text-zinc-900 dark:text-zinc-50">
                    <span className="flex h-8 w-8 items-center justify-center rounded-full bg-rose-100 dark:bg-rose-900/30 shrink-0">
                        <AlertTriangle className="h-4 w-4 text-rose-600 dark:text-rose-400" aria-hidden="true" />
                    </span>
                    Excluir Projeto
                </DialogTitle>
            </DialogHeader>
            <div className="flex flex-col gap-5 pt-1">
                <p className="text-sm text-zinc-600 dark:text-zinc-400 leading-relaxed">
                    Você está prestes a excluir permanentemente{' '}
                    <span className="font-semibold text-zinc-900 dark:text-zinc-100">
                        {project?.name}
                    </span>
                    . Esta ação não pode ser desfeita.
                </p>
                <div className="flex justify-end gap-2">
                    <Button variant="ghost" onClick={onCancel} disabled={loading}>
                        Cancelar
                    </Button>
                    <Button
                        id="btn-confirm-delete"
                        variant="destructive"
                        onClick={onConfirm}
                        disabled={loading}
                        className="min-w-[100px]"
                    >
                        {loading ? <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" /> : 'Excluir'}
                    </Button>
                </div>
            </div>
        </DialogContent>
    </Dialog>
);

// ─── New Project Dialog ───────────────────────────────────────────────────────

interface NewProjectDialogProps {
    onCreated: () => void;
}

const NewProjectDialog: React.FC<NewProjectDialogProps> = ({ onCreated }) => {
    const [open, setOpen] = useState(false);
    const [name, setName] = useState('');
    const [error, setError] = useState<string | null>(null);

    const createProject = useCreateProject();

    const handleCreate = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!name.trim()) return;
        setError(null);
        createProject.mutate(name.trim(), {
            onSuccess: () => {
                setName('');
                setOpen(false);
                onCreated();
            },
            onError: () => {
                setError('Não foi possível criar o projeto. Tente novamente.');
            },
        });
    };

    return (
        <Dialog open={open} onOpenChange={(o) => { setOpen(o); if (!o) { setName(''); setError(null); } }}>
            <DialogTrigger asChild>
                <Button
                    id="btn-new-project"
                    className="bg-emerald-50/80 text-emerald-700 hover:bg-emerald-100 hover:text-emerald-800 dark:bg-emerald-500/10 dark:text-emerald-400 dark:hover:bg-emerald-500/20 gap-2 shrink-0 rounded-full h-11 px-6 shadow-sm transition-all border border-emerald-200/50 dark:border-emerald-800/50 w-full sm:w-auto font-semibold text-base"
                >
                    <Plus className="h-5 w-5" aria-hidden="true" />
                    Novo Projeto
                </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-md">
                <DialogHeader>
                    <DialogTitle className="flex items-center gap-2 text-zinc-900 dark:text-zinc-50">
                        <Briefcase className="h-5 w-5 text-emerald-600" aria-hidden="true" />
                        Criar Novo Projeto
                    </DialogTitle>
                </DialogHeader>
                <form id="form-new-project" onSubmit={handleCreate} className="flex flex-col gap-4 pt-2">
                    <div className="flex flex-col gap-1.5">
                        <label htmlFor="project-name" className="text-sm font-medium text-zinc-700 dark:text-zinc-300">
                            Nome do Projeto
                        </label>
                        <Input
                            id="project-name"
                            placeholder="Ex: Site Corporativo, App Mobile…"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            autoFocus
                            maxLength={120}
                            className="h-11"
                        />
                        {error && (
                            <p className="text-sm text-rose-600 dark:text-rose-400">{error}</p>
                        )}
                    </div>
                    <div className="flex justify-end gap-2 pt-1">
                        <Button type="button" variant="ghost" onClick={() => setOpen(false)} disabled={createProject.isPending}>
                            Cancelar
                        </Button>
                        <Button
                            type="submit"
                            id="btn-submit-project"
                            disabled={createProject.isPending || !name.trim()}
                            className="bg-emerald-600 hover:bg-emerald-700 text-white min-w-[100px]"
                        >
                            {createProject.isPending ? <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" /> : 'Criar'}
                        </Button>
                    </div>
                </form>
            </DialogContent>
        </Dialog>
    );
};

// ─── Projects Page ────────────────────────────────────────────────────────────

export const Projects: React.FC = () => {
    const navigate = useNavigate();
    const [page, setPage] = useState(0);
    const [searchQuery, setSearchQuery] = useState('');
    const deferredSearch = useDeferredValue(searchQuery);
    const isSearchPending = searchQuery !== deferredSearch;

    const [projectToDelete, setProjectToDelete] = useState<Project | null>(null);

    const { data, isLoading, isError } = useProjects({ page, search: deferredSearch });
    const deleteProject = useDeleteProject();

    const projects = data?.content ?? [];
    const totalElements = data?.totalElements ?? 0;
    const totalPages = data?.totalPages ?? 0;

    const handleDeleteConfirm = () => {
        if (!projectToDelete) return;
        deleteProject.mutate(projectToDelete.id, {
            onSuccess: () => setProjectToDelete(null),
            onError: () => setProjectToDelete(null),
        });
    };

    return (
        <div className="min-h-screen bg-white dark:bg-zinc-950 flex flex-col">
            <TopNav />

            <DeleteConfirmDialog
                project={projectToDelete}
                onConfirm={handleDeleteConfirm}
                onCancel={() => setProjectToDelete(null)}
                loading={deleteProject.isPending}
            />

            <main className="flex-1 container mx-auto px-4 py-8 max-w-5xl">

                <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 mb-8">
                    <div className="flex-1">
                        <h1 className="text-4xl md:text-5xl font-bold tracking-tight text-zinc-900 dark:text-zinc-50">
                            Projetos
                        </h1>
                        <p className="text-lg text-zinc-500 dark:text-zinc-400 mt-1 font-medium">
                            {totalElements > 0
                                ? `${totalElements} projeto${totalElements !== 1 ? 's' : ''} no workspace`
                                : 'Nenhum projeto criado ainda'}
                        </p>
                    </div>

                    <div className="flex flex-col sm:flex-row items-center gap-4 w-full md:w-auto pt-2 md:pt-0">
                        <div className="relative w-full sm:w-72 md:w-80">
                            {isSearchPending || isLoading ? (
                                <Loader2 className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-zinc-400 animate-spin" aria-hidden="true" />
                            ) : (
                                <Search className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-zinc-400" aria-hidden="true" />
                            )}
                            <Input
                                id="search-projects"
                                placeholder="Buscar projetos…"
                                value={searchQuery}
                                onChange={(e) => { setSearchQuery(e.target.value); setPage(0); }}
                                className="pl-11 h-11 w-full rounded-full bg-white dark:bg-zinc-900/50 border-zinc-200/80 dark:border-zinc-800/80 focus-visible:ring-emerald-500/30 shadow-sm text-base transition-all"
                            />
                        </div>
                        <div className="w-full sm:w-auto">
                            {!(isLoading && projects.length === 0 && !deferredSearch) && (
                                <NewProjectDialog onCreated={() => setPage(0)} />
                            )}
                        </div>
                    </div>
                </div>

                {isError && (
                    <div className="mb-6 rounded-lg bg-red-50 dark:bg-red-900/30 p-4 text-sm text-red-600 dark:text-red-400">
                        Falha ao carregar os projetos. Tente recarregar a página.
                    </div>
                )}

                <div className="flex flex-col gap-2 w-full mt-2">
                    {isLoading ? (
                        <div className="py-16 flex flex-col items-center justify-center gap-2 text-zinc-500 dark:text-zinc-400">
                            <Loader2 className="h-5 w-5 animate-spin text-emerald-500" aria-hidden="true" />
                            <span className="text-sm">Carregando projetos…</span>
                        </div>
                    ) : projects.length === 0 ? (
                        <div className="py-24 flex flex-col items-center gap-3 text-zinc-400 dark:text-zinc-600">
                            <FolderOpen className="h-10 w-10 mb-1" aria-hidden="true" />
                            <p className="text-sm font-medium text-zinc-600 dark:text-zinc-300">Nenhum projeto encontrado</p>
                            <p className="text-xs text-zinc-500 mb-2">
                                {deferredSearch
                                    ? `Nenhum resultado para "${deferredSearch}"`
                                    : 'Crie seu primeiro projeto para começar a rastrear o tempo.'}
                            </p>
                            {!deferredSearch && (
                                <div className="mt-2">
                                    <NewProjectDialog onCreated={() => setPage(0)} />
                                </div>
                            )}
                        </div>
                    ) : (
                        projects.map((project, index) => {
                            const accentColor = accentColorFor(project.id);
                            return (
                                <button
                                    key={project.id}
                                    type="button"
                                    onClick={() => navigate(`/projects/${project.id}`)}
                                    onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') navigate(`/projects/${project.id}`); }}
                                    className="group text-left cursor-pointer relative flex items-center justify-between min-h-[56px] px-4 border-b border-zinc-100 dark:border-zinc-800/60 last:border-0 transition-all duration-200 hover:bg-zinc-100/50 dark:hover:bg-zinc-800/30 animate-in fade-in w-full"
                                    style={{
                                        animationDelay: `${index * 40}ms`,
                                        animationFillMode: 'both',
                                        animationDuration: '300ms',
                                    }}
                                >
                                    <div className="flex items-center gap-4 flex-1 overflow-hidden">
                                        <span
                                            className="h-2.5 w-2.5 shrink-0 rounded-full shadow-sm"
                                            style={{ backgroundColor: accentColor }}
                                            aria-hidden="true"
                                        />
                                        <span className="text-[15px] font-semibold text-zinc-900 dark:text-zinc-100 tracking-tight truncate">
                                            {project.name}
                                        </span>
                                    </div>

                                    <div className="flex items-center gap-6 justify-end shrink-0 pl-4 w-[200px]">
                                        <span className="text-sm font-medium text-zinc-400 dark:text-zinc-500 tracking-tight">
                                            {project.createdAt ? formatDate(project.createdAt) : '—'}
                                        </span>

                                        <div onClick={(e) => e.stopPropagation()} onKeyDown={(e) => e.stopPropagation()}>
                                            <DropdownMenu>
                                                <DropdownMenuTrigger asChild>
                                                    <Button
                                                        variant="ghost"
                                                        aria-label={`Opções de ${project.name}`}
                                                        className="h-8 w-8 p-0 text-zinc-300 hover:text-zinc-600 dark:text-zinc-600 dark:hover:text-zinc-300 opacity-0 group-hover:opacity-100 focus-visible:opacity-100 transition-opacity"
                                                    >
                                                        <MoreVertical className="h-4 w-4" aria-hidden="true" />
                                                    </Button>
                                                </DropdownMenuTrigger>
                                                <DropdownMenuContent align="end" className="w-[160px]">
                                                    <DropdownMenuItem
                                                        className="text-rose-600 focus:text-rose-600 focus:bg-rose-50 dark:focus:bg-rose-900/20 cursor-pointer transition-colors"
                                                        disabled={deleteProject.isPending && projectToDelete?.id === project.id}
                                                        onClick={() => setProjectToDelete(project)}
                                                    >
                                                        {deleteProject.isPending && projectToDelete?.id === project.id ? (
                                                            <Loader2 className="mr-2 h-4 w-4 animate-spin" aria-hidden="true" />
                                                        ) : (
                                                            <Trash2 className="mr-2 h-4 w-4" aria-hidden="true" />
                                                        )}
                                                        Excluir
                                                    </DropdownMenuItem>
                                                </DropdownMenuContent>
                                            </DropdownMenu>
                                        </div>
                                    </div>
                                </button>
                            );
                        })
                    )}
                </div>

                {totalPages > 1 && (
                    <div className="flex items-center justify-between mt-4 px-1">
                        <p className="text-xs text-zinc-500 dark:text-zinc-400">
                            Página {page + 1} de {totalPages}
                        </p>
                        <div className="flex items-center gap-2">
                            <Button
                                id="btn-prev-page"
                                variant="outline"
                                size="icon"
                                className="h-8 w-8"
                                disabled={page === 0 || isLoading}
                                onClick={() => setPage((p) => Math.max(0, p - 1))}
                                aria-label="Página anterior"
                            >
                                <ChevronLeft className="h-4 w-4" aria-hidden="true" />
                            </Button>
                            <Button
                                id="btn-next-page"
                                variant="outline"
                                size="icon"
                                className="h-8 w-8"
                                disabled={page >= totalPages - 1 || isLoading}
                                onClick={() => setPage((p) => p + 1)}
                                aria-label="Próxima página"
                            >
                                <ChevronRight className="h-4 w-4" aria-hidden="true" />
                            </Button>
                        </div>
                    </div>
                )}
            </main>
        </div>
    );
};
