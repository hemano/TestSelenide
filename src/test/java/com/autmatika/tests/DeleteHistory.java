package com.autmatika.tests;

import com.codeborne.selenide.Condition;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.switchTo;


public class DeleteHistory {

    @Test
    public void delete(){
        open("http://bamboo.apolloglobal.int/");
        $("#loginForm_os_username").setValue("hojha");
        $("#loginForm_os_password").setValue("India@100");
        $("#loginForm_save").click();


        $(By.linkText("Noah_Run_Tags_Sharepoint")).should(Condition.appear).click();
        $(By.linkText("History")).should(Condition.appear).click();


        while($(By.linkText("Delete")).isDisplayed()){
            $(By.linkText("Delete")).click();
            switchTo().alert().accept();
        }

    }
}
