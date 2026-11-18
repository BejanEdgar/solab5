# Laboratorul 5 – Funcții de nivel înalt ale sistemului de operare

Aplicație Java cu interfață Swing pentru două sarcini de laborator. Fiecare sarcină are o filă separată în aceeași fereastră.

| Filă | Funcționalitate |
| --- | --- |
| Task 1 – Editor + repornire | Deschide un editor, afișează codul de terminare și permite repornirea calculatorului și redeschiderea fișierului. |
| Task 4 – Monitor CPU / RAM | Afișează încărcarea procesorului și memoria RAM liberă la fiecare 5 secunde, împreună cu două grafice și o opțiune de pornire automată. |

## Cerințe

- JDK 11 sau o versiune mai nouă, cu comenzile `java`, `javac` și `jar` disponibile.
- Un mediu grafic pentru interfața Swing.
- Opțional, VS Code cu extensia **Extension Pack for Java**.

Verificarea instalării Java:

```powershell
java -version
javac -version
```

## Compilare și rulare în Windows

Deschideți PowerShell în directorul proiectului și executați:

```powershell
.\build.bat
java -jar out\lab5.jar
```

Prima comandă compilează proiectul și creează `out\lab5.jar`. A doua pornește aplicația. După modificarea codului, rulați din nou `build.bat` pentru a actualiza fișierul JAR. Scriptul recreează directorul `out`, deci nu păstrați acolo fișiere personale.

Pentru a deschide direct fila de monitorizare:

```powershell
java -jar out\lab5.jar --task cpu-monitor
```

## Rulare din VS Code

1. Deschideți **folderul proiectului** `solab5` cu **File → Open Folder**.
2. Deschideți `src/lab5/Main.java`.
3. Dacă extensia Java este instalată, apăsați **Run** de deasupra metodei `main`.
4. Alegeți fila dorită în fereastra aplicației.

`Task04CpuMonitor.java` este o componentă a proiectului și nu are propria metodă `main`. Rularea lui individuală cu Code Runner poate produce erori precum `cannot find symbol`, deoarece celelalte clase nu sunt găsite. Porniți aplicația prin `Main.java` sau folosiți comenzile din secțiunea pentru Windows.

## Compilare și rulare în Linux

```sh
./build.sh
java -jar out/lab5.jar
```

Dacă scriptul nu este executabil, îl puteți porni cu `sh build.sh`. Pe Linux, comanda de repornire poate necesita permisiuni suplimentare. Pornirea automată nu este implementată pentru macOS.

## Task 1 – Editor și repornire

1. Alegeți fișierul text și, opțional, un editor. Dacă lăsați câmpul editorului gol, aplicația încearcă să aleagă unul disponibil.
2. Apăsați **Pornește editorul**, lucrați în el și apoi închideți-l.
3. Aplicația afișează codul de terminare al procesului editorului.
4. Cu **Mod simulare** activat, fișierul este redeschis fără repornirea calculatorului. Modul este bifat implicit.
5. Cu simularea dezactivată, aplicația cere confirmare și programează repornirea. După autentificare, încearcă să redeschidă fișierul. Repornirea programată poate fi anulată din aplicație.

Pe Windows 11, unele versiuni Notepad pot transmite fișierul unei ferestre deja deschise și pot încheia imediat procesul lansat. În acest caz, jurnalul afișează un avertisment; încercați să închideți celelalte ferestre Notepad sau să alegeți alt editor.

## Task 4 – Monitorizarea CPU și RAM

Fila afișează:

- încărcarea procesorului, în procente;
- memoria RAM liberă, în MiB și GiB;
- un grafic al încărcării procesorului și unul al procentului de RAM utilizată;
- ora ultimei actualizări.

Datele sunt citite cu `com.sun.management.OperatingSystemMXBean` și actualizate cu un `javax.swing.Timer` la fiecare 5 secunde. Graficele păstrează până la 5 minute de valori, începând din momentul pornirii aplicației. Ele reprezintă resursele sistemului, nu consumul unui singur program. Dacă o valoare nu este disponibilă, aceasta este marcată ca atare.

Metodele de citire folosite sunt compatibile cu Java 11 și pot apărea ca **deprecated** în versiunile mai noi de Java; acestea sunt avertizări, nu erori de compilare.

### Pornire automată

Bifați **Pornește aplicația la autentificarea în sistem** pentru a deschide aplicația pe fila de monitorizare la autentificările următoare. Debifați opțiunea pentru a o dezactiva.

- În Windows, intrarea este adăugată în registrul utilizatorului curent, la `HKCU\Software\Microsoft\Windows\CurrentVersion\Run`.
- În Linux, se folosește un fișier `.desktop` în directorul de autostart al utilizatorului.
- Comanda reține calea aplicației și a instalării Java. Dacă mutați aplicația sau schimbați instalarea Java, dezactivați și activați din nou opțiunea din noua locație.

## Structura proiectului

| Fișier | Rol |
| --- | --- |
| `src/lab5/Main.java` | Punctul de pornire al aplicației. |
| `src/lab5/MainFrame.java` | Construiește fereastra și filele. |
| `src/lab5/LabTask.java` | Definește interfața comună a sarcinilor. |
| `src/lab5/TaskRegistry.java` | Înregistrează sarcinile afișate. |
| `src/lab5/tasks/Task01EditorRestart.java` | Implementează Task 1. |
| `src/lab5/tasks/Task04CpuMonitor.java` | Implementează monitorizarea CPU și RAM. |
| `src/lab5/util/AutostartManager.java` | Gestionează intrările pentru pornirea automată. |
| `src/lab5/util/EditorLauncher.java` | Pregătește și pornește editorul de text. |
| `src/lab5/util/OsUtils.java` | Conține utilitare pentru sistemul de operare și comenzile externe. |
