package com.imcs.presentation;

import com.imcs.StageReadyEvent;
import com.imcs.security.AuthService;
import com.imcs.security.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Autowired private AuthService authService;
    @Autowired private SessionManager sessionManager;
    @Autowired private ConfigurableApplicationContext applicationContext;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Label attemptsLabel;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password.");
            return;
        }

        AuthService.LoginResult result = authService.login(username, password);

        switch (result) {
            case SUCCESS:
                sessionManager.reset();
                openMainWindow();
                break;
            case WRONG_PASSWORD:
                errorLabel.setText("Incorrect password.");
                attemptsLabel.setText("Warning: Too many failed attempts will lock your account.");
                passwordField.clear();
                break;
            case ACCOUNT_LOCKED:
                errorLabel.setText("Account is locked. Contact administrator.");
                attemptsLabel.setText("");
                break;
            case USER_NOT_FOUND:
                errorLabel.setText("User not found.");
                break;
        }
    }

    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            Scene scene = new Scene(root, 1000, 650);
            stage.setScene(scene);
            stage.setTitle("In-Memory Cache System — Banking");
            stage.show();
        } catch (Exception e) {
            log.error("Failed to load main window", e);
            errorLabel.setText("Failed to load main window.");
        }
    }
}