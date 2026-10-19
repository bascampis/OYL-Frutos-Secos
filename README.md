# OYL Frutos Secos — Android / ZIP 01

Proyecto académico de **DSY1105, Duoc UC**, desarrollado en **Kotlin**, **Jetpack Compose**, **Material Design 3** y **MVVM**.

**Estado:** versión base 0.1.0 (ZIP 01). Este repositorio es un MVP en construcción, **no es un sistema de ventas real**.

## Qué funciona en ZIP 01

- Pantalla de bienvenida con animación de entrada.
- Navegación real entre Bienvenida → Inicio de sesión → Panel; pantalla «Acerca del proyecto».
- Cambio de rol **Cliente** / **Administrador**.
- Formulario con validaciones separadas en `domain/validation` y retroalimentación en los campos.
- Autenticación **local de demostración**, sin conexión remota, con mensajes de error.
- Pantalla de inicio diferenciada por rol, botón «Acerca de» y cierre de sesión.
- Pruebas unitarias escritas para la validación del formulario.

### Cuenta ficticia para ingresar

| Rol | Alias/correo | Contraseña ficticia |
|---|---|---|
| Cliente | `cliente` | `DemoOYL2026!` |
| Administrador | `admin@admin.cl` | `DemoOYL2026!` |

En el formulario puedes pulsar **«Rellenar credenciales de demostración»** después de elegir el rol. Estas cuentas están codificadas solo para **enseñanza**, no para uso real. El PDF especifica el correo del administrador, **no especifica su contraseña**: aquí se inventó exclusivamente para pruebas. No hay datos de usuarios reales ni contraseñas de producción.

## Cómo abrir por primera vez en Android Studio (Windows)

1. Descarga el archivo `OYL_Frutos_Secos_ZIP01.zip` y extrae todo su contenido, por ejemplo en `Documentos/OYL-Proyecto/`.
2. Abre **Android Studio → Open** y selecciona **la carpeta `OYL-Frutos-Secos`** (la que contiene `settings.gradle.kts`, `app` y `gradlew.bat`). **No** abras solo `app`.
3. Si Android Studio pregunta si confías en el proyecto, confirma solo si el ZIP provino de esta conversación y revisaste su contenido.
4. Espera a que termine **Gradle Sync**. Necesitas internet **en tu PC**, JDK 17 y **Android SDK 35** instalados. La primera vez se descargan Gradle 8.11.1 y las dependencias.
5. Abre **Tools → Device Manager** y crea/inicia un emulador con Android 8.0 (API 26) o superior, o conecta un teléfono con depuración USB.
6. En la barra superior, selecciona la configuración **app** y pulsa **Run ▶**. Espera a ver «OYL Frutos Secos».
7. Pulsa **Comenzar → Cliente → Rellenar credenciales de demostración → Iniciar sesión**. Comprueba que aparece el panel Cliente. Pulsa **Salir**. Repite con Administrador.

Si **Run** está desactivado o hay error, envía una captura de **Build Output** o el mensaje exacto. No borres carpetas de sistema ni cambies versiones al azar.

## Comprobar desde la terminal (opcional)

Abre **Terminal** en Android Studio, en la carpeta raíz (junto a `gradlew.bat`):

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

El APK de depuración, si compila, estará en `app/build/outputs/apk/debug/app-debug.apk`.

> **Comprobación real de esta entrega:** la estructura, XML y archivo ZIP fueron comprobados fuera de Android Studio. Las reglas puras de validación se ejecutaron localmente con Kotlin. **No se compiló el APK** porque el entorno que creó este ZIP no tiene instalado Android SDK ni puede descargar Gradle y las librerías Android. La compilación completa debe comprobarse en tu PC.

## Gradle Wrapper: detalle de procedencia

Esta entrega incluye `gradlew`, `gradlew.bat`, `gradle-wrapper.properties` y `gradle-wrapper.jar`. **El JAR es un bootstrap Java pequeño, con código fuente visible en `gradle/wrapper/bootstrap-src`, NO es el binario oficial del Gradle Wrapper**, pues este entorno no pudo descargarlo.

