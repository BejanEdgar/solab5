package lab5;

import javax.swing.JComponent;

/**
 * Contractul pe care îl implementează fiecare task din laborator.
 *
 * Fiecare task = o clasă separată în pachetul {@code lab5.tasks}, care apare
 * ca un tab în fereastra principală. Astfel, doi colegi pot lucra în paralel
 * pe fișiere diferite, fără conflicte în git.
 */
public interface LabTask {

    /**
     * Identificator scurt, unic (ex. "cpu-monitor"). Se folosește în linia de
     * comandă: {@code java -jar lab5.jar --task cpu-monitor} deschide direct
     * tab-ul respectiv (util la pornirea automată).
     */
    String id();

    /** Titlul afișat pe tab. */
    String title();

    /** Construiește panoul Swing al task-ului. Apelat o singură dată, pe EDT. */
    JComponent createPanel();

    /** Apelat la închiderea ferestrei: opriți aici Timer-ele, firele, procesele. */
    default void onClose() {
    }
}
