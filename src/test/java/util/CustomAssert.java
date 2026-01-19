package util;

import lombok.extern.log4j.Log4j2;
import org.testng.Assert;

@Log4j2
public class CustomAssert {

    public static void assertEquals(Object actual, Object expected) {
        log.debug("assertEquals");

        validateSameType(actual, expected);

        try {
            Assert.assertEquals(actual, expected);
        } catch (AssertionError e) {
            log.error("Assertion failed: ожидали получить [{}], но получили [{}]", expected, actual);
            throw e;
        }
    }


    public static void assertNotEquals(Object actual, Object expected) {
        log.debug("assertNotEquals");

        validateSameType(actual, expected);

        try {
            Assert.assertNotEquals(actual, expected);
        } catch (AssertionError e) {
            log.error("Assertion failed: ожидали получить [{}] но получили [{}]", expected, actual);
            throw e;
        }
    }

    public static void assertTrue(boolean condition, String message) {
        try {
            Assert.assertTrue(condition);
        } catch (AssertionError e) {
            log.error("Assertion failed - {}: condition is false", message);
            throw e;
        }
    }

    private static void validateSameType(Object actual, Object expected) {
        if (!actual.getClass().equals(expected.getClass())) {
            String errorMessage = String.format(
                    "Типы переменных не совпадают: actual имеет тип %s, а expected имеет тип %s",
                    actual.getClass().getName(),
                    expected.getClass().getName()
            );
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }


}