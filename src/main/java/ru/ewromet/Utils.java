package ru.ewromet;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ru.ewromet.converter1.OrderParserException;

public class Utils {

    public static String getFileExtension(File file) {
        final String sourceFileName = file.getName();
        final int lastCommaPos = sourceFileName.lastIndexOf(".");
        return sourceFileName.substring(lastCommaPos);
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
        final BiFunction<File, FileFilter, File[]> function = File::listFiles;
        return getFileStreamRecursively(dir, filter, function).collect(Collectors.toList());
    }

    private static Stream<File> getFileStreamRecursively(File file, FileFilter filter, BiFunction<File, FileFilter, File[]> function) {
        return Stream.of(function.apply(file, filter)).flatMap(f ->
                f.isDirectory()
                        ? getFileStreamRecursively(f, filter, function)
                        : Stream.of(f)
        );
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
}
