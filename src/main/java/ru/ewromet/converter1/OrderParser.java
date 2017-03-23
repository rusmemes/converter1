package ru.ewromet.converter1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class OrderParser {

    private static final BiConsumer<OrderRow, String> EMPTY_SETTER = (orderRow, string) -> {};

    private static final Map<Integer, Pair<BiConsumer<OrderRow, String>, String>> tableColumns = new HashMap<Integer, Pair<BiConsumer<OrderRow, String>, String>>() {{
        put(1, Pair.of(OrderRow::setPosNumber, "\u2116"));
        put(2, Pair.of(OrderRow::setDetailName, "наименование детали"));
        put(3, Pair.of(OrderRow::setCount, "количество"));
        put(4, Pair.of(OrderRow::setMaterial, "материал"));
        put(5, Pair.of(OrderRow::setMaterialBrand, "марка материала"));
        put(6, Pair.of(EMPTY_SETTER, "толщина"));
        put(7, Pair.of(OrderRow::setColor, "для окраски"));
        put(8, Pair.of(OrderRow::setOwner, "принадлежность металла"));
        put(9, Pair.of(OrderRow::setBendsCount, "количество гибов на деталь"));
        put(10, Pair.of(EMPTY_SETTER, "создание чертежа"));
        put(11, Pair.of(EMPTY_SETTER, "зачистка"));
        put(12, Pair.of(EMPTY_SETTER, "возврат отходов"));
        put(13, Pair.of(EMPTY_SETTER, "возврат высечки"));
        put(14, Pair.of(OrderRow::setComment, "комментарий"));
    }};

    public ParseResult parse(File orderExcelFile, Logger logger) throws Exception {

        try (FileInputStream inputStream = new FileInputStream(orderExcelFile);
             Workbook workbook = getWorkbook(inputStream, orderExcelFile.getAbsolutePath())
        ) {
            String clientName = StringUtils.EMPTY;
            String posColumnHeader = StringUtils.EMPTY;
            int tableHeaderRowNum = 0;
            final String posNumberHeaderRowSymbol = tableColumns.get(1).getRight();

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                SHEET:
                for (int j = sheet.getFirstRowNum(); j < sheet.getLastRowNum(); j++) {
                    final Row row = sheet.getRow(j);
                    for (int k = row.getFirstCellNum(); k < row.getLastCellNum(); k++) {
                        Cell cell = row.getCell(k);
                        if (cell != null) {
                            String value = StringUtils.EMPTY;
                            try {
                                value = cell.getStringCellValue();
                            } catch (Exception ignored) {
                            }

                            if (StringUtils.containsIgnoreCase(value, "заказчик")) {
                                cell = row.getCell(k + 1);
                                if (cell != null) {
                                    try {
                                        value = cell.getStringCellValue();
                                        clientName = value;
                                        if (StringUtils.isNotBlank(clientName)) {
                                            logger.logMessage("Имя клиента: " + clientName);
                                        }
                                    } catch (Exception ignored) {
                                    }
                                }
                                continue SHEET;
                            }

                            if (StringUtils.containsIgnoreCase(value, posNumberHeaderRowSymbol)) {
                                posColumnHeader = value;
                                tableHeaderRowNum = j;
                                break SHEET;
                            }
                        }
                    }
                }

                if (!StringUtils.contains(posColumnHeader, posNumberHeaderRowSymbol)) {
                    throw new Exception("Ошибка при попытке найти колонку с позициями деталей по подстроке '" + posNumberHeaderRowSymbol + "'");
                }

                logger.logMessage("Проверка шапки таблицы");

                final Row tableHeadRow = sheet.getRow(tableHeaderRowNum);
                final short firstCellNum = tableHeadRow.getFirstCellNum();
                final short lastCellNum = tableHeadRow.getLastCellNum();

                if (lastCellNum - firstCellNum != tableColumns.size()) {
                    throw new Exception("Количество колонок таблицы не корректное");
                }

                for (int l = firstCellNum; l < lastCellNum; l++) {
                    Cell cell = tableHeadRow.getCell(l);
                    final String label = tableColumns.get(l).getRight();
                    if (cell == null || !StringUtils.containsIgnoreCase(cell.getStringCellValue(), label)) {
                        throw new Exception("Ожидается, что в строке шапки таблицы (строка " + (tableHeaderRowNum + 1) + ") " +
                                "в ячейке " + "№" + (l + 1) + " будет содержаться подстрока '" + label + "'");
                    }
                }

                logger.logMessage("Проверка шапки таблицы (строка " + (tableHeaderRowNum + 1) + ") завершена");

                final ObservableList<OrderRow> orderRows = FXCollections.observableArrayList();

                for (int l = tableHeaderRowNum + 1; l < sheet.getLastRowNum(); l++) {
                    final Row row = sheet.getRow(l);
                    Cell cell = row.getCell(firstCellNum);
                    if (cell == null) {
                        continue;
                    } else {
                        try {
                            cell.getNumericCellValue();
                        } catch (Exception e) {
                            logger.logMessage("Окончание таблицы - строка " + l);
                            break;
                        }
                    }
                    cell = row.getCell(firstCellNum + 1);
                    if (cell == null || StringUtils.isBlank(cell.getStringCellValue())) {
                        continue;
                    }
                    orderRows.add(createOrderRowFromExcelRow(row));
                }

                return new ParseResult(orderRows, clientName);
            }


        }

        logger.logError("Данные не найдены");
        return new ParseResult(FXCollections.emptyObservableList(), StringUtils.EMPTY);
    }

    private OrderRow createOrderRowFromExcelRow(Row excelRow) {
        final OrderRow orderRow = new OrderRow();
        for (Integer columnIndex : tableColumns.keySet()) {
            final BiConsumer<OrderRow, String> setter = tableColumns.get(columnIndex).getLeft();
            if (setter == EMPTY_SETTER) {
                continue;
            }
            final Cell cell = excelRow.getCell(columnIndex);
            if (cell == null) {
                continue;
            }
            String value = StringUtils.EMPTY;
            try {
                value = cell.getStringCellValue();
            } catch (Exception ignored1) {
                try {
                    value = String.valueOf((int) cell.getNumericCellValue());
                } catch (Exception ignored2) {}
            }
            setter.accept(orderRow, value);
        }
        return orderRow;
    }

    private Workbook getWorkbook(FileInputStream inputStream, String excelFilePath) throws IOException {
        Workbook workbook;

        if (excelFilePath.toLowerCase().endsWith("xlsx")) {
            workbook = new XSSFWorkbook(inputStream);
        } else if (excelFilePath.toLowerCase().endsWith("xls")) {
            workbook = new HSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("The specified file is not Excel file");
        }

        return workbook;
    }
}
