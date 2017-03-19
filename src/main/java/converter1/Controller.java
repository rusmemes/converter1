package converter1;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import converter1.table.EditableStringCell;
import converter1.table.TableIntegerColumnFactory;
import converter1.table.TableStringColumnFactory;

import java.io.File;

public class Controller {

    private static final EventHandler<KeyEvent> EMPTY_EVENT_HANDLER = event -> {};

    private Stage primaryStage;
    private File selectedFile;

    @FXML
    private javafx.scene.control.TextField orderPathField;

    @FXML
    private javafx.scene.control.TextField orderNumberField;

    @FXML
    private javafx.scene.control.TextField clientNameField;

    @FXML
    private TableView filesTable;

    @FXML
    private TableView<OrderRow> orderTable;
    private ObservableList<OrderRow> orderTableData = Fixtures.getData();

    @FXML
    public void initialize() {
        initializeOrderTable();
    }

    private void initializeOrderTable() {
        orderTable.setEditable(true);


        TableColumn<OrderRow, Integer> posNumberColumn = TableIntegerColumnFactory.createColumn(
                "№", 10, "posNumber", OrderRow::setPosNumber, EMPTY_EVENT_HANDLER
        );

        posNumberColumn.setEditable(false);

        TableColumn<OrderRow, String> detailNameColumn = TableStringColumnFactory.createColumn("Наименование детали", 100, "detailName"
                , OrderRow::setDetailName
                , event -> {
                    ObservableList<String> styleClassList = ((TableCell) event.getSource()).getStyleClass();
                    TextField textField = (TextField) event.getTarget();

                    if (textField.getText().length() > 10) {
                        styleClassList.add(EditableStringCell.CSS_ERROR);
                    } else {
                        styleClassList.removeAll(
                                EditableStringCell.CSS_ORIGINAL, EditableStringCell.CSS_CHANGED,
                                EditableStringCell.CSS_ERROR, EditableStringCell.CSS_ERROR_AND_CHANGED
                        );
                    }
                });

        TableColumn<OrderRow, Integer> countColumn = TableIntegerColumnFactory.createColumn(
                "Количество", 50, "count", OrderRow::setCount, EMPTY_EVENT_HANDLER
        );

        orderTable.setItems(orderTableData);
        orderTable.getColumns().addAll(posNumberColumn, detailNameColumn, countColumn);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void orderButtonAction() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Только файлы с расширением '.xls' либо '.xlsx'", "*.xls", "*.xlsx"));
        fileChooser.setTitle("Укажите файл с заявкой");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            orderPathField.setText(file.getAbsolutePath());
            selectedFile = file;
            orderNumberField.setText(file.getParentFile().getName());
            clientNameField.setText("test");
        }
    }

}
