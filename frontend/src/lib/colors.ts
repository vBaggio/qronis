export const ACCENT_COLORS = [
    '#10b981', // emerald-500
    '#0ea5e9', // sky-500
    '#f59e0b', // amber-500
    '#f43f5e', // rose-500
    '#6366f1', // indigo-500
    '#f97316', // orange-500
    '#14b8a6', // teal-500
    '#d946ef', // fuchsia-500
];

export function accentColorFor(id: string): string {
    const hash = id.slice(0, 8).split('').reduce((acc, c) => acc + c.charCodeAt(0), 0);
    return ACCENT_COLORS[hash % ACCENT_COLORS.length];
}

export function accentBgFor(hex: string): string {
    return `${hex}1A`;
}
