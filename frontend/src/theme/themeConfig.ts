import type { ThemeConfig } from 'antd';
import { theme } from 'antd';

const { darkAlgorithm } = theme;

export const customTheme: ThemeConfig = {
    algorithm: darkAlgorithm,
    token: {
        colorPrimary: '#1CB5E0', // Cyan-like blue from JoinGame button gradient
        colorInfo: '#1CB5E0',
        colorSuccess: '#4ca1af',
        colorWarning: '#ffd700', // Gold color used for game codes
        colorError: '#ff6b6b',
        colorTextBase: '#ffffff',

        // Customizing generic text colors
        colorText: 'rgba(255, 255, 255, 0.95)',
        colorTextSecondary: 'rgba(255, 255, 255, 0.65)',

        // Backgrounds
        colorBgBase: '#1f1c2c', // Dark purple/blue base
        colorBgContainer: 'rgba(255, 255, 255, 0.1)', // Glassmorphic

        // Fonts
        fontFamily: "'Inter', system-ui, Avenir, Helvetica, Arial, sans-serif",

        // Borders
        borderRadius: 8,
    },
    components: {
        Button: {
            fontWeight: 600,
            primaryShadow: '0 4px 14px 0 rgba(28, 181, 224, 0.39)',
        },
        Card: {
            colorBgContainer: 'rgba(255, 255, 255, 0.05)',
            colorBorderSecondary: 'rgba(255, 255, 255, 0.15)',
        },
        Typography: {
            colorTextHeading: '#ffffff',
        },
        Input: {
            colorBgContainer: 'rgba(255, 255, 255, 0.1)',
            colorBorder: 'rgba(255, 255, 255, 0.3)',
            activeBorderColor: '#1CB5E0',
            hoverBorderColor: '#1CB5E0',
            colorTextPlaceholder: 'rgba(255, 255, 255, 0.5)',
            colorText: '#fff'
        }
    }
};
