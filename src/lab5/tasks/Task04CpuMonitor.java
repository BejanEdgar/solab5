package lab5.tasks;

import lab5.LabTask;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;

/**
 * TASK 4 – de implementat de coleg.
 *
 * Enunț: „Creați un program care afișează încărcarea curentă a procesorului și
 * spațiul liber din memoria RAM la fiecare 5 secunde, asigurând și adăugarea
 * acestuia în pornirea automată. Folosiți OperatingSystemMXBean pentru Java."
 *
 * Tot ce trebuie făcut se află în acest fișier (clasa e deja înregistrată în
 * {@link lab5.TaskRegistry}, deci nu trebuie modificat niciun alt fișier).
 *
 * Schelet sugerat:
 * <pre>
 *   // 1) Citirea datelor
 *   com.sun.management.OperatingSystemMXBean os =
 *           java.lang.management.ManagementFactory.getPlatformMXBean(
 *                   com.sun.management.OperatingSystemMXBean.class);
 *   double cpu = os.getCpuLoad() * 100;                          // Java 14+ (înlocuiește getSystemCpuLoad)
 *   long freeMb = os.getFreeMemorySize() / (1024 * 1024);        // Java 14+ (înlocuiește getFreePhysicalMemorySize)
 *
 *   // 2) La fiecare 5 secunde, pe EDT:
 *   javax.swing.Timer timer = new javax.swing.Timer(5000, e -> { ... actualizați etichetele ... });
 *   timer.start();
 *
 *   // 3) Pornire automată (CheckBox „Pornește cu sistemul"):
 *   lab5.util.AutostartManager.enable("Lab5CpuMonitor",
 *           lab5.util.AutostartManager.selfCommand("--task", "cpu-monitor"));
 *   lab5.util.AutostartManager.disable("Lab5CpuMonitor");
 *   lab5.util.AutostartManager.isEnabled("Lab5CpuMonitor");
 * </pre>
 *
 * Pentru că `selfCommand("--task", "cpu-monitor")` relansează aplicația cu acest tab
 * selectat, id()-ul de mai jos trebuie păstrat ca "cpu-monitor".
 *
 * Nu uitați: opriți Timer-ul în {@link #onClose()}.
 */
public class Task04CpuMonitor implements LabTask {

    @Override
    public String id() {
        return "cpu-monitor";
    }

    @Override
    public String title() {
        return "Task 4 – Monitor CPU / RAM";
    }

    @Override
    public JComponent createPanel() {
        // TODO (coleg): înlocuiți conținutul acestui panou cu implementarea reală.
        JPanel panel = new JPanel(new BorderLayout());
        JLabel placeholder = new JLabel("Task 4 – de implementat", JLabel.CENTER);
        placeholder.setFont(placeholder.getFont().deriveFont(Font.ITALIC, 16f));
        panel.add(placeholder, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void onClose() {
        // TODO (coleg): timer.stop();
    }
}
