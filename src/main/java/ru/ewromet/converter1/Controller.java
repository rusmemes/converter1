package ru.ewromet.converter1;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
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

import static java.util.Arrays.asList;
import static ru.ewromet.converter1.OrderRow.MATERIAL_LABELS;

public class Controller implements Logger {

    private Stage primaryStage;
    private File selectedFile;
    private OrderParser parser;

    @FXML
    private MenuBar menuBar;

    @FXML
    private ProgressBar progressBar;

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
    private CheckMenuItem renameFilesItem;

    @FXML
    private Button bindButton;

    private List<String> options = asList(System.getProperty("user.home"), "true");

    private static final Comparator<OrderRow> ORDER_ROW_COMPARATOR = Comparator.comparing(OrderRow::getPosNumber);
    private static final Comparator<FileRow> FILE_ROW_COMPARATOR = Comparator.comparing(FileRow::getPosNumber);

    @FXML
    public void initialize() {
        initializeOptionsFromFile();
        initializeMenu();
        initializeFilesTable();
        initializeOrderTable();
        parser = new OrderParser();
        hideHTMLEditorToolbars(logArea);

        bindButton.setOnAction(event -> {
            OrderRow orderRow = orderTable.getSelectionModel().getSelectedItem();
            FileRow fileRow = filesTable.getSelectionModel().getSelectedItem();
            if (orderRow != null && fileRow != null) {
                if (StringUtils.isEmpty(orderRow.getRelativeFilePath())) {
                    if (!StringUtils.isBlank(fileRow.getStringPosNumber())) {
                        fileRow = new FileRow(fileRow.getRelativeFilePath());
                        filesTable.getItems().add(fileRow);
                    }
                    fileRow.setPosNumber(orderRow.getPosNumber());
                    orderRow.setRelativeFilePath(fileRow.getRelativeFilePath());
                    logMessage("Файл " + fileRow.getRelativeFilePath() + " связан с позицией " + orderRow.getPosNumber());

                    refreshTable(orderTable, ORDER_ROW_COMPARATOR);
                    refreshTable(filesTable, FILE_ROW_COMPARATOR);
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

        renameFilesItem = new CheckMenuItem("Переименовывать файлы");
        renameFilesItem.setSelected(Boolean.valueOf(options.get(1)));
        renameFilesItem.setOnAction(event -> {
            updateOption(1, String.valueOf(((CheckMenuItem)event.getSource()).isSelected()));
        });

        menu.getItems().addAll(newOrderItem, saveItem, renameFilesItem);
        menuBar.getMenus().add(menu);
    }

    private void initializeOptionsFromFile() {
        final File converter1File = Paths.get(new File(System.getProperty("user.home")).getAbsolutePath(), "converter1.txt").toFile();
        if (converter1File.exists()) {
            try {
                final List<String> list = Files.readAllLines(converter1File.toPath(), Charset.forName("UTF-8"));
                if (CollectionUtils.isNotEmpty(list)) {
                    final String path = list.get(0);
                    if (StringUtils.isNotBlank(path)) {
                        options.set(0, path);
                    }
                    if (list.size() > 1) {
                        final String renameFiles = list.get(1);
                        options.set(1, String.valueOf(Boolean.valueOf(renameFiles)));
                    }
                }
            } catch (Exception e) {
                logError("Ошибка при чтении файла настроек " + e.getMessage());
            }
        }
    }

    private void updateOption(int pos, String value) {
        if (options.size() < pos) {
            for (int i = options.size(); i < pos + 1; i++) {
                options.add(StringUtils.EMPTY);
            }
        }
        options.set(pos, value);

        try {
            FileUtils.writeLines(Paths.get(new File(System.getProperty("user.home")).getAbsolutePath(), "converter1.txt").toFile(), "UTF-8", options);
        } catch (Exception e) {
            logError("Ошибка при записи настроек " + e.getMessage());
        }
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
        logArea.setHtmlText("<span style='color:red;font-family: monospace;'>" + line + "</span><br />" + logArea.getHtmlText());
    }

    @Override
    public void logMessage(String line) {
        logArea.setHtmlText("<span style='color:blue;font-family: monospace;'>" + line + "</span><br />" + logArea.getHtmlText());
    }

    private void initializeFilesTable() {
        filesTable.setEditable(true);

        TableColumn<FileRow, String> filePathColumn = ColumnFactory.createColumn(
                "Файл", 100, "relativeFilePath",
                column -> new TooltipTextFieldTableCell<FileRow>() {
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
                column -> new TooltipTextFieldTableCell<OrderRow>() {
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
                column -> new TooltipTextFieldTableCell<>(), OrderRow::setMaterialBrand
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
                column -> new TooltipTextFieldTableCell<>(), OrderRow::setComment
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

    /**
     * 2. В момент нажатия кнопки Сохранить, хотелось бы, чтобы что-то в программе происходило,
     * а именно в окне, где отображается статус обработки заявки, выводилось бы сообщение,
     * что заказ сохранён или что-то такое, потому что я сначала ничего не понял, пока не посмотрел в папку,
     * хотел даже повторно нажать "Сохранить".
     */

    public void orderButtonAction() {
        progressBar.setProgress(0);
        logArea.setHtmlText(StringUtils.EMPTY);
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Файлы с расширением '.xls' либо '.xlsx'", "*.xls", "*.xlsx"));
        fileChooser.setTitle("Укажите файл с заявкой");
        final File homeUserDir = new File(System.getProperty("user.home"));
        File dirToOpen = homeUserDir;
        File dirFromCOnfig = new File(options.get(0));
        while (!dirFromCOnfig.exists()) {
            dirFromCOnfig = dirFromCOnfig.getParentFile();
        }
        dirToOpen = dirFromCOnfig;
        fileChooser.setInitialDirectory(dirToOpen);
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                final Pair<ObservableList<OrderRow>, ObservableList<FileRow>> parseResult = parser.parse(file, this);
                orderTable.setItems(parseResult.getLeft());
                filesTable.setItems(parseResult.getRight());
                orderPathField.setText(file.getAbsolutePath());
                selectedFile = file;
                orderNumberField.setText(file.getParentFile().getName());
                saveItem.setDisable(false);
                progressBar.setProgress(0.5);
            } catch (Exception e) {
                logError(e.getMessage());
            }
            updateOption(0, file.getParent());
        } else {
            logMessage("Файл не был выбран");
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
            final File orderAbsDir = Paths.get(outerDirectory.getAbsolutePath(), orderNumberFinal).toFile();
            final File sourceFilesDir = Paths.get(orderAbsDir.getAbsolutePath(), "Исходные данные").toFile();

            final File[] files = directory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return !file.equals(selectedFile) && !file.equals(sourceFilesDir);
                }
            });
            for (File file : files) {
                FileUtils.moveToDirectory(file, sourceFilesDir, true);
            }
            logMessage("Исходные данные сохранены в " + sourceFilesDir.getAbsolutePath());

            final List<OrderRow> orderRows = orderTable.getItems();
            for (OrderRow orderRow : orderRows) {
                if (StringUtils.isBlank(orderRow.getRelativeFilePath())) {
                    logError("Для детали " + orderRow.getDetailName() + " соответствующий файл не указан");
                    continue;
                }

                final File sourceFile = Paths.get(sourceFilesDir.getAbsolutePath(), orderRow.getRelativeFilePath()).toFile();
                final String materialLabel = OrderRow.MATERIAL_LABELS.get(orderRow.getMaterial());
                final String dirName = materialLabel + StringUtils.SPACE + orderRow.getThickness() + "mm";
                final String destFileName = getDestFileName(orderNumberFinal, sourceFile, orderRow);
                final File destFile = Paths.get(orderAbsDir.getAbsolutePath(), dirName, destFileName).toFile();

                if (!destFile.exists()) {
                    logMessage("Копирование " + sourceFile.getAbsolutePath() + " в " + destFile);
                    FileUtils.copyFile(sourceFile, destFile);
                }

                orderRow.setRelativeFilePath(destFile.getAbsolutePath());
                orderRow.setMaterial(materialLabel);
                if (renameFilesItem.isSelected()) {
                    orderRow.setDetailName(destFileName.replace(getExtension(destFile), StringUtils.EMPTY));
                }
            }

            logMessage("Создание csv-файла в директории " + orderAbsDir);
            createCsvFile(orderAbsDir, orderNumber, orderRows);
            logMessage("csv-файл создан");

            logMessage("ДАННЫЕ СОХРАНЕНЫ");
            saveItem.setDisable(true);
            progressBar.setProgress(1);
        } catch (Exception e) {
            logError(e.getMessage());
        }
    }

    private static final String CSV_HEADER_ROW = "000 | ;110 | Название;119 | Материал;120 | Толщина;121 | Толщин.велич.;177 | Последнее плановое количество";

    private void createCsvFile(File directory, String orderNumber, List<OrderRow> orderRows) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(CSV_HEADER_ROW);
        orderRows.forEach(row -> {
            if (StringUtils.isBlank(row.getRelativeFilePath())) {
                ;
            }
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
    private String getDestFileName(String orderNumber, File sourceFile, OrderRow row) {

        if (renameFilesItem.isSelected()) {
            return FILENAME_TEMPLATE
                    .replace("Nz", orderNumber)
                    .replace("Np", String.valueOf(row.getPosNumber()))
                    .replace("Q", String.valueOf(row.getCount()))
                    .replace("_g", row.getBendsCount() > 0 ? "_g" : StringUtils.EMPTY)
                    .replace("K", row.getBendsCount() > 0 ? String.valueOf(row.getBendsCount()) : StringUtils.EMPTY)
                    .replace("_O", StringUtils.isNotBlank(row.getColor()) ? "_O" : StringUtils.EMPTY)
                    .replace(".f", getExtension(sourceFile))
                    ;
        }

        return sourceFile.getName();
    }

    private String getExtension(File file) {
        final String sourceFileName = file.getName();
        final int lastCommaPos = sourceFileName.lastIndexOf(".");
        return sourceFileName.substring(lastCommaPos);
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
