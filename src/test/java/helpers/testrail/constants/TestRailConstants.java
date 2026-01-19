package helpers.testrail.constants;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import util.ConfigReader;

@Log4j2
public class TestRailConstants {
    public static final int TEXT_MAX_LENGTH = 100;

    public static String TESTRAIL_URL;
    public static String SUIT_ID;
    public static String TESTRAIL_PROJECT_ID;
    public static String TESTRAIL_USERNAME;
    public static String TESTRAIL_PASSWORD;

    public static MediaType HTML;
    public static MediaType JSON;
    public static OkHttpClient client;
    public static ObjectMapper mapper;

    public static void initialize() {
        log.info("инициализация тестрейл констант...");
        TESTRAIL_URL = ConfigReader.getProperty("TESTRAIL_URL");
        SUIT_ID = ConfigReader.getProperty("SUIT_ID");
        TESTRAIL_PROJECT_ID = ConfigReader.getProperty("TESTRAIL_PROJECT_ID");
        TESTRAIL_USERNAME = ConfigReader.getProperty("TESTRAIL_USERNAME");
        TESTRAIL_PASSWORD = ConfigReader.getProperty("TESTRAIL_PASSWORD");

        HTML = MediaType.parse("text/html");
        JSON = MediaType.get("application/json; charset=utf-8");
        client = new OkHttpClient();
        mapper = new ObjectMapper();
    }

}
