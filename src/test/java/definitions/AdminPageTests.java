package definitions;

import io.cucumber.java.en.When;

public class AdminPageTests {
    private final StepDefinitions sd;

    public AdminPageTests() {
        this.sd = new StepDefinitions();
    }

    @When("Авторизация админом")
    public void adminAuthorisation() throws Exception {
        sd.sendTextInField("username", "Admin");
        sd.sendTextInField("пароль", "admin123");
        sd.clickElement("логин");
    }

}