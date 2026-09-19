import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';

export default function Cart() {
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [ordering, setOrdering] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => { fetchCart(); }, []);

  const fetchCart = async () => {
    try {
      const res = await api.get('/cart');
      setCart(res.data);
    } catch (err) {
      console.error('Failed to fetch cart:', err);
    } finally {
      setLoading(false);
    }
  };

  const updateQuantity = async (productId, newQty) => {
    if (newQty < 1) return;
    try {
      const res = await api.put(`/cart/items/${productId}?quantity=${newQty}`);
      setCart(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update quantity');
    }
  };

  const removeItem = async (productId) => {
    try {
      const res = await api.delete(`/cart/items/${productId}`);
      setCart(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to remove item');
    }
  };

  const placeOrder = async () => {
    setOrdering(true);
    setError('');
    try {
      const res = await api.post('/orders');
      navigate(`/orders/${res.data.id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to place order');
    } finally {
      setOrdering(false);
    }
  };

  if (loading) return <div className="loading">Loading cart...</div>;

  const items = cart?.items || [];

  return (
    <div className="container">
      <h1 className="page-title">Shopping Cart</h1>

      {error && <div className="error-msg">{error}</div>}

      {items.length === 0 ? (
        <div className="card" style={{ padding: '3rem', textAlign: 'center' }}>
          <p style={{ fontSize: '1.1rem', color: '#64748b' }}>Your cart is empty</p>
          <button className="btn btn-primary" onClick={() => navigate('/')} style={{ marginTop: '1rem' }}>
            Browse Products
          </button>
        </div>
      ) : (
        <div className="card">
          {items.map(item => (
            <div key={item.productId} className="cart-item">
              <div className="cart-item-info">
                <div className="cart-item-name">{item.productName}</div>
                <div className="cart-item-price">${item.price?.toFixed(2)} each</div>
              </div>
              <div className="cart-item-actions">
                <div className="qty-control">
                  <button onClick={() => updateQuantity(item.productId, item.quantity - 1)}>−</button>
                  <span style={{ fontWeight: 600, minWidth: '24px', textAlign: 'center' }}>{item.quantity}</span>
                  <button onClick={() => updateQuantity(item.productId, item.quantity + 1)}>+</button>
                </div>
                <span style={{ fontWeight: 700, minWidth: '80px', textAlign: 'right' }}>${item.subtotal?.toFixed(2)}</span>
                <button className="btn btn-danger btn-sm" onClick={() => removeItem(item.productId)}>Remove</button>
              </div>
            </div>
          ))}

          <div className="cart-summary">
            <div className="cart-total">
              <span>Total</span>
              <span>${cart.totalPrice?.toFixed(2)}</span>
            </div>
            <button className="btn btn-success btn-block" onClick={placeOrder} disabled={ordering}>
              {ordering ? 'Placing Order...' : 'Place Order'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
