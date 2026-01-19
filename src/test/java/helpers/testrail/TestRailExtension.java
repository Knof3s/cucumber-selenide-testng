package helpers.testrail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import lombok.extern.log4j.Log4j2;
import okhttp3.Credentials;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.testng.ITestListener;
import helpers.testrail.models.AddRunRequest;
import helpers.testrail.models.FailedResult;
import helpers.testrail.models.Result;
import helpers.testrail.models.TestResultsRequest;
import helpers.testrail.storage.CaseIdsListStorage;
import helpers.testrail.storage.TestrailMapStorage;
import helpers.testrail.util.AllureUtil;
import helpers.testrail.util.StringUtil;
import helpers.testrail.util.TestRailUtil;
import util.DateTimeUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static helpers.testrail.TestRailCredentials.getTestRailPassword;
import static helpers.testrail.TestRailCredentials.getTestRailUsername;
import static helpers.testrail.constants.TestRailConstants.HTML;
import static helpers.testrail.constants.TestRailConstants.JSON;
import static helpers.testrail.constants.TestRailConstants.SUIT_ID;
import static helpers.testrail.constants.TestRailConstants.TESTRAIL_PROJECT_ID;
import static helpers.testrail.constants.TestRailConstants.TESTRAIL_URL;
import static helpers.testrail.constants.TestRailConstants.TEXT_MAX_LENGTH;
import static helpers.testrail.constants.TestRailConstants.client;
import static helpers.testrail.constants.TestRailConstants.mapper;

/**
 * Класс расширяет интерфейс ITestListener и взаимодействует с системой TestRail для отправки результатов тестов.
 * Включает логику для:
 * <ul>
 *     <li>Отправки результатов тестов в TestRail;</li>
 *     <li>Создания тест-ранов;</li>
 *     <li>Добавления ошибок в результаты тестов;</li>
 *     <li>Добавления отчетов Allure в TestRail.</li>
 * </ul>
 */
@Log4j2
public class TestRailExtension implements ITestListener {
    /**
     * Метод для отправки результатов тестов в TestRail.
     * Получает результаты тестов из кэша (используя TestrailMapStorage) и отправляет их в TestRail.
     */
    public static void sendResultsToTestrail(int runId) {
        log.info("sending results to helpers.testrail");
        Map<Integer, Map<Integer, String>> testrailCaseIdResultMap = TestrailMapStorage.getTestrailCaseIdResultMap();
        log.debug("testrailCaseIdResultMap: {}", testrailCaseIdResultMap);

        if (TestrailMapStorage.getResultsCollectorQueue().isEmpty()) {
            log.debug("все тесты пропущены");
            return;
        }

        for (Map<Integer, Result> resultMap : TestrailMapStorage.getResultsCollectorQueue()) {
            if (!resultMap.isEmpty()) {
                int caseId = resultMap.keySet().stream().findFirst().get();
                if (CaseIdsListStorage.getValidCaseIds().contains(caseId)) {
                    log.info("Отправляем результаты тест кейса {}", caseId);
                    sendTestResults(runId, resultMap);
                } else {
                    log.info("Результаты тест кейса {} не отправлены, тест пропущен", caseId);
                }
            } else {
                log.debug("resultMap пустая, нечего отправлять");
            }
        }
    }

