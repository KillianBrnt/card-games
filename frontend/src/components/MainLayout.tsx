import React from 'react';
import { Layout, Dropdown, Space, Avatar } from 'antd';
import { UserOutlined, DownOutlined, LogoutOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import type { RootState } from '../store';
import { logout } from '../store/authSlice';
import { authService } from '../services/authService';

const { Header, Content, Footer } = Layout;

interface MainLayoutProps {
    children: React.ReactNode;
    background?: string;
}

const MainLayout: React.FC<MainLayoutProps> = ({ children, background }) => {
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const user = useSelector((state: RootState) => state.auth.user);

    const handleLogout = async () => {
        try {
            await authService.logout();
        } catch (error) {
            console.error("Logout failed", error);
        } finally {
            dispatch(logout());
            navigate('/login');
        }
    };

    const userMenu = [
        {
            key: '1',
            label: (
                <div onClick={handleLogout}>
                    <Space>
                        <LogoutOutlined /> Logout
                    </Space>
                </div>
            ),
        },
    ];

    return (
        <Layout style={{ height: '100vh', overflow: 'hidden', background: background || 'linear-gradient(135deg, #1f1c2c, #928dab)' }}>
            <Header style={{ display: 'flex', alignItems: 'center', background: 'rgba(0,0,0,0.3)', backdropFilter: 'blur(10px)', padding: '0 24px', height: '64px', flexShrink: 0 }}>
                <div
                    style={{ color: 'white', fontSize: 24, fontWeight: 'bold', marginRight: 40, fontFamily: 'Inter', cursor: 'pointer' }}
                    onClick={() => navigate('/')}
                >
                    Card Games
                </div>
                <div style={{ marginLeft: 'auto', marginRight: 0 }}>
                    {user && (
                        <Dropdown menu={{ items: userMenu }}>
                            <a onClick={(e) => e.preventDefault()} style={{ color: 'white', cursor: 'pointer' }}>
                                <Space>
                                    <Avatar icon={<UserOutlined />} style={{ backgroundColor: '#f56a00' }} />
                                    <span style={{ fontWeight: 500 }}>{user.username}</span>
                                    <DownOutlined />
                                </Space>
                            </a>
                        </Dropdown>
                    )}
                </div>
            </Header>
            <Content style={{
                padding: '0 24px',
                display: 'flex',
                flexDirection: 'column',
                overflow: 'hidden', // Let children handle scroll if needed
                flex: 1
            }}>
                {children}
            </Content>
            {/* Optional footer, maybe hide in game mode? keeping for consistency but minimizing impact */}
            <Footer style={{ textAlign: 'center', background: 'transparent', color: 'rgba(255,255,255,0.5)', padding: '5px 50px', flexShrink: 0 }}>
                Card Games Platform ©{new Date().getFullYear()} Created by Killian
            </Footer>
        </Layout>
    );
};

export default MainLayout;
