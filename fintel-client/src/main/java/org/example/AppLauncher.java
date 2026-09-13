package org.example;

/**
 * Standard launcher entry-point for shaded fat-JAR execution of the JavaFX application.
 * Prevents "JavaFX runtime components are missing" when executed via java -jar.
 */
public class AppLauncher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
