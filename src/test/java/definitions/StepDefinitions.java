package definitions;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import helpers.Refl;
import helpers.VariableStorage;
import helpers.reflects.PageContext;
import helpers.testrail.storage.TestrailMapStorage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebElement;
import util.ConfigReader;
import util.CustomAssert;
import util.DataFakerUtils;

import java.time.Duration;
import java.util.Map;

import static com.codeborne.selenide.Selenide.open;
import static util.ConfigReader.properties;


@Log4j2
public class StepDefinitions {
    private final PageContext pageContext;
    Duration duration = Duration.ofSeconds(20);

    public StepDefinitions() {
        this.pageContext = PageContext.getInstance(); // Получаем изолированный контекст
    }

    @Given("Открыть страницу - {string}")
    public void pageObjectActivator(String pageName) throws Exception {
        log.info("Открываем страницу {}", pageName);
        changePageContext(pageName);
        open(properties.getProperty(pageName));
    }

    @Given("Страница активна - {string}")
    @Given("Форма активна - {string}")
    public void changePageContext(String pageName) throws Exception {
        log.info("Меняем активный PageObject на {}", pageName);
        Refl refl = new Refl(pageName);
        pageContext.setRefl(refl); // Сохраняем объект в TestContext
    }

    @When("Отправить в элемент {string} текст {string}")
    public void sendTextInField(String elementName, String text) throws Exception {
        if (text.contains("config")) text = configKey(text);
        if (isSensitiveField(elementName)) {
            sendSensitiveTextInField(elementName, text);
            return;
        }
        log.info("Вводим текст в поле '{}'", elementName);
        log.debug("Текст для ввода: '{}'", text);
        pageContext.getRefl().findElementByName(elementName).should(Condition.visible, duration).sendKeys(text);
    }

    /**
     * Безопасно отправляет текст в элемент.
     * Для чувствительных полей (например, паролей) используется прямой вызов sendKeys через WebElement,
     * чтобы избежать отображения текста во вложенных шагах Allure.
     *
     * @param elementName имя элемента, в который нужно ввести текст
     * @param text    Текст для ввода
     */
    @When("Отправить в элемент {string} текст {string} секретные данные")
    public void sendSensitiveTextInField(String elementName, String text) throws Exception {
        log.info("Вводим конфиденциальный текст в поле '{}'", elementName);
        SelenideElement element = pageContext.getRefl().findElementByName(elementName).should(Condition.visible, duration);
        WebElement rawElement = element.toWebElement();
        rawElement.sendKeys(text);
    }

    @When("Нажать на элемент {string}")
    public void clickElement(String elementName) throws Exception {
        log.info("Нажатие на кнопку '{}'", elementName);
        pageContext.getRefl().findElementByName(elementName).should(Condition.visible, duration).click();
    }

    @When("Нажать на элемент {string} с параметром {string}")
    public void clickElementWithParams(String elementName, String params) {
        log.info("Нажатие на '{}' с маркером '{}'", elementName, params);
        pageContext.getRefl().findElementByNameWithParams(elementName, params).should(Condition.visible, duration).click();
    }

    @When("Проверка элемент {string} имеет текст {string}")
    public void assertFieldHasText(String elementName, String expectedText) throws Exception {
        String actualText = pageContext.getRefl().findElementByName(elementName).should(Condition.visible, duration).getText();
        log.info("Проверка текста в поле '{}'", elementName);
        log.debug("Ожидалось '{}', фактически '{}'", expectedText, actualText);
        CustomAssert.assertEquals(actualText, expectedText);
    }

    @When("Переменная - {string} запомнить текст из элемента страницы {string}")
    public void getElementTextToVariable(String variableName, String elementName) throws Exception {
        String elementValue = pageContext.getRefl().findElementByName(elementName).should(Condition.visible, duration).getText();
        VariableStorage.setVariable(variableName, elementValue);
    }

    @When("Отправить в элемент {string} текст из переменной {string}")
    public void fillElementWithVariableValue(String elementName, String variableName) throws Exception {
        sendTextInField(elementName, VariableStorage.getVariable(variableName, String.class));
    }

    @When("Отправить в элемент {string} фейк значением типа {string} для языка {string}")
    public void fillElementWithFakeValue(String elementName, String parameterType, String language) throws Exception {
        sendTextInField(elementName, DataFakerUtils.getFakeValue(parameterType, language));
    }

    @When("Ожидаем появления элемента {string} в течении {int} секунд")
    public void waitForElementVisible(String elementName, int intDuration) throws Exception {
        Duration exactDuration = Duration.ofSeconds(intDuration);
        log.info("Ожидаем появление элемента в течении '{}'", intDuration);
        pageContext.getRefl().findElementByName(elementName).should(Condition.visible, exactDuration);
    }

    @When("Ожидаем пропадание элемента {string} в течении {int} секунд")
    public void waitForElementDisappear(String elementName, int intDuration) throws Exception {
        Duration exactDuration = Duration.ofSeconds(intDuration);
        log.info("Ожидаем пропадание элемента в течении '{}'", intDuration);
        pageContext.getRefl().findElementByName(elementName).should(Condition.disappear, exactDuration);
    }

    @When("Шаг - {int} описание {string}")
    public void step(int stepCount, String stepInfo) {
        log.info("Шаг - {} описание шага: {}", stepCount, stepInfo);
        Map<Integer, String> strepResultMap = TestrailMapStorage.getTestrailStepResultMap();
        strepResultMap.put(stepCount, stepInfo);
        log.debug("strepResultMap: {}", strepResultMap);
    }

    private String configKey(String key) {
        key = key.replace("config.", "");
        log.info("Забираем из properties файла параметр '{}'", key);
        return ConfigReader.getProperty(key);
    }

    /**
     * Проверяет, является ли поле чувствительным (например, поле пароля или токена),
     * по ключевым словам в его имени.
     *
     * @param fieldName Название поля (например, "пароль", "password", "токен")
     * @return true, если поле считается чувствительным, иначе false
     */
    private boolean isSensitiveField(String fieldName) {
        return fieldName.toLowerCase().matches(".*(пароль|password|pwd|токен|token).*");
    }

}