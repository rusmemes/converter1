package ru.ewromet.converter2;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import ru.ewromet.Controller;
import ru.ewromet.converter1.Controller1;

import static ru.ewromet.Preferences.Key.LAST_PATH;
import static ru.ewromet.Preferences.Key.SPECIFICATION_TEMPLATE_PATH;

public class Controller2 extends Controller {

    private Controller1 controller1;

    @FXML
    private Button orderFilePathButton;

    @FXML
    private TextField orderFilePathField;

    @FXML
    private Button templateButton;

    @FXML
    private TextField templateField;

    public void setController1(Controller1 controller1) {
        this.controller1 = controller1;

        File orderFile = controller1.getSelectedFile();
        if (orderFile != null) {
            orderFilePathField.setText(orderFile.getAbsolutePath());
        }
    }

    @Override
    protected void initController() {

        String templatePath = preferences.get(SPECIFICATION_TEMPLATE_PATH);
        if (StringUtils.isNotBlank(templatePath)) {
            File file = new File(templatePath);
            if (file.exists()) {
                templateField.setText("<ПРЕДЫДУЩИЙ ШАБЛОН>");
            }
        }

        orderFilePathButton.setOnAction(event -> changePathAction(orderFilePathField));
        templateButton.setOnAction(event -> changePathAction(templateField));
    }

    public void changePathAction(TextField field) {
        logArea.getItems().clear();

        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Файлы с расширением '.xls' либо '.xlsx'", "*.xls", "*.xlsx"
                )
        );
        fileChooser.setTitle("Выбор файла");
        File dirFromConfig = new File((String) preferences.get(LAST_PATH));
        while (!dirFromConfig.exists()) {
            dirFromConfig = dirFromConfig.getParentFile();
        }
        File dirToOpen = dirFromConfig;
        fileChooser.setInitialDirectory(dirToOpen);

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                field.setText(file.getAbsolutePath());
            } catch (Exception e) {
                logError(e.getMessage());
            }
            try {
                preferences.update(LAST_PATH, file.getParent());
            } catch (IOException e) {
                logError("Ошибка записи настроек " + e.getMessage());
            }
        } else {
            logMessage("Файл не был выбран");
        }
    }
}
