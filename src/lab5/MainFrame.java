package lab5;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/** Fereastra principală: un tab pentru fiecare task din {@link TaskRegistry}. */
public class MainFrame extends JFrame {

    private final List<LabTask> tasks = TaskRegistry.all();

    public MainFrame(String initialTaskId) {
        super("Laborator 5 – Funcții de nivel înalt ale sistemului de operare");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        for (int i = 0; i < tasks.size(); i++) {
            LabTask task = tasks.get(i);
            tabs.addTab(task.title(), task.createPanel());
            if (task.id().equals(initialTaskId)) {
                tabs.setSelectedIndex(i);
            }
        }
        add(tabs);

        // Listener-ul rulează înainte de EXIT_ON_CLOSE: task-urile își pot opri resursele.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                for (LabTask task : tasks) {
                    try {
                        task.onClose();
                    } catch (RuntimeException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        setMinimumSize(new Dimension(640, 460));
        setSize(780, 560);
        setLocationRelativeTo(null);
    }
}
