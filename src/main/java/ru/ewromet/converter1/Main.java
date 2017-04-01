package ru.ewromet.converter1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/converter1.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
        primaryStage.setTitle("Конвертер заявки");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMaximized(true);
        primaryStage.setOnCloseRequest(controller::closeApplicationAction);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
