package converter1.table;

import converter1.OrderRow;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

import java.util.function.BiConsumer;

public class TableIntegerColumnFactory implements ColumnFactory {

    private static Callback<TableColumn<OrderRow, Integer>, TableCell<OrderRow, Integer>> createCellFactory(
            EventHandler<? super KeyEvent> cellEditingListener
    ) {
        return column -> {
            TableCell<OrderRow, Integer> cell = new EditableIntegerCell();
            cell.setOnKeyReleased(cellEditingListener);
            return cell;
        };
    }

    private TableIntegerColumnFactory() {
    }

    public static TableColumn<OrderRow, Integer> createColumn(String text, double minWidth, String property
            , BiConsumer<OrderRow, Integer> setter
            , EventHandler<? super KeyEvent> cellEditingListener
    ) {
        return ColumnFactory.createColumn(text, minWidth, property, createCellFactory(cellEditingListener), setter);
    }
}
