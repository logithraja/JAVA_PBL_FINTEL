package org.example.utils;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class ViewNavigator {
    private static Stage mainStage;

    public static void setMainStage(Stage stage){
        mainStage = stage;
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void switchViews(Scene scene){
        if(mainStage != null && scene != null){
            ThemeManager.apply(scene);
            mainStage.setScene(scene);
            mainStage.show();
        }
    }
}
