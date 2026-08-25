package org.example.utils;

import javafx.scene.Scene;
import javafx.scene.text.Font;
import java.io.InputStream;
import java.util.Objects;

public final class ThemeManager {

    private static final String THEME_CSS = "/theme.css";
    private static final String STYLE_CSS = "/style.css";
    private static boolean darkMode = false;

    static {
        loadFonts();
    }

    private ThemeManager() {}

    public static void loadFonts() {
        try {
            InputStream fontStream = ThemeManager.class.getResourceAsStream("/fonts/Inter-Regular.ttf");
            if (fontStream != null) {
                Font.loadFont(fontStream, 14);
            }
        } catch (Exception e) {
            System.err.println("Note: Custom Inter font could not be loaded, using system fallback font: " + e.getMessage());
        }
    }

    public static void apply(Scene scene) {
        if (scene == null) return;
        
        loadFonts();

        String themeUrl = Objects.requireNonNull(ThemeManager.class.getResource(THEME_CSS)).toExternalForm();
        if (!scene.getStylesheets().contains(themeUrl)) {
            scene.getStylesheets().add(0, themeUrl);
        }

        try {
            String styleUrl = Objects.requireNonNull(ThemeManager.class.getResource(STYLE_CSS)).toExternalForm();
            if (!scene.getStylesheets().contains(styleUrl)) {
                scene.getStylesheets().add(styleUrl);
            }
        } catch (Exception ignored) {}

        applyThemeClass(scene);
    }

    public static void toggleTheme(Scene scene) {
        darkMode = !darkMode;
        applyThemeClass(scene);
    }

    public static void setDarkMode(Scene scene, boolean dark) {
        darkMode = dark;
        applyThemeClass(scene);
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    private static void applyThemeClass(Scene scene) {
        if (scene != null && scene.getRoot() != null) {
            scene.getRoot().getStyleClass().removeAll("theme-dark", "theme-light");
            if (darkMode) {
                scene.getRoot().getStyleClass().add("theme-dark");
            } else {
                scene.getRoot().getStyleClass().add("theme-light");
            }
        }
    }
}