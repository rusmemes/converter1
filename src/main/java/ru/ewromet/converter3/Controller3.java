package ru.ewromet.converter3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.xml.sax.SAXException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import ru.ewromet.Controller;
import ru.ewromet.OrderRow;
import ru.ewromet.OrderRowsFileUtil;
import ru.ewromet.Utils;
import ru.ewromet.converter1.ColumnFactory;
import ru.ewromet.converter1.Controller1;
import ru.ewromet.converter1.TooltipTextFieldTableCell;
import ru.ewromet.converter2.parser.Attr;
import ru.ewromet.converter2.parser.Group;
import ru.ewromet.converter2.parser.Info;
import ru.ewromet.converter2.parser.QuotationInfo;
import ru.ewromet.converter2.parser.RadanAttributes;
import ru.ewromet.converter2.parser.RadanCompoundDocument;
import ru.ewromet.converter2.parser.SymFileParser;

import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static ru.ewromet.Preferences.Key.PRODUCE_ORDER_TEMPLATE_PATH;
import static ru.ewromet.Utils.containsIgnoreCase;
import static ru.ewromet.Utils.equalsBy;
import static ru.ewromet.Utils.getFileExtension;
import static ru.ewromet.Utils.getWorkbook;

public class Controller3 extends Controller {

    @FXML
    private ChoiceBox priceTypeChoiceBox;

    @FXML
    private ChoiceBox thinknessTypeChoiceBox;

    @FXML
    private TextField laserDiscount;

    @FXML
    private TextField thinknessDiscount;

    @FXML
    private TextField draftingTime;

    @FXML
    private TextField locksmith;

    @FXML
    private TextField poddons;

    @FXML
    private TextField boxesAndBags;

    @FXML
    private TableView<Compound> table1;

    @FXML
    private TableView<CompoundAggregation> table2;

    @FXML
    private MenuBar menuBar;

    private List<OrderRow> orderRows;
    private Integer orderNumber;
    private File[] files;

    private String orderFilePath;
    private File specFile;

    public void setOrderFilePath(String orderFilePath) {
        this.orderFilePath = orderFilePath;
    }

    public void setSpecFile(File specFile) {
        this.specFile = specFile;
    }

    public void setCompoundsPath(String compoundsDirPath) throws Exception {
        File compoundsDir = new File(compoundsDirPath);
        if (!compoundsDir.exists() || !compoundsDir.isDirectory()) {
            throw new Exception("Не найдена папка с компоновками");
        }
        files = compoundsDir.listFiles((file, name) -> {
            return name.toLowerCase().endsWith(".drg");
        });
        if (ArrayUtils.isEmpty(files)) {
            throw new Exception("В папке " + compoundsDir + " drg-файлы не найдены");
        }
        Arrays.sort(files, new WindowsExplorerFilesComparator());
    }

    public static void main(String[] args) {
        Path path = Paths.get("\\\\fs\\exchange");
        System.out.println(path);
        System.out.println(path.toFile().exists());
        System.out.println(path.toFile().getAbsolutePath());
    }

    @FXML
    private void initialize() {

        String template = preferences.get(PRODUCE_ORDER_TEMPLATE_PATH);
        if (StringUtils.isBlank(template)) {
            logError("Необходимо указать шаблон для заявки на производство в 'Меню' -> 'Указать шаблон заказа на производство'");
        } else {
            logMessage("Шаблон заказа на производство будет взят из " + template);
        }

        initializeMenu();
        initializeChoiceBoxes();
        initializeTextField();
        initializeTable1();
        initializeTable2();
    }

