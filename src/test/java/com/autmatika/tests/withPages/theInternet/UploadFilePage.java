package com.autmatika.tests.withPages.theInternet;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.support.FindBy;

import static com.codeborne.selenide.Condition.text;

public class UploadFilePage {


    @FindBy(id = "file-upload")
    private SelenideElement chooseFile;

    @FindBy(id = "file-submit")
    private SelenideElement submit;

    @FindBy(css = "div.example>h3")
    private SelenideElement fileUploadedMessage;

    public UploadFilePage uploadFile(String fileNameInResources) {
        chooseFile.uploadFromClasspath(fileNameInResources);
        submit.click();
        return this;
    }

    public void checkFileUploaded(){
        fileUploadedMessage.shouldHave(text("File Uploaded!"));
    }


}
