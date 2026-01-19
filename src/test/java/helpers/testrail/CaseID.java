package helpers.testrail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для указания идентификатора кейса в TestRail.
 * Может применяться к методам тестов.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CaseID {
    /**
     * Возвращает идентификатор тест-кейса в TestRail.
     *
     * @return ID тест-кейса
     */
    int value();
}
