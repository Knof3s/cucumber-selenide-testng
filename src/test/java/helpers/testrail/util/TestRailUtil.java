package helpers.testrail.util;

import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.Scenario;
import lombok.extern.log4j.Log4j2;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import org.testng.SkipException;
import helpers.testrail.storage.CaseIdsListStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static helpers.testrail.constants.TestRailConstants.TESTRAIL_PASSWORD;
import static helpers.testrail.constants.TestRailConstants.TESTRAIL_URL;
import static helpers.testrail.constants.TestRailConstants.TESTRAIL_USERNAME;
import static helpers.testrail.constants.TestRailConstants.client;
import static helpers.testrail.constants.TestRailConstants.mapper;

/**
 * Утилитный класс для работы с TestRail.
 * Содержит методы для получения тестовых кейсов из TestRail, проверки наличия ID теста, а также логирование информации.
 */
@Log4j2
public class TestRailUtil {

    /**
     * Получает список идентификаторов тестов из TestRail по ID прогона.
     *
     * @param runId ID прогона в TestRail.
     * @return Список ID тестов, привязанных к указанному прогону.
     */
    public static List<Integer> getCaseIdsFromTestRailByRunId(int runId) {
        List<Integer> caseIds = new ArrayList<>();

        HttpUrl httpUrl = HttpUrl.parse(TESTRAIL_URL + "/get_tests/" + runId);

        if (httpUrl == null) {
            throw new IllegalArgumentException("Invalid URL: " + TESTRAIL_URL + "/get_tests/" + runId);
        }

        String url = httpUrl.newBuilder().build().toString();

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", Credentials.basic(TESTRAIL_USERNAME, TESTRAIL_PASSWORD))
                .header("Content-Type", "application/json")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            log.trace("logging helpers.testrail response: {}", response);

            if (response.body() != null) {
                String responseBodyString = response.body().string();

                if (response.isSuccessful()) {
                    JsonNode jsonNodeTests = mapper.readTree(responseBodyString);
                    logTestCases(jsonNodeTests, runId);
                    for (JsonNode test : jsonNodeTests) {
                        if (test.get("case_id") != null && test.get("custom_automation_type") != null) {
                            int caseId = test.get("case_id").asInt();
                            int automationTypeValue = test.get("custom_automation_type").asInt();
                            if (automationTypeValue == 2) {
                                log.debug("Добавление тест-кейса в список -> Case ID: {} (Automation Type: {})", caseId, automationTypeValue);
                                caseIds.add(caseId);
                            } else {
                                log.debug("Пропущен тест-кейс -> Case ID: {} (Automation Type: {})", caseId, automationTypeValue);
                                CaseIdsListStorage.getSkippedCaseIds().add(caseId);
                            }
                        } else {
                            log.error("Тест {} не имеет case_id или custom_automation_type", test);
                        }
                    }
                    log.debug("После фильтрации по run id {} получили валидные case ids: {}", runId, caseIds);
                    log.debug("После фильтрации по run id {} пропущены невалидные case ids: {}", runId, CaseIdsListStorage.getSkippedCaseIds());
                } else {
                    log.error("Ошибка запроса в тестрейл: {}", response.code());
                    log.trace("логирование тела неуспешного ответа от helpers.testrail: {}", responseBodyString);
                    throw new RuntimeException("Ошибка запроса в тестрейл: " + response.code());
                }
            } else {
                log.error("response body from helpers.testrail is null");
                throw new RuntimeException("response body from helpers.testrail is null");
            }
        } catch (IOException e) {
            log.error("Ошибка при запросе в TestRail", e);
        }
        if (caseIds.isEmpty()) {
            log.error("в тестране {} нету тесткейсов", runId);
            throw new RuntimeException(String.format("в тестране %d нету тесткейсов", runId));
        }
        return caseIds;
    }

    /**
     * Получает ID теста из тега сценария Cucumber.
     *
     * @param scenario Сценарий Cucumber.
     * @return Опциональный ID теста из тега сценария.
     */
    public static Optional<String> getCaseIdFromScenario(Scenario scenario) {
        return scenario.getSourceTagNames().stream()
                .filter(tag -> tag.startsWith("@CaseID(") && tag.endsWith(")"))
                .map(tag -> tag.replace("@CaseID(", "").replace(")", ""))
                .findFirst();
    }

    /**
     * Проверяет наличие ID теста в списке и пропускает тест, если он не найден.
     *
     * @param scenario Сценарий Cucumber.
     */
    public static void caseIDChecker(Scenario scenario) {
        try {
            int caseId = getCaseIdAsInt(scenario);
            List<Integer> caseIds = CaseIdsListStorage.getValidCaseIds();

            if (!caseIds.contains(caseId)) {
                log.warn("caseIDChecker: Тест с Case ID {} не найден в списке. Пропускаем тест.", caseId);
                throw new SkipException("Тест пропущен, так как не найден в списке caseIds");
            }
        } catch (IllegalArgumentException e) {
            log.error("Ошибка при получении Case ID: {}", e.getMessage());
        }

    }

    /**
     * Получает и проверяет run_id из системных параметров.
     *
     * @return Значение run_id.
     */
    public static int getAndCheckRunId() {
        String runIdStr = System.getProperty("run_id");

        if (runIdStr == null) {
            log.fatal("Параметр run_id не передан, завершаем работу приложения");
            throw new IllegalStateException("Параметр run_id не найден. Завершаем выполнение.");
        }

        int runId;
        try {
            runId = Integer.parseInt(runIdStr);
        } catch (NumberFormatException e) {
            log.fatal("Невалидный формат run_id, завершаем работу приложения");
            throw new IllegalStateException("Невалидный формат run_id. Завершаем выполнение.");
        }
        log.info("получен run_id {} из системных параметров", runId);
        return runId;
    }

    /**
     * Извлекает и преобразует Case ID в число.
     *
     * @param scenario Сценарий Cucumber.
     * @return Case ID как число.
     * @throws IllegalArgumentException если ID отсутствует или не является числом.
     */
    public static int getCaseIdAsInt(Scenario scenario) {
        String caseIdStr = TestRailUtil.getCaseIdFromScenario(scenario)
                .orElseThrow(() -> new IllegalArgumentException("Case ID отсутствует в сценарии"));

        try {
            return Integer.parseInt(caseIdStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Case ID не является числом: " + caseIdStr);
        }
    }

    /**
     * Логирует тест-кейсы в формате таблицы.
     *
     * @param jsonNodeTests JSON-данные с тест-кейсами.
     * @param runId         run id тестрейла
     */
    private static void logTestCases(JsonNode jsonNodeTests, int runId) {
        log.info("из run с id {} получены следующие тест-кейсы:", runId);
        log.debug("{}", String.format("%-10s %-10s %-30s %-20s %s",
                "Case ID", "Run ID", "Title", "Automation Type", "Steps"));
        log.debug("{}", "-".repeat(100));

        for (JsonNode test : jsonNodeTests) {
            if (test.get("case_id") != null && test.get("custom_automation_type") != null) {
                int caseId = test.get("case_id").asInt();
                String title = test.has("title") ? test.get("title").asText() : "N/A";
                int automationTypeValue = test.get("custom_automation_type").asInt();

                // Получаем шаги (если есть)
                String steps = "N/A";
                if (test.has("custom_steps_separated") && test.get("custom_steps_separated").isArray()) {
                    List<String> stepDescriptions = new ArrayList<>();
                    for (JsonNode step : test.get("custom_steps_separated")) {
                        if (step.has("content")) {
                            stepDescriptions.add(step.get("content").asText());
                        }
                    }
                    steps = String.join(" | ", stepDescriptions);
                }

                // Логируем таблицу с форматированными значениями
                log.debug("{}", String.format("%-10d %-10d %-30s %-20d %s",
                        caseId, runId,
                        StringUtil.truncateString(title, 37),
                        automationTypeValue,
                        StringUtil.truncateString(steps, 47)
                ));
            } else {
                log.error("У теста {} нет case_id или custom_automation_type", test);
            }
        }
        log.debug("{}", "-".repeat(100));

    }

}
