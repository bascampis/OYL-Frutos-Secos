# Carrito y confirmación — actualización sobre 33c4de0

## Instalar en tu proyecto actual

1. Guarda tus cambios actuales (commit o copia de respaldo).
2. Descomprime `OYL_Carrito_confirmacion.zip`.
3. Copia las carpetas `app` y `docs` del ZIP dentro de la raíz de tu proyecto OYL, donde está `settings.gradle.kts`.
4. Combina carpetas y reemplaza los archivos coincidentes. No borres tu carpeta `app` ni crees una carpeta `app/app`.
5. Abre Android Studio y ejecuta la aplicación. No se agregaron dependencias; no hace falta modificar Gradle, el Manifest ni el backend.
6. Para enviar pedidos, inicia el backend que ya tienes: desde la carpeta `backend`, ejecuta `mvn spring-boot:run`. La URL actual `10.0.2.2:8080` está preparada para el emulador.

Aplica los archivos juntos: navegación, carrito, confirmación, ViewModels, reglas y almacenamiento. Reemplazar solamente una pantalla deja referencias incompletas.

## Cambios

- Carrito con fotografías, precio unitario, cantidad y subtotal por artículo.
- Retiro sin costo. Se elimina el despacho de $2.000 que antes se sumaba a pedidos enviados como retiro.
- Botón Revisar pedido: consulta el servidor antes de abrir el resumen.
- Si cambia un precio, se actualiza la copia local y se pide revisar de nuevo antes de confirmar.
- Si no hay stock, se identifica el producto y la cantidad disponible; si un producto queda inactivo, sigue visible en el carrito para poder quitarlo.
- Revisión con artículos, cantidades, total, modalidad y datos de contacto del perfil, cuando existen. El contacto es una referencia local: esta actualización no modifica los campos de la API del pedido.
- Confirmación con artículos, cantidades, precios, total, fecha y estado devueltos por el backend.
- Se conserva una copia de la respuesta por usuario y pedido para restaurar el comprobante. Si falta, se intenta recuperar desde el historial del servidor.
- Fecha real también en Mis pedidos; se presenta en la zona horaria del dispositivo.
- Animación breve de éxito y accesos al historial y catálogo.
- La confirmación comprueba que el pedido pertenezca al cliente conectado.
- Los controles se bloquean mientras se procesa una acción y el botón de envío no admite otra confirmación una vez recibida la respuesta exitosa.

## Alcance

- Se implementa retiro. No se ofrece despacho porque el backend actual no calcula una tarifa de entrega.
- La confirmación requiere el backend disponible. El servidor vuelve a comprobar stock/precios al crear el pedido y descuenta stock dentro de su transacción existente.
- Si cambian precios entre revisión y envío, el servidor utiliza el precio vigente y ese es el total mostrado en el comprobante.
- La aplicación no realiza pagos.
- Si se corta la conexión después de pulsar Confirmar, el resultado puede ser incierto: consulta Mis pedidos antes de volver a enviar. El backend actual no tiene claves de idempotencia.
- La seguridad/autenticación del backend y la selección de punto de retiro quedan fuera de esta actualización.

## Verificación realizada

Se compilaron con Kotlin 2.1.20 y ejecutaron en JUnit 4.13.2:

- 8 pruebas nuevas de reglas de compra y fecha: precio cambiado, stock insuficiente, producto inactivo o ausente, carrito vacío, precio/cantidad inválidos y fecha del servidor.
- 5 pruebas existentes del validador de registro.
- 3 pruebas existentes del validador de login.

Resultado: **16 pruebas aprobadas**. También se ejecutó `git diff --check`.

Estas pruebas verifican lógica JVM. No se compiló la aplicación Android completa ni se ejecutaron sus pantallas en un emulador en este entorno. La ejecución anterior de GitHub Actions corresponde al commit base, no a esta actualización.

## Prueba manual en Android Studio

1. Inicia sesión como cliente y agrega dos productos.
2. Abre el carrito: verifica fotografías, subtotales y retiro sin costo.
3. Prueba +, − y eliminar. Al quitar todos los artículos debe aparecer el estado vacío.
4. Con el backend encendido, pulsa Revisar pedido. Debe aparecer el resumen y luego Confirmar pedido.
5. Confirma: coteja el total del comprobante con Mis pedidos. Deben coincidir y mostrar fecha real.
6. El carrito debe quedar vacío después del envío correcto.
7. Apaga el backend: Revisar pedido debe mostrar un mensaje y conservar el carrito.
8. Cambia un precio en el backend: al revisar debe actualizarlo y solicitar una nueva revisión.
9. Reduce el stock de un producto por debajo de la cantidad del carrito: el mensaje debe identificar ese producto. Quita o reduce la cantidad para continuar.
10. Prueba con dos clientes: cada uno debe ver sus propios artículos y pedidos.
11. Ejecuta `CheckoutRulesTest` y las pruebas existentes desde Android Studio, o `./gradlew :app:testDebugUnitTest`.
