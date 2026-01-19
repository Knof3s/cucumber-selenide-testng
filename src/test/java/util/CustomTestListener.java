package util;

import helpers.VariableStorage;
import io.qameta.allure.Step;
import lombok.extern.log4j.Log4j2;
import org.testng.IExecutionListener;
import helpers.testrail.constants.TestRailConstants;
import helpers.testrail.storage.CaseIdsListStorage;
import helpers.testrail.TestRailExtension;
import helpers.testrail.util.TestRailUtil;

import java.util.List;

/**
 * Кастомный слушатель TestNG, выполняющийся перед и после всех тестов.
 * Используется для управления интеграцией с TestRail:
 * - Создание тестового прогона перед запуском тестов
 * - Отправка результатов тестов в TestRail
 * - Добавление Allure-отчета
 * - Очистка хранилища переменных
 */

@Log4j2
public class CustomTestListener implements IExecutionListener {

    @Override
    public void onExecutionStart() {

        if (System.getProperty("run_id") != null) {
            int runId = TestRailUtil.getAndCheckRunId();
            log.info("using helpers.testrail module with run id {}", runId);
            TestRailConstants.initialize();

            List<Integer> caseIds;
            try {
                caseIds = TestRailUtil.getCaseIdsFromTestRailByRunId(runId);
            } catch (RuntimeException e) {
                log.fatal("При получении case ids из тестрана {} возникла следующая ошибка: {}\n" +
                        "завершаем работу приложения", runId, e.getMessage());
                throw e;
            }

            log.info("Полученные caseId перед тестами: {}", caseIds);
            TestRailExtension.checkTestRunExistsForAllGivenCaseIds(caseIds);
            CaseIdsListStorage.setValidCaseIds(caseIds);
        } else {
            log.info("helpers.testrail module is not used");
        }

    }

    @Step("Завершение всех тестов: закрытие окружения и отправка allure отчета в TestRail")
    @Override
    public void onExecutionFinish() {
        log.info("Начало завершающих действий после всех тестов...");

        if (System.getProperty("run_id") != null) {
            int runId = Integer.parseInt(System.getProperty("run_id"));
            log.info("onExecutionFinish: Получен ID запуска из TestRail: {}", runId);
            log.info("отправляем результаты в тестрейл...");
            TestRailExtension.sendResultsToTestrail(runId);

            // Добавляем Allure-отчет в TestRail
            log.info("Добавление Allure-отчета в TestRail...");
            TestRailExtension.addAllureReportToRun(runId);
            log.info("Allure-отчет успешно добавлен в TestRail для запуска с ID: {}", runId);
        }

        // Очищаем хранилище переменных
        log.info("Очистка хранилища переменных...");
        VariableStorage.clearVariables();
        log.info("Хранилище переменных успешно очищено.");

        log.info("Завершающие действия после всех тестов успешно выполнены.");
    }

}
