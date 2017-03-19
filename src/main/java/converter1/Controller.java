package converter1;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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

        TableColumn<OrderRow, String> detailNameColumn = TableStringColumnFactory.createColumn(
                "Наименование детали", 100, "detailName"
                , OrderRow::setDetailName
                , EMPTY_EVENT_HANDLER);

        TableColumn<OrderRow, Integer> countColumn = TableIntegerColumnFactory.createColumn(
                "Количество", 50, "count", OrderRow::setCount, EMPTY_EVENT_HANDLER
        );

        TableColumn<OrderRow, String> materialColumn = TableStringColumnFactory.createColumn(
                "Материал", 50, "material"
                , OrderRow::setMaterial, EMPTY_EVENT_HANDLER
        );

        TableColumn<OrderRow, String> materialBrandColumn = TableStringColumnFactory.createColumn(
                "Марка материала", 50, "materialBrand"
                , OrderRow::setMaterialBrand, EMPTY_EVENT_HANDLER
        );

        TableColumn<OrderRow, String> colorColumn = TableStringColumnFactory.createColumn(
                "Окраска", 50, "color"
                , OrderRow::setColor, EMPTY_EVENT_HANDLER
        );

        TableColumn<OrderRow, String> ownerColumn = TableStringColumnFactory.createColumn(
                "Принадлежность", 50, "owner"
                , OrderRow::setOwner, EMPTY_EVENT_HANDLER
        );

        TableColumn<OrderRow, String> bendingColumn = TableStringColumnFactory.createColumn(
                "Гибка", 50, "bending"
                , OrderRow::setBending, EMPTY_EVENT_HANDLER
        );

        TableColumn<OrderRow, Integer> bendsCountColumn = TableIntegerColumnFactory.createColumn(
                "Количество гибов", 50, "bendsCount"
                , OrderRow::setBendsCount, EMPTY_EVENT_HANDLER
        );

        TableColumn<OrderRow, String> commentColumn = TableStringColumnFactory.createColumn(
                "Комментарий", 50, "comment"
                , OrderRow::setComment, EMPTY_EVENT_HANDLER
        );

        orderTable.setItems(orderTableData);
        orderTable.getColumns().addAll(
                posNumberColumn,
                detailNameColumn,
                countColumn,
                materialColumn,
                materialBrandColumn,
                colorColumn,
                ownerColumn,
                bendingColumn,
                bendsCountColumn,
                commentColumn
        );
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void orderButtonAction() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Файлы с расширением '.xls' либо '.xlsx'", "*.xls", "*.xlsx"));
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
