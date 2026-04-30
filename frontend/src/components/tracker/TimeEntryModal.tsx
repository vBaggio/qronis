import React, { useState } from 'react';
import axios from 'axios';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Loader2 } from 'lucide-react';
import { useCreateTimeEntry } from '@/hooks/useTimeEntries';

interface TimeEntryModalProps {
    isOpen: boolean;
    onClose: () => void;
    projectId: string;
    onSuccess: () => void;
}

export const TimeEntryModal: React.FC<TimeEntryModalProps> = ({ isOpen, onClose, projectId, onSuccess }) => {
    const today = new Date();
    today.setHours(9, 0, 0, 0);
    const defaultStart = new Date(today.getTime() - (today.getTimezoneOffset() * 60000)).toISOString().slice(0, 16);
    today.setHours(10, 0, 0, 0);
    const defaultEnd = new Date(today.getTime() - (today.getTimezoneOffset() * 60000)).toISOString().slice(0, 16);

    const [description, setDescription] = useState('');
    const [startTime, setStartTime] = useState(defaultStart);
    const [endTime, setEndTime] = useState(defaultEnd);
    const [error, setError] = useState<string | null>(null);

    const createTimeEntry = useCreateTimeEntry();

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        if (!startTime || !endTime) {
            setError('Preencha início e fim');
            return;
        }

        const start = new Date(startTime);
        const end = new Date(endTime);

        if (end <= start) {
            setError('O fim deve ser maior que o início');
            return;
        }

        createTimeEntry.mutate(
            {
                projectId,
                description: description.trim(),
                startTime: start.toISOString(),
                endTime: end.toISOString(),
            },
            {
                onSuccess: () => {
                    setDescription('');
                    onSuccess();
                    onClose();
                },
                onError: (err: unknown) => {
                    const message = axios.isAxiosError(err)
                        ? err.response?.data?.message
                        : undefined;
                    setError(message || 'Erro ao salvar o lançamento');
                },
            }
        );
    };

    return (
        <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
            <DialogContent className="sm:max-w-[425px]">
                <DialogHeader>
                    <DialogTitle>Novo Lançamento Retroativo</DialogTitle>
                </DialogHeader>

                <form onSubmit={handleSubmit} className="space-y-4 py-4">
                    <div className="space-y-1.5">
                        <Label htmlFor="description">O que foi feito?</Label>
                        <Input
                            id="description"
                            name="description"
                            placeholder="Descreva a atividade…"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            autoComplete="off"
                        />
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div className="space-y-1.5">
                            <Label htmlFor="start">Início</Label>
                            <Input
                                id="start"
                                type="datetime-local"
                                value={startTime}
                                onChange={(e) => setStartTime(e.target.value)}
                                required
                            />
                        </div>
                        <div className="space-y-1.5">
                            <Label htmlFor="end">Fim</Label>
                            <Input
                                id="end"
                                type="datetime-local"
                                value={endTime}
                                onChange={(e) => setEndTime(e.target.value)}
                                required
                            />
                        </div>
                    </div>

                    {error && (
                        <p className="text-sm font-medium text-destructive">{error}</p>
                    )}

                    <DialogFooter className="pt-4">
                        <Button type="button" variant="ghost" onClick={onClose} disabled={createTimeEntry.isPending}>
                            Cancelar
                        </Button>
                        <Button type="submit" disabled={createTimeEntry.isPending} className="bg-emerald-600 hover:bg-emerald-700 text-white">
                            {createTimeEntry.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" aria-hidden="true" />}
                            Salvar Lançamento
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
};
