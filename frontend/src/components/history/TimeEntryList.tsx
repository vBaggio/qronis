import React, { useMemo } from 'react';
import { TimeEntryRow, type TimeEntry } from './TimeEntryRow';
import { formatRelativeDate } from '../../lib/time-utils';

interface TimeEntryListProps {
    entries: TimeEntry[];
    isReadOnly?: boolean;
    groupByDay?: boolean;
    showProjectBadge?: boolean;
    onDelete?: (id: string) => void;
    onUpdate?: (updatedEntry: TimeEntry) => void;
    isLoading?: boolean;
}

export const TimeEntryList: React.FC<TimeEntryListProps> = ({
    entries,
    isReadOnly = false,
    groupByDay = false,
    showProjectBadge = true,
    onDelete,
    onUpdate,
    isLoading = false
}) => {

    if (isLoading && entries.length === 0) {
        return (
            <div className="flex justify-center items-center py-12">
                <div className="h-6 w-6 animate-spin rounded-full border-2 border-emerald-500 border-t-transparent"></div>
            </div>
        );
    }

    if (entries.length === 0) {
        return (
            <div className="py-16 text-center text-zinc-500 dark:text-zinc-400">
                <p>Nenhum registro de tempo encontrado.</p>
            </div>
        );
    }

    const content = useMemo(() => {
        if (!groupByDay) {
            return entries.map((entry) => (
                <TimeEntryRow
                    key={entry.id}
                    entry={entry}
                    isReadOnly={isReadOnly}
                    showDate={true}
                    showProjectBadge={showProjectBadge}
                    onDelete={onDelete}
                    onUpdate={onUpdate}
                />
            ));
        }

        const grouped = entries.reduce((acc, entry) => {
            const day = formatRelativeDate(entry.startTime);
            if (!acc[day]) acc[day] = [];
            acc[day].push(entry);
            return acc;
        }, {} as Record<string, TimeEntry[]>);

        // Calculate total seconds per day group
        const calcDayTotal = (dayEntries: TimeEntry[]): string => {
            const totalSeconds = dayEntries.reduce((sum, e) => {
                if (!e.startTime || !e.endTime) return sum;
                const start = new Date(e.startTime).getTime();
                const end = new Date(e.endTime).getTime();
                return sum + Math.max(0, Math.floor((end - start) / 1000));
            }, 0);

            if (totalSeconds < 60) return `${totalSeconds}s`;
            const minutes = Math.floor(totalSeconds / 60);
            if (minutes < 60) return `${minutes}m`;
            const hours = Math.floor(minutes / 60);
            const remainingMinutes = minutes % 60;
            return remainingMinutes > 0 ? `${hours}h ${remainingMinutes}m` : `${hours}h`;
        };

        return Object.entries(grouped).map(([day, dayEntries]) => (
            <div key={day} className="flex flex-col mb-12 last:mb-0">
                <div className="flex items-center gap-3 pl-4 mb-3">
                    <h3 className="text-[11px] font-bold text-zinc-400 dark:text-zinc-500 uppercase tracking-widest">{day}</h3>
                    <span className="text-[11px] font-medium text-zinc-400 dark:text-zinc-500">—</span>
                    <span className="text-[11px] font-semibold text-zinc-500 dark:text-zinc-400">{calcDayTotal(dayEntries)}</span>
                </div>
                <div className="flex flex-col">
                    {dayEntries.map((entry) => (
                        <TimeEntryRow
                            key={entry.id}
                            entry={entry}
                            isReadOnly={isReadOnly}
                            showDate={false}
                            showProjectBadge={showProjectBadge}
                            onDelete={onDelete}
                            onUpdate={onUpdate}
                        />
                    ))}
                </div>
            </div>
        ));
    }, [entries, groupByDay, isReadOnly, showProjectBadge, onDelete, onUpdate]);

    return (
        <div className="flex flex-col w-full">
            {content}
        </div>
    );
};
