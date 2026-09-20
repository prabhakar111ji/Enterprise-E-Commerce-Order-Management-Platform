import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';

export default function Home() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const categories = ['', 'Electronics', 'Books', 'Clothing', 'Home'];

  useEffect(() => {
    fetchProducts();
  }, [page, category]);

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const params = { page, size: 12, sort: 'id,desc' };
      if (category) params.category = category;
      if (search) params.search = search;
      const res = await api.get('/products', { params });
      setProducts(res.data.content);
      setTotalPages(res.data.totalPages);
    } catch (err) {
      console.error('Failed to fetch products:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    fetchProducts();
  };

  return (
    <div className="container">
      <h1 className="page-title">Our Products</h1>

      <div className="filters">
        <form onSubmit={handleSearch} style={{ display: 'flex', gap: '0.5rem', flex: 1 }}>
          <input
            type="text"
            placeholder="Search products..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <button type="submit" className="btn btn-primary">Search</button>
        </form>
        <select value={category} onChange={(e) => { setCategory(e.target.value); setPage(0); }}>
          <option value="">All Categories</option>
          {categories.filter(c => c).map(c => (
            <option key={c} value={c}>{c}</option>
          ))}
        </select>
      </div>

      {loading ? (
        <div className="loading">Loading products...</div>
      ) : products.length === 0 ? (
        <div className="loading">No products found</div>
      ) : (
        <>
          <div className="product-grid">
            {products.map(product => (
              <Link key={product.id} to={`/products/${product.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                <div className="card product-card">
                  <img src={product.imageUrl || `https://via.placeholder.com/300x200?text=${encodeURIComponent(product.name)}`} alt={product.name} />
                  <div className="product-info">
                    <div className="product-category">{product.category}</div>
                    <div className="product-name">{product.name}</div>
                    <div className="product-price">₹{product.price?.toFixed(2)}</div>
                    <div className={`product-stock ${product.stock > 0 ? '' : 'out'}`}>
                      {product.stock > 0 ? `${product.stock} in stock` : 'Out of stock'}
                    </div>
                  </div>
                </div>
              </Link>
            ))}
          </div>

          {totalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'center', gap: '0.5rem', marginTop: '2rem' }}>
              <button className="btn btn-outline btn-sm" disabled={page === 0} onClick={() => setPage(p => p - 1)}>Previous</button>
              <span style={{ padding: '0.4rem 1rem', fontSize: '0.9rem' }}>Page {page + 1} of {totalPages}</span>
              <button className="btn btn-outline btn-sm" disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>Next</button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
