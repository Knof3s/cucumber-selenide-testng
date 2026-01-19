package util;

import lombok.extern.log4j.Log4j2;
import java.time.LocalDate;
import java.util.Random;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;

@Log4j2
public class RandomUtils {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final Random RANDOM = new Random();

    //Получить рандомную строку заданной длины
    public static String generateRandomString(int length) {
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            stringBuilder.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return stringBuilder.toString();
    }

    //Получить рандомное целое число в заданном диапазоне
    public static int generateRandomInt(int min, int max) {
        int number;
        do {
            number = RANDOM.nextInt((max - min) + 1) + min;
        } while (String.valueOf(number).startsWith("0"));
        return number;
    }

    //Получить рандомное число с плавающей запятой в заданном интервале
    public static double generateRandomDouble(double min, double max, int decimalPlaces) {
        double number;
        do {
            number = min + (max - min) * RANDOM.nextDouble();
        } while (String.valueOf((int) number).startsWith("0"));
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(decimalPlaces);
        df.setMinimumFractionDigits(decimalPlaces);
        df.setGroupingUsed(false);
        var numberInString = df.format(number).replace(",", ".");
        var endNumber = Double.parseDouble(numberInString);
        return endNumber;
    }

    //Получить рандомное булевское значение
    public static boolean generateRandomBoolean() {
        return RANDOM.nextBoolean();
    }

    //Получить рандомную дату в пределах заданных лет
    public static String generateRandomDate(int startYear, int endYear, String format) {
        int day = generateRandomInt(1, 28);
        int month = generateRandomInt(1, 12);
        int year = generateRandomInt(startYear, endYear);
        LocalDate date = LocalDate.of(year, month, day);
        return date.format(DateTimeFormatter.ofPattern(format));
    }

    //Получить рандомный email
    public static String generateRandomEmail() {
        String username = generateRandomString(8);
        String domain = generateRandomString(5);
        return username + "@" + domain + ".com";
    }
}