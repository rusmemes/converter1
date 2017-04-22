package ru.ewromet.converter1;

import java.io.File;
import java.io.IOException;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import ru.ewromet.Controller;
import ru.ewromet.FileRow;
import ru.ewromet.OrderRow;
import ru.ewromet.OrderRowsFileUtil;
import ru.ewromet.converter2.Controller2;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static ru.ewromet.FileSearchUtil.findRecursively;
import static ru.ewromet.Utils.getFileExtension;
import static ru.ewromet.OrderRow.MATERIAL_LABELS;
import static ru.ewromet.Preferences.Key.LAST_PATH;
import static ru.ewromet.Preferences.Key.RENAME_FILES;

public class Controller1 extends Controller {

    private static final String ALIGNMENT_BASELINE_CENTER = "-fx-alignment: BASELINE-CENTER;";
    private static final String ALIGNMENT_CENTER_LEFT = "-fx-alignment: CENTER-LEFT;";

    private File selectedFile;
    private OrderRowsFileUtil orderRowsFileUtil = new OrderRowsFileUtil();
    protected OrderParser parser = new OrderParser();

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

    private MenuItem saveItem;
    private CheckMenuItem renameFilesItem;
    public MenuItem continueWork;

    @FXML
    private Button bindButton;

    private static final Comparator<OrderRow> ORDER_ROW_COMPARATOR = Comparator.comparing(OrderRow::getPosNumber);
    private static final Comparator<FileRow> FILE_ROW_COMPARATOR
            = Comparator.comparing(FileRow::getPosNumber).thenComparing(Comparator.comparing(FileRow::getFilePath));

    public File getSelectedFile() {
        return selectedFile;
    }

    @FXML
    private void initialize() {
        initializeMenu();
        initializeFilesTable();
        initializeOrderTable();

        bindButton.setOnAction(event -> {
            OrderRow orderRow = orderTable.getSelectionModel().getSelectedItem();
            FileRow fileRow = filesTable.getSelectionModel().getSelectedItem();
            if (orderRow != null && fileRow != null) {
                if (StringUtils.isEmpty(orderRow.getFilePath())) {
                    if (fileRow.getPosNumber() > 0) {
                        fileRow = new FileRow(fileRow.getFilePath());
                        filesTable.getItems().add(fileRow);
                    }
                    fileRow.setPosNumber(orderRow.getPosNumber());
                    orderRow.setFilePath(fileRow.getFilePath());
                    logMessage("Файл " + fileRow.getFilePath() + " связан с позицией " + orderRow.getPosNumber());

                    refreshTable(orderTable, null);
                    refreshTable(filesTable, null);
                }
            }
        });
    }

    private static <T> void refreshTable(TableView<T> tableView, Comparator<T> comparator) {
        final List<T> items = tableView.getItems();
        if (items == null || items.size() == 0) {
            return;
        }
        if (comparator != null) {
            Collections.sort(items, comparator);
        }
        tableView.refresh();
    }

    private void initializeMenu() {
        final Menu menu = new Menu();
        menu.setText("Меню");
        final MenuItem newOrderItem = new MenuItem();
        newOrderItem.setText("Новая заявка");
        newOrderItem.setOnAction(event -> newOrderMenuItemAction());
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

        continueWork = new MenuItem();
        continueWork.setText("Продолжить расчёт");
        continueWork.setOnAction(event -> openConverter2Window());
        continueWork.setAccelerator(KeyCombination.keyCombination("F2"));

        menu.getItems().addAll(newOrderItem, saveItem, renameFilesItem, continueWork);
        menuBar.getMenus().add(menu);
    }

