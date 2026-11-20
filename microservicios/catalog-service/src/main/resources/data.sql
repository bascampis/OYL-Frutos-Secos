-- Script de carga inicial: se ejecuta automáticamente al iniciar Spring Boot
INSERT INTO products (id, name, category, description, presentation, price, stock, active, image_url) VALUES
('OY001', 'Almendras naturales', 'Frutos secos', 'Almendras crujientes, sin sal ni aditivos.', '250 g', 2990, 25, TRUE, NULL),
('OY002', 'Nueces', 'Frutos secos', 'Nueces en mitades, ricas en omega 3.', '250 g', 3490, 18, TRUE, NULL),
('OY003', 'Mix premium', 'Frutos secos', 'Mezcla de almendras, nueces y pasas.', '250 g', 3990, 40, TRUE, NULL),
('OY004', 'Pasas morenas', 'Deshidratados', 'Pasas morenas dulces y jugosas.', '250 g', 2490, 36, TRUE, NULL),
('OY005', 'Semillas de chía', 'Semillas', 'Semillas de chía naturales.', '250 g', 2290, 12, TRUE, NULL),
('OY006', 'Cranberries', 'Deshidratados', 'Arándanos rojos deshidratados.', '250 g', 2890, 16, TRUE, NULL);
