import axios from 'axios';
import type { InternalAxiosRequestConfig, AxiosError, AxiosResponse } from 'axios';

// A URL base deve apontar para o backend Spring Boot local durante o dev
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
const AUTH_URL = import.meta.env.VITE_AUTH_URL || 'http://localhost:8080/auth';

// Instância principal para rotas protegidas
export const api = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Instância para rotas públicas (Login/Register)
export const authApi = axios.create({
    baseURL: AUTH_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Chave para armazenar o token no LocalStorage
export const TOKEN_KEY = 'qronis_auth_token';

// Interceptor para injetar o JWT nas requisições protegidas
api.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const token = localStorage.getItem(TOKEN_KEY);
        if (token && config.headers) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error: AxiosError) => {
        return Promise.reject(error);
    }
);

// Interceptor para tratar respostas e erros de autorização globais
api.interceptors.response.use(
    (response: AxiosResponse) => {
        return response;
    },
    (error: AxiosError) => {
        // Se o token expirou ou for inválido, limpa e redireciona (401 Unauthorized)
        if (error.response && error.response.status === 401) {
            console.warn('Sessão expirada ou token inválido. Redirecionando para login.');
            localStorage.removeItem(TOKEN_KEY);
            // O React Router no AppShell ouvirá essa mudança se integrarmos com contexto
            // mas como fallback bruto, podemos forçar loading da página:
            window.location.href = '/login';
        }

        // Devolve o erro para ser tratado no nível do componente ou hook
        return Promise.reject(error);
    }
);
