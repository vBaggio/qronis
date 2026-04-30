import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../lib/api';
import { timeEntryKeys } from './useTimeEntries';
import type { TimeEntry } from '../lib/types';

export const timerKeys = {
    active: ['timer', 'active'] as const,
};

export function useActiveTimer() {
    return useQuery({
        queryKey: timerKeys.active,
        queryFn: async ({ signal }) => {
            // 204 No Content (no active timer) → data is empty, id is undefined → returns null
            const { data } = await api.get<TimeEntry>('/time-entries/active', { signal });
            return data?.id ? data : null;
        },
        staleTime: 1000 * 5,
        retry: false,
    });
}

export function useStartTimer() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ projectId, description }: { projectId: string; description: string }) =>
            api.post<TimeEntry>('/time-entries/start', { projectId, description }),
        onSuccess: (res) => {
            queryClient.setQueryData(timerKeys.active, res.data);
        },
    });
}

export function useStopTimer() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: () => api.put('/time-entries/stop'),
        onSuccess: () => {
            queryClient.setQueryData(timerKeys.active, null);
            queryClient.invalidateQueries({ queryKey: timeEntryKeys.all });
        },
    });
}
