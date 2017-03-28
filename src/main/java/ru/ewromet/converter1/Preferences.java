package ru.ewromet.converter1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class Preferences {

    private static final File file = Paths.get(new File(System.getProperty("user.home")).getAbsolutePath(), "converter1.ini").toFile();

    public enum Key {
        LAST_PATH(String.class), RENAME_FILES(Boolean.class);

        private Method valueOfMethod;

        Key(Class<?> klass) {
            try {
                valueOfMethod = klass.getMethod("valueOf", String.class);
            } catch (NoSuchMethodException e) {
                try {
                    valueOfMethod = klass.getMethod("valueOf", Object.class);
                } catch (NoSuchMethodException e1) {
                    e1.addSuppressed(e);
                    throw new RuntimeException(e1);
                }
            }
        }
    }

    private Map<Key, Object> options = new EnumMap<Key, Object>(Key.class) {{
        put(Key.LAST_PATH, System.getProperty("user.home"));
        put(Key.RENAME_FILES, true);
    }};

    public Preferences() throws IOException, InvocationTargetException, IllegalAccessException {
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                Properties properties = new Properties();
                properties.load(in);
                for (Key key : Key.values()) {
                    String value = properties.getProperty(key.name());
                    if (StringUtils.isNotBlank(value)) {
                        options.put(key, key.valueOfMethod.invoke(null, value));
                    }
                }
            }
        } else {
            saveToDisk();
        }
    }

    public <T extends Object> T get(Key key) {
        return (T) options.get(key);
    }

    public void update(Key key, Object value) throws IOException {
        options.put(key, value);
        saveToDisk();
    }

    private void saveToDisk() throws IOException {
        Properties properties = new Properties();
        options.forEach((key, value) -> {
            properties.setProperty(key.name(), value.toString());
        });
        if (!file.exists()) {
            file.createNewFile();
        }
        try (OutputStream os = new FileOutputStream(file)) {
            properties.store(os, "настройки конвертера");
        }
    }
}
