package util;

import helpers.extensions.StringExtensions;
import lombok.extern.log4j.Log4j2;
import static org.reflections.Reflections.log;

@Log4j2
public class DataTypeConverterUtils {
    //Конвертация строки в число
    public static int convertStringToInt(String str) {
        log.info("Входная строка: " + str);
        String filteredStr = str.replaceAll("[^\\d]", "");
        if(!StringExtensions.isValidNumberStringForInteger(filteredStr)) {
            throw new IllegalArgumentException("Невозможно конвертировать строку в целое число: " + filteredStr);
        }
        Integer outInt = Integer.parseInt(filteredStr);
        log.info("Выход: "+ outInt + " " + outInt.getClass());
        return outInt;
    }

    //Конвертация строки в число с плавающей запятой
    public static double convertStringToDouble(String str) {
        log.info("Входная строка: " + str);
        String filteredStr= str.replace(",", ".");
        filteredStr = filteredStr.replaceAll("[^\\d.]", "");
        if(!StringExtensions.isValidNumberStringForDouble(filteredStr)){
            throw new IllegalArgumentException("Невозможно конвертировать строку в число c плавающей запятой: " + filteredStr);
        }
        Double outDouble = Double.parseDouble(filteredStr);
        log.info("Выход: "+ outDouble + " " + outDouble.getClass());
        return outDouble;
    }

    //Конвертация числа в строку
    public static String convertIntToString(int number) {
        log.info("Входное число: " + number);
        return Integer.toString(number);
    }

    //Конвертация числа с плавающей запятой в строку
    public static String convertDoubleToString(double number) {
        log.info("Входное число: " + number);
        return Double.toString(number);
    }

    //Конвертация строки в булевское значение
    public static boolean convertStringToBoolean(String str) {
        log.info("Входная строка: " + str);
        return Boolean.parseBoolean(str);
    }

    //Конвертация булевского значения в строку
    public static String convertBooleanToString(boolean value) {
        log.info("Входное значение: " + value);
        return Boolean.toString(value);
    }

    //Получить объект в его строковом представлении
    public static String convertObjectToString(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.toString();
    }
}