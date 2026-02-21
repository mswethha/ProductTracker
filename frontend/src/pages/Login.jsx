import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMe, setAuth } from '../api';

export default function Login({ onSuccess }) {
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      setAuth(username, password);
      const me = await onSuccess(username, password);
      if (me?.admin) navigate('/admin');
      else navigate('/');
    } catch (err) {
      setError('Invalid username or password');
      setAuth('', '');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <h2>Log in</h2>
      <p style={{ color: '#a1a1aa', marginBottom: '1rem' }}>
        Use your subscriber account to view product status. Admins can open the Admin panel after login.
      </p>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Username</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            placeholder="Your username"
          />
        </div>
        <div className="form-group">
          <label>Password</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            placeholder="Your password"
          />
        </div>
        {error && <p className="error-msg">{error}</p>}
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Logging in…' : 'Log in'}
        </button>
      </form>
    </div>
  );
}
