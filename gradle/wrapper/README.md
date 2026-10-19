# Sobre gradle-wrapper.jar

Por limitación de red de este entorno, el archivo `gradle-wrapper.jar` incluido **no es el archivo oficial** de Gradle. Es un pequeño arrancador Java reproducible, cuyo código está en `bootstrap-src/org/gradle/wrapper/GradleWrapperMain.java`.

Qué hace: lee `gradle-wrapper.properties`, descarga por HTTPS `gradle-8.11.1-bin.zip` de Gradle, comprueba SHA-256 y ejecuta la distribución descargada. Se puede auditar y regenerar con:

```
javac --release 11 -d classes gradle/wrapper/bootstrap-src/org/gradle/wrapper/GradleWrapperMain.java
jar cf gradle/wrapper/gradle-wrapper.jar -C classes .
```

Se recomienda reemplazarlo por el `gradle-wrapper.jar` **oficial** generado por una instalación auténtica de Gradle 8.11.1 una vez esté disponible la red. No confundir este archivo con una distribución oficial firmada. No modificar ni eliminar `distributionSha256Sum`.
