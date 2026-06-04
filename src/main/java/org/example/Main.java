package org.example;
import javafx.application.Application;
import javafx.stage.Stage;
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        GameView gameView = new GameView(primaryStage);
        gameView.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