    private ChangeListener<String> getChangeListenerFor(TextField textField) {
        if (textField == laserDiscount || textField == thinknessDiscount) {
            return new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        if (isBlank(newValue) || newValue.equals("-") || newValue.equals("+")) {
                            return;
                        }
                        if (newValue.endsWith(".")) {
                            if (newValue.indexOf('.') != newValue.length() - 1) {
                                textField.setText(oldValue);
                            }
                            return;
                        }
                        int anInt = Integer.parseInt(newValue);
                        if (anInt < -100 || anInt > 500) {
                            textField.setText(oldValue);
                        }
                    } catch (NumberFormatException e) {
                        try {
                            double value = Double.parseDouble(newValue);
                            if (value < -100D || value > 500D) {
                                textField.setText(oldValue);
                            } else {
                                textField.setText(Double.toString(round(value, 1)));
                            }
                        } catch (NumberFormatException e1) {
                            textField.setText(oldValue);
                        }
                    }
                }
            };
        } else if (textField == draftingTime || textField == locksmith) {
            return new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        if (isBlank(newValue)) {
                            return;
                        }
                        if (newValue.endsWith(".")) {
                            if (newValue.indexOf('.') != newValue.length() - 1) {
                                textField.setText(oldValue);
                            }
                            return;
                        }
                        Integer.parseUnsignedInt(newValue);
                    } catch (NumberFormatException e) {
                        try {
                            double value = Double.parseDouble(newValue);
                            if (value < 0) {
                                textField.setText(oldValue);
                            } else {
                                textField.setText(Double.toString(round(value, 2)));
                            }
                        } catch (NumberFormatException e1) {
                            textField.setText(oldValue);
                        }
                    }
                }
            };
        } else {
            return new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        if (isBlank(newValue)) {
                            return;
                        }
                        Integer.parseUnsignedInt(newValue);
                    } catch (NumberFormatException e) {
                        textField.setText(oldValue);
                    }
                }
            };
        }
    }

    private void initializeTextField() {
        laserDiscount.textProperty().addListener(getChangeListenerFor(laserDiscount));
        thinknessDiscount.textProperty().addListener(getChangeListenerFor(thinknessDiscount));
        draftingTime.textProperty().addListener(getChangeListenerFor(draftingTime));
        locksmith.textProperty().addListener(getChangeListenerFor(locksmith));
        poddons.textProperty().addListener(getChangeListenerFor(poddons));
        boxesAndBags.textProperty().addListener(getChangeListenerFor(boxesAndBags));
    }

    private void initializeChoiceBoxes() {
        priceTypeChoiceBox.setItems(FXCollections.observableArrayList("", "розн", "мелк опт", "опт"));
        thinknessTypeChoiceBox.setItems(FXCollections.observableArrayList("", "розн", "мелк опт", "опт"));
    }

    private void initializeMenu() {
        final Menu menu = new Menu();
        menu.setText("Меню");

        MenuItem produceOrderTemplateMenuItem = new MenuItem();
        produceOrderTemplateMenuItem.setText("Указать шаблон заказа на производство");
        produceOrderTemplateMenuItem.setOnAction(event -> {
            logArea.getItems().clear();

            chooseFileAndAccept(
                    new FileChooser.ExtensionFilter(
                            "Файлы с расширением '.xls' либо '.xlsx'", "*.xls", "*.xlsx"
                    ),
                    "Выбор файла",
                    file -> {
                        preferences.update(PRODUCE_ORDER_TEMPLATE_PATH, file.getAbsolutePath());
                        logMessage("Указан шаблон заказа на производство " + file);
                    }
            );
        });
        produceOrderTemplateMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));

        MenuItem saveItem = new MenuItem();
        saveItem.setText("Сохранить расход металла и сформировать заказ на производство");
        saveItem.setOnAction(event -> save());
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));

        menu.getItems().addAll(produceOrderTemplateMenuItem, saveItem);
        menuBar.getMenus().add(menu);
    }

    private void save() {
        // сохранить расход металла
        String fileExtension = getFileExtension(specFile);
        File sourceFile = new File(specFile.getAbsolutePath().replace(fileExtension, ".tmp" + fileExtension));
        try {
            FileUtils.copyFile(specFile, sourceFile);
            specFile.delete();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при копировании " + specFile + " в " + sourceFile);
        }

        Set<Pair<String, Double>> alreadyDefinedMaterials = new HashSet<>();
        Map<Pair<String, String>, String> materials = Controller1.getMATERIALS();

        try (FileInputStream inputStream = new FileInputStream(sourceFile);
             Workbook workbook = getWorkbook(inputStream, sourceFile.getAbsolutePath());
             OutputStream out = new FileOutputStream(specFile);
        ) {
            Sheet sheet = workbook.getSheet("расчет");

            boolean hearedRowFound = false;
            int posNumberCellNum = -1;
            int metallCellNum = -1;
            int priceCellNum = -1;
            int materialCellNum = -1;
            int materialBrandCellNum = -1;
            int thinknessCellNum = -1;

            Cell laserPriceTypeCell = null;
            Cell laserDiscountCell = null;
            Cell thinknessPriceTypeCell = null;
            Cell thinknessDiscountCell = null;
            Cell draftingTimeCell = null;
            Cell locksmithCell = null;
            Cell poddonsCell = null;
            Cell boxesAndBagsCell = null;

            ROWS:
            for (int j = sheet.getFirstRowNum(); j <= sheet.getLastRowNum(); j++) {
                final Row row = sheet.getRow(j);
                if (row == null) {
                    continue;
                }
                if (!hearedRowFound) {
                    for (int k = row.getFirstCellNum(); k < row.getLastCellNum(); k++) {
                        Cell cell = row.getCell(k);
                        if (cell != null) {
                            String value;
                            try {
                                value = cell.getStringCellValue();
                                if (!hearedRowFound) {
                                    if (StringUtils.equals(value, "\u2116")) {
                                        hearedRowFound = true;
                                        posNumberCellNum = k;
                                    } else if (containsIgnoreCase(value, "Тип цены для резки")) {
                                        Row nextRow = sheet.getRow(cell.getRowIndex() + 1);
                                        laserPriceTypeCell = nextRow.getCell(cell.getColumnIndex());
                                        if (laserPriceTypeCell == null) {
                                            laserPriceTypeCell = nextRow.createCell(cell.getColumnIndex());
                                        }
                                    } else if (containsIgnoreCase(value, "Тип цены для гибки")) {
                                        Row nextRow = sheet.getRow(cell.getRowIndex() + 1);
                                        thinknessPriceTypeCell = nextRow.getCell(cell.getColumnIndex());
                                        if (thinknessPriceTypeCell == null) {
                                            thinknessPriceTypeCell = nextRow.createCell(cell.getColumnIndex());
                                        }
                                    } else if (containsIgnoreCase(value, "Скидка-") && containsIgnoreCase(value, "Наценка+")) {
                                        if (containsIgnoreCase(value, "Лазер")) {
                                            Row nextRow = sheet.getRow(cell.getRowIndex() + 1);
                                            laserDiscountCell = nextRow.getCell(cell.getColumnIndex());
                                            if (laserDiscountCell == null) {
                                                laserDiscountCell = nextRow.createCell(cell.getColumnIndex());
                                            }
                                        } else if (containsIgnoreCase(value, "Гибка")) {
                                            Row nextRow = sheet.getRow(cell.getRowIndex() + 1);
                                            thinknessDiscountCell = nextRow.getCell(cell.getColumnIndex());
                                            if (thinknessDiscountCell == null) {
                                                thinknessDiscountCell = nextRow.createCell(cell.getColumnIndex());
                                            }
                                        }
                                    } else if (containsIgnoreCase(value, "Время черчения")) {
                                        Row nextRow = sheet.getRow(cell.getRowIndex() + 1);
                                        draftingTimeCell = nextRow.getCell(cell.getColumnIndex());
                                        if (draftingTimeCell == null) {
                                            draftingTimeCell = nextRow.createCell(cell.getColumnIndex());
                                        }
                                    } else if (containsIgnoreCase(value, "слесарка")) {
                                        Row nextRow = sheet.getRow(cell.getRowIndex() + 1);
                                        locksmithCell = nextRow.getCell(cell.getColumnIndex());
                                        if (locksmithCell == null) {
                                            locksmithCell = nextRow.createCell(cell.getColumnIndex());
                                        }
                                    } else if (containsIgnoreCase(value, "Поддоны")) {
                                        Row nextRow = sheet.getRow(cell.getRowIndex() + 1);
                                        poddonsCell = nextRow.getCell(cell.getColumnIndex());
                                        if (poddonsCell == null) {
                                            poddonsCell = nextRow.createCell(cell.getColumnIndex());
                                        }
                                    } else if (containsIgnoreCase(value, "Коробки и мешки")) {
                                        Row nextRow = sheet.getRow(cell.getRowIndex() + 1);
                                        boxesAndBagsCell = nextRow.getCell(cell.getColumnIndex());
                                        if (boxesAndBagsCell == null) {
                                            boxesAndBagsCell = nextRow.createCell(cell.getColumnIndex());
                                        }
                                    }
                                } else if (containsIgnoreCase(value, "Расход металла, кв.м.")) {
                                    metallCellNum = k;
                                } else if (containsIgnoreCase(value, "Цена металла, руб/кг")) {
                                    priceCellNum = k;
                                } else if (containsIgnoreCase(value, "Материал")) {
                                    materialCellNum = k;
                                } else if (containsIgnoreCase(value, "Марка")) {
                                    materialBrandCellNum = k;
                                } else if (containsIgnoreCase(value, "Тощлина металла, мм") || containsIgnoreCase(value, "Толщина металла, мм")) {
                                    thinknessCellNum = k;
                                }
                                if (hearedRowFound
                                        && metallCellNum != -1
                                        && priceCellNum != -1
                                        && materialCellNum != -1
                                        && materialBrandCellNum != -1
                                        && thinknessCellNum != -1
                                        ) {
                                    break;
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    if (hearedRowFound) {
                        continue ROWS;
                    } else {
                        continue;
                    }
                } else {
                    if (metallCellNum == -1) {
                        throw new RuntimeException("В файле " + specFile + " на вкладке 'расчет' в шапке таблицы не найдена колонка, содержащая фразу 'Расход металла, кв.м.'");
                    }
                    if (priceCellNum == -1) {
                        throw new RuntimeException("В файле " + specFile + " на вкладке 'расчет' в шапке таблицы не найдена колонка, содержащая фразу 'Цена металла, руб/кг'");
                    }
                    if (materialCellNum == -1) {
                        throw new RuntimeException("В файле " + specFile + " на вкладке 'расчет' в шапке таблицы не найдена колонка, содержащая фразу 'Материал'");
                    }
                    if (materialBrandCellNum == -1) {
                        throw new RuntimeException("В файле " + specFile + " на вкладке 'расчет' в шапке таблицы не найдена колонка, содержащая фразу 'Марка'");
                    }
                    if (thinknessCellNum == -1) {
                        throw new RuntimeException("В файле " + specFile + " на вкладке 'расчет' в шапке таблицы не найдена колонка, содержащая фразу 'Толщина металла, мм'");
                    }
                }

                Cell cell = row.getCell(posNumberCellNum);
                if (cell == null) {
                    continue;
                } else {
                    try {
                        double numericCellValue = cell.getNumericCellValue();
                        if (numericCellValue < 1) {
                            continue;
                        }
                    } catch (Exception ignored) {
                        continue;
                    }
                }

                cell = row.getCell(materialCellNum);
                String material = null;
                if (cell != null) {
                    try {
                        material = cell.getStringCellValue();
                    } catch (Exception ignored) {
                        logError("В файле " + specFile + " на вкладке 'расчет' в строке таблицы #" + metallCellNum + " не найден вид металла");
                        continue;
                    }
                } else {
                    continue;
                }

                cell = row.getCell(materialBrandCellNum);
                String materialBrand = null;
                if (cell != null) {
                    try {
                        materialBrand = cell.getStringCellValue();
                    } catch (Exception ignored) {
                        logError("В файле " + specFile + " на вкладке 'расчет' в строке таблицы #" + metallCellNum + " не найдена марка металла");
                        continue;
                    }
                } else {
                    continue;
                }

                cell = row.getCell(thinknessCellNum);
                double thinkness = -1;
                if (cell != null) {
                    try {
                        thinkness = cell.getNumericCellValue();
                    } catch (Exception ignored) {
                        logError("В файле " + specFile + " на вкладке 'расчет' в строке таблицы #" + metallCellNum + " не найдена толщина металла");
                        continue;
                    }
                } else {
                    continue;
                }

                String foundMaterial = materials.get(Pair.of(material, materialBrand));

                if (foundMaterial == null) {
                    if (isNotBlank(material) && isNotBlank(materialBrand)) {
                        logError("Для материала " + material + " " + materialBrand + " не нашлось соответствия в таблице соответствия материалов");
                    }
                    continue;
                }

                for (CompoundAggregation aggregation : table2.getItems()) {
                    if (aggregation.getThickness() == thinkness && aggregation.getMaterial().equals(foundMaterial)) {
                        setValueToCell(row, priceCellNum, aggregation.getPrice());
                        if (alreadyDefinedMaterials.add(Pair.of(foundMaterial, thinkness))) {
                            setValueToCell(row, metallCellNum, aggregation.getTotalConsumption());
                            break;
                        }
                    }
                }
            }

            if (laserPriceTypeCell != null && priceTypeChoiceBox.getValue() != null && !priceTypeChoiceBox.getValue().equals("")) {
                setValueToCell(laserPriceTypeCell.getRow(), laserPriceTypeCell.getColumnIndex(), priceTypeChoiceBox.getValue().toString());
            }

            if (laserDiscountCell != null && isNotBlank(laserDiscount.getText())) {
                setValueToCell(laserDiscountCell.getRow(), laserDiscountCell.getColumnIndex(), laserDiscount.getText());
            }

            if (thinknessPriceTypeCell != null && thinknessTypeChoiceBox.getValue() != null && !thinknessTypeChoiceBox.getValue().equals("")) {
                setValueToCell(thinknessPriceTypeCell.getRow(), thinknessPriceTypeCell.getColumnIndex(), thinknessTypeChoiceBox.getValue().toString());
            }

            if (thinknessDiscountCell != null && isNotBlank(thinknessDiscount.getText())) {
                setValueToCell(thinknessDiscountCell.getRow(), thinknessDiscountCell.getColumnIndex(), thinknessDiscount.getText());
            }

            if (draftingTimeCell != null && isNotBlank(draftingTime.getText())) {
                setValueToCell(draftingTimeCell.getRow(), draftingTimeCell.getColumnIndex(), draftingTime.getText());
            }

            if (locksmithCell != null && isNotBlank(locksmith.getText())) {
                setValueToCell(locksmithCell.getRow(), locksmithCell.getColumnIndex(), locksmith.getText());
            }

            if (poddonsCell != null && isNotBlank(poddons.getText())) {
                setValueToCell(poddonsCell.getRow(), poddonsCell.getColumnIndex(), poddons.getText());
            }

            if (boxesAndBagsCell != null && isNotBlank(boxesAndBags.getText())) {
                setValueToCell(boxesAndBagsCell.getRow(), boxesAndBagsCell.getColumnIndex(), boxesAndBags.getText());
            }

            workbook.setForceFormulaRecalculation(true);
            workbook.write(out);
        } catch (Exception e) {
            logError("Ошибка при заполнении спецификации: " + e.getClass().getName() + ' ' + e.getMessage());
            return;
        } finally {
            if (!sourceFile.delete()) {
                sourceFile.deleteOnExit();
            }
        }

        createProduceOrder();

        logMessage("ДАННЫЕ СОХРАНЕНЫ");
    }

    private void createProduceOrder() {

        List<Compound> compounds = table1.getItems();

        if (isEmpty(compounds)) {
            return;
        }

        Iterator<Compound> compoundIterator = compounds.iterator();

        logMessage("Создание заказа на производство");

        String templatePath = preferences.get(PRODUCE_ORDER_TEMPLATE_PATH);
        File template = new File(templatePath);
        if (!template.exists()) {
            logError("Не найден шаблон заказа на производство по адресу " + template.getAbsolutePath());
            return;
        }

        List<String> colors = orderRows.stream().map(OrderRow::getColor).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        boolean bending = orderRows.stream().map(OrderRow::getBendsCount).filter(Objects::nonNull).filter(c -> c > 0).count() > 0;
        boolean cuttingReturn = orderRows.stream().map(OrderRow::getCuttingReturn).anyMatch(s -> containsIgnoreCase(s, "да"));
        boolean wasteReturn = orderRows.stream().map(OrderRow::getWasteReturn).anyMatch(s -> containsIgnoreCase(s, "да"));

        int fileNumber = 1;
        FILES_CYCLE:
        while (true) { // цикл создания файлов для заказов на производство
            String templateName = template.getName();
            String fileExtension = Utils.getFileExtension(template);
            File file = Paths.get(
                    new File(orderFilePath).getParentFile().getPath(),
                    templateName.replace(fileExtension, (fileNumber++) + fileExtension)
            )
                    .toFile();
            try (
                    FileInputStream inputStream = new FileInputStream(template);
                    Workbook workbook = getWorkbook(inputStream, template.getAbsolutePath());
                    OutputStream out = new FileOutputStream(file);
            ) {
                Sheet sheet = workbook.getSheet("Заказ на производство");
                if (sheet == null) {
                    logError("Не найдена вкладка 'Заказ на производство' в шаблоне");
                    return;
                }

                boolean hearedRowFound = false;

                int posNumberCellNum = -1;
                int compoundNameCellNum = -1;
                int countCellNum = -1;
                int metallCellNum = -1;
                int minSizeCellNum = -1;
                int sizeCellNum = -1;
                int ourMaterialCellNum = -1;
                int ownerMaterialCellNum = -1;

                Cell clientCell = null; // заказчик
                Cell orderCell = null; // заказ на производство
                Cell bendingCell = null; // гибка
                Cell coloringCell = null; // окраска
                Cell wasteReturnCell = null; // возврат отходов
                Cell cuttingReturnCell = null; // возврат высечки

                ROWS:
                for (
                        int lineNumber = sheet.getFirstRowNum(),
                        savedRowsNumber = 0 // счетчик сохраненных в шаблон компановок
                        ; lineNumber <= sheet.getLastRowNum() && savedRowsNumber < 16;
                        lineNumber++
                        ) {
                    final Row row = sheet.getRow(lineNumber);
                    if (row == null) {
                        continue;
                    }
                    if (!hearedRowFound) {
                        for (int k = row.getFirstCellNum(); k < row.getLastCellNum(); k++) {
                            Cell cell = row.getCell(k);
                            if (cell != null) {
                                String value;
                                try {
                                    value = cell.getStringCellValue();
                                    if (!hearedRowFound) {
                                        if (StringUtils.equals(value, "\u2116")) {
                                            hearedRowFound = true;
                                            posNumberCellNum = k;
                                        } else {
                                            if (containsIgnoreCase(value, "Заказ на производство")) {
                                                orderCell = row.getCell(cell.getColumnIndex() + 2);
                                                if (orderCell == null) {
                                                    orderCell = row.createCell(cell.getColumnIndex() + 2);
                                                }
                                            } else if (containsIgnoreCase(value, "Заказчик:")) {
                                                clientCell = cell;
                                            } else if (containsIgnoreCase(value, "Гибка")) {
                                                bendingCell = cell;
                                            } else if (containsIgnoreCase(value, "Окраска")) {
                                                coloringCell = cell;
                                            } else if (containsIgnoreCase(value, "Возврат отходов")) {
                                                wasteReturnCell = cell;
                                            } else if (containsIgnoreCase(value, "Высечки")) {
                                                cuttingReturnCell = cell;
                                            }
                                        }
                                    } else if (containsIgnoreCase(value, "Металл")) {
                                        if (metallCellNum == -1) {
                                            metallCellNum = k;
                                        } else {
                                            ourMaterialCellNum = k;
                                            ownerMaterialCellNum = k + 1;
                                        }
                                    } else if (containsIgnoreCase(value, "Программа")) {
                                        compoundNameCellNum = k;
                                    } else if (containsIgnoreCase(value, "Кол-во")) {
                                        countCellNum = k;
                                    } else if (containsIgnoreCase(value, "Минимальный размер заготовки")) {
                                        minSizeCellNum = k;
                                    } else if (StringUtils.contains(value, "Размер заготовки")) {
                                        sizeCellNum = k;
                                    }
                                    if (hearedRowFound
                                            && metallCellNum != -1
                                            && compoundNameCellNum != -1
                                            && countCellNum != -1
                                            && minSizeCellNum != -1
                                            && sizeCellNum != -1
                                            && ourMaterialCellNum != -1
                                            && ownerMaterialCellNum != -1
                                            ) {
                                        break;
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        }
                        if (hearedRowFound) {
                            lineNumber += 1;
                            continue ROWS;
                        } else {
                            continue;
                        }
                    } else {
                        if (metallCellNum == -1) {
                            throw new RuntimeException("В шаблоне на вкладке 'Заявка на производство' в шапке таблицы не найдена колонка, содержащая фразу 'Металл'");
                        }
                        if (compoundNameCellNum == -1) {
                            throw new RuntimeException("В шаблоне на вкладке 'Заявка на производство' в шапке таблицы не найдена колонка, содержащая фразу 'Программа'");
                        }
                        if (countCellNum == -1) {
                            throw new RuntimeException("В шаблоне на вкладке 'Заявка на производство' в шапке таблицы не найдена колонка, содержащая фразу 'Кол-во'");
                        }
                        if (minSizeCellNum == -1) {
                            throw new RuntimeException("В шаблоне на вкладке 'Заявка на производство' в шапке таблицы не найдена колонка, содержащая фразу 'Минимальный размер заготовки'");
                        }
                        if (sizeCellNum == -1) {
                            throw new RuntimeException("В шаблоне на вкладке 'Заявка на производство' в шапке таблицы не найдена колонка, содержащая фразу 'Размер заготовки'");
                        }
                    }

                    if (!compoundIterator.hasNext()) {
                        endProductionOrder(colors, bending, cuttingReturn, wasteReturn, orderNumber, orderCell, bendingCell, coloringCell, wasteReturnCell, cuttingReturnCell);
                        workbook.write(out);
                        break FILES_CYCLE;
                    }

                    Compound compound = compoundIterator.next();
                    setValueToCell(row, posNumberCellNum, savedRowsNumber + 1);
                    setValueToCell(row, compoundNameCellNum, compound.getName());
                    setValueToCell(row, countCellNum, compound.getN());
                    setValueToCell(row, minSizeCellNum, round(compound.getXmin() / 1000D, 2) + " x " + round(compound.getYmin() / 1000D, 2));
                    setValueToCell(row, sizeCellNum, round(compound.getXst() / 1000D, 2) + " x " + round(compound.getYst() / 1000D, 2));

                    ORDER_ROWS:
                    for (OrderRow orderRow : orderRows) {
                        if (Double.compare(orderRow.getThickness(), compound.getThickness()) == 0) {
                            for (Map.Entry<Pair<String, String>, String> pairStringEntry : Controller1.getMATERIALS().entrySet()) {
                                if (compound.getMaterial().equals(pairStringEntry.getValue())
                                        && Pair.of(orderRow.getOriginalMaterial(), orderRow.getMaterialBrand()).equals(pairStringEntry.getKey())
                                        ) {
                                    setValueToCell(row, metallCellNum, "#" + orderRow.getThickness() + " " + orderRow.getOriginalMaterial() + " " + orderRow.getMaterialBrand());
                                    if (containsIgnoreCase(orderRow.getOwner(), "заказчик")) {
                                        setValueToCell(row, ownerMaterialCellNum, "V");
                                    } else {
                                        setValueToCell(row, ourMaterialCellNum, "V");
                                    }

                                    break ORDER_ROWS;
                                }
                            }
                        }
                    }

                    savedRowsNumber++;
                    lineNumber++;
                }

                endProductionOrder(colors, bending, cuttingReturn, wasteReturn, orderNumber, orderCell, bendingCell, coloringCell, wasteReturnCell, cuttingReturnCell);
                workbook.write(out);
            } catch (Exception e) {
                logError("Ошибка при создании заказа на производство " + e.getMessage());
            }
        }
    }

    private void endProductionOrder(List<String> colors, boolean bending, boolean cuttingReturn, boolean wasteReturn, Integer orderNumber, Cell orderCell, Cell bendingCell, Cell coloringCell, Cell wasteReturnCell, Cell cuttingReturnCell) {

        if (orderCell != null) {
            setValueToCell(orderCell.getRow(), orderCell.getColumnIndex(), orderNumber);
        }

        if (bending && bendingCell != null) { // гибка
            setValueToCell(bendingCell.getRow(), bendingCell.getColumnIndex(), "Гибка: да");
        }

        if (cuttingReturn && cuttingReturnCell != null) { // высечка
            setValueToCell(cuttingReturnCell.getRow(), cuttingReturnCell.getColumnIndex(), "Высечки: да");
        }

        if (wasteReturn && wasteReturnCell != null) { // отходы
            setValueToCell(wasteReturnCell.getRow(), wasteReturnCell.getColumnIndex(), "Возврат отходов: да");
        }

        if (isNotEmpty(colors) && coloringCell != null) { // окраска
            if (colors.size() == 1) {
                setValueToCell(coloringCell.getRow(), coloringCell.getColumnIndex(), "Окраска: " + colors.get(0));
            } else {
                setValueToCell(coloringCell.getRow(), coloringCell.getColumnIndex(), "Окраска: да");
            }
        }
    }

    public void fillTables() throws Exception {
        fillTable1();
        fillTable2();
    }

    private void fillTable2() {
        List<CompoundAggregation> oldItems = new ArrayList(table2.getItems());
        Map<Triple<Double, String, String>, Double> oldItemsMaterialsPrices = new HashMap<>();

        if (isNotEmpty(oldItems)) {
            for (CompoundAggregation oldItem : oldItems) {
                if (oldItem.getPrice() > 0) {
                    Triple<Double, String, String> triple = oldItem.materialTriple();
                    oldItemsMaterialsPrices.computeIfAbsent(triple, (key) -> oldItem.getPrice());
                }
            }
            oldItems = null;
            table2.getItems().clear();
        }

        ObservableList<Compound> compounds = table1.getItems();
        if (isEmpty(compounds)) {
            return;
        }
        List<CompoundAggregation> compoundAggregations = new ArrayList<>();
        for (Compound compound : compounds) {
            CompoundAggregation compoundAggregation = new CompoundAggregation();

            compoundAggregation.setMaterial(compound.getMaterial());
            compoundAggregation.setMaterialBrand(compound.getMaterialBrand());
            compoundAggregation.setThickness(compound.getThickness());
            compoundAggregation.setSize(round(compound.getXr() / 1000D * compound.getYr() / 1000D) * compound.getN());
            compoundAggregation.setListsCount(compound.getN());

            String material = compound.getMaterial();
            if (isAluminium(material)) {
                compoundAggregation.setMaterialDensity(2700);
            } else if (isBrass(material)) {
                compoundAggregation.setMaterialDensity(8800);
            } else if (isCopper(material)) {
                compoundAggregation.setMaterialDensity(8900);
            } else if (isSteelOrZintec(material)) {
                compoundAggregation.setMaterialDensity(7850);
            } else {
                compoundAggregation.setMaterialDensity(7850);
            }

            compoundAggregation.setXMin_x_yMin_m(round(compound.getXmin() / 1000D) + " x " + round(compound.getYmin() / 1000D));
            compoundAggregation.setXSt_x_ySt_m(round(compound.getXst() / 1000D) + " x " + round(compound.getYst() / 1000D));

            compoundAggregations.add(compoundAggregation);
        }

        List<Integer> indexesToDelete = new ArrayList<>();
        // aggregation
        for (int i = 0; i < compoundAggregations.size(); i++) {
            CompoundAggregation first = compoundAggregations.get(i);
            for (int j = 0; j < compoundAggregations.size(); j++) {
                if (i == j || indexesToDelete.contains(i) || indexesToDelete.contains(j)) {
                    continue;
                }
                CompoundAggregation second = compoundAggregations.get(j);
                if (needToAggregate(first, second)) {
                    first.setSize(first.getSize() + second.getSize());
                    first.setListsCount(first.getListsCount() + second.getListsCount());
                    indexesToDelete.add(j);
                }
            }
        }

        Collections.sort(indexesToDelete, Comparator.reverseOrder());

        indexesToDelete.forEach(index -> compoundAggregations.remove(index.intValue()));

        for (int i = 0; i < compoundAggregations.size(); i++) {
            compoundAggregations.get(i).setPosNumber(i + 1);
        }

        ObservableList<CompoundAggregation> items = FXCollections.observableList(compoundAggregations);

        calcTotalConsumption(items);

        if (MapUtils.isNotEmpty(oldItemsMaterialsPrices)) {
            for (CompoundAggregation item : items) {
                Double price;
                if ((price = oldItemsMaterialsPrices.get(item.materialTriple())) != null) {
                    item.setPrice(price);
                    item.setTotalPrice(round(item.getWeight() * item.getPrice()));
                }
            }
        }

        table2.setItems(items);
    }

    private static double round(double value) {
        return round(value, 3);
    }

    private static double round(double value, int precision) {
        double v = Math.pow(10, precision);
        return Math.ceil(value * v) / v;
    }

    private boolean needToAggregate(CompoundAggregation first, CompoundAggregation second) {
        return first.materialTriple().equals(second.materialTriple())
                && first.getXMin_x_yMin_m().equals(second.getXMin_x_yMin_m())
                && first.getXSt_x_ySt_m().equals(second.getXSt_x_ySt_m())
                ;
    }

    private void fillTable1() throws Exception {

        List<RadanCompoundDocument> radanCompoundDocuments = Arrays.stream(files)
                .map(file -> {
                    try {
                        return SymFileParser.parse(file.getAbsolutePath());
                    } catch (ParserConfigurationException | SAXException | IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

        String projectDirName = new File(orderFilePath).getParentFile().getName();

        try {
            orderNumber = Integer.parseUnsignedInt(projectDirName);
        } catch (NumberFormatException e) {
            throw new Exception("Ошибка при попытке получить номер заказа по названию папки заказа " + projectDirName, e);
        }

        orderRows = new OrderRowsFileUtil().restoreOrderRows(orderNumber);

        for (int i = 0, filesLength = files.length; i < filesLength; i++) {
            Compound compound = new Compound();
            compound.setPosNumber(i + 1);
            compound.setName(removeExtension(files[i].getName()));

            RadanCompoundDocument radanCompoundDocument = radanCompoundDocuments.get(i);
            RadanAttributes radanAttributes = radanCompoundDocument.getRadanAttributes();

            compound.setMaterial(getAttrValue(radanAttributes, "119"));
            compound.setThickness(Double.valueOf(getAttrValue(radanAttributes, "120")));
            compound.setXst((int) Math.ceil(Double.valueOf(getAttrValue(radanAttributes, "124"))));
            compound.setYst((int) Math.ceil(Double.valueOf(getAttrValue(radanAttributes, "125"))));
            compound.setN(Integer.valueOf(getAttrValue(radanAttributes, "137")));

            QuotationInfo quotationInfo = radanCompoundDocument.getQuotationInfo();
            compound.setXmin(Math.min(compound.getXst(), 20 + (int) Math.ceil(Double.valueOf(getInfoValue(quotationInfo, "1")))));
            compound.setYmin(Math.min(compound.getYst(), 20 + (int) Math.ceil(Double.valueOf(getInfoValue(quotationInfo, "2")))));

            compound.setMaterialBrand(getBrand(orderRows, quotationInfo));

            setXrYr(compound);
            calcCompoundEditableCells(compound);

            table1.getItems().addAll(compound);
        }

        refreshTable(table1, Comparator.comparing(Compound::getPosNumber));
    }

    private void calcCompoundEditableCells(Compound compound) {
        compound.setSk(round(compound.getXr() / 1000D * compound.getYr() / 1000D));
        compound.setSo(round(compound.getN() * compound.getSk()));
    }

    private void calcTotalConsumption(List<CompoundAggregation> aggregations) {
        if (isEmpty(aggregations)) {
            return;
        }
        Map<Triple<Double, String, String>, Set<CompoundAggregation>> map = aggregations.stream().collect(Collectors.groupingBy(
                CompoundAggregation::materialTriple, Collectors.toSet()
        ));

        for (Triple<Double, String, String> triple : map.keySet()) {
            Set<CompoundAggregation> aggregationSet = map.get(triple);
            double sum = round(
                    aggregationSet.stream()
                            .mapToDouble(aggregation -> aggregation.getSize())
                            .sum()
            );
            aggregationSet.forEach(aggregation -> {
                aggregation.setTotalConsumption(sum);
                aggregation.setWeight(round(sum * (aggregation.getThickness() / 1000D) * (aggregation.getMaterialDensity())));
            });
        }
    }

    private void setXrYr(Compound compound) {
        String material = compound.getMaterial();
        double thickness = compound.getThickness();

        if (isMildSteelHkOrZintec(material)) {

            // Если Xmin > или = 80% от Xst, то Xr = Xst, иначе Xr=Xmin*1,2
            int xMin = compound.getXmin();
            int xSt = compound.getXst();
            compound.setXr(round(xMin >= xSt * 0.8 ? xSt : xMin * 1.2));

            // Если Ymin < (Yst/2), то Yr = Yst/2, иначе Yr = Yst
            int yMin = compound.getYmin();
            int ySt = compound.getYst();
            compound.setYr(round(yMin < ySt / 2 ? ySt / 2 : ySt));

        } else if (isMildSteelGk(material)) {

            // Если Xmin > или = 90% от Xst, то Xr = Xst, иначе Xr=Xmin*1,2
            int xMin = compound.getXmin();
            int xSt = compound.getXst();
            compound.setXr(round(xMin >= xSt * 0.9 ? xSt : xMin * 1.2));

            // Если Ymin < (Yst/2), то Yr = Yst/2, иначе Yr = Yst
            int yMin = compound.getYmin();
            int ySt = compound.getYst();
            compound.setYr(round(yMin < ySt / 2 ? ySt / 2 : ySt));

        } else if ((thickness > 2 && isAluminium(material)) || isStainlessSteelNoFoilNoShlif(material)) {

            // Если Xmin > или = 70% от Xst, то Xr = Xst, иначе Xr=Xmin*1,2
            int xMin = compound.getXmin();
            int xSt = compound.getXst();
            compound.setXr(round(xMin >= xSt * 0.7 ? xSt : xMin * 1.2));

            // Yr = Yst - всегда
            compound.setYr(round(compound.getYst()));

        } else if (
            // @formatter:off
                        (thickness <= 2 && isAluminium(material))
                                || (
                                thickness <= 0.8
                                        && (isStainlessSteelFoil(material) || isStainlessSteelShlif(material))
                                )
            // @formatter:on
                ) {
            // Xr = Xst - всегда
            compound.setXr(compound.getXst());

            // Yr = Yst - всегда
            compound.setYr(compound.getYst());

        } else if (thickness >= 1 && (isStainlessSteelFoil(material) || isStainlessSteelShlif(material))) {

            // Если Xmin > или = 50% от Xst, то Xr = Xst, иначе Xr=Xmin*1,2
            int xMin = compound.getXmin();
            int xSt = compound.getXst();
            compound.setXr(xMin >= xSt / 2 ? xSt : xMin * 1.2);

            // Yr = Yst - всегда
            compound.setYr(compound.getYst());

        } else if (isBrass(material) || isCopper(material)) {

            // Если Xmin > или = 80% от Xst, то Xr = Xst, иначе Xr=Xmin*1,2
            int xMin = compound.getXmin();
            int xSt = compound.getXst();
            compound.setXr(xMin >= xSt * 0.8 ? xSt : xMin * 1.2);

            // Yr = Yst - всегда
            compound.setYr(compound.getYst());
        }
    }

    private static boolean isCopper(String material) {
        return StringUtils.startsWithIgnoreCase(material, "Copper");
    }

    private static boolean isBrass(String material) {
        return StringUtils.startsWithIgnoreCase(material, "Brass");
    }

    private static boolean isStainlessSteelFoil(String material) {
        return StringUtils.startsWithIgnoreCase(material, "Stainless Steel Foil");
    }

    private static boolean isStainlessSteelShlif(String material) {
        return StringUtils.startsWithIgnoreCase(material, "Stainless Steel Shlif");
    }

    private static boolean isSteelOrZintec(String material) {
        return isMildSteelHkOrZintec(material) || isStainlessSteel(material);
    }

    private static boolean isStainlessSteel(String material) {
        return isStainlessSteelShlif(material) || isStainlessSteelFoil(material) || isStainlessSteelNoFoilNoShlif(material);
    }

    private static boolean isAluminium(String material) {
        return StringUtils.startsWithIgnoreCase(material, "Aluminium");
    }

    private static boolean isStainlessSteelNoFoilNoShlif(String material) {
        return StringUtils.startsWithIgnoreCase(material, "Stainless Steel")
                && !containsIgnoreCase(material, "foil")
                && !containsIgnoreCase(material, "shlif");
    }

    private static boolean isMildSteelHkOrZintec(String material) {
        return isZintec(material) || isMildSteelHk(material);
    }

    private static boolean isZintec(String material) {
        return StringUtils.startsWithIgnoreCase(material, "Zintec");
    }

    private static boolean isMildSteelHk(String material) {
        return StringUtils.startsWithIgnoreCase(material, "Mild Steel hk");
    }

    private static boolean isMildSteelGk(String material) {
        return StringUtils.startsWithIgnoreCase(material, "Mild Steel gk");
    }

    private static String getAttrValue(RadanAttributes radanAttributes, String attrNum) {
        return ofNullable(radanAttributes)
                .map(RadanAttributes::getGroups)
                .orElse(Collections.emptyList())
                .stream()
                .filter(containsIgnoreCase(Group::getName, "Производство"))
                .map(Group::getAttrs)
                .flatMap(List::stream)
                .filter(equalsBy(Attr::getNum, attrNum))
                .map(Attr::getValue)
                .findFirst().get();
    }

    private static String getInfoValue(QuotationInfo quotationInfo, String infoNum) {
        return ofNullable(quotationInfo)
                .map(QuotationInfo::getInfos)
                .orElse(Collections.emptyList())
                .stream()
                .filter(equalsBy(Info::getNum, infoNum))
                .map(Info::getValue)
                .findFirst().get();
    }

    private static String getBrand(List<OrderRow> orderRows, QuotationInfo quotationInfo) throws Exception {

        String name = ofNullable(quotationInfo)
                .map(QuotationInfo::getInfos)
                .orElse(Collections.emptyList())
                .stream()
                .filter(equalsBy(Info::getNum, "4"))
                .map(Info::getSymbols)
                .flatMap(List::stream)
                .findFirst()
                .get().getName();

        for (OrderRow orderRow : orderRows) {
            if (StringUtils.startsWithIgnoreCase(orderRow.getDetailResultName(), name)) {
                return orderRow.getMaterialBrand();
            }
        }
        throw new Exception("Не найдена марка материала для " + name);
    }

    private void initializeTable1() {
        table1.setEditable(true);

        TableColumn<Compound, Integer> posNumberColumn = ColumnFactory.createColumn(
                "№", 30, "posNumber",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), Compound::setPosNumber
        );

        posNumberColumn.setEditable(false);
        posNumberColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, String> nameColumn = ColumnFactory.createColumn(
                "Компоновка", 100, "name",
                column -> new TooltipTextFieldTableCell<>(), Compound::setName
        );

        nameColumn.setEditable(false);
        nameColumn.setStyle(ALIGNMENT_CENTER_LEFT);

        TableColumn<Compound, String> materialColumn = ColumnFactory.createColumn(
                "Материал", 50, "material",
                column -> new TooltipTextFieldTableCell<>(),
                Compound::setMaterial
        );

        materialColumn.setEditable(false);
        materialColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Double> thicknessColumn = ColumnFactory.createColumn(
                "t, мм", 50, "thickness",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), Compound::setThickness
        );

        thicknessColumn.setEditable(false);
        thicknessColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Integer> nColumn = ColumnFactory.createColumn(
                "n, шт", 50, "n",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), Compound::setN
        );

        nColumn.setEditable(false);
        nColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Integer> ystColumn = ColumnFactory.createColumn(
                "Yst, мм", 50, "yst",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), Compound::setYst
        );

        ystColumn.setEditable(false);
        ystColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Integer> xstColumn = ColumnFactory.createColumn(
                "Xst, мм", 50, "xst",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), Compound::setXst
        );

        xstColumn.setEditable(false);
        xstColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Integer> xMinColumn = ColumnFactory.createColumn(
                "Xmin, мм", 50, "xmin",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()),
                (Compound compound, Integer value) -> {
                    compound.setXmin(value);
                    recalcXrYr(compound);
                    refreshTable(table1, null);
                    fillTable2();
                }
        );

        xMinColumn.setEditable(true);
        xMinColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Integer> yMinColumn = ColumnFactory.createColumn(
                "Ymin, мм", 50, "ymin",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()),
                (Compound compound, Integer value) -> {
                    compound.setYmin(value);
                    recalcXrYr(compound);
                    refreshTable(table1, null);
                    fillTable2();
                }
        );

        yMinColumn.setEditable(true);
        yMinColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Double> yrColumn = ColumnFactory.createColumn(
                "Yr, мм", 50, "yr",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()),
                (Compound compound, Double value) -> {
                    compound.setYr(value);
                    calcCompoundEditableCells(compound);
                    refreshTable(table1, null);
                    fillTable2();
                }
        );

        yrColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Double> xrColumn = ColumnFactory.createColumn(
                "Xr, мм", 50, "xr",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()),
                (Compound compound, Double value) -> {
                    compound.setXr(value);
                    calcCompoundEditableCells(compound);
                    refreshTable(table1, null);
                    fillTable2();
                }
        );

        xrColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Boolean> fullListColumn = ColumnFactory.createColumn(
                "Весь лист", 50, "fullList",
                new Callback<TableColumn<Compound, Boolean>, TableCell<Compound, Boolean>>() {
                    @Override
                    public TableCell<Compound, Boolean> call(TableColumn<Compound, Boolean> param) {
                        return new CheckBoxTableCell<>(index -> {
                            BooleanProperty active = new SimpleBooleanProperty(table1.getItems().get(index).isFullList());
                            active.addListener((obs, wasActive, isNowActive) -> {
                                Compound compound = table1.getItems().get(index);
                                compound.setFullList(isNowActive);
                                recalcXrYr(compound);
                                refreshTable(table1, null);
                                fillTable2();
                            });
                            return active;
                        });
                    }
                },
                (Compound compound, Boolean value) -> {
                }
        );

        fullListColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Double> skColumn = ColumnFactory.createColumn(
                "Sk, кв. м.", 50, "sk",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), Compound::setSk
        );

        skColumn.setEditable(false);
        skColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Double> soColumn = ColumnFactory.createColumn(
                "So, кв. м.", 50, "so",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), Compound::setSo
        );

        soColumn.setEditable(false);
        soColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        table1.getColumns().addAll(
                posNumberColumn,
                nameColumn,
                materialColumn,
                thicknessColumn,
                nColumn,
                ystColumn,
                xstColumn,
                yMinColumn,
                xMinColumn,
                yrColumn,
                xrColumn,
                fullListColumn,
                skColumn,
                soColumn
        );

        table1.getColumns().forEach(column -> {
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
                table1.requestFocus();
            });
        });
    }

    private void recalcXrYr(Compound compound) {
        if (compound.isFullList()) {
            compound.setYr(compound.getYst());
            compound.setXr(compound.getXst());
        } else {
            setXrYr(compound);
        }
        calcCompoundEditableCells(compound);
    }

    private void initializeTable2() {
        table2.setEditable(true);

        TableColumn<CompoundAggregation, Integer> posNumberColumn = ColumnFactory.createColumn(
                "№", 30, "posNumber",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), CompoundAggregation::setPosNumber
        );

        posNumberColumn.setEditable(false);
        posNumberColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<CompoundAggregation, String> materialColumn = ColumnFactory.createColumn(
                "Материал", 50, "material",
                column -> new TooltipTextFieldTableCell<>(),
                CompoundAggregation::setMaterial
        );

        materialColumn.setEditable(false);
        materialColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<CompoundAggregation, String> materialBrandColumn = ColumnFactory.createColumn(
                "Марка материала", 50, "materialBrand",
                column -> new TooltipTextFieldTableCell<>(),
                CompoundAggregation::setMaterialBrand
        );

        materialBrandColumn.setEditable(false);
        materialBrandColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<CompoundAggregation, Double> thicknessColumn = ColumnFactory.createColumn(
                "Толщина, мм", 50, "thickness",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), CompoundAggregation::setThickness
        );

        thicknessColumn.setEditable(false);
        thicknessColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<CompoundAggregation, String> xMin_x_yMin_mColumn = ColumnFactory.createColumn(
                "Габариты листа, Xmin x Ymin, м", 150, "xMin_x_yMin_m",
                column -> new TooltipTextFieldTableCell<>(), CompoundAggregation::setXMin_x_yMin_m
        );

        xMin_x_yMin_mColumn.setEditable(false);
        xMin_x_yMin_mColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<CompoundAggregation, String> xSt_x_ySt_mColumn = ColumnFactory.createColumn(
                "Габариты листа, Xst x Yst, м", 150, "xSt_x_ySt_m",
                column -> new TooltipTextFieldTableCell<>(), CompoundAggregation::setXSt_x_ySt_m
        );

        xSt_x_ySt_mColumn.setEditable(false);
        xSt_x_ySt_mColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<CompoundAggregation, Integer> countColumn = ColumnFactory.createColumn(
                "Количество листов, шт", 50, "listsCount",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), CompoundAggregation::setListsCount
        );

        countColumn.setEditable(false);
        countColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<CompoundAggregation, Double> totalConsumptionColumn = ColumnFactory.createColumn(
                "Общий расход металла, кв.м.", 70, "totalConsumption",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), CompoundAggregation::setTotalConsumption
        );

        totalConsumptionColumn.setEditable(false);
        totalConsumptionColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<CompoundAggregation, Double> materialDensityColumn = ColumnFactory.createColumn(
                "Плотность материала, кг/м3", 50, "materialDensity",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()),
                (CompoundAggregation aggreration, Double value) -> {
                    aggreration.setMaterialDensity(value);
                    double totalConsumption = aggreration.getTotalConsumption();
                    double thickness = aggreration.getThickness() / 1000D;
                    double materialDensity = aggreration.getMaterialDensity();
                    aggreration.setWeight(round(totalConsumption * thickness * materialDensity));
                    aggreration.setTotalPrice(round(aggreration.getWeight() * aggreration.getPrice()));
                    refreshTable(table2, null);
                }
        );

        materialDensityColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<CompoundAggregation, Double> weightColumn = ColumnFactory.createColumn(
                "Масса материала, кг", 50, "weight",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), CompoundAggregation::setWeight
        );

        weightColumn.setEditable(false);
        weightColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<CompoundAggregation, Double> priceColumn = ColumnFactory.createColumn(
                "Цена за 1 кг, руб.", 50, "price",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()),
                (CompoundAggregation aggr, Double value) -> {
                    table2.getItems().stream()
                            .filter(aggregation -> aggregation.materialTriple().equals(aggr.materialTriple()))
                            .forEach(aggregation -> {
                                aggregation.setPrice(value);
                                aggregation.setTotalPrice(round(aggregation.getWeight() * value));
                            });
                    refreshTable(table2, null);
                }
        );

        priceColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<CompoundAggregation, Double> totalPriceColumn = ColumnFactory.createColumn(
                "Общая стоимость, руб.", 50, "totalPrice",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), CompoundAggregation::setTotalPrice
        );

        totalPriceColumn.setEditable(false);
        totalPriceColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        table2.getColumns().addAll(
                posNumberColumn,
                materialColumn,
                materialBrandColumn,
                thicknessColumn,
                xMin_x_yMin_mColumn,
                xSt_x_ySt_mColumn,
                countColumn,
                totalConsumptionColumn,
                materialDensityColumn,
                weightColumn,
                priceColumn,
                totalPriceColumn
        );

        table2.getColumns().forEach(column -> {
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
                table2.requestFocus();
            });
        });
    }
}
