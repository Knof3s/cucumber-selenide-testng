package runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import util.CustomTestListener;
import lombok.extern.log4j.Log4j2;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import helpers.testrail.TestRailExtension;


@CucumberOptions(
        tags = "@LoginTest",
        features = {"classpath:features"},
        glue = {"definitions"},
        plugin = {
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "ru.testit.listener.BaseCucumber7Listener"
        }

)
@Listeners({TestRailExtension.class, CustomTestListener.class}) // Добавляем адаптер
@Log4j2
public class CucumberRunnerTests extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }

}