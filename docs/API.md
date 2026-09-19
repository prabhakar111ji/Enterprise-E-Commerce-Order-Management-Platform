# API Reference

Base URL: `http://localhost:8080/api`

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Authentication

### Register
```
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}

Response 201:
{
  "token": "eyJhbGci...",
  "email": "john@example.com",
  "name": "John Doe",
  "role": "USER"
}
```

### Login
```
POST /api/auth/login

{ "email": "admin@ecommerce.com", "password": "admin123" }

Response 200: { "token": "...", "email": "...", "name": "...", "role": "ADMIN" }
```

---

## Products

### List Products (Public)
```
GET /api/products?page=0&size=10&sort=price,asc&category=Electronics&search=macbook

Response 200: { "content": [...], "totalPages": 5, "totalElements": 50 }
```

### Get Product (Public)
```
GET /api/products/{id}

Response 200: { "id": 1, "name": "...", "price": 999.99, "category": "...", "stock": 50 }
```

### Create Product (Admin Only)
```
POST /api/products
Authorization: Bearer <admin-token>

{ "name": "New Product", "description": "...", "price": 49.99, "category": "Books", "stock": 100 }

Response 201: { "id": 11, ... }
```

### Update Product (Admin Only)
```
PUT /api/products/{id}
Authorization: Bearer <admin-token>

{ "name": "Updated", "price": 59.99, "category": "Books", "stock": 80 }
```

### Delete Product (Admin Only)
```
DELETE /api/products/{id}
Authorization: Bearer <admin-token>

Response 204: No Content
```

---

## Cart (Authenticated)

### Get Cart
```
GET /api/cart
Authorization: Bearer <token>

Response 200:
{
  "cartId": 1,
  "items": [
    { "productId": 1, "productName": "MacBook Pro", "price": 2499.99, "quantity": 1, "subtotal": 2499.99 }
  ],
  "totalPrice": 2499.99
}
```

### Add Item to Cart
```
POST /api/cart/items
Authorization: Bearer <token>

{ "productId": 1, "quantity": 2 }
```

### Update Item Quantity
```
PUT /api/cart/items/{productId}?quantity=3
Authorization: Bearer <token>
```

### Remove Item
```
DELETE /api/cart/items/{productId}
Authorization: Bearer <token>
```

### Clear Cart
```
DELETE /api/cart
Authorization: Bearer <token>
```

---

## Orders (Authenticated)

### Place Order
```
POST /api/orders
Authorization: Bearer <token>

Response 201:
{
  "id": 1,
  "status": "CONFIRMED",
  "totalAmount": 2499.99,
  "createdAt": "2024-01-15T10:30:00",
  "items": [...]
}
```

### Get My Orders
```
GET /api/orders?page=0&size=10
Authorization: Bearer <token>
```

### Get Order Detail
```
GET /api/orders/{id}
Authorization: Bearer <token>
```

### Cancel Order
```
PUT /api/orders/{id}/cancel
Authorization: Bearer <token>
```

---

## Admin Orders (Admin Only)

### Get All Orders
```
GET /api/admin/orders?page=0&size=20
Authorization: Bearer <admin-token>
```

### Update Order Status
```
PUT /api/admin/orders/{id}/status
Authorization: Bearer <admin-token>

{ "status": "SHIPPED" }
```

---

## Error Responses

All errors follow this format:
```json
{
  "timestamp": "2024-01-15 10:30:00",
  "status": 400,
  "message": "Insufficient stock for 'MacBook Pro': requested 5, available 3",
  "path": "/api/orders"
}
```

| Status | Meaning |
|--------|---------|
| 400 | Bad request / validation error |
| 401 | Unauthorized (missing/invalid JWT) |
| 403 | Forbidden (insufficient role) |
| 404 | Resource not found |
| 500 | Internal server error |
