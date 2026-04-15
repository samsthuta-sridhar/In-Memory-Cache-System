package com.imcs;

import com.imcs.presentation.LoginController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    private final ConfigurableApplicationContext applicationContext;

    public StageInitializer(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/fxml/login.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = event.getStage();
            stage.setScene(new Scene(root, 600, 500));
            stage.setTitle("In-Memory Cache System — Login");
            stage.show();

            // Pass stage to login controller
            LoginController controller = loader.getController();
            controller.setStage(stage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}