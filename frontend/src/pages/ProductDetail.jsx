import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';

export default function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [quantity, setQuantity] = useState(1);
  const [adding, setAdding] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    api.get(`/products/${id}`)
      .then(res => setProduct(res.data))
      .catch(() => navigate('/'))
      .finally(() => setLoading(false));
  }, [id]);

  const addToCart = async () => {
    if (!isAuthenticated()) { navigate('/login'); return; }
    setAdding(true);
    try {
      await api.post('/cart/items', { productId: product.id, quantity });
      setMessage('Added to cart!');
      setTimeout(() => setMessage(''), 2000);
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed to add to cart');
    } finally {
      setAdding(false);
    }
  };

  if (loading) return <div className="loading">Loading...</div>;
  if (!product) return <div className="loading">Product not found</div>;

  return (
    <div className="container">
      <div className="card">
        <div className="product-detail">
          <img src={product.imageUrl || `https://via.placeholder.com/400x300?text=${encodeURIComponent(product.name)}`} alt={product.name} />
          <div className="product-detail-info">
            <span className="product-category">{product.category}</span>
            <h1>{product.name}</h1>
            <div className="price">₹{product.price?.toFixed(2)}</div>
            <p style={{ color: '#64748b', marginBottom: '1.5rem' }}>{product.description}</p>
            <div className={`product-stock ${product.stock > 0 ? '' : 'out'}`} style={{ marginBottom: '1.5rem', fontSize: '1rem' }}>
              {product.stock > 0 ? `✓ ${product.stock} in stock` : '✕ Out of stock'}
            </div>

            {product.stock > 0 && (
              <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', marginBottom: '1rem' }}>
                <div className="form-group" style={{ margin: 0, width: '100px' }}>
                  <label>Qty</label>
                  <input type="number" min="1" max={product.stock} value={quantity}
                    onChange={(e) => setQuantity(Math.max(1, Math.min(product.stock, parseInt(e.target.value) || 1)))} />
                </div>
                <button className="btn btn-primary" onClick={addToCart} disabled={adding} style={{ marginTop: '1.2rem' }}>
                  {adding ? 'Adding...' : 'Add to Cart'}
                </button>
              </div>
            )}

            {message && <div className={message.includes('Failed') ? 'error-msg' : 'success-msg'}>{message}</div>}
          </div>
        </div>
      </div>
    </div>
  );
}
