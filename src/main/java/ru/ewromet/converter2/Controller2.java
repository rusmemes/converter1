package ru.ewromet.converter2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.ewromet.Controller;
import ru.ewromet.OrderRow;
import ru.ewromet.OrderRowsFileUtil;
import ru.ewromet.converter1.Controller1;
import ru.ewromet.converter2.parser.Attr;
import ru.ewromet.converter2.parser.Group;
import ru.ewromet.converter2.parser.Info;
import ru.ewromet.converter2.parser.MC;
import ru.ewromet.converter2.parser.QuotationInfo;
import ru.ewromet.converter2.parser.RadanAttributes;
import ru.ewromet.converter2.parser.RadanCompoundDocument;
import ru.ewromet.converter2.parser.SymFileParser;
import ru.ewromet.converter3.Controller3;

import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static ru.ewromet.Preferences.Key.NESTS_BASE_PATH;
import static ru.ewromet.Preferences.Key.SPECIFICATION_TEMPLATE_PATH;
import static ru.ewromet.Utils.containsIgnoreCase;
import static ru.ewromet.Utils.equalsBy;
import static ru.ewromet.Utils.getFileExtension;
import static ru.ewromet.Utils.getWorkbook;
import static ru.ewromet.Utils.replaceLast;

public class Controller2 extends Controller {

    private Controller1 controller1;
    private OrderRowsFileUtil orderRowsFileUtil = new OrderRowsFileUtil();

    @FXML
    private Button orderFilePathButton;

    @FXML
    private TextField orderFilePathField;

    @FXML
    private Button templateButton;

    @FXML
    private TextField templateField;

    @FXML
    private Button compoundsButton;

    @FXML
    private TextField compoundsField;

    @FXML
    public Button calcButton;

    public void setController1(Controller1 controller1) {
        this.controller1 = controller1;

        File orderFile = controller1.getSelectedFile();
        if (orderFile != null) {
            orderFilePathField.setText(orderFile.getAbsolutePath());
            String nestsBasePath = preferences.get(NESTS_BASE_PATH);
            if (isNotBlank(nestsBasePath) && !nestsBasePath.equals("null")) {
                compoundsField.setText(Paths.get(nestsBasePath, orderFile.getParentFile().getName(), "nests").toFile().getAbsolutePath());
            }
        }
    }

    public TextField getCompoundsField() {
        return compoundsField;
    }

