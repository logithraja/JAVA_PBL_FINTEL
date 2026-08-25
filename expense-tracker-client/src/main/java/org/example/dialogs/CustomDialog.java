package org.example.dialogs;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.util.Duration;
import org.example.models.User;
import org.example.utils.ThemeManager;

public class CustomDialog<T> extends Dialog<T> { 
    protected User user;

    public CustomDialog(User user){
        this.user = user;

        getDialogPane().getStylesheets().add(getClass().getResource("/theme.css").toExternalForm());
        try {
            getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception ignored) {}
       
        getDialogPane().getButtonTypes().addAll(ButtonType.OK);
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.setVisible(false);
        okButton.setDisable(true);

        setOnShowing(e -> {
            DialogPane pane = getDialogPane();
            if (pane != null) {
                if (pane.getScene() != null) {
                    ThemeManager.apply(pane.getScene());
                }
                pane.setOpacity(0.0);
                pane.setScaleX(0.93);
                pane.setScaleY(0.93);

                FadeTransition fade = new FadeTransition(Duration.millis(200), pane);
                fade.setFromValue(0.0);
                fade.setToValue(1.0);
                fade.setInterpolator(Interpolator.EASE_OUT);

                ScaleTransition scale = new ScaleTransition(Duration.millis(200), pane);
                scale.setFromX(0.93);
                scale.setFromY(0.93);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.setInterpolator(Interpolator.EASE_OUT);

                ParallelTransition pt = new ParallelTransition(fade, scale);
                pt.play();
            }
        });
    }
}