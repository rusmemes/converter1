package converter1.table;

import javafx.scene.control.TextField;

@FunctionalInterface
public interface TextFieldCreator {

    default TextField create() {
        TextField field = new TextField();
        customLogic(field);
        return field;
    }

    void customLogic(TextField textField);
}
