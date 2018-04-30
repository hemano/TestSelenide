package com.autmatika.tests.withPages;

import com.codeborne.selenide.ElementsCollection;
import org.openqa.selenium.support.FindBy;
import org.testng.Reporter;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.CollectionCondition.texts;

public class SearchResultsPage {
    @FindBy(css = "#ires .g")
    private ElementsCollection results;

    public void checkResultsSize(int expectedSize) {

        results.shouldHave(sizeGreaterThan(expectedSize));
        Reporter.log("Results size is greater than "+ expectedSize);
    }

    public void checkResults(String... expectedTexts) {
        results.shouldHave(texts(expectedTexts));
    }

    public ElementsCollection getResults() {
        return results;
    }
}
