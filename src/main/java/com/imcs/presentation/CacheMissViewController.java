package com.imcs.presentation;

import com.imcs.cache.CacheManager;
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
public class CacheMissViewController implements Initializable {

    @Autowired private CacheService cacheService;
    @Autowired private CacheManager cacheManager;

    @FXML private TextField keyField;
    @FXML private TextArea resultArea;
    @FXML private Label statusLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        keyField.textProperty().addListener((obs, oldVal, newVal) -> {
            resultArea.clear();
            statusLabel.setText("");
        });
    }

    @FXML
    public void handleFetch() {
        String key = keyField.getText().trim();

        if (key.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Please enter a key.");
            return;
        }

        // Force a cache miss by removing from cache first
        boolean wasInCache = cacheManager.containsKey(key);
        if (wasInCache) {
            cacheManager.removeEntry(key);
        }

        // Now try to get — will trigger DB fallback
        String value = cacheService.getValue(key);

        if (value != null) {
            resultArea.setText(
                "Step 1: Checked cache → " + (wasInCache ? "was present, cleared for demo" : "NOT FOUND") + "\n" +
                "Step 2: Fell back to PostgreSQL → FOUND\n" +
                "Step 3: Stored back in cache\n\n" +
                "Key: " + key + "\nValue: " + value);
            statusLabel.setStyle("-fx-text-fill: #2196F3;");
            statusLabel.setText("CACHE MISS → DB HIT → CACHED");
        } else {
            resultArea.setText(
                "Step 1: Checked cache → NOT FOUND\n" +
                "Step 2: Fell back to PostgreSQL → NOT FOUND\n\n" +
                "Key '" + key + "' does not exist anywhere.");
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("NOT FOUND IN CACHE OR DB");
        }
    }
}