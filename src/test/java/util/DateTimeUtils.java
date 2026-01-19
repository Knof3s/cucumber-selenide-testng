package util;

import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Утилитарный класс для работы с датой и временем.
 * Предоставляет методы для форматирования даты, добавления дней и получения даты в Unix-формате.
 */
@Log4j2
public class DateTimeUtils {

    /**
     * Возвращает текущую дату в заданном формате.
     *
     * @param format строка формата даты (например, "dd.MM.yyyy HH:mm:ss")
     * @return текущая дата в виде строки в указанном формате
     */
    public static String getCurrentDate(String format) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * Возвращает текущую дату с добавлением (или вычитанием) указанного количества дней в заданном формате.
     *
     * @param days   количество дней для добавления (может быть отрицательным для вычитания)
     * @param format строка формата даты (например, "dd.MM.yyyy")
     * @return дата в виде строки в указанном формате
     */
    public static String addDays(int days, String format) {
        return LocalDateTime.now().plusDays(days).format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * Возвращает текущую дату в формате Unix (количество секунд с 1 января 1970 года).
     *
     * @return текущая дата в виде строки в Unix-формате
     */
    public static String getCurrentDateUnix() {
        long unix = Instant.now().getEpochSecond();
        return Long.toString(unix);
    }

    /**
     * Возвращает текущую дату без времени в формате "dd.MM.yyyy".
     *
     * @return текущая дата в виде строки без времени
     */
    public static String getCurrentDateWithoutTime() {
        return getCurrentDate("dd.MM.yyyy");
    }
}