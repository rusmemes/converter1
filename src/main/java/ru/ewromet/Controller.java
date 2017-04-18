package ru.ewromet;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ru.ewromet.converter1.OrderParser;

public abstract class Controller implements Logger {

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

    protected OrderParser parser;

    @FXML
    protected ListView<Text> logArea;

    @FXML
    protected final void initialize() {
        parser = new OrderParser();
        initController();
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
