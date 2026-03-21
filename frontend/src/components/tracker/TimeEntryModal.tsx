import React, { useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Loader2 } from 'lucide-react';
import { api } from '@/lib/api';

interface TimeEntryModalProps {
    isOpen: boolean;
    onClose: () => void;
    projectId: string;
    onSuccess: () => void;
}

export const TimeEntryModal: React.FC<TimeEntryModalProps> = ({ isOpen, onClose, projectId, onSuccess }) => {
    const [description, setDescription] = useState('');
    // Defaulting to today at 09:00 for start, and 10:00 for end as a convenience
    const today = new Date();
    today.setHours(9, 0, 0, 0);
    const [startTime, setStartTime] = useState(new Date(today.getTime() - (today.getTimezoneOffset() * 60000)).toISOString().slice(0, 16));

    today.setHours(10, 0, 0, 0);
    const [endTime, setEndTime] = useState(new Date(today.getTime() - (today.getTimezoneOffset() * 60000)).toISOString().slice(0, 16));

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
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

        setIsSubmitting(true);
        try {
            await api.post('/time-entries', {
                projectId,
                description: description.trim(),
                startTime: start.toISOString(),
                endTime: end.toISOString()
            });

            // Reset state
            setDescription('');
            onSuccess();
            onClose();
        } catch (err: any) {
            console.error('Failed to create manual entry:', err);
            setError(err.response?.data?.message || 'Erro ao salvar o lançamento');
        } finally {
            setIsSubmitting(false);
        }
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
                            placeholder="Descreva a atividade..."
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
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
                        <Button type="button" variant="ghost" onClick={onClose} disabled={isSubmitting}>
                            Cancelar
                        </Button>
                        <Button type="submit" disabled={isSubmitting} className="bg-emerald-600 hover:bg-emerald-700 text-white">
                            {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                            Salvar Lançamento
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
};
