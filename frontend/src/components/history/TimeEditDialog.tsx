import React from 'react';
import { Clock, Loader2 } from 'lucide-react';
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogFooter,
} from '../ui/dialog';
import { Button } from '../ui/button';

interface TimeEditDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    hasEndTime: boolean;
    startTime: string;
    endTime: string;
    onStartTimeChange: (v: string) => void;
    onEndTimeChange: (v: string) => void;
    onSave: () => void;
    isSaving: boolean;
}

export const TimeEditDialog: React.FC<TimeEditDialogProps> = ({
    open,
    onOpenChange,
    hasEndTime,
    startTime,
    endTime,
    onStartTimeChange,
    onEndTimeChange,
    onSave,
    isSaving,
}) => (
    <Dialog open={open} onOpenChange={onOpenChange}>
        <DialogContent className="sm:max-w-xs">
            <DialogHeader>
                <DialogTitle className="flex items-center gap-2 text-zinc-900 dark:text-zinc-50">
                    <Clock className="h-4 w-4 text-emerald-600" aria-hidden="true" />
                    Ajustar horário
                </DialogTitle>
            </DialogHeader>
            <div className="flex flex-col gap-4 pt-2">
                <div className="flex flex-col gap-1.5">
                    <label htmlFor="edit-start-time" className="text-xs font-medium text-zinc-500">
                        Início
                    </label>
                    <input
                        id="edit-start-time"
                        type="time"
                        value={startTime}
                        onChange={(e) => onStartTimeChange(e.target.value)}
                        className="h-11 rounded-lg border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900 px-3 text-base text-zinc-900 dark:text-zinc-100 focus:outline-none focus:ring-2 focus:ring-emerald-500/30"
                    />
                </div>
                {hasEndTime && (
                    <div className="flex flex-col gap-1.5">
                        <label htmlFor="edit-end-time" className="text-xs font-medium text-zinc-500">
                            Fim
                        </label>
                        <input
                            id="edit-end-time"
                            type="time"
                            value={endTime}
                            onChange={(e) => onEndTimeChange(e.target.value)}
                            className="h-11 rounded-lg border border-zinc-200 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900 px-3 text-base text-zinc-900 dark:text-zinc-100 focus:outline-none focus:ring-2 focus:ring-emerald-500/30"
                        />
                    </div>
                )}
            </div>
            <DialogFooter>
                <Button
                    variant="ghost"
                    className="rounded-full"
                    onClick={() => onOpenChange(false)}
                    disabled={isSaving}
                >
                    Cancelar
                </Button>
                <Button
                    onClick={onSave}
                    disabled={isSaving}
                    className="bg-emerald-600 hover:bg-emerald-700 text-white rounded-full min-w-[80px]"
                >
                    {isSaving ? <Loader2 className="h-4 w-4 animate-spin" aria-hidden="true" /> : 'Salvar'}
                </Button>
            </DialogFooter>
        </DialogContent>
    </Dialog>
);
