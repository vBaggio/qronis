import React, { useState, useEffect } from 'react';

interface TimerDisplayProps {
    isActive: boolean;
    startTime: string | null;
}

export const TimerDisplay: React.FC<TimerDisplayProps> = ({ isActive, startTime }) => {
    // We only use state for visual updates, the math is absolute based on startTime
    const [elapsedSeconds, setElapsedSeconds] = useState(0);

    useEffect(() => {
        let interval: ReturnType<typeof setInterval>;

        if (isActive && startTime) {
            const startMs = new Date(startTime).getTime();

            // Immediate update to avoid 1-second delay
            setElapsedSeconds(Math.max(0, Math.floor((Date.now() - startMs) / 1000)));

            interval = setInterval(() => {
                setElapsedSeconds(Math.max(0, Math.floor((Date.now() - startMs) / 1000)));
            }, 1000);
        } else if (!isActive) {
            setElapsedSeconds(0);
        }

        return () => {
            if (interval) clearInterval(interval);
        };
    }, [isActive, startTime]);

    const formatTime = (totalSeconds: number) => {
        const h = Math.floor(totalSeconds / 3600);
        const m = Math.floor((totalSeconds % 3600) / 60);
        const s = totalSeconds % 60;

        return {
            hours: h.toString().padStart(2, '0'),
            minutes: m.toString().padStart(2, '0'),
            seconds: s.toString().padStart(2, '0')
        };
    };

    const time = formatTime(elapsedSeconds);

    return (
        <div className="font-mono text-[6rem] sm:text-8xl md:text-[10rem] lg:text-[12rem] leading-none tracking-tighter text-zinc-900 dark:text-zinc-50 transition-all duration-700 tabular-nums">
            {elapsedSeconds >= 3600 && (
                <>
                    <span>{time.hours}</span>
                    <span className="text-zinc-300 dark:text-zinc-700 transition-opacity">:</span>
                </>
            )}
            <span>{time.minutes}</span>
            <span className="text-zinc-300 dark:text-zinc-700 transition-opacity">:</span>
            <span>{time.seconds}</span>
        </div>
    );
};
