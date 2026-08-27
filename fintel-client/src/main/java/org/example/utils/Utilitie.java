package org.example.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ColorPicker;
import org.example.models.TransactionCategory;

import java.util.List;

public class Utilitie {
    public static final int APP_WIDTH = 1280;
    public static final int APP_HEIGHT = 768;

    public static void showAlertDialog(Alert.AlertType alertType, String message){
        org.example.dialogs.FintelAlert.AlertType targetType = switch (alertType) {
            case INFORMATION, CONFIRMATION -> org.example.dialogs.FintelAlert.AlertType.SUCCESS;
            case ERROR -> org.example.dialogs.FintelAlert.AlertType.ERROR;
            case WARNING -> org.example.dialogs.FintelAlert.AlertType.WARNING;
            default -> org.example.dialogs.FintelAlert.AlertType.INFO;
        };
        org.example.dialogs.FintelAlert.showAndWait(targetType, message);
    }

    public static String getHexColorValue(ColorPicker colorPicker){
        String color = colorPicker.getValue().toString();
        return color.substring(2, color.length() - 2);
    }

    public static TransactionCategory findTransactionCategoryByName(List<TransactionCategory> transactionCategories,
                                                              String categoryName){
        for(TransactionCategory transactionCategory : transactionCategories){
            if(transactionCategory.getCategoryName().equals(categoryName)){
                return transactionCategory;
            }
        }

        return null;
    }
}
