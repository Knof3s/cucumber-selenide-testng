package definitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.testng.Assert;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class StepDefinitions {

    @Given("User is on HRMLogin page {string}")
    public void loginTest(String url) {
        open(url);
    }

    @When("User enters username as {string} and password as {string}")
    public void goToHomePage(String userName, String passWord) {

        // login to application
        $(By.name("username")).sendKeys(userName);
        $(By.name("password")).sendKeys(passWord);
        $(By.xpath("//*[@class='oxd-form']/div[3]/button")).submit();

    }

    @Then("User should be able to login successfully and new page open")
    public void verifyLogin() {

        String homePageHeading = $(By.xpath("//*[@class='oxd-topbar-header-breadcrumb']/h6")).getText();

        //Verify new page - HomePage
        Assert.assertEquals(homePageHeading, "Dashboard");

    }

    @Then("User should be able to see error message {string}")
    public void verifyErrorMessage(String expectedErrorMessage) {

        String actualErrorMessage = $(By.xpath("//*[@class='orangehrm-login-error']/div[1]/div[1]/p")).getText();

        // Verify Error Message
        Assert.assertEquals(actualErrorMessage, expectedErrorMessage);

    }
}
