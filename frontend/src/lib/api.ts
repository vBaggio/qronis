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

// Evento customizado para sinalizar expiração de sessão ao AuthContext
export const AUTH_EXPIRED_EVENT = 'auth:expired';

// Interceptor para tratar respostas e erros de autorização globais
api.interceptors.response.use(
    (response: AxiosResponse) => response,
    (error: AxiosError) => {
        if (error.response?.status === 401) {
            localStorage.removeItem(TOKEN_KEY);
            window.dispatchEvent(new CustomEvent(AUTH_EXPIRED_EVENT));
        }
        return Promise.reject(error);
    }
);
