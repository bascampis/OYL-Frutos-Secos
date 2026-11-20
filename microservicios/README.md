# Microservicios Spring Boot — OYL Frutos Secos

Tres servicios independientes (Java 17, Spring Boot 3.4.5, Spring Data JPA, H2). Cada uno tiene su propia base de datos
en memoria, que **se crea y se puebla con `src/main/resources/data.sql` cada vez que arranca**.

| Servicio | Puerto | Responsabilidad | Base de datos |
|---|---|---|---|
| catalog-service | 8081 | Productos e inventario (CRUD + ajuste de stock) | tabla `products` |
| order-service | 8082 | Pedidos; descuenta stock llamando a catalog-service | tablas `orders`, `order_lines` |
| user-service | 8083 | Registro, login y CRUD de usuarios (SHA-256 + salt) | tabla `users` |

## Ejecutar
Requisitos: Java 17+ y Maven 3.9+. Abre **3 terminales** dentro de `microservicios/`:

```bash
mvn -pl catalog-service spring-boot:run
mvn -pl order-service spring-boot:run
mvn -pl user-service spring-boot:run
```
Pruebas unitarias: `mvn test` (desde `microservicios/`).
Consola de la BD (H2): `http://localhost:808X/h2-console`, JDBC URL `jdbc:h2:mem:catalog` (o `orders`, `users`), usuario `sa`, sin clave.
Comprobación rápida: `http://localhost:8081/api/health`, `8082`, `8083`.

## Endpoints

### catalog-service (8081)
| Método | Ruta | Descripción |
|---|---|---|
| GET | /api/health | Estado del servicio |
| GET | /api/products | Lista productos |
| GET | /api/products/{id} | Detalle de un producto |
| POST | /api/products | Crea producto (201; 409 si el código existe) |
| PUT | /api/products/{id} | Edita producto (no cambia el stock) |
| DELETE | /api/products/{id} | Elimina producto (204) |
| PATCH | /api/products/{id}/stock | Ajusta stock `{"delta": -2}` (409 si queda negativo) |

### order-service (8082)
| Método | Ruta | Descripción |
|---|---|---|
| GET | /api/health | Estado del servicio |
| POST | /api/orders | Crea pedido y descuenta stock en catalog-service (409 sin stock, 503 si el catálogo está caído) |
| GET | /api/orders | Todos los pedidos (administrador) |
| GET | /api/orders?clientAlias=cliente | Pedidos de un cliente |
| GET | /api/orders/{id} | Detalle de un pedido |
| PATCH | /api/orders/{id}/status | Cambia estado `{"status":"Confirmado"}` |
| DELETE | /api/orders/{id} | Cancela un pedido pendiente y repone el stock |

### user-service (8083)
| Método | Ruta | Descripción |
|---|---|---|
| GET | /api/health | Estado del servicio |
| POST | /api/users/register | Registra usuario (valida correo, teléfono +569XXXXXXXX y contraseña) |
| POST | /api/users/login | Inicia sesión (401 si las credenciales son inválidas) |
| GET | /api/users | Lista usuarios |
| GET | /api/users/{id} | Detalle de un usuario |
| PUT | /api/users/{id} | Edita nombre, teléfono y dirección |
| DELETE | /api/users/{id} | Elimina usuario (no permite eliminar al administrador) |

Cuentas sembradas: `admin@admin.cl` (ADMIN) y `cliente@cliente.cl` (CLIENT), contraseña `DemoOYL2026!`.

> Proyecto académico: sin autenticación por token entre servicios. No exponer a internet.
