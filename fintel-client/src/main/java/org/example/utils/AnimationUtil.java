package org.example.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.util.Locale;

public final class AnimationUtil {

    private AnimationUtil() {}

    public static void animateCurrency(Label label, BigDecimal targetValue) {
        if (label == null || targetValue == null) return;
        
        double target = targetValue.doubleValue();
        DoubleProperty valueProp = new SimpleDoubleProperty(0.0);

        valueProp.addListener((obs, oldVal, newVal) -> {
            double current = newVal.doubleValue();
            if (current < 0) {
                label.setText("-₹" + String.format(Locale.US, "%.2f", Math.abs(current)));
            } else {
                label.setText("₹" + String.format(Locale.US, "%.2f", current));
            }
        });

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(valueProp, 0.0)),
            new KeyFrame(Duration.millis(550), new KeyValue(valueProp, target))
        );
        timeline.play();
    }
}
