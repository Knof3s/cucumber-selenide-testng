package util;

import lombok.extern.log4j.Log4j2;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Передача среды для загрузки конфиг файла, по умолчанию pres.
 * Для смены среды, следует передать -Denv=среда, например mvn test -Denv=dev
 * Для получения данных из конфига вызываем ConfigReader.getProperty("");
 */
@Log4j2
public class ConfigReader {
    public static final Properties properties = new Properties();

    static {
        String environment = System.getProperty("env", "pres");
        log.debug("Загружаю конфиг для среды: {}", environment);
        String propertiesFileName = String.format("src/test/resources/configs/config-%s.properties", environment);

        try (InputStream input = new FileInputStream(propertiesFileName)) {
            properties.load(input);
        } catch (IOException e) {
            log.error(e);
        }
    }

    public static String getProperty(String key) {
        if (key == null) {
            log.error("Ключ не может быть null");
            throw new IllegalArgumentException("Ключ не может быть null");
        }
        String value = properties.getProperty(key);
        if (value == null) {
            log.error("Значение для ключа '{}' не найдено", key);
            throw new IllegalArgumentException("Значение для ключа '" + key + "' не найдено");
        }
        return value;
    }
}

