package ru.ewromet.converter2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
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

import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static ru.ewromet.Preferences.Key.LAST_PATH;
import static ru.ewromet.Preferences.Key.SPECIFICATION_TEMPLATE_PATH;
import static ru.ewromet.Utils.getFileExtension;
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
    public Button calcButton;

    public void setController1(Controller1 controller1) {
        this.controller1 = controller1;

        File orderFile = controller1.getSelectedFile();
        if (orderFile != null) {
            orderFilePathField.setText(orderFile.getAbsolutePath());
        }
    }

    @FXML
    private void initialize() {
        String templatePath = preferences.get(SPECIFICATION_TEMPLATE_PATH);
        if (isNotBlank(templatePath)) {
            templateField.setText(templatePath);
        }

        orderFilePathButton.setOnAction(event -> changePathAction(orderFilePathField));
        templateButton.setOnAction(event -> {
            changePathAction(templateField);
            String text = templateField.getText();
            if (StringUtils.isNotBlank(text)) {
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

        final FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Файлы с расширением '.xls' либо '.xlsx'", "*.xls", "*.xlsx"
                )
        );
        fileChooser.setTitle("Выбор файла");
        File dirFromConfig = new File((String) preferences.get(LAST_PATH));
        while (!dirFromConfig.exists()) {
            dirFromConfig = dirFromConfig.getParentFile();
        }
        File dir2Open = dirFromConfig;
        fileChooser.setInitialDirectory(dir2Open);

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                field.setText(file.getAbsolutePath());
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

    private void runCalc() {
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

        String orderNumber = orderFile.getParentFile().getName();
        File specFile = Paths.get(orderFile.getParent(), orderNumber + getFileExtension(template)).toFile();
        if (!specFile.exists()) {
            try {
                FileUtils.copyFile(template, specFile);
            } catch (IOException e) {
                logError("Не удалось скопировать шаблон спецификации в " + specFile.getAbsolutePath() + " " + e.getMessage());
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

        row2SymInfo.forEach((r, s) -> logMessage(r + ", " + s));
        // TODO
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
        String symFilePath = filePath.replace(getFileExtension(file), ".sym");
        if (!new File(symFilePath).exists()) {
            symFilePath = replaceLast(symFilePath, '_', '-');
            if (!new File(symFilePath).exists()) {
                symFilePath = filePath.replace(getFileExtension(file), ".SYM");
                if (!new File(symFilePath).exists()) {
                    throw new RuntimeException("Не найден файл " + symFilePath);
                }
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
     * /rcd:RadanCompoundDocument/rcd:QuotationInfo/rcd:Info[@name='Ожидаемое время обработки']/rcd:MC
     */
    private double injectCutTime(RadanCompoundDocument radanCompoundDocument, String mcMachine) {
        double psys_ewr001_2 = getInfosMcsValuesStream(
                radanCompoundDocument,
                info -> equalsIgnoreCase(info.getName(), "Кол-во врезов"),
                mc -> containsIgnoreCase(mc.getMachine(), "psys_EWR")
        ).mapToDouble(Double::valueOf)
                .findFirst()
                .getAsDouble();
        return ((int) (psys_ewr001_2 * 100)) / 100D;
    }

    /**
     * /rcd:RadanCompoundDocument/rcd:RadanAttributes/rcd:Group[@name='Геометрия']/rcd:Attr[@name='Ограничивающий прямоугольник Y']
     */
    private int injectSizeY(RadanCompoundDocument radanCompoundDocument) {
        return (int) Math.floor(
                getGroupAttrValueAsDouble(
                        radanCompoundDocument,
                        group -> equalsIgnoreCase(group.getName(), "Геометрия"),
                        attr -> equalsIgnoreCase(attr.getName(), "Ограничивающий прямоугольник Y")
                )
        );
    }

    /**
     * /rcd:RadanCompoundDocument/rcd:RadanAttributes/rcd:Group[@name='Геометрия']/rcd:Attr[@name='Ограничивающий прямоугольник Х']
     */
    private int injectSizeX(RadanCompoundDocument radanCompoundDocument) {
        return (int) Math.floor(
                getGroupAttrValueAsDouble(
                        radanCompoundDocument,
                        group -> equalsIgnoreCase(group.getName(), "Геометрия"),
                        attr -> equalsIgnoreCase(attr.getName(), "Ограничивающий прямоугольник Х")
                )
        );
    }

    /**
     * /rcd:RadanCompoundDocument/rcd:RadanAttributes/rcd:Group[@name='Геометрия']/rcd:Attr[@name='Область, включающая отверстия']
     */
    private int injectAreaWithInternalContours(RadanCompoundDocument radanCompoundDocument) {
        return (int) Math.floor(
                getGroupAttrValueAsDouble(
                        radanCompoundDocument,
                        group -> equalsIgnoreCase(group.getName(), "Геометрия"),
                        attr -> equalsIgnoreCase(attr.getName(), "Область, включающая отверстия")
                )
        );
    }

    /**
     * /rcd:RadanCompoundDocument/rcd:RadanAttributes/rcd:Group[@name='Геометрия']/rcd:Attr[@name='Область']
     */
    private int injectActualArea(RadanCompoundDocument radanCompoundDocument) {
        return (int) Math.floor(
                getGroupAttrValueAsDouble(
                        radanCompoundDocument,
                        group -> equalsIgnoreCase(group.getName(), "Геометрия"),
                        attr -> equalsIgnoreCase(attr.getName(), "Область")
                )
        );
    }

    /**
     * /rcd:RadanCompoundDocument/rcd:QuotationInfo/rcd:Info[@name='Кол-во врезов']/rcd:MC[1]
     * /rcd:RadanCompoundDocument/rcd:QuotationInfo/rcd:Info[@name='Кол-во врезов']/rcd:MC[2]
     */
    private int injectInsertsCount(RadanCompoundDocument radanCompoundDocument) {
        return getInfosMcsValuesStream(
                radanCompoundDocument,
                info -> equalsIgnoreCase(info.getName(), "Кол-во врезов"),
                mc -> containsIgnoreCase(mc.getMachine(), "psys_EWR")
        ).mapToInt(Integer::valueOf)
                .max()
                .getAsInt();
    }

    /**
     * /rcd:RadanCompoundDocument/rcd:RadanAttributes/rcd:Group[@name='Геометрия']/rcd:Attr[@name='Периметр ']
     */
    private int injectCutLength(RadanCompoundDocument radanCompoundDocument) throws Exception {
        double asDouble = getGroupAttrValueAsDouble(
                radanCompoundDocument,
                group -> equalsIgnoreCase(group.getName(), "Геометрия"),
                attr -> containsIgnoreCase(attr.getName(), "Периметр")
        );
        return (int) Math.floor(asDouble / 50) * 50;
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

    private Stream<String> getInfosMcsValuesStream(RadanCompoundDocument radanCompoundDocument, Predicate<Info> infoPredicate, Predicate<MC> mcPredicate) {
        return ofNullable(radanCompoundDocument.getQuotationInfo())
                .map(QuotationInfo::getInfos)
                .orElse(Collections.emptyList())
                .stream()
                .filter(infoPredicate)
                .map(Info::getMcs)
                .flatMap(List::stream)
                .filter(mcPredicate)
                .map(MC::getValue);
    }
}
