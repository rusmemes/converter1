package converter1.table;

import converter1.OrderRow;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

public class EditableStringCell extends TableCell<OrderRow, String> {

    public static final String CSS_ORIGINAL = "cell-renderer-original";
    public static final String CSS_CHANGED = "cell-renderer-changed";
    public static final String CSS_ERROR = "cell-renderer-error";
    public static final String CSS_ERROR_AND_CHANGED = "cell-renderer-error-and-changed";

    private TextField textField;

    public EditableStringCell() {}

    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            createTextField();
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        String newValue = textField.getText();
        String oldValue = getItem();
        if (!Objects.equals(oldValue, newValue)) {
            updateItem(newValue, StringUtils.isEmpty(newValue));
        } else {
            setText(oldValue);
            setGraphic(null);
        }
    }

    @Override
    public void updateItem(String item, boolean empty) {
        if (textField != null) {
            item = textField.getText();
            empty = StringUtils.isEmpty(item);
        }
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setText(null);
                setGraphic(textField);
            } else {
                if (textField != null) {
                    textField.setText(getString());
                }
                setText(getString());
                setGraphic(null);
            }
        }
    }

    private void createTextField() {
        textField = new TextField(getString());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
        textField.focusedProperty().addListener((arg0, arg1, arg2) -> {
            if (!arg2) {
                commitEdit(textField.getText());
            }
        });
    }

    private String getString() {
        return getItem() == null ? "" : getItem();
    }
}