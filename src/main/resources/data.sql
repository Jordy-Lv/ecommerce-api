INSERT INTO users (name, email, password, role, created_at) VALUES
('Admin', 'admin@store.com', '$2a$10$TNAKZUGw/x.USLXmUw4CLegRAhIcYIDa9CzpacicTjtJZ4bXjbc5a', 'ADMIN', NOW()),
('Juan Perez', 'user@store.com', '$2a$10$MDOqcw9c0MxiMxvGxqvZLuC8vD0skZzUeyavPy32mwiagIfMzEUeS', 'CUSTOMER', NOW());

INSERT INTO categories (name) VALUES
('Electronics'),
('Clothing'),
('Books');

INSERT INTO products (name, description, price, stock, category_id, created_at) VALUES
('Laptop Dell Inspiron', 'Laptop 15 pulgadas, 16GB RAM, 512GB SSD', 899.99, 10, 1, NOW()),
('iPhone 15', 'Smartphone Apple 128GB negro', 1099.00, 25, 1, NOW()),
('Camiseta Basica', 'Camiseta de algodon color blanco', 19.90, 100, 2, NOW()),
('Pantalon Jeans', 'Jeans azul clasico corte recto', 49.50, 60, 2, NOW()),
('Clean Code', 'Libro de Robert C. Martin sobre buenas practicas', 35.00, 40, 3, NOW());
