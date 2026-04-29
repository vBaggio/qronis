import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../lib/api';
import { PAGE_SIZE } from '../lib/constants';
import type { Project, PageResponse } from '../lib/types';

export const projectKeys = {
    all: ['projects'] as const,
    list: (page: number, search: string) => ['projects', 'list', page, search] as const,
    detail: (id: string) => ['projects', 'detail', id] as const,
    summary: (id: string) => ['projects', 'summary', id] as const,
};

export function useProjects({ page, search }: { page: number; search: string }) {
    return useQuery({
        queryKey: projectKeys.list(page, search),
        queryFn: async ({ signal }) => {
            const params: Record<string, unknown> = {
                page,
                size: PAGE_SIZE.projects,
                sort: 'createdAt,desc',
            };
            if (search) params.name = search;
            const { data } = await api.get<PageResponse<Project>>('/projects', { params, signal });
            return data;
        },
    });
}

export function useProject(id: string | undefined) {
    return useQuery({
        queryKey: projectKeys.detail(id!),
        queryFn: async ({ signal }) => {
            const { data } = await api.get<Project>(`/projects/${id}`, { signal });
            return data;
        },
        enabled: !!id && id !== 'undefined',
    });
}

export function useProjectSummary(id: string | undefined) {
    return useQuery({
        queryKey: projectKeys.summary(id!),
        queryFn: async ({ signal }) => {
            const { data } = await api.get<{ projectId: string; totalDurationSeconds: number }>(
                `/projects/${id}/summary`,
                { signal }
            );
            return data;
        },
        enabled: !!id && id !== 'undefined',
    });
}

export function useCreateProject() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (name: string) => api.post<Project>('/projects', { name }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: projectKeys.all });
        },
    });
}

export function useUpdateProject(id: string) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (name: string) => api.put<Project>(`/projects/${id}`, { name }),
        onSuccess: (res) => {
            queryClient.setQueryData(projectKeys.detail(id), res.data);
            queryClient.invalidateQueries({ queryKey: projectKeys.all });
        },
    });
}

export function useDeleteProject() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: string) => api.delete(`/projects/${id}`),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: projectKeys.all });
        },
    });
}
