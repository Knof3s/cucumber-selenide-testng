package util;

import net.datafaker.Faker;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Map;

public class DataFakerUtils {

    private static Faker getFaker(String language) {
        switch (language.toLowerCase()) {
            case "ru":
                return new Faker(Locale.forLanguageTag("ru"));
            case "en":
            default:
                return new Faker(Locale.ENGLISH);
        }
    }

    /**
     * Генерация фейкового значения в зависимости от типа данных.
     * @param parameter Тип данных (например, "name", "city").
     * @param language Язык (например, "ru", "en").
     * @return Сгенерированное значение.
     */
    public static String getFakeValue(String parameter, String language) {
        Faker faker = getFaker(language);

        switch (parameter.toLowerCase()) {
            case "город":
            case "city":
                return faker.address().city();
            case "имя":
            case "name":
                return faker.name().firstName();
            case "фамилия":
            case "surname":
            case "last name":
                return faker.name().lastName();
            case "телефон":
            case "phone":
                return faker.phoneNumber().phoneNumber();
            case "адрес":
            case "address":
                return faker.address().streetAddress();
            case "почта":
            case "email":
                return faker.internet().emailAddress();
            case "компания":
            case "company":
                return faker.company().name();
            case "текст":
            case "text":
                return faker.lorem().sentence();
            default:
                throw new IllegalArgumentException("Параметр '" + parameter + "' не поддерживается.");
        }
    }

    /**
     * Кастомная генерация JSON на основе переданной структуры узлов.
     * Пример: {"name": "имя", "city": "город"}.
     * @param structure Структура JSON в виде Map<"ключ", "тип_данных">.
     * @param language Язык для генерации.
     * @return Сгенерированный JSON.
     */
    public static JSONObject generateCustomJson(Map<String, String> structure, String language) {
        JSONObject json = new JSONObject();

        structure.forEach((key, type) -> {
            String fakeData = getFakeValue(type, language);
            json.put(key, fakeData);
        });

        return json;
    }
}