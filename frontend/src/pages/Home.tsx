import React from 'react';
import { Layout, Menu, Breadcrumb, theme, Button } from 'antd';
import { useNavigate } from 'react-router-dom';

const { Header, Content, Footer } = Layout;

const Home: React.FC = () => {
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();
  const navigate = useNavigate();

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ display: 'flex', alignItems: 'center' }}>
        <div style={{ color: 'white', fontSize: 20, fontWeight: 'bold', marginRight: 40 }}>Card Games</div>
        <Menu
          theme="dark"
          mode="horizontal"
          defaultSelectedKeys={['1']}
          items={[{ key: '1', label: 'Home' }, { key: '2', label: 'Games' }, { key: '3', label: 'Profile' }]}
          style={{ flex: 1, minWidth: 0 }}
        />
        <Button ghost onClick={() => navigate('/login')}>Login</Button>
      </Header>
      <Content style={{ padding: '0 48px', marginTop: 24 }}>
        <Breadcrumb style={{ margin: '16px 0' }}>
          <Breadcrumb.Item>Home</Breadcrumb.Item>
          <Breadcrumb.Item>Dashboard</Breadcrumb.Item>
        </Breadcrumb>
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
