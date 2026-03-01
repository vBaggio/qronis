import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { TOKEN_KEY, AUTH_EXPIRED_EVENT, api } from './api';

// Define the shape of our User based on the backend DTO
export interface User {
    id: string;
    email: string;
    name: string;
    tenantId: string;
    tenantName: string;
    role: string;
}

interface AuthContextType {
    user: User | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: (token: string) => Promise<void>;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    const logout = useCallback(() => {
        localStorage.removeItem(TOKEN_KEY);
        setUser(null);
    }, []);

    // Listen for 401 auth expired events from the Axios interceptor
    useEffect(() => {
        const handleExpired = () => logout();
        window.addEventListener(AUTH_EXPIRED_EVENT, handleExpired);
        return () => window.removeEventListener(AUTH_EXPIRED_EVENT, handleExpired);
    }, [logout]);

    // Check for token on mount and fetch user profile
    useEffect(() => {
        const initAuth = async () => {
            const token = localStorage.getItem(TOKEN_KEY);
            if (token) {
                try {
                    const { data } = await api.get('/users/me');
                    setUser(data);
                } catch (error) {
                    console.error('Failed to authenticate token during app load', error);
                    localStorage.removeItem(TOKEN_KEY);
                    setUser(null);
                }
            }
            setIsLoading(false);
        };

        initAuth();
    }, []);

    const login = async (token: string) => {
        localStorage.setItem(TOKEN_KEY, token);

        try {
            setIsLoading(true);
            const { data } = await api.get('/users/me');
            setUser(data);
        } catch (error) {
            console.error('Failed to fetch user after login', error);
            logout();
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <AuthContext.Provider value={{
            user,
            isAuthenticated: !!user,
            isLoading,
            login,
            logout
        }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};
