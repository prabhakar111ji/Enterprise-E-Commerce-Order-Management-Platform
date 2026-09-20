-- ============================================
-- SEED DATA FOR LOCAL DEVELOPMENT
-- ============================================
-- Admin: admin@ecommerce.com / admin123
-- User:  user@ecommerce.com  / user123
-- ============================================

-- Users (passwords are BCrypt hashed)
INSERT INTO users (name, email, password, role, created_at)
VALUES ('Admin User', 'admin@ecommerce.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (name, email, password, role, created_at)
VALUES ('John Doe', 'user@ecommerce.com', '$2a$10$LqXwzUh2u6bKGQH3BxKVKOW3oyF7f7pS38mXq3bZofnfKq0K2bGKm', 'USER', NOW())
ON CONFLICT (email) DO NOTHING;

-- Products
INSERT INTO products (name, description, price, category, image_url, created_at) VALUES
-- Electronics
('MacBook Pro 16"', 'Apple MacBook Pro with M3 chip, 16GB RAM, 512GB SSD', 189999.00, 'Electronics', 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=500', NOW()),
('iPhone 15 Pro', 'Apple iPhone 15 Pro 256GB - Natural Titanium', 134900.00, 'Electronics', 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=500', NOW()),
('Sony WH-1000XM5', 'Wireless Noise-Cancelling Headphones', 29990.00, 'Electronics', 'https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?auto=format&fit=crop&w=500', NOW()),
('Mechanical Keyboard', 'Cherry MX Blue mechanical keyboard - RGB backlit', 12500.00, 'Electronics', 'https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=500', NOW()),

-- Books
('Clean Code', 'A Handbook of Agile Software Craftsmanship by Robert C. Martin', 2500.00, 'Books', 'https://images.unsplash.com/photo-1532012197267-da84d127e765?auto=format&fit=crop&w=500', NOW()),
('Design Patterns', 'Elements of Reusable Object-Oriented Software - GoF', 3200.00, 'Books', 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=500', NOW()),
('Java: The Complete Reference', 'Comprehensive Java programming guide by Herbert Schildt', 1800.00, 'Books', 'https://images.unsplash.com/photo-1589998059171-988d887df646?auto=format&fit=crop&w=500', NOW()),
('The Pragmatic Programmer', 'Journey to Mastery by David Thomas and Andrew Hunt', 2800.00, 'Books', 'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=500', NOW()),

-- Clothing
('Nike Air Max 90', 'Classic running shoes - White/Black/Red', 11500.00, 'Clothing', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=500', NOW()),
('Levi''s 501 Jeans', 'Original fit straight leg jeans - Medium Wash', 3999.00, 'Clothing', 'https://images.pexels.com/photos/1598505/pexels-photo-1598505.jpeg?auto=compress&cs=tinysrgb&w=500', NOW()),
('Premium Cotton T-Shirt', '100% Organic Cotton comfortable basic t-shirt', 899.00, 'Clothing', 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=500', NOW()),
('Winter Puffer Jacket', 'Water-resistant insulated winter jacket', 4500.00, 'Clothing', 'https://images.pexels.com/photos/1689731/pexels-photo-1689731.jpeg?auto=compress&cs=tinysrgb&w=500', NOW()),

-- Home
('Ergonomic Standing Desk', 'Electric adjustable standing desk - 60x30 inches', 24999.00, 'Home', 'https://images.unsplash.com/photo-1595515106969-1ce29566ff1c?auto=format&fit=crop&w=500', NOW()),
('Executive Office Chair', 'Ergonomic mesh office chair with lumbar support', 8500.00, 'Home', 'https://images.unsplash.com/photo-1505843490538-5133c6c7d0e1?auto=format&fit=crop&w=500', NOW()),
('Modern Table Lamp', 'Minimalist LED desk lamp with adjustable brightness', 1200.00, 'Home', 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=500', NOW()),
('Espresso Coffee Maker', 'Automatic espresso machine with milk frother', 5499.00, 'Home', 'https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=500', NOW())
ON CONFLICT DO NOTHING;

-- Inventory (stock for each product)
INSERT INTO inventory (product_id, quantity, last_updated)
SELECT p.id, stock.qty, NOW()
FROM products p
JOIN (VALUES
    ('MacBook Pro 16"', 25),
    ('iPhone 15 Pro', 50),
    ('Sony WH-1000XM5', 100),
    ('Mechanical Keyboard', 80),
    ('Clean Code', 200),
    ('Design Patterns', 150),
    ('Java: The Complete Reference', 180),
    ('The Pragmatic Programmer', 200),
    ('Nike Air Max 90', 75),
    ('Levi''s 501 Jeans', 120),
    ('Premium Cotton T-Shirt', 300),
    ('Winter Puffer Jacket', 40),
    ('Ergonomic Standing Desk', 30),
    ('Executive Office Chair', 55),
    ('Modern Table Lamp', 100),
    ('Espresso Coffee Maker', 45)
) AS stock(name, qty) ON p.name = stock.name
WHERE NOT EXISTS (SELECT 1 FROM inventory i WHERE i.product_id = p.id);
