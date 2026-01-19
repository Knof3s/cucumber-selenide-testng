package helpers.testrail.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Модель запроса на обновление результатов тестов в TestRail.
 */
@Data
@AllArgsConstructor
public class TestResultsRequest {
    public List<Result> results;

    @Data
    @AllArgsConstructor
    public static class Result {
        public int case_id;
        public int status_id;
        public String comment;
    }

}

