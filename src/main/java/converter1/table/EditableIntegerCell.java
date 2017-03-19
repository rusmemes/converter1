package converter1.table;

import converter1.OrderRow;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

public class EditableIntegerCell extends TableCell<OrderRow, Integer> {

    public static final String CSS_ORIGINAL = "cell-renderer-original";
    public static final String CSS_CHANGED = "cell-renderer-changed";
    public static final String CSS_ERROR = "cell-renderer-error";
    public static final String CSS_ERROR_AND_CHANGED = "cell-renderer-error-and-changed";

    private TextField textField;

    public EditableIntegerCell() {}

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

        String newTextValue = textField.getText();
        if (StringUtils.isNotEmpty(newTextValue)) {
            try {
                Integer newValue = Integer.parseUnsignedInt(newTextValue);
                Integer oldValue = getItem();
                if (!Objects.equals(oldValue, newValue)) {
                    updateItem(newValue, false);
                }
            } catch (NumberFormatException ignored){}
        }
        setText(getItem().toString());
        setGraphic(null);
    }

    @Override
    public void updateItem(Integer item, boolean empty) {
        if (textField != null) {
            String textValue = textField.getText();
            if (StringUtils.isNotEmpty(textValue)) {
                try {
                    item = Integer.parseUnsignedInt(textValue);
                    empty = false;
                } catch (NumberFormatException ignored) {}
            }
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
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) {
                String value = textField.getText();
                try {
                    commitEdit(Integer.parseUnsignedInt(value));
                } catch (NumberFormatException ignored) {
                    commitEdit(0);
                }
            }
        });
    }

    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }
}