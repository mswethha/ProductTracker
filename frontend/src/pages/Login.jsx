import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { setAuth } from '../api';

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
      console.log('1. Setting auth for:', username);
      setAuth(username, password);

      console.log('2. Calling onSuccess...');
      const me = await onSuccess(username, password);

      console.log('3. Got me:', me);

      if (!me) {
        console.log('4. me is null/undefined — login failed');
        setError('Invalid username or password');
        setAuth('', '');
        return;
      }

      console.log('5. Admin?', me.admin);
      if (me.admin) {
        console.log('6. Navigating to /admin');
        navigate('/admin');
      } else {
        console.log('6. Navigating to /status');
        navigate('/status');
      }
    } catch (err) {
      console.error('Login error:', err);
      setError('Invalid username or password');
      setAuth('', '');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '400px', margin: '0 auto' }}>
      <div className="card">
        <h2>Log in</h2>
        <p style={{ color: '#a1a1aa', marginBottom: '1rem' }}>
          Log in to view product status and manage your account.
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
          <button
            type="submit"
            className="btn btn-primary"
            disabled={loading}
            style={{ width: '100%' }}
          >
            {loading ? 'Logging in…' : 'Log in'}
          </button>
        </form>
      </div>
    </div>
  );
}