package lab5.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lansează un editor de text extern (Notepad pe Windows, Gedit & co. pe Linux, TextEdit pe macOS).
 *
 * Pe Linux, multe editoare sunt „single-instance": dacă mai rulează o fereastră, comanda se
 * întoarce imediat și lasă fereastra existentă să deschidă fișierul. De aceea adăugăm opțiunea
 * care forțează o instanță proprie, ca {@code waitFor()} să aștepte cu adevărat închiderea.
 */
public final class EditorLauncher {

    /** editor -> opțiuni suplimentare care îl fac să „blocheze" până la închidere. */
    private static final Map<String, List<String>> LINUX_EDITORS = new LinkedHashMap<>();

    static {
        LINUX_EDITORS.put("gedit", Arrays.asList("--standalone"));
        LINUX_EDITORS.put("gnome-text-editor", Arrays.<String>asList());
        LINUX_EDITORS.put("kate", Arrays.asList("--block"));
        LINUX_EDITORS.put("mousepad", Arrays.asList("--disable-server"));
        LINUX_EDITORS.put("xed", Arrays.asList("--standalone"));
        LINUX_EDITORS.put("pluma", Arrays.asList("--standalone"));
        LINUX_EDITORS.put("kwrite", Arrays.asList("--block"));
    }

    private EditorLauncher() {
    }

    /** Numele editorului ales automat pe acest sistem sau {@code null} dacă nu s-a găsit niciunul. */
    public static String defaultEditorName() {
        if (OsUtils.isWindows()) {
            return "notepad.exe";
        }
        if (OsUtils.isMac()) {
            return "TextEdit";
        }
        for (String editor : LINUX_EDITORS.keySet()) {
            if (OsUtils.commandExists(editor)) {
                return editor;
            }
        }
        return null;
    }

    /**
     * Comanda completă pentru a deschide {@code file}.
     *
     * @param customEditor executabil ales de utilizator; gol/null = alegere automată
     */
    public static List<String> command(String customEditor, Path file) throws IOException {
        List<String> command = new ArrayList<>();
        if (customEditor != null && !customEditor.trim().isEmpty()) {
            command.add(customEditor.trim());
            command.add(file.toString());
            return command;
        }

        String editor = defaultEditorName();
        if (editor == null) {
            throw new IOException("Nu am găsit niciun editor de text (gedit, kate, mousepad…). "
                    + "Introduceți manual comanda editorului.");
        }
        if (OsUtils.isMac()) {
            command.addAll(Arrays.asList("open", "-W", "-n", "-a", editor));
        } else {
            command.add(editor);
            List<String> extra = LINUX_EDITORS.get(editor);
            if (extra != null) {
                command.addAll(extra);
            }
        }
        command.add(file.toString());
        return command;
    }

    /** Pornește editorul și returnează procesul (apelantul poate face {@code waitFor()}). */
    public static Process launch(String customEditor, Path file) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command(customEditor, file));
        // Ieșirea editorului nu ne interesează; fără asta, un buffer plin ar putea bloca procesul.
        builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        builder.redirectError(ProcessBuilder.Redirect.DISCARD);
        return builder.start();
    }
}
