package helpers.testrail.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * Модель запроса на создание тестового прогона в TestRail.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddRunRequest {
    public int suite_id;
    public String name;
    public boolean include_all = false;
    public List<Integer> case_ids;
    public String refs;

    public AddRunRequest(String name, List<Integer> case_ids, String refs, int suite_id) {
        this.name = name;
        this.case_ids = case_ids;
        this.refs = refs;
        this.suite_id = suite_id;
    }

}
