package helpers.testrail.models;

import io.cucumber.plugin.event.Result;

public record FailedResult(int stepNumber, Result result) {
}