    @FXML
    private void initialize() {
        String templatePath = preferences.get(SPECIFICATION_TEMPLATE_PATH);
        if (isNotBlank(templatePath)) {
            templateField.setText(templatePath);
        }

        orderFilePathButton.setOnAction(event -> {
            changePathAction(orderFilePathField);
            if (isBlank(compoundsField.getText())) {
                String nestsBasePath = preferences.get(NESTS_BASE_PATH);
                if (isNotBlank(nestsBasePath) && !nestsBasePath.equals("null")) {
                    File nestsDir = Paths.get(nestsBasePath, new File(orderFilePathField.getText()).getParentFile().getName(), "nests").toFile();
                    if (nestsDir.exists()) {
                        compoundsField.setText(nestsDir.getAbsolutePath());
                    }
                }
            }
        });

        compoundsButton.setOnAction(event -> {
            logArea.getItems().clear();

            chooseDirAndAccept(
                    "Директория с файлами компановок",
                    file -> {
                        compoundsField.setText(file.getAbsolutePath());
                        try {
                            File nestsDir = file.getParentFile().getParentFile();
                            if (nestsDir.exists()) {
                                String newNestsBasePath = nestsDir.getAbsolutePath();
                                preferences.update(NESTS_BASE_PATH, newNestsBasePath);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
            );
        });

        templateButton.setOnAction(event -> {
            changePathAction(templateField);
            String text = templateField.getText();
            if (isNotBlank(text)) {
                try {
                    preferences.update(SPECIFICATION_TEMPLATE_PATH, text);
                } catch (IOException e) {
                    logError("Не удалось сохранить путь к шаблону спецификации для будущего использования " + e.getMessage());
                }
            }
        });

        calcButton.setOnAction(event -> runCalc());
    }

    public void setFocus() {
        if (isBlank(orderFilePathField.getText())) {
            orderFilePathField.requestLayout();
        } else if (isBlank(templateField.getText())) {
            templateField.requestFocus();
        } else {
            calcButton.requestFocus();
        }
    }

    public void changePathAction(TextField field) {
        logArea.getItems().clear();

        chooseFileAndAccept(
                new FileChooser.ExtensionFilter(
                        "Файлы с расширением '.xls' либо '.xlsx'", "*.xls", "*.xlsx"
                ),
                "Выбор файла",
                file -> field.setText(file.getAbsolutePath())
        );
    }

    private void runCalc() {
        logArea.getItems().clear();
        logMessage("Начало работы...");
        logMessage("Проверка доступности заявки и шаблона спецификации...");
        String path = orderFilePathField.getText();
        if (isBlank(path)) {
            logError("Укажите файл заявки");
            return;
        }
        File orderFile = new File(path);
        if (!orderFile.exists() || !orderFile.isFile()) {
            logError("Файл заявки не найден");
            return;
        }
        File template;
        path = templateField.getText();
        if (isBlank(path) || !(template = new File(path)).exists() || !template.isFile()) {
            logError("Не найден или не указан файл шаблона спецификации");
            return;
        }

        Integer orderNumber;
        try {
            orderNumber = Integer.parseUnsignedInt(orderFile.getParentFile().getName());
        } catch (Exception e) {
            logError("Ошибка при попытке ипользовать название папки с заявкой в числовом виде как номер заказа для вставки в спецификацию");
            return;
        }

        File specTmpFile = Paths.get(orderFile.getParent(), orderNumber + ".tmp." + getExtension(template.getName())).toFile();
        if (!specTmpFile.exists()) {
            try {
                FileUtils.copyFile(template, specTmpFile);
            } catch (IOException e) {
                logError("Не удалось скопировать шаблон спецификации в " + specTmpFile.getAbsolutePath() + " " + e.getMessage());
                return;
            }
        }

        List<OrderRow> orderRows;
        try {
            orderRows = orderRowsFileUtil.restoreOrderRows(orderNumber);
        } catch (IOException e) {
            logError("Ошибка при выгрузке информации по заявке из временного файла: " + e.getMessage());
            return;
        }

        Map<OrderRow, SymFileInfo> row2SymInfo = new TreeMap<>(Comparator.comparing(OrderRow::getPosNumber));
        try {
            row2SymInfo.putAll(orderRows.stream().collect(toMap(identity(), this::symFileOf)));
        } catch (Exception e) {
            logError("Ошибка при выгрузке информации из sym-файлов: " + e.getMessage());
            return;
        }

        File specFile = new File(specTmpFile.getAbsolutePath().replace(".tmp", ""));
        try (FileInputStream inputStream = new FileInputStream(specTmpFile);
             Workbook workbook = getWorkbook(inputStream, specTmpFile.getAbsolutePath());
             OutputStream out = new FileOutputStream(specFile);
        ) {
            Sheet sheet = workbook.getSheet("расчет");
            setValueToCell(sheet.getRow(20), 0, orderNumber);
            row2SymInfo.forEach((orderRow, symInfo) -> {
                Row row = sheet.getRow(23 + orderRow.getPosNumber() - 1);
                setValueToCell(row, 2, orderRow.getPosNumber());
                setValueToCell(row, 3, orderRow.getDetailName());
                setValueToCell(row, 4, orderRow.getCount());
                setValueToCell(row, 5, orderRow.getOriginalMaterial());
                setValueToCell(row, 6, orderRow.getMaterialBrand());
                setValueToCell(row, 7, orderRow.getThickness());
                setValueToCell(row, 8, orderRow.getColor());
                setValueToCell(row, 11, symInfo.getCutLength());
                setValueToCell(row, 12, symInfo.getInsertsCount());
                setValueToCell(row, 13, symInfo.getActualArea());
                setValueToCell(row, 14, symInfo.getAreaWithInternalContours());
                setValueToCell(row, 16, symInfo.getSizeX());
                setValueToCell(row, 17, symInfo.getSizeY());
                setValueToCell(row, 18, orderRow.getBendsCount());
                setValueToCell(row, 24, symInfo.getCutTimeUniMach());
                setValueToCell(row, 25, symInfo.getCutTimeTrumpf());
            });
            workbook.setForceFormulaRecalculation(true);
            workbook.write(out);
        } catch (IOException e) {
            logError("Ошибка при заполнении спецификации: " + e.getClass().getName() + ' ' + e.getMessage());
            return;
        } finally {
            if (!specTmpFile.delete()) {
                specTmpFile.deleteOnExit();
            }
        }
        logMessage("ДАННЫЕ СОХРАНЕНЫ");
        openConverter3Window(specFile);
    }

    private SymFileInfo symFileOf(OrderRow orderRow) {
        String filePath = orderRow.getFilePath();
        if (isBlank(filePath)) {
            throw new RuntimeException("Не задан файл для позиции " + orderRow);
        }
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("Не найден файл " + file.getAbsolutePath());
        }
        String startSymFile;
        String symFilePath = startSymFile = filePath.replace(getFileExtension(file), ".sym");
        if (!new File(symFilePath).exists()) {
            String oldSymFilePath = symFilePath;
            symFilePath = replaceLast(symFilePath, '_', '-');
            boolean found = false;
            while (!oldSymFilePath.equals(symFilePath)) {
                if (!new File(symFilePath).exists()) {
                    symFilePath = filePath.replace(getFileExtension(file), ".SYM");
                    if (!new File(symFilePath).exists()) {
                        oldSymFilePath = symFilePath;
                        symFilePath = replaceLast(symFilePath, '_', '-').replace(".SYM", ".sym");
                    } else {
                        found = true;
                        break;
                    }
                } else {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new RuntimeException("Не найден файл " + startSymFile);
            }
        }

        RadanCompoundDocument radanCompoundDocument;
        try {
            radanCompoundDocument = SymFileParser.parse(symFilePath);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка парсинга sym-файла", e);
        }

        SymFileInfo symFileInfo = new SymFileInfo();

        try {
            symFileInfo.setCutLength(injectCutLength(radanCompoundDocument));
        } catch (Exception e) {
            logError("Ошибка при извлечении длины резки из sym-файла " + symFilePath);
        }

        try {
            symFileInfo.setInsertsCount(injectInsertsCount(radanCompoundDocument));
        } catch (Exception e) {
            logError("Ошибка при извлечении количества  врезов из sym-файла " + symFilePath);
        }

        try {
            symFileInfo.setActualArea(injectActualArea(radanCompoundDocument));
        } catch (Exception e) {
            logError("Ошибка при извлечении фактической площади детали из sym-файла " + symFilePath);
        }

        try {
            symFileInfo.setAreaWithInternalContours(injectAreaWithInternalContours(radanCompoundDocument));
        } catch (Exception e) {
            logError("Ошибка при извлечении площади детали с внутренними контурами из sym-файла " + symFilePath);
        }

        try {
            symFileInfo.setSizeX(injectSizeX(radanCompoundDocument));
        } catch (Exception e) {
            logError("Ошибка при извлечении габарита по X из sym-файла " + symFilePath);
        }

        try {
            symFileInfo.setSizeY(injectSizeY(radanCompoundDocument));
        } catch (Exception e) {
            logError("Ошибка при извлечении габарита по Y из sym-файла " + symFilePath);
        }

        try {
            symFileInfo.setCutTimeUniMach(injectCutTime(radanCompoundDocument, "psys_EWR001_2"));
        } catch (Exception e) {
            logError("Ошибка при извлечении время резки UniMach из sym-файла " + symFilePath);
        }

        try {
            symFileInfo.setCutTimeTrumpf(injectCutTime(radanCompoundDocument, "psys_EWR001_1"));
        } catch (Exception e) {
            logError("Ошибка при извлечении время резки Trumpf из sym-файла " + symFilePath);
        }

        return symFileInfo;
    }

    /**
     * /rcd:RadanCompoundDocument/rcd:RadanAttributes/rcd:Group[@name='Производство  ']/rcd:Attr[@num='123']/rcd:MC
     */
    private double injectCutTime(RadanCompoundDocument radanCompoundDocument, String mcMachine) {
        double psys_ewr = ofNullable(radanCompoundDocument.getRadanAttributes())
                .map(RadanAttributes::getGroups)
                .orElse(Collections.emptyList())
                .stream()
                .filter(containsIgnoreCase(Group::getName, "Производство"))
                .map(Group::getAttrs)
                .flatMap(List::stream)
                .filter(equalsBy(Attr::getNum, "123"))
                .map(Attr::getMcs)
                .flatMap(List::stream)
                .filter(mc -> equalsIgnoreCase(mc.getMachine(), mcMachine))
                .map(MC::getValue)
                .mapToDouble(Double::valueOf)
                .findFirst()
                .getAsDouble() * 60;

        return ((int) (psys_ewr * 100)) / 100D;
    }

    /**
     * /rcd:RadanCompoundDocument/rcd:RadanAttributes/rcd:Group[@name='Геометрия']/rcd:Attr[@num='166']
     */
    private int injectSizeY(RadanCompoundDocument radanCompoundDocument) {
        return (int) Math.ceil(
                getGroupAttrValueAsDouble(
                        radanCompoundDocument,
                        containsIgnoreCase(Group::getName, "Геометрия"),
                        equalsBy(Attr::getNum, "166")
                )
        );
    }

    /**
     * /rcd:RadanCompoundDocument/rcd:RadanAttributes/rcd:Group[@name='Геометрия']/rcd:Attr[@num='165']
     */
    private int injectSizeX(RadanCompoundDocument radanCompoundDocument) {
        return (int) Math.ceil(
                getGroupAttrValueAsDouble(
                        radanCompoundDocument,
                        containsIgnoreCase(Group::getName, "Геометрия"),
                        equalsBy(Attr::getNum, "165")
                )
        );
    }

    /**
     * /rcd:RadanCompoundDocument/rcd:RadanAttributes/rcd:Group[@name='Геометрия']/rcd:Attr[@num='163']
     */
    private int injectAreaWithInternalContours(RadanCompoundDocument radanCompoundDocument) {
        return (int) Math.round(
                getGroupAttrValueAsDouble(
                        radanCompoundDocument,
                        containsIgnoreCase(Group::getName, "Геометрия"),
                        equalsBy(Attr::getNum, "163")
                )
        );
    }

    /**
     * /rcd:RadanCompoundDocument/rcd:RadanAttributes/rcd:Group[@name='Геометрия']/rcd:Attr[@num='162']
     */
    private int injectActualArea(RadanCompoundDocument radanCompoundDocument) {
        return (int) Math.round(
                getGroupAttrValueAsDouble(
                        radanCompoundDocument,
                        containsIgnoreCase(Group::getName, "Геометрия"),
                        equalsBy(Attr::getNum, "162")
                )
        );
    }

    /**
     * /rcd:RadanCompoundDocument/rcd:QuotationInfo/rcd:Info[@num='0']/@value
     */
    private int injectInsertsCount(RadanCompoundDocument radanCompoundDocument) {
        return ofNullable(radanCompoundDocument.getQuotationInfo())
                .map(QuotationInfo::getInfos)
                .orElse(Collections.emptyList())
                .stream()
                .filter(equalsBy(Info::getNum, "0"))
                .map(Info::getValue)
                .mapToInt(Integer::valueOf)
                .findFirst()
                .getAsInt();
    }

    /**
     * /rcd:RadanCompoundDocument/rcd:RadanAttributes/rcd:Group[@name='Геометрия']/rcd:Attr[@num='168']
     */
    private int injectCutLength(RadanCompoundDocument radanCompoundDocument) throws Exception {
        double asDouble = getGroupAttrValueAsDouble(
                radanCompoundDocument,
                containsIgnoreCase(Group::getName, "Геометрия"),
                equalsBy(Attr::getNum, "168")
        );
        return (int) Math.ceil(asDouble / 50) * 50;
    }

    private double getGroupAttrValueAsDouble(RadanCompoundDocument radanCompoundDocument, Predicate<Group> groupPredicate, Predicate<Attr> attrPredicate) {
        return ofNullable(radanCompoundDocument.getRadanAttributes())
                .map(RadanAttributes::getGroups)
                .orElse(Collections.emptyList())
                .stream()
                .filter(groupPredicate)
                .map(Group::getAttrs)
                .flatMap(List::stream)
                .filter(attrPredicate)
                .map(Attr::getValue)
                .mapToDouble(Double::valueOf)
                .findFirst()
                .getAsDouble();
    }

    private void openConverter3Window(File specFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/converter3.fxml"));
            Parent root = loader.load();
            Controller3 controller = loader.getController();
            controller.setCompoundsPath(compoundsField.getText());
            controller.setOrderFilePath(orderFilePathField.getText());
            controller.setSpecFile(specFile);
            controller.fillTables();
            Stage stage = new Stage();
            stage.setHeight(765);
            stage.setWidth(1360);
            controller.setStage(stage);
            stage.setTitle("Обработка компоновок");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            logError("Ошибка при открытии окна: " + e.getMessage());
        }
    }
}
