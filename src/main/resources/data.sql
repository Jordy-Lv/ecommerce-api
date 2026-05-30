-- Seed idempotente: con ddl-auto=update la base persiste entre reinicios y este
-- script corre en cada arranque (spring.sql.init.mode=always), por lo que cada
-- INSERT se protege con NOT EXISTS para no violar restricciones unique ni duplicar.

INSERT INTO users (name, email, password, role, created_at)
SELECT 'Admin', 'admin@store.com', '$2a$10$TNAKZUGw/x.USLXmUw4CLegRAhIcYIDa9CzpacicTjtJZ4bXjbc5a', 'ADMIN', NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@store.com');

INSERT INTO users (name, email, password, role, created_at)
SELECT 'Juan Perez', 'user@store.com', '$2a$10$MDOqcw9c0MxiMxvGxqvZLuC8vD0skZzUeyavPy32mwiagIfMzEUeS', 'CUSTOMER', NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'user@store.com');

INSERT INTO categories (name)
SELECT 'Electronics' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Electronics');
INSERT INTO categories (name)
SELECT 'Clothing' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Clothing');
INSERT INTO categories (name)
SELECT 'Books' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Books');

INSERT INTO products (name, description, price, stock, category_id, created_at)
SELECT 'Laptop Dell Inspiron', 'Laptop 15 pulgadas, 16GB RAM, 512GB SSD', 899.99, 10,
       (SELECT id FROM categories WHERE name = 'Electronics'), NOW()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Laptop Dell Inspiron');

INSERT INTO products (name, description, price, stock, category_id, created_at)
SELECT 'iPhone 15', 'Smartphone Apple 128GB negro', 1099.00, 25,
       (SELECT id FROM categories WHERE name = 'Electronics'), NOW()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'iPhone 15');

INSERT INTO products (name, description, price, stock, category_id, created_at)
SELECT 'Camiseta Basica', 'Camiseta de algodon color blanco', 19.90, 100,
       (SELECT id FROM categories WHERE name = 'Clothing'), NOW()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Camiseta Basica');

INSERT INTO products (name, description, price, stock, category_id, created_at)
SELECT 'Pantalon Jeans', 'Jeans azul clasico corte recto', 49.50, 60,
       (SELECT id FROM categories WHERE name = 'Clothing'), NOW()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pantalon Jeans');

INSERT INTO products (name, description, price, stock, category_id, created_at)
SELECT 'Clean Code', 'Libro de Robert C. Martin sobre buenas practicas', 35.00, 40,
       (SELECT id FROM categories WHERE name = 'Books'), NOW()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Clean Code');
