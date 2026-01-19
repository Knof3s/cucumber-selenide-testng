package definitions;

import helpers.VariableStorage;
import io.cucumber.datatable.DataTable;
import util.DataFakerUtils;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.testng.Assert;
import util.CustomAssert;
import util.DataFakerUtils;

import java.util.Map;

import static org.reflections.Reflections.log;


public class VariableStepDefinitions {

    public VariableStepDefinitions() {
    }

    @ParameterType("равны|не равны")
    public Boolean equalsOrNot(String comparison) {
        return "равны".equalsIgnoreCase(comparison);
    }

    @When("Переменная - {string}, сохранить строку {string}")
    public void saveStringToVariable(String value, String variableName) {
        VariableStorage.setVariable(variableName, value);
    }

    @When("Переменная - {string}, сохранить число {int}")
    public void saveIntegerToVariable(int value, String variableName) {
        VariableStorage.setVariable(variableName, value);
    }

    @When("Переменная - {string}, сохранить двоичное число {double}")
    public void saveDoubleToVariable(String variableName, double value) {
        VariableStorage.setVariable(variableName, value);
    }

    @When("Переменная - {string}, сохранить объекта класса {double}")
    public void saveCustomObjectToVariable(String variableName, String className) throws Exception {
        Class<?> clazz = Class.forName(className);
        Object instance = clazz.getDeclaredConstructor().newInstance();
        VariableStorage.setVariable(variableName, instance);
    }

    @Then("Проверка значение переменной {string} и строка {string} - {equalsOrNot}")
    public void checkVariableEqualsString(String variableName, String expectedValue, Boolean isEqual) {
        String value = VariableStorage.getVariable(variableName, String.class);
        areValuesEqual(value, expectedValue, isEqual);
    }

    @Then("Проверка значение переменной {string} и число {int} - {equalsOrNot}")
    public void checkVariableEqualsInteger(String variableName, int expectedValue) {
        int actualValue = VariableStorage.getVariable(variableName, Integer.class);
        CustomAssert.assertEquals(expectedValue, actualValue);
    }

    @When("ОТЛАДКА:Выводим значение переменной {string}")
    public void printVariableValue(String variableName){
        Object variableValue = VariableStorage.getVariable(variableName);
        log.info("Значение переменной '{}': {}", variableName, variableValue.toString());
    }

    @When("Переменная - {string}, генерируем значение типа {string} для языка {string}")
    public void generateFakeValueToVariable(String variableName, String parameterType, String language) {
        VariableStorage.setVariable(variableName, DataFakerUtils.getFakeValue(parameterType, language));
        log.info("В переменную '{}' сохранено фейковое значение типа '{}'", variableName, parameterType);
    }

    @When("Переменная - {string}, заполнить данными из JSON с параметрами для языка {string}")
    public void generateCustomJsnToVariable(String variableName, String language, DataTable dataTable) {
        Map<String, String> structure = dataTable.asMap();
        VariableStorage.setVariable(variableName, DataFakerUtils.generateCustomJson(structure, language));
        log.info("В переменную '{}' сохранен кастомный JSON", variableName);
    }

    @Then("Проверка переменная {string} и переменная {string} - {equalsOrNot}")
    public void compareVariables(String var1, String var2, Boolean isEqual) {
        Object value1 = VariableStorage.getVariable(var1);
        Object value2 = VariableStorage.getVariable(var2);

        areValuesEqual(value1, value2, isEqual);
    }

    private void areValuesEqual(Object value1, Object value2, Boolean isEqual) {
        if (isEqual) {
            CustomAssert.assertEquals(value1, value2);
        } else {
            CustomAssert.assertNotEquals(value1, value2);
        }
    }
}