    /**
     * Сохраняет результат выполнения теста в коллекцию результатов.
     * <p>
     * Этот метод обрабатывает информацию о тесте, такую как его статус и возможные ошибки,
     * и отправляет ее в TestRail.
     * </p>
     *
     * @param scenario сценарий теста (Cucumber)
     */
    public static void saveTestResult(Scenario scenario, int runId) {
        Optional<String> caseIdTag = TestRailUtil.getCaseIdFromScenario(scenario);
        caseIdTag.ifPresent(caseIdStr -> {
            try {
                int caseId = Integer.parseInt(caseIdTag.get());
                Map<Integer, Result> resultsCollector = getResultCollectorMap(scenario, runId, caseId);

                // Если тест упал, отправляем ошибку
                if (scenario.isFailed()) {

                    // Получаем результат неудачного шага, если он есть
                    FailedResult failedResult = getFailedResult(scenario);

                    // Используем Optional.ofNullable для обработки возможного null
                    Optional<FailedResult> optionalFailedResult = Optional.ofNullable(failedResult);

                    // Если неудачный шаг найден, отправляем информацию в TestRail
                    optionalFailedResult.ifPresentOrElse(
                            failed -> {
                                // Получаем номер шага и ошибку
                                String errorMessage = failed.result().getError().getLocalizedMessage();

                                addErrorToResultsCollectorQueue(errorMessage, runId, caseId);

                                Allure.getLifecycle().updateStep(step -> {
                                    if (step.getName().equals("Test failed step")) {
                                        step.setStatus(Status.FAILED);
                                        step.setStatusDetails(new io.qameta.allure.model.StatusDetails()
                                                .setMessage(errorMessage));
                                    }
                                });
                            },
                            () -> {
                                // Если неудачного шага не было, отправляем общий статус
                                String errorMessage = scenario.getStatus().name();
                                addErrorToResultsCollectorQueue(errorMessage, runId, caseId);
                            }
                    );

                } else {
                    Map<Integer, String> strepResultMap = TestrailMapStorage.getTestrailStepResultMap();
                    TestrailMapStorage
                            .getTestrailCaseIdResultMap()
                            .put(caseId, strepResultMap);

                    // сохраняем resultsCollector
                    TestrailMapStorage.getResultsCollectorQueue().add(resultsCollector);
                }


            } catch (NumberFormatException e) {
                log.error("Ошибка парсинга CaseID: {}", caseIdStr);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Добавляет ошибку в коллекцию результатов и отправляет ее в TestRail.
     * Формирует комментарий с описанием ошибки.
     *
     * @param errorMessage сообщение об ошибке
     * @param runId        идентификатор тест-рана
     * @param caseId       идентификатор теста в TestRail
     */
    public static void addErrorToResultsCollectorQueue(String errorMessage,
                                                       int runId,
                                                       Integer caseId) {
        try {
            Map<Integer, String> strepResultMap = TestrailMapStorage.getTestrailStepResultMap();
            TestrailMapStorage
                    .getTestrailCaseIdResultMap()
                    .put(caseId, strepResultMap);

            String comment;
            if (strepResultMap != null && !strepResultMap.isEmpty()) {
                int failedStepNumber = Collections.max(strepResultMap.keySet());
                String stepDescription = strepResultMap.get(failedStepNumber);

                comment = String.format("Ошибка на шаге %d: \"%s\"\nТекст ошибки: %s",
                        failedStepNumber, stepDescription, errorMessage);

                log.error(comment);
            } else {
                comment = String.format("не удалось получить информацию о номере шага \nТекст ошибки: %s", errorMessage);
                log.error("Ошибка: strepResultMap пустой или отсутствует. Текст ошибки: {}", errorMessage);
            }


            // Обновляем информацию о тесте
            Map<Integer, Result> resultsCollector = new HashMap<>();
            resultsCollector.put(caseId, new Result(runId, false, comment));

            // сохраняем resultsCollector
            TestrailMapStorage.getResultsCollectorQueue().add(resultsCollector);

        } catch (Exception e) {
            log.error("Не удалось отправить ошибку в TestRail", e);
        }
    }

    /**
     * Добавляет отчет Allure к тест-рану в TestRail.
     * Генерирует отчет Allure и отправляет его как вложение к указанному тест-рану.
     *
     * @param runId идентификатор тест-рана, к которому будет добавлен отчет
     */
    public static void addAllureReportToRun(int runId) {
        File allureReport = AllureUtil.generateAllureReport();

        // Логируем информацию о сгенерированном отчете
        AllureUtil.loggingAllureReport(allureReport);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("attachment", AllureUtil.getAllureReportNameWithDate(), RequestBody.create(HTML, allureReport))
                .build();
        Request addAllureReportRequest = new Request.Builder()
                .url(TESTRAIL_URL + "/add_attachment_to_run/" + runId)
                .header("Authorization", Credentials.basic(getTestRailUsername(), getTestRailPassword()))
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();
        try (Response response = client.newCall(addAllureReportRequest).execute()) {
            if (response.isSuccessful()) {
                log.info("addAllureReportToRun method: Allure Отчет успешно отправлен в TestRail.");
            } else {
                log.error("addAllureReportToRun method: Ошибка при отправке Allure отчета в TestRail. Статус: {}", response.code());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Создает тест-ран в TestRail для всех переданных идентификаторов тест-кейсов.
     * Если тест-ран за текущую дату уже существует, новый ран не создается.
     *
     * @param caseIds список идентификаторов тест-кейсов, которые будут включены в тест-ран
     */
    public static void checkTestRunExistsForAllGivenCaseIds(List<Integer> caseIds) {
        int suiteId = StringUtil.checkIfStringIsNumberAndGetIt("SUIT_ID", SUIT_ID);
        int testrailProjectId = StringUtil.checkIfStringIsNumberAndGetIt("TESTRAIL_PROJECT_ID", TESTRAIL_PROJECT_ID);

        log.info("проверяем существование run...");

        try {
            int existingRunIdByDate = getExistingRunIdByDate(DateTimeUtils.getCurrentDateWithoutTime());
            if (existingRunIdByDate != -1) {
                log.info("test run на сегодня уже существует, run id: {}", existingRunIdByDate);
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при поиске существующего прогона", e);
        }

        try {
            RequestBody body = RequestBody.create(
                    mapper.writeValueAsString(
                            new AddRunRequest(
                                    "Автотест " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy: HH:mm")),
                                    caseIds,
                                    System.getProperty("task"),
                                    suiteId
                            )),
                    JSON);
            Request createRunRequest = new Request.Builder()
                    .url(String.format("%s/add_run/%s", TESTRAIL_URL, testrailProjectId)) // ID проекта в Testrail
                    .header("Authorization", Credentials.basic(getTestRailUsername(), getTestRailPassword()))
                    .post(body)
                    .build();

            try (Response response = client.newCall(createRunRequest).execute()) {
                log.debug("logging response: {}", response);
                if (response.body() != null) {
                    JsonNode responseBody = mapper.readTree(response.body().string());
                    log.debug("logging response body: {}", responseBody);
                } else {
                    log.error("response body is null");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    /**
     * Получает информацию о неудавшемся шаге теста.
     * <p>
     * Метод пытается найти первый шаг с ошибкой и возвращает информацию о нем.
     * </p>
     *
     * @param scenario получение ошибки с помощью рефлексии
     * @return объект, содержащий информацию о неудавшемся шаге
     * @throws NoSuchFieldException   если не удалось найти поле
     * @throws IllegalAccessException если невозможно получить доступ к полю
     */
    private static FailedResult getFailedResult(Scenario scenario) throws NoSuchFieldException, IllegalAccessException {
        Collection<String> sourceTagNames = scenario.getSourceTagNames();

        log.info("At Hooks: {}", scenario.getId());

        for (String buffer : sourceTagNames) {

            if (buffer.startsWith("@LoginTest")) {

                List<io.cucumber.plugin.event.Result> results = getResults(scenario);

                io.cucumber.plugin.event.Result failResult = null;
                int stepNumber = 0;
                for (io.cucumber.plugin.event.Result result : results) {
                    stepNumber++;
                    if (result.getStatus().name().equalsIgnoreCase("FAILED")) {
                        failResult = result;
                        break;
                    }
                }
                return new FailedResult(stepNumber, failResult);
            }
        }
        return null;
    }

    /**
     * Возвращает карту с результатами выполнения теста для указанного сценария и идентификатора тест-кейса.
     *
     * @param scenario сценарий теста (Cucumber)
     * @param caseId   идентификатор тест-кейса
     * @return карта с результатами выполнения теста
     */
    private static @NotNull Map<Integer, Result> getResultCollectorMap(Scenario scenario, int runId, int caseId) {
        boolean isSuccess = !scenario.isFailed();  // Успешность теста
        String errorMessage = isSuccess ? "" : scenario.getStatus().name(); // Статус ошибки, если не пройден

        // Создаем или получаем мапу с результатами тестов
        Map<Integer, Result> resultsCollector = new HashMap<>();
        resultsCollector.put(caseId, new Result(runId, isSuccess, errorMessage)); // Добавляем результат теста
        return resultsCollector;
    }

    /**
     * Получает результаты выполнения шагов теста из сценария.
     *
     * @param scenario сценарий теста (Cucumber)
     * @return список результатов выполнения шагов теста
     * @throws NoSuchFieldException   если не удалось найти поле
     * @throws IllegalAccessException если невозможно получить доступ к полю
     */
    private static List<io.cucumber.plugin.event.Result> getResults(Scenario scenario) throws NoSuchFieldException, IllegalAccessException {
        // Получаем доступ к delegate
        Field delegateField = scenario.getClass().getDeclaredField("delegate");
        delegateField.setAccessible(true);

        // Получаем объект delegate
        Object delegate = delegateField.get(scenario);

        // Теперь нужно получить шаги из delegate
        Field stepResultsField = delegate.getClass().getDeclaredField("stepResults");  // Название поля может быть другим
        stepResultsField.setAccessible(true);
        // Object stepResults = stepResultsField.get(delegate);

        TestCaseState testCaseState = (TestCaseState) delegateField.get(scenario);

        // Get the test case results from the delegate
        Field stepResults = testCaseState.getClass().getDeclaredField("stepResults");
        stepResults.setAccessible(true);
        return (List<io.cucumber.plugin.event.Result>) stepResults.get(testCaseState);
    }

    /**
     * Отправляет запрос на обновление результатов тестов по заданному идентификатору тест-рана.
     *
     * @param runId     идентификатор тест-рана
     * @param resultMap карта с результатами выполнения тестов
     */
    private static void sendTestResults(Integer runId, Map<Integer, Result> resultMap) {
        try {
            // Создаем тело запроса
            RequestBody body = createTestResultsRequestBody(resultMap);

            // Логируем тело запроса
            String requestBodyString = mapper.writeValueAsString(resultMap);
            log.info("request body: {}", StringUtil.truncateString(requestBodyString, TEXT_MAX_LENGTH));

            // Создаем и отправляем запрос
            Request updateTestResultsRequest = new Request.Builder()
                    .url(TESTRAIL_URL + "/add_results_for_cases/" + runId)
                    .header("Authorization", Credentials.basic(getTestRailUsername(), getTestRailPassword()))
                    .post(body)
                    .build();

            // Выполняем запрос
            log.info("Отправляем запрос по URL {}", TESTRAIL_URL + "/add_results_for_cases/" + runId);
            try (Response response = client.newCall(updateTestResultsRequest).execute()) {
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    log.error("updateTestResults method: Пустой response body. Код ответа: {}", response.code());
                    return;
                }

                JsonNode jsonResponse = mapper.readTree(responseBody.string());
                if (response.isSuccessful()) {
                    log.info("updateTestResults method: Результаты успешно отправлены в TestRail.");
                    log.trace("Response body: {}", StringUtil.truncateString(jsonResponse.toString(), TEXT_MAX_LENGTH));
                } else {
                    log.error("updateTestResults method: Ошибка при отправке результатов в TestRail. Статус: {}; \ntestresults map: {}", response.code(), resultMap);
                    log.error("response body: {}", jsonResponse.toString());
                }
            } catch (IOException e) {
                log.error("updateTestResults method: Ошибка при выполнении запроса в TestRail", e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Создает тело запроса для обновления результатов тестов в TestRail.
     *
     * @param testResults карта с результатами выполнения тестов
     * @return тело запроса для отправки в TestRail
     * @throws JsonProcessingException если возникает ошибка при сериализации данных в JSON
     */
    private static RequestBody createTestResultsRequestBody(Map<Integer, Result> testResults) throws JsonProcessingException {
        List<TestResultsRequest.Result> results = testResults.entrySet().stream()
                .map(entry -> {
                    boolean isSuccess = entry.getValue().isSuccessful();
                    int caseId = entry.getKey();
                    int statusId = getStatusId(isSuccess, caseId);
                    String comment = getComment(entry.getValue().exceptionMessage(), caseId);
                    return new TestResultsRequest.Result(
                            caseId,
                            statusId,
                            comment
                    );
                })
                .toList();

        // Логируем итоговое тело запроса перед отправкой
        log.info("Итоговое тело запроса: {}", StringUtil.truncateString(mapper.writeValueAsString(new TestResultsRequest(results)), TEXT_MAX_LENGTH));

        return RequestBody.create(
                mapper.writeValueAsString(new TestResultsRequest(results)), JSON);
    }

    /**
     * Ищет существующий тест-ран по указанной дате.
     *
     * @param date дата в формате "dd.MM.yyyy"
     * @return идентификатор тест-рана, если он найден, иначе -1
     * @throws IOException если возникает ошибка при выполнении запроса
     */
    private static int getExistingRunIdByDate(String date) throws IOException {
        log.info("ищем ран за дату {}...", date);
        String url = TESTRAIL_URL + "/get_runs/" + TESTRAIL_PROJECT_ID;

        // Создаем запрос
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", Credentials.basic(getTestRailUsername(), getTestRailPassword()))
                .header("Content-Type", "application/json") // Заголовок для JSON
                .build();

        try (Response response = client.newCall(request).execute()) {
            // Проверяем успешность запроса
            if (!response.isSuccessful()) {
                log.error("Ошибка запроса: {} {}", response.code(), response.message());
                return -1;
            }

            // Проверяем, что тело ответа не null
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                log.error("Тело ответа пусто");
                return -1;
            }

            // Читаем и парсим тело ответа
            String responseBodyString = responseBody.string();
            JsonNode responseJson = mapper.readTree(responseBodyString);

            // Проверяем, что responseBody — это массив (ArrayNode)
            if (responseJson.isArray()) {
                // Проходим по всем элементам массива
                for (JsonNode run : responseJson) {
                    // Получаем имя прогона
                    String runName = run.get("name").asText();
                    log.info("проверяем ран {}...", runName);

                    // Получаем время создания (Unix timestamp)
                    long createdOn = run.get("created_on").asLong(); // Получаем Unix timestamp

                    // Преобразуем Unix timestamp в строку с датой (формат "yyyy-MM-dd")
                    String runDate = new java.text.SimpleDateFormat("dd.MM.yyyy").format(new java.util.Date(createdOn * 1000));

                    // Проверяем, совпадает ли дата с переданной
                    if (runDate.contains(date)) {
                        int runId = run.get("id").asInt();
                        log.info("ран с названием {} и id {} за дату {} подходит!", runName, runId, runDate);
                        return runId; // Возвращаем ID подходящего прогона
                    }
                }
            } else {
                log.error("Ответ не является массивом");
            }
        } catch (IOException e) {
            // Логируем ошибку
            log.error("Ошибка при выполнении запроса: {}", e.getMessage());
            throw e;
        }

        return -1; // Возвращаем -1, если не нашли подходящий прогон
    }

    /**
     * Определяет статус теста для отправки в TestRail.
     *
     * <p>Коды статусов:
     * <ul>
     *     <li>1 - passed (тест успешно пройден)</li>
     *     <li>2 - blocked (тест заблокирован)</li>
     *     <li>4 - retest (нужно повторное тестирование)</li>
     *     <li>5 - failed (тест провален)</li>
     *     <li>6 - passed w/e (пройден с исключениями)</li>
     *     <li>7 - review (на рассмотрении)</li>
     *     <li>8 - review passed (рассмотрен и пройден)</li>
     *     <li>9 - review failed (рассмотрен и провален)</li>
     * </ul>
     * </p>
     *
     * @param isSuccess Успешно ли выполнен тест.
     * @param caseId    ID тест-кейса.
     * @return Код статуса для TestRail.
     */
    private static int getStatusId(boolean isSuccess, int caseId) {
        int statusId = isSuccess ? 1 : 5; // 1 - passed, 5 - failed
        if (CaseIdsListStorage.getSkippedCaseIds().contains(caseId)) {
            statusId = 2; // 2 - blocked
        }
        return statusId;
    }

    /**
     * Генерирует комментарий для результата теста.
     *
     * @param exceptionMessage Сообщение об ошибке, если тест провалился.
     * @param caseId           ID тест-кейса.
     * @return Комментарий для TestRail.
     */
    private static String getComment(String exceptionMessage, int caseId) {
        if (exceptionMessage.isEmpty() || exceptionMessage.isBlank()) {
            if (CaseIdsListStorage.getSkippedCaseIds().contains(caseId)) {
                return "Тест был пропущен";
            } else {
                return "Тест пройден успешно";
            }
        }
        return exceptionMessage;
    }

}