package ru.ewromet.converter1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/converter1.fxml"));
        Parent root = loader.load();
        Controller1 controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
        root.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.F2 && !controller.continueWork.isDisable()) {
                controller.continueWork.fire();
            }
        });
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
