package util;

import com.codeborne.selenide.Configuration;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 * Утилитный класс для настройки браузера в тестах с использованием Selenide.
 * Этот класс предоставляет методы для конфигурации браузера перед запуском тестов.
 *
 * <p>Основные параметры, которые настраиваются:
 * <ul>
 *     <li>Выбор браузера (по умолчанию Chrome)</li>
 *     <li>Размер окна браузера (по умолчанию 1920x1080)</li>
 *     <li>Режим работы без графического интерфейса (headless), который можно настроить через параметр командной строки {@code -Dheadless=false}</li>
 *     <li>Таймауты ожиданий элементов (по умолчанию 5000 миллисекунд)</li>
 * </ul>
 *
 * <p>Пример использования:
 * <pre>
 *     BrowserUtils.browserSetup();
 * </pre>
 *
 * <p>Пример использования команд Maven для задания различных характеристик:
 * <ul>
 *     <li>Для указания браузера Chrome:
 *         <pre>
 *             mvn clean test -Dbrowser=chrome
 *         </pre>
 *     </li>
 *     <li>Для указания браузера Firefox:
 *         <pre>
 *             mvn clean test -Dbrowser=firefox
 *         </pre>
 *     </li>
 *     <li>Для включения режима без графического интерфейса (headless):
 *         <pre>
 *             mvn clean test -Dheadless=true
 *         </pre>
 *     </li>
 *     <li>Для отключения режима без графического интерфейса (headless):
 *         <pre>
 *             mvn clean test -Dheadless=false
 *         </pre>
 *     </li>
 * </ul>
 */
@Log4j2
public class BrowserUtils {
    /**
     * Настройка конфигурации браузера для тестов.
     * Устанавливает параметры браузера, такие как выбор браузера, размер окна,
     * режим без GUI и таймауты ожидания элементов.
     * <p>
     * Параметры можно настроить через системные свойства:
     * <ul>
     *     <li>{@code browser} - выбирает браузер (по умолчанию {@code chrome})</li>
     *     <li>{@code headless} - включает или отключает режим без GUI (по умолчанию {@code false})</li>
     * </ul>
     */
    public static void browserSetup() {
        // Устанавливаем браузер (по умолчанию Chrome)
        Configuration.browser = System.getProperty("browser", "chrome");
        log.info("Выбран браузер: {}", Configuration.browser);

        // Задаем размер окна браузера
        Configuration.browserSize = "1920x1080";
        log.debug("Установлен размер окна браузера: {}", Configuration.browserSize);

        // Режим без GUI, отключается через -Dheadless=false
        Configuration.headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
        log.info("Режим headless: {}", Configuration.headless ? "включен" : "отключен");

        // Таймауты ожиданий элементов
        Configuration.timeout = 5000;
        log.debug("Таймаут ожидания элементов установлен: {} мс", Configuration.timeout);

        switch ( Configuration.browser){
            case "firefox"  -> Configuration.browserCapabilities = firefoxOptions();
            case "edge" -> Configuration.browserCapabilities = edgeOptions();
            default -> Configuration.browserCapabilities = chromeOptions();
        }
        log.debug("Таймаут ожидания элементов установлен: {} мс", Configuration.timeout);
    }

    private static ChromeOptions chromeOptions(){
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--allow-running-insecure-content");
        return options;
    }
    private static FirefoxOptions firefoxOptions(){
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--allow-running-insecure-content");
        return options;
    }

    private static EdgeOptions edgeOptions(){
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--allow-running-insecure-content");
        return options;
    }
}
