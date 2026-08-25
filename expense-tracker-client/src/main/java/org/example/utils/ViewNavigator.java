package org.example.utils;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ViewNavigator {
    private static Stage mainStage;

    public static void setMainStage(Stage stage){
        mainStage = stage;
        applyAppIcon(stage);
    }

    public static void applyAppIcon(Stage stage) {
        if (stage == null) return;
        try {
            int[] sizes = {16, 32, 64, 128, 256, 512};
            for (int s : sizes) {
                java.io.InputStream stream = ViewNavigator.class.getResourceAsStream("/images/finvora_icon_" + s + ".png");
                if (stream != null) {
                    stage.getIcons().add(new javafx.scene.image.Image(stream));
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load app icons: " + e.getMessage());
        }
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void switchViews(Scene scene){
        if(mainStage != null && scene != null){
            ThemeManager.apply(scene);
            
            Node root = scene.getRoot();
            if (root != null) {
                root.setOpacity(0.0);
                root.setTranslateY(10.0);

                mainStage.setScene(scene);
                mainStage.show();

                FadeTransition fade = new FadeTransition(Duration.millis(240), root);
                fade.setFromValue(0.0);
                fade.setToValue(1.0);
                fade.setInterpolator(Interpolator.EASE_OUT);

                TranslateTransition slide = new TranslateTransition(Duration.millis(240), root);
                slide.setFromY(10.0);
                slide.setToY(0.0);
                slide.setInterpolator(Interpolator.EASE_OUT);

                ParallelTransition transition = new ParallelTransition(fade, slide);
                transition.play();
            } else {
                mainStage.setScene(scene);
                mainStage.show();
            }
        }
    }
}

