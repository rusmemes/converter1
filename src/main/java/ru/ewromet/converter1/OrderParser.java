package ru.ewromet.converter1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OrderParser {

    private static final Map<Integer, String> headerLabels = new HashMap<Integer, String>() {{
        put(2, "наименование");
        put(3, "количеств");
        put(4, "материал");
        put(5, "марка");
        put(6, "толщин");
        put(7, "окраск");
        put(8, "принадлеж");
        put(9, "гиб");
        put(10, "чертеж");
        put(11, "чист");
        put(12, "отход");
        put(13, "высеч");
        put(14, "коммент");
    }};

    public ParseResult parse(File orderExcelFile, Logger logger) throws Exception {

        try (FileInputStream inputStream = new FileInputStream(orderExcelFile);
             Workbook workbook = getWorkbook(inputStream, orderExcelFile.getAbsolutePath())
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            Row orderInfoRow = sheet.getRow(5);

            Cell labelCell = orderInfoRow.getCell(2);
            Cell clientNameCell = orderInfoRow.getCell(3);
            String clientName = StringUtils.EMPTY;

            if (labelCell == null || !StringUtils.contains(labelCell.getStringCellValue(), "Заказчик")
                    || clientNameCell == null || StringUtils.isEmpty(clientNameCell.getStringCellValue())) {
                clientName = clientNameCell.getStringCellValue();
                logger.logMessage("Имя клиента: " + clientName);
            }

            int tableHeaderRowNum = 0;
            Row tableHeaderRow = null;
            String posColumnHeader = StringUtils.EMPTY;
            for (int i = 6; i < sheet.getLastRowNum(); i++) {
                tableHeaderRow = sheet.getRow(i);
                final Cell cell = tableHeaderRow.getCell(1);
                if (cell != null) {
                    posColumnHeader = cell.getStringCellValue();
                    if (StringUtils.contains(posColumnHeader, "\u2116")) {
                        tableHeaderRowNum = i;
                        break;
                    }
                }
            }

            if (!StringUtils.contains(posColumnHeader, "\u2116")) {
                throw new Exception("Ошибка при попытке найти колонку с позициями деталей по символу '\u2116'");
            }

            logger.logMessage("Проверка шапки таблицы");

            for (int i = 2; i < tableHeaderRow.getLastCellNum(); i++) {
                Cell cell = tableHeaderRow.getCell(i);
                if (i < 15) {
                    if (cell == null || !StringUtils.containsIgnoreCase(cell.getStringCellValue(), headerLabels.get(i))) {
                        throw new Exception("Ожидается, что в строке шапки таблицы (строка " + tableHeaderRowNum + ") в ячейке " +
                                "№" + (i + 1) + " будет содержаться подстрока '" + headerLabels.get(i) + "'");
                    }
                } else if (cell != null && StringUtils.isNotBlank(cell.getStringCellValue())) {
                    throw new Exception(
                            "Ожидается, что последней колонкой с контентом в строке шапки (строка " + tableHeaderRowNum + ") таблицы " +
                                    "будет колонка №15. Наличие контента в любой последующей ячейке данной строки " +
                                    "может свидетельствовать о некорректности заполнения файла заявки");
                }
            }

            logger.logMessage("Проверка шапки таблицы (строка " + tableHeaderRowNum + ") завершена");

            final ObservableList<OrderRow> orderRows = FXCollections.observableArrayList();

            for (int i = tableHeaderRowNum + 1; i < sheet.getLastRowNum(); i++) {
                final Row row = sheet.getRow(i);
                Cell cell = row.getCell(1);
                if (cell == null) {
                    continue;
                } else {
                    try {
                        cell.getNumericCellValue();
                    } catch (Exception e) {
                        logger.logMessage("Окончание таблицы - строка " + i);
                        break;
                    }
                }
                cell = row.getCell(2);
                if (cell == null || StringUtils.isBlank(cell.getStringCellValue())) {
                    continue;
                }
                orderRows.add(createOrderRowFromExcelRow(row));
            }

            return new ParseResult(orderRows, clientName);
        }
    }

    private OrderRow createOrderRowFromExcelRow(Row excelRow) {
        return new OrderRow(
                String.valueOf((int) excelRow.getCell(1).getNumericCellValue()),
                excelRow.getCell(2).getStringCellValue(),
                String.valueOf((int) excelRow.getCell(3).getNumericCellValue()),
                excelRow.getCell(4).getStringCellValue(),
                excelRow.getCell(5).getStringCellValue(),
                excelRow.getCell(7).getStringCellValue(),
                excelRow.getCell(8).getStringCellValue(),
                excelRow.getCell(9).getNumericCellValue() > 0 ? "да" : "нет",
                String.valueOf((int) excelRow.getCell(9).getNumericCellValue()),
                excelRow.getCell(14).getStringCellValue()
        );
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
