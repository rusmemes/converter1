package order;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.function.BiConsumer;

public interface ColumnFactory {

    static <S, T> TableColumn<S, T> createColumn(String text, double minWidth, String property
            , Callback<TableColumn<S, T>, TableCell<S, T>> cellFactory, BiConsumer<S, T> setter
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
