package helpers.testrail.storage;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


public class CaseIdsListStorage {
    @Getter
    @Setter
    private static List<Integer> validCaseIds = new ArrayList<>();

    @Getter
    @Setter
    private static List<Integer> skippedCaseIds = new ArrayList<>();

}
