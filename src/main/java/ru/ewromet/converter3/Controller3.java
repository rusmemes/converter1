package ru.ewromet.converter3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.xml.sax.SAXException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import ru.ewromet.Controller;
import ru.ewromet.OrderRow;
import ru.ewromet.OrderRowsFileUtil;
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
import static org.apache.commons.io.FilenameUtils.removeExtension;
import static ru.ewromet.Preferences.Key.PRODUCE_ORDER_TEMPLATE_PATH;
import static ru.ewromet.Utils.containsIgnoreCase;
import static ru.ewromet.Utils.equalsBy;
import static ru.ewromet.Utils.getFileExtension;
import static ru.ewromet.Utils.getWorkbook;

public class Controller3 extends Controller {

    @FXML
    private TableView<Compound> table1;

    @FXML
    private TableView<CompoundAggregation> table2;

    @FXML
    private MenuBar menuBar;

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
    }

    @FXML
    private void initialize() {

        String template = preferences.get(PRODUCE_ORDER_TEMPLATE_PATH);
        if (StringUtils.isBlank(template)) {
            //            logError("Необходимо указать шаблон для заявки на производство в 'Меню' -> 'Указать шаблон заказа на производство'");
            // TODO
        } else {
            logMessage("Шаблон заказа на производство будет взят из " + template);
        }

        initializeMenu();
        initializeTable1();
        initializeTable2();
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

        Set<String> alreadyDefinedMaterials = new HashSet<>();

        try (FileInputStream inputStream = new FileInputStream(sourceFile);
             Workbook workbook = getWorkbook(inputStream, sourceFile.getAbsolutePath());
             OutputStream out = new FileOutputStream(specFile);
        ) {
            Sheet sheet = workbook.getSheet("Спецификация");

            boolean hearedRowFound = false;
            int metallCellNum = -1;
            int priceCellNum = -1;
            int materialCellNum = -1;
            int materialBrandCellNum = -1;
            int thinknessCellNum = -1;

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
                                    }
                                } else if (StringUtils.containsIgnoreCase(value, "Расход металла, кг")) {
                                    metallCellNum = k;
                                } else if (StringUtils.containsIgnoreCase(value, "Цена металла, руб/кг")) {
                                    priceCellNum = k;
                                } else if (StringUtils.containsIgnoreCase(value, "Вид металла")) {
                                    materialCellNum = k;
                                } else if (StringUtils.containsIgnoreCase(value, "Марка металла")) {
                                    materialBrandCellNum = k;
                                } else if (StringUtils.containsIgnoreCase(value, "Толщина металла") || StringUtils.containsIgnoreCase(value, "Тощлина металла")) {
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
                        throw new RuntimeException("В файле " + specFile + " на вкладке 'Спецификация' в шапке таблицы не найдена колонка, содержащая фразу 'Расход металла, кг'");
                    }
                    if (priceCellNum == -1) {
                        throw new RuntimeException("В файле " + specFile + " на вкладке 'Спецификация' в шапке таблицы не найдена колонка, содержащая фразу 'Цена металла, руб/кг'");
                    }
                    if (materialCellNum == -1) {
                        throw new RuntimeException("В файле " + specFile + " на вкладке 'Спецификация' в шапке таблицы не найдена колонка, содержащая фразу 'Вид металла'");
                    }
                    if (materialBrandCellNum == -1) {
                        throw new RuntimeException("В файле " + specFile + " на вкладке 'Спецификация' в шапке таблицы не найдена колонка, содержащая фразу 'Марка металлa'");
                    }
                    if (thinknessCellNum == -1) {
                        throw new RuntimeException("В файле " + specFile + " на вкладке 'Спецификация' в шапке таблицы не найдена колонка, содержащая фразу 'Толщина металла'");
                    }
                }

                Cell cell;

                cell = row.getCell(row.getFirstCellNum());
                if (cell == null) {
                    break;
                } else {
                    try {
                        if (cell.getNumericCellValue() == 0.0) {
                            break;
                        }
                    } catch (Exception e) {
                        break;
                    }
                }

                cell = row.getCell(materialCellNum);
                String material = null;
                if (cell != null) {
                    try {
                        material = cell.getStringCellValue();
                    } catch (Exception ignored) {
                        logError("В файле " + specFile + " на вкладке 'Спецификация' в строке таблицы #" + metallCellNum + " не найден вид металла");
                        continue;
                    }
                }

                cell = row.getCell(materialBrandCellNum);
                String materialBrand = null;
                if (cell != null) {
                    try {
                        materialBrand = cell.getStringCellValue();
                    } catch (Exception ignored) {
                        logError("В файле " + specFile + " на вкладке 'Спецификация' в строке таблицы #" + metallCellNum + " не найдена марка металла");
                        continue;
                    }
                }

                cell = row.getCell(thinknessCellNum);
                double thinkness = -1;
                if (cell != null) {
                    try {
                        thinkness = cell.getNumericCellValue();
                    } catch (Exception ignored) {
                        logError("В файле " + specFile + " на вкладке 'Спецификация' в строке таблицы #" + metallCellNum + " не найдена толщина металла");
                        continue;
                    }
                }

                Map<Pair<String, String>, String> materials = Controller1.getMATERIALS();
                String foundMaterial = materials.get(Pair.of(material, materialBrand));

                if (foundMaterial == null) {
                    continue;
                }

                for (CompoundAggregation aggregation : table2.getItems()) {
                    if (aggregation.getThickness() == thinkness && aggregation.getMaterial().equals(foundMaterial)) {
                        Cell priceCell = row.getCell(priceCellNum);
                        String cellFormula = priceCell.getCellFormula();
                        if (StringUtils.isNotBlank(cellFormula)) {
                            try {
                                char column = cellFormula.charAt(cellFormula.indexOf('!') + 1);
                                int rowNum = Integer.parseUnsignedInt(cellFormula.substring(cellFormula.indexOf('!') + 2, cellFormula.indexOf('>'))) - 1;
                                Sheet calcSheet = workbook.getSheet("расчет");
                                calcSheet.getRow(rowNum).getCell(Character.getNumericValue(column) - 10).setCellValue(aggregation.getPrice());
                            } catch (Exception e) {
                                logError("Не удалось проставить цену металла во вкладке 'расчет' для материала " + foundMaterial + " " + thinkness + ":" + e.getMessage());
                            }
                        } else {
                            setValueToCell(row, priceCellNum, aggregation.getPrice());
                        }
                        if (alreadyDefinedMaterials.add(foundMaterial)) {
                            setValueToCell(row, metallCellNum, aggregation.getTotalConsumption());
                            break;
                        }
                    }
                }
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

        // сформировать заказ на производство
        // TODO
    }

    public void fillTables() throws Exception {
        fillTable1();
        fillTable2();
    }

    private void fillTable2() {
        List<CompoundAggregation> oldItems = new ArrayList(table2.getItems());
        if (CollectionUtils.isNotEmpty(oldItems)) {
            table2.getItems().clear();
        }

        ObservableList<Compound> compounds = table1.getItems();
        if (CollectionUtils.isEmpty(compounds)) {
            return;
        }
        List<CompoundAggregation> compoundAggregations = new ArrayList<>();
        for (Compound compound : compounds) {
            CompoundAggregation compoundAggregation = new CompoundAggregation();

            compoundAggregation.setMaterial(compound.getMaterial());
            compoundAggregation.setMaterialBrand(compound.getMaterialBrand());
            compoundAggregation.setThickness(compound.getThickness());
            compoundAggregation.setSize(round(compound.getXr() / 1000 * compound.getYr() / 1000));
            compoundAggregation.setListsCount(compound.getN());
            compoundAggregation.setTotalConsumption(round(compoundAggregation.getListsCount() * compoundAggregation.getSize()));

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

            double totalConsumption = compoundAggregation.getTotalConsumption();
            double thickness = compoundAggregation.getThickness() / 1000;
            double materialDensity = compoundAggregation.getMaterialDensity();
            compoundAggregation.setWeight(round(totalConsumption * thickness * materialDensity));

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
                    first.setTotalConsumption(round(first.getTotalConsumption() + second.getTotalConsumption()));
                    first.setWeight(round(first.getWeight() + second.getWeight()));
                    indexesToDelete.add(j);
                }
            }
        }

        Collections.reverse(indexesToDelete);
        indexesToDelete.forEach(index -> compoundAggregations.remove(index.intValue()));

        for (int i = 0; i < compoundAggregations.size(); i++) {
            compoundAggregations.get(i).setPosNumber(i + 1);
        }

        ObservableList<CompoundAggregation> items = FXCollections.observableList(compoundAggregations);
        table2.setItems(items);

        if (CollectionUtils.isNotEmpty(oldItems)) {
            for (CompoundAggregation oldItem : oldItems) {
                for (CompoundAggregation item : items) {
                    if (item.getPosNumber() == oldItem.getPosNumber()) {
                        item.setPrice(oldItem.getPrice());
                        item.setTotalPrice(round(item.getWeight() * item.getPrice()));
                        break;
                    }
                }
            }
            refreshTable(table2, null);
        }
    }

    private static double round(double value) {
        return Math.ceil(value * 1000) / 1000;
    }

    private boolean needToAggregate(CompoundAggregation first, CompoundAggregation second) {
        return first.getThickness() == second.getThickness()
                && first.getMaterialBrand().equals(second.getMaterialBrand())
                && first.getMaterial().equals(second.getMaterial());
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

        int orderNumber;
        try {
            orderNumber = Integer.parseUnsignedInt(projectDirName);
        } catch (NumberFormatException e) {
            throw new Exception("Ошибка при попытке получить номер заказа по названию папки заказа " + projectDirName, e);
        }

        List<OrderRow> orderRows = new OrderRowsFileUtil().restoreOrderRows(orderNumber);

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
            compound.setXmin((int) Math.ceil(Double.valueOf(getInfoValue(quotationInfo, "1"))));
            compound.setYmin((int) Math.ceil(Double.valueOf(getInfoValue(quotationInfo, "2"))));

            setXYst(compound);

            calcCompoundEditableCells(compound);

            compound.setMaterialBrand(getBrand(orderRows, quotationInfo));

            table1.getItems().add(compound);
        }
    }

    private void calcCompoundEditableCells(Compound compound) {
        compound.setSk(round(compound.getXr() / 1000 * compound.getYr() / 1000));
        compound.setSo(round(compound.getN() * compound.getSk()));
    }


    private void setXYst(Compound compound) {
        String material = compound.getMaterial();
        double thickness = compound.getThickness();

        if (isMildSteelHkOrZintec(material)) {

            // Если Xmin > или = 80% от Xst, то Xr = Xst, иначе Xr=Xmin*1,2
            int xMin = compound.getXmin();
            int xSt = compound.getXst();
            compound.setXr(xMin >= xSt * 0.8 ? xSt : xMin * 1.2);

            // Если Ymin < (Yst/2), то Yr = Yst/2, иначе Yr = Yst
            int yMin = compound.getYmin();
            int ySt = compound.getYst();
            compound.setYr(yMin < ySt / 2 ? ySt / 2 : ySt);

        } else if (isMildSteelGk(material)) {

            // Если Xmin > или = 90% от Xst, то Xr = Xst, иначе Xr=Xmin*1,2
            int xMin = compound.getXmin();
            int xSt = compound.getXst();
            compound.setXr(xMin >= xSt * 0.9 ? xSt : xMin * 1.2);

            // Если Ymin < (Yst/2), то Yr = Yst/2, иначе Yr = Yst
            int yMin = compound.getYmin();
            int ySt = compound.getYst();
            compound.setYr(yMin < ySt / 2 ? ySt / 2 : ySt);

        } else if ((thickness > 2 && isAluminium(material)) || isStainlessSteelNoFoilNoShlif(material)) {

            // Если Xmin > или = 70% от Xst, то Xr = Xst, иначе Xr=Xmin*1,2
            int xMin = compound.getXmin();
            int xSt = compound.getXst();
            compound.setXr(xMin >= xSt * 0.7 ? xSt : xMin * 1.2);

            // Yr = Yst - всегда
            compound.setYr(compound.getYst());

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
                && !StringUtils.containsIgnoreCase(material, "foil")
                && !StringUtils.containsIgnoreCase(material, "shlif");
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
                yrColumn,
                xrColumn,
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

        TableColumn<CompoundAggregation, Double> sizeColumn = ColumnFactory.createColumn(
                "Габариты листа, Xr x Yr", 50, "size",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()),
                CompoundAggregation::setSize
        );

        sizeColumn.setEditable(false);
        sizeColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

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
                    double thickness = aggreration.getThickness() / 1000;
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
                (CompoundAggregation aggregation, Double value) -> {
                    aggregation.setPrice(value);
                    aggregation.setTotalPrice(round(aggregation.getWeight() * value));
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
                sizeColumn,
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
