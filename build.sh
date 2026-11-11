#!/usr/bin/env sh
# Compilează proiectul și creează out/lab5.jar (necesită JDK 11 sau mai nou).
set -e
cd "$(dirname "$0")"

rm -rf out
mkdir -p out/classes

# javac urmărește singur toate clasele referite plecând de la Main (inclusiv task-urile din registru).
javac -encoding UTF-8 -d out/classes -sourcepath src src/lab5/Main.java

printf 'Main-Class: lab5.Main\n' > out/manifest.txt
jar cfm out/lab5.jar out/manifest.txt -C out/classes .

echo "Gata: out/lab5.jar"
echo "Rulare: java -jar out/lab5.jar"
