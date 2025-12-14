import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login/Login';
import SignIn from './pages/SignIn/SignIn';
import Home from './pages/Home/Home';
import JoinGame from './pages/JoinGame/JoinGame';
import Lobby from './pages/Lobby/Lobby';
import FlipSeven from './pages/FlipSeven/FlipSeven';
import Uno from './pages/Uno/Uno';
import SkullKing from './pages/SkullKing/SkullKing';

import { ConfigProvider } from 'antd';
import { customTheme } from './theme/themeConfig';
import { WebSocketProvider } from './context/WebSocketContext';

const App: React.FC = () => {
  return (
    <ConfigProvider theme={customTheme}>
      <BrowserRouter>
        <WebSocketProvider>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<SignIn />} />
            <Route path="/" element={<Home />} />
            <Route path="/join-game" element={<JoinGame />} />
            <Route path="/lobby" element={<Lobby />} />
            <Route path="/flipseven" element={<FlipSeven />} />
            <Route path="/uno" element={<Uno />} />
            <Route path="/skullking" element={<SkullKing />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </WebSocketProvider>
      </BrowserRouter>
    </ConfigProvider>
  );
};

export default App;
