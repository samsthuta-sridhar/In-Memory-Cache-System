package com.imcs.presentation;

import com.imcs.cache.CacheManager;
import com.imcs.entity.AuditLogEntity;
import com.imcs.entity.CacheDataEntity;
import com.imcs.repository.CacheDataRepository;
import com.imcs.security.EncryptionService;
import com.imcs.service.AuditLogService;
import com.imcs.service.CacheService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class CacheMaintenanceController implements Initializable {

    @Autowired private CacheService cacheService;
    @Autowired private AuditLogService auditLogService;
    @Autowired private CacheManager cacheManager;
    @Autowired private CacheDataRepository cacheDataRepository;
    @Autowired private EncryptionService encryptionService;

    @FXML private TextField keyField;
    @FXML private TextField valueField;
    @FXML private TextArea logArea;
    @FXML private Label statusLabel;
    @FXML private TableView<CacheDataEntity> accountsTable;
    @FXML private TableColumn<CacheDataEntity, String> accountKeyCol;
    @FXML private TableColumn<CacheDataEntity, String> accountValueCol;
    @FXML private TableColumn<CacheDataEntity, String> accountCacheCol;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        accountKeyCol.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getKey()));

        accountValueCol.setCellValueFactory(d -> {
            try {
                return new SimpleStringProperty(
                    encryptionService.decrypt(d.getValue().getValue()));
            } catch (Exception e) {
                return new SimpleStringProperty(d.getValue().getValue());
            }
        });

        accountCacheCol.setCellValueFactory(d ->
            new SimpleStringProperty(
                cacheManager.containsKey(d.getValue().getKey())
                    ? "✓ In Cache" : "✗ DB Only"));

        accountsTable.getSelectionModel()
            .selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    keyField.setText(newVal.getKey());
                    valueField.clear();
                    valueField.setPromptText("Enter new value...");
                    statusLabel.setStyle("-fx-text-fill: #2196F3;");
                    statusLabel.setText("Selected: " + newVal.getKey());
                }
            });

        refreshAll();
    }

    @FXML
    public void handleUpdate() {
        String key = keyField.getText().trim();
        String value = valueField.getText().trim();
        if (key.isEmpty() || value.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Enter both key and new value.");
            return;
        }
        cacheService.update(key, value);
        statusLabel.setStyle("-fx-text-fill: green;");
        statusLabel.setText("✓ Updated: " + key + " → " + value);
        valueField.clear();
        refreshAll();
    }

    @FXML
    public void handleDelete() {
        String key = keyField.getText().trim();
        if (key.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Enter a key to delete.");
            return;
        }
        cacheService.delete(key);
        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setText("✗ Deleted: " + key);
        keyField.clear();
        valueField.clear();
        refreshAll();
    }

    @FXML
    public void handleRefreshLog() {
        refreshAll();
    }

    private void refreshAll() {
        refreshLog();
        refreshTable();
    }

    private void refreshLog() {
        List<AuditLogEntity> logs = auditLogService.getRecentLogs();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-19s  %-6s  %-8s  %-25s  %s%n",
            "TIMESTAMP", "USER", "OP", "KEY", "DETAILS"));
        sb.append("─".repeat(90)).append("\n");
        for (AuditLogEntity log : logs) {
            sb.append(String.format("%-19s  %-6s  %-8s  %-25s  %s%n",
                log.getTimestamp().toString().substring(0, 19),
                log.getUsername(),
                log.getOperation(),
                log.getCacheKey(),
                log.getStatus()));
        }
        logArea.setText(sb.toString());
    }

    private void refreshTable() {
        accountsTable.setItems(
            FXCollections.observableArrayList(
                cacheDataRepository.findAll()));
    }
}