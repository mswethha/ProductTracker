import { BrowserRouter, Routes, Route, Link, Navigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { getMe, setAuth, clearAuth } from './api';
import Home from './pages/Home';
import ProductStatus from './pages/ProductStatus';
import Profile from './pages/Profile';
import Register from './pages/Register';
import Login from './pages/Login';
import Admin from './pages/Admin';
import './App.css';

function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getMe()
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  const handleLogin = (username, password) => {
    setAuth(username, password);
    return getMe().then((me) => {
      setUser(me);
      return me;
    });
  };

  const handleLogout = () => {
    clearAuth();
    setUser(null);
  };

  if (loading) {
    return <div className="app-loading">Loading…</div>;
  }

  return (
    <BrowserRouter>
      <nav className="nav">
        <Link to="/">Home</Link>
        {user && <Link to="/status">Product Status</Link>}
        {user ? (
          <>
            <span className="nav-user">Hello, {user.username}</span>
            {user.admin && <Link to="/admin">Admin</Link>}
            <Link to="/profile">Profile</Link>
            <button type="button" onClick={handleLogout} className="btn-link">
              Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </nav>

      <main className="main">
        <Routes>
          <Route path="/" element={<Home user={user} />} />
          <Route
            path="/status"
            element={user ? <ProductStatus /> : <Navigate to="/login" replace />}
          />
          <Route path="/profile" element={user ? <Profile user={user} /> : <Navigate to="/login" replace />} />
          <Route path="/register" element={<Register onSuccess={handleLogin} />} />
          <Route path="/login" element={<Login onSuccess={handleLogin} />} />
          <Route
            path="/admin"
            element={user?.admin ? <Admin /> : <Navigate to="/login" replace />}
          />
        </Routes>
      </main>
    </BrowserRouter>
  );
}

export default App;