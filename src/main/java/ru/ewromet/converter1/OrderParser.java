package ru.ewromet.converter1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import static ru.ewromet.converter1.OrderRow.MATERIALS_LABELS;

public class OrderParser {

    private static final Map<Integer, String> tableColumns = new HashMap<Integer, String>() {{
        put(1, "\u2116");
        put(2, "наименование детали");
        put(3,"количество");
        put(4, "материал");
        put(5, "марка материала");
        put(6, "толщина");
        put(7,"для окраски");
        put(8,"принадлежность металла");
        put(9, "количество гибов на деталь");
        put(10, "создание чертежа");
        put(11, "зачистка");
        put(12, "возврат отходов");
        put(13, "возврат высечки");
        put(14, "комментарий");
    }};

    public ParseResult parse(File orderExcelFile, Logger logger) throws Exception {

        try (FileInputStream inputStream = new FileInputStream(orderExcelFile);
             Workbook workbook = getWorkbook(inputStream, orderExcelFile.getAbsolutePath())
        ) {
            String clientName = StringUtils.EMPTY;
            String posColumnHeader = StringUtils.EMPTY;
            int tableHeaderRowNum = 0;
            final String posNumberHeaderRowSymbol = tableColumns.get(1);

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
                    final String label = tableColumns.get(l);
                    if (cell == null || !StringUtils.containsIgnoreCase(cell.getStringCellValue(), label)) {
                        throw new Exception("Ожидается, что в строке шапки таблицы (строка " + (tableHeaderRowNum + 1) + ") " +
                                "в ячейке " + "№" + (l + 1) + " будет содержаться подстрока '" + label + "'");
                    }
                }

                logger.logMessage("Проверка шапки таблицы (строка " + (tableHeaderRowNum + 1) + ") завершена");

                final ObservableList<OrderRow> orderRows = FXCollections.observableArrayList();

                Set<String> addedDetailNames = new HashSet<>();

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
                    if (!addedDetailNames.add(cell.getStringCellValue())) {
                        throw new Exception("Дублирующееся наименование детали: " + cell.getStringCellValue());
                    }
                    orderRows.add(createOrderRowFromExcelRow(row));
                }

                return new ParseResult(orderRows, clientName);
            }
        }

        logger.logError("Данные не найдены");
        return new ParseResult(FXCollections.emptyObservableList(), StringUtils.EMPTY);
    }

    private OrderRow createOrderRowFromExcelRow(Row excelRow) throws Exception {
        final OrderRow orderRow = new OrderRow();
        for (Integer columnIndex : tableColumns.keySet()) {
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
            switch (columnIndex) {
                case 1:
                    final Integer posNumber = Integer.valueOf(value);
                    if (posNumber < 1) {
                        throw new Exception("Некорретный номер позиции: " + posNumber);
                    }
                    orderRow.setPosNumber(posNumber);
                    break;
                case 2:
                    orderRow.setDetailName(value);
                    break;
                case 3:
                    final Integer count;
                    if  (StringUtils.isBlank(value) || (count = Integer.valueOf(value)) == 0) {
                        throw new Exception("Количество деталей не может быть равно 0, строка " + orderRow.getPosNumber());
                    }
                    orderRow.setCount(count);
                    break;
                case 4:
                    value = value.trim().toLowerCase();
                    if (!MATERIALS_LABELS.containsKey(value)) {
                        throw new Exception("Строка " + orderRow.getPosNumber() + ": материал указан некорректно, допустимые варианты " + MATERIALS_LABELS.keySet());
                    }
                    orderRow.setMaterial(value);
                    break;
                case 5:
                    orderRow.setMaterialBrand(value);
                    break;
                case 6:
                    final Float thickness;
                    if  (StringUtils.isBlank(value) || (thickness = Float.valueOf(value)) == 0) {
                        throw new Exception("Толщина не может быть равной 0, строка " + orderRow.getPosNumber());
                    }
                    orderRow.setThickness(thickness);
                    break;
                case 7:
                    orderRow.setColor(value);
                    break;
                case 8:
                    orderRow.setOwner(value);
                    break;
                case 9:
                    if  (StringUtils.isBlank(value)) {
                        value = "0";
                    }
                    orderRow.setBendsCount(Integer.valueOf(value));
                    break;
                case 10:
                    orderRow.setDrawCreation(value);
                    break;
                case 11:
                    orderRow.setCleaning(value);
                    break;
                case 12:
                    orderRow.setWasteReturn(value);
                    break;
                case 13:
                    orderRow.setCuttingReturn(value);
                    break;
                case 14:
                    orderRow.setComment(value);
                    break;
            }
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
