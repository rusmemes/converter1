package ru.ewromet.converter1;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.converter.IntegerStringConverter;

import static ru.ewromet.converter1.FileSearchUtil.findRecursively;
import static ru.ewromet.converter1.OrderRow.MATERIAL_LABELS;
import static ru.ewromet.converter1.Preferences.Key.LAST_PATH;
import static ru.ewromet.converter1.Preferences.Key.RENAME_FILES;

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
    private ListView<Text> logArea;

    private MenuItem saveItem;
    private CheckMenuItem renameFilesItem;

    @FXML
    private Button bindButton;

    private Preferences preferences;

    private static final Comparator<OrderRow> ORDER_ROW_COMPARATOR = Comparator.comparing(OrderRow::getPosNumber);
    private static final Comparator<FileRow> FILE_ROW_COMPARATOR = Comparator.comparing(FileRow::getPosNumber);

    @FXML
    public void initialize() {
        initializePreferences();
        initializeMenu();
        initializeFilesTable();
        initializeOrderTable();
        parser = new OrderParser();

        bindButton.setOnAction(event -> {
            OrderRow orderRow = orderTable.getSelectionModel().getSelectedItem();
            FileRow fileRow = filesTable.getSelectionModel().getSelectedItem();
            if (orderRow != null && fileRow != null) {
                if (StringUtils.isEmpty(orderRow.getFilePath())) {
                    if (!StringUtils.isBlank(fileRow.getStringPosNumber())) {
                        fileRow = new FileRow(fileRow.getFilePath());
                        filesTable.getItems().add(fileRow);
                    }
                    fileRow.setPosNumber(orderRow.getPosNumber());
                    orderRow.setFilePath(fileRow.getFilePath());
                    logMessage("Файл " + fileRow.getFilePath() + " связан с позицией " + orderRow.getPosNumber());

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
        newOrderItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        saveItem = new MenuItem();
        saveItem.setText("Сохранить результат");
        saveItem.setDisable(true);
        saveItem.setOnAction(event -> saveAction());
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));

        renameFilesItem = new CheckMenuItem("Переименовывать файлы");
        renameFilesItem.setSelected(preferences.get(RENAME_FILES));
        renameFilesItem.setOnAction(event -> {
            try {
                preferences.update(RENAME_FILES, ((CheckMenuItem) event.getSource()).isSelected());
            } catch (IOException e) {
                logError("Ошибка записи настроек " + e.getMessage());
            }
        });

        menu.getItems().addAll(newOrderItem, saveItem, renameFilesItem);
        menuBar.getMenus().add(menu);
    }

    private void initializePreferences() {
        try {
            preferences = new Preferences();
        } catch (Exception e) {
            logError("Ошибка при чтении файла настроек " + e.getMessage());
        }
    }

    @Override
    public void logError(String line) {
        Text text = new Text(line);
        text.setFill(Color.RED);
        logArea.getItems().add(0, text);
    }

    @Override
    public void logMessage(String line) {
        Text text = new Text(line);
        text.setFill(Color.BLUE);
        logArea.getItems().add(0, text);
    }

    private void initializeFilesTable() {
        filesTable.setEditable(true);

        TableColumn<FileRow, String> filePathColumn = ColumnFactory.createColumn(
                "Файл", 100, "filePath",
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
                }, FileRow::setFilePath
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
                        if (orderRow != null && StringUtils.isBlank(orderRow.getFilePath())) {
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

    public void orderButtonAction() {
        progressBar.setProgress(0);
        logArea.getItems().clear();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Файлы с расширением '.xls' либо '.xlsx'", "*.xls", "*.xlsx"));
        fileChooser.setTitle("Укажите файл с заявкой");
        File dirFromConfig = new File((String) preferences.get(LAST_PATH));
        while (!dirFromConfig.exists()) {
            dirFromConfig = dirFromConfig.getParentFile();
        }
        File dirToOpen = dirFromConfig;
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
            try {
                preferences.update(LAST_PATH, file.getParent());
            } catch (IOException e) {
                logError("Ошибка записи настроек " + e.getMessage());
            }
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

            List<File> fileList = findRecursively(directory, file -> !file.equals(selectedFile) && !file.equals(sourceFilesDir));
            List<File> filesToDeleteInFuture = new LinkedList<>();

            for (File file : fileList) {
                if (file.getName().equalsIgnoreCase("thumbs.db")) {continue;}
                File dst = Paths.get(file.getAbsolutePath().replace(directory.getAbsolutePath(), sourceFilesDir.getAbsolutePath())).toFile();
                try {
                    FileUtils.copyFile(file, dst);
                    filesToDeleteInFuture.add(file);
                } catch (Exception e) {
                    logError("Ошибка при копировании " + file.getAbsolutePath() + " в " + dst.getParentFile().getAbsolutePath() + ": " + e.getMessage());
                }
            }
            logMessage("Исходные данные сохранены в " + sourceFilesDir.getAbsolutePath());

            final List<OrderRow> orderRows = orderTable.getItems();
            for (OrderRow orderRow : orderRows) {
                if (StringUtils.isBlank(orderRow.getFilePath())) {
                    logError("Для детали " + orderRow.getDetailName() + " соответствующий файл не указан");
                    continue;
                }

                final File sourceFile = Paths.get(sourceFilesDir.getAbsolutePath(), orderRow.getFilePath()).toFile();
                final String materialLabel = OrderRow.MATERIAL_LABELS.get(orderRow.getMaterial());
                final String dirName = materialLabel + StringUtils.SPACE + orderRow.getThickness() + "mm";
                final String destFileName = getDestFileName(orderNumberFinal, sourceFile, orderRow);
                final File destFile = Paths.get(orderAbsDir.getAbsolutePath(), dirName, destFileName).toFile();

                if (!destFile.exists()) {
                    if (sourceFile.exists()) {
                        logMessage("Копирование " + sourceFile.getAbsolutePath() + " в " + destFile);
                        FileUtils.copyFile(sourceFile, destFile);
                    } else {
                        logError("Файл " + sourceFile.getAbsolutePath() + " не найден, попробуем найти его на старом месте");
                        File old = Paths.get(sourceFile.getAbsolutePath().replace(sourceFilesDir.getAbsolutePath(), directory.getAbsolutePath())).toFile();
                        if (old.exists()) {
                            logMessage("Файл " + old.getAbsolutePath() + " найден, копируем его в " + destFile.getParent());
                            try {
                                FileUtils.copyFile(old, destFile);
                                logMessage("Файл " + old.getAbsolutePath() + " успешно скопирован в " + destFile.getParent());
                                logMessage("Попытка сохранить " + old.getAbsolutePath() + " в папку исходных данных");
                                File path = Paths.get(old.getAbsolutePath().replace(directory.getAbsolutePath(), sourceFilesDir.getAbsolutePath())).toFile();
                                try {
                                    FileUtils.copyFile(old, path);
                                } catch (Exception e) {
                                    filesToDeleteInFuture.remove(old);
                                    logMessage("Попытка не удалась");
                                }
                            } catch (Exception e) {
                                logError("Ошибка при копировании " + old.getAbsolutePath() + " в " + destFile.getParent() + ": " + e.getMessage());
                                throw new Exception("Проверьте, не заблокирован ли файл " + old.getAbsolutePath() + " какой-либо программой");
                            }
                        } else {
                            throw new Exception("Файл " + old.getAbsolutePath() + " не найден");
                        }
                    }
                }

                orderRow.setFilePath(destFile.getAbsolutePath());
                orderRow.setMaterial(materialLabel);
                if (renameFilesItem.isSelected()) {
                    orderRow.setDetailName(destFileName.replace(getExtension(destFile), StringUtils.EMPTY));
                }
            }

            logMessage("Удаление старых файлов");
            for (File oldFile : filesToDeleteInFuture) {
                FileUtils.deleteQuietly(oldFile);
            }
            File[] emptyDirs = directory.listFiles(Controller::isEmptyDirectory);
            if (ArrayUtils.isNotEmpty(emptyDirs)) {
                for (File emptyDir : emptyDirs) {
                    FileUtils.deleteQuietly(emptyDir);
                }
            }
            logMessage("Старые файлы удалены");

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

    private static boolean isEmptyDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (ArrayUtils.isEmpty(files)) {
                return true;
            }
            return Arrays.stream(files).allMatch(Controller::isEmptyDirectory);
        }
        return false;
    }

    private static final String CSV_HEADER_ROW = "000 | ;110 | Название;119 | Материал;120 | Толщина;121 | Толщин.велич.;177 | Последнее плановое количество";

    private void createCsvFile(File directory, String orderNumber, List<OrderRow> orderRows) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(CSV_HEADER_ROW);
        orderRows.forEach(row -> {
            if (StringUtils.isNotBlank(row.getFilePath())) {
                lines.add(createCsvLine(row));
            }
        });
        final File csvFile = Paths.get(directory.getAbsolutePath(), orderNumber + ".csv").toFile();
        FileUtils.writeLines(csvFile, "UTF-8", lines);
    }

    private String createCsvLine(OrderRow row) {
        return String.format(
                "%s;%s;%s;%s;mm;%d"
                , row.getFilePath()
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
