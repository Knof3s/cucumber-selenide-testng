package pages;

import helpers.interfaces.ElementName;
import helpers.interfaces.PageName;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@PageName(name = "test_page")

public class LoginPage {

    /**
     * Пример использования локатора в который можно передать переменную, например "//div[@data-item-marker=34]"
     */
    @ElementName(name = "Маркер", xpathTemplate = "//div[@data-item-marker='%s']")
    private WebElement markerElement;

    // Локаторы для элементов на странице
    @ElementName(name = "username")
    @FindBy(name = "username")
    private WebElement usernameField;
    @ElementName(name = "пароль")
    @FindBy(name = "password")
    private WebElement passwordField;
    @ElementName(name = "логин")
    @FindBy(xpath = "//*[@class='oxd-form']/div[3]/button")
    private WebElement loginButton;
    @ElementName(name = "Сообщение с ошибкой")
    @FindBy(xpath = "//*[@class='orangehrm-login-error']/div[1]/div[1]/p")
    private WebElement errorMessage;
    @ElementName(name = "Хеддер домашней страницы")
    @FindBy(xpath = "//*[@class='oxd-topbar-header-breadcrumb']/h6")
    private WebElement homePageHeading;


}