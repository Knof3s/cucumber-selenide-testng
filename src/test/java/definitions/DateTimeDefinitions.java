package definitions;

import helpers.VariableStorage;
import io.cucumber.java.en.When;
import util.DateTimeUtils;

import static org.reflections.Reflections.log;

public class DateTimeDefinitions {
    @When("Дата получить текущую в формате {string} и сохранить как {string}")
    public void getCurrentDate(String format, String storeName) {
        String ourDate = DateTimeUtils.getCurrentDate(format);
        log.info("Текущая дата: {}", ourDate);
        VariableStorage.setVariable(storeName, ourDate);
    }

    @When("Дата получить текущую плюс {int} дней в формате {string} и сохранить как {string}")
    public void getCurrentDatePlusMinusDays(int days, String format, String storeName) {
        String ourDate = DateTimeUtils.addDays(days, format);
        log.info("Текущая дата плюс {} дней: {}", days, ourDate);
        VariableStorage.setVariable(storeName, ourDate);
    }

    @When("Дата получить текущую в формате unix и сохранить как {string}")
    public void getUnixDate(String storeName) {
        String ourDate = DateTimeUtils.getCurrentDateUnix();
        log.info("Текущая дата в unix формате: {}", ourDate);
        VariableStorage.setVariable(storeName, ourDate);
    }
}