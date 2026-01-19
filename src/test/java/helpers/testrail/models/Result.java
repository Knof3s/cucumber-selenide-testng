package helpers.testrail.models;

/**
 * Модель результата тест-кейса в TestRail.
 */
public record Result(Integer runId, boolean isSuccessful, String exceptionMessage) {
}