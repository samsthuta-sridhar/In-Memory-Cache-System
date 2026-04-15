package com.imcs;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InMemoryCacheApplication {

    public static void main(String[] args) {
        Application.launch(CacheSystemApp.class, args);
    }
}