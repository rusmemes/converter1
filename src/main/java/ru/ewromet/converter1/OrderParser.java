package ru.ewromet.converter1;

import javafx.collections.FXCollections;
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

            if (labelCell == null || !StringUtils.contains(labelCell.getStringCellValue(), "Заказчик")
                    || clientNameCell == null || StringUtils.isEmpty(clientNameCell.getStringCellValue())) {
                throw new Exception("Ожидается, что в 4-ой колонке 6-ой строки будет информация о заказчике");
            }

            String clientName = clientNameCell.getStringCellValue();
            logger.logMessage("Имя клиента: " + clientName);
            Row tableHeaderRow = sheet.getRow(13);
            String posColumnHeader = tableHeaderRow.getCell(1).getStringCellValue();

            if (!StringUtils.contains(posColumnHeader, "\u2116")) {
                throw new Exception("Ошибка при попытке найти колонку с позициями деталей по символу '\u2116'");
            }

            logger.logMessage("Проверка шапки таблицы");

            for (int i = 2; i < tableHeaderRow.getLastCellNum(); i++) {
                Cell cell = tableHeaderRow.getCell(i);
                if (i < 15) {
                    if (cell == null || !StringUtils.containsIgnoreCase(cell.getStringCellValue(), headerLabels.get(i))) {
                        throw new Exception("Ожидается, что в строке шапки таблицы (14-я строка) в ячейке " +
                                "№" + (i + 1) + " будет содержаться подстрока '" + headerLabels.get(i) + "'");
                    }
                } else if (cell != null && StringUtils.isNotBlank(cell.getStringCellValue())) {
                    throw new Exception(
                            "Ожидается, что последней колонкой с контентом в строке шапки (14-я строка) таблицы " +
                                    "будет колонка №15. Наличие контента в любой последующей ячейке данной строки " +
                                    "может свидетельствовать о некорректности заполнения файла заявки");
                }
            }

            logger.logMessage("Проверка шапки таблицы завершена");
//            for (Row nextRow : sheet) {
//                Iterator<Cell> cellIterator = nextRow.cellIterator();
//
//                while (cellIterator.hasNext()) {
//                    Cell nextCell = cellIterator.next();
//                    int columnIndex = nextCell.getColumnIndex();
//
//                    switch (columnIndex) {
//                        case 1:
//
//                            break;
//                        case 2:
//
//                            break;
//                        case 3:
//
//                            break;
//                    }
//                }
//            }
            return new ParseResult(FXCollections.emptyObservableList(), clientName);
        }
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
