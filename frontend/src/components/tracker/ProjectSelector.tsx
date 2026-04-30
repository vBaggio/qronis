import React, { useState } from 'react';
import type { Project } from '@/lib/types';
import { cva } from 'class-variance-authority';

import { useProjects, useCreateProject } from '@/hooks/useProjects';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Folder, Plus, Check } from 'lucide-react';
import { cn } from '@/lib/utils';

const triggerVariants = cva(
    'w-full justify-between rounded-full bg-white dark:bg-zinc-900 border-zinc-200 dark:border-zinc-800 text-zinc-600 dark:text-zinc-400 shrink-0',
    {
        variants: {
            size: {
                default: 'h-14 md:h-16 px-6 sm:w-[250px]',
                compact: 'h-10 md:h-12 px-4 text-sm sm:w-[200px]',
            },
        },
        defaultVariants: { size: 'default' },
    }
);

interface ProjectSelectorProps {
    selectedProjectId: string | null;
    onSelect: (id: string | null) => void;
    disabled?: boolean;
    allowCreate?: boolean;
    size?: 'default' | 'compact';
}

export const ProjectSelector: React.FC<ProjectSelectorProps> = ({
    selectedProjectId,
    onSelect,
    disabled,
    allowCreate = true,
    size = 'default',
}) => {
    const [open, setOpen] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');

    const { data, isLoading } = useProjects({ page: 0, search: '' });
    const createProject = useCreateProject();

    const projects: Project[] = data?.content ?? [];
    const filteredProjects = projects.filter((p) =>
        p.name.toLowerCase().includes(searchQuery.toLowerCase())
    );
    const selectedProject = projects.find((p) => p.id === selectedProjectId);

    const handleCreateProject = () => {
        if (!searchQuery.trim()) return;
        createProject.mutate(searchQuery.trim(), {
            onSuccess: (res) => {
                onSelect(res.data.id);
                setOpen(false);
                setSearchQuery('');
            },
        });
    };

    return (
        <Popover open={open} onOpenChange={setOpen}>
            <PopoverTrigger asChild>
                <Button
                    variant="outline"
                    role="combobox"
                    aria-expanded={open}
                    aria-label="Selecionar projeto"
                    disabled={disabled}
                    className={cn(triggerVariants({ size }))}
                >
                    <div className="flex items-center gap-2 truncate">
                        <Folder className="h-4 w-4 shrink-0 text-emerald-600 dark:text-emerald-500" aria-hidden="true" />
                        <span className="truncate">
                            {selectedProject ? selectedProject.name : 'Selecione um projeto…'}
                        </span>
                    </div>
                </Button>
            </PopoverTrigger>
            <PopoverContent className="w-[300px] p-2" align="start">
                <div className="flex flex-col gap-2">
                    <Input
                        id="projectSearch"
                        name="projectSearch"
                        placeholder={allowCreate ? 'Buscar ou criar novo…' : 'Buscar projeto…'}
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="h-9 border-none bg-zinc-100 dark:bg-zinc-800 focus-visible:ring-0"
                    />
                    <div className="max-h-[200px] overflow-y-auto pt-2 flex flex-col gap-1">
                        {isLoading && (
                            <div className="text-sm text-center py-4 text-zinc-500">Carregando…</div>
                        )}
                        {!isLoading && filteredProjects.map((project) => (
                            <Button
                                key={project.id}
                                variant="ghost"
                                className="justify-start font-normal"
                                onClick={() => { onSelect(project.id); setOpen(false); }}
                            >
                                <Check
                                    className={cn('mr-2 h-4 w-4 text-emerald-600', selectedProjectId === project.id ? 'opacity-100' : 'opacity-0')}
                                    aria-hidden="true"
                                />
                                {project.name}
                            </Button>
                        ))}
                        {allowCreate && !isLoading && searchQuery.trim() && filteredProjects.length === 0 && (
                            <Button
                                variant="ghost"
                                className="justify-start text-emerald-600 dark:text-emerald-500 font-medium"
                                onClick={handleCreateProject}
                                disabled={createProject.isPending}
                            >
                                <Plus className="mr-2 h-4 w-4" aria-hidden="true" />
                                Criar projeto "{searchQuery}"
                            </Button>
                        )}
                        {!isLoading && !searchQuery.trim() && filteredProjects.length === 0 && (
                            <div className="text-sm text-center py-4 text-zinc-500">
                                Nenhum projeto encontrado.
                            </div>
                        )}
                    </div>
                </div>
            </PopoverContent>
        </Popover>
    );
};
