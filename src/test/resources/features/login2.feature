Feature: Login to HRM Application

  Background:
    Given User is on HRMLogin page "https://opensource-demo.orangehrmlive.com/"

  @Prost
  Scenario: Login with valid credentials

    When User enters username as "Admin" and password as "admin123"
    Then User should be able to login successfully and new page open