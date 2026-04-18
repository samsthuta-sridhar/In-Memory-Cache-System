package com.imcs.presentation;

import com.imcs.security.AuthService;
import com.imcs.security.SessionManager;
import com.imcs.service.CacheService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class MainViewController implements Initializable {

    @Autowired private AuthService authService;
    @Autowired private SessionManager sessionManager;
    @Autowired private ConfigurableApplicationContext applicationContext;
    @Autowired private CacheService cacheService;

    @FXML private Label statusBar;

    private Timeline sessionTimer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sessionManager.reset();
        startSessionTimer();
    }

    private void startSessionTimer() {
        sessionTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            long remaining = sessionManager.getRemainingMs();
            long minutes = remaining / 60000;
            long seconds = (remaining % 60000) / 1000;

            // Always reads live policy from CacheService
            String policy = cacheService.getCurrentPolicy();

            Platform.runLater(() ->
                statusBar.setText(String.format(
                    "● Connected  |  User: %s  |  Eviction: %s  |  Session expires in: %d:%02d",
                    authService.getCurrentUser(),
                    policy,
                    minutes,
                    seconds))
            );

            if (sessionManager.isSessionExpired()) {
                Platform.runLater(this::handleLogout);
            }
        }));
        sessionTimer.setCycleCount(Timeline.INDEFINITE);
        sessionTimer.play();
    }

    @FXML
    public void handleLogout() {
        if (sessionTimer != null) sessionTimer.stop();
        authService.logout();
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            Stage stage = (Stage) statusBar.getScene().getWindow();
            stage.setScene(new Scene(root, 600, 500));
            stage.setTitle("In-Memory Cache System — Login");
            LoginController controller = loader.getController();
            controller.setStage(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}