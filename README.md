# OYL Frutos Secos

**Asignatura:** Desarrollo de Aplicaciones Móviles (DSY1105) — Duoc UC
**Evaluación:** Parcial 4 (continuación de la Evaluación 2)
**Caso de estudio:** OYL Frutos Secos, tienda de frutos secos, deshidratados y semillas.

> Proyecto académico y de demostración. Usa datos ficticios; no está preparado para uso comercial.

## Integrantes
- Catalina Aguirre
- Bastian Campos
- Suimey Flores

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
2. En `app/src/main/java/cl/oyl/frutossecos/data/remote/ApiConfig.kt`, el valor es `HOST = "127.0.0.1"`. La app se conecta al backend mediante redirección de puertos por USB (`adb reverse`), por lo que no depende de la red ni de la IP del computador.
3. Iniciar los tres microservicios (sección 1).
4. Conectar el celular (o emulador) por USB con la depuración USB activada y redirigir los puertos desde una terminal:
   ```bash
   adb reverse tcp:8081 tcp:8081
   adb reverse tcp:8082 tcp:8082
   adb reverse tcp:8083 tcp:8083
   ```
   `adb` está en `Android/Sdk/platform-tools`. Con `adb devices` se comprueba que el dispositivo esté conectado. Estos comandos deben repetirse cada vez que se reconecta el dispositivo.
5. Ejecutar la app desde Android Studio, o instalar el APK firmado:
   ```bash
   adb install app/release/app-release.apk
   ```
   Requiere Android 8 (API 26) o superior. Si el dispositivo tiene instalada una versión debug, desinstalarla antes.
6. Pruebas unitarias de la app: `./gradlew testDebugUnitTest` (184 pruebas).

Si el backend no está disponible, la app funciona en modo local con los datos guardados en el dispositivo.

**Cuentas de prueba** (contraseña ficticia `DemoOYL2026!`): `admin@admin.cl` (administrador) y `cliente@cliente.cl` (cliente).


## Captura del APK firmado y archivo .jks

El APK de la versión release de **OYL Frutos Secos** fue firmado digitalmente utilizando una llave propia (*keystore* `.jks`), configurada en el archivo `app/build.gradle.kts`.

### Archivos y configuración de firma

- **APK firmado:** `app/release/app-release.apk`
- **Keystore:** `keystore/OYL-Frutos-Secos.jks`
- **Alias de la llave:** `oyl_frutos_secos`
- **Configuración:** bloques `signingConfigs` y `buildTypes.release` en `app/build.gradle.kts`.
- **Contraseñas:** se obtienen desde `key.properties`, archivo que no se incluye en el repositorio por seguridad.

Para volver a firmar la aplicación, se debe crear el archivo `key.properties` con las propiedades `storeFile`, `storePassword`, `keyAlias` y `keyPassword`, utilizando los datos de firma correspondientes.

### 1. Generación del APK firmado

La siguiente captura muestra la evidencia de generación del APK firmado.

<img width="642" height="600" alt="1" src="https://github.com/user-attachments/assets/dbde5365-7d52-41ef-b7ed-2d3ff332f263" />

### 2. Configuración de firma en build.gradle.kts

La siguiente imagen muestra la configuración de firma utilizada para generar la versión release.

<img width="642" height="672" alt="2" src="https://github.com/user-attachments/assets/3d269e02-29db-4123-8d72-a8b36b79368a" />


### 3. Keystore (.jks) del proyecto

La siguiente captura muestra la ubicación del archivo keystore utilizado para la firma digital.

<img width="1492" height="261" alt="3" src="https://github.com/user-attachments/assets/3e236cae-9a70-4701-a616-a104e9beec52" />

**Nota de seguridad:** el archivo `.jks` y las contraseñas de firma deben mantenerse protegidos y no publicarse en el repositorio.
  

## Organización del trabajo en equipo

Para organizar el desarrollo del proyecto **OYL Frutos Secos**, utilizamos Trello como herramienta de planificación y seguimiento de actividades, distribuyendo las tareas entre los tres integrantes del equipo: **Bastián, Catalina y Suimey**.

### Tablero de Trello

[OYL Frutos Secos – DSY1105](https://trello.com/invite/b/6b025a75b2bd272593e406c8/ATTIedf5e028767b3dc3e41dcb8eba7508802997A205/oyl-frutos-secos-dsy1105)
El tablero está organizado en tres columnas:

- **Por hacer:** tareas pendientes.
- **En proceso:** tareas en desarrollo.
- **Una columna por integrante:** tareas asignadas a cada uno (Bastián, Catalina y Suimey).
- **Terminado:** tareas completadas y verificadas.

### Distribución de tareas

Las actividades del proyecto se distribuyeron entre los integrantes del equipo, permitiendo organizar el trabajo y visualizar el avance de cada tarea.

### Evidencia del tablero Trello

La siguiente captura muestra la organización y el estado de las actividades del proyecto.

<img width="1035" height="359" alt="4" src="https://github.com/user-attachments/assets/fa3278ba-0255-472c-889c-fdcb0ee460b7" />

