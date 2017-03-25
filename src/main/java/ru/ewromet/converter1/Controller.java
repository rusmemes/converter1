package ru.ewromet.converter1;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.converter.IntegerStringConverter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static ru.ewromet.converter1.OrderRow.MATERIAL_LABELS;

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

    @FXML
    private HTMLEditor logArea;

    private MenuItem saveItem;

    @FXML
    private Button bindButton;

    @FXML
    public void initialize() {
        initializeMenu();
        initializeFilesTable();
        initializeOrderTable();
        parser = new OrderParser();
        hideHTMLEditorToolbars(logArea);

        bindButton.setOnAction(event -> {
            OrderRow selectedOrderRow = orderTable.getSelectionModel().getSelectedItem();
            FileRow selectedFileRow = filesTable.getSelectionModel().getSelectedItem();
            if (selectedOrderRow != null && selectedFileRow != null) {
                if (StringUtils.isEmpty(selectedFileRow.getStringPosNumber()) && StringUtils.isEmpty(selectedOrderRow.getRelativeFilePath())) {
                    selectedFileRow.setPosNumber(selectedOrderRow.getPosNumber());
                    selectedOrderRow.setRelativeFilePath(selectedFileRow.getRelativeFilePath());
                    logMessage("Файл " + selectedFileRow.getRelativeFilePath() + " связан с позицией " + selectedOrderRow.getPosNumber());
                    refreshTable(orderTable, Comparator.comparing(OrderRow::getPosNumber));
                    refreshTable(filesTable, Comparator.comparing(FileRow::getPosNumber));
                }
            }
        });
    }

    private static <T> void refreshTable(TableView<T> tableView, Comparator<T> comparator) {
        final List<T> items = tableView.getItems();
        if (items == null || items.size() == 0) {
            return;
        }

        Collections.sort(items, comparator);
        tableView.refresh();
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
        saveItem.setOnAction(event -> saveAction());

        menu.getItems().addAll(newOrderItem, saveItem);
        menuBar.getMenus().add(menu);
    }

    public static void hideHTMLEditorToolbars(final HTMLEditor editor) {
        editor.setVisible(false);
        Platform.runLater(() -> {
            Node[] nodes = editor.lookupAll(".tool-bar").toArray(new Node[0]);
            for (Node node : nodes) {
                node.setVisible(false);
                node.setManaged(false);
            }
            editor.setVisible(true);
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
        filesTable.setEditable(true);

        TableColumn<FileRow, String> filePathColumn = ColumnFactory.createColumn(
                "Файл", 100, "relativeFilePath",
                column -> new ToolTipedTextFieldTableCell<FileRow>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        TableRow<FileRow> currentRow = getTableRow();
                        final FileRow fileRow = currentRow.getItem();
                        if (fileRow != null && StringUtils.isBlank(fileRow.getStringPosNumber())) {
                            currentRow.getStyleClass().add("unbinded-table-row");
                        } else {
                            currentRow.getStyleClass().remove("unbinded-table-row");
                        }
                    }
                }, FileRow::setRelativeFilePath
        );
        filePathColumn.setEditable(false);

        TableColumn<FileRow, String> posNumberColumn = ColumnFactory.createColumn(
                "№", 30, "stringPosNumber",
                TextFieldTableCell.forTableColumn(), FileRow::setStringPosNumber
        );

        posNumberColumn.setEditable(false);
        posNumberColumn.setMaxWidth(30);
        posNumberColumn.setResizable(false);
        posNumberColumn.setStyle("-fx-alignment: BASELINE-CENTER;");

        filesTable.getSelectionModel().setCellSelectionEnabled(true);
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
                column -> new ToolTipedTextFieldTableCell<OrderRow>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        TableRow<OrderRow> currentRow = getTableRow();
                        final OrderRow orderRow = currentRow.getItem();
                        if (orderRow != null && StringUtils.isBlank(orderRow.getRelativeFilePath())) {
                            currentRow.getStyleClass().add("unbinded-table-row");
                        } else {
                            currentRow.getStyleClass().remove("unbinded-table-row");
                        }
                    }
                }, OrderRow::setDetailName
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
                ChoiceBoxTableCell.forTableColumn(MATERIAL_LABELS.keySet().toArray(new String[MATERIAL_LABELS.size()])),
                OrderRow::setMaterial
        );
        materialColumn.setStyle("-fx-alignment: BASELINE-CENTER;");

        TableColumn<OrderRow, String> materialBrandColumn = ColumnFactory.createColumn(
                "Марка материала", 50, "materialBrand",
                column -> new ToolTipedTextFieldTableCell<>(), OrderRow::setMaterialBrand
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
                column -> new ToolTipedTextFieldTableCell<>(), OrderRow::setComment
        );
        commentColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        orderTable.getSelectionModel().setCellSelectionEnabled(true);

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
                logError(e.getMessage());
            }
        }
    }

    private void saveAction() {
        try {
            final File directory = selectedFile.getParentFile();
            final File outerDirectory = directory.getParentFile();
            String orderNumber = orderNumberField.getText();
            if (StringUtils.isBlank(orderNumber)) {
                logError("Не указан номер заказа");
                return;
            }
            final String orderNumberFinal = orderNumber.trim();
            final File orderAbsTempDir = Paths.get(outerDirectory.getAbsolutePath(), orderNumberFinal + "temp" + Math.random()).toFile();
            final File orderAbsDir = Paths.get(outerDirectory.getAbsolutePath(), orderNumberFinal).toFile();
            final File sourceFilesDir = Paths.get(orderAbsTempDir.getAbsolutePath(), "Исходные данные").toFile();

            FileUtils.copyDirectory(directory, sourceFilesDir);
            logMessage("Исходные данные сохранены в " + sourceFilesDir.getAbsolutePath());

            final List<OrderRow> orderRows = orderTable.getItems();
            for (OrderRow orderRow : orderRows) {
                if (StringUtils.isBlank(orderRow.getRelativeFilePath())) {
                    logError("Для детали " + orderRow.getDetailName() + " соответствующий файл не указан");
                    continue;
                }

                final File sourceFile = Paths.get(directory.getAbsolutePath(), orderRow.getRelativeFilePath()).toFile();
                final String materialLabel = OrderRow.MATERIAL_LABELS.get(orderRow.getMaterial());
                final String dirName = materialLabel + StringUtils.SPACE + orderRow.getThickness() + "mm";
                final String destFileName = getDestFileName(orderNumberFinal, sourceFile, orderRow);
                final File destTempFile = Paths.get(orderAbsTempDir.getAbsolutePath(), dirName, destFileName).toFile();
                final File destFile = Paths.get(orderAbsDir.getAbsolutePath(), dirName, destFileName).toFile();

                logMessage("Копирование файла " + sourceFile.getAbsolutePath() + " в " + destTempFile);
                FileUtils.moveFile(sourceFile, destTempFile);
                logMessage("Файл скопирован");

                orderRow.setRelativeFilePath(destFile.getAbsolutePath());
                orderRow.setMaterial(materialLabel);
                orderRow.setDetailName(destFileName);
            }

            logMessage("Удаление старой директории " + directory.getAbsolutePath());
            FileUtils.deleteDirectory(directory);
            logMessage("Директория удалена");

            logMessage("Перенос готовых файлов из " + orderAbsTempDir + " в " + orderAbsDir);
            FileUtils.moveDirectory(orderAbsTempDir, orderAbsDir);
            logMessage("Перенос завершён");

            logMessage("Создание csv-файла в директории " + orderAbsDir);
            createCsvFile(orderAbsDir, orderNumber, orderRows);
            logMessage("csv-файл создан");

            saveItem.setDisable(true);
        } catch (Exception e) {
            e.printStackTrace();
            logError(e.getMessage());
        }
    }

    private static final String CSV_HEADER_ROW = "000 | ;110 | Название;119 | Материал;120 | Толщина;121 | Толщин.велич.;177 | Последнее плановое количество";

    private void createCsvFile(File directory, String orderNumber, List<OrderRow> orderRows) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(CSV_HEADER_ROW);
        orderRows.forEach(row -> {
            if (StringUtils.isBlank(row.getRelativeFilePath())) ;
            lines.add(createCsvLine(row));
        });
        final File csvFile = Paths.get(directory.getAbsolutePath(), orderNumber + ".csv").toFile();
        FileUtils.writeLines(csvFile, "UTF-8", lines);
    }

    private String createCsvLine(OrderRow row) {
        return String.format(
                "%s;%s;%s;%s;mm;%d"
                , row.getRelativeFilePath()
                , row.getDetailName()
                , row.getMaterial()
                , row.getThickness()
                , row.getCount()
        );
    }

    private static final String FILENAME_TEMPLATE = "Nz-Np-Q_gK_O.f";

    /**
     * Nz - номер заказа,
     * Np - номер позиции файла по спецификации,
     * Q - количество деталей по спецификации,
     * _g - ставится в том случае, если деталь гнётся,
     * K - количество гибов по спецификации, ставится также только в том случае, если деталь гнётся,
     * _O - если указана окраска в спецификации
     * .f - сохраняется формат, в котором была изначально деталь ("*.dwg" и "*.dxf")
     */
    private static String getDestFileName(String orderNumber, File sourceFile, OrderRow row) {

        final String sourceFileName = sourceFile.getName();
        final int lastCommaPos = sourceFileName.lastIndexOf(".");
        final String extension = sourceFileName.substring(lastCommaPos);

        return FILENAME_TEMPLATE
                .replace("Nz", orderNumber)
                .replace("Np", String.valueOf(row.getPosNumber()))
                .replace("Q", String.valueOf(row.getCount()))
                .replace("_g", row.getBendsCount() > 0 ? "_g" : StringUtils.EMPTY)
                .replace("K", row.getBendsCount() > 0 ? String.valueOf(row.getBendsCount()) : StringUtils.EMPTY)
                .replace("_O", StringUtils.isNotBlank(row.getColor()) ? "_O" : StringUtils.EMPTY)
                .replace(".f", extension)
                ;
    }

    public void closeApplicationAction(WindowEvent windowEvent) {
        if (!saveItem.isDisable()) {
            Dialog dialog = new Dialog();
            dialog.setHeaderText("Предупреждение");
            dialog.setContentText("Имеются несохраненные изменения. Вы действительно хотите закрыть окно программы?");

            ButtonType okButtonType = new ButtonType("Да", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType("Нет", ButtonBar.ButtonData.CANCEL_CLOSE);

            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

            Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
            okButton.managedProperty().bind(okButton.visibleProperty());
            okButton.setVisible(true);

            Node cancelButton = dialog.getDialogPane().lookupButton(cancelButtonType);
            cancelButton.managedProperty().bind(cancelButton.visibleProperty());
            cancelButton.setVisible(true);
            dialog.showAndWait();
            if (dialog.getResult() == cancelButtonType) {
                windowEvent.consume();
            }
        }
    }
}
