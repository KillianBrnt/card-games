export interface User {
    id: number;
    username: string;
    email: string;
}

export interface AuthResponse extends User {
    token?: string;
}

export interface LoginCredentials {
    email?: string;
    password?: string;
}

export interface RegisterCredentials {
    username: string;
    email: string;
    password?: string;
}
