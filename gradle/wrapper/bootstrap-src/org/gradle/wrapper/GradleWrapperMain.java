package org.gradle.wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Bootstrap educativo (NO el binario oficial de Gradle Wrapper).
 * Desarrollado para el paquete ZIP de OYL cuando el servicio que crea el
 * ZIP no puede obtener el JAR oficial. Usa la distribucion oficial con hash.
 * Se distribuye tambien el codigo fuente para que sea auditable.
 */
public final class GradleWrapperMain {
    private GradleWrapperMain() { }

    public static void main(String[] args) throws Exception {
        try {
            Path launcherJar = Path.of(GradleWrapperMain.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path project = launcherJar.getParent().getParent().getParent().toAbsolutePath();
            Path config = project.resolve("gradle/wrapper/gradle-wrapper.properties");
            Properties p = new Properties();
            try (InputStream stream = Files.newInputStream(config)) { p.load(stream); }
            String link = p.getProperty("distributionUrl");
            String sha = p.getProperty("distributionSha256Sum");
            if (link == null || !link.startsWith("https://services.gradle.org/distributions/gradle-8.11.1-bin.zip")) {
                throw new IllegalStateException("La URL Gradle configurada no es la aprobada para ZIP 01");
            }
            if (sha == null || !sha.matches("[a-fA-F0-9]{64}")) {
                throw new IllegalStateException("Falta SHA-256 de la distribucion");
            }
            Path cache = Path.of(System.getProperty("user.home"), ".gradle", "wrapper", "dists", "oyl-bootstrap-8.11.1");
            Path home = cache.resolve("gradle-8.11.1");
            Path marker = cache.resolve(".ready");
            if (!Files.exists(marker)) {
                Files.createDirectories(cache);
                Path zipFile = cache.resolve("gradle-8.11.1-bin.zip");
                if (!Files.exists(zipFile) || !sha256(zipFile).equalsIgnoreCase(sha)) {
                    Files.deleteIfExists(zipFile);
                    Path tmp = cache.resolve("download.tmp");
                    System.out.println("Descargando Gradle 8.11.1 (primera ejecucion; requiere internet)...");
                    HttpClient client = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .connectTimeout(Duration.ofSeconds(30)).build();
                    HttpRequest request = HttpRequest.newBuilder(URI.create(link))
                        .timeout(Duration.ofMinutes(10)).header("User-Agent", "OYL-Gradle-Bootstrap/1.0")
                        .GET().build();
                    HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
                    if (response.statusCode() != 200) {
                        response.body().close();
                        throw new IOException("Respuesta HTTP al descargar Gradle: " + response.statusCode());
                    }
                    try (InputStream in = response.body()) { Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING); }
                    if (!sha256(tmp).equalsIgnoreCase(sha)) {
                        Files.deleteIfExists(tmp);
                        throw new SecurityException("SHA-256 de Gradle no coincide con el oficial configurado");
                    }
                    Files.move(tmp, zipFile, StandardCopyOption.REPLACE_EXISTING);
                }
                System.out.println("Instalando Gradle 8.11.1...");
                unzipSafely(zipFile, cache);
                if (!Files.isDirectory(home.resolve("bin"))) throw new IOException("Distribucion incompleta");
                Files.writeString(marker, "Gradle 8.11.1 verificado con SHA-256\n");
            }
            boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
            Path cli = home.resolve(windows ? "bin/gradle.bat" : "bin/gradle");
            if (!Files.isRegularFile(cli)) throw new IOException("Falta ejecutable Gradle: " + cli);
            List<String> command = new ArrayList<>();
            if (windows) {
                command.add("cmd.exe"); command.add("/c"); command.add(cli.toString());
            } else {
                cli.toFile().setExecutable(true);
                command.add(cli.toString());
            }
            command.addAll(Arrays.asList(args));
            int code = new ProcessBuilder(command).directory(project.toFile()).inheritIO().start().waitFor();
            System.exit(code);
        } catch (Exception ex) {
            System.err.println("No se pudo iniciar Gradle: " + ex);
            System.err.println("Consulta README.md / Solucion de problemas.");
            System.exit(1);
        }
    }

    private static String sha256(Path file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buf = new byte[65536]; int count;
            while ((count = in.read(buf)) != -1) md.update(buf, 0, count);
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : md.digest()) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    private static void unzipSafely(Path archive, Path dest) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(archive))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                Path output = dest.resolve(entry.getName()).normalize();
                if (!output.startsWith(dest.normalize())) throw new SecurityException("Ruta ZIP fuera del destino");
                if (entry.isDirectory()) Files.createDirectories(output);
                else {
                    Files.createDirectories(output.getParent());
                    Files.copy(zip, output, StandardCopyOption.REPLACE_EXISTING);
                }
                zip.closeEntry();
            }
        }
    }
}
