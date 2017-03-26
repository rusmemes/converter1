package ru.ewromet.converter1;

import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;

public class TooltipTextFieldTableCell<S> extends TextFieldTableCell<S, String> {

    private Tooltip tooltip = new Tooltip();

    public TooltipTextFieldTableCell() {
        super(new DefaultStringConverter());
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null) {
            setTooltip(null);
        } else {
            tooltip.setText(item);
            setTooltip(tooltip);
        }
    }
}
