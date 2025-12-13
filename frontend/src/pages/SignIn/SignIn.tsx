import React from 'react';
import { Form, Input, Button, Card, Typography, message } from 'antd';
import { UserOutlined, MailOutlined, LockOutlined } from '@ant-design/icons';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '../../services/authService';
import type { RegisterCredentials } from '../../types/auth';

import MainLayout from '../../components/MainLayout';

const { Title } = Typography;

const SignIn: React.FC = () => {
  const navigate = useNavigate();
  const [form] = Form.useForm();

  const onFinish = async (values: any) => {
    try {
      const registerData: RegisterCredentials = {
        username: values.username,
        email: values.email,
        password: values.password
      };

      await authService.register(registerData);
      message.success('Registration successful! Please login.');
      navigate('/login');
    } catch (error: unknown) {
      console.error(error);
      if (error instanceof Error) {
        message.error('Registration failed: ' + error.message);
      } else {
        message.error('An error occurred');
      }
    }
  };

  return (
    <MainLayout>
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flex: 1, height: '100%' }}>
        <Card style={{ width: 450, backdropFilter: 'blur(10px)' }} bordered={false}>
          <div style={{ textAlign: 'center', marginBottom: 24 }}>
            <Title level={2}>Create Account</Title>
            <Typography.Text type="secondary">Join the Card Games Platform</Typography.Text>
          </div>

          <Form
            form={form}
            name="register"
            onFinish={onFinish}
            layout="vertical"
            size="large"
            scrollToFirstError
          >
            <Form.Item
              name="username"
              label="Pseudo"
              rules={[{ required: true, message: 'Please input your nickname!', whitespace: true }]}
            >
              <Input prefix={<UserOutlined />} placeholder="Pseudo" />
            </Form.Item>

            <Form.Item
              name="email"
              label="E-mail"
              rules={[
                { type: 'email', message: 'The input is not valid E-mail!' },
                { required: true, message: 'Please input your E-mail!' },
              ]}
            >
              <Input prefix={<MailOutlined />} placeholder="Email" />
            </Form.Item>

            <Form.Item
              name="password"
              label="Password"
              rules={[
                { required: true, message: 'Please input your password!' },
                { min: 6, message: 'Password must be at least 6 characters!' }
              ]}
              hasFeedback
            >
              <Input.Password prefix={<LockOutlined />} placeholder="Password" />
            </Form.Item>

            <Form.Item
              name="confirm"
              label="Confirm Password"
              dependencies={['password']}
              hasFeedback
              rules={[
                { required: true, message: 'Please confirm your password!' },
                ({ getFieldValue }) => ({
                  validator(_, value) {
                    if (!value || getFieldValue('password') === value) {
                      return Promise.resolve();
                    }
                    return Promise.reject(new Error('The two passwords that you entered do not match!'));
                  },
                }),
              ]}
            >
              <Input.Password prefix={<LockOutlined />} placeholder="Confirm Password" />
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" block>
                Register
              </Button>
            </Form.Item>

            <div style={{ textAlign: 'center' }}>
              <Typography.Text>
                Already have an account? <Link to="/login">Log in</Link>
              </Typography.Text>
            </div>
          </Form>
        </Card>
      </div>
    </MainLayout>
  );
};

export default SignIn;
