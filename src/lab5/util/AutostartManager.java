package lab5.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Adaugă / elimină programe din pornirea automată a sistemului (fără drepturi de administrator).
 *
 * <ul>
 *   <li>Windows: cheile de registru {@code HKCU\...\Run} (la fiecare autentificare) și
 *       {@code HKCU\...\RunOnce} (o singură dată, apoi Windows șterge intrarea);</li>
 *   <li>Linux: fișier {@code ~/.config/autostart/<nume>.desktop}.</li>
 * </ul>
 *
 * Exemplu (pentru Task 4):
 * <pre>
 *   AutostartManager.enable("Lab5CpuMonitor",
 *           AutostartManager.selfCommand("--task", "cpu-monitor"));
 * </pre>
 */
public final class AutostartManager {

    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_.-]+");

    private static final String RUN_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    private static final String RUN_ONCE_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\RunOnce";

    private AutostartManager() {
    }

    // ------------------------------------------------------------------ API public

    /** Pornire la fiecare autentificare a utilizatorului. */
    public static void enable(String name, List<String> command) throws IOException {
        install(name, command, false);
    }

    /**
     * Pornire o singură dată, la următoarea autentificare.
     * Windows șterge singur intrarea; pe Linux aplicația trebuie să apeleze
     * {@link #removeOnce(String)} după ce rulează.
     */
    public static void enableOnce(String name, List<String> command) throws IOException {
        install(name, command, true);
    }

    /** Elimină pornirea automată permanentă. Nu aruncă eroare dacă intrarea nu există. */
    public static void disable(String name) throws IOException {
        remove(name, false);
    }

    /** Elimină o intrare „o singură dată" (ex. dacă utilizatorul anulează repornirea). */
    public static void removeOnce(String name) throws IOException {
        remove(name, true);
    }

    /** Există pornirea automată permanentă? */
    public static boolean isEnabled(String name) {
        checkName(name);
        try {
            if (OsUtils.isWindows()) {
                return exec(Arrays.asList("reg", "query", "HKCU\\" + RUN_KEY, "/v", name)).ok();
            }
            return Files.exists(linuxEntry(name));
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Comanda care relansează ACEASTĂ aplicație (din .jar sau din directorul de clase),
     * urmată de argumentele date. Folosește {@code javaw} pe Windows, ca să nu apară consolă.
     */
    public static List<String> selfCommand(String... appArgs) throws IOException {
        List<String> command = new ArrayList<>();
        String javaExe = OsUtils.isWindows() ? "javaw.exe" : "java";
        command.add(Path.of(System.getProperty("java.home"), "bin", javaExe).toString());

        Path location;
        try {
            location = Path.of(AutostartManager.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).toAbsolutePath();
        } catch (URISyntaxException | RuntimeException e) {
            throw new IOException("Nu pot determina locația aplicației: " + e.getMessage(), e);
        }
        if (Files.isRegularFile(location) && location.toString().endsWith(".jar")) {
            command.add("-jar");
            command.add(location.toString());
        } else {
            command.add("-cp");
            command.add(location.toString());
            command.add("lab5.Main");
        }
        command.addAll(Arrays.asList(appArgs));
        return command;
    }

    // ------------------------------------------------------------------ implementare

    private static void install(String name, List<String> command, boolean once) throws IOException {
        checkName(name);
        if (command == null || command.isEmpty()) {
            throw new IllegalArgumentException("Comanda nu poate fi goală.");
        }
        if (OsUtils.isWindows()) {
            installWindows(name, command, once);
        } else if (OsUtils.isMac()) {
            throw new IOException("Pornirea automată nu este implementată pentru macOS.");
        } else {
            Path entry = linuxEntry(name);
            Files.createDirectories(entry.getParent());
            Files.write(entry, desktopFileContent(name, command).getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void remove(String name, boolean once) throws IOException {
        checkName(name);
        if (OsUtils.isWindows()) {
            String key = "HKCU\\" + (once ? RUN_ONCE_KEY : RUN_KEY);
            if (exec(Arrays.asList("reg", "query", key, "/v", name)).ok()) {
                OsUtils.CommandResult r = exec(Arrays.asList("reg", "delete", key, "/v", name, "/f"));
                if (!r.ok()) {
                    throw new IOException("reg delete a eșuat: " + r.output);
                }
            }
        } else if (!OsUtils.isMac()) {
            Files.deleteIfExists(linuxEntry(name));
        }
    }

    private static void installWindows(String name, List<String> command, boolean once) throws IOException {
        String key = "HKEY_CURRENT_USER\\" + (once ? RUN_ONCE_KEY : RUN_KEY);
        // Importăm printr-un fișier .reg: evităm complet problemele de ghilimele
        // dintre Java, reg.exe și căile cu spații.
        Path regFile = Files.createTempFile("lab5-", ".reg");
        try {
            Files.write(regFile, regFileBytes(key, name, joinWindows(command)));
            OsUtils.CommandResult r = exec(Arrays.asList("reg", "import", regFile.toString()));
            if (!r.ok()) {
                throw new IOException("reg import a eșuat (cod " + r.exitCode + "): " + r.output);
            }
        } finally {
            Files.deleteIfExists(regFile);
        }
    }

    private static Path linuxEntry(String name) {
        String xdg = System.getenv("XDG_CONFIG_HOME");
        Path configDir = (xdg != null && !xdg.isEmpty())
                ? Path.of(xdg)
                : Path.of(System.getProperty("user.home"), ".config");
        return configDir.resolve("autostart").resolve(name + ".desktop");
    }

    private static OsUtils.CommandResult exec(List<String> command) throws IOException {
        try {
            return OsUtils.run(command);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Operațiune întreruptă.", e);
        }
    }

    private static void checkName(String name) {
        if (name == null || !VALID_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "Nume invalid pentru pornirea automată (permis: litere, cifre, _ . -): " + name);
        }
    }

    // ------------------------------------------------------------------ formatare (testabilă)

    /** Un argument citat după regulile liniei de comandă Windows (CommandLineToArgvW). */
    static String quoteWindows(String arg) {
        boolean plain = !arg.isEmpty() && arg.chars().noneMatch(c -> c == ' ' || c == '\t' || c == '"');
        if (plain) {
            return arg;
        }
        StringBuilder sb = new StringBuilder("\"");
        int backslashes = 0;
        for (char c : arg.toCharArray()) {
            if (c == '\\') {
                backslashes++;
            } else if (c == '"') {
                sb.append("\\".repeat(backslashes * 2 + 1)).append('"');
                backslashes = 0;
            } else {
                sb.append("\\".repeat(backslashes)).append(c);
                backslashes = 0;
            }
        }
        sb.append("\\".repeat(backslashes * 2)).append('"');
        return sb.toString();
    }

    static String joinWindows(List<String> command) {
        StringBuilder sb = new StringBuilder();
        for (String arg : command) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(quoteWindows(arg));
        }
        return sb.toString();
    }

    /** Conținutul unui fișier .reg care setează o singură valoare șir. */
    static String regFileContent(String key, String name, String value) {
        return "Windows Registry Editor Version 5.00\r\n\r\n"
                + "[" + key + "]\r\n"
                + "\"" + regEscape(name) + "\"=\"" + regEscape(value) + "\"\r\n";
    }

    /** Fișierele .reg trebuie să fie UTF-16 LE cu BOM. */
    static byte[] regFileBytes(String key, String name, String value) {
        byte[] body = regFileContent(key, name, value).getBytes(StandardCharsets.UTF_16LE);
        byte[] bytes = new byte[body.length + 2];
        bytes[0] = (byte) 0xFF;
        bytes[1] = (byte) 0xFE;
        System.arraycopy(body, 0, bytes, 2, body.length);
        return bytes;
    }

    private static String regEscape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** Conținutul unui fișier .desktop pentru ~/.config/autostart. */
    static String desktopFileContent(String name, List<String> command) {
        StringBuilder exec = new StringBuilder();
        for (String arg : command) {
            if (exec.length() > 0) {
                exec.append(' ');
            }
            exec.append(quoteDesktop(arg));
        }
        return "[Desktop Entry]\n"
                + "Type=Application\n"
                + "Name=" + name + "\n"
                + "Exec=" + exec + "\n"
                + "Terminal=false\n"
                + "X-GNOME-Autostart-enabled=true\n";
    }

    /** Citare pentru cheia Exec din specificația Desktop Entry. */
    static String quoteDesktop(String arg) {
        String a = arg.replace("%", "%%");
        if (!a.isEmpty() && !Pattern.compile("[\\s\"'\\\\<>~|&;$*?#()`]").matcher(a).find()) {
            return a;
        }
        StringBuilder sb = new StringBuilder("\"");
        for (char c : a.toCharArray()) {
            if (c == '"' || c == '`' || c == '$' || c == '\\') {
                sb.append('\\');
            }
            sb.append(c);
        }
        sb.append('"');
        // Al doilea nivel: backslash-ul însuși se dublează în valorile din fișierul .desktop.
        return sb.toString().replace("\\", "\\\\");
    }
}
