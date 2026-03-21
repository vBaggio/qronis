import { format, formatDistanceToNow, differenceInSeconds, parseISO, isToday, isYesterday } from 'date-fns';
import { ptBR } from 'date-fns/locale';

/**
 * Retorna a duração formatada visualmente para a Badge Tipográfica do Histórico.
 */
export function formatSmartDuration(startTimeIso: string, endTimeIso: string | null): { value: string, weight: 'light' | 'normal' | 'bold' } {
    if (!startTimeIso) return { value: '--', weight: 'light' };

    const start = parseISO(startTimeIso);
    const end = endTimeIso ? parseISO(endTimeIso) : new Date();
    const diffSeconds = Math.max(0, differenceInSeconds(end, start));

    if (diffSeconds < 60) {
        return { value: `${diffSeconds}s`, weight: 'light' };
    }

    const minutes = Math.floor(diffSeconds / 60);

    if (minutes < 60) {
        return { value: `${minutes}m`, weight: 'normal' };
    }

    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;

    return { value: remainingMinutes > 0 ? `${hours}h ${remainingMinutes}m` : `${hours}h`, weight: 'bold' };
}

/**
 * Formata o timestamp de exibição esquerda da linha (ex: "14 Mar, 09:00 - 11:30")
 * Quando start e end estão em dias diferentes, exibe ambas as datas.
 */
export function formatTimeRange(startTimeIso: string, endTimeIso: string | null): string {
    if (!startTimeIso) return '';

    const start = parseISO(startTimeIso);
    const startDateStr = formatRelativeDate(start);
    const startStr = format(start, 'HH:mm');

    if (!endTimeIso) {
        return `${startDateStr}, ${startStr} - Agora`;
    }

    const end = parseISO(endTimeIso);
    const endStr = format(end, 'HH:mm');

    // Same calendar day → single date prefix
    if (format(start, 'yyyy-MM-dd') === format(end, 'yyyy-MM-dd')) {
        return `${startDateStr}, ${startStr} - ${endStr}`;
    }

    // Cross-day → show both dates
    const endDateStr = formatRelativeDate(end);
    return `${startDateStr}, ${startStr} - ${endDateStr}, ${endStr}`;
}

/**
 * Formata apenas o range de horário sem prefixo de dia (ex: "09:00 - 11:30")
 * Usado quando as entradas já estão agrupadas por dia (o header do grupo já mostra a data).
 */
export function formatTimeOnly(startTimeIso: string, endTimeIso: string | null): string {
    if (!startTimeIso) return '';

    const start = parseISO(startTimeIso);
    const startStr = format(start, 'HH:mm');
    const endStr = endTimeIso ? format(parseISO(endTimeIso), 'HH:mm') : 'Agora';

    return `${startStr} - ${endStr}`;
}

export function formatRelativeDate(dateInput: Date | string): string {
    const date = typeof dateInput === 'string' ? parseISO(dateInput) : dateInput;
    if (isToday(date)) return 'Hoje';
    if (isYesterday(date)) return 'Ontem';
    return format(date, 'dd MMM', { locale: ptBR });
}

export function formatTimeAgo(isoString: string): string {
    if (!isoString) return '';
    return formatDistanceToNow(parseISO(isoString), { addSuffix: true, locale: ptBR });
}
