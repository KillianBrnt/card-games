import type { LoginCredentials, RegisterCredentials, AuthResponse } from '../types/auth';

export const authService = {
    login: async (credentials: LoginCredentials): Promise<AuthResponse> => {
        const response = await fetch('/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(credentials),
        });

        if (!response.ok) {
            // Handle non-200 repsonses
            const errorText = await response.text();
            throw new Error(errorText || 'Login failed');
        }

        return response.json();
    },

    register: async (userData: RegisterCredentials): Promise<AuthResponse> => {
        const response = await fetch('/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Registration failed');
        }

        return response.json();
    },

    logout: async (): Promise<void> => {
        await fetch('/auth/logout', { method: 'POST' });
    }
};
