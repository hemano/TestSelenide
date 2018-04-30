package com.autmatika.tests.withPages.theInternet;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Reporter;

public class LoginPage {


    @FindBy(id = "username")
    private SelenideElement userNameText;

    @FindBy(id = "password")
    private SelenideElement passwordText;

    @FindBy(xpath = "//i[@class='fa fa-2x fa-sign-in']")
    private SelenideElement signInBtn;

    @FindBy(id = "flash")
    private SelenideElement successMessage;


    public LoginPage loginWithUserNameAndPassword(String username, String password) {

        userNameText.setValue(username);
        passwordText.setValue(password);
        signInBtn.click();

        Reporter.log("Entered username " + username + " and "  + " password " + password);
        return this;
    }

    public SelenideElement getSuccessMessage() {
        return successMessage;
    }
}
