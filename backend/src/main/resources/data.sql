-- ============================================
-- SEED DATA FOR LOCAL DEVELOPMENT
-- ============================================
-- Admin: admin@ecommerce.com / admin123
-- User:  user@ecommerce.com  / user123
-- ============================================

-- Users (passwords are BCrypt hashed)
-- admin123 → $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- user123  → $2a$10$LqXwzUh2u6bKGQH3BxKVKOW3oyF7f7pS38mXq3bZofnfKq0K2bGKm
INSERT INTO users (name, email, password, role, created_at)
VALUES ('Admin User', 'admin@ecommerce.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (name, email, password, role, created_at)
VALUES ('John Doe', 'user@ecommerce.com', '$2a$10$LqXwzUh2u6bKGQH3BxKVKOW3oyF7f7pS38mXq3bZofnfKq0K2bGKm', 'USER', NOW())
ON CONFLICT (email) DO NOTHING;

-- Products
INSERT INTO products (name, description, price, category, image_url, created_at) VALUES
('MacBook Pro 16"', 'Apple MacBook Pro with M3 chip, 16GB RAM, 512GB SSD', 2499.99, 'Electronics', 'https://via.placeholder.com/300x200?text=MacBook+Pro', NOW()),
('iPhone 15 Pro', 'Apple iPhone 15 Pro 256GB - Natural Titanium', 999.99, 'Electronics', 'https://via.placeholder.com/300x200?text=iPhone+15', NOW()),
('Sony WH-1000XM5', 'Wireless Noise-Cancelling Headphones', 349.99, 'Electronics', 'https://via.placeholder.com/300x200?text=Sony+Headphones', NOW()),
('Clean Code', 'A Handbook of Agile Software Craftsmanship by Robert C. Martin', 39.99, 'Books', 'https://via.placeholder.com/300x200?text=Clean+Code', NOW()),
('Design Patterns', 'Elements of Reusable Object-Oriented Software - GoF', 49.99, 'Books', 'https://via.placeholder.com/300x200?text=Design+Patterns', NOW()),
('Nike Air Max 90', 'Classic running shoes - White/Black', 129.99, 'Clothing', 'https://via.placeholder.com/300x200?text=Nike+Air+Max', NOW()),
('Levi''s 501 Jeans', 'Original fit straight leg jeans - Medium Wash', 69.99, 'Clothing', 'https://via.placeholder.com/300x200?text=Levis+501', NOW()),
('Standing Desk', 'Electric adjustable standing desk - 60x30 inches', 449.99, 'Home', 'https://via.placeholder.com/300x200?text=Standing+Desk', NOW()),
('Mechanical Keyboard', 'Cherry MX Blue mechanical keyboard - RGB backlit', 149.99, 'Electronics', 'https://via.placeholder.com/300x200?text=Mechanical+Keyboard', NOW()),
('Java: The Complete Reference', 'Comprehensive Java programming guide by Herbert Schildt', 44.99, 'Books', 'https://via.placeholder.com/300x200?text=Java+Reference', NOW())
ON CONFLICT DO NOTHING;

-- Inventory (stock for each product)
INSERT INTO inventory (product_id, quantity, last_updated)
SELECT p.id, stock.qty, NOW()
FROM products p
JOIN (VALUES
    ('MacBook Pro 16"', 25),
    ('iPhone 15 Pro', 50),
    ('Sony WH-1000XM5', 100),
    ('Clean Code', 200),
    ('Design Patterns', 150),
    ('Nike Air Max 90', 75),
    ('Levi''s 501 Jeans', 120),
    ('Standing Desk', 30),
    ('Mechanical Keyboard', 80),
    ('Java: The Complete Reference', 180)
) AS stock(name, qty) ON p.name = stock.name
WHERE NOT EXISTS (SELECT 1 FROM inventory i WHERE i.product_id = p.id);
