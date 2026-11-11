# Laborator 5 – Funcții de nivel înalt ale sistemului de operare (Java Swing)

Aplicație Java cu interfață grafică, în care fiecare task din laborator este un **tab separat**,
implementat într-o **clasă separată**. Astfel, doi colegi pot lucra simultan fără conflicte în git.

| Task | Stare | Fișier |
|------|-------|--------|
| 1 – Editor de text → cod de terminare → repornire → deschiderea fișierului | ✅ implementat | `src/lab5/tasks/Task01EditorRestart.java` |
| 4 – Monitor CPU / RAM la 5 secunde + pornire automată | ⏳ de implementat (coleg) | `src/lab5/tasks/Task04CpuMonitor.java` |

## Cerințe

- JDK 11 sau mai nou (`java -version`, `javac -version`)
- Git

## Compilare și rulare

```bash
# Linux / macOS
./build.sh
java -jar out/lab5.jar
```

```bat
:: Windows
build.bat
java -jar out\lab5.jar
```

Se poate deschide și direct într-un IDE (IntelliJ / Eclipse / VS Code): directorul sursă este `src/`,
clasa principală este `lab5.Main`.

## Structura proiectului

```
src/lab5/
├── Main.java               punct de intrare (argumente: --task <id>, --open-file <cale>)
├── MainFrame.java          fereastra cu tab-uri (un tab per task)
├── LabTask.java            interfața pe care o implementează fiecare task
├── TaskRegistry.java       lista task-urilor afișate
├── util/
│   ├── OsUtils.java            Windows/Linux + rularea comenzilor externe
│   ├── AutostartManager.java   pornire automată (registru Windows / ~/.config/autostart pe Linux)
│   └── EditorLauncher.java     lansarea editorului de text
└── tasks/
    ├── Task01EditorRestart.java   ← Task 1
    └── Task04CpuMonitor.java      ← Task 4 (schelet pentru coleg)
```

## Task 1 – cum funcționează

1. Alegeți fișierul text (implicit `~/lab5_text.txt`; se creează dacă nu există) și, opțional, editorul
   (gol = `notepad.exe` pe Windows, `gedit` / `kate` / `mousepad` … pe Linux).
2. **Pornește editorul** → editorul se lansează cu `ProcessBuilder`, iar un `SwingWorker` așteaptă
   (`process.waitFor()`) fără să blocheze interfața.
3. După închiderea editorului apare un mesaj cu **codul de terminare** al procesului.
4. Înainte de repornire, aplicația înregistrează o intrare *„o singură dată"* în pornirea automată
   (`RunOnce` în registru pe Windows, fișier `.desktop` pe Linux), apoi rulează
   `shutdown /r /t 30` (Windows) sau `shutdown -r +1` (Linux).
5. După repornire și autentificare, intrarea relansează aplicația cu `--open-file <cale>`, care
   deschide fișierul în editor și se închide.
6. Cât timp repornirea este programată, butonul **Anulează repornirea** execută `shutdown /a` /
   `shutdown -c` și șterge intrarea temporară.

### Mod simulare (bifat implicit)

Cu **Mod simulare** bifat, sistemul **nu** se repornește: după închiderea editorului, fișierul se
deschide imediat. Folosiți-l la dezvoltare și testare. Debifați-l doar pentru demonstrația reală.

### Observații

- **Windows 11:** Notepad-ul nou poate întoarce controlul imediat (pasează fișierul unei instanțe
  existente). Aplicația avertizează în jurnal dacă procesul se termină în < 1,5 s; în acest caz
  închideți toate ferestrele Notepad înainte sau alegeți alt editor în câmpul „Editor".
- **Linux:** `shutdown` poate cere drepturi de administrator (depinde de configurarea polkit).
  Pentru editoarele „single-instance" se adaugă automat opțiunea care forțează o instanță proprie
  (`gedit --standalone` etc.).
- Pornirea automată nu necesită drepturi de administrator (se scrie doar în profilul utilizatorului).

---

## Pentru coleg: cum adaugi Task 4

Tot ce ai de făcut se află în **un singur fișier**: `src/lab5/tasks/Task04CpuMonitor.java`.
Clasa este deja înregistrată în `TaskRegistry`, deci nu modifici alte fișiere → fără conflicte la merge.

1. **Clonează repo-ul și creează o ramură**
   ```bash
   git clone <URL-ul-repo-ului>
   cd lab5-system-tasks
   git checkout -b task4-cpu-monitor
   ```
2. **Implementează** în `Task04CpuMonitor.createPanel()` (în fișier găsești și un schelet de cod):
   - citire date: `ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class)`
     → `getCpuLoad()` (Java 14+; pe Java 11–13 `getSystemCpuLoad()`) și `getFreeMemorySize()`
     (respectiv `getFreePhysicalMemorySize()`);
   - actualizare la fiecare 5 secunde cu `javax.swing.Timer(5000, …)` (rulează pe EDT, deci poți
     modifica direct etichetele);
   - checkbox „Pornește cu sistemul":
     ```java
     AutostartManager.enable("Lab5CpuMonitor", AutostartManager.selfCommand("--task", "cpu-monitor"));
     AutostartManager.disable("Lab5CpuMonitor");
     AutostartManager.isEnabled("Lab5CpuMonitor");
     ```
     `--task cpu-monitor` relansează aplicația direct pe tab-ul tău, deci păstrează `id()` = `"cpu-monitor"`;
   - oprește `Timer`-ul în `onClose()`.
3. **Compilează și testează:** `./build.sh && java -jar out/lab5.jar`
4. **Commit și push**
   ```bash
   git add src/lab5/tasks/Task04CpuMonitor.java
   git commit -m "Task 4: monitor CPU/RAM cu pornire automată"
   git push -u origin task4-cpu-monitor
   ```
   Apoi deschide un Pull Request spre `main` (sau fă merge local:
   `git checkout main && git merge task4-cpu-monitor`).

Dacă trebuie să adaugi un task nou (ex. Task 5): creezi `Task05….java` care implementează `LabTask`
și adaugi o linie în `TaskRegistry.all()`.

## Publicarea repo-ului (o singură dată, la cel care a creat proiectul)

```bash
git remote add origin <URL-ul-repo-ului-gol-de-pe-GitHub/GitLab>
git push -u origin main
```

Commit-ul inițial folosește o identitate generică. Ca să apară numele tău, rulează înainte de push:
`git config user.name "Numele Tău"`, `git config user.email "email@exemplu.com"`,
apoi `git commit --amend --reset-author --no-edit`.
