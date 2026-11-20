# Guía Evaluación 4 — pasos que faltan (entrega lunes 23-nov, 08:00)

## 1. Dependencias de la app (`app/build.gradle.kts`, dentro de `dependencies { }`)
```kotlin
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
testImplementation("io.mockk:mockk:1.13.12")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
```

## 2. Permitir HTTP al backend (`app/src/main/AndroidManifest.xml`)
En la etiqueta `<application ...>` agrega `android:usesCleartextTraffic="true"` (solo laboratorio).
`INTERNET` ya está declarado.

## 3. Dirección del servidor (`ApiConfig.kt`)
- Emulador: `HOST = "10.0.2.2"`.
- Celular físico (tu Honor): `HOST = "<IP de tu PC>"` (ejecuta `ipconfig`, usa la IPv4 del WiFi). PC y celular en la misma red, y permite Java en el firewall de Windows.

## 4. Firmar el APK
Terminal en la raíz del proyecto (usa el `keytool` del JDK de Android Studio, carpeta `jbr/bin`):
```
keytool -genkeypair -v -keystore oyl-release.jks -alias oyl -keyalg RSA -keysize 2048 -validity 10000
```
Guarda `oyl-release.jks` en `app/`. En `app/build.gradle.kts`, dentro de `android { }`:
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("oyl-release.jks")
        storePassword = "TU_CLAVE"
        keyAlias = "oyl"
        keyPassword = "TU_CLAVE"
    }
}
buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        isMinifyEnabled = false
    }
}
```
Genera el APK: **Build > Generate Signed App Bundle / APK > APK > (elige el .jks) > release**, o
`./gradlew assembleRelease` → `app/build/outputs/apk/release/app-release.apk`.
Copia el APK a una carpeta `entrega/` del repo. Saca captura de pantalla de la configuración `signingConfigs` (la pide el README).
Usa una clave creada solo para este trabajo.

## 5. Probar todo (orden)
1. `mvn test` en `microservicios/` y `./gradlew testDebugUnitTest` en la app.
2. Levantar los 3 servicios; abrir `/api/health` de cada uno.
3. Correr la app: registro/login, catálogo, crear producto (admin), pedido (cliente), historial.
4. Apagar order-service y confirmar que la app muestra el mensaje de error, sin cerrarse.

## 6. Trello (foto del tablero)
Listas: Backlog · En progreso · En revisión · Hecho. Una tarjeta por tarea con responsable (una integrante por tarea), por ejemplo:
catalog-service · order-service · user-service · data.sql · Retrofit en la app · API externa (dólar) · Pruebas unitarias app · Pruebas unitarias backend · APK firmado · README.docx · Video individual.
Toma la foto con las tarjetas distribuidas entre las dos integrantes.

## 7. GitHub
- Cada integrante hace commits con su propia cuenta (mínimo 3 o 4 cada una) y los push se ven en `Insights > Contributors`.
- Borra la carpeta `backend/` (reemplazada por `microservicios/`).
- No se pueden subir cambios después del lunes 08:00 (nota 1,0).

## 8. Entregables AVA (checklist)
- [ ] App .zip  - [ ] Spring Boot .zip (`microservicios/`)  - [ ] BD poblada por `data.sql`  - [ ] APK firmada  - [ ] README.docx
- [ ] Activar el enlace individual en AVA (nombre, apellido, nº de equipo, nombre de la app).
- Defensa individual (Ev. 5): video breve con explicación técnica del proyecto funcionando.
