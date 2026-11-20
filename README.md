# OYL Frutos Secos

**Asignatura:** Desarrollo de Aplicaciones Móviles (DSY1105) — Duoc UC
**Evaluación:** Parcial 4 (continuación de la Evaluación 2)
**Caso de estudio:** OYL Frutos Secos, tienda de frutos secos, deshidratados y semillas.

> Proyecto académico y de demostración. Usa datos ficticios; no está preparado para uso comercial.

## Integrantes
- Catalina Aguirre
- *(nombre del compañero/a)*

## Funcionalidades
**Cliente**
- Pantalla de bienvenida, inicio de sesión y registro con validaciones (correo, teléfono `+569`, contraseña con mayúscula y número).
- Catálogo con búsqueda y categorías, detalle de producto y carrito.
- Confirmación de pedidos e historial.
- Escáner QR de productos (Google Code Scanner) con permiso de cámara.
- Perfil con foto, edición de datos, direcciones y soporte.
- Tarjeta con el valor del dólar, obtenida de una API externa con Retrofit.

**Administrador** (`admin@admin.cl`)
- Gestión de productos: crear, editar, activar/desactivar y agregar foto por URL.
- Inventario con ajuste de stock y confirmación.
- Gestión de pedidos y cambio de estado.
- Preferencias operativas.

## Arquitectura
| Capa | Tecnología |
|---|---|
| App móvil | Kotlin, Jetpack Compose (Material 3), MVVM, Navigation Compose, Room, Retrofit, Coil |
| Microservicios | Java 17, Spring Boot 3.4.5, Spring Data JPA, base H2 por servicio |
| API externa | open.er-api.com (tipo de cambio USD → CLP) |

```
OYL-Frutos-Secos/
  app/             aplicación Android
  microservicios/  catalog-service, order-service, user-service
  docs/            documentación y guías
```

## Microservicios y endpoints
Cada servicio tiene su propia base de datos H2 en memoria, que se **crea y se puebla con `data.sql` al iniciar Spring Boot**.

**catalog-service — puerto 8081**
| Método | Ruta | Descripción |
|---|---|---|
| GET | /api/products | Lista productos |
| GET | /api/products/{id} | Detalle |
| POST | /api/products | Crea producto |
| PUT | /api/products/{id} | Edita producto |
| DELETE | /api/products/{id} | Elimina producto |
| PATCH | /api/products/{id}/stock | Ajusta stock |

**order-service — puerto 8082** (descuenta stock llamando a catalog-service)
| Método | Ruta | Descripción |
|---|---|---|
| POST | /api/orders | Crea pedido |
| GET | /api/orders | Todos los pedidos (administrador) |
| GET | /api/orders?clientAlias= | Pedidos de un cliente |
| GET | /api/orders/{id} | Detalle |
| PATCH | /api/orders/{id}/status | Cambia el estado |
| DELETE | /api/orders/{id} | Cancela un pedido pendiente y repone stock |

**user-service — puerto 8083**
| Método | Ruta | Descripción |
|---|---|---|
| POST | /api/users/register | Registra usuario |
| POST | /api/users/login | Inicia sesión |
| GET | /api/users | Lista usuarios |
| GET | /api/users/{id} | Detalle |
| PUT | /api/users/{id} | Edita datos |
| DELETE | /api/users/{id} | Elimina usuario |

Todos los servicios exponen además `GET /api/health`.

**API externa (Retrofit):** `GET https://open.er-api.com/v6/latest/USD`, que entrega el valor del dólar en pesos chilenos y se muestra en la app.

Más detalle en [`microservicios/README.md`](microservicios/README.md).

## Pasos para ejecutar

### 1. Backend
Requisitos: Java 17 o superior y Maven 3.9 o superior. Desde la carpeta `microservicios/`, en tres terminales:
```bash
mvn -pl catalog-service spring-boot:run
mvn -pl order-service spring-boot:run
mvn -pl user-service spring-boot:run
```
Comprobar: `http://localhost:8081/api/products`, `http://localhost:8082/api/orders` y `http://localhost:8083/api/users`.
Pruebas unitarias del backend: `mvn test` (37 pruebas).

### 2. App Android
1. Abrir en Android Studio la **carpeta raíz** (donde está `settings.gradle.kts`) y sincronizar Gradle.
2. En `app/src/main/java/cl/oyl/frutossecos/data/remote/ApiConfig.kt`, usar `HOST = "10.0.2.2"` en el emulador, o la IP del PC en la red WiFi si se prueba en un celular físico.
3. Ejecutar en un emulador o dispositivo con Android 8 (API 26) o superior.
4. Pruebas unitarias de la app: `./gradlew testDebugUnitTest`.

**Cuentas de prueba** (contraseña ficticia `DemoOYL2026!`): `admin@admin.cl` (administrador) y `cliente@cliente.cl` (cliente).

## APK firmado
*(Se agregará la captura de la configuración de firma y del archivo `.jks`.)*

## Planificación del proyecto
La planificación se realiza en Trello con un tablero compartido entre los integrantes.

Tablero: [OYL Frutos Secos - DSY1105](https://trello.com/invite/b/6ad592d5b21864ef22331411/ATTI82ce34dee0663009dc827e7d14b22176E5003B1A/oyl-frutos-secos-dsy1105)

- Por hacer: funcionalidades pendientes.
- En desarrollo: tareas en implementación.
- En revisión: funcionalidades pendientes de pruebas.
- Finalizado: tareas implementadas y verificadas.
- Bloqueado: tareas con impedimentos identificados.

Las tarjetas incluyen responsables, descripción, checklists y evidencias.

## Trabajo en equipo
Los commits de cada integrante quedan registrados en GitHub (Insights > Contributors).

Licencia: no se ha seleccionado. Uso educativo, sin datos reales.
