package sample;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Controller {

    private Stage primaryStage;
    private File selectedFile;

    @FXML
    private javafx.scene.control.TextField orderPathField;

    @FXML
    private javafx.scene.control.TextField orderNumberField;

    @FXML
    private javafx.scene.control.TextField clientNameField;

    @FXML
    private TableView filesTable;

    @FXML
    private TableView orderTable;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void orderButtonAction() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Only *.xls or *.xlsx files", "*.xls", "*.xlsx"));
        fileChooser.setTitle("Укажите файл с заявкой");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            orderPathField.setText(file.getAbsolutePath());
            selectedFile = file;
            orderNumberField.setText(file.getParentFile().getName());
            clientNameField.setText("test");
        }
    }

}
