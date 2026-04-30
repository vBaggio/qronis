export interface User {
    id: string;
    email: string;
    name: string;
    tenantId: string;
    tenantName: string;
    role: string;
}

export interface Project {
    id: string;
    name: string;
    tenantId?: string;
    createdAt?: string;
}

export interface ProjectSummary {
    projectId: string;
    totalDurationSeconds: number;
}

export interface TimeEntry {
    id: string;
    description: string;
    startTime: string;
    endTime: string | null;
    projectId: string | null;
    projectName: string | null;
    createdAt?: string;
}

export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
}
