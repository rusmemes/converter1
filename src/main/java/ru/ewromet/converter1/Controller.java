package ru.ewromet.converter1;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Controller implements Logger {

    private Stage primaryStage;
    private File selectedFile;
    private OrderParser parser;

    @FXML
    private javafx.scene.control.TextField orderPathField;

    @FXML
    private javafx.scene.control.TextField orderNumberField;

    @FXML
    private javafx.scene.control.TextField clientNameField;

    @FXML
    private TableView filesTable;
    private ObservableList<FileRow> filesTableData = Fixtures.getFilesData();

    @FXML
    private TableView<OrderRow> orderTable;
    private ObservableList<OrderRow> orderTableData = Fixtures.getOrderData();

    @FXML
    private TextArea logArea;

    @FXML
    public void initialize() {
        initializeFilesTable();
        initializeOrderTable();
        logArea.setEditable(false);
        parser = new OrderParser();
        logMessage("Initialized");
    }

    @Override
    public void logError(String line) {
        logArea.setText(logArea.getText() + "[ERROR] " + line + " [/ERROR]\n");
    }

    @Override
    public void logMessage(String line) {
        logArea.setText(logArea.getText() + "[MESSAGE] " + line + " [/MESSAGE]\n");
    }

    private void initializeFilesTable() {
        TableColumn<FileRow, String> fileNameColumn = ColumnFactory.createColumn(
                "Файл", 100, "fileName",
                TextFieldTableCell.forTableColumn(), FileRow::setFileName
        );
        filesTable.setItems(filesTableData);
        filesTable.getColumns().addAll(fileNameColumn);
    }

    private void initializeOrderTable() {
        orderTable.setEditable(true);

        TableColumn<OrderRow, String> posNumberColumn = ColumnFactory.createColumn(
                "№", 10, "posNumber",
                TextFieldTableCell.forTableColumn(), OrderRow::setPosNumber
        );

        posNumberColumn.setEditable(false);

        TableColumn<OrderRow, String> detailNameColumn = ColumnFactory.createColumn(
                "Наименование детали", 100, "detailName",
                TextFieldTableCell.forTableColumn(), OrderRow::setDetailName
        );

        TableColumn<OrderRow, String> countColumn = ColumnFactory.createColumn(
                "Количество", 50, "count",
                TextFieldTableCell.forTableColumn(), OrderRow::setCount
        );

        TableColumn<OrderRow, String> materialColumn = ColumnFactory.createColumn(
                "Материал", 50, "material",
                TextFieldTableCell.forTableColumn(), OrderRow::setMaterial
        );

        TableColumn<OrderRow, String> materialBrandColumn = ColumnFactory.createColumn(
                "Марка материала", 50, "materialBrand",
                TextFieldTableCell.forTableColumn(), OrderRow::setMaterialBrand
        );

        TableColumn<OrderRow, String> colorColumn = ColumnFactory.createColumn(
                "Окраска", 50, "color",
                TextFieldTableCell.forTableColumn(), OrderRow::setColor
        );

        TableColumn<OrderRow, String> ownerColumn = ColumnFactory.createColumn(
                "Принадлежность", 50, "owner",
                TextFieldTableCell.forTableColumn(), OrderRow::setOwner
        );

        TableColumn<OrderRow, String> bendingColumn = ColumnFactory.createColumn(
                "Гибка", 50, "bending",
                TextFieldTableCell.forTableColumn(), OrderRow::setBending
        );

        TableColumn<OrderRow, String> bendsCountColumn = ColumnFactory.createColumn(
                "Количество гибов", 50, "bendsCount",
                TextFieldTableCell.forTableColumn(), OrderRow::setBendsCount
        );

        TableColumn<OrderRow, String> commentColumn = ColumnFactory.createColumn(
                "Комментарий", 50, "comment",
                TextFieldTableCell.forTableColumn(), OrderRow::setComment
        );

        orderTable.getSelectionModel().setCellSelectionEnabled(true);

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

        orderTable.getColumns().forEach(c -> c.setOnEditCommit(event -> {
            TableColumn<OrderRow, ?> column = event.getTableColumn();
            String posNumber = event.getRowValue().getPosNumber();
            Object oldValue = event.getOldValue();
            Object newValue = event.getNewValue();
            logMessage(String.format(
                    "Изменение: колонка '%s', строка '%s', старое значение: '%s', новое значение: '%s'"
                    , column.getText()
                    , posNumber
                    , oldValue
                    , newValue
            ));
            orderTable.requestFocus();
        }));
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
            try {
                ParseResult parseResult = parser.parse(file, this);
                orderTable.setItems(parseResult.getOrderRows());
                orderPathField.setText(file.getAbsolutePath());
                selectedFile = file;
                orderNumberField.setText(file.getParentFile().getName());
                clientNameField.setText(parseResult.getClientName());
            } catch (Exception e) {
                logError(e.getMessage());
            }
        }
    }
}
