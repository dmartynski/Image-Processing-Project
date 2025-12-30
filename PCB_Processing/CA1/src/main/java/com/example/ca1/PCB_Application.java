package com.example.ca1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class PCB_Application extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(PCB_Application.class.getResource("PCBVIEW.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("PCB Component Analyser App!");
        stage.setScene(scene);
        stage.setWidth(1100);
        stage.setHeight(800);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}