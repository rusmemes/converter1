package ru.ewromet;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import static ru.ewromet.Preferences.Key.LAST_PATH;

public abstract class Controller implements Logger {

    protected static final String ALIGNMENT_BASELINE_CENTER = "-fx-alignment: BASELINE-CENTER;";
    protected static final String ALIGNMENT_CENTER_LEFT = "-fx-alignment: CENTER-LEFT;";

    protected static Preferences preferences;

    static {
        try {
            preferences = new Preferences();
        } catch (Exception e) {
            System.out.println("Ошибка при чтении файла настроек");
            e.printStackTrace();
        }
    }

    protected Stage stage;

    @FXML
    protected ListView<Text> logArea;

    @Override
    public void logError(String line) {
        Text text = new Text(line);
        text.setFill(Color.RED);
        logArea.getItems().add(0, text);
    }

    @Override
    public void logMessage(String line) {
        Text text = new Text(line);
        text.setFill(Color.BLUE);
        logArea.getItems().add(0, text);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void chooseDirAndAccept(String title, ExtendedConsumer<File> fileConsumer) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        if (title != null) {
            directoryChooser.setTitle(title);
        }

        File dirFromConfig = new File((String) preferences.get(LAST_PATH));
        while (!dirFromConfig.exists()) {
            dirFromConfig = dirFromConfig.getParentFile();
        }

        directoryChooser.setInitialDirectory(dirFromConfig);
        File dir = directoryChooser.showDialog(stage);

        if (dir != null) {
            try {
                fileConsumer.accept(dir);
            } catch (Exception e) {
                logError(e.getMessage());
            }
            try {
                preferences.update(LAST_PATH, dir.getParent());
            } catch (IOException e) {
                logError("Ошибка записи настроек " + e.getMessage());
            }
        } else {
            logMessage("Файл не был выбран");
        }
    }

    public void chooseFileAndAccept(FileChooser.ExtensionFilter extensionFilter, String title, ExtendedConsumer<File> fileConsumer) {

        final FileChooser fileChooser = new FileChooser();

        if (extensionFilter != null) {
            fileChooser.getExtensionFilters().add(extensionFilter);
        }
        if (title != null) {
            fileChooser.setTitle(title);
        }

        File dirFromConfig = new File((String) preferences.get(LAST_PATH));
        while (!dirFromConfig.exists()) {
            dirFromConfig = dirFromConfig.getParentFile();
        }

        fileChooser.setInitialDirectory(dirFromConfig);
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                fileConsumer.accept(file);
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
