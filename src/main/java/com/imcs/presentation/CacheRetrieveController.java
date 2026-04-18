package com.imcs.presentation;

import com.imcs.service.CacheService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class CacheRetrieveController implements Initializable {

    @Autowired private CacheService cacheService;

    @FXML private TextField keyField;
    @FXML private TextArea resultArea;
    @FXML private Label statusLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Clear result when user starts typing
        keyField.textProperty().addListener((obs, oldVal, newVal) -> {
            resultArea.clear();
            statusLabel.setText("");
        });
    }

    @FXML
    public void handleGet() {
        String key = keyField.getText().trim();

        if (key.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Key cannot be empty.");
            return;
        }
        if (!key.matches("[a-zA-Z0-9:_\\-]+")) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Invalid characters. Use letters, numbers, :, _, -");
            return;
        }

        String value = cacheService.getValue(key);

        if ("RATE_LIMITED".equals(value)) {
            resultArea.setText("Too many requests for this key. Please slow down.");
            statusLabel.setStyle("-fx-text-fill: orange;");
            statusLabel.setText("RATE LIMITED");
        } else if (value != null) {
            resultArea.setText("Key: " + key + "\nValue: " + value);
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText("CACHE HIT");
        } else {
            resultArea.setText("Key '" + key + "' not found in cache or database.");
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("NOT FOUND");
        }
    }
}