import React, { useState, useEffect } from 'react';
import { formatSmartDuration, formatTimeRange, formatTimeOnly } from '../../lib/time-utils';
import { api } from '../../lib/api';
import { accentColorFor, accentBgFor } from '../../lib/colors';
import type { TimeEntry } from '../../lib/types';
import { Trash2, Loader2 } from 'lucide-react';
import { parseISO, format } from 'date-fns';
import { TimeEditDialog } from './TimeEditDialog';

export type { TimeEntry };

interface TimeEntryRowProps {
    entry: TimeEntry;
    isReadOnly?: boolean;
    showDate?: boolean;
    showProjectBadge?: boolean;
    onDelete?: (id: string) => void;
    onUpdate?: (updatedEntry: TimeEntry) => void;
}

// ─── Time helpers ─────────────────────────────────────────────────────────────

function isoToTimeInput(iso: string): string {
    return format(parseISO(iso), 'HH:mm');
}

function replaceTimeInIso(originalIso: string, newTime: string): string {
    const date = parseISO(originalIso);
    const [hours, minutes] = newTime.split(':').map(Number);
    date.setHours(hours, minutes, 0, 0);
    return date.toISOString();
}

export const TimeEntryRow: React.FC<TimeEntryRowProps> = ({
    entry,
    isReadOnly = false,
    showDate = true,
    showProjectBadge = true,
    onDelete,
    onUpdate
}) => {
    const [description, setDescription] = useState(entry.description || '');
    const [isSaving, setIsSaving] = useState(false);

    // Time edit dialog state
    const [isTimeDialogOpen, setIsTimeDialogOpen] = useState(false);
    const [editStartTime, setEditStartTime] = useState('');
    const [editEndTime, setEditEndTime] = useState('');
    const [isTimeSaving, setIsTimeSaving] = useState(false);

    useEffect(() => {
        setDescription(entry.description || '');
    }, [entry.description]);

    // ─── Description edit ─────────────────────────────────────────────────────

    const handleDescriptionBlur = async () => {
        if (isReadOnly) return;
        if (description.trim() === (entry.description || '').trim()) return;

        setIsSaving(true);
        try {
            if (onUpdate) onUpdate({ ...entry, description: description.trim() });
            await api.patch(`/time-entries/${entry.id}`, { description: description.trim() });
        } catch (error) {
            console.error('Failed to patch description', error);
            setDescription(entry.description || '');
            if (onUpdate) onUpdate({ ...entry });
        } finally {
            setIsSaving(false);
        }
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') e.currentTarget.blur();
        else if (e.key === 'Escape') {
            setDescription(entry.description || '');
            e.currentTarget.blur();
        }
    };

    // ─── Time edit dialog ─────────────────────────────────────────────────────

    const openTimeDialog = () => {
        if (isReadOnly) return;
        setEditStartTime(isoToTimeInput(entry.startTime));
        setEditEndTime(entry.endTime ? isoToTimeInput(entry.endTime) : '');
        setIsTimeDialogOpen(true);
    };

    const handleTimeSave = async () => {
        if (!editStartTime) return;

        const patch: Record<string, string> = {};
        const newStartIso = replaceTimeInIso(entry.startTime, editStartTime);
        if (newStartIso !== entry.startTime) patch.startTime = newStartIso;

        if (entry.endTime && editEndTime) {
            const newEndIso = replaceTimeInIso(entry.endTime, editEndTime);
            if (newEndIso !== entry.endTime) patch.endTime = newEndIso;
        }

        if (Object.keys(patch).length === 0) {
            setIsTimeDialogOpen(false);
            return;
        }

        setIsTimeSaving(true);
        try {
            const updatedEntry = { ...entry, ...patch };
            if (onUpdate) onUpdate(updatedEntry);
            await api.patch(`/time-entries/${entry.id}`, patch);
            setIsTimeDialogOpen(false);
        } catch (error) {
            console.error('Failed to patch time', error);
            if (onUpdate) onUpdate({ ...entry });
        } finally {
            setIsTimeSaving(false);
        }
    };

    const duration = formatSmartDuration(entry.startTime, entry.endTime);
    const timeRange = showDate
        ? formatTimeRange(entry.startTime, entry.endTime)
        : formatTimeOnly(entry.startTime, entry.endTime);

    const accentHex = entry.projectId ? accentColorFor(entry.projectId) : null;

    return (
        <>
            <div className="group grid grid-cols-[auto_1fr_auto] items-center gap-6 py-4 border-b border-zinc-100 dark:border-zinc-800/50 hover:bg-zinc-50/50 dark:hover:bg-zinc-900/50 transition-colors">

                {/* 1. Date & Time Range */}
                <div className="text-sm font-medium text-zinc-500 shrink-0 min-w-[100px]">
                    {isReadOnly ? (
                        timeRange
                    ) : (
                        <button
                            type="button"
                            onClick={openTimeDialog}
                            className="text-left text-sm font-medium text-zinc-500 hover:text-emerald-600 hover:bg-zinc-50 dark:hover:bg-zinc-800/30 rounded-sm px-1.5 -mx-1.5 py-0.5 transition-colors cursor-pointer"
                            aria-label={`Ajustar horário: ${timeRange}`}
                        >
                            {timeRange}
                        </button>
                    )}
                </div>

                {/* 2. Project Pill & Description */}
                <div className="flex items-center gap-3 min-w-0">
                    {showProjectBadge && (
                        entry.projectName && accentHex ? (
                            <span
                                className="shrink-0 inline-flex items-center px-2 py-0.5 rounded-sm text-xs font-semibold"
                                style={{ backgroundColor: accentBgFor(accentHex), color: accentHex }}
                            >
                                {entry.projectName}
                            </span>
                        ) : (
                            <span className="shrink-0 inline-flex items-center px-2 py-0.5 rounded-sm text-xs font-medium bg-zinc-100 text-zinc-500 dark:bg-zinc-800 dark:text-zinc-400">
                                Nenhum
                            </span>
                        )
                    )}

                    <div className="flex-1 relative min-w-0 flex items-center">
                        {isReadOnly ? (
                            <span className="text-zinc-900 dark:text-zinc-100 truncate text-base">
                                {description || <span className="text-zinc-500 italic">Sem descrição</span>}
                            </span>
                        ) : (
                            <input
                                type="text"
                                aria-label="Descrição do registro"
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                onBlur={handleDescriptionBlur}
                                onKeyDown={handleKeyDown}
                                placeholder="Adicionar descrição…"
                                className="w-full bg-transparent border-none p-0 m-0 text-base text-zinc-900 dark:text-zinc-100 placeholder:text-zinc-400 focus:outline-none focus:ring-1 focus:ring-emerald-500/50 rounded-sm truncate transition-shadow hover:bg-zinc-50 dark:hover:bg-zinc-800/30 px-1.5 -mx-1.5"
                            />
                        )}
                        {isSaving && (
                            <Loader2 className="absolute -right-6 w-3 h-3 text-zinc-400 animate-spin" aria-hidden="true" />
                        )}
                    </div>
                </div>

                {/* 3. Duration & Actions */}
                <div className="flex items-center justify-end gap-3 shrink-0 w-24">
                    {!isReadOnly && onDelete && (
                        <button
                            type="button"
                            onClick={() => onDelete(entry.id)}
                            aria-label="Excluir registro"
                            className="p-1.5 text-zinc-400 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-950/30 rounded-md transition-all opacity-0 group-hover:opacity-100 focus-visible:opacity-100 translate-x-2 group-hover:translate-x-0 focus-visible:translate-x-0"
                        >
                            <Trash2 className="w-4 h-4" aria-hidden="true" />
                        </button>
                    )}

                    <span className={`text-right w-16
                        ${duration.weight === 'light' ? 'text-zinc-400 font-normal' : ''}
                        ${duration.weight === 'normal' ? 'text-zinc-700 dark:text-zinc-300 font-medium' : ''}
                        ${duration.weight === 'bold' ? 'text-zinc-950 dark:text-white font-bold' : ''}
                    `}>
                        {duration.value}
                    </span>
                </div>
            </div>

            {!isReadOnly && (
                <TimeEditDialog
                    open={isTimeDialogOpen}
                    onOpenChange={setIsTimeDialogOpen}
                    hasEndTime={!!entry.endTime}
                    startTime={editStartTime}
                    endTime={editEndTime}
                    onStartTimeChange={setEditStartTime}
                    onEndTimeChange={setEditEndTime}
                    onSave={handleTimeSave}
                    isSaving={isTimeSaving}
                />
            )}
        </>
    );
};
