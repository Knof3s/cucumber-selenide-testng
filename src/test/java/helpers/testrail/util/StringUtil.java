package helpers.testrail.util;

import lombok.extern.log4j.Log4j2;

/**
 * Утилитный класс для работы со строками.
 * Содержит методы для обработки строк и проверки их на соответствие числовому формату.
 */
@Log4j2
public class StringUtil {
    /**
     * Проверяет, является ли строка числом, и возвращает его в виде целого числа.
     * В случае ошибки преобразования выбрасывает исключение.
     *
     * @param variableName  имя переменной, которое будет отображаться в сообщении об ошибке.
     * @param numberToParse строка, которая должна быть преобразована в целое число.
     * @return целое число, полученное из строки.
     * @throws IllegalArgumentException если строка не является допустимым числом.
     */
    public static int checkIfStringIsNumberAndGetIt(String variableName, String numberToParse) {
        int numberToReturn;
        try {
            numberToReturn = Integer.parseInt(numberToParse);
        } catch (NumberFormatException e) {
            log.fatal("Error parsing {}: {}", variableName, e.getMessage());
            throw new IllegalArgumentException(String.format("%s must be a valid integer. Provided value: %s", variableName, numberToParse), e);
        }
        return numberToReturn;
    }

    /**
     * Обрезает строку до указанного количества символов, добавляя "..." в конце, если строка длиннее.
     *
     * @param text Входная строка.
     * @param maxLength Максимальная длина строки.
     * @return Обрезанная строка с "..." в конце, если была усечена.
     */
    public static String truncateString(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() > maxLength) {
            return text.substring(0, maxLength) + "...";
        }
        return text;
    }

}
