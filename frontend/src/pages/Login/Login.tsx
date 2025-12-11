import React from 'react';
import { Form, Input, Button, Card, Typography, message } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useNavigate, Link } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { setCredentials } from '../../store/authSlice';
import { authService } from '../../services/authService';
import type { LoginCredentials } from '../../types/auth';

const { Title } = Typography;

const Login: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const onFinish = async (values: LoginCredentials) => {
    try {
      const data = await authService.login(values);
      message.success('Login successful!');

      // Store token and user data in Redux
      if (data.token) {
        dispatch(setCredentials({ token: data.token, user: data }));
      } else {
        // Fallback if token is missing
        dispatch(setCredentials({ token: '', user: data }));
      }

      navigate('/');
    } catch (error: unknown) {
      console.error(error);
      if (error instanceof Error) {
        message.error(error.message || 'Login failed: Invalid credentials');
      } else {
        message.error('An error occurred during login');
      }
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#f0f2f5' }}>
      <Card style={{ width: 400 }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={2}>Welcome Back</Title>
          <Typography.Text type="secondary">Please sign in to continue</Typography.Text>
        </div>

        <Form
          name="login"
          initialValues={{ remember: true }}
          onFinish={onFinish}
          layout="vertical"
          size="large"
        >
          <Form.Item
            name="email"
            rules={[{ required: true, message: 'Please input your Email!' }, { type: 'email', message: 'Please enter a valid email!' }]}
          >
            <Input prefix={<UserOutlined />} placeholder="Email" />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: 'Please input your Password!' }]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="Password" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" block>
              Log in
            </Button>
          </Form.Item>

          <div style={{ textAlign: 'center' }}>
            Don't have an account? <Link to="/signup">Sign up now</Link>
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default Login;
