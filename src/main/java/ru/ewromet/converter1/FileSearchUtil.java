package ru.ewromet.converter1;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSearchUtil {

    public static List<File> findRecursively(File dir, FileFilter filter) {
        final BiFunction<File, FileFilter, File[]> function = File::listFiles;
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory()) {
                    return true;
                }
                return filter.accept(pathname);
            }
        };
        return getFileStreamRecursively(dir, filter, function).collect(Collectors.toList());
    }

    private static Stream<File> getFileStreamRecursively(File file, FileFilter filter, BiFunction<File, FileFilter, File[]> function) {
        return Stream.of(function.apply(file, filter)).flatMap(f ->
                f.isDirectory()
                        ? getFileStreamRecursively(f, filter, function)
                        : Stream.of(f)
        );
    }
}
