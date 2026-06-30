package com.imcs.presentation;

import com.imcs.cache.CacheEntry;
import com.imcs.cache.CacheManager;
import com.imcs.security.EncryptionService;
import com.imcs.service.CacheService;
import com.imcs.service.CacheStatsService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.ResourceBundle;

@Component
public class CacheStatsController implements Initializable {

    @Autowired private CacheStatsService statsService;
    @Autowired private CacheManager cacheManager;
    @Autowired private EncryptionService encryptionService;
    @Autowired private CacheService cacheService;

    @FXML private Label hitLabel;
    @FXML private Label missLabel;
    @FXML private Label hitRateLabel;
    @FXML private Label storedLabel;
    @FXML private Label evictionLabel;
    @FXML private Label capacityLabel;
    @FXML private Label policyLabel;
    @FXML private RadioButton lruRadio;
    @FXML private RadioButton lfuRadio;
    @FXML private RadioButton fifoRadio;

    @FXML private TableView<Map.Entry<String, CacheEntry>> cacheTable;
    @FXML private TableColumn<Map.Entry<String, CacheEntry>, String> keyCol;
    @FXML private TableColumn<Map.Entry<String, CacheEntry>, String> valueCol;
    @FXML private TableColumn<Map.Entry<String, CacheEntry>, String> accessCol;
    @FXML private TableColumn<Map.Entry<String, CacheEntry>, String> expiryCol;
    @FXML private TableColumn<Map.Entry<String, CacheEntry>, String> lastAccessCol;

    private ToggleGroup policyGroup;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        policyGroup = new ToggleGroup();
        lruRadio.setToggleGroup(policyGroup);
        lfuRadio.setToggleGroup(policyGroup);
        fifoRadio.setToggleGroup(policyGroup);
        lruRadio.setSelected(true);

        keyCol.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getKey()));

        valueCol.setCellValueFactory(d -> {
            try {
                return new SimpleStringProperty(
                    encryptionService.decrypt(
                        d.getValue().getValue().getValue()));
            } catch (Exception e) {
                return new SimpleStringProperty("");
            }
        });

        accessCol.setCellValueFactory(d ->
            new SimpleStringProperty(
                String.valueOf(
                    d.getValue().getValue().getAccessCount())));

        expiryCol.setCellValueFactory(d -> {
            long remaining = d.getValue().getValue().getExpiryTime()
                - System.currentTimeMillis();
            if (remaining <= 0)
                return new SimpleStringProperty("EXPIRED");
            long mins = remaining / 60000;
            long secs = (remaining % 60000) / 1000;
            return new SimpleStringProperty(
                mins > 0 ? mins + "m " + secs + "s" : secs + "s");
        });

        lastAccessCol.setCellValueFactory(d -> {
            long ts = d.getValue().getValue().getLastAccessTime();
            return new SimpleStringProperty(
                new SimpleDateFormat("HH:mm:ss").format(new Date(ts)));
        });

        Timeline autoRefresh = new Timeline(
            new KeyFrame(Duration.seconds(1),
                e -> Platform.runLater(this::refreshData)));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();

        refreshData();
    }

    @FXML
    public void selectLRU() {
        cacheService.setEvictionPolicy("LRU");
        policyLabel.setText("Active: LRU — removes least recently accessed");
        policyLabel.setStyle(
            "-fx-text-fill: #2196F3; -fx-font-weight: bold;");
        lruRadio.setSelected(true);
    }

    @FXML
    public void selectLFU() {
        cacheService.setEvictionPolicy("LFU");
        policyLabel.setText("Active: LFU — removes least frequently accessed");
        policyLabel.setStyle(
            "-fx-text-fill: #9C27B0; -fx-font-weight: bold;");
        lfuRadio.setSelected(true);
    }

    @FXML
    public void selectFIFO() {
        cacheService.setEvictionPolicy("FIFO");
        policyLabel.setText("Active: FIFO — removes oldest inserted entry");
        policyLabel.setStyle(
            "-fx-text-fill: #FF5722; -fx-font-weight: bold;");
        fifoRadio.setSelected(true);
    }

    @FXML public void handleRefresh() { refreshData(); }

    @FXML
    public void handleReset() {
        statsService.reset();
        refreshData();
    }

    private void refreshData() {
        int currentSize = cacheManager.getCurrentSize();
        int maxSize = cacheManager.getMaxCapacity();

        hitLabel.setText(String.valueOf(statsService.getHitCount()));
        missLabel.setText(String.valueOf(statsService.getMissCount()));
        hitRateLabel.setText(
            String.format("%.1f%%", statsService.getHitRate()));
        storedLabel.setText(currentSize + "/" + maxSize);
        evictionLabel.setText(
            String.valueOf(statsService.getEvictionCount()));

        double pct = maxSize > 0 ? (double) currentSize / maxSize : 0;
        capacityLabel.setText(String.format(
            "Capacity: %d/%d (%d%%)",
            currentSize, maxSize, (int)(pct * 100)));

        if (pct >= 1.0) {
            capacityLabel.setStyle(
                "-fx-text-fill: #F44336; -fx-font-weight: bold;");
        } else if (pct >= 0.7) {
            capacityLabel.setStyle(
                "-fx-text-fill: #FF9800; -fx-font-weight: bold;");
        } else {
            capacityLabel.setStyle(
                "-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        }

        cacheTable.getItems().clear();
        cacheTable.getItems().addAll(
            cacheManager.getAllEntries().entrySet());

        cacheTable.getColumns().forEach(col -> {
            col.setVisible(false);
            col.setVisible(true);
        });
    }
}