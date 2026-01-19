Feature: Login to HRM Application

  Background:
    * Открыть страницу - "test_page"

  @LoginTest
  @ExternalId=externalId
  @DisplayName=integration_test
  @Title=integration_test
  @Description=description
  @WorkItemIds=436
  Scenario: integration_test
    * Шаг - 1 описание "зайти под админом на демо страницу"
    * Отправить в элемент "username" текст "Admin"
    * Отправить в элемент "пароль" текст "admin123"
    * Нажать на элемент "логин"
    * Шаг - 2 описание "проверить Хеддер домашней страницы"
    * Проверка элемент "Хеддер домашней страницы" имеет текст "Dashboard"

