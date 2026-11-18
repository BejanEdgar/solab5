package lab5.tasks;

import com.sun.management.OperatingSystemMXBean;
import lab5.LabTask;
import lab5.util.AutostartManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ArrayDeque;
import java.util.Deque;

/** Monitorizează încărcarea procesorului și memoria RAM liberă. */
public class Task04CpuMonitor implements LabTask {
    private static final String AUTOSTART_NAME = "Lab5CpuMonitor";
    private static final long MIB = 1024L * 1024L;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private Timer timer;

    @Override
    public String id() {
        return "cpu-monitor";
    }

    @Override
    public String title() {
        return "Task 4 - Monitor CPU / RAM";
    }

    @Override
    public JComponent createPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel heading = new JLabel("Monitorizarea resurselor sistemului");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 18f));
        JLabel cpuLabel = new JLabel("Încărcare CPU: se citește...");
        JLabel memoryLabel = new JLabel("Memorie RAM liberă: se citește...");
        JLabel timeLabel = new JLabel("Actualizare la fiecare 5 secunde.");
        JCheckBox autostart = new JCheckBox("Pornește aplicația la autentificarea în sistem");
        ResourceGraph cpuGraph = new ResourceGraph("Procesor", new Color(35, 120, 195));
        ResourceGraph memoryGraph = new ResourceGraph("Memorie RAM utilizată", new Color(135, 75, 185));
        autostart.setSelected(AutostartManager.isEnabled(AUTOSTART_NAME));

        panel.add(heading);
        panel.add(Box.createVerticalStrut(24));
        panel.add(cpuLabel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(memoryLabel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(timeLabel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(cpuGraph);
        panel.add(Box.createVerticalStrut(8));
        panel.add(memoryGraph);
        panel.add(Box.createVerticalStrut(8));
        panel.add(autostart);

        OperatingSystemMXBean os = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        if (os == null) {
            cpuLabel.setText("Încărcare CPU: indisponibilă");
            memoryLabel.setText("Memorie RAM liberă: indisponibilă");
            timeLabel.setText("Sistemul Java nu oferă datele necesare.");
        } else {
            Runnable refresh = () -> {
                // Metodele sunt disponibile și în Java 11, versiunea minimă a proiectului.
                double cpu = os.getSystemCpuLoad();
                long freeMemory = os.getFreePhysicalMemorySize();
                long totalMemory = os.getTotalPhysicalMemorySize();
                cpuLabel.setText(cpu < 0 ? "Încărcare CPU: indisponibilă momentan"
                        : String.format(Locale.ROOT, "Încărcare CPU: %.1f%%", cpu * 100));
                memoryLabel.setText(freeMemory < 0 ? "Memorie RAM liberă: indisponibilă"
                        : String.format(Locale.ROOT, "Memorie RAM liberă: %.0f MiB (%.2f GiB)",
                                (double) freeMemory / MIB, (double) freeMemory / (MIB * 1024)));
                cpuGraph.addSample(cpu < 0 ? Double.NaN : cpu * 100);
                double usedMemory = totalMemory > 0 && freeMemory >= 0 && freeMemory <= totalMemory
                        ? 100.0 * (totalMemory - freeMemory) / totalMemory
                        : Double.NaN;
                memoryGraph.addSample(usedMemory);
                timeLabel.setText("Ultima actualizare: " + LocalTime.now().format(TIME)
                        + " (la fiecare 5 secunde)");
            };
            refresh.run();
            timer = new Timer(5000, e -> refresh.run());
            timer.start();
        }

        autostart.addActionListener(e -> {
            boolean requested = autostart.isSelected();
            try {
                if (requested) {
                    AutostartManager.enable(AUTOSTART_NAME,
                            AutostartManager.selfCommand("--task", id()));
                } else {
                    AutostartManager.disable(AUTOSTART_NAME);
                }
            } catch (IOException | SecurityException ex) {
                autostart.setSelected(!requested);
                JOptionPane.showMessageDialog(panel, "Nu am putut modifica pornirea automată:\n"
                        + ex.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
            }
        });
        return panel;
    }

    @Override
    public void onClose() {
        if (timer != null) {
            timer.stop();
        }
    }

    /** Istoric de până la cinci minute; datele lipsă nu sunt desenate ca zero. */
    private static final class ResourceGraph extends JPanel {
        private static final int MAX_SAMPLES = 61;
        private final Deque<Double> samples = new ArrayDeque<>();
        private final String name;
        private final Color lineColor;

        ResourceGraph(String name, Color lineColor) {
            this.name = name;
            this.lineColor = lineColor;
            setPreferredSize(new Dimension(550, 125));
            setMinimumSize(new Dimension(200, 70));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
            setToolTipText("Istoric de până la 5 minute; o citire la fiecare 5 secunde.");
        }

        void addSample(double value) {
            if (samples.size() == MAX_SAMPLES) {
                samples.removeFirst();
            }
            samples.addLast(value);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int left = 40;
                int top = 24;
                int width = Math.max(1, getWidth() - left - 8);
                int height = Math.max(1, getHeight() - top - 20);

                g.setColor(getForeground());
                g.drawString(name, left, 16);
                Double current = samples.peekLast();
                String currentText = current != null && Double.isFinite(current)
                        ? String.format(Locale.ROOT, "%.1f%%", current)
                        : "indisponibil";
                g.drawString(currentText, Math.max(left, getWidth() - g.getFontMetrics().stringWidth(currentText) - 8),
                        16);

                g.setColor(new Color(245, 249, 252));
                g.fillRect(left, top, width, height);
                g.setColor(new Color(206, 220, 231));
                for (int i = 0; i <= 4; i++) {
                    int y = top + i * height / 4;
                    g.drawLine(left, y, left + width, y);
                }
                for (int i = 0; i <= 6; i++) {
                    int x = left + i * width / 6;
                    g.drawLine(x, top, x, top + height);
                }
                g.setColor(getForeground());
                g.drawString("100%", 2, top + 10);
                g.drawString("0%", 15, top + height);
                g.drawString("5 min", left, getHeight() - 3);
                String now = "acum";
                g.drawString(now, left + width - g.getFontMetrics().stringWidth(now), getHeight() - 3);

                Path2D path = new Path2D.Double();
                boolean connected = false;
                int index = MAX_SAMPLES - samples.size();
                for (double value : samples) {
                    double x = left + (double) index++ * width / (MAX_SAMPLES - 1);
                    if (!Double.isFinite(value)) {
                        connected = false;
                        continue;
                    }
                    double y = top + height * (1 - Math.max(0, Math.min(100, value)) / 100);
                    if (connected) {
                        path.lineTo(x, y);
                    } else {
                        path.moveTo(x, y);
                        connected = true;
                    }
                }
                g.setColor(lineColor);
                g.setStroke(new BasicStroke(2f));
                g.draw(path);
            } finally {
                g.dispose();
            }
        }
    }
}
