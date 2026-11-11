package lab5;

import lab5.tasks.Task01EditorRestart;
import lab5.tasks.Task04CpuMonitor;

import java.util.Arrays;
import java.util.List;

/**
 * Lista task-urilor afișate în aplicație (în ordinea tab-urilor).
 *
 * Pentru un task nou: creați clasa în {@code lab5/tasks/}, implementați
 * {@link LabTask} și adăugați o linie aici.
 */
public final class TaskRegistry {

    private TaskRegistry() {
    }

    public static List<LabTask> all() {
        return Arrays.asList(
                new Task01EditorRestart(),
                new Task04CpuMonitor()
        );
    }
}
