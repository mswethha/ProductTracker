import { Link } from 'react-router-dom';

export default function Home({ user }) {
  return (
    <div style={{ textAlign: 'center', padding: '3rem 1rem' }}>
      <div className="card" style={{ maxWidth: '500px', margin: '0 auto' }}>
        <h2> Product Tracker</h2>
        <p style={{ color: '#a1a1aa', marginBottom: '1.5rem' }}>
          Get instant Telegram alerts when your favourite products come back in stock.
        </p>
        {user ? (
          <>
            <Link to="/status">
              <button className="btn btn-primary" style={{ width: '100%', marginBottom: '0.5rem' }}>
                View Product Status
              </button>
            </Link>
            <Link to="/profile">
              <button className="btn btn-secondary" style={{ width: '100%' }}>
                Link Telegram
              </button>
            </Link>
          </>
        ) : (
          <>
            <Link to="/register">
              <button className="btn btn-primary" style={{ width: '100%', marginBottom: '0.5rem' }}>
                Get Started
              </button>
            </Link>
            <Link to="/login">
              <button className="btn btn-secondary" style={{ width: '100%' }}>
                Log In
              </button>
            </Link>
          </>
        )}
      </div>
    </div>
  );
}