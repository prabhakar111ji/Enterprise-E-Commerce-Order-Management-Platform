import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout, isAdmin, isAuthenticated } = useAuth();

  return (
    <header className="navbar">
      <Link to="/" className="logo">Shop<span>Ease</span></Link>
      <nav>
        <Link to="/">Products</Link>
        {isAuthenticated() ? (
          <>
            <Link to="/cart">Cart</Link>
            <Link to="/orders">My Orders</Link>
            {isAdmin() && <Link to="/admin" className="nav-btn">Admin</Link>}
            <span style={{ color: '#94a3b8', fontSize: '0.85rem' }}>Hi, {user.name}</span>
            <button onClick={logout} className="nav-btn danger">Logout</button>
          </>
        ) : (
          <>
            <Link to="/login" className="nav-btn">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </nav>
    </header>
  );
}
