package helpers;

import com.codeborne.selenide.SelenideElement;
import helpers.interfaces.ElementName;
import helpers.interfaces.PageName;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

@Log4j2
public final class Refl {
    private final Class<?> pageClass;
    private final Object pageInstance;
    private final Map<String, Field> elementFields;

    public Refl(String pageName) {
        log.info("Инициализация Page Object: {}", pageName);

        this.pageClass = findPageClassByName(pageName);
        this.pageInstance = page(pageClass);
        this.elementFields = collectAnnotatedFields(pageClass);

        log.debug("Найдено {} элементов с аннотацией @ElementName в {}", elementFields.size(), pageName);
    }

    private Class<?> findPageClassByName(String pageName) {
        return new Reflections("pages")
                .getTypesAnnotatedWith(PageName.class, true).stream()
                .filter(clazz -> {
                    PageName annotation = clazz.getAnnotation(PageName.class);
                    return annotation != null && annotation.name().equals(pageName);
                })
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Page Object '{}' не найден!", pageName);
                    return new PageNotFoundException("Page for initialization is not found: " + pageName);
                });
    }

    private Map<String, Field> collectAnnotatedFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ElementName.class))
                .peek(field -> field.setAccessible(true))
                .collect(Collectors.toMap(
                        field -> {
                            ElementName annotation = field.getAnnotation(ElementName.class);
                            if (annotation == null) {
                                throw new IllegalStateException("Поле " + field.getName() + " помечено как @ElementName, но аннотация недоступна");
                            }
                            return annotation.name();
                        },
                        Function.identity()
                ));
    }

    public SelenideElement findElementByName(String elementName) {
        log.info("Поиск элемента '{}'", elementName);
        Field field = getFieldByName(elementName);

        try {
            return (SelenideElement) field.get(pageInstance);
        } catch (IllegalAccessException e) {
            throw new ElementAccessException("Ошибка доступа к элементу: " + elementName, e);
        }
    }

    public SelenideElement findElementByNameWithParams(String elementName, Object... params) {
        log.info("Поиск элемента '{}' с параметрами {}", elementName, Arrays.toString(params));
        Field field = getFieldByName(elementName);

        ElementName annotation = field.getAnnotation(ElementName.class);
        String xpathTemplate = annotation.xpathTemplate();

        if (xpathTemplate.isEmpty()) {
            throw new InvalidTemplateException("XPath-шаблон не задан для элемента '" + elementName + "'");
        }

        String finalXpath = MessageFormat.format(xpathTemplate, params);
        log.debug("Сформированный XPath: {}", finalXpath);
        return $(By.xpath(finalXpath));
    }

    public List<SelenideElement> findElementsByName(String elementsName) {
        log.info("Поиск элементов '{}'", elementsName);
        Field field = getFieldByName(elementsName);

        try {
            Object value = field.get(pageInstance);
            if (value instanceof List<?>) {
                return ((List<?>) value).stream()
                        .filter(SelenideElement.class::isInstance)
                        .map(SelenideElement.class::cast)
                        .collect(Collectors.toList());
            }
        } catch (IllegalAccessException e) {
            throw new ElementAccessException("Ошибка доступа к элементу: " + elementsName, e);
        }

        throw new ElementNotFoundException("Элементы с именем '" + elementsName + "' не найдены или не являются списком");
    }

    private Field getFieldByName(String name) {
        return Optional.ofNullable(elementFields.get(name))
                .orElseThrow(() -> new ElementNotFoundException("Элемент с именем '" + name + "' не найден в Page Object " + pageClass));
    }

    public static class PageNotFoundException extends RuntimeException {
        public PageNotFoundException(String message) {
            super(message);
        }
    }

    public static class ElementNotFoundException extends RuntimeException {
        public ElementNotFoundException(String message) {
            super(message);
        }
    }

    public static class ElementAccessException extends RuntimeException {
        public ElementAccessException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class InvalidTemplateException extends RuntimeException {
        public InvalidTemplateException(String message) {
            super(message);
        }
    }
}

