package lab5.tasks;

import lab5.LabTask;
import lab5.util.AutostartManager;
import lab5.util.EditorLauncher;
import lab5.util.OsUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * TASK 1: lansează un editor de text, așteaptă închiderea lui, afișează codul de
 * terminare al procesului, repornește sistemul și, după repornire, deschide fișierul creat.
 *
 * Cum se deschide fișierul după repornire: înainte de repornire se înregistrează o intrare
 * „o singură dată" în pornirea automată ({@link AutostartManager#enableOnce}), care rulează
 * aplicația cu {@code --open-file <cale>}; vezi {@link #handleStartupArgs(String[])}.
 */
public class Task01EditorRestart implements LabTask {

    public static final String ID = "editor-restart";

    /** Numele intrării temporare din pornirea automată. */
    static final String ONCE_NAME = "Lab5OpenFileAfterReboot";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String title() {
        return "Task 1 – Editor + repornire";
    }

    @Override
    public JComponent createPanel() {
        return new Panel();
    }

    // ------------------------------------------------------------------ după repornire

    /**
     * Apelat din {@code Main}. Dacă argumentele conțin {@code --open-file}, deschide fișierul în
     * editor, curăță intrarea temporară din pornirea automată și returnează {@code true}
     * (aplicația se închide fără fereastră). Altfel returnează {@code false}.
     */
    public static boolean handleStartupArgs(String[] args) {
        String file = null;
        String editor = null;
        for (int i = 0; i < args.length - 1; i++) {
            if ("--open-file".equals(args[i])) {
                file = args[i + 1];
            } else if ("--editor".equals(args[i])) {
                editor = args[i + 1];
            }
        }
        if (file == null) {
            return false;
        }

        try {
            EditorLauncher.launch(editor, Path.of(file));
        } catch (IOException | InvalidPathException e) {
            final String message = "Nu am putut deschide fișierul după repornire:\n" + e.getMessage();
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(null, message, "Laborator 5", JOptionPane.ERROR_MESSAGE));
        }
        try {
            AutostartManager.removeOnce(ONCE_NAME); // pe Linux fișierul .desktop rămâne altfel
        } catch (IOException ignored) {
            // nu e critic
        }
        return true;
    }

    // ------------------------------------------------------------------ interfața

    private static final class Panel extends JPanel {

        private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

        private final JTextField fileField = new JTextField(32);
        private final JTextField editorField = new JTextField(32);
        private final JCheckBox simulation = new JCheckBox(
                "Mod simulare – NU repornește sistemul, fișierul se deschide imediat", true);
        private final JButton browseButton = new JButton("Alege…");
        private final JButton startButton = new JButton("Pornește editorul");
        private final JButton cancelButton = new JButton("Anulează repornirea");
        private final JTextArea log = new JTextArea();
        private final JLabel status = new JLabel("Gata.");

        Panel() {
            super(new BorderLayout(8, 8));
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            fileField.setText(Path.of(System.getProperty("user.home"), "lab5_text.txt").toString());
            String detected = EditorLauncher.defaultEditorName();
            JLabel editorHint = new JLabel("Gol = automat (detectat: "
                    + (detected != null ? detected : "niciunul – introduceți manual") + ")");
            editorHint.setFont(editorHint.getFont().deriveFont(Font.ITALIC));

            JPanel form = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(3, 3, 3, 3);
            c.anchor = GridBagConstraints.WEST;

            c.gridx = 0; c.gridy = 0;
            form.add(new JLabel("Fișier text:"), c);
            c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
            form.add(fileField, c);
            c.gridx = 2; c.weightx = 0; c.fill = GridBagConstraints.NONE;
            form.add(browseButton, c);

            c.gridx = 0; c.gridy = 1;
            form.add(new JLabel("Editor:"), c);
            c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
            form.add(editorField, c);

            c.gridx = 1; c.gridy = 2;
            form.add(editorHint, c);

            c.gridx = 0; c.gridy = 3; c.gridwidth = 3;
            form.add(simulation, c);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
            cancelButton.setEnabled(false);
            buttons.add(startButton);
            buttons.add(cancelButton);

            JPanel north = new JPanel(new BorderLayout());
            north.add(form, BorderLayout.CENTER);
            north.add(buttons, BorderLayout.SOUTH);

            log.setEditable(false);
            log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

            add(north, BorderLayout.NORTH);
            add(new JScrollPane(log), BorderLayout.CENTER);
            add(status, BorderLayout.SOUTH);

            browseButton.addActionListener(e -> chooseFile());
            startButton.addActionListener(e -> start());
            cancelButton.addActionListener(e -> cancelReboot());

            appendLog("Pași: 1) editorul se lansează și aplicația așteaptă să-l închideți; "
                    + "2) se afișează codul de terminare; 3) sistemul se repornește; "
                    + "4) după repornire fișierul se deschide automat.");
        }

        // -------------------------------------------------------------- acțiuni

        private void chooseFile() {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new java.io.File(fileField.getText().trim()));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                fileField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        }

        private void start() {
            String fileText = fileField.getText().trim();
            if (fileText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Indicați fișierul text.", "Laborator 5",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Path file;
            try {
                file = Path.of(fileText).toAbsolutePath();
            } catch (InvalidPathException e) {
                JOptionPane.showMessageDialog(this, "Cale invalidă: " + e.getMessage(), "Laborator 5",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            setBusy(true);
            setStatus("Editorul rulează – închideți-l pentru a continua…");
            new EditorWorker(file, editorField.getText().trim()).execute();
        }

        /** Lansează editorul și așteaptă terminarea lui, fără a bloca interfața. */
        private final class EditorWorker extends SwingWorker<Integer, String> {
            private final Path file;
            private final String editor;
            private volatile long elapsedMs;

            EditorWorker(Path file, String editor) {
                this.file = file;
                this.editor = editor;
            }

            @Override
            protected Integer doInBackground() throws Exception {
                if (file.getParent() != null) {
                    Files.createDirectories(file.getParent());
                }
                if (!Files.exists(file)) {
                    Files.createFile(file);
                    publish("Fișier creat: " + file);
                }
                List<String> command = EditorLauncher.command(editor, file);
                publish("Lansez: " + String.join(" ", command));

                long start = System.nanoTime();
                Process process = EditorLauncher.launch(editor, file);
                publish("Editor pornit (PID " + process.pid() + "). Aștept închiderea lui…");
                int exitCode = process.waitFor();
                elapsedMs = (System.nanoTime() - start) / 1_000_000;
                return exitCode;
            }

            @Override
            protected void process(List<String> messages) {
                for (String message : messages) {
                    appendLog(message);
                }
            }

            @Override
            protected void done() {
                int exitCode;
                try {
                    exitCode = get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    setBusy(false);
                    return;
                } catch (ExecutionException e) {
                    fail("Nu am putut lansa editorul", e.getCause());
                    return;
                }

                appendLog("Editorul s-a închis. Cod de terminare: " + exitCode
                        + " (după " + elapsedMs + " ms).");
                if (elapsedMs < 1500) {
                    appendLog("ATENȚIE: procesul s-a terminat aproape instant. Unele editoare "
                            + "(ex. Notepad din Windows 11) pasează fișierul unei instanțe existente "
                            + "și ies imediat. Dacă fereastra editorului e încă deschisă, alegeți "
                            + "alt editor în câmpul „Editor\".");
                }
                JOptionPane.showMessageDialog(Panel.this,
                        "Editorul s-a închis.\nCodul de terminare al procesului: " + exitCode,
                        "Editor închis", JOptionPane.INFORMATION_MESSAGE);

                afterEditor(file, editor);
            }
        }

        private void afterEditor(Path file, String editor) {
            if (simulation.isSelected()) {
                appendLog("Simulare: sistemul NU se repornește. Deschid fișierul acum.");
                try {
                    EditorLauncher.launch(editor, file);
                    appendLog("Fișier deschis: " + file);
                    setStatus("Simulare încheiată.");
                } catch (IOException e) {
                    fail("Nu am putut deschide fișierul", e);
                    return;
                }
                setBusy(false);
                return;
            }

            int answer = JOptionPane.showConfirmDialog(this,
                    "Calculatorul va fi repornit în aproximativ " + delayText() + ".\n"
                            + "Salvați lucrul din alte aplicații înainte să continuați.\n\n"
                            + "Continuați?",
                    "Confirmare repornire", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer != JOptionPane.YES_OPTION) {
                appendLog("Repornire refuzată de utilizator.");
                setStatus("Oprit înainte de repornire.");
                setBusy(false);
                return;
            }
            scheduleReboot(file, editor);
        }

        /** Înregistrează deschiderea fișierului după repornire, apoi programează repornirea. */
        private void scheduleReboot(Path file, String editor) {
            setStatus("Programez repornirea…");
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() {
                    try {
                        List<String> command = new ArrayList<>(
                                AutostartManager.selfCommand("--open-file", file.toString()));
                        if (!editor.isEmpty()) {
                            command.add("--editor");
                            command.add(editor);
                        }
                        AutostartManager.enableOnce(ONCE_NAME, command);
                    } catch (Exception e) {
                        return "Nu am putut înregistra deschiderea fișierului după repornire: "
                                + e.getMessage();
                    }

                    try {
                        OsUtils.CommandResult result = OsUtils.run(rebootCommand());
                        if (result.exitCode != 0) {
                            removeOnceQuietly();
                            return "Comanda de repornire a eșuat (cod " + result.exitCode + "): "
                                    + result.output
                                    + (OsUtils.isWindows() ? ""
                                    : "\nPe Linux repornirea poate necesita drepturi de administrator "
                                    + "(rulați aplicația cu sudo sau configurați polkit).");
                        }
                    } catch (IOException e) {
                        removeOnceQuietly();
                        return "Nu am putut rula comanda de repornire: " + e.getMessage();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        removeOnceQuietly();
                        return "Operațiune întreruptă.";
                    }
                    return null;
                }

                @Override
                protected void done() {
                    String error;
                    try {
                        error = get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    } catch (ExecutionException e) {
                        error = String.valueOf(e.getCause());
                    }
                    if (error != null) {
                        fail(error, null);
                        return;
                    }
                    appendLog("Repornirea este programată (în aproximativ " + delayText() + "). "
                            + "După autentificare, fișierul se va deschide automat.");
                    setStatus("Repornire programată – o puteți anula.");
                    cancelButton.setEnabled(true);
                }
            }.execute();
        }

        private void cancelReboot() {
            cancelButton.setEnabled(false);
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() {
                    String problem = null;
                    try {
                        OsUtils.CommandResult result = OsUtils.run(cancelCommand());
                        if (result.exitCode != 0) {
                            problem = "Anularea a răspuns cu cod " + result.exitCode + ": " + result.output;
                        }
                    } catch (IOException e) {
                        problem = "Nu am putut anula repornirea: " + e.getMessage();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    removeOnceQuietly();
                    return problem;
                }

                @Override
                protected void done() {
                    String problem = null;
                    try {
                        problem = get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException e) {
                        problem = String.valueOf(e.getCause());
                    }
                    appendLog(problem == null ? "Repornirea a fost anulată." : problem);
                    setStatus(problem == null ? "Repornire anulată." : "Anulare incompletă – vezi jurnalul.");
                    setBusy(false);
                }
            }.execute();
        }

        // -------------------------------------------------------------- utilitare

        private static List<String> rebootCommand() {
            if (OsUtils.isWindows()) {
                return Arrays.asList("shutdown", "/r", "/t", "30", "/c", "Lab5 - repornire programata");
            }
            return Arrays.asList("shutdown", "-r", "+1");
        }

        private static List<String> cancelCommand() {
            return OsUtils.isWindows() ? Arrays.asList("shutdown", "/a") : Arrays.asList("shutdown", "-c");
        }

        private static String delayText() {
            return OsUtils.isWindows() ? "30 de secunde" : "1 minut";
        }

        private static void removeOnceQuietly() {
            try {
                AutostartManager.removeOnce(ONCE_NAME);
            } catch (IOException ignored) {
                // nu e critic
            }
        }

        private void setBusy(boolean busy) {
            startButton.setEnabled(!busy);
            browseButton.setEnabled(!busy);
            fileField.setEnabled(!busy);
            editorField.setEnabled(!busy);
            simulation.setEnabled(!busy);
            if (!busy) {
                cancelButton.setEnabled(false);
            }
        }

        private void fail(String message, Throwable cause) {
            String full = cause == null ? message : message + ": " + cause.getMessage();
            appendLog("EROARE: " + full);
            setStatus("Eroare – vezi jurnalul.");
            JOptionPane.showMessageDialog(this, full, "Laborator 5", JOptionPane.ERROR_MESSAGE);
            setBusy(false);
        }

        private void appendLog(String line) {
            log.append("[" + LocalTime.now().format(TIME) + "] " + line + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        }

        private void setStatus(String text) {
            status.setText(text);
        }
    }
}
