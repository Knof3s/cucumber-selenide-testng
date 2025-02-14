package definitions;

import io.cucumber.java.After;
import io.cucumber.java.Before;


import static com.codeborne.selenide.Selenide.closeWebDriver;


public class InitialStep {

    @Before
    public void setUpTest() {

    }

    @After
    public void teardown() {
        closeWebDriver();
    }
}
