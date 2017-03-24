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
        put(8,"принадлежность");
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
                    throw new OrderParserException("Ошибка при попытке найти колонку с позициями деталей по подстроке '" + posNumberHeaderRowSymbol + "'");
                }

                logger.logMessage("Проверка шапки таблицы");

                final Row tableHeadRow = sheet.getRow(tableHeaderRowNum);
                final short firstCellNum = tableHeadRow.getFirstCellNum();
                final short lastCellNum = tableHeadRow.getLastCellNum();

                if (lastCellNum - firstCellNum != tableColumns.size()) {
                    throw new OrderParserException("Количество колонок таблицы не корректное, ожидаемый набор колонок " + tableColumns.values());
                }

                for (int l = firstCellNum; l < lastCellNum; l++) {
                    Cell cell = tableHeadRow.getCell(l);
                    final String label = tableColumns.get(l);
                    if (cell == null || !StringUtils.containsIgnoreCase(cell.getStringCellValue(), label)) {
                        throw new OrderParserException("Ожидается, что в строке шапки таблицы (строка " + (tableHeaderRowNum + 1) + ") " +
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
                    if (cell == null) {
                        continue;
                    } else {
                        try {
                            if (StringUtils.isBlank(cell.getStringCellValue())) {
                                continue;
                            }
                        } catch (Exception e) {
                            throw new OrderParserException(l, "проверьте наименование детали " + e.getMessage());
                        }
                    }
                    if (!addedDetailNames.add(cell.getStringCellValue())) {
                        throw new OrderParserException("Дублирующееся наименование детали: " + cell.getStringCellValue());
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
            switch (columnIndex) {
                case 1:
                    try {
                        final int posNumber = (int)cell.getNumericCellValue();
                        if (posNumber < 1) {
                            throw new OrderParserException(posNumber);
                        }
                        orderRow.setPosNumber(posNumber);
                    } catch (Exception e) {
                        throw new OrderParserException("Некорректная позиция: " + e.getMessage());
                    }
                    break;
                case 2:
                    try {
                        orderRow.setDetailName(cell.getStringCellValue());
                    } catch (Exception e) {
                        throw new OrderParserException(orderRow.getPosNumber(), e.getMessage());
                    }
                    break;
                case 3:
                    try {
                        final int count = (int)cell.getNumericCellValue();
                        if (count < 1) {
                            throw new OrderParserException(count);
                        }
                        orderRow.setCount(count);
                    } catch (Exception e) {
                        throw new OrderParserException(orderRow.getPosNumber(), "проверьте количество " + e.getMessage());
                    }
                    break;
                case 4:
                    String value;
                    try {
                        value = cell.getStringCellValue().trim().toLowerCase();
                    } catch (Exception e) {
                        throw new OrderParserException(orderRow.getPosNumber(), "проверьте материал " + e.getMessage());
                    }
                    if (!MATERIALS_LABELS.containsKey(value)) {
                        throw new OrderParserException(orderRow.getPosNumber(), "некорректный Материал - '" + value + "', допустимые варианты " + MATERIALS_LABELS.keySet());
                    }
                    orderRow.setMaterial(value);
                    break;
                case 5:
                    try {
                        orderRow.setMaterialBrand(cell.getStringCellValue());
                    } catch (Exception e) {
                        throw new OrderParserException(orderRow.getPosNumber(), "проверьте марку материала " + e.getMessage());
                    }
                    break;
                case 6:
                    try {
                        final double thickness = cell.getNumericCellValue();
                        if  (thickness < 0D) {
                            throw new OrderParserException(thickness);
                        }
                        orderRow.setThickness(thickness);
                    } catch (Exception e) {
                        throw new OrderParserException(orderRow.getPosNumber(), "толщина материала " + e.getMessage());
                    }
                    break;
                case 7:
                    try {
                        orderRow.setColor(cell.getStringCellValue());
                    } catch (Exception e) {
                        try {
                            final double numericCellValue = cell.getNumericCellValue();
                            if (numericCellValue == (int) numericCellValue) {
                                orderRow.setColor(String.valueOf((int) numericCellValue));
                            } else {
                                orderRow.setColor(String.valueOf(numericCellValue));
                            }
                        } catch (Exception e1) {
                            throw new OrderParserException(orderRow.getPosNumber(), "проверьте цвет " + e1.getMessage());
                        }
                    }
                    break;
                case 8:
                    try {
                        orderRow.setOwner(cell.getStringCellValue());
                    } catch (Exception e) {
                        throw new OrderParserException(orderRow.getPosNumber(), "проверьте принадлежность " + e.getMessage());
                    }
                    break;
                case 9:
                    try {
                        final int bendsCount = (int)cell.getNumericCellValue();
                        if (bendsCount < 0) {
                            throw new OrderParserException(bendsCount);
                        }
                        orderRow.setBendsCount(bendsCount);
                    } catch (Exception e) {
                        throw new OrderParserException(orderRow.getPosNumber(), "проверьте количество гибов " + e.getMessage());
                    }
                    break;
                case 10:
                    try {
                        orderRow.setDrawCreation(cell.getStringCellValue());
                    } catch (Exception e) {
                        throw new OrderParserException(orderRow.getPosNumber(), "проверьте создание чертежа " + e.getMessage());
                    }
                    break;
                case 11:
                    try {
                        orderRow.setCleaning(cell.getStringCellValue());
                    } catch (Exception e) {
                        throw new OrderParserException(orderRow.getPosNumber(), "проверьте зачистку " + e.getMessage());
                    }
                    break;
                case 12:
                    try {
                        orderRow.setWasteReturn(cell.getStringCellValue());
                    } catch (Exception e) {
                        throw new OrderParserException(orderRow.getPosNumber(), "проверьте возврат отходов " + e.getMessage());
                    }
                    break;
                case 13:
                    try {
                        orderRow.setCuttingReturn(cell.getStringCellValue());
                    } catch (Exception e) {
                        throw new OrderParserException(orderRow.getPosNumber(), "проверьте возврат высечки " + e.getMessage());
                    }
                    break;
                case 14:
                    try {
                        orderRow.setComment(cell.getStringCellValue());
                    } catch (Exception e) {
                        throw new OrderParserException(orderRow.getPosNumber(), "проверьте возврат комментарий " + e.getMessage());
                    }
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
            throw new OrderParserException("The specified file is not Excel file");
        }

        return workbook;
    }
}
