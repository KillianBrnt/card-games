import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import type { User } from '../types/auth';

interface AuthState {
    token: string | null;
    user: User | null;
}

const loadState = (): AuthState => {
    try {
        const serializedState = localStorage.getItem('authState');
        if (serializedState === null) {
            return { token: null, user: null };
        }
        return JSON.parse(serializedState);
    } catch (err) {
        return { token: null, user: null };
    }
};

const initialState: AuthState = loadState();

const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        setCredentials: (
            state,
            action: PayloadAction<{ token: string; user: User }>
        ) => {
            state.token = action.payload.token;
            state.user = action.payload.user;
            localStorage.setItem('authState', JSON.stringify(state));
        },
        logout: (state) => {
            state.token = null;
            state.user = null;
            localStorage.removeItem('authState');
        },
    },
});

export const { setCredentials, logout } = authSlice.actions;

export default authSlice.reducer;
