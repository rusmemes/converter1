package ru.ewromet.converter1;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import static ru.ewromet.converter1.OrderRow.MATERIALS_LABELS;

public class Controller implements Logger {

    private Stage primaryStage;
    private File selectedFile;
    private OrderParser parser;

    @FXML
    private MenuBar menuBar;

    @FXML
    private javafx.scene.control.TextField orderPathField;

    @FXML
    private javafx.scene.control.TextField orderNumberField;

    @FXML
    private TableView<FileRow> filesTable;
    @FXML
    private TableView<OrderRow> orderTable;
    private ObservableList<OrderRow> orderData = Fixtures.getOrderData();

    @FXML
    private HTMLEditor logArea;

    private MenuItem saveItem;

    @FXML
    public void initialize() {
        initializeMenu();
        initializeFilesTable();
        initializeOrderTable();
        parser = new OrderParser();
        hideHTMLEditorToolbars(logArea);
        logArea.setDisable(true);
    }

    private void initializeMenu() {
        final Menu menu = new Menu();
        menu.setText("Меню");
        final MenuItem newOrderItem = new MenuItem();
        newOrderItem.setText("Новая заявка");
        newOrderItem.setOnAction(event -> orderButtonAction());
        saveItem = new MenuItem();
        saveItem.setText("Сохранить результат");
        saveItem.setDisable(true);
//        saveItem.setOnAction(event -> orderButtonAction());

        menu.getItems().addAll(newOrderItem, saveItem);
        menuBar.getMenus().add(menu);
    }

    public static void hideHTMLEditorToolbars(final HTMLEditor editor) {
        editor.setVisible(false);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Node[] nodes = editor.lookupAll(".tool-bar").toArray(new Node[0]);
                for (Node node : nodes) {
                    node.setVisible(false);
                    node.setManaged(false);
                }
                editor.setVisible(true);
            }
        });
    }


    @Override
    public void logError(String line) {
        logArea.setHtmlText(logArea.getHtmlText() + "<span style='color:red;'>" + line + "</span><br />");
    }

    @Override
    public void logMessage(String line) {
        logArea.setHtmlText(logArea.getHtmlText() + "<span style='color:blue;'>" + line + "</span><br />");
    }

    private void initializeFilesTable() {
        TableColumn<FileRow, String> filePathColumn = ColumnFactory.createColumn(
                "Файл", 100, "relativeFilePath",
                TextFieldTableCell.forTableColumn(), FileRow::setRelativeFilePath
        );

        TableColumn<FileRow, Integer> posNumberColumn = ColumnFactory.createColumn(
                "№", 30, "posNumber",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), FileRow::setPosNumber
        );

        posNumberColumn.setEditable(false);
        posNumberColumn.setMaxWidth(30);
        posNumberColumn.setResizable(false);
        posNumberColumn.setStyle("-fx-alignment: BASELINE-CENTER;");

        orderData.forEach(orderRow -> filesTable.getItems().add(orderRow.getAsFileRow()));
        filesTable.getColumns().addAll(filePathColumn, posNumberColumn);
    }

    private void initializeOrderTable() {
        orderTable.setEditable(true);

        TableColumn<OrderRow, Integer> posNumberColumn = ColumnFactory.createColumn(
                "№", 30, "posNumber",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), OrderRow::setPosNumber
        );

        posNumberColumn.setEditable(false);
        posNumberColumn.setMaxWidth(30);
        posNumberColumn.setResizable(false);
        posNumberColumn.setStyle("-fx-alignment: BASELINE-CENTER;");

        TableColumn<OrderRow, String> detailNameColumn = ColumnFactory.createColumn(
                "Наименование детали", 100, "detailName",
                TextFieldTableCell.forTableColumn(), OrderRow::setDetailName
        );
        detailNameColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<OrderRow, Integer> countColumn = ColumnFactory.createColumn(
                "Кол-во", 50, "count",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), OrderRow::setCount
        );

        countColumn.setMaxWidth(50);
        countColumn.setResizable(false);
        countColumn.setStyle("-fx-alignment: BASELINE-CENTER;");

        TableColumn<OrderRow, String> materialColumn = ColumnFactory.createColumn(
                "Материал", 50, "material",
                ChoiceBoxTableCell.forTableColumn(MATERIALS_LABELS.keySet().toArray(new String[MATERIALS_LABELS.size()])),
                OrderRow::setMaterial
        );
        materialColumn.setStyle("-fx-alignment: BASELINE-CENTER;");

        TableColumn<OrderRow, String> materialBrandColumn = ColumnFactory.createColumn(
                "Марка материала", 50, "materialBrand",
                TextFieldTableCell.forTableColumn(), OrderRow::setMaterialBrand
        );
        materialBrandColumn.setStyle("-fx-alignment: BASELINE-CENTER;");

        TableColumn<OrderRow, String> colorColumn = ColumnFactory.createColumn(
                "Окраска", 50, "color",
                TextFieldTableCell.forTableColumn(), OrderRow::setColor
        );
        colorColumn.setStyle("-fx-alignment: BASELINE-CENTER;");

        TableColumn<OrderRow, String> ownerColumn = ColumnFactory.createColumn(
                "Принадлежность", 50, "owner",
                TextFieldTableCell.forTableColumn(), OrderRow::setOwner
        );
        ownerColumn.setStyle("-fx-alignment: BASELINE-CENTER;");

        TableColumn<OrderRow, Integer> bendsCountColumn = ColumnFactory.createColumn(
                "Кол-во гибов", 85, "bendsCount",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), OrderRow::setBendsCount
        );

        bendsCountColumn.setMaxWidth(85);
        bendsCountColumn.setResizable(false);
        bendsCountColumn.setStyle("-fx-alignment: BASELINE-CENTER;");

        TableColumn<OrderRow, String> commentColumn = ColumnFactory.createColumn(
                "Комментарий", 50, "comment",
                TextFieldTableCell.forTableColumn(), OrderRow::setComment
        );
        commentColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        orderTable.getSelectionModel().setCellSelectionEnabled(true);

        orderTable.setItems(orderData);
        orderTable.getColumns().addAll(
                posNumberColumn,
                detailNameColumn,
                countColumn,
                materialColumn,
                materialBrandColumn,
                colorColumn,
                ownerColumn,
                bendsCountColumn,
                commentColumn
        );

        orderTable.getColumns().forEach(column -> {
            final EventHandler oldOnEditCommitListener = column.getOnEditCommit();
            column.setOnEditCommit(event -> {
                Object oldValue = event.getOldValue();
                Object newValue = event.getNewValue();
                oldOnEditCommitListener.handle(event);
                final int posNumber = event.getRowValue().getPosNumber();
                logMessage(String.format(
                        "Изменение: колонка '%s', строка '%d', старое значение: '%s', новое значение: '%s'"
                        , event.getTableColumn().getText()
                        , posNumber
                        , oldValue
                        , newValue
                ));
                orderTable.requestFocus();
            });
        });
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
            logArea.setHtmlText(StringUtils.EMPTY);
            try {
                final Pair<ObservableList<OrderRow>, ObservableList<FileRow>> parseResult = parser.parse(file, this);
                orderTable.setItems(parseResult.getLeft());
                filesTable.setItems(parseResult.getRight());
                orderPathField.setText(file.getAbsolutePath());
                selectedFile = file;
                orderNumberField.setText(file.getParentFile().getName());
                saveItem.setDisable(false);
            } catch (Exception e) {
                e.printStackTrace();
                logError(e.getMessage());
            }
        }
    }
}
