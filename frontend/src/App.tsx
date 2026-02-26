import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './lib/auth-context';
import { ProtectedRoute } from './components/layout/ProtectedRoute';
import { TopNav } from './components/layout/TopNav';
import { Login } from './pages/auth/Login';

// Placeholder for the main dashboard 
const DashboardPlaceholder = () => {
  return (
    <div className="min-h-screen bg-zinc-50 dark:bg-zinc-950">
      <TopNav />
      <main className="container mx-auto p-4 md:p-8">
        <div className="flex flex-col items-center justify-center space-y-4 pt-20">
          <div className="text-9xl font-bold tracking-tighter text-zinc-900 dark:text-zinc-50">
            00<span className="text-zinc-400 dark:text-zinc-600">:</span>00<span className="text-zinc-400 dark:text-zinc-600">:</span>00
          </div>
          <p className="text-xl text-zinc-500 dark:text-zinc-400">Zen Mode UI placeholder is ready.</p>
        </div>
      </main>
    </div>
  );
};

const AppRoutes = () => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-zinc-50 dark:bg-zinc-950">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-emerald-500 border-t-transparent"></div>
      </div>
    );
  }

  return (
    <Routes>
      <Route path="/login" element={isAuthenticated ? <Navigate to="/tracker" replace /> : <Login />} />
      {/* TODO: Add Register inside Public layout if needed */}

      <Route
        path="/tracker"
        element={
          <ProtectedRoute>
            <DashboardPlaceholder />
          </ProtectedRoute>
        }
      />

      <Route path="/" element={<Navigate to="/tracker" replace />} />
      <Route path="*" element={<Navigate to="/tracker" replace />} />
    </Routes>
  );
};

export const App = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
};

export default App;
