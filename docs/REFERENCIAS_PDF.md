# Resumen de requisitos a partir de los tres PDF

Se revisaron completos los 3 archivos que acompañan el encargo.

## 036 — Caso OYL Frutos Secos (3 páginas)
- Problema real planteado: inventario inconsistente entre tienda física, WhatsApp Business y Mercado Libre; riesgo de sobreventas.
- Cliente: consulta de catálogo y creación de solicitudes. Administrador: gestión de productos, precios e inventario.
- Datos mínimos de Producto: ID, nombre, categoría, precio, stock y URI de foto.
- Datos mínimos de Pedido: ID, fecha/hora, productos, total y estado.
- Requiere conexión de Android por Retrofit y API REST con Spring Boot para inventario oficial y pedidos. Room es una caché/localización, no el inventario transaccional.
- Solo datos sintéticos; no usar credenciales productivas.

## 059 — Pantallas Caso OYL Frutos Secos (5 páginas)
- Página 2: bienvenida, registro, login y dashboard.
- Página 3: mapa, catálogo, detalle y QR.
- Página 4: carrito, confirmación, gestión de productos e inventario.
- Página 5: historial/gestión de pedidos, notificaciones y perfil.
- Total: 15 pantallas. Administrador preconfigurado `admin@admin.cl` (no hay contraseña real especificada en PDF).
- Visual: verdes, superficies claras, botones redondeados, fotografías de frutos secos, tarjetas y barra inferior en dashboard.
- La copia local de inventario es informativa; el backend debe validar precios, stock y pedidos.

## Evaluación Parcial 2 — Rúbrica DSY1105 (6 páginas)
- Interfaz y navegación 15%; formularios validados 15%; validaciones desacopladas 10%; animaciones 10%.
- Modularidad y persistencia 15%; GitHub + Trello con evidencia real 20%; al menos 2 recursos nativos funcionales 15%.
- Proyecto público y código en AVA. Enlace individual activado con datos del estudiante.
- Advertencia severa por modificar el repositorio tras la fecha de entrega y antes de evaluación.
- No se identifica aquí una fecha exacta para la sección actual: confirmar con el docente.

Este texto es un resumen de las fuentes del encargo y no amplía la lista de funciones implementadas en ZIP 01.
