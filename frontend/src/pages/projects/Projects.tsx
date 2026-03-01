import React, { useState, useEffect, useCallback } from 'react';
import { api } from '@/lib/api';
import { TopNav } from '@/components/layout/TopNav';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table';
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

// ─── Types ────────────────────────────────────────────────────────────────────

interface Project {
    id: string;
    name: string;
    tenantId: string;
    createdByName: string;
    createdAt: string;
}

interface Page<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
}

// ─── Accent colors — border-left (Linear/Notion style) ───────────────────────
// Mapeados como hex para garantir compatibilidade com inline style.
// A cor é determinística: mesma cor sempre para o mesmo UUID de projeto.
const ACCENT_COLORS = [
    '#10b981', // emerald-500
    '#0ea5e9', // sky-500
    '#f59e0b', // amber-500
    '#f43f5e', // rose-500
    '#6366f1', // indigo-500
    '#f97316', // orange-500
    '#14b8a6', // teal-500
    '#d946ef', // fuchsia-500
];

function accentColorFor(id: string): string {
    const hash = id.slice(0, 8).split('').reduce((acc, c) => acc + c.charCodeAt(0), 0);
    return ACCENT_COLORS[hash % ACCENT_COLORS.length];
}

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
                        <AlertTriangle className="h-4 w-4 text-rose-600 dark:text-rose-400" />
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
                        {loading
                            ? <Loader2 className="h-4 w-4 animate-spin" />
                            : 'Excluir'}
                    </Button>
                </div>
            </div>
        </DialogContent>
    </Dialog>
);

// ─── New Project Dialog ────────────────────────────────────────────────────────

interface NewProjectDialogProps {
    onCreated: () => void;
}

const NewProjectDialog: React.FC<NewProjectDialogProps> = ({ onCreated }) => {
    const [open, setOpen] = useState(false);
    const [name, setName] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleCreate = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!name.trim()) return;
        setLoading(true);
        setError(null);
        try {
            await api.post('/projects', { name: name.trim() });
            setName('');
            setOpen(false);
            onCreated();
        } catch {
            setError('Não foi possível criar o projeto. Tente novamente.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Dialog open={open} onOpenChange={(o) => { setOpen(o); if (!o) { setName(''); setError(null); } }}>
            <DialogTrigger asChild>
                <Button
                    id="btn-new-project"
                    className="bg-emerald-50 text-emerald-700 hover:bg-emerald-100 hover:text-emerald-800 dark:bg-emerald-500/10 dark:text-emerald-400 dark:hover:bg-emerald-500/20 gap-2 shrink-0 rounded-full px-5 shadow-sm transition-colors border border-emerald-200 dark:border-emerald-800/50 w-full sm:w-auto"
                >
                    <Plus className="h-4 w-4" />
                    Novo Projeto
                </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-md">
                <DialogHeader>
                    <DialogTitle className="flex items-center gap-2 text-zinc-900 dark:text-zinc-50">
                        <Briefcase className="h-5 w-5 text-emerald-600" />
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
                            placeholder="Ex: Site Corporativo, App Mobile..."
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
                        <Button type="button" variant="ghost" onClick={() => setOpen(false)} disabled={loading}>
                            Cancelar
                        </Button>
                        <Button
                            type="submit"
                            id="btn-submit-project"
                            disabled={loading || !name.trim()}
                            className="bg-emerald-600 hover:bg-emerald-700 text-white min-w-[100px]"
                        >
                            {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : 'Criar'}
                        </Button>
                    </div>
                </form>
            </DialogContent>
        </Dialog>
    );
};

// ─── Projects Page ─────────────────────────────────────────────────────────────

const PAGE_SIZE = 15;

export const Projects: React.FC = () => {
    const [projects, setProjects] = useState<Project[]>([]);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [page, setPage] = useState(0);
    const [loading, setLoading] = useState(true);

    // Live search com debounce
    const [searchQuery, setSearchQuery] = useState('');
    const [debouncedSearch, setDebouncedSearch] = useState('');
    const isSearchPending = searchQuery !== debouncedSearch;

    const [projectToDelete, setProjectToDelete] = useState<Project | null>(null);
    const [deletingId, setDeletingId] = useState<string | null>(null);

    // Dispara debounce 400ms após o usuário parar de digitar
    useEffect(() => {
        const timer = setTimeout(() => {
            setDebouncedSearch(searchQuery);
            setPage(0);
        }, 400);
        return () => clearTimeout(timer);
    }, [searchQuery]);

    const fetchProjects = useCallback(async () => {
        setLoading(true);
        try {
            const params: Record<string, unknown> = {
                page,
                size: PAGE_SIZE,
                sort: 'createdAt,desc',
            };
            if (debouncedSearch) params.name = debouncedSearch;
            const { data } = await api.get<Page<Project>>('/projects', { params });
            setProjects(data.content);
            setTotalElements(data.totalElements);
            setTotalPages(data.totalPages);
        } catch {
            setProjects([]);
        } finally {
            setLoading(false);
        }
    }, [page, debouncedSearch]);

    useEffect(() => {
        fetchProjects();
    }, [fetchProjects]);

    const handleDeleteConfirm = async () => {
        if (!projectToDelete) return;
        setDeletingId(projectToDelete.id);
        try {
            await api.delete(`/projects/${projectToDelete.id}`);
            setProjectToDelete(null);
            fetchProjects();
        } catch {
            setProjectToDelete(null);
        } finally {
            setDeletingId(null);
        }
    };

    const formatDate = (iso: string) => {
        try {
            return format(new Date(iso), "dd 'de' MMM 'de' yyyy", { locale: ptBR });
        } catch {
            return iso;
        }
    };

    return (
        <div className="min-h-screen bg-zinc-50 dark:bg-zinc-950 flex flex-col">
            <TopNav />

            {/* Dialog de exclusão com confirmação visual */}
            <DeleteConfirmDialog
                project={projectToDelete}
                onConfirm={handleDeleteConfirm}
                onCancel={() => setProjectToDelete(null)}
                loading={deletingId !== null}
            />

            <main className="flex-1 container mx-auto px-4 py-8 max-w-5xl">

                {/* ── Unified Page Header & Search ── */}
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 mb-8">
                    <div className="flex-1">
                        <h1 className="text-2xl font-semibold tracking-tight text-zinc-900 dark:text-zinc-50">
                            Projetos
                        </h1>
                        <p className="text-sm text-zinc-500 dark:text-zinc-400 mt-1">
                            {totalElements > 0
                                ? `${totalElements} projeto${totalElements !== 1 ? 's' : ''} no workspace`
                                : 'Nenhum projeto criado ainda'}
                        </p>
                    </div>

                    <div className="flex flex-col sm:flex-row items-center gap-3 w-full md:w-auto">
                        <div className="relative w-full sm:w-64 md:w-80">
                            {isSearchPending || loading ? (
                                <Loader2 className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-zinc-400 animate-spin" />
                            ) : (
                                <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-zinc-400" />
                            )}
                            <Input
                                id="search-projects"
                                placeholder="Buscar projetos..."
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                className="pl-10 h-10 w-full rounded-full bg-white dark:bg-zinc-900 border-zinc-200 dark:border-zinc-800 focus-visible:ring-emerald-500 shadow-sm"
                            />
                        </div>
                        <div className="w-full sm:w-auto">
                            {(!(!loading && projects.length === 0 && !debouncedSearch)) && (
                                <NewProjectDialog onCreated={fetchProjects} />
                            )}
                        </div>
                    </div>
                </div>

                {/* ── Table ── */}
                <div className="rounded-xl border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900 overflow-hidden shadow-sm">
                    <Table>
                        <TableHeader>
                            <TableRow className="bg-zinc-50/80 dark:bg-zinc-800/50 hover:bg-zinc-50/80 dark:hover:bg-zinc-800/50 border-b border-zinc-200 dark:border-zinc-800">
                                <TableHead className="pl-6 w-[50%] text-sm font-medium text-zinc-500 dark:text-zinc-400">
                                    Projeto
                                </TableHead>
                                <TableHead className="text-sm font-medium text-zinc-500 dark:text-zinc-400">
                                    Data de criação
                                </TableHead>
                                <TableHead className="w-[80px] text-right pr-6"></TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {loading ? (
                                <TableRow>
                                    <TableCell colSpan={3} className="py-16 text-center">
                                        <div className="flex items-center justify-center gap-2 text-zinc-500 dark:text-zinc-400">
                                            <Loader2 className="h-5 w-5 animate-spin text-emerald-500" />
                                            <span className="text-sm">Carregando projetos...</span>
                                        </div>
                                    </TableCell>
                                </TableRow>
                            ) : projects.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={3} className="py-24 text-center">
                                        <div className="flex flex-col items-center gap-3 text-zinc-400 dark:text-zinc-600">
                                            <FolderOpen className="h-10 w-10 mb-1" />
                                            <p className="text-sm font-medium text-zinc-600 dark:text-zinc-300">Nenhum projeto encontrado</p>
                                            <p className="text-xs text-zinc-500 mb-2">
                                                {debouncedSearch
                                                    ? `Nenhum resultado para "${debouncedSearch}"`
                                                    : 'Crie seu primeiro projeto para começar a rastrear o tempo.'}
                                            </p>
                                            {!debouncedSearch && (
                                                <div className="mt-2">
                                                    <NewProjectDialog onCreated={fetchProjects} />
                                                </div>
                                            )}
                                        </div>
                                    </TableCell>
                                </TableRow>
                            ) : (
                                projects.map((project, index) => {
                                    const accentColor = accentColorFor(project.id);
                                    return (
                                        <TableRow
                                            key={project.id}
                                            className="group border-zinc-100 dark:border-zinc-800 transition-colors duration-150 hover:bg-zinc-50 dark:hover:bg-zinc-800/40 animate-in fade-in"
                                            style={{
                                                animationDelay: `${index * 40}ms`,
                                                animationFillMode: 'both',
                                                animationDuration: '300ms',
                                            }}
                                        >
                                            {/* Indicador pill color interno sutil */}
                                            <TableCell className="py-5 pl-6">
                                                <div className="flex items-center gap-3">
                                                    <span
                                                        className="h-2.5 w-2.5 shrink-0 rounded-full shadow-sm"
                                                        style={{ backgroundColor: accentColor }}
                                                    />
                                                    <span className="text-[15px] font-semibold text-zinc-900 dark:text-zinc-100 tracking-tight leading-none pt-[2px]">
                                                        {project.name}
                                                    </span>
                                                </div>
                                            </TableCell>

                                            <TableCell className="py-5 text-sm text-zinc-500 dark:text-zinc-400">
                                                {formatDate(project.createdAt)}
                                            </TableCell>

                                            <TableCell className="py-5 pr-6 text-right">
                                                <DropdownMenu>
                                                    <DropdownMenuTrigger asChild>
                                                        <Button
                                                            variant="ghost"
                                                            className="h-8 w-8 p-0 text-zinc-400 hover:text-zinc-600 dark:hover:text-zinc-300 transition-colors"
                                                        >
                                                            <span className="sr-only">Abrir menu</span>
                                                            <MoreVertical className="h-4 w-4" />
                                                        </Button>
                                                    </DropdownMenuTrigger>
                                                    <DropdownMenuContent align="end" className="w-[160px]">
                                                        <DropdownMenuItem
                                                            className="text-rose-600 focus:text-rose-600 focus:bg-rose-50 dark:focus:bg-rose-900/20 cursor-pointer transition-colors"
                                                            disabled={deletingId === project.id}
                                                            onClick={() => setProjectToDelete(project)}
                                                        >
                                                            {deletingId === project.id ? (
                                                                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                                            ) : (
                                                                <Trash2 className="mr-2 h-4 w-4" />
                                                            )}
                                                            Excluir
                                                        </DropdownMenuItem>
                                                    </DropdownMenuContent>
                                                </DropdownMenu>
                                            </TableCell>
                                        </TableRow>
                                    );
                                })
                            )}
                        </TableBody>
                    </Table>
                </div>

                {/* ── Pagination ── */}
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
                                disabled={page === 0 || loading}
                                onClick={() => setPage((p) => Math.max(0, p - 1))}
                                aria-label="Página anterior"
                            >
                                <ChevronLeft className="h-4 w-4" />
                            </Button>
                            <Button
                                id="btn-next-page"
                                variant="outline"
                                size="icon"
                                className="h-8 w-8"
                                disabled={page >= totalPages - 1 || loading}
                                onClick={() => setPage((p) => p + 1)}
                                aria-label="Próxima página"
                            >
                                <ChevronRight className="h-4 w-4" />
                            </Button>
                        </div>
                    </div>
                )}
            </main>
        </div>
    );
};
