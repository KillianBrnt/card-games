import React from 'react';
import { Layout, theme, Button, Dropdown, Space, Avatar } from 'antd';
import { UserOutlined, DownOutlined, LogoutOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import type { RootState } from '../../store';
import { logout } from '../../store/authSlice';
import { authService } from '../../services/authService';

const { Header, Content, Footer } = Layout;

const Home: React.FC = () => {
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();
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
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ display: 'flex', alignItems: 'center' }}>
        <div style={{ color: 'white', fontSize: 20, fontWeight: 'bold', marginRight: 40 }}>Card Games</div>
        <div style={{ marginLeft: 'auto', marginRight: 0 }}>
          {user ? (
            <Dropdown menu={{ items: userMenu }}>
              <a onClick={(e) => e.preventDefault()} style={{ color: 'white', cursor: 'pointer' }}>
                <Space>
                  <Avatar icon={<UserOutlined />} />
                  {user.username}
                  <DownOutlined />
                </Space>
              </a>
            </Dropdown>
          ) : (
            <Button ghost onClick={() => navigate('/login')}>Login</Button>
          )}
        </div>
      </Header>
      <Content style={{ padding: '0 48px', marginTop: 24 }}>
        <div
          style={{
            background: colorBgContainer,
            minHeight: 280,
            padding: 24,
            borderRadius: borderRadiusLG,
            textAlign: 'center'
          }}
        >
          <h1>Welcome to the Platform</h1>
          <p>This is the starting point of your card gaming journey.</p>
        </div>
      </Content>
      <Footer style={{ textAlign: 'center' }}>
        Card Games Platform ©{new Date().getFullYear()} Created by Killian
      </Footer>
    </Layout>
  );
};

export default Home;
