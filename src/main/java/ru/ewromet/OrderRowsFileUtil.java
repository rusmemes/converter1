package ru.ewromet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static ru.ewromet.Preferences.CONVERTER_DIR_ABS_PATH;

public class OrderRowsFileUtil {

    private static final String EXTENSION = ".cor";

    public void saveOrderRows(List<OrderRow> rowList, String orderNumber) throws IOException {
        if (CollectionUtils.isEmpty(rowList) || isBlank(orderNumber)) {
            return;
        }

        final File file = Paths.get(CONVERTER_DIR_ABS_PATH, orderNumber + EXTENSION).toFile();
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file)), "UTF-8"))) {
            for (OrderRow orderRow : rowList) {
                if (isNotBlank(orderRow.getFilePath())) {
                    out.write(createCsvLine(orderRow));
                    out.newLine();
                }
            }
        }
    }

    private String createCsvLine(OrderRow orderRow) {
        return String.format("%d;%s;%s;%d;%s;%s;%s;%s;%d;%s;%s;%s;%s",
                orderRow.getPosNumber(),
                trimToEmpty(orderRow.getDetailName()).replace("\n", ""),
                trimToEmpty(orderRow.getDetailResultName()).replace("\n", ""),
                orderRow.getCount(),
                trimToEmpty(orderRow.getOriginalMaterial()).replace("\n", ""),
                trimToEmpty(orderRow.getMaterialBrand()).replace("\n", ""),
                orderRow.getThickness(),
                trimToEmpty(orderRow.getColor()).replace("\n", ""),
                orderRow.getBendsCount(),
                trimToEmpty(orderRow.getFilePath()).replace("\n", ""),
                trimToEmpty(orderRow.getOwner()).replace("\n", ""),
                trimToEmpty(orderRow.getCuttingReturn()).replace("\n", ""), // высечка
                trimToEmpty(orderRow.getWasteReturn()).replace("\n", "") // отходы
        );
    }

    public List<OrderRow> restoreOrderRows(Integer orderNumber) throws IOException {
        final File file = Paths.get(CONVERTER_DIR_ABS_PATH, orderNumber + EXTENSION).toFile();
        List<OrderRow> list = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), "UTF-8"))) {
            String line;
            while (isNotEmpty((line = in.readLine()))) {
                list.add(createOrderRowFromCsvLine(line));
            }
        }
        return list;
    }

    private OrderRow createOrderRowFromCsvLine(String line) {
        String[] split = line.split(";");
        int i = 0;
        OrderRow orderRow = new OrderRow();
        orderRow.setPosNumber(Integer.parseUnsignedInt(split[i++]));
        orderRow.setDetailName(split[i++]);
        orderRow.setDetailResultName(split[i++]);
        orderRow.setCount(Integer.parseUnsignedInt(split[i++]));
        orderRow.setOriginalMaterial(split[i++]);
        orderRow.setMaterialBrand(split[i++]);
        orderRow.setThickness(Double.parseDouble(split[i++]));
        orderRow.setColor(StringUtils.trimToNull(split[i++]));
        orderRow.setBendsCount(Integer.parseUnsignedInt(split[i++]));
        orderRow.setFilePath(split[i++]);
        if (split.length > i) {
            orderRow.setOwner(split[i++]);
        }
        if (split.length > i) {
            orderRow.setCuttingReturn(split[i++]);
        }
        if (split.length > i) {
            orderRow.setWasteReturn(split[i++]);
        }
        return orderRow;
    }
}
