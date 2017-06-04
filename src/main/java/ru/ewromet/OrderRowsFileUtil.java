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
import static ru.ewromet.Preferences.CONVERTER_DIR_ABS_PATH;

public class OrderRowsFileUtil {

    private static final String EXTENSION = ".cor";

    public void saveOrderRows(List<OrderRow> rowList, String orderNumber) throws IOException {
        if (CollectionUtils.isEmpty(rowList) || isBlank(orderNumber)) {
            return;
        }

        final File file = Paths.get(CONVERTER_DIR_ABS_PATH, orderNumber + EXTENSION).toFile();
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file))))) {
            for (OrderRow orderRow : rowList) {
                if (isNotBlank(orderRow.getFilePath())) {
                    out.write(createCsvLine(orderRow));
                    out.newLine();
                }
            }
        }
    }

    private String createCsvLine(OrderRow orderRow) {
        return String.format("%d;%s;%d;%s;%s;%s;%s;%d;%s",
                orderRow.getPosNumber(),
                orderRow.getDetailName(),
                orderRow.getCount(),
                orderRow.getOriginalMaterial(),
                orderRow.getMaterialBrand(),
                orderRow.getThickness(),
                StringUtils.trimToEmpty(orderRow.getColor()),
                orderRow.getBendsCount(),
                orderRow.getFilePath()
        );
    }

    public List<OrderRow> restoreOrderRows(Integer orderNumber) throws IOException {
        final File file = Paths.get(CONVERTER_DIR_ABS_PATH, orderNumber + EXTENSION).toFile();
        List<OrderRow> list = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))))) {
            String line;
            while (isNotEmpty((line = in.readLine()))) {
                list.add(createOrderRowFromCsvLine(line));
            }
        }
        return list;
    }

    private OrderRow createOrderRowFromCsvLine(String line) {
        String[] split = line.split(";");
        OrderRow orderRow = new OrderRow();
        orderRow.setPosNumber(Integer.parseUnsignedInt(split[0]));
        orderRow.setDetailName(split[1]);
        orderRow.setCount(Integer.parseUnsignedInt(split[2]));
        orderRow.setOriginalMaterial(split[3]);
        orderRow.setMaterialBrand(split[4]);
        orderRow.setThickness(Double.parseDouble(split[5]));
        orderRow.setColor(StringUtils.trimToNull(split[6]));
        orderRow.setBendsCount(Integer.parseUnsignedInt(split[7]));
        orderRow.setFilePath(split[8]);
        return orderRow;
    }
}
