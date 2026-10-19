# Backend académico Spring Boot para OYL

**ATENCIÓN: Solo para pruebas locales, NO desplegar en internet.** Este MVP carece de autenticación/autorización remota. No hay gestión remota de administrador habilitada.

## Requisitos
Java 17, Maven 3.9 o superior.

## Inicio
Desde la carpeta backend ejecutar:

```bash
mvn spring-boot:run
```

Probar: http://localhost:8080/api/health

El servidor utiliza H2 en `backend/data/` (datos ficticios), con semillas solo si la tabla está vacía. La base central representa el inventario oficial **únicamente en el entorno de prueba**.

### Endpoints
- GET /api/products — catálogo y stock del servidor
- GET /api/products/OY001 — ficha
- POST /api/orders — reserva transaccional y rebaja de stock mediante bloqueo de filas
- GET /api/orders?clientAlias=cliente — historial de solicitudes de prueba
- GET /api/health — estado

Pedido de ejemplo (JSON):
```json
{"clientAlias":"cliente","fulfillment":"Retiro","items":[{"productId":"OY001","quantity":1}]}
```

No hay pago integrado. El stock se descuenta al aceptar la solicitud dentro de la misma transacción. El acceso a los pedidos por alias NO es seguro: la API debe incorporar identidad y autorización antes de usarse fuera del ejercicio académico.

## Próximos trabajos
Cliente Retrofit de Android conectado al backend, autenticación real, gestión administrativa con autorización, pruebas de concurrencia, confirmación visual e historial.
