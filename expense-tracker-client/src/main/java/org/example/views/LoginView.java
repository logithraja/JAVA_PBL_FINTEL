package org.example.views;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.example.controllers.LoginController;
import org.example.utils.Utilitie;
import org.example.utils.ViewNavigator;

public class LoginView {
    private Label expenseTrackerLabel = new Label("Finance Tracker");
    private TextField usernameField = new TextField();
    private PasswordField passwordField = new PasswordField();
    private Button loginButton = new Button("Login");
    private Label signupLabel = new Label("Don't have an account? Click Here");

    public void show(){
        Scene scene = createScene();
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        new LoginController(this);
        ViewNavigator.switchViews(scene);
    }

    private Scene createScene(){
        javafx.scene.layout.HBox root = new javafx.scene.layout.HBox();
        root.getStyleClass().addAll("main-background", "split-root");
        root.setAlignment(Pos.CENTER);

        // --- LEFT SIDE (Deep Indigo Gradient + Hero Visual) ---
        VBox leftSide = new VBox(20);
        leftSide.setStyle("-fx-background-color: linear-gradient(to bottom right, #0F172A 0%, #1E1B4B 60%, #312E81 100%);");
        leftSide.setAlignment(Pos.CENTER);
        javafx.scene.layout.HBox.setHgrow(leftSide, javafx.scene.layout.Priority.ALWAYS);
        leftSide.setMaxWidth(Double.MAX_VALUE);
        
        try {
            javafx.scene.image.Image logoImg = new javafx.scene.image.Image(java.util.Objects.requireNonNull(getClass().getResourceAsStream("/images/finvora_logo.png")));
            javafx.scene.image.ImageView logoView = new javafx.scene.image.ImageView(logoImg);
            logoView.setPreserveRatio(true);
            logoView.setFitHeight(44);
            
            Label logoText = new Label("FINVORA");
            logoText.setStyle("-fx-font-size: 26px; -fx-font-weight: 900; -fx-text-fill: white; -fx-letter-spacing: 2px;");
            
            javafx.scene.layout.HBox logoBox = new javafx.scene.layout.HBox(15, logoView, logoText);
            logoBox.setAlignment(Pos.CENTER_LEFT);
            logoBox.setPadding(new javafx.geometry.Insets(35, 0, 30, 50));
            leftSide.getChildren().add(logoBox);
        } catch (Exception e) {
            System.err.println("Could not load logo image");
        }

        try {
            javafx.scene.image.Image heroImg = new javafx.scene.image.Image(java.util.Objects.requireNonNull(getClass().getResourceAsStream("/images/finvora_login_hero.png")));
            javafx.scene.image.ImageView heroView = new javafx.scene.image.ImageView(heroImg);
            heroView.setPreserveRatio(true);
            heroView.setFitHeight(340);
            leftSide.getChildren().add(heroView);
        } catch (Exception e) {
            System.err.println("Could not load login hero image");
        }
        
        Label quote = new Label("Get All Your Finances\nAt One Place.");
        quote.setStyle("-fx-font-size: 30px; -fx-font-weight: 800; -fx-text-fill: #FFFFFF; -fx-text-alignment: center;");
        quote.setPadding(new javafx.geometry.Insets(30, 0, 0, 0));
        leftSide.getChildren().add(quote);

        // --- RIGHT SIDE (Elevated Card Form) ---
        VBox rightSide = new VBox();
        rightSide.setAlignment(Pos.CENTER);
        rightSide.getStyleClass().add("main-background");
        javafx.scene.layout.HBox.setHgrow(rightSide, javafx.scene.layout.Priority.ALWAYS);
        rightSide.setMaxWidth(Double.MAX_VALUE);
        
        VBox formContainer = new VBox(16);
        formContainer.getStyleClass().addAll("card-elevated");
        formContainer.setAlignment(Pos.CENTER_LEFT);
        formContainer.setMaxWidth(420); 
        
        Label loginTitle = new Label("Login to your account");
        loginTitle.getStyleClass().add("h1");
        
        Label loginSub = new Label("Welcome back! Please enter your details.");
        loginSub.getStyleClass().add("caption-text");
        
        VBox headerBox = new VBox(6, loginTitle, loginSub);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new javafx.geometry.Insets(0, 0, 15, 0));

        Label emailLabel = new Label("Email Address");
        emailLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        usernameField.getStyleClass().add("input-field");
        usernameField.setPromptText("Enter your email");
        VBox emailBox = new VBox(6, emailLabel, usernameField);

        javafx.scene.layout.HBox passHeader = new javafx.scene.layout.HBox();
        Label passLabel = new Label("Password");
        passLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        Label forgotLabel = new Label("Forgot password?");
        forgotLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4F46E5; -fx-cursor: hand; -fx-font-weight: 600;");
        javafx.scene.layout.Region passSpacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(passSpacer, javafx.scene.layout.Priority.ALWAYS);
        passHeader.getChildren().addAll(passLabel, passSpacer, forgotLabel);

        passwordField.getStyleClass().add("input-field");
        passwordField.setPromptText("••••••••");
        VBox passBox = new VBox(6, passHeader, passwordField);

        javafx.scene.control.CheckBox rememberMe = new javafx.scene.control.CheckBox("Remember Me");
        rememberMe.setStyle("-fx-padding: 6 0 10 0; -fx-font-size: 13px;");

        loginButton.setText("Sign In");
        loginButton.getStyleClass().addAll("btn-primary");
        loginButton.setMaxWidth(Double.MAX_VALUE);

        javafx.scene.layout.HBox signupBox = new javafx.scene.layout.HBox(6);
        signupBox.setAlignment(Pos.CENTER);
        signupBox.setPadding(new javafx.geometry.Insets(14, 0, 0, 0));
        Label noAcc = new Label("Don't have an account?");
        noAcc.getStyleClass().add("body-text");
        signupLabel.setText("Sign Up");
        signupLabel.setStyle("-fx-text-fill: #4F46E5; -fx-font-weight: bold; -fx-cursor: hand;");
        signupBox.getChildren().addAll(noAcc, signupLabel);

        formContainer.getChildren().addAll(headerBox, emailBox, passBox, rememberMe, loginButton, signupBox);
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

    public Button getLoginButton() {
        return loginButton;
    }

    public void setLoginButton(Button loginButton) {
        this.loginButton = loginButton;
    }

    public Label getSignupLabel() {
        return signupLabel;
    }

    public void setSignupLabel(Label signupLabel) {
        this.signupLabel = signupLabel;
    }
}