El bootstrap descarga exclusivamente Gradle **8.11.1** desde `services.gradle.org`, valida el SHA-256 esperado antes de extraerlo y lanza Gradle. Para tener **el Gradle Wrapper oficial** después de una sincronización exitosa, en la raíz del proyecto puedes ejecutar `gradle wrapper --gradle-version 8.11.1 --distribution-type bin` usando una instalación oficial de Gradle, o pedir al profesor / equipo que genere el wrapper desde Android Studio y compartir su `gradle-wrapper.jar` oficial. El JAR de esta entrega es auditable, pero no debe hacerse pasar por un binario firmado de Gradle.

## Arquitectura y carpetas

```text
OYL-Frutos-Secos/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/cl/oyl/frutossecos/
│       │   │   ├── MainActivity.kt
│       │   │   ├── navigation/       # Rutas Navigation Compose
│       │   │   ├── ui/screens/       # Pantallas
│       │   │   ├── ui/components/    # Componentes reutilizables
│       │   │   ├── ui/theme/         # Tema Material 3
│       │   │   ├── viewmodel/        # Estado y eventos, MVVM
│       │   │   ├── data/repository/  # Interfaz y repositorio demo
│       │   │   ├── data/local/       # Reservado, Room en ZIP 02
│       │   │   ├── data/remote/      # Reservado, Retrofit más adelante
│       │   │   └── domain/validation/ # Reglas independientes de UI
│       │   └── res/
│       └── test/                 # Tests JUnit
├── gradle/wrapper/
├── docs/
├── gradlew, gradlew.bat
├── settings.gradle.kts
└── build.gradle.kts
```

Paquete y aplicación: `cl.oyl.frutossecos` (mantener en todas las etapas). Gradle Kotlin DSL, Java 17, minSdk 26, compileSdk/targetSdk 35, AGP 8.9.2, Kotlin/Compose plugin 2.1.20 y Compose BOM 2025.04.01. El tema utiliza verde, blanco y tonos naturales de la referencia visual del caso.

## Lo que todavía NO funciona

- **ZIP 02:** registro, perfiles y sesiones persistentes con Room/SQLite.
- **ZIP 03:** catálogo, categorías, búsqueda, fichas y carrito separado por cliente.
- **ZIP 04:** confirmación e historial de pedidos, administración, Retrofit y backend Spring Boot real o demostración señalizada (sin fingir stock oficial).
- **ZIP 05:** escáner QR por cámara, mapa/ubicación y permisos.
- **ZIP 06:** animaciones adicionales, notificaciones, accesibilidad, más pruebas y evidencia.

**No hay** Room, Retrofit, Spring Boot, cámara, GPS, QR, pedidos ni stock sincronizado en esta versión. Las tarjetas del panel muestran los módulos futuros como texto **no interactivo** para no crear botones falsos.

## Git y colaboración

Crea un repositorio público **cuando el proyecto esté abierto y probado**. Si es nuevo:

```bash
git init
git branch -M main
git status
git add .
git commit -m "feat(android): crear base Compose MVVM con login demo"
git remote add origin URL_DE_TU_REPOSITORIO
git push -u origin main
```

No ejecutes `git init` si esta carpeta **ya es un repositorio Git**. Si trabajas con un proyecto grupal existente, coordina primero con el equipo y no sustituyas su `.git` ni su historial. Revisa `git status` antes de cada commit. `local.properties`, llaves y archivos compilados quedan excluidos con `.gitignore`.

**Importante:** la pauta indica que **no se deben hacer cambios en el repositorio entre la entrega y la evaluación**. Consulta la fecha exacta de tu sección antes de entregar. También menciona trabajo en parejas; si trabajan tres, confírmenlo con su docente.

Para planificación y evaluación consulta `docs/ESTADO_RUBRICA.md`, `docs/PLAN_TRELLO.md` y `docs/REFERENCIAS_PDF.md`.

## Responsables

Completar antes de entregar: nombres del equipo, sección, docente, número de equipo, enlace de GitHub y enlace de Trello. No se colocan datos personales ficticios.
