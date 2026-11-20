-- Script de carga inicial (pedidos de ejemplo)
INSERT INTO orders (id, client_alias, created_at, total, status, fulfillment) VALUES
('demo-0001', 'cliente', TIMESTAMP '2026-11-01 10:30:00', 8970, 'Entregado', 'Retiro en tienda'),
('demo-0002', 'cliente', TIMESTAMP '2026-11-10 16:05:00', 3490, 'Pendiente de Validación', 'Retiro en tienda');
INSERT INTO order_lines (order_id, product_id, product_name, quantity, unit_price) VALUES
('demo-0001', 'OY001', 'Almendras naturales', 3, 2990),
('demo-0002', 'OY002', 'Nueces', 1, 3490);
