package helpers.extensions;

public class StringExtensions {
    //Метод, проверяющий, что строка содержит только цифры
    public static boolean isValidNumberStringForInteger(String inputString) {
        String regex = "^[0-9.]+$";
        return inputString.matches(regex);
    }

    //Метод, проверяющий, что строка содержит только цифры и точку
    public static boolean isValidNumberStringForDouble(String inputString) {
        String regex = "^[0-9.]+$";
        return inputString.matches(regex);
    }
}