package converter1.table;

import converter1.OrderRow;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

import java.util.function.BiConsumer;

public class TableStringColumnFactory {

    private static Callback<TableColumn<OrderRow, String>, TableCell<OrderRow, String>> createCellFactory(
            EventHandler<? super KeyEvent> cellEditingListener
    ) {
        return column -> {
            TableCell<OrderRow, String> cell = new EditableStringCell();
            cell.setOnKeyReleased(cellEditingListener);
            return cell;
        };
    }

    private TableStringColumnFactory() {
    }

    public static TableColumn<OrderRow, String> createColumn(String text, double minWidth, String property
            , BiConsumer<OrderRow, String> setter
            , EventHandler<? super KeyEvent> cellEditingListener
    ) {
        return ColumnFactory.createColumn(text, minWidth, property, createCellFactory(cellEditingListener), setter);
    }
}
