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
    Briefcase,
    Plus,
    Search,
    ChevronLeft,
    ChevronRight,
    Loader2,
    Trash2,
    FolderOpen,
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

// ─── Accent colors palette ─────────────────────────────────────────────────────
// Deterministic: derived from the project id to keep color stable across renders.
const ACCENT_PALETTES = [
    { bg: 'bg-emerald-100 dark:bg-emerald-900/30', text: 'text-emerald-700 dark:text-emerald-300', dot: 'bg-emerald-500' },
    { bg: 'bg-sky-100 dark:bg-sky-900/30', text: 'text-sky-700 dark:text-sky-300', dot: 'bg-sky-500' },
    { bg: 'bg-amber-100 dark:bg-amber-900/30', text: 'text-amber-700 dark:text-amber-300', dot: 'bg-amber-500' },
    { bg: 'bg-rose-100 dark:bg-rose-900/30', text: 'text-rose-700 dark:text-rose-300', dot: 'bg-rose-500' },
    { bg: 'bg-indigo-100 dark:bg-indigo-900/30', text: 'text-indigo-700 dark:text-indigo-300', dot: 'bg-indigo-500' },
    { bg: 'bg-orange-100 dark:bg-orange-900/30', text: 'text-orange-700 dark:text-orange-300', dot: 'bg-orange-500' },
    { bg: 'bg-teal-100 dark:bg-teal-900/30', text: 'text-teal-700 dark:text-teal-300', dot: 'bg-teal-500' },
    { bg: 'bg-fuchsia-100 dark:bg-fuchsia-900/30', text: 'text-fuchsia-700 dark:text-fuchsia-300', dot: 'bg-fuchsia-500' },
];

function accentFor(id: string) {
    // Simple hash of first 8 chars of uuid to pick a stable palette index
    const hash = id.slice(0, 8).split('').reduce((acc, c) => acc + c.charCodeAt(0), 0);
    return ACCENT_PALETTES[hash % ACCENT_PALETTES.length];
}

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
                <Button id="btn-new-project" className="bg-emerald-600 hover:bg-emerald-700 text-white gap-2 shrink-0">
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
                        <Button
                            type="button"
                            variant="ghost"
                            onClick={() => setOpen(false)}
                            disabled={loading}
                        >
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
    const [search, setSearch] = useState('');
    const [searchInput, setSearchInput] = useState('');
    const [deletingId, setDeletingId] = useState<string | null>(null);

    const fetchProjects = useCallback(async () => {
        setLoading(true);
        try {
            const params: Record<string, unknown> = {
                page,
                size: PAGE_SIZE,
                sort: 'createdAt,desc',
            };
            const { data } = await api.get<Page<Project>>('/projects', { params });
            setProjects(data.content);
            setTotalElements(data.totalElements);
            setTotalPages(data.totalPages);
        } catch {
            setProjects([]);
        } finally {
            setLoading(false);
        }
    }, [page, search]); // eslint-disable-line react-hooks/exhaustive-deps

    useEffect(() => {
        fetchProjects();
    }, [fetchProjects]);

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        setSearch(searchInput);
        setPage(0);
    };

    const handleDelete = async (id: string, name: string) => {
        if (!window.confirm(`Excluir o projeto "${name}"? Esta ação não pode ser desfeita.`)) return;
        setDeletingId(id);
        try {
            await api.delete(`/projects/${id}`);
            fetchProjects();
        } catch {
            alert('Não foi possível excluir o projeto.');
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

            <main className="flex-1 container mx-auto px-4 py-8 max-w-5xl">

                {/* ── Page Header ── */}
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
                    <div>
                        <h1 className="text-2xl font-bold tracking-tight text-zinc-900 dark:text-zinc-50">
                            Projetos
                        </h1>
                        <p className="text-sm text-zinc-500 dark:text-zinc-400 mt-1">
                            {totalElements > 0
                                ? `${totalElements} projeto${totalElements !== 1 ? 's' : ''} no workspace`
                                : 'Nenhum projeto criado ainda'}
                        </p>
                    </div>
                    <NewProjectDialog onCreated={fetchProjects} />
                </div>

                {/* ── Search Bar ── */}
                <form onSubmit={handleSearch} className="flex gap-2 mb-6">
                    <div className="relative flex-1 max-w-sm">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-zinc-400" />
                        <Input
                            id="search-projects"
                            placeholder="Buscar projetos..."
                            value={searchInput}
                            onChange={(e) => setSearchInput(e.target.value)}
                            className="pl-9 h-10"
                        />
                    </div>
                    <Button type="submit" variant="outline" className="h-10">Buscar</Button>
                </form>

                {/* ── Table ── */}
                <div className="rounded-xl border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900 overflow-hidden shadow-sm">
                    <Table>
                        <TableHeader>
                            <TableRow className="bg-zinc-50/80 dark:bg-zinc-800/50 hover:bg-zinc-50/80 dark:hover:bg-zinc-800/50">
                                <TableHead className="pl-5 w-[40%] text-xs uppercase tracking-wide text-zinc-500 dark:text-zinc-400 font-semibold">
                                    Projeto
                                </TableHead>
                                <TableHead className="text-xs uppercase tracking-wide text-zinc-500 dark:text-zinc-400 font-semibold">
                                    Criado por
                                </TableHead>
                                <TableHead className="text-xs uppercase tracking-wide text-zinc-500 dark:text-zinc-400 font-semibold">
                                    Data de criação
                                </TableHead>
                                <TableHead className="text-right pr-5 text-xs uppercase tracking-wide text-zinc-500 dark:text-zinc-400 font-semibold">
                                    Ações
                                </TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {loading ? (
                                <TableRow>
                                    <TableCell colSpan={4} className="py-16 text-center">
                                        <div className="flex items-center justify-center gap-2 text-zinc-500 dark:text-zinc-400">
                                            <Loader2 className="h-5 w-5 animate-spin text-emerald-500" />
                                            <span className="text-sm">Carregando projetos...</span>
                                        </div>
                                    </TableCell>
                                </TableRow>
                            ) : projects.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={4} className="py-20 text-center">
                                        <div className="flex flex-col items-center gap-3 text-zinc-400 dark:text-zinc-600">
                                            <FolderOpen className="h-10 w-10" />
                                            <p className="text-sm font-medium">Nenhum projeto encontrado</p>
                                            <p className="text-xs">Crie seu primeiro projeto usando o botão acima.</p>
                                        </div>
                                    </TableCell>
                                </TableRow>
                            ) : (
                                projects.map((project) => {
                                    const accent = accentFor(project.id);
                                    return (
                                        <TableRow
                                            key={project.id}
                                            className="group border-zinc-100 dark:border-zinc-800 transition-colors"
                                        >
                                            {/* Project Name with Accent Badge */}
                                            <TableCell className="pl-5 py-4">
                                                <div className="flex items-center gap-3">
                                                    <span className={`inline-block h-2.5 w-2.5 rounded-full shrink-0 ${accent.dot}`} />
                                                    <span className={`
                                                        inline-flex items-center rounded-md px-2.5 py-1
                                                        text-sm font-semibold tracking-tight
                                                        ${accent.bg} ${accent.text}
                                                    `}>
                                                        {project.name}
                                                    </span>
                                                </div>
                                            </TableCell>

                                            {/* Creator */}
                                            <TableCell className="py-4 text-sm text-zinc-600 dark:text-zinc-300">
                                                {project.createdByName}
                                            </TableCell>

                                            {/* Created At */}
                                            <TableCell className="py-4 text-sm text-zinc-500 dark:text-zinc-400">
                                                {formatDate(project.createdAt)}
                                            </TableCell>

                                            {/* Actions */}
                                            <TableCell className="py-4 pr-5 text-right">
                                                <Button
                                                    id={`btn-delete-${project.id}`}
                                                    variant="ghost"
                                                    size="icon"
                                                    disabled={deletingId === project.id}
                                                    onClick={() => handleDelete(project.id, project.name)}
                                                    className="opacity-0 group-hover:opacity-100 transition-opacity h-8 w-8 text-zinc-400 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-900/20"
                                                    title="Excluir projeto"
                                                >
                                                    {deletingId === project.id
                                                        ? <Loader2 className="h-4 w-4 animate-spin" />
                                                        : <Trash2 className="h-4 w-4" />
                                                    }
                                                </Button>
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
