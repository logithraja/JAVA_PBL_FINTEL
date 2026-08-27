package org.example.controllers;

import com.google.gson.JsonObject;
import org.example.utils.ApiClient;
import org.example.views.ScenarioView;

public class ScenarioController {

    private final ScenarioView view;

    public ScenarioController(ScenarioView view) {
        this.view = view;
        initListeners();
    }

    private void initListeners() {
        view.getSimulateBtn().setOnAction(e -> runSimulation());
        view.getAmountField().setOnAction(e -> runSimulation());
    }

    private void runSimulation() {
        String amountText = view.getAmountField().getText().trim();
        String category = view.getCategoryComboBox().getValue();
        Integer month = view.getMonthComboBox().getValue();
        Integer year = view.getYearComboBox().getValue();

        if (amountText.isEmpty()) {
            showResults(null);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText.replace(",", "").replace("₹", ""));
        } catch (NumberFormatException ex) {
            showResults(null);
            return;
        }

        if (category == null || category.isEmpty()) {
            showResults(null);
            return;
        }

        if (month == null) {
            showResults(null);
            return;
        }

        view.getLoadingLabel().setVisible(true);
        view.getSimulateBtn().setDisable(true);

        int userId = view.getUserId();
        int finalMonth = month;
        int finalYear = year != null ? year : java.time.LocalDate.now().getYear();

        new Thread(() -> {
            try {
                JsonObject result = ApiClient.simulateScenario(userId, amount, category, finalMonth, finalYear);
                javafx.application.Platform.runLater(() -> {
                    view.getLoadingLabel().setVisible(false);
                    view.getSimulateBtn().setDisable(false);
                    view.showResults(result);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    view.getLoadingLabel().setVisible(false);
                    view.getSimulateBtn().setDisable(false);
                    view.showResults(null);
                });
            }
        }).start();
    }

    private void showResults(JsonObject result) {
        view.showResults(result);
    }
}
