package org.example.dialogs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import org.example.models.SavingsGoal;
import org.example.utils.GoalStore;
import org.example.utils.ThemeManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ViewGoalsDialog extends Dialog<Void> {

    private final int userId;
    private final List<SavingsGoal> goals;

    public ViewGoalsDialog(List<SavingsGoal> goals, int userId) {
        this.userId = userId;
        this.goals = goals;

        setTitle("Your Savings Goals");
        DialogPane pane = getDialogPane();
        pane.setPrefSize(560, 540);
        pane.getStylesheets().add(getClass().getResource("/theme.css").toExternalForm());
        pane.getButtonTypes().add(new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE));

        VBox content = new VBox(16);
        content.setPadding(new Insets(16));
        if (goals == null || goals.isEmpty()) {
            Label noGoals = new Label("No savings goals found yet. Create one to get started!");
            noGoals.getStyleClass().add("body-text");
            content.getChildren().add(noGoals);
        } else {
            for (SavingsGoal goal : goals) {
                content.getChildren().add(createGoalNode(goal));
            }
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        pane.setContent(scrollPane);

        setOnShowing(e -> {
            if (pane.getScene() != null) {
                ThemeManager.apply(pane.getScene());
                javafx.stage.Window w = pane.getScene().getWindow();
                if (w instanceof javafx.stage.Stage stage) {
                    stage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/images/fintel_app_icon.png")));
                }
            }
        });
    }

    private VBox createGoalNode(SavingsGoal goal) {
        VBox box = new VBox(8);
        box.getStyleClass().addAll("card", "card-elevated");
        box.setPadding(new Insets(14, 16, 14, 16));

        Label title = new Label("🎯 " + goal.getName());
        title.getStyleClass().add("h3");

        Label deadline = new Label("⏳ Target Deadline: " + (goal.getDeadline() != null ? goal.getDeadline() : "Ongoing"));
        deadline.getStyleClass().add("caption-text");

        Label money = new Label("💰 ₹" + goal.getCurrentAmount() + " / ₹" + goal.getTargetAmount());
        money.getStyleClass().addAll("body-text", "text-primary");
        money.setStyle("-fx-font-weight: bold;");

        ProgressBar bar = new ProgressBar();
        double pct = 0;
        try {
            if (goal.getTargetAmount() != null && goal.getTargetAmount().doubleValue() > 0) {
                pct = goal.getCurrentAmount().divide(goal.getTargetAmount(), 4, java.math.RoundingMode.HALF_UP).doubleValue();
            }
        } catch (Exception ignored) {}
        bar.setProgress(Math.min(1.0, pct));
        bar.setPrefWidth(360);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.getStyleClass().add("progress-bar");

        Button add = new Button("➕ Add Savings");
        add.getStyleClass().add("btn-primary");
        add.setOnAction(e -> {
            TextInputDialog d = new TextInputDialog("0");
            d.setHeaderText("Add amount to: " + goal.getName());
            Optional<String> r = d.showAndWait();
            r.ifPresent(val -> {
                try {
                    BigDecimal inc = new BigDecimal(val);
                    BigDecimal next = goal.getCurrentAmount().add(inc);
                    if (goal.getTargetAmount() != null && next.compareTo(goal.getTargetAmount()) > 0) {
                        next = goal.getTargetAmount();
                    }
                    goal.setCurrentAmount(next);
                    GoalStore.update(userId, goal);
                    refresh();
                } catch (Exception ex) {
                    FintelAlert.showError("Invalid amount entered!");
                }
            });
        });

        Button edit = new Button("✏ Edit");
        edit.getStyleClass().add("btn-secondary");
        edit.setOnAction(e -> {
            TextInputDialog d = new TextInputDialog(goal.getName());
            d.setHeaderText("Edit Goal Name:");
            d.showAndWait().ifPresent(name -> {
                goal.setName(name);
                GoalStore.update(userId, goal);
                refresh();
            });
        });

        Button del = new Button("🗑 Delete");
        del.getStyleClass().add("btn-secondary");
        del.setOnAction(e -> {
            boolean confirmed = FintelAlert.confirm("Delete Savings Goal", "Are you sure you want to delete '" + goal.getName() + "'?");
            if (confirmed) {
                GoalStore.delete(userId, goal.getId());
                refresh();
            }
        });

        ToolBar tools = new ToolBar(add, edit, del);
        box.getChildren().addAll(title, deadline, money, bar, tools);
        return box;
    }

    private void refresh() {
        this.close();
        List<SavingsGoal> updated = GoalStore.getGoals(userId);
        new ViewGoalsDialog(updated, userId).showAndWait();
    }
}