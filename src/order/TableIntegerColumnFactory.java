package order;

import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

import java.util.function.BiConsumer;

public class TableIntegerColumnFactory {

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
        return createColumnInternal(text, minWidth, property, createCellFactory(cellEditingListener), setter);
    }

    private static <S, T> TableColumn<S, T> createColumnInternal(
            String text, double minWidth, String property
            , Callback<TableColumn<S, T>, TableCell<S, T>> cellFactory
            , BiConsumer<S, T> setter
    ) {
        TableColumn<S, T> column = new TableColumn<>(text);
        column.setMinWidth(minWidth);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setCellFactory(cellFactory);
        column.setOnEditCommit(
                cellEditEvent -> setter.accept(cellEditEvent.getRowValue(), cellEditEvent.getNewValue())
        );
        return column;
    }
}
