package ru.ewromet.converter2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import ru.ewromet.Controller;
import ru.ewromet.OrderRow;
import ru.ewromet.OrderRowsFileUtil;
import ru.ewromet.converter1.Controller1;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static ru.ewromet.FileUtil.getExtension;
import static ru.ewromet.Preferences.Key.LAST_PATH;
import static ru.ewromet.Preferences.Key.SPECIFICATION_TEMPLATE_PATH;

public class Controller2 extends Controller {

    private Controller1 controller1;
    private OrderRowsFileUtil orderRowsFileUtil = new OrderRowsFileUtil();

    @FXML
    private Button orderFilePathButton;

    @FXML
    private TextField orderFilePathField;

    @FXML
    private Button templateButton;

    @FXML
    private TextField templateField;

    @FXML
    public Button calcButton;

    public void setController1(Controller1 controller1) {
        this.controller1 = controller1;

        File orderFile = controller1.getSelectedFile();
        if (orderFile != null) {
            orderFilePathField.setText(orderFile.getAbsolutePath());
        }
    }

    @FXML
    private void initialize() {
        String templatePath = preferences.get(SPECIFICATION_TEMPLATE_PATH);
        if (isNotBlank(templatePath)) {
            templateField.setText(templatePath);
        }

        orderFilePathButton.setOnAction(event -> changePathAction(orderFilePathField));
        templateButton.setOnAction(event -> {
            changePathAction(templateField);
            String text = templateField.getText();
            if (StringUtils.isNotBlank(text)) {
                try {
                    preferences.update(SPECIFICATION_TEMPLATE_PATH, text);
                } catch (IOException e) {
                    logError("Не удалось сохранить путь к шаблону спецификации для будущего использования " + e.getMessage());
                }
            }
        });

        calcButton.setOnAction(event -> runCalc());
    }

    public void setFocus() {
        if (isBlank(orderFilePathField.getText())) {
            orderFilePathField.requestLayout();
        } else if (isBlank(templateField.getText())) {
            templateField.requestFocus();
        } else {
            calcButton.requestFocus();
        }
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

    private void runCalc() {
        logMessage("Начало работы...");
        logMessage("Проверка доступности заявки и шаблона спецификации...");
        String path = orderFilePathField.getText();
        if (StringUtils.isBlank(path)) {
            logError("Укажите файл заявки");
            return;
        }
        File orderFile = new File(path);
        if (!orderFile.exists() || !orderFile.isFile()) {
            logError("Файл заявки не найден");
            return;
        }
        File template;
        path = templateField.getText();
        if (isBlank(path) || !(template = new File(path)).exists() || !template.isFile()) {
            logError("Не найден или не указан файл шаблона спецификации");
            return;
        }
        logMessage("Нужные файлы найдены");

        String orderNumber = orderFile.getParentFile().getName();
        File specFile = Paths.get(orderFile.getParent(), orderNumber + getExtension(template)).toFile();
        if (!specFile.exists()) {
            try {
                FileUtils.copyFile(template, specFile);
            } catch (IOException e) {
                logError("Не удалось скопировать шаблон спецификации в " + specFile.getAbsolutePath() + " " + e.getMessage());
                return;
            }
        }

        List<OrderRow> orderRows;
        try {
            orderRows = orderRowsFileUtil.restoreOrderRows(orderNumber);
        } catch (IOException e) {
            logError("Ошибка при выгрузке информации по заявке из временного файла: " + e.getMessage());
            return;
        }
        orderRows.forEach(row -> logMessage(row.toString()));
        // TODO
    }
}
