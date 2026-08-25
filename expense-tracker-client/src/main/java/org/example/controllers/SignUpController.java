package org.example.controllers;

import com.google.gson.JsonObject;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import org.example.utils.ApiClient;
import org.example.utils.Utilitie;
import org.example.views.LoginView;
import org.example.views.SignUpView;

public class SignUpController {
    private SignUpView signUpView;

    public SignUpController(SignUpView signUpView){
        this.signUpView = signUpView;
        initialize();
    }

    private void initialize(){
        signUpView.getLoginLabel().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                new LoginView().show();
            }
        });

        signUpView.getRegisterButton().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(!validateInput()){
                    return;
                }

                String name = signUpView.getNameField().getText().trim();
                String username = signUpView.getUsernameField().getText().trim();
                String password = signUpView.getPasswordField().getText();

                JsonObject jsonData = new JsonObject();
                jsonData.addProperty("name", name);
                jsonData.addProperty("email", username);
                jsonData.addProperty("password", password);

                boolean postCreateAccountStatus = ApiClient.postCreateUser(jsonData);

                if(postCreateAccountStatus){
                    Utilitie.showAlertDialog(Alert.AlertType.INFORMATION, "Successfully created new account!");
                    new LoginView().show();
                }else{
                    Utilitie.showAlertDialog(Alert.AlertType.ERROR, "Failed to create new account. Email may already be registered.");
                }
            }
        });
    }

    private boolean validateInput(){
        String name = signUpView.getNameField().getText().trim();
        String email = signUpView.getUsernameField().getText().trim();
        String password = signUpView.getPasswordField().getText();
        String rePassword = signUpView.getRePasswordField().getText();

        if(name.isEmpty()){
            Utilitie.showAlertDialog(Alert.AlertType.ERROR, "Please enter your name");
            return false;
        }

        if(email.isEmpty() || !email.contains("@")){
            Utilitie.showAlertDialog(Alert.AlertType.ERROR, "Please enter a valid email address");
            return false;
        }

        if(password.isEmpty() || password.length() < 6){
            Utilitie.showAlertDialog(Alert.AlertType.ERROR, "Password must be at least 6 characters");
            return false;
        }

        if(!password.equals(rePassword)){
            Utilitie.showAlertDialog(Alert.AlertType.ERROR, "Passwords do not match");
            return false;
        }

        if(ApiClient.checkEmailExists(email)){
            Utilitie.showAlertDialog(Alert.AlertType.ERROR, "An account with this email already exists");
            return false;
        }

        return true;
    }
}
