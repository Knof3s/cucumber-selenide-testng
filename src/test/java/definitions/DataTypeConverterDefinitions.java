package definitions;

import io.cucumber.java.en.When;
import util.DataTypeConverterUtils;
import helpers.VariableStorage;

public class DataTypeConverterDefinitions {
    @When("Конвертировать строку {string} в целое число и сохранить как {string}")
    public void convertStringToInt(String strToInt, String storeName){
        Integer ourInt = DataTypeConverterUtils.convertStringToInt(strToInt);
        VariableStorage.setVariable(storeName, ourInt);
    }

    @When("Конвертировать строку {string} в число c плавающей запятой и сохранить как {string}")
    public void convertStringToDouble(String strToDouble, String storeName){
        Double ourDouble = DataTypeConverterUtils.convertStringToDouble(strToDouble);
        VariableStorage.setVariable(storeName, ourDouble);
    }

    @When("Конвертировать строку {string} в булевское значение и сохранить как {string}")
    public void convertStringToBoolean(String boolSting, String storeName){
        Boolean ourBoolean = DataTypeConverterUtils.convertStringToBoolean(boolSting);
        VariableStorage.setVariable(storeName, ourBoolean);
    }

    @When("Конвертировать целое число {int} в строку и сохранить как {string}")
    public void convertIntToString(int number, String storeName){
        String ourString = DataTypeConverterUtils.convertIntToString(number);
        VariableStorage.setVariable(storeName, ourString);
    }

    @When("Конвертировать число с плавающей запятой {double} в строку и сохранить как {string}")
    public void convertDoubleToString(double number, String storeName){
        String ourString = DataTypeConverterUtils.convertDoubleToString(number);
        VariableStorage.setVariable(storeName, ourString);
    }
}