package definitions;

import helpers.VariableStorage;
import io.cucumber.java.en.When;
import util.RandomUtils;

import static org.reflections.Reflections.log;

public class RandomDefinitions {
    @When("Рандом строки из {int} символов сохранить как {string}")
    public void getRandomString(int length, String storeName) {
        String ourString = RandomUtils.generateRandomString(length);
        log.info("Рандомная строка: {}", ourString);
        VariableStorage.setVariable(storeName, ourString);
    }

    @When("Рандом целого числа из диапазона с минимальным значением {int} и максимальным значением {int} сохранить как {string}")
    public void getRandomInt(int min, int max, String storeName) {
        int ourNumber = RandomUtils.generateRandomInt(min, max);
        log.info("Рандомное целое число: {}", ourNumber);
        VariableStorage.setVariable(storeName, ourNumber);
    }

    @When("Рандом числа с плавающей запятой с минимальным значением {double} и максимальным значением {double} и ограничением до {int} знаков после запятой сохранить как {string}")
    public void getRandomDouble(double min, double max, int decimalPlaces, String storeName) {
        double ourNumber = RandomUtils.generateRandomDouble(min, max, decimalPlaces);
        log.info("Рандомное число с плавающей запятой: {}", ourNumber);
        VariableStorage.setVariable(storeName, ourNumber);
    }

    @When("Рандом булевского значения сохранить как {string}")
    public void getRandomBoolean(String storeName) {
        boolean ourBoolean = RandomUtils.generateRandomBoolean();
        log.info("Рандомное булевское значение: {}", ourBoolean);
        VariableStorage.setVariable(storeName, ourBoolean);
    }

    @When("Рандом даты из диапазона {int} начального года и {int} конечного года в формате {string} сохранить как {string}")
    public void getRandomDate(int minYear, int maxYear, String format, String storeName) {
        String ourDate = RandomUtils.generateRandomDate(minYear, maxYear, format);
        log.info("Рандомная дата: {}", ourDate);
        VariableStorage.setVariable(storeName, ourDate);
    }

    @When("Рандом email сохранить как {string}")
    public void getRandomEmail(String storeName) {
        String ourEmail = RandomUtils.generateRandomEmail();
        log.info("Рандомный email: {}", ourEmail);
        VariableStorage.setVariable(storeName, ourEmail);
    }
}