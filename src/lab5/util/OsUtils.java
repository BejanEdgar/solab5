package lab5.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;

/** Utilitare comune: detectarea sistemului de operare și rularea comenzilor externe. */
public final class OsUtils {

    private static final String OS = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);

    private OsUtils() {
    }

    public static boolean isWindows() {
        return OS.contains("win");
    }

    public static boolean isMac() {
        return OS.contains("mac");
    }

    /** Linux sau alt sistem de tip Unix (tot ce nu e Windows / macOS). */
    public static boolean isLinux() {
        return !isWindows() && !isMac();
    }

    /** Rezultatul unei comenzi: codul de terminare + ieșirea (stdout și stderr împreună). */
    public static final class CommandResult {
        public final int exitCode;
        public final String output;

        CommandResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }

        public boolean ok() {
            return exitCode == 0;
        }
    }

    /**
     * Rulează o comandă, așteaptă terminarea și returnează codul + ieșirea.
     * Argumentele se dau ca listă (nu ca un singur șir), deci spațiile din
     * căi nu produc probleme.
     */
    public static CommandResult run(List<String> command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output;
        try (InputStream in = process.getInputStream()) {
            output = new String(in.readAllBytes(), consoleCharset());
        }
        int exitCode = process.waitFor();
        return new CommandResult(exitCode, output.trim());
    }

    /** Verifică dacă un executabil există în PATH. */
    public static boolean commandExists(String name) {
        String path = System.getenv("PATH");
        if (path == null) {
            return false;
        }
        for (String dir : path.split(File.pathSeparator)) {
            if (dir.isEmpty()) {
                continue;
            }
            File candidate = new File(dir, name);
            if (candidate.isFile() && candidate.canExecute()) {
                return true;
            }
            if (isWindows() && new File(dir, name + ".exe").isFile()) {
                return true;
            }
        }
        return false;
    }

    private static Charset consoleCharset() {
        String nativeEncoding = System.getProperty("native.encoding"); // Java 17+
        if (nativeEncoding != null) {
            try {
                return Charset.forName(nativeEncoding);
            } catch (RuntimeException ignored) {
                // cădem pe valoarea implicită
            }
        }
        return Charset.defaultCharset();
    }
}
