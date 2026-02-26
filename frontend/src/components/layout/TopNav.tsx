import React from 'react';
import { useAuth } from '../../lib/auth-context';
import { Clock, Briefcase, LogOut } from 'lucide-react';
import { Button } from '../ui/button';

export const TopNav: React.FC = () => {
    const { user, logout } = useAuth();

    return (
        <header className="sticky top-0 z-50 w-full border-b border-zinc-200 bg-white dark:border-zinc-800 dark:bg-zinc-950">
            <div className="container mx-auto flex h-16 items-center justify-between px-4">
                {/* LOGO AREA */}
                <div className="flex items-center gap-2">
                    <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-emerald-100 dark:bg-emerald-900/30">
                        <Clock className="h-5 w-5 text-emerald-600 dark:text-emerald-400" />
                    </div>
                    <span className="text-xl font-bold tracking-tight text-zinc-900 dark:text-zinc-50">Qronis</span>
                </div>

                {/* NAVIGATION AREA */}
                <nav className="hidden md:flex items-center gap-6">
                    <Button variant="ghost" className="text-zinc-600 dark:text-zinc-400 font-medium hover:text-zinc-900 dark:hover:text-zinc-50">
                        <Clock className="mr-2 h-4 w-4" />
                        Timer
                    </Button>
                    <Button variant="ghost" className="text-zinc-600 dark:text-zinc-400 font-medium hover:text-zinc-900 dark:hover:text-zinc-50">
                        <Briefcase className="mr-2 h-4 w-4" />
                        Projetos
                    </Button>
                </nav>

                {/* USER PROFILE AREA */}
                <div className="flex items-center gap-4">
                    <div className="hidden sm:flex flex-col items-end">
                        <span className="text-sm font-medium text-zinc-900 dark:text-zinc-50">{user?.name || 'User'}</span>
                        <span className="text-xs text-zinc-500 dark:text-zinc-400">{user?.role || 'Membro'}</span>
                    </div>
                    <div className="flex h-9 w-9 items-center justify-center rounded-full bg-zinc-100 text-sm font-semibold text-zinc-900 dark:bg-zinc-800 dark:text-zinc-50">
                        {user?.name?.charAt(0).toUpperCase() || 'U'}
                    </div>
                    <Button variant="ghost" size="icon" onClick={logout} title="Sair" className="text-zinc-500 hover:text-red-600">
                        <LogOut className="h-5 w-5" />
                    </Button>
                </div>
            </div>
        </header>
    );
};
