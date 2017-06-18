package ru.ewromet.converter3;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import ru.ewromet.Controller;
import ru.ewromet.OrderRow;
import ru.ewromet.OrderRowsFileUtil;
import ru.ewromet.converter1.ColumnFactory;
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
import static ru.ewromet.Utils.containsIgnoreCase;
import static ru.ewromet.Utils.equalsBy;

public class Controller3 extends Controller {

    @FXML
    private TableView<Compound> table1;

    @FXML
    private TableView<CompoundAggregation> table2;

    private File[] files;

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
        initializeTable1();
        initializeTable2();
    }

    public void fillTables() throws Exception {
        fillTable1();
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

        String projectDirName = files[0].getParentFile().getParentFile().getName();

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

            compound.setSk(compound.getXr() * compound.getYr());
            compound.setSo(compound.getN() * compound.getSk());

            compound.setMaterialBrand(getBrand(orderRows, quotationInfo));

            table1.getItems().add(compound);
        }
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
        posNumberColumn.setResizable(false);
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
        thicknessColumn.setResizable(false);
        thicknessColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Integer> nColumn = ColumnFactory.createColumn(
                "n, шт", 50, "n",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), Compound::setN
        );

        nColumn.setEditable(false);
        nColumn.setResizable(false);
        nColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Integer> ystColumn = ColumnFactory.createColumn(
                "Yst, мм", 50, "yst",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), Compound::setYst
        );

        ystColumn.setEditable(false);
        ystColumn.setResizable(false);
        ystColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Integer> xstColumn = ColumnFactory.createColumn(
                "Xst, мм", 50, "xst",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), Compound::setXst
        );

        xstColumn.setEditable(false);
        xstColumn.setResizable(false);
        xstColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Double> yrColumn = ColumnFactory.createColumn(
                "Yr, мм", 50, "yr",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), Compound::setYr
        );

        yrColumn.setResizable(false);
        yrColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Double> xrColumn = ColumnFactory.createColumn(
                "Xr, мм", 50, "xr",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), Compound::setXr
        );

        xrColumn.setResizable(false);
        xrColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Double> skColumn = ColumnFactory.createColumn(
                "Sk, кв. м.", 50, "sk",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), Compound::setSk
        );

        skColumn.setEditable(false);
        skColumn.setResizable(false);
        skColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Double> soColumn = ColumnFactory.createColumn(
                "So, кв. м.", 50, "so",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), Compound::setSo
        );

        soColumn.setEditable(false);
        soColumn.setResizable(false);
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
    }

    private void initializeTable2() {
        table2.setEditable(true);

        TableColumn<CompoundAggregation, Integer> posNumberColumn = ColumnFactory.createColumn(
                "№", 30, "posNumber",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), CompoundAggregation::setPosNumber
        );

        posNumberColumn.setEditable(false);
        posNumberColumn.setResizable(false);
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
        thicknessColumn.setResizable(false);
        thicknessColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<CompoundAggregation, String> sizeColumn = ColumnFactory.createColumn(
                "Габариты листа, Xr x Yr", 50, "size",
                column -> new TooltipTextFieldTableCell<>(),
                CompoundAggregation::setSize
        );

        sizeColumn.setEditable(false);
        sizeColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<CompoundAggregation, Integer> countColumn = ColumnFactory.createColumn(
                "Количество листов, шт", 50, "listsCount",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), CompoundAggregation::setListsCount
        );

        countColumn.setEditable(false);
        countColumn.setResizable(false);
        countColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<CompoundAggregation, Double> totalConsumptionColumn = ColumnFactory.createColumn(
                "Общий расход металла, кв.м.", 70, "totalConsumption",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), CompoundAggregation::setTotalConsumption
        );

        totalConsumptionColumn.setEditable(false);
        totalConsumptionColumn.setResizable(false);
        totalConsumptionColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<CompoundAggregation, Double> materialDensityColumn = ColumnFactory.createColumn(
                "Плотность материала, кг/м3", 50, "materialDensity",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), CompoundAggregation::setMaterialDensity
        );

        materialDensityColumn.setResizable(false);
        materialDensityColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<CompoundAggregation, Double> weightColumn = ColumnFactory.createColumn(
                "Масса материала, кг", 50, "weight",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), CompoundAggregation::setWeight
        );

        weightColumn.setEditable(false);
        weightColumn.setResizable(false);
        weightColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<CompoundAggregation, Double> priceColumn = ColumnFactory.createColumn(
                "Масса материала, кг", 50, "price",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), CompoundAggregation::setPrice
        );

        priceColumn.setResizable(false);
        priceColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<CompoundAggregation, Double> totalPriceColumn = ColumnFactory.createColumn(
                "Масса материала, кг", 50, "totalPrice",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), CompoundAggregation::setTotalPrice
        );

        totalPriceColumn.setEditable(false);
        totalPriceColumn.setResizable(false);
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
    }
}
