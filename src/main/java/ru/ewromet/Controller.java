package ru.ewromet;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public abstract class Controller implements Logger {

    protected Stage stage;
    protected Preferences preferences;
    protected OrderParser parser;

    @FXML
    protected ListView<Text> logArea;

    @FXML
    protected final void initialize() {
        initializePreferences();
        parser = new OrderParser();
        initController();
    }

    private void initializePreferences() {
        try {
            preferences = new Preferences();
        } catch (Exception e) {
            logError("Ошибка при чтении файла настроек " + e.getMessage());
        }
    }

    protected abstract void initController();

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
}
