package org.example.controllers;

import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import org.example.utils.ApiClient;
import org.example.utils.Utilitie;
import org.example.views.DashboardView;
import org.example.views.LoginView;
import org.example.views.SignUpView;

public class LoginController {
    private LoginView loginView;

    public LoginController(LoginView loginView){
        this.loginView = loginView;
        initialize();
    }

    private void initialize(){
        loginView.getLoginButton().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(!validateUser()) return;

                String email = loginView.getUsernameField().getText().trim();
                String password = loginView.getPasswordField().getText();

                if(ApiClient.postLoginUser(email, password)){
                    Utilitie.showAlertDialog(Alert.AlertType.INFORMATION, "Login Successful!");
                    new DashboardView(email).show();
                }else{
                    Utilitie.showAlertDialog(Alert.AlertType.ERROR, "Failed to authenticate. Please check your credentials.");
                }
            }
        });

        loginView.getSignupLabel().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                new SignUpView().show();
            }
        });
    }

    private boolean validateUser(){
        if(loginView.getUsernameField().getText().trim().isEmpty()){
            Utilitie.showAlertDialog(Alert.AlertType.WARNING, "Email is required");
            return false;
        }

        if(loginView.getPasswordField().getText().isEmpty()){
            Utilitie.showAlertDialog(Alert.AlertType.WARNING, "Password is required");
            return false;
        }

        return true;
    }
}
