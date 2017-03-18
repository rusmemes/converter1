package sample;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Controller {

    private Stage primaryStage;

    @FXML
    javafx.scene.control.TextField orderPathField;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void orderButtonAction() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Only *.xls or *.xlsx files", "*.xls", "*.xlsx"));
        File file = fileChooser.showOpenDialog(primaryStage);
        orderPathField.setText(file.getAbsolutePath());
    }

}
