import { useInfiniteQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../lib/api';
import { PAGE_SIZE } from '../lib/constants';
import type { TimeEntry, PageResponse } from '../lib/types';

export const timeEntryKeys = {
    all: ['timeEntries'] as const,
    list: (filters: { projectId?: string | null; sort?: string }) =>
        ['timeEntries', 'list', filters] as const,
};

export function useTimeEntries({
    projectId,
    sort = 'startTime,desc',
}: {
    projectId?: string | null;
    sort?: string;
}) {
    return useInfiniteQuery({
        queryKey: timeEntryKeys.list({ projectId, sort }),
        queryFn: async ({ pageParam = 0, signal }) => {
            const params: Record<string, unknown> = {
                page: pageParam,
                size: PAGE_SIZE.entries,
                sort,
            };
            if (projectId) params.projectId = projectId;
            const { data } = await api.get<PageResponse<TimeEntry>>('/time-entries', {
                params,
                signal,
            });
            return data;
        },
        initialPageParam: 0,
        getNextPageParam: (lastPage) =>
            lastPage.last ? undefined : lastPage.number + 1,
    });
}

export function usePatchTimeEntry() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ id, patch }: { id: string; patch: Partial<TimeEntry> }) =>
            api.patch(`/time-entries/${id}`, patch),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: timeEntryKeys.all });
        },
    });
}

export function useDeleteTimeEntry() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: string) => api.delete(`/time-entries/${id}`),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: timeEntryKeys.all });
        },
    });
}
