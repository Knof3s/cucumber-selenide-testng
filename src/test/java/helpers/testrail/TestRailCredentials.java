package helpers.testrail;

import util.ConfigReader;

import java.util.function.Supplier;

/**
 * Класс для получения учетных данных TestRail из конфигурационного файла.
 */
public class TestRailCredentials {
    private static final Supplier<String>
            TESTRAIL_USERNAME = () -> ConfigReader.getProperty("TESTRAIL_USERNAME"),
            TESTRAIL_PASSWORD = () -> ConfigReader.getProperty("TESTRAIL_PASSWORD");

    public static String getTestRailUsername() {
        return TESTRAIL_USERNAME.get();
    }

    public static String getTestRailPassword() {
        return TESTRAIL_PASSWORD.get();
    }
}