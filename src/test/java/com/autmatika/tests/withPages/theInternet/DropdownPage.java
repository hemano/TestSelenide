package com.autmatika.tests.withPages.theInternet;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.support.FindBy;

public class DropdownPage {


    @FindBy(id = "dropdown")
    private SelenideElement dropdown;

    public DropdownPage selectOption(String optionValue) {

        dropdown.selectOptionContainingText(optionValue);
        return this;
    }

    public SelenideElement getDropdown() {
        return dropdown;
    }

}
