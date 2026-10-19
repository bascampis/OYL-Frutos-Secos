# OYL Frutos Secos — Proyecto Android de DSY1105

Aplicación **académica y de demostración** (Duoc UC). Kotlin, Jetpack Compose, Material 3, MVVM, Room, Retrofit y backend Spring Boot + H2. Este proyecto **no está preparado para uso comercial**.

## Estado de desarrollo
**Código implementado en GitHub, no compilado ni validado en Android Studio después de la versión ZIP 01.**

- Bienvenida, login local, registro, validaciones desacopladas, roles, sesión persistente y perfil.
- Room para clientes registrados, catálogo en caché y carrito independiente por alias.
- Catálogo sintético con búsqueda, categorías, ficha simple y carrito editable.
- Confirmación de solicitudes desde el cliente mediante API REST; sin servidor, el pedido **no se confirma**.
- Historial de pedidos del backend para el cliente.
- Administración académica: edición/alta de productos, movimientos de stock y cambio de estados.
- QR con Google Code Scanner (Google Play Services y dispositivo compatible).
- Ubicación aproximada nativa (permiso) y enlace a aplicación de mapas externa; **sin puntos OYL confirmados**.
- Notificaciones **locales de prueba**, no push FCM; separadas por usuario.
- Tema verde, navegación Compose y animación de bienvenida.

### Sin terminar / pendiente de validar
- **Compilar y ejecutar todos los cambios en Android Studio**, pruebas de dispositivo, accesibilidad y pruebas de integración.
- Seguridad/autorización en API de administración, autenticación backend, endurecimiento de passwords y HTTPS.
- Notificaciones push reales desde servidor, puntos de retiro verificados y mapa integrado.
- Fotografías de productos mediante cámara y subida a backend.
- Flujos de pago (no exigidos para MVP), APK release y pruebas de concurrencia automatizadas.
- Formularios con iconos y errores visuales por cada campo en todas las pantallas.
- Trello compartido, evidencias de evaluación y nombre de integrantes (se debe completar).

## Requisitos
- Android Studio compatible con Android Gradle Plugin 8.9.2 y Kotlin 2.1.20.
- JDK 17 y Android SDK 35. Emulador Pixel con Google Play recomendado.
- Para pedidos/stock reales de **demostración**, Java 17 y Maven 3.9+ para el proyecto `backend/`.

## Ejecutar Android
1. Clona `https://github.com/bascampis/OYL-Frutos-Secos.git` o actualiza tu copia con `git pull origin main`.
2. Abre en Android Studio la **carpeta raíz** que contiene `settings.gradle.kts` (no solo `app`).
3. Sincroniza Gradle y ejecuta **Build > Make Project**. La primera vez requiere internet.
4. Selecciona emulador con Android 8+ y presiona Run.
5. Si falla la compilación, conserva la salida de **Build Output** para depurar antes de entregar.

Las credenciales académicas de demostración siguen siendo cliente `cliente` o administrador `admin@admin.cl`, contraseña ficticia `DemoOYL2026!`. No las utilices para sistemas reales. Puedes registrar cuentas inventadas con alias y correo de prueba. Nunca ingreses datos personales reales.

## Iniciar backend Spring Boot
1. Abre otra terminal en `backend/` y ejecuta `mvn spring-boot:run` (Java 17).
2. Comprueba `http://localhost:8080/api/health`, debe mostrar `ACADEMIC_DEMO`.
3. En el emulador, Android utiliza `http://10.0.2.2:8080/` para conectar con tu PC.
4. Entra al catálogo y verifica que diga **sincronizado con Spring Boot** antes de confirmar pedidos.
5. Si el backend está apagado, el catálogo muestra copia local ficticia y el botón de confirmar **debe fallar sin generar pedido**.

El cleartext HTTP se permite solo hacia `10.0.2.2` para laboratorio. Teléfono físico requiere adaptar IP/red y configuración de seguridad. **No expongas el backend a internet:** los endpoints administrativos no tienen autenticación ni control de roles en el servidor y el endpoint de historial usa alias suministrado por cliente.

## Pruebas manuales mínimas
1. Cliente y administrador ficticios: login, logout, persistencia de sesión.
2. Registro de dos perfiles ficticios, guardado/edición, comprobación de independencia.
3. Catálogo, filtros, detalle, carrito con cantidades independientes y persistencia al reiniciar.
4. Con backend: nuevo pedido, descuento stock y lectura de historial; sin backend: rechazo visible.
5. Administrador: cambiar precio, ajustar stock, cambiar estado de pedido y comprobar desde cliente.
6. QR: escanear código `OY001`, verificar ficha local; código inválido y cancelación.
7. Ubicación: aceptar/denegar permiso; notificación local y permiso Android 13+.
8. Ejecutar pruebas unitarias (validadores) y comprobar funcionamiento en emulador.

## GitHub, rúbrica y entrega
Mantener commits **reales** en `main`, documentar actividad del equipo y tareas en Trello. Revisar `docs/ESTADO_IMPLEMENTACION.md` y los PDF del caso. Antes de fecha límite guardar evidencias, APK release, ZIP de GitHub, README con integrantes y entrega AVA. **No realizar commits después de la fecha oficial de entrega**, ya que la pauta académica los penaliza.

## Aviso sobre Gradle Wrapper
El ZIP 01 incluyó un mecanismo de arranque compilado localmente en lugar del `gradle-wrapper.jar` oficial. Conviene regenerar y verificar el Wrapper oficial desde una instalación legítima de Gradle antes de la entrega, sin cambiar arbitrariamente la versión.

Licencia: no se ha seleccionado. Uso educativo, sin datos reales.
