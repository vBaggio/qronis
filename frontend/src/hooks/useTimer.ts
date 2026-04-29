import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
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
            try {
                const { data } = await api.get<TimeEntry>('/time-entries/active', { signal });
                return data?.id ? data : null;
            } catch (error) {
                // 204 No Content = no active timer, not an error
                if (axios.isAxiosError(error) && error.response?.status === 204) {
                    return null;
                }
                throw error;
            }
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
