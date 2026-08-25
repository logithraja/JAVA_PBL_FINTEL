package org.example.animations;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class LoadingAnimationPane extends StackPane {
    private final Region backdrop;
    private final VBox card;
    private final Label titleLabel;
    private final Label subtitleLabel;
    private final ProgressIndicator spinner;

    public LoadingAnimationPane(double screenWidth, double screenHeight) {
        setAlignment(Pos.CENTER);
        setMinSize(screenWidth, screenHeight);

        backdrop = new Region();
        backdrop.setStyle("-fx-background-color: rgba(15, 23, 42, 0.45);");
        backdrop.setPrefSize(screenWidth, screenHeight);

        spinner = new ProgressIndicator();
        spinner.setPrefSize(48, 48);
        spinner.setStyle("-fx-progress-color: #4F46E5;");

        titleLabel = new Label("Loading Finvora...");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        subtitleLabel = new Label("Updating your financial data...");
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        card = new VBox(12, spinner, titleLabel, subtitleLabel);
        card.setAlignment(Pos.CENTER);
        card.setMaxSize(260, 160);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20px; -fx-border-radius: 20px; -fx-border-color: #E2E8F0; -fx-border-width: 1px; -fx-padding: 24px; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 24, 0, 0, 8);");

        getChildren().addAll(backdrop, card);
        setVisible(false);
        setManaged(false);
    }

    public void showLoading(String message) {
        if (message != null && !message.isEmpty()) {
            titleLabel.setText(message);
        }
        setVisible(true);
        setManaged(true);
        setOpacity(0.0);
        FadeTransition ft = new FadeTransition(Duration.millis(180), this);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.setInterpolator(Interpolator.EASE_OUT);
        ft.play();
    }

    public void hideLoading() {
        FadeTransition ft = new FadeTransition(Duration.millis(150), this);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            setVisible(false);
            setManaged(false);
        });
        ft.play();
    }

    public void resizeWidth(double newWidth) {
        setMinWidth(newWidth);
        backdrop.setPrefWidth(newWidth);
    }

    public void resizeHeight(double newHeight) {
        setMinHeight(newHeight);
        backdrop.setPrefHeight(newHeight);
    }
}

