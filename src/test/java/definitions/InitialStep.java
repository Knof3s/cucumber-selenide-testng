package definitions;

import com.codeborne.selenide.logevents.SelenideLogger;
import io.cucumber.java.*;
import io.qameta.allure.Step;
import io.qameta.allure.selenide.AllureSelenide;
import lombok.extern.log4j.Log4j2;
import helpers.testrail.TestRailExtension;
import helpers.testrail.storage.CaseIdsListStorage;
import helpers.testrail.storage.TestrailMapStorage;
import helpers.testrail.util.TestRailUtil;
import util.BrowserUtils;
import helpers.testit.TestItIntegration;

import java.util.Map;

import static com.codeborne.selenide.Selenide.closeWebDriver;

/**
 * Класс, представляющий базовую настройку для тестов.
 * Содержит методы для подготовки и завершения работы с тестами.
 */
@Log4j2
public class InitialStep {
    public  Map<Integer, String> strepResultMap;
    public  Map<Integer, Map<Integer, String>> caseIdResultMap;
    private TestItIntegration testIt = new TestItIntegration();

    /**
     * Метод, который выполняется перед каждым тестом.
     * Здесь настраивается браузер для тестов и активируется логирование для Allure.
     * Добавляет слушателя для AllureSelenide, чтобы делать скриншоты и сохранять исходный код страницы.
     */
    @Before
    @Step("Setting up tests")
    public void setUpTest(Scenario scenario) {


        // Логируем название сценария
        log.info("Настройка окружения перед тестом");
        log.info("Запуск теста: {}", scenario.getName());

        strepResultMap = TestrailMapStorage.getTestrailStepResultMap();
        caseIdResultMap = TestrailMapStorage.getTestrailCaseIdResultMap();

        if (System.getProperty("run_id") != null) {
            TestRailUtil.caseIDChecker(scenario);
        }

        // установка / настройка браузера перед запуском тестов
        BrowserUtils.browserSetup();

        // Включение логирования Allure для Selenide (скриншоты, исходный код страницы)
        log.info("Добавляем слушатель AllureSelenide");
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide()
                .screenshots(false) // Включить сохранение скриншотов
                .savePageSource(true)); // Сохранение исходного кода страницы

        log.debug("AllureSelenide listener успешно добавлен");
    }


    /**
     * Метод, который выполняется после каждого теста.
     * Здесь завершается работа с браузером и запись результатов для Allure.
     * Также удаляется слушатель AllureSelenide.
     */
    @After
    @Step("Closing webdriver")
    public void teardown(Scenario scenario) {
        log.info("Завершение работы теста: {}", scenario.getName());
        String status = scenario.isFailed() ? "Failed" : "Passed";
        log.info("Сценарий {} завершен со статусом: {}", scenario.getName(), status);

        // Передаем результаты в TestRailExtension если есть параметр "helpers.testrail"
        if (System.getProperty("run_id") != null) {
            int caseId = TestRailUtil.getCaseIdAsInt(scenario);
            if (CaseIdsListStorage.getValidCaseIds().contains(caseId)) {
                log.info("После завершения работы теста передаем результаты в TestRailExtension");
                TestRailExtension.saveTestResult(scenario, Integer.parseInt(System.getProperty("run_id")));
            } else {
                log.debug("Результаты теста с Case ID: {} не переданы в TestRailExtension", caseId);
            }
        }

        log.debug("Закрываем веб-драйвер");
        closeWebDriver();

        log.info("Убираем слушатель AllureSelenide");
        SelenideLogger.removeListener("AllureSelenide");

        log.debug("AllureSelenide listener успешно удален");
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        if (scenario.isFailed()) {
            testIt.attachScreenshot();
        }
    }

}
