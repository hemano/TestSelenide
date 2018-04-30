package com.autmatika.tests;

import com.autmatika.tests.withPages.theInternet.HomePage;
import org.openqa.selenium.By;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utopia.sphnx.dataconversion.datagen.generator.DataGenerator;

import static com.codeborne.selenide.Condition.disappears;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;


public class TheInternetTest {


    @Test(dataProvider = "Authentication")
    public void invalidAuthenticationWithRandomData(String name, String password) {

        HomePage homePage = open("http://the-internet.herokuapp.com", HomePage.class);

        homePage.moveToFormAuthenticationPage()
                .loginWithUserNameAndPassword(name, password)
                .getSuccessMessage()
                .shouldHave(text("You logged into a secure area!"));
    }

    @DataProvider(name = "Authentication")
    private Object[][] getCredentials() {

//        return new Object[][] { { "testuser_1", "Test@123" }, { "testuser_1", "Test@123" }};

        Object[][] array = new Object[5][2];

        for (int i = 0; i < 5; i++) {

            String name = DataGenerator.getInstance().getFirstName().getKey();
            String password = DataGenerator.getInstance().getEmailAddress();

            array[i][0] = name;
            array[i][1] = password;
        }

        return array;
    }

    @Test
    public void testIfCorrectTextValueIsSelectedInDropdown() {

        HomePage homePage = open("http://the-internet.herokuapp.com", HomePage.class);

        homePage
                .moveToDropdownPage()
                .selectOption("Option 2")
                .getDropdown()
                .shouldHave(text("Option 2"));
    }

    @Test
    public void testIfUserCanUploadFiles() {

        HomePage homePage = open("http://the-internet.herokuapp.com", HomePage.class);

        homePage
                .moveToFileUploadPage()
                .uploadFile("SampleFileToUpload.txt")
                .checkFileUploaded();

    }

    @Test
    public void testTheDynamicLocators() {

        open("http://the-internet.herokuapp.com");
        $(By.linkText("Dynamic Loading")).click();
        $("div.example a:nth-of-type(1)").click();
        $("#start>button").click();

        $("#loading").waitUntil(disappears, 10000);

        $("div#finish>h4").shouldHave(text("Hello World!"));
    }


    @Test
    public void validAuthentication() {


        open("http://the-internet.herokuapp.com");
        $(By.linkText("Form Authentication")).click();
        $("#username").setValue("tomsmith");
        $("#password").setValue("SuperSecretPassword!");
        $(By.xpath("//i[@class='fa fa-2x fa-sign-in']")).click();

        $("#flash").shouldHave(text("You logged into a secure area!"));

    }


    @Test
    public void howSelenideWorks2() {


        open("http://the-internet.herokuapp.com");
        $(By.linkText("Form Authentication")).click();
        $("#username").setValue("tomsmith");
        $("#password").setValue("SuperSecretPassword!");
        $(By.xpath("//i[@class='fa fa-2x fa-sign-in']")).click();

        $("#flash").shouldHave(text("You logged into a secure area! blah blah blah"));


    }


}
