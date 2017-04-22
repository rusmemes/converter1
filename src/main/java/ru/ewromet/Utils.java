package ru.ewromet;

import java.io.File;

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
}
