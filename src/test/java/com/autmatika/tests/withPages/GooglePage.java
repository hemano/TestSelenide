package com.autmatika.tests.withPages;

import com.codeborne.selenide.SelenideElement;
import org.testng.Reporter;

import static com.codeborne.selenide.Selenide.page;

public class GooglePage {
    private SelenideElement q;

    public SearchResultsPage searchFor(String text) {
        Reporter.log("Search for text: " + text);
        q.val(text).pressEnter();
        return page(SearchResultsPage.class);
    }
}
