package com.autmatika.tests.withPages.theInternet;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.support.FindBy;

import static com.codeborne.selenide.Selenide.page;

public class HomePage {


    @FindBy(linkText = "Form Authentication")
    private SelenideElement formAuthenticationLink;

    @FindBy(xpath = "//a[@href='/dropdown']")
    private SelenideElement dropdownLink;

    @FindBy(xpath = "//a[@href='/upload']")
    private SelenideElement fileUploadLink;

    public DropdownPage moveToDropdownPage(){
        dropdownLink.click();
        return page(DropdownPage.class);
    }

    public LoginPage moveToFormAuthenticationPage(){
        formAuthenticationLink.click();
        return page(LoginPage.class);
    }

    public UploadFilePage moveToFileUploadPage(){
        fileUploadLink.click();
        return page(UploadFilePage.class);
    }

}