    private void openConverter2Window() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/converter2.fxml"));
            Parent root = loader.load();
            Controller2 controller = loader.getController();
            controller.setController1(this);
            Stage stage = new Stage();
            controller.setStage(stage);
            root.setOnKeyReleased(event -> {
                if (event.getCode() == KeyCode.R && event.isControlDown()) {
                    controller.calcButton.fire();
                }
            });
            stage.setTitle("Окно расчёта");
            stage.setScene(new Scene(root));
            stage.show();
            controller.setFocus();
        } catch (Exception e) {
            logError("Ошибка при открытии окна " + e.getMessage());
        }
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
                        if (fileRow != null && fileRow.getPosNumber() < 1) {
                            currentRow.getStyleClass().add("unbinded-table-row");
                        } else {
                            currentRow.getStyleClass().remove("unbinded-table-row");
                        }
                    }
                }, FileRow::setFilePath
        );
        filePathColumn.setEditable(false);

        TableColumn<FileRow, Integer> posNumberColumn = ColumnFactory.createColumn(
                "№", 30, "posNumber",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), FileRow::setPosNumber
        );

        posNumberColumn.setEditable(false);
        posNumberColumn.setMaxWidth(30);
        posNumberColumn.setResizable(false);
        posNumberColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

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
        posNumberColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<OrderRow, String> detailNameColumn = ColumnFactory.createColumn(
                "Наименование детали", 100, "detailName",
                column -> new TooltipTextFieldTableCell<OrderRow>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        TableRow<OrderRow> currentRow = getTableRow();
                        final OrderRow orderRow = currentRow.getItem();
                        if (orderRow != null && isBlank(orderRow.getFilePath())) {
                            currentRow.getStyleClass().add("unbinded-table-row");
                        } else {
                            currentRow.getStyleClass().remove("unbinded-table-row");
                        }
                    }
                }, OrderRow::setDetailName
        );

        detailNameColumn.setStyle(ALIGNMENT_CENTER_LEFT);

        TableColumn<OrderRow, Integer> countColumn = ColumnFactory.createColumn(
                "Кол-во", 50, "count",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), OrderRow::setCount
        );

        countColumn.setMaxWidth(50);
        countColumn.setResizable(false);
        countColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<OrderRow, String> materialColumn = ColumnFactory.createColumn(
                "Материал", 50, "material",
                ChoiceBoxTableCell.forTableColumn(MATERIAL_LABELS.keySet().toArray(new String[MATERIAL_LABELS.size()])),
                OrderRow::setMaterial
        );
        materialColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<OrderRow, String> materialBrandColumn = ColumnFactory.createColumn(
                "Марка материала", 50, "materialBrand",
                column -> new TooltipTextFieldTableCell<>(), OrderRow::setMaterialBrand
        );
        materialBrandColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<OrderRow, Double> thicknessColumn = ColumnFactory.createColumn(
                "Толщина", 70, "thickness",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), OrderRow::setThickness
        );

        thicknessColumn.setMaxWidth(70);
        thicknessColumn.setResizable(false);
        thicknessColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<OrderRow, String> colorColumn = ColumnFactory.createColumn(
                "Окраска", 50, "color",
                TextFieldTableCell.forTableColumn(), OrderRow::setColor
        );
        colorColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<OrderRow, String> ownerColumn = ColumnFactory.createColumn(
                "Принадлежность", 50, "owner",
                TextFieldTableCell.forTableColumn(), OrderRow::setOwner
        );
        ownerColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<OrderRow, Integer> bendsCountColumn = ColumnFactory.createColumn(
                "Кол-во гибов", 85, "bendsCount",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), OrderRow::setBendsCount
        );

        bendsCountColumn.setMaxWidth(85);
        bendsCountColumn.setResizable(false);
        bendsCountColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<OrderRow, String> commentColumn = ColumnFactory.createColumn(
                "Комментарий", 50, "comment",
                column -> new TooltipTextFieldTableCell<>(), OrderRow::setComment
        );
        commentColumn.setStyle(ALIGNMENT_CENTER_LEFT);

        orderTable.getSelectionModel().setCellSelectionEnabled(true);

        orderTable.getColumns().addAll(
                posNumberColumn,
                detailNameColumn,
                countColumn,
                materialColumn,
                materialBrandColumn,
                thicknessColumn,
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

    public void newOrderMenuItemAction() {
        progressBar.setProgress(0);
        logArea.getItems().clear();
        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Файлы с расширением '.xls' либо '.xlsx'", "*.xls", "*.xlsx"
                )
        );
        fileChooser.setTitle("Укажите файл с заявкой");
        File dirFromConfig = new File((String) preferences.get(LAST_PATH));
        while (!dirFromConfig.exists()) {
            dirFromConfig = dirFromConfig.getParentFile();
        }
        File dir2Open = dirFromConfig;
        fileChooser.setInitialDirectory(dir2Open);
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                final Pair<ObservableList<OrderRow>, ObservableList<FileRow>> parseResult = parser.parse(file, this);
                orderTable.setItems(parseResult.getLeft());
                filesTable.setItems(parseResult.getRight());
                refreshTable(filesTable, FILE_ROW_COMPARATOR);
                refreshTable(orderTable, ORDER_ROW_COMPARATOR);
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
            if (isBlank(orderNumber)) {
                logError("Не указан номер заказа");
                return;
            }
            final String orderNumberFinal = orderNumber.trim();
            final File orderAbsDir = Paths.get(outerDirectory.getAbsolutePath(), orderNumberFinal).toFile();
            final File sourceFilesDir = Paths.get(orderAbsDir.getAbsolutePath(), "Исходные данные").toFile();

            List<File> fileList = findRecursively(directory, file -> !file.equals(selectedFile) && !file.equals(sourceFilesDir));
            List<File> files2DeleteInFuture = new LinkedList<>();

            for (File file : fileList) {
                if (file.getName().equalsIgnoreCase("thumbs.db")) {
                    continue;
                }
                File dst = Paths.get(file.getAbsolutePath().replace(directory.getAbsolutePath(), sourceFilesDir.getAbsolutePath())).toFile();
                try {
                    FileUtils.copyFile(file, dst);
                    files2DeleteInFuture.add(file);
                } catch (Exception e) {
                    logError("Ошибка при копировании " + file.getAbsolutePath() + " в " + dst.getParentFile().getAbsolutePath() + ": " + e.getMessage());
                }
            }
            logMessage("Исходные данные сохранены в " + sourceFilesDir.getAbsolutePath());

            final List<OrderRow> orderRows = orderTable.getItems();
            for (OrderRow orderRow : orderRows) {
                if (isBlank(orderRow.getFilePath())) {
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
                                    files2DeleteInFuture.remove(old);
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

                orderRow.setDetailResultName(
                        renameFilesItem.isSelected()
                                ? destFileName.replace(getFileExtension(destFile), StringUtils.EMPTY)
                                : orderRow.getDetailName()
                );
            }

            logMessage("Удаление старых файлов");
            for (File oldFile : files2DeleteInFuture) {
                FileUtils.deleteQuietly(oldFile);
            }
            File[] emptyDirs = directory.listFiles(Controller1::isEmptyDirectory);
            if (ArrayUtils.isNotEmpty(emptyDirs)) {
                for (File emptyDir : emptyDirs) {
                    FileUtils.deleteQuietly(emptyDir);
                }
            }
            logMessage("Старые файлы удалены");

            logMessage("Создание csv-файла в директории " + orderAbsDir);
            createCsvFile(orderAbsDir, orderNumber, orderRows);
            logMessage("csv-файл создан");

            logMessage("Сохранение файла соответствий " + orderAbsDir);
            orderRowsFileUtil.saveOrderRows(orderRows, orderNumberFinal);
            logMessage("файл соответствий создан");

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
            return Arrays.stream(files).allMatch(Controller1::isEmptyDirectory);
        }
        return false;
    }

    private static final String CSV_HEADER_ROW = "000 | ;110 | Название;119 | Материал;120 | Толщина;121 | Толщин.велич.;177 | Последнее плановое количество";

    private void createCsvFile(File directory, String orderNumber, List<OrderRow> orderRows) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(CSV_HEADER_ROW);
        orderRows.forEach(row -> {
            if (isNotBlank(row.getFilePath())) {
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
                , row.getDetailResultName()
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
                    .replace("_O", isNotBlank(row.getColor()) ? "_O" : StringUtils.EMPTY)
                    .replace(".f", getFileExtension(sourceFile))
                    ;
        }

        return sourceFile.getName();
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
