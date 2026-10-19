# Estado técnico y seguimiento de evaluación

Fecha de revisión: 2026-10-18. Esta tabla describe **código escrito**, no validación en dispositivo.

| Módulo | Código | Validación pendiente |
|---|---|---|
| Bienvenida y login inicial | Probado por estudiante en ZIP 01 | Repetir regresión |
| Registro, roles, perfiles y Room | Incorporado | Gradle Sync, registro multiusuario |
| Catálogo, filtros y detalle simple | Incorporado con datos sintéticos | UI en emulador |
| Carrito por alias | Incorporado en Room | Persistencia y separación entre dos cuentas |
| Backend productos y pedidos | Incorporado con H2 y Spring Boot | Iniciar Maven, validar transacción y stock |
| Retrofit Android | Incorporado | Conexión desde emulador a 10.0.2.2 |
| Administración productos/stock/pedidos | Incorporado solo demo sin auth API | Pruebas funcionales y seguridad |
| QR Code Scanner | Incorporado | Teléfono/emulador con Play Services |
| Ubicación | Incorporado | Solicitar permisos y obtener posición |
| Notificaciones | Locales de demostración | Validar Android 13+, push sin desarrollar |
| Fotografía de producto | No implementada | Cámara y subida |
| Mapa integrado con puntos OYL | No implementado | Ubicaciones oficiales confirmadas |
| Accesibilidad/animaciones completas | Parcial | Pruebas y mejoras |
| APK release y entrega AVA | No realizado | Compilar, firmar y entregar |

## Rúbrica DSY1105
- IE 2.1.1 (15%): navegación y diseño visual requieren revisión integral.
- IE 2.1.2 (15%): formularios existen parcialmente; completar feedback por campo.
- IE 2.2.1 (10%): validaciones aisladas y estado MVVM en auth; auditar nuevas pantallas.
- IE 2.2.2 (10%): animación bienvenida; requieren más animaciones funcionales.
- IE 2.3.1 (15%): Room implementado, falta demostrar persistencia real.
- IE 2.3.2 (20%): historial de GitHub real; Trello y participación equipo aún pendientes.
- IE 2.4.1 (15%): recursos QR/ubicación están en código; falta prueba en dispositivo.

## Riesgos conocidos
- No hay autenticación ni autorización en endpoints admin; solo red de prueba aislada.
- Contraseñas cliente: hash con sal para demo, no se considera solución de producción.
- El catálogo sin conexión es ficticio y no debe usarse como stock oficial.
- Los identificadores de cliente suministrados a API son ficticios y no validan identidad real.
- No se ha ejecutado el SDK Android ni Maven en la sesión que publicó estos commits.

## Protocolo de verificación
1. Compilar backend con `mvn test`, luego `mvn spring-boot:run`.
2. Probar endpoints `/api/health`, `/api/products` y pedidos.
3. Compilar Android y ejecutar pruebas unitarias.
4. Ejecutar flujo completo de compra y administración en emulador.
5. Corregir errores encontrados antes de considerar módulos completados.
