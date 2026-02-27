import React, { useState } from 'react';
import { useAuth } from '../../lib/auth-context';
import { Clock, Briefcase, LogOut, Menu, UserCircle } from 'lucide-react';
import { Button } from '../ui/button';
import { Link, useLocation } from 'react-router-dom';
import {
    Sheet,
    SheetContent,
    SheetHeader,
    SheetTitle,
    SheetTrigger,
} from '@/components/ui/sheet';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';

export const TopNav: React.FC = () => {
    const { user, logout } = useAuth();
    const location = useLocation();
    const [isOpen, setIsOpen] = useState(false);

    return (
        <header className="sticky top-0 z-50 w-full border-b border-zinc-200 bg-white dark:border-zinc-800 dark:bg-zinc-950">
            <div className="container mx-auto flex h-16 items-center justify-between px-4">

                {/* LEFT SIDE: Hamburger (Mobile) + Logo */}
                <div className="flex items-center gap-3">
                    {/* MOBILE HAMBURGER MENU */}
                    <Sheet open={isOpen} onOpenChange={setIsOpen}>
                        <SheetTrigger asChild>
                            <Button variant="ghost" size="icon" className="md:hidden text-zinc-600 hover:text-zinc-900 dark:text-zinc-400 dark:hover:text-zinc-50">
                                <Menu className="h-6 w-6" />
                                <span className="sr-only">Abrir menu</span>
                            </Button>
                        </SheetTrigger>
                        <SheetContent side="left" className="w-[300px] sm:w-[340px] bg-white dark:bg-zinc-950 p-6 flex flex-col">
                            <SheetHeader className="pb-6 border-b border-zinc-100 dark:border-zinc-800 text-left">
                                <SheetTitle className="flex items-center gap-2">
                                    <img src="/qronis_ext.svg" alt="Qronis" className="h-10 md:h-12 w-auto object-contain drop-shadow-sm" />
                                </SheetTitle>
                            </SheetHeader>
                            <nav className="flex-1 flex flex-col gap-2 pt-6">
                                <Button asChild variant={location.pathname === '/tracker' ? 'secondary' : 'ghost'} className="justify-start text-base h-12" onClick={() => setIsOpen(false)}>
                                    <Link to="/tracker">
                                        <Clock className="mr-3 h-5 w-5" />
                                        Timer
                                    </Link>
                                </Button>
                                <Button asChild variant={location.pathname === '/projects' ? 'secondary' : 'ghost'} className="justify-start text-base h-12" onClick={() => setIsOpen(false)}>
                                    <Link to="/projects">
                                        <Briefcase className="mr-3 h-5 w-5" />
                                        Projetos
                                    </Link>
                                </Button>
                            </nav>

                            {/* MOBILE USER PROFILE FOOTER */}
                            <div className="mt-auto border-t border-zinc-100 dark:border-zinc-800 pt-6 flex items-center justify-between">
                                <div className="flex items-center gap-3">
                                    <div className="flex h-10 w-10 items-center justify-center rounded-full bg-emerald-100 text-base font-bold text-emerald-900 dark:bg-emerald-900/40 dark:text-emerald-50">
                                        {user?.name?.charAt(0).toUpperCase() || <UserCircle className="h-5 w-5" />}
                                    </div>
                                    <div className="flex flex-col">
                                        <span className="text-sm font-semibold text-zinc-900 dark:text-zinc-50 truncate max-w-[150px]">{user?.name || 'Usuário'}</span>
                                        <span className="text-xs text-zinc-500 dark:text-zinc-400">{user?.email || 'admin@qronis.com'}</span>
                                    </div>
                                </div>
                                <Button variant="ghost" size="icon" onClick={logout} className="text-zinc-500 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20">
                                    <LogOut className="h-5 w-5" />
                                </Button>
                            </div>
                        </SheetContent>
                    </Sheet>

                    {/* DESKTOP LOGO */}
                    <div className="hidden md:flex items-center">
                        <img src="/qronis_ext.svg" alt="Qronis" className="h-9 md:h-11 w-auto object-contain drop-shadow-sm" />
                    </div>
                </div>

                {/* CENTER/DESKTOP MOBILE LOGO */}
                <div className="md:hidden absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2">
                    <img src="/qronis_ext.svg" alt="Qronis" className="h-11 w-auto object-contain drop-shadow-sm" />
                </div>


                {/* NAVIGATION AREA (Desktop Only) */}
                <nav className="hidden md:flex items-center gap-6">
                    <Button asChild variant={location.pathname === '/tracker' ? 'secondary' : 'ghost'} className="text-zinc-600 dark:text-zinc-400 font-medium hover:text-zinc-900 dark:hover:text-zinc-50">
                        <Link to="/tracker">
                            <Clock className="mr-2 h-4 w-4" />
                            Timer
                        </Link>
                    </Button>
                    <Button asChild variant={location.pathname === '/projects' ? 'secondary' : 'ghost'} className="text-zinc-600 dark:text-zinc-400 font-medium hover:text-zinc-900 dark:hover:text-zinc-50">
                        <Link to="/projects">
                            <Briefcase className="mr-2 h-4 w-4" />
                            Projetos
                        </Link>
                    </Button>
                </nav>

                {/* USER PROFILE AREA (Desktop Only Dropdown) */}
                <div className="hidden md:flex items-center gap-2">
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button variant="ghost" className="relative h-12 px-3 rounded-xl focus-visible:ring-1 focus-visible:ring-emerald-500">
                                <div className="flex items-center gap-3">
                                    <div className="flex flex-col items-end">
                                        <span className="text-sm font-medium text-zinc-900 dark:text-zinc-50 truncate max-w-[120px]">{user?.name || 'Usuário'}</span>
                                    </div>
                                    <div className="flex h-9 w-9 items-center justify-center rounded-full bg-emerald-100 text-sm font-bold text-emerald-900 dark:bg-emerald-900/40 dark:text-emerald-50">
                                        {user?.name?.charAt(0).toUpperCase() || <UserCircle className="h-5 w-5" />}
                                    </div>
                                </div>
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent className="w-56" align="end" forceMount>
                            <DropdownMenuLabel className="font-normal">
                                <div className="flex flex-col space-y-1">
                                    <p className="text-sm font-medium leading-none text-zinc-900 dark:text-zinc-50">{user?.name || 'Conta'}</p>
                                    <p className="text-xs leading-none text-zinc-500 dark:text-zinc-400">
                                        {user?.email || 'admin@qronis.com'}
                                    </p>
                                </div>
                            </DropdownMenuLabel>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem className="text-red-600 focus:text-red-600 cursor-pointer" onClick={logout}>
                                <LogOut className="mr-2 h-4 w-4" />
                                <span>Sair da Plataforma</span>
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>
            </div>
        </header>
    );
};
