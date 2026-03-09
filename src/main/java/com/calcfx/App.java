package com.calcfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class App extends Application {

    private static final String FXML_PATH = "/com/calcfx/view/calculator.fxml";
    private static final String CSS_PATH = "/com/calcfx/css/style.css";
    private static final String APP_TITLE = "CalcFX — Calculadora Científica";
    private static final double MIN_WIDTH = 420;
    private static final double MIN_HEIGHT = 620;

    @Override
    public void start(Stage stage) throws IOException {
        URL fxmlUrl = getClass().getResource(FXML_PATH);
        if (fxmlUrl == null) {
            throw new IOException("FXML não encontrado: " + FXML_PATH);
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(loader.load());

        URL cssUrl = getClass().getResource(CSS_PATH);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setTitle(APP_TITLE);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
