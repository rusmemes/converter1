package ru.ewromet;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ru.ewromet.converter1.OrderParserException;

public class Utils {

    public static String getFileExtension(File file) {
        return '.' + FilenameUtils.getExtension(file.getName());
    }

    public static String replaceLast(String string, char oldChar, char newChar) {
        int index = string.lastIndexOf(oldChar);
        if (index < 0) {
            return string;
        }
        char[] chars = string.toCharArray();
        for (int i = chars.length - 1; i >= 0; i--) {
            if (chars[i] == oldChar) {
                chars[i] = newChar;
                break;
            }
        }
        return new String(chars);
    }

    public static List<File> searchFilesRecursively(File dir, FileFilter filter) {
        return getFileStreamRecursively(dir, filter).collect(Collectors.toList());
    }

    private static Stream<File> getFileStreamRecursively(File file, FileFilter filter) {
        return Stream.of(file.listFiles(filter)).flatMap(f ->
                f.isDirectory()
                        ? getFileStreamRecursively(f, filter)
                        : Stream.of(f)
        );
    }

    public static String getClientNameFromOrderFile(String pathToOrderFile) {
        return "client name";
    }

    public static Workbook getWorkbook(FileInputStream inputStream, String excelFilePath) throws IOException {
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

    public static <T, R> Predicate<T> equalsBy(Function<T, R> function, R expected) {
        return (T arg) -> Objects.equals(function.apply(arg), expected);
    }

    public static <T> Predicate<T> containsIgnoreCase(Function<T, String> function, String subStr) {
        return (T arg) -> StringUtils.containsIgnoreCase(function.apply(arg), subStr);
    }
}
