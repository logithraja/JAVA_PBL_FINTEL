package org.example.views;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.example.controllers.LoginController;
import org.example.controllers.SignUpController;
import org.example.utils.Utilitie;
import org.example.utils.ViewNavigator;

public class SignUpView {
    private Label expenseTrackerLabel = new Label("Fintel");
    private TextField nameField = new TextField();
    private TextField usernameField = new TextField();
    private PasswordField passwordField = new PasswordField();
    private PasswordField rePasswordField = new PasswordField();
    private Button registerButton = new Button("Register");
    private Label loginLabel = new Label("Already have an account? Login here");

    public void show(){
        Scene scene = createScene();
        org.example.utils.ThemeManager.apply(scene);

        new SignUpController(this);
        ViewNavigator.switchViews(scene);
    }

    private Scene createScene(){
        javafx.scene.layout.HBox root = new javafx.scene.layout.HBox();
        root.getStyleClass().addAll("main-background", "split-root");
        root.setAlignment(Pos.CENTER);

        // --- LEFT SIDE (Gradient + Hero) ---
        VBox leftSide = new VBox();
        leftSide.setStyle("-fx-background-color: linear-gradient(to bottom right, #0F172A 0%, #1E1B4B 60%, #312E81 100%);"); 
        leftSide.setAlignment(Pos.CENTER);
        javafx.scene.layout.HBox.setHgrow(leftSide, javafx.scene.layout.Priority.ALWAYS);
        leftSide.setMaxWidth(Double.MAX_VALUE);
        
        try {
            javafx.scene.image.Image heroImg = new javafx.scene.image.Image(java.util.Objects.requireNonNull(getClass().getResourceAsStream("/images/fintel_register_hero.png")));
            javafx.scene.image.ImageView heroView = new javafx.scene.image.ImageView(heroImg);
            heroView.setPreserveRatio(true);
            heroView.setFitHeight(460);
            leftSide.getChildren().add(heroView);
        } catch (Exception e) {
            System.err.println("Could not load register hero image");
        }

        // --- RIGHT SIDE (Elevated Card Form) ---
        VBox rightSide = new VBox();
        rightSide.setAlignment(Pos.CENTER);
        rightSide.getStyleClass().add("main-background");
        javafx.scene.layout.HBox.setHgrow(rightSide, javafx.scene.layout.Priority.ALWAYS);
        rightSide.setMaxWidth(Double.MAX_VALUE);
        
        VBox formContainer = new VBox(14);
        formContainer.getStyleClass().addAll("card-elevated");
        formContainer.setAlignment(Pos.CENTER_LEFT);
        formContainer.setMaxWidth(420);
        
        Label subTitle = new Label("Create Fintel Account");
        subTitle.getStyleClass().add("h1");
        
        Label subDesc = new Label("Start tracking your wealth and goals today.");
        subDesc.getStyleClass().add("caption-text");
        
        VBox headerBox = new VBox(4, subTitle, subDesc);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new javafx.geometry.Insets(0, 0, 10, 0));

        nameField.getStyleClass().add("input-field");
        nameField.setPromptText("Full Name");

        usernameField.getStyleClass().add("input-field");
        usernameField.setPromptText("Email Address");

        passwordField.getStyleClass().add("input-field");
        passwordField.setPromptText("Create Password");

        rePasswordField.getStyleClass().add("input-field");
        rePasswordField.setPromptText("Confirm Password");

        javafx.scene.control.CheckBox termsBox = new javafx.scene.control.CheckBox("I agree to the Terms of Service and Privacy Policy");
        termsBox.setWrapText(true);
        termsBox.setStyle("-fx-font-size: 12px; -fx-padding: 6 0 6 0;");

        registerButton.setText("Register Account");
        registerButton.getStyleClass().addAll("btn-primary");
        registerButton.setMaxWidth(Double.MAX_VALUE);

        javafx.scene.layout.HBox loginBox = new javafx.scene.layout.HBox(6);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new javafx.geometry.Insets(10, 0, 0, 0));
        Label alreadyAcc = new Label("Already have an account?");
        alreadyAcc.getStyleClass().add("body-text");
        loginLabel.setText("Sign In");
        loginLabel.setStyle("-fx-text-fill: #4F46E5; -fx-font-weight: bold; -fx-cursor: hand;");
        loginBox.getChildren().addAll(alreadyAcc, loginLabel);

        formContainer.getChildren().addAll(headerBox, nameField, usernameField, passwordField, rePasswordField, termsBox, registerButton, loginBox);
        rightSide.getChildren().add(formContainer);

        root.getChildren().addAll(leftSide, rightSide);
        return new Scene(root, Utilitie.APP_WIDTH, Utilitie.APP_HEIGHT);
    }

    public Label getExpenseTrackerLabel() {
        return expenseTrackerLabel;
    }

    public void setExpenseTrackerLabel(Label expenseTrackerLabel) {
        this.expenseTrackerLabel = expenseTrackerLabel;
    }

    public TextField getNameField() {
        return nameField;
    }

    public void setNameField(TextField nameField) {
        this.nameField = nameField;
    }

    public TextField getUsernameField() {
        return usernameField;
    }

    public void setUsernameField(TextField usernameField) {
        this.usernameField = usernameField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public void setPasswordField(PasswordField passwordField) {
        this.passwordField = passwordField;
    }

    public PasswordField getRePasswordField() {
        return rePasswordField;
    }

    public void setRePasswordField(PasswordField rePasswordField) {
        this.rePasswordField = rePasswordField;
    }

    public Button getRegisterButton() {
        return registerButton;
    }

    public void setRegisterButton(Button registerButton) {
        this.registerButton = registerButton;
    }

    public Label getLoginLabel() {
        return loginLabel;
    }

    public void setLoginLabel(Label loginLabel) {
        this.loginLabel = loginLabel;
    }
}
