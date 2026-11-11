package lab5;

import lab5.tasks.Task01EditorRestart;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punct de intrare.
 *
 * Argumente acceptate:
 * <ul>
 *   <li>(niciunul) – pornește fereastra principală;</li>
 *   <li>{@code --task <id>} – pornește fereastra cu tab-ul respectiv selectat;</li>
 *   <li>{@code --open-file <cale> [--editor <comandă>]} – folosit intern de Task 1
 *       după repornirea sistemului: deschide fișierul în editor și se închide.</li>
 * </ul>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        // Mod fără fereastră: acțiunea de după repornire (Task 1).
        if (Task01EditorRestart.handleStartupArgs(args)) {
            return;
        }

        final String initialTaskId = argValue(args, "--task");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // rămânem pe look-and-feel-ul implicit
            }
            new MainFrame(initialTaskId).setVisible(true);
        });
    }

    private static String argValue(String[] args, String name) {
        for (int i = 0; i < args.length - 1; i++) {
            if (name.equals(args[i])) {
                return args[i + 1];
            }
        }
        return null;
    }
}
