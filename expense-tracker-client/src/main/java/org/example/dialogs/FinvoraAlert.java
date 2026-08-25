package org.example.dialogs;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.example.utils.ThemeManager;
import org.example.utils.ViewNavigator;

public class FinvoraAlert {

    public enum AlertType {
        SUCCESS,
        ERROR,
        WARNING,
        INFO,
        CONFIRMATION
    }

    private final Stage stage;
    private final AlertType type;
    private final String titleText;
    private final String messageText;
    private boolean result = false;

    public FinvoraAlert(AlertType type, String message) {
        this(type, getDefaultTitle(type), message);
    }

    public FinvoraAlert(AlertType type, String title, String message) {
        this.type = type;
        this.titleText = (title != null && !title.isEmpty()) ? title : getDefaultTitle(type);
        this.messageText = message != null ? message : "";

        stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);
        if (ViewNavigator.getMainStage() != null) {
            stage.initOwner(ViewNavigator.getMainStage());
        }

        buildUI();
    }

    private static String getDefaultTitle(AlertType type) {
        return switch (type) {
            case SUCCESS -> "Success";
            case ERROR -> "Error";
            case WARNING -> "Warning";
            case INFO -> "Notice";
            case CONFIRMATION -> "Confirm Action";
        };
    }

    private void buildUI() {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: transparent;");

        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(28, 28, 24, 28));
        card.setMinWidth(360);
        card.setMaxWidth(440);
        card.getStyleClass().addAll("card-elevated", "card");

        // Styling based on AlertType
        String badgeColor;
        String badgeIcon;
        switch (type) {
            case SUCCESS -> {
                badgeColor = "#10B981";
                badgeIcon = "✓";
            }
            case ERROR -> {
                badgeColor = "#EF4444";
                badgeIcon = "✕";
            }
            case WARNING -> {
                badgeColor = "#F59E0B";
                badgeIcon = "!";
            }
            case CONFIRMATION -> {
                badgeColor = "#6366F1";
                badgeIcon = "?";
            }
            case INFO -> {
                badgeColor = "#4F46E5";
                badgeIcon = "ℹ";
            }
            default -> {
                badgeColor = "#4F46E5";
                badgeIcon = "ℹ";
            }
        }

        // Icon badge
        StackPane iconBadge = new StackPane();
        Circle circle = new Circle(26);
        circle.setFill(Color.web(badgeColor, 0.15));
        circle.setStroke(Color.web(badgeColor, 0.5));
        circle.setStrokeWidth(1.5);

        Label iconLabel = new Label(badgeIcon);
        iconLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: " + badgeColor + ";");
        iconBadge.getChildren().addAll(circle, iconLabel);

        // Title & Message
        Label titleLabel = new Label(titleText);
        titleLabel.getStyleClass().add("h3");
        titleLabel.setStyle("-fx-font-weight: 800; -fx-text-alignment: center;");
        titleLabel.setWrapText(true);

        Label messageLabel = new Label(messageText);
        messageLabel.getStyleClass().add("body-text");
        messageLabel.setStyle("-fx-text-alignment: center; -fx-line-spacing: 3px;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(380);

        // Button action area
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(8, 0, 0, 0));

        if (type == AlertType.CONFIRMATION) {
            Button cancelBtn = new Button("Cancel");
            cancelBtn.getStyleClass().add("btn-secondary");
            cancelBtn.setMinWidth(110);
            cancelBtn.setOnAction(e -> {
                result = false;
                closeWithAnimation(card);
            });

            Button confirmBtn = new Button("Confirm");
            confirmBtn.getStyleClass().add("btn-primary");
            confirmBtn.setMinWidth(110);
            confirmBtn.setOnAction(e -> {
                result = true;
                closeWithAnimation(card);
            });

            buttonBox.getChildren().addAll(cancelBtn, confirmBtn);
        } else {
            Button okBtn = new Button("Got it");
            okBtn.getStyleClass().add("btn-primary");
            okBtn.setMinWidth(130);
            okBtn.setOnAction(e -> {
                result = true;
                closeWithAnimation(card);
            });
            buttonBox.getChildren().add(okBtn);
        }

        card.getChildren().addAll(iconBadge, titleLabel, messageLabel, buttonBox);

        // Soft drop shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.28));
        shadow.setRadius(24);
        shadow.setOffsetY(10);
        card.setEffect(shadow);

        root.getChildren().add(card);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/theme.css").toExternalForm());
        ThemeManager.apply(scene);

        stage.setScene(scene);

        // Pop-in entrance animation
        stage.setOnShowing(e -> {
            card.setOpacity(0);
            card.setScaleX(0.85);
            card.setScaleY(0.85);

            FadeTransition ft = new FadeTransition(Duration.millis(200), card);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);

            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setFromX(0.85);
            st.setFromY(0.85);
            st.setToX(1.0);
            st.setToY(1.0);

            new ParallelTransition(ft, st).play();
        });
    }

    private void closeWithAnimation(VBox card) {
        FadeTransition ft = new FadeTransition(Duration.millis(150), card);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);

        ScaleTransition st = new ScaleTransition(Duration.millis(150), card);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(0.88);
        st.setToY(0.88);

        ParallelTransition pt = new ParallelTransition(ft, st);
        pt.setOnFinished(e -> stage.close());
        pt.play();
    }

    public void show() {
        stage.show();
    }

    public boolean showAndWait() {
        stage.showAndWait();
        return result;
    }

    // Static Helpers
    public static void show(AlertType type, String message) {
        new FinvoraAlert(type, message).show();
    }

    public static void show(AlertType type, String title, String message) {
        new FinvoraAlert(type, title, message).show();
    }

    public static boolean showAndWait(AlertType type, String message) {
        return new FinvoraAlert(type, message).showAndWait();
    }

    public static boolean showAndWait(AlertType type, String title, String message) {
        return new FinvoraAlert(type, title, message).showAndWait();
    }

    public static boolean confirm(String title, String message) {
        return new FinvoraAlert(AlertType.CONFIRMATION, title, message).showAndWait();
    }

    public static void showSuccess(String message) {
        show(AlertType.SUCCESS, message);
    }

    public static void showError(String message) {
        show(AlertType.ERROR, message);
    }

    public static void showWarning(String message) {
        show(AlertType.WARNING, message);
    }

    public static void showInfo(String message) {
        show(AlertType.INFO, message);
    }
}
