import React from 'react';

interface Props {
    children: React.ReactNode;
    fallback?: React.ReactNode;
}

interface State {
    hasError: boolean;
    error: Error | null;
}

export class ErrorBoundary extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = { hasError: false, error: null };
    }

    static getDerivedStateFromError(error: Error): State {
        return { hasError: true, error };
    }

    componentDidCatch(error: Error, info: React.ErrorInfo) {
        console.error('[ErrorBoundary]', error, info.componentStack);
    }

    render() {
        if (this.state.hasError) {
            if (this.props.fallback) return this.props.fallback;

            return (
                <div className="flex min-h-screen flex-col items-center justify-center bg-white dark:bg-zinc-950 gap-4 p-8 text-center">
                    <p className="text-2xl font-semibold text-zinc-900 dark:text-zinc-50">
                        Algo deu errado
                    </p>
                    <p className="text-sm text-zinc-500 dark:text-zinc-400 max-w-sm">
                        Ocorreu um erro inesperado. Recarregue a página para continuar.
                    </p>
                    <button
                        type="button"
                        onClick={() => window.location.reload()}
                        className="mt-2 rounded-full bg-emerald-600 px-6 py-2 text-sm font-semibold text-white hover:bg-emerald-700 transition-colors"
                    >
                        Recarregar
                    </button>
                </div>
            );
        }

        return this.props.children;
    }
}
