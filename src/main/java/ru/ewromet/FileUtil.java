package ru.ewromet;

import java.io.File;

public class FileUtil {

    public static String getExtension(File file) {
        final String sourceFileName = file.getName();
        final int lastCommaPos = sourceFileName.lastIndexOf(".");
        return sourceFileName.substring(lastCommaPos);
    }
}
