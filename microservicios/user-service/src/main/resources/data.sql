-- Script de carga inicial. Contraseña de ambas cuentas de demostración: DemoOYL2026!
-- (hash = SHA-256 de "oyl-seed" + contraseña)
INSERT INTO users (name, email, phone, address, role, salt, password_hash) VALUES
('Administrador OYL', 'admin@admin.cl', '+56911111111', 'Bodega central', 'ADMIN', 'oyl-seed', '0fd211d1a109523cfa93a4df947692cdc4c5be809b6b4ee0e06ab1ba09b65376'),
('Cliente Demo', 'cliente@cliente.cl', '+56922222222', 'Av. Siempre Viva 123', 'CLIENT', 'oyl-seed', '0fd211d1a109523cfa93a4df947692cdc4c5be809b6b4ee0e06ab1ba09b65376');
