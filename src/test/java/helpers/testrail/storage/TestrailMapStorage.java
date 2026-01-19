package helpers.testrail.storage;

import helpers.testrail.models.Result;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TestrailMapStorage {
    private static final Map<Integer, String> STEP_RESULT_MAP = new ConcurrentHashMap<>();
    private static final Map<Integer, Map<Integer, String>> CASE_ID_RESULT_MAP = new ConcurrentHashMap<>();
    private static final Queue<Map<Integer, Result>> RESULTS_COLLECTOR_QUEUE = new ConcurrentLinkedQueue<>();

    private TestrailMapStorage() {
    }

    public static Map<Integer, String> getTestrailStepResultMap() {
        return STEP_RESULT_MAP;
    }

    public static Map<Integer, Map<Integer, String>> getTestrailCaseIdResultMap() {
        return CASE_ID_RESULT_MAP;
    }

    public static Queue<Map<Integer, Result>> getResultsCollectorQueue() {
        return RESULTS_COLLECTOR_QUEUE;
    }

}


