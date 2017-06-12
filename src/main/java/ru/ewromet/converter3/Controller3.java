package ru.ewromet.converter3;

import java.io.File;

import org.apache.commons.lang3.ArrayUtils;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import ru.ewromet.Controller;
import ru.ewromet.converter1.ColumnFactory;
import ru.ewromet.converter1.TooltipTextFieldTableCell;

public class Controller3 extends Controller {

    @FXML
    private TableView table1;

    @FXML
    private TableView table2;

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

    private void initializeTable1() {
        table1.setEditable(true);

        TableColumn<Compound, Integer> posNumberColumn = ColumnFactory.createColumn(
                "№", 30, "posNumber",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), Compound::setPosNumber
        );

        posNumberColumn.setEditable(false);
        posNumberColumn.setMaxWidth(30);
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
                "t, мм", 70, "thickness",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), Compound::setThickness
        );

        thicknessColumn.setEditable(false);
        thicknessColumn.setMaxWidth(70);
        thicknessColumn.setResizable(false);
        thicknessColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Integer> nColumn = ColumnFactory.createColumn(
                "n, шт", 50, "n",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), Compound::setN
        );

        nColumn.setEditable(false);
        nColumn.setMaxWidth(50);
        nColumn.setResizable(false);
        nColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Integer> ystColumn = ColumnFactory.createColumn(
                "Yst, мм", 70, "yst",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), Compound::setYst
        );

        ystColumn.setEditable(false);
        ystColumn.setMaxWidth(70);
        ystColumn.setResizable(false);
        ystColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Integer> xstColumn = ColumnFactory.createColumn(
                "Xst, мм", 70, "xst",
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()), Compound::setXst
        );

        xstColumn.setEditable(false);
        xstColumn.setMaxWidth(70);
        xstColumn.setResizable(false);
        xstColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Double> yrColumn = ColumnFactory.createColumn(
                "Yr, мм", 70, "yr",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), Compound::setYr
        );

        yrColumn.setMaxWidth(70);
        yrColumn.setResizable(false);
        yrColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Double> xrColumn = ColumnFactory.createColumn(
                "Xr, мм", 70, "xr",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), Compound::setXr
        );

        xrColumn.setMaxWidth(70);
        xrColumn.setResizable(false);
        xrColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Double> skColumn = ColumnFactory.createColumn(
                "Sk, кв. м.", 70, "sk",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), Compound::setSk
        );

        skColumn.setEditable(false);
        skColumn.setMaxWidth(70);
        skColumn.setResizable(false);
        skColumn.setStyle(ALIGNMENT_BASELINE_CENTER);

        TableColumn<Compound, Double> soColumn = ColumnFactory.createColumn(
                "So, кв. м.", 70, "so",
                TextFieldTableCell.forTableColumn(new DoubleStringConverter()), Compound::setSo
        );

        soColumn.setEditable(false);
        soColumn.setMaxWidth(70);
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

    }
}
