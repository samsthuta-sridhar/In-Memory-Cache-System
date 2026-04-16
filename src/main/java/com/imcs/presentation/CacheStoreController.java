package com.imcs.presentation;

import com.imcs.cache.CacheManager;
import com.imcs.service.CacheService;
import com.imcs.service.CacheStatsService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class CacheStoreController implements Initializable {

    @Autowired private CacheService cacheService;
    @Autowired private CacheManager cacheManager;
    @Autowired private CacheStatsService statsService;

    @FXML private TextField keyField;
    @FXML private TextField valueField;
    @FXML private TextArea resultArea;
    @FXML private Label statusLabel;
    @FXML private Label capacityLabel;
    @FXML private ProgressBar capacityBar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        keyField.textProperty().addListener((obs, o, n) -> {
            resultArea.clear();
            statusLabel.setText("");
        });
        valueField.textProperty().addListener((obs, o, n) -> {
            resultArea.clear();
            statusLabel.setText("");
        });

        // Auto-refresh capacity bar every second
        Timeline autoRefresh = new Timeline(
            new KeyFrame(Duration.seconds(1),
                e -> Platform.runLater(this::updateCapacityIndicator)));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();

        updateCapacityIndicator();
    }

    @FXML
    public void handleStore() {
        String key = keyField.getText().trim();
        String value = valueField.getText().trim();

        if (key.isEmpty() || value.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Please enter both key and value.");
            return;
        }
        if (!key.matches("[a-zA-Z0-9:_\\-]+")) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Invalid key. Use letters, numbers, :, _, -");
            return;
        }

        int sizeBefore = cacheManager.getCurrentSize();
        int maxSize = cacheManager.getMaxCapacity();

        CacheService.PutResult result = cacheService.put(key, value);

        StringBuilder sb = new StringBuilder();

        if (result.evictionHappened) {
            sb.append("CACHE FULL — EVICTION TRIGGERED\n\n");
            sb.append("Eviction Policy: ")
              .append(result.evictionPolicy).append("\n\n");

            sb.append("EVICTED ENTRY:\n\n");
            sb.append("Key: ").append(result.evictedKey).append("\n");
            sb.append("Value: ").append(result.evictedValue).append("\n");
            sb.append("Reason: ");
            if (result.evictionPolicy.equals("LRU")) {
                sb.append("Least Recently Used (had the oldest lastAccessTime)\n\n");
            } else {
                sb.append("Least Frequently Used (had the lowest accessCount)\n\n");
            }

            sb.append("NEW ENTRY STORED:\n\n");
            sb.append("Key: ").append(key).append("\n");
            sb.append("Value: ").append(value).append("\n\n");
            sb.append("Cache now: ")
              .append(cacheManager.getCurrentSize())
              .append("/").append(maxSize).append("\n");

            statusLabel.setStyle(
                "-fx-text-fill: #FF9800; -fx-font-weight: bold;");
            statusLabel.setText("⚠ STORED — evicted "
                + result.evictedKey
                + " (" + result.evictionPolicy + ")");
        } else {
            sb.append("✓  STORED SUCCESSFULLY\n\n");
            sb.append("Key: ").append(key).append("\n");
            sb.append("Value: ").append(value).append("\n\n");

            statusLabel.setStyle(
                "-fx-text-fill: green; -fx-font-weight: bold;");
            statusLabel.setText("✓ STORED");
        }

        resultArea.setText(sb.toString());
        updateCapacityIndicator();
    }

    private void updateCapacityIndicator() {
        int current = cacheManager.getCurrentSize();
        int max = cacheManager.getMaxCapacity();
        double percent = max > 0 ? (double) current / max : 0;

        capacityBar.setProgress(percent);
        capacityLabel.setText(String.format(
            "Cache: %d / %d entries  (%d%%)",
            current, max, (int)(percent * 100)));

        if (percent >= 1.0) {
            capacityLabel.setStyle(
                "-fx-text-fill: #F44336; -fx-font-weight: bold;");
            capacityBar.setStyle("-fx-accent: #F44336;");
        } else if (percent >= 0.7) {
            capacityLabel.setStyle(
                "-fx-text-fill: #FF9800; -fx-font-weight: bold;");
            capacityBar.setStyle("-fx-accent: #FF9800;");
        } else {
            capacityLabel.setStyle(
                "-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            capacityBar.setStyle("-fx-accent: #4CAF50;");
        }
    }
}