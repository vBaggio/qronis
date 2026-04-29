import React, { useState, useEffect } from 'react';
import { api } from '../../lib/api';
import type { Project } from '../../lib/types';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Folder, Plus, Check } from 'lucide-react';
import { cn } from '@/lib/utils';

export type { Project };

interface ProjectSelectorProps {
    selectedProjectId: string | null;
    onSelect: (id: string | null) => void;
    disabled?: boolean;
    allowCreate?: boolean;
    size?: 'default' | 'compact';
}

export const ProjectSelector: React.FC<ProjectSelectorProps> = ({ selectedProjectId, onSelect, disabled, allowCreate = true, size = 'default' }) => {
    const [open, setOpen] = useState(false);
    const [projects, setProjects] = useState<Project[]>([]);
    const [loading, setLoading] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedProject, setSelectedProject] = useState<Project | null>(null);

    // Fetch server-side com debounce de 300ms — evita query por tecla
    useEffect(() => {
        if (!open) return;
        const timer = setTimeout(() => fetchProjects(searchQuery), 300);
        return () => clearTimeout(timer);
    }, [open, searchQuery]);

    // Resolve o nome do projeto selecionado para exibição no trigger
    useEffect(() => {
        if (!selectedProjectId) { setSelectedProject(null); return; }
        const found = projects.find(p => p.id === selectedProjectId);
        if (found) setSelectedProject(found);
    }, [selectedProjectId, projects]);

    const fetchProjects = async (query: string) => {
        try {
            setLoading(true);
            const params = new URLSearchParams({ size: '20' });
            if (query.trim()) params.set('name', query.trim());
            const res = await api.get(`/projects?${params.toString()}`);
            setProjects(res.data.content || []);
        } catch (error) {
            console.error('Failed to fetch projects', error);
        } finally {
            setLoading(false);
        }
    };

    const handleCreateProject = async () => {
        if (!searchQuery.trim()) return;
        try {
            setLoading(true);
            const res = await api.post('/projects', { name: searchQuery.trim() });
            const newProject = res.data;
            setSelectedProject(newProject);
            onSelect(newProject.id);
            setOpen(false);
            setSearchQuery('');
        } catch (error) {
            console.error('Failed to create project', error);
        } finally {
            setLoading(false);
        }
    };

    const sizeClasses = size === 'compact'
        ? 'h-10 md:h-12 px-4 text-sm'
        : 'h-14 md:h-16 px-6';

    return (
        <Popover open={open} onOpenChange={setOpen}>
            <PopoverTrigger asChild>
                <Button
                    variant="outline"
                    role="combobox"
                    aria-expanded={open}
                    disabled={disabled}
                    className={`${sizeClasses} w-full sm:w-[250px] justify-between rounded-full bg-white dark:bg-zinc-900 border-zinc-200 dark:border-zinc-800 text-zinc-600 dark:text-zinc-400 shrink-0`}
                >
                    <div className="flex items-center gap-2 truncate">
                        <Folder className="h-4 w-4 shrink-0 text-emerald-600 dark:text-emerald-500" />
                        <span className="truncate">{selectedProject ? selectedProject.name : 'Selecione um projeto...'}</span>
                    </div>
                </Button>
            </PopoverTrigger>
            <PopoverContent className="w-[300px] p-2" align="start">
                <div className="flex flex-col gap-2">
                    <Input
                        id="projectSearch"
                        name="projectSearch"
                        placeholder={allowCreate ? "Buscar ou criar novo..." : "Buscar projeto..."}
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="h-9 border-none bg-zinc-100 dark:bg-zinc-800 focus-visible:ring-0"
                    />
                    <div className="max-h-[200px] overflow-y-auto pt-2 flex flex-col gap-1">
                        {loading && <div className="text-sm text-center py-4 text-zinc-500">Carregando...</div>}
                        {!loading && projects.map(project => (
                            <Button
                                key={project.id}
                                variant="ghost"
                                className="justify-start font-normal"
                                onClick={() => {
                                    onSelect(project.id);
                                    setOpen(false);
                                }}
                            >
                                <Check className={cn("mr-2 h-4 w-4 text-emerald-600", selectedProjectId === project.id ? "opacity-100" : "opacity-0")} />
                                {project.name}
                            </Button>
                        ))}
                        {allowCreate && !loading && searchQuery.trim() && projects.length === 0 && (
                            <Button
                                variant="ghost"
                                className="justify-start text-emerald-600 dark:text-emerald-500 font-medium"
                                onClick={handleCreateProject}
                            >
                                <Plus className="mr-2 h-4 w-4" />
                                Criar projeto "{searchQuery}"
                            </Button>
                        )}
                        {!loading && !searchQuery.trim() && projects.length === 0 && (
                            <div className="text-sm text-center py-4 text-zinc-500">Nenhum projeto encontrado.</div>
                        )}
                    </div>
                </div>
            </PopoverContent>
        </Popover>
    );
};
