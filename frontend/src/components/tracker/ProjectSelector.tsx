import React, { useState, useEffect } from 'react';
import { api } from '../../lib/api';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Folder, Plus, Check } from 'lucide-react';
import { cn } from '@/lib/utils';

export interface Project {
    id: string;
    name: string;
}

interface ProjectSelectorProps {
    selectedProjectId: string | null;
    onSelect: (id: string | null) => void;
    disabled?: boolean;
}

export const ProjectSelector: React.FC<ProjectSelectorProps> = ({ selectedProjectId, onSelect, disabled }) => {
    const [open, setOpen] = useState(false);
    const [projects, setProjects] = useState<Project[]>([]);
    const [loading, setLoading] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');

    useEffect(() => {
        if (open) {
            fetchProjects();
        }
    }, [open]);

    const fetchProjects = async () => {
        try {
            setLoading(true);
            const res = await api.get('/projects?size=100');
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
            setProjects(prev => [newProject, ...prev]);
            onSelect(newProject.id);
            setOpen(false);
            setSearchQuery('');
        } catch (error) {
            console.error('Failed to create project', error);
        } finally {
            setLoading(false);
        }
    };

    const filteredProjects = projects.filter(p => p.name.toLowerCase().includes(searchQuery.toLowerCase()));

    const selectedProject = projects.find(p => p.id === selectedProjectId);

    return (
        <Popover open={open} onOpenChange={setOpen}>
            <PopoverTrigger asChild>
                <Button
                    variant="outline"
                    role="combobox"
                    aria-expanded={open}
                    disabled={disabled}
                    className="h-14 md:h-16 px-6 w-full sm:w-[250px] justify-between rounded-full bg-white dark:bg-zinc-900 border-zinc-200 dark:border-zinc-800 text-zinc-600 dark:text-zinc-400 shrink-0"
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
                        placeholder="Buscar ou criar novo..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="h-9 border-none bg-zinc-100 dark:bg-zinc-800 focus-visible:ring-0"
                    />
                    <div className="max-h-[200px] overflow-y-auto pt-2 flex flex-col gap-1">
                        {loading && <div className="text-sm text-center py-4 text-zinc-500">Carregando...</div>}
                        {!loading && filteredProjects.map(project => (
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
                        {!loading && searchQuery.trim() && filteredProjects.length === 0 && (
                            <Button
                                variant="ghost"
                                className="justify-start text-emerald-600 dark:text-emerald-500 font-medium"
                                onClick={handleCreateProject}
                            >
                                <Plus className="mr-2 h-4 w-4" />
                                Criar projeto "{searchQuery}"
                            </Button>
                        )}
                        {!loading && !searchQuery.trim() && filteredProjects.length === 0 && (
                            <div className="text-sm text-center py-4 text-zinc-500">Nenhum projeto encontrado.</div>
                        )}
                    </div>
                </div>
            </PopoverContent>
        </Popover>
    );
};
