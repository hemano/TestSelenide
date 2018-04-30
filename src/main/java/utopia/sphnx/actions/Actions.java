package utopia.sphnx.actions;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.Uninterruptibles;
import conversion.setup.Variables;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.*;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import utopia.sphnx.config.ParseConfigurations;
import utopia.sphnx.core.support.xmlmapping.controlmap.Control;
import utopia.sphnx.dataconversion.datagen.GenerateData;
import utopia.sphnx.dataconversion.datagen.execution.ExecutionContext;
import utopia.sphnx.dataconversion.datagen.metadata.MetaData;
import utopia.sphnx.dataconversion.parsing.Parser;
import utopia.sphnx.logging.LoggerReporter;
import utopia.sphnx.reports.Report;

import javax.annotation.Nullable;
import java.io.*;
import java.math.RoundingMode;
import java.net.HttpCookie;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static utopia.sphnx.logging.LoggerReporter.LOGNREPORT;

/**
 * Created by jitendrajogeshwar on 31/05/17.
 */
public class Actions extends BaseActions implements ActionsController {

    //region variables
    private static Connection connection = null;
    private static Statement statement = null;
    private static ResultSet resultSet = null;
    private static ParseConfigurations configurations;

    /**
     * The Constant TO_MILLIS.
     */
    private static final int TO_MILLIS = 1000;

    /**
     * The Constant THREAD_SLEEP.
     */
    private static final int THREAD_SLEEP = 100;

    /**
     * The Constant XPATH.
     */
    private static final String XPATH = "xpath";

    /**
     * The Constant CSS.
     */
    private static final String CSS = "css";

    /**
     * The Constant Class.
     */
    private static final String CLASS = "class";

    /**
     * The Constant ID.
     */
    private static final String ID = "id";

    /**
     * The Constant LINKTEXT.
     */
    private static final String LINKTEXT = "linktext";

    /**
     * The Constant TAGNAME.
     */
    private static final String TAGNAME = "tagname";

    /**
     * The Constant NAME.
     */
    private static final String NAME = "name";

    public static boolean didPass = false;

    private long waitForPageToLoad = 120;
    private long waitForElement = 10;
    private long waitForElementInvisibility = 10;
    private long pollingTime = 500;

    private static String ACTIONS = utopia.sphnx.actions.Actions.class.getCanonicalName();
    private String LOCATOR = "<UNKNOWN>";
    private String FULLNAME = "<UNKNOWN>";

    private boolean isTestngTests;

    //endregion

    public Actions(ApplicationContext applicationContext) {
        super(applicationContext);
        configurations = new ParseConfigurations();
    }

    public Actions(ApplicationContext applicationContext, boolean isTestngTests) {
        super(applicationContext);
        configurations = new ParseConfigurations();
        this.isTestngTests = isTestngTests;
    }

    public Actions(WebDriver webDriver) {
        super(webDriver);
        configurations = new ParseConfigurations();
    }


    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * waitForPageLoading(java.lang.String)
     */
    @Override
    public void waitForCondition(String jscondition) throws InterruptedException {
        waitForCondition(jscondition, getWaitForElement());
    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * waitForCondition(java.lang.String, int)
     */
    @Override
    public void waitForCondition(String jscondition, long waitSeconds) throws InterruptedException {
        try {
            JavascriptExecutor js = (JavascriptExecutor) this.webDriver;
            boolean conditionResult;
            long startTime = System.currentTimeMillis();
            do {
                conditionResult = ((Boolean) js.executeScript(jscondition)).booleanValue();
                try {
                    Thread.sleep(THREAD_SLEEP);
                } catch (InterruptedException e) {
                    LOGNREPORT.sphnxError(ACTIONS, e.getMessage());
                    if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
                }
            } while (!conditionResult && System.currentTimeMillis() - startTime <= waitSeconds * TO_MILLIS);
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in waitForCondition with condition:" + jscondition + " and timeout: " + waitSeconds);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
        }
    }

    /**
     * Find locator substring.
     *
     * @param locator the element locator
     * @return the string after the character '='
     */
    private String findLocatorSubstring(String locator) {
        return locator.substring(locator.indexOf('=') + 1);
    }

    /**
     * waitForelement wait till the element appear on the page
     *
     * @param locator an element locator
     * @return
     */
    @Override
    public WebElement waitForElement(String locator) {
        return waitForElement(locator, getWaitForElement());
    }

    /**
     * waitForelement wait till the element appear on the page
     *
     * @param locator an element locator
     * @return
     */

    public WebElement waitForElement(By locator) {
        return waitForElement(locator, getWaitForElement());
    }

    /**
     * (non-Javadoc)
     *
     * @see (String, int)
     */
    public WebElement waitForElement(By locator, long waitSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(this.webDriver, waitSeconds);
            return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in waitForElement with locator:" +
                    locator + " and timeout: " + waitSeconds);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return null;
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see (String, int)
     */
    public WebElement waitForElement(String locator, long waitSeconds) {
        // to-do
        // We need to add logic to check first that the locator we have provided is valid

        try {
            String parent = getParent(locator);
            if (parent != null && !parent.equals("")) {

                String parentWindowHandler = webDriver.getWindowHandle(); // Store your parent window
                if (parent.equalsIgnoreCase("popup")) {
                    String subWindowHandler = null;

                    Set<String> handles = webDriver.getWindowHandles(); // get all window handles
                    Iterator<String> iterator = handles.iterator();
                    while (iterator.hasNext()) {
                        subWindowHandler = iterator.next();
                    }
                    webDriver.switchTo().window(subWindowHandler); // switch to popup window
                } else {
                    webDriver.switchTo().defaultContent();
                }

                handleFrames(parent);
            }
            WebDriverWait wait = new WebDriverWait(this.webDriver, waitSeconds);
            return wait.until(ExpectedConditions.visibilityOfElementLocated(getLocator(locator)));
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in waitForElement with locator:" +
                    locator + " and timeout: " + waitSeconds);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return null;
        }
    }

    /**
     * Waits for a page to load completely
     *
     * @param timeoutSeconds: the integer value that specifies the timeout
     */
    public void waitForPageLoad(int timeoutSeconds) {
        try {
            Wait<WebDriver> wait = new WebDriverWait(webDriver, timeoutSeconds, 500).ignoring(WebDriverException.class);
            wait.until(new Function<WebDriver, Boolean>() {
                public Boolean apply(WebDriver driver) {
                    return String.valueOf(((JavascriptExecutor) driver).executeScript("return document.readyState"))
                            .equals("complete");
                }
            });
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "waitForPageLoad Failed with timeout: " + timeoutSeconds);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
        }
    }

    /**
     * waitForElementInvisibility method wait till the element disappear on the screen
     *
     * @param locator the element locator
     */
    @Override
    public void waitForElementInvisibility(String locator) {
        // to-do
        // We need to add logic to check first that the locator we have provided is valid
        waitForElementInvisibility(locator, getWaitForElementInvisibility());
    }

    /**
     * waitForElementInvisibility method wait till the element disappear on the screen
     *
     * @param locator     the element locator
     * @param waitSeconds time to wait in seconds, for element to become invisible
     */
    @Override
    public void waitForElementInvisibility(String locator, long waitSeconds) {
        // to-do
        // We need to add logic to check first that the locator we have provided is valid
        try {
            WebDriverWait wait = new WebDriverWait(this.webDriver, waitSeconds);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(getLocator(locator)));
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "waitForElementInvisibility Failed with locator: " + locator + " timeout: " + waitSeconds);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
        }

    }

//    /**
//     * waitForElementInvisibility method wait till the element disappear on the screen
//     *
//     * @param element     the element locator
//     * @param waitSeconds time to wait in seconds, for element to become invisible
//     */
//    public void waitForElementInvisibility(WebElement element, long waitSeconds) {
//        try {
//            WebDriverWait wait = new WebDriverWait(this.webDriver, waitSeconds);
//            wait.until(ExpectedConditions.invisibilityOf(element));
//        } catch (Exception e) {
//            LOGNREPORT.sphnxFAIL(ACTIONS, "waitForElementInvisibility timeout: " + waitSeconds);
//            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
//        }
//    }

    /**
     * waitForElementPresense method wait till the element is visible on the page
     *
     * @param locator the element locator
     * @return
     */
    public WebElement waitForElementPresence(String locator) {
        // to-do
        // We need to add logic to check first that the locator we have provided is valid

        return waitForElementPresence(locator, getWaitForElement());
    }

    /**
     * waitForElementOresense method wait till the element appear on page within a specified time
     *
     * @param locator     the locator
     * @param waitSeconds time to wait in seconds, for element to become present
     * @return
     */
    public WebElement waitForElementPresence(String locator, long waitSeconds) {
        // to-do
        // We need to add logic to check first that the locator we have provided is valid
        try {
            WebDriverWait wait = new WebDriverWait(this.webDriver, waitSeconds);
            return wait.until(ExpectedConditions.presenceOfElementLocated(getLocator(locator)));
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "waitForElementPresence Failed with locator: " + locator + " timeout: " + waitSeconds);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return null;
        }
    }

    public WebElement findElementIn(String parentLocator, String locator) throws Exception {
        try{
            By parentBy = (By) applicationContext.getBean("lookUp", parentLocator);
            this.waitForElementToBeVisible(parentBy);
            return this.webDriver.findElement(parentBy).findElement(getLocator(locator));
        }
        catch (Exception e){
            LOGNREPORT.sphnxError(ACTIONS, "Element " + locator + " could not be located in " + getLocator(locator),
                    "red", 1, true);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return null;

        }
    }
    
    /**
     * findElements method find the numbers of elements on the page
     *
     * @param locator the element locator
     * @return
     */
    @Override
    public List<WebElement> findElements(String locator) {
        // to-do
        // We need to add logic to check first that the locator we have provided is valid
        try {
            WebDriverWait wait = new WebDriverWait(this.webDriver, getWaitForElement());
            return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(getLocator(locator)));
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "findElements Failed with locator: " + locator);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return null;
        }
    }

    /**
     * findElements method find the numbers of elements on the page
     *
     * @param locator the element locator
     * @return
     */

    public List<WebElement> findElements(By locator) {
        try {
            WebDriverWait wait = new WebDriverWait(this.webDriver, getWaitForElement());
            return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "findElements Failed with locator: " + locator);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return null;
        }

    }

    /**
     * Returns the WebElement after waiting
     *
     * @param locator
     * @return
     */
    public WebElement findElement(String locator) {
        return waitForElement(locator);
    }

    /**
     * Returns the WebElement after waiting
     *
     * @param locator
     * @return
     */
    public WebElement findElement(By locator) {
        return waitForElement(locator);
    }


    /**
     * enterText method Enter the value in the text box or the input tag
     *
     * @param locator the element locator
     * @param value   the value you want to type in
     * @return
     */
    @Override
    public boolean enterText(String locator, String value) {
        if (isValidLocator(locator)) {
            LOGNREPORT.sphnxInfo(ACTIONS, FULLNAME + " locator found in ControlMap: " + LOCATOR, "blue", 1, false);
            waitForPageLoad();

            String parent = getParent(locator);
            String parentWindowHandler = "";

            if (parent != null && !parent.equals("")) {

                parentWindowHandler = webDriver.getWindowHandle(); // Store your parent window
                if (parent.equalsIgnoreCase("popup")) {
                    String subWindowHandler = null;

                    Set<String> handles = webDriver.getWindowHandles(); // get all window handles
                    Iterator<String> iterator = handles.iterator();
                    while (iterator.hasNext()) {
                        subWindowHandler = iterator.next();
                    }
                    webDriver.switchTo().window(subWindowHandler); // switch to popup window
                } else {
                    webDriver.switchTo().defaultContent();
                }

                handleFrames(parent);
            }

            handleFrames(parent);

            WebDriverWait wait = new WebDriverWait(webDriver, waitForElement);
            try {
                WebElement element = wait
                        .until(ExpectedConditions.elementToBeClickable(getLocator(locator)));

                outline(element);
                removeOutline(element);
                element.clear();
                element.sendKeys(value);

                //LOGNREPORT.sphnxPASS(ACTIONS, "enterText() " + locator);
                if (!parentWindowHandler.equals("")) {
                    webDriver.switchTo().window(parentWindowHandler);
                }
                return true;
            } catch (TimeoutException t) {
                try {
                    if (configurations.getAllConfigurations()
                            .get(ParseConfigurations.Configs.RUN_HEADLESS.name())
                            .equalsIgnoreCase("true")) {
                        Dimension d = new Dimension(1920, 1080);
                        webDriver.manage().window().setSize(d);
                        WebElement element = webDriver.findElement(getLocator(locator));
                        element.clear();
                        element.sendKeys(value.toString());
                        if (!parentWindowHandler.equals("")) {
                            webDriver.switchTo().window(parentWindowHandler);
                        }
                        return true;
                    }
                    LOGNREPORT.sphnxFAIL(ACTIONS, "enterText() " + locator + " - TIMEOUT: Failed to locate the control on the page.");
                    LOGNREPORT.sphnxFAIL(ACTIONS, (FULLNAME + " locator is not valid: " + LOCATOR));
                    if (configurations.getAllConfigurations().containsKey("iscomplex")) throw t;
                    return false;

                } catch (Exception s) {
                    webDriver.switchTo().defaultContent();
                    LOGNREPORT.sphnxFAIL(ACTIONS, "enterText() " + locator + " - Failed to locate the control on the page.");
                    LOGNREPORT.sphnxFAIL(ACTIONS, (FULLNAME + " locator is not valid: " + LOCATOR));
                    if (configurations.getAllConfigurations().containsKey("iscomplex")) throw s;
                    return false;
                }
            } catch (Exception e) {
                webDriver.switchTo().defaultContent();
                LOGNREPORT.sphnxFAIL(ACTIONS, "enterText() " + locator + " - Failed to locate the control on the page.");
                LOGNREPORT.sphnxFAIL(ACTIONS, (FULLNAME + " locator was not found on page: " + LOCATOR));
                if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
                return false;
            }
        }
        return false;
    }

    public boolean enterText(String locator, Keys key) {
        try {
            WebElement element = waitForElement(locator);
            element.sendKeys(key);
            return true;
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in enterText with locator:" + locator);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }


    /**
     * (non-Javadoc)
     * <p>
     * org.openqa.selenium.String)
     */
    @Override
    public boolean press(String locator) {
        if (isValidLocator(locator)) {
            LOGNREPORT.sphnxInfo(ACTIONS, (FULLNAME + " locator found in ControlMap: " + LOCATOR), "blue", 1, false);
            try {
                waitForElement(locator).click();
                return true;
            } catch (Exception e) {
                LOGNREPORT.sphnxError(ACTIONS, (FULLNAME + " locator was not found on page: " + LOCATOR));
                if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
                return false;
            }
        }
        return false;
    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * pressAndWaitForPageToLoad(java.lang.String)
     */
    @Override
    public void pressAndWaitForPageToLoad(String locator) {
        press(locator);
    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * pressAndWaitForElement(java.lang.String, java.lang.String, int)
     */
    @Override
    public void pressAndWaitForElement(String pressLocator, String elementToWaitLocator, long waitSeconds) {
        press(pressLocator);
        waitForElement(elementToWaitLocator, waitSeconds);
    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * pressAndWaitForElement(java.lang.String, java.lang.String)
     */
    @Override
    public void pressAndWaitForElement(String pressLocator, String elementToWaitLocator) {
        pressAndWaitForElement(pressLocator, elementToWaitLocator, getWaitForElement());
    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * pressAndClickOkInAlert(java.lang.String)
     */
    @Override
    public void pressAndClickOkInAlert(String locator) {
        press(locator);
        clickOkInAlert();

    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * pressAndClickOkInAlertNoPageLoad(java.lang.String)
     */
    @Override
    public void pressAndClickOkInAlertNoPageLoad(String locator) {
        pressAndClickOkInAlert(locator);
    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * pressAndClickCancelInAlert(java.lang.String)
     */
    @Override
    public void pressAndClickCancelInAlert(String locator) {
        press(locator);
        clickCancelInAlert();
    }

    /**
     * (non-Javadoc)
     * <p>
     * (java.lang.String, java.lang.String)
     */
    @Override
    public void multiSelectAdd(String locator, String option) {

    }

    /**
     * clear method clear the value entered in the input tag
     *
     * @param locator the locator of the input field you want to clear
     * @return
     */
    @Override
    public boolean clear(String locator) {
        try {
            WebElement element = waitForElement(locator);
            element.clear();
            return true;
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, ("Exception executing clear with locator:" + locator));
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }

    /**
     * (non-Javadoc)
     */
    @Override
    public org.openqa.selenium.interactions.Actions getBuilder() {
        return new org.openqa.selenium.interactions.Actions(this.webDriver);
    }

    /**
     * (non-Javadoc)
     */
    @Override
    public boolean mouseOver(String locator) {
        try {
            getBuilder().moveToElement(waitForElement(locator)).perform();
            return true;
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, ("Exception mouseOver clear with locator:" + locator));
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }

    /**
     * (non-Javadoc)
     */
    @Override
    public void mouseUp(String locator) {
        getBuilder().release(waitForElement(locator)).perform();
    }

    /**
     * (non-Javadoc)
     * <p>
     * (java.lang.String)
     */
    @Override
    public void mouseDown(String locator) {
        getBuilder().clickAndHold(waitForElement(locator)).perform();
    }

    /**
     * doubleClick method perform double click on the provided locator
     *
     * @param locator locator of the element where double click is performed
     * @return
     */
    @Override
    public boolean doubleClick(String locator) {
        try {
            getBuilder().doubleClick(waitForElement(locator)).perform();
            return true;
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, ("Exception executing doubleClick with locator:" + locator));
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }

    /**
     * click method perform click action on the provided locator
     *
     * @param locator the locator of the element(i.e link, button, etc) to perform the click action
     * @return
     */
    public boolean click(String locator) {
        if (isValidLocator(locator)) try {
            LOGNREPORT.sphnxInfo(ACTIONS, (FULLNAME + " locator found in ControlMap: " + LOCATOR), "blue", 1, false);
            waitForPageLoad();
            String parent = getParent(locator);
            handleFrames(parent);

            WebElement element = waitForElement(locator);

            element.click();
            return true;
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, ("Exception executing click with locator:" + locator));
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
        return false;
    }

    /**
     * We need to validate the locator for every method that interacts with a control
     */
    public boolean isValidLocator(String locator) {
        // added to skip the validation for the purpose of Testng tests
        if(isTestngTests){
            return true;
        }

        boolean isValid = false;
        List<Control> controls = (List<Control>) applicationContext.getBean("controlMap");
        try {
            int i = controls.size();
            for (Control control : controls) {
                String controlFullName = control.getContext() + "." + control.getLogicalName();
                if (controlFullName.equalsIgnoreCase(locator)) {
                    controlType = control.getObjectType();
                    controlDescriptor = control.getDescriptor();
                    LOCATOR = control.getDescriptor();
                    FULLNAME = controlFullName;
                    return true;
                }
            }
            LOGNREPORT.sphnxError(ACTIONS, ("Locator was not found in ControMap: " + locator));
            return false;
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, ("Exception executing isValidLocator with locator:" + locator));
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }


    /**
     * clicks on the element and wait for page to load
     *
     * @param locator
     */
    public void clickAndWaitForPage(String locator) {
        try {
            waitForElement(locator).click();
            waitForPageLoad();
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, ("Exception executing clickAndWaitForPage with locator:" + locator));
            throw e;
        }
    }

    /**
     * selects the element if it is not already selected
     *
     * @param locator
     */
    public boolean selectCheckBox(String locator) {
        try {
            WebElement element = waitForElement(locator);

            if (!element.isSelected()) {
                element.click();
            }
            return true;
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, ("Exception executing selectCheckBox with locator:" + locator));
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }

    /**
     * Validates that the checkbox is unchecked.
     *
     * @param locator
     */
    public boolean validateCheckboxIsUnchecked(String locator) {
        try {
            WebElement element = waitForElement(locator);

            if (!element.isSelected()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, ("Exception executing validateCheckboxIsUnchecked with locator:" + locator));
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }

    /**
     * Validates that the checkbox is checked.
     *
     * @param locator
     */
    public boolean validateCheckboxIsChecked(String locator) {
        try {
            WebElement element = waitForElement(locator);

            if (element.isSelected()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, ("Exception executing validateCheckboxIsChecked with locator:" + locator));
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }


    /**
     * selects the element if it is not already selected
     *
     * @param locator
     */
    public boolean selectCheckBox(String locator, String option) {
        try {
            WebElement element = waitForElement(locator);

            if ((!element.isSelected() && option.toLowerCase().equals("check")) ||
                    (element.isSelected() && option.toLowerCase().equals("uncheck"))) {
                element.click();
            }
            return true;
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, ("Exception executing selectCheckBox with locator:" + locator));
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }

    /**
     * selects the element if it is not already selected
     *
     * @param locator
     */
    public boolean selectRadioButton(String locator) {
        try {
            WebElement element = waitForElement(locator);
            outline(element);
            removeOutline(element);
            if (!element.isSelected()) {
                element.click();
            }
            return true;
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, ("Exception executing selectRadioButton with locator:" + locator));
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }

    /**
     * Find whether element exists or not
     *
     * @param locator
     * @return
     */
    public Optional<WebElement> elementExists(String locator) {
        List<WebElement> list = findElements(locator);
        if (list.size() >= 1) {
            return Optional.of(list.get(0));
        } else if (list.isEmpty()) {
            return Optional.absent();
        }
        return null;
    }
    /**
     * Find whether element exists or not
     *
     * @param locator
     * @return
     */
    public boolean ifElementExists(String locator) {
        try {
            WebElement element = findWebDriverElement(locator);
            return true;
        } catch (Exception e) {
            try {
                List<WebElement> list = findWebDriverElements(locator);
                if(list.size()>=1) {
                    return true;
                }
            } catch (Exception e1) {
                return false;
            }
        }
        return false;
    }

    /**
     * (non-Javadoc)
     * <p>
     * (java.lang.String, java.lang.String)
     */
    @Override
    public void typeKeys(String locator, String value) {
        waitForElement(locator).sendKeys(value);
    }

    /**
     * (non-Javadoc)
     * <p>
     * (java.lang.String, org.openqa.selenium.Keys)
     */
    @Override
    public void keyDown(String locator, CharSequence thekey) {
//        getBuilder().keyDown(waitForElement(locator), thekey).perform();

    }

    /**
     * (non-Javadoc)
     * <p>
     * java.lang.String, org.openqa.selenium.Keys)
     */
    @Override
    public void keyUp(String locator, CharSequence thekey) {
//        getBuilder().keyUp(waitForElement(locator), thekey).perform();
    }

    /**
     * (non-Javadoc)
     * <p>
     * (org.openqa.selenium.Keys)
     */
    @Override
    public void keyDown(CharSequence thekey) {
//        getBuilder().keyDown(thekey).perform();
    }

    /**
     * (non-Javadoc)
     * <p>
     * org.openqa.selenium.Keys)
     */
    @Override
    public void keyUp(CharSequence thekey) {
//        getBuilder().keyUp(thekey).perform();
    }

    /**
     * (non-Javadoc)
     * <p>
     * (org.openqa.selenium.Keys)
     */
    @Override
    public void keyPress(CharSequence thekey) {
        getBuilder().sendKeys(thekey).perform();
    }

    /**
     * (non-Javadoc)
     */
    @Override
    public void keyPress(String locator, CharSequence thekey) {
        waitForElement(locator).sendKeys(thekey);

    }

    /**
     * Highlight.
     *
     * @param element the element
     */
    public void highlight(WebElement element) {
        executeJavascript("arguments[0].style.backgroundColor = 'rgb(255, 255, 0)'", element);
    }


    /**
     * Outline.
     *
     * @param element the element
     */
    public void outline(WebElement element) {
        executeJavascript("arguments[0].style.border = '3px solid red'", element);
    }

    /**
     * Outline.
     *
     * @param element the element
     */
    public void removeOutline(WebElement element) {
        executeJavascript("arguments[0].style.border = 'none'", element);
    }

    /**
     * (non-Javadoc)
     * <p>
     * (org.openqa.selenium.String)
     */
    @Override
    public boolean highlight(String locator) {
        try {
            executeJavascript("arguments[0].style.backgroundColor = 'rgb(255, 255, 0)'", waitForElement(locator));
            return true;
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, ("Exception highlight with locator:" + locator));
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }

    /**
     * (non-Javadoc)
     * <p>
     * (org.openqa.selenium.String)
     */
    public boolean outline(String locator) {
        try {
            executeJavascript("arguments[0].style.border = '3px solid red'", waitForElement(locator));
            return true;
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, ("Exception outline with locator:" + locator));
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }

    /**
     * (non-Javadoc)
     * <p>
     * (org.openqa.selenium.String)
     */
    public boolean removeOutline(String locator) {
        try {
            executeJavascript("arguments[0].style.border = 'arguments[0].style.border = 'none'", waitForElement(locator));
            return true;
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, ("Exception removeOutline with locator:" + locator));
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }

    /**
     * (non-Javadoc)
     * <p>
     * (java.lang.String, java.lang.String)
     */
    @Override
    public boolean highlight(String locator, String color) {
        try {
            executeJavascript("arguments[0].style.backgroundColor = arguments[1]", waitForElement(locator), color);
            return true;
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, ("Exception highlight with locator:" + locator + " color:" + color));
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }

    /**
     * (non-Javadoc)
     * <p>
     * (java.lang.String, java.lang.String)
     */
    public boolean outline(String locator, String color) {
        try {
            executeJavascript("arguments[0].style.border = '3px solid ' + arguments[1]", waitForElement(locator), color);
            return true;
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, ("Exception outline with locator:" + locator + " color:" + color));
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }

    /**
     * getText method return the text value from the element
     *
     * @param locator of the element
     * @return
     */
    @Override
    public String getText(String locator) {
        return waitForElement(locator).getText();
    }

    public String getText(By selector){
        return waitForElement(selector).getText();
    }
    /**
     * (non-Javadoc)
     * <p>
     * (java.lang.String)
     */
    @Override
    public void getFocus(String locator) {
        WebElement element = waitForElement(locator);
        if ("input".equals(element.getTagName())) {
            element.sendKeys("");
        } else {
            new org.openqa.selenium.interactions.Actions(this.webDriver).moveToElement(element).perform();

        }
    }


    /**
     * (non-Javadoc)
     * <p>
     * (org.openqa.selenium.String)
     */
    @Override
    public String getInputValue(String locator) {
        return waitForElement(locator).getAttribute("value");
    }

    /**
     * isAlertPresent method return True if Alert is present
     *
     * @return
     */
    @Override
    public boolean isAlertPresent() {
        WebDriverWait wait = new WebDriverWait(this.webDriver, this.getWaitForElement());
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * isTextPresent method check the Text is present in the page
     *
     * @param value the text value you want to check for presence
     * @return
     */
    @Override
    public boolean isTextPresent(String value) {
        return this.webDriver.getPageSource().contains(value);
    }

    /**
     * isTextNotPresent method check the Text should not present on page
     *
     * @param value the text value you want to check
     * @return
     */
    @Override
    public boolean isTextNotPresent(String value) {
        return !this.webDriver.getPageSource().contains(value);
    }

    /**
     * (non-Javadoc)
     */
    @Override
    public String getPageSource() {
        return this.webDriver.getPageSource();
    }


    /**
     * (non-Javadoc)
     */
    @Override
    public void sleep(String millis) {
        try {
            Thread.sleep(Integer.parseInt(millis));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * isComponentEditable method return true if locator is enabled on page
     *
     * @param locator the locator of the element
     * @return
     */
    @Override
    public boolean isComponentEditable(String locator) {
        return waitForElement(locator).isEnabled();
    }

    /**
     * isComponentDisabled method true if locat is not enable on page
     *
     * @param locator the locator of the element you want to chek
     * @return
     */
    @Override
    public boolean isComponentDisabled(String locator) {
        return !waitForElement(locator).isEnabled();
    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * isComponentPresent(org.openqa.selenium.String)
     */
    @Override
    public boolean isComponentPresent(String locator) {
        return this.webDriver.findElements(getLocator(locator)).size() != 0;
    }

    /**
     * Switch to window.
     */
    public void switchToWindow() {
        Set<String> availableWindows = this.webDriver.getWindowHandles();
        Iterator<String> itr = availableWindows.iterator();
        String lastElement = itr.next();
        while (itr.hasNext()) {
            lastElement = itr.next();
        }
        this.webDriver.switchTo().window(lastElement);
    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * isComponentPresent(java.lang.String, int)
     */
    @Override
    public boolean isComponentPresent(String locator, long seconds) {
        try {
            WebDriverWait wait = new WebDriverWait(this.webDriver, THREAD_SLEEP);
            wait.until(ExpectedConditions.presenceOfElementLocated(getLocator(locator)));
            return true;
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, ("Exception executing isComponentPresent with locator:" + locator + " and timeout: " + seconds));
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;

        }
    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * isComponentNotPresent(org.openqa.selenium.String)
     */
    @Override
    public boolean isComponentNotPresent(String locator) {
        return this.webDriver.findElements(getLocator(locator)).size() == 0;
    }

    /**
     * isComponentVisible method check the locator is present and also Displayed on the page
     *
     * @param locator the locator of the element
     * @return
     */
    @Override
    public boolean isComponentVisible(String locator) {
        return isComponentPresent(locator) && this.webDriver.findElement(getLocator(locator)).isDisplayed();
    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * isComponentVisible(java.lang.String, int)
     */
    @Override
    public boolean isComponentVisible(String locator, long seconds) {
        try {
            WebDriverWait wait = new WebDriverWait(this.webDriver, seconds);
            wait.until(ExpectedConditions.visibilityOfElementLocated(getLocator(locator)));
            return true;
        } catch (TimeoutException e) {
            LOGNREPORT.sphnxError(ACTIONS, ("Exception executing isComponentVisible with locator:" + locator + " timeout: " + seconds));
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * isComponentNotVisible(org.openqa.selenium.String)
     */
    @Override
    public boolean isComponentNotVisible(final String locator) {
        return !isComponentVisible(locator);
    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * isComponentNotVisible(java.lang.String, int)
     */
    @Override
    public boolean isComponentNotVisible(String locator, long seconds) {
        try {
            WebDriverWait wait = new WebDriverWait(this.webDriver, seconds);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(getLocator(locator)));
            return true;
        } catch (TimeoutException e) {
            LOGNREPORT.sphnxError(ACTIONS, ("Exception executing isComponentNotVisible with locator:" + locator + " timeout: " + seconds));
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * isComponentSelected(org.openqa.selenium.String)
     */
    @Override
    public boolean isComponentSelected(String locator) {
        return waitForElement(locator).isSelected();
    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * isComponentNotSelected(org.openqa.selenium.String)
     */
    @Override
    public boolean isComponentNotSelected(String locator) {
        return !waitForElement(locator).isSelected();
    }

    /**
     * Wait for alert.
     *
     * @return the alert
     */
    public Alert waitForAlert() {
        try {
            WebDriverWait wait = new WebDriverWait(this.webDriver, getWaitForElement());
            return wait.until(ExpectedConditions.alertIsPresent());
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in waitForAlert timeout: " + getWaitForElement());
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return null;
        }
    }

    /**
     * waitForAlert method accept the Alert
     */
    public void clickOkInAlert() {
        try {
            waitForAlert().accept();
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in clickOkInAlert");
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
        }

    }

    /**
     * clickCancelInAlert method click cancel on Alert
     */
    public void clickCancelInAlert() {
        try {
            waitForAlert().dismiss();
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in clickCancelInAlert");
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
        }
    }

    /**
     * Gets the full xpath.
     *
     * @param locator the locator
     * @return the full xpath
     */
    public String getFullXpath(String locator) {
        WebElement element = waitForElement(locator);
        try {
            String js = "gPt=function(c){if(c.id!==''){return'id(\"'+c.id+'\")'}if(c===document.body){return c.tagName}var a=0;var e=c.parentNode.childNodes;for(var b=0;b<e.length;b++){var d=e[b];if(d===c){return gPt(c.parentNode)+'/'+c.tagName+'['+(a+1)+']'}if(d.nodeType===1&&d.tagName===c.tagName){a++}}};return gPt(arguments[0]).toLowerCase()";
            return "//" + executeJavascript(js, element);
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in clickCancelInAlert");
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return null;
        }
    }

    /**
     * Gets the full xpath.
     *
     * @param element the Webelement
     * @return the full xpath
     */
    public String getFullXpath(WebElement element) {
        String js = "gPt=function(c){if(c.id!==''){return'id(\"'+c.id+'\")'}if(c===document.body){return c.tagName}var a=0;var e=c.parentNode.childNodes;for(var b=0;b<e.length;b++){var d=e[b];if(d===c){return gPt(c.parentNode)+'/'+c.tagName+'['+(a+1)+']'}if(d.nodeType===1&&d.tagName===c.tagName){a++}}};return gPt(arguments[0]).toLowerCase()";
        return "//" + executeJavascript(js, element);
    }

    /**
     * Gets and Stores the table header position.
     *
     * @param locator    the locator
     * @param headerName the header name
     * @return the table header position is stored in the EC
     */
    public String getTableHeaderPosition(String locator, String headerName) {
        List<WebElement> columnHeaders = null;

        WebElement element = waitForElement(locator);
        try {
            columnHeaders = element.findElements(By.cssSelector("th"));
            String pos;
            int position = 1;
            for (WebElement record : columnHeaders) {
                if (record.getText().equals(headerName)) {
                    pos = String.valueOf(position);
                    storeECValue(headerName, pos);
                    return String.valueOf(pos);
                }
                position++;
            }
            throw new WebDriverException("Header name :" + headerName + "not Found");
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in getTableHeaderPosition locator:" + locator + " and header name:" + headerName);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return null;
        }

    }

    /**
     * Gets and Stores the column position for given element value
     *
     * @param locator     the locator for the table
     * @param elementName element to be searched in table  EC
     *                    getTableElementColumnPosition(java.lang.String, java.lang.String)
     */
    @Override
    public String getTableElementColumnPosition(String locator, String elementName) {
        try {
            List<WebElement> tableRows = null;
            List<WebElement> tableColumnsPerRow = null;
            WebElement element = waitForElement(locator);
            tableRows = element.findElements(By.cssSelector("tbody tr"));

            int position = 1;
            String pos;
            for (WebElement row : tableRows) {
                tableColumnsPerRow = row.findElements(By.cssSelector("td"));
                for (WebElement column : tableColumnsPerRow) {
                    if (column.getText().equals(elementName)) {
                        pos = String.valueOf(position);
                        storeECValue(elementName, pos);
                        return pos;
                    }
                    position++;
                }
                position = 1;
            }
            throw new WebDriverException("Element :" + elementName + " not Found");
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in getTableElementColumnPosition locator:" + locator + " and element name:" + elementName);
            throw e;
        }

    }

    /**
     * Gets and Stores the row position for given element value and stored in EC
     *
     * @param locator     the locator for the table
     * @param elementName element to be searched in table  EC
     *                    getTableElementColumnPosition(java.lang.String, java.lang.String)
     */
    public String getTableElementRowPosition(String locator, String elementName) {

        List<WebElement> tableRows = null;
        List<WebElement> tableColumnsPerRow = null;
        WebElement element = waitForElement(locator);
        tableRows = element.findElements(By.cssSelector("tbody tr"));

        int position = 1;
        String pos;
        for (WebElement row : tableRows) {
            tableColumnsPerRow = row.findElements(By.cssSelector("td"));
            for (WebElement column : tableColumnsPerRow) {
                if (column.getText().equals(elementName)) {
                    pos = String.valueOf(position);
                    storeECValue(elementName, pos);
                    return pos;
                }
            }
            position++;
        }
        throw new WebDriverException("Element :" + elementName + " not Found");
    }

    /**
     * Gets and Stores value under header name for given element name
     * e.g. if table has 2 columns A,B with value 1,2
     * if user passes 1 as elementname and B for headerName then return value will be 2
     *
     * @param locator     the locator for the table
     * @param elementName element to be searched in table
     * @param headerName  value under which header is to be found
     */
    public String getTableElementTextUnderHeader(String locator, String elementName, String headerName) {
        WebElement element = waitForElement(locator);
        try {
            String text = element.
                    findElement(By
                            .cssSelector("tbody tr:nth-child(" +
                                    getTableElementRowPosition(locator, elementName) +
                                    ") td:nth-child(" + getTableHeaderPosition(locator, headerName) +
                                    ")"))
                    .getText();
            storeECValue(headerName, text);
            return text;
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in getTableElementTextUnderHeader locator:" + locator + " and element name:" + elementName
                    + " header name: " + headerName);
            throw e;
        }


    }

    /**
     * Gets and Stores the list of values under header
     *
     * @param locator    the locator for the table
     * @param headerName get records under header name
     *                   getTableRecordsUnderHeader(java.lang.String, java.lang.String)
     */
    public List<String> getTableRecordsUnderHeader(String locator, String headerName) {
        try {
            List<String> records = new ArrayList<String>();
            WebElement element = waitForElement(locator);
            String headerPosition = getTableHeaderPosition(locator, headerName);
            List<WebElement> rows = element.findElements(By.cssSelector("tbody tr"));
            boolean isFirst = true;
            for (WebElement row : rows) {
                if (isFirst) {
                    isFirst = false;
                    continue;
                }
                records.add(row.findElement(By.cssSelector("th:nth-child(" +
                        headerPosition + "),td:nth-child(" +
                        headerPosition + ")")).getText());
            }
            storeECValue(headerName, records);
            return records;
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in getTableRecordsUnderHeader locator:" + locator + " header name: " + headerName);
            throw e;

        }

    }

    /**
     * Gets and Stores the list of values under header
     *
     * @param locator   the locator for the table
     * @param rowNumber index of row for which records are to be fetched
     * @param variable  store values in variable in EC
     *                  getTableRecordsUnderHeader(java.lang.String, java.lang.String)
     */
    public List<String> getTableRecordsForRow(String locator, String rowNumber, String variable) {
        try {
            List<String> records = new ArrayList<String>();
            WebElement tableElement = waitForElement(locator);
            String colCount = getTableColumnCount(locator, "");
            WebElement row = tableElement.findElement(By.cssSelector("tbody tr:nth-child(" + rowNumber + ")"));
            for (int i = 1; i <= Integer.valueOf(colCount); i++) {
                records.add(row.findElement(By.cssSelector("td:nth-child(" +
                        String.valueOf(i)
                        + ")")).getText());
            }
            storeECValue(variable, records);
            return records;
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in getTableRecordsForRow locator:" + locator + " row number: " + rowNumber
                    + " variable:" + variable);
            throw e;

        }


    }

    /**
     * Gets and stores the total number of rows in table*
     *
     * @param locator   Table locator to find rows for
     * @param tableName value in EC will be stored in variable tableName_rowCount
     * @return String value of total number of rows
     */
    public String getTableRowCount(String locator, String tableName) {
        try {
            WebElement element = waitForElement(locator);
            String rowCount = String.valueOf(element.findElements(By.cssSelector("tbody tr")).size());
            if (!tableName.equalsIgnoreCase("")) {
                storeECValue(tableName + "_rowCount", rowCount);
            }
            return rowCount;
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in getTableRowCount locator:" + locator + " table name:" + tableName);
            throw e;
        }
    }

    /**
     * Gets and stores the total number of columns in table*
     *
     * @param locator   Table locator to find columns for
     * @param tableName value in EC will be stored in variable tableName_columnCount
     * @return String value of total number of columns
     */
    public String getTableColumnCount(String locator, String tableName) {
        try {
            WebElement element = waitForElement(locator);
            String colCount = String.valueOf(element.findElements(By.cssSelector("tbody tr th")).size());
            if (!tableName.equalsIgnoreCase("")) {
                storeECValue(tableName + "_columnCount", colCount);
            }
            return colCount;
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in getTableColumnCount locator:" + locator + " table name:" + tableName);
            throw e;
        }

    }

    /**
     * Search for any value in given column
     *
     * @param locator    the locator of the table
     * @param columnName column name in which value is to be searched
     * @param value      value to search for
     * @return a boolean value showing if value is found or not
     */
    public String findValueInColumnName(String locator, String columnName, String value, String variable) {
        try {
            String headerPosition;
            if (StringUtils.isNumeric(columnName)) {
                headerPosition = columnName;
                int totalColumns = Integer.parseInt(getTableColumnCount(locator, ""));
                if (Integer.parseInt(headerPosition) > totalColumns) {
                    LOGNREPORT.sphnxError(this.getClass().getCanonicalName(),
                            "Number of columns:" + totalColumns + " less than given column number" + headerPosition, false);
                    throw new IllegalArgumentException("Number of columns:" + totalColumns + " less than given column number" + headerPosition);
                }
            } else {
                headerPosition = getTableHeaderPosition(locator, columnName);
            }
            WebElement element = waitForElement(locator);


            int numOfRows = Integer.valueOf(getTableRowCount(locator, ""));

            String rowText;
            for (int i = 1; i <= numOfRows; i++) {
                if (element.findElements(By.cssSelector("tbody tr:nth-child(" +
                        String.valueOf(i) + ") th")).size() > 0) {
                    continue;
                }
                rowText = element.findElement(By.cssSelector("tbody tr:nth-child(" +
                        String.valueOf(i) + ") td:nth-child(" +
                        headerPosition + ")"))
                        .getText();
                if (rowText.equalsIgnoreCase(value)) {
                    String rowNumber = String.valueOf(i);
                    if (!variable.equalsIgnoreCase("")) {
                        storeECValue(variable, rowNumber);
                    }
                    return rowNumber;
                }
            }
            return "";
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in findValueInColumnName locator:" + locator +
                    " column name:" + columnName + " value:" + value + "variable:" + variable);
            throw e;
        }
    }

    /**
     * Gets and Stores the value in given column and row
     *
     * @param locator  the locator for the table
     * @param variable save string value in variable in EC
     * @param column   column where value is to be fetched
     * @param row      row where value is to be searched
     *                 getTableElementTextForRowAndColumn(java.lang.String, java.lang.String,
     *                 java.lang.String)
     */
    @Override
    public String getTableElementTextForRowAndColumn(String locator, String variable, String row, String column) {
        try {
            WebElement element = waitForElement(locator);
            String value = element.findElement(By.cssSelector("tr:nth-child(" + row + ") td:nth-child(" + column + ")")).getText();
            storeECValue(variable, value);
            return value;
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in getTableElementTextForRowAndColumn locator:" + locator +
                    " column:" + column + " row:" + row + " variable:" + variable);
            throw e;
        }
    }

    /**
     * Gets and Stores all the values in table in 2D array
     *
     * @param locator  the locator for the table
     * @param variable save array value in variable in EC
     *                 getTableElements2DArray(java.lang.String)
     */
    @Override
    public String[][] getTableElements2DArray(String locator, String variable) {
        try {
            WebElement element = waitForElement(locator);
            List<WebElement> tableRows = element.findElements(By.cssSelector("tbody tr"));
            int numberOrRows = tableRows.size();
            int numberOfColumns = element.findElements(By.cssSelector("tbody tr:nth-child(1) th")).size();
            String[][] table = new String[numberOrRows][numberOfColumns];

            for (int i = 0; i < numberOrRows; i++) {
                List<WebElement> tableColumnsPerRow;
                //First row will header
                if (i == 0) {
                    tableColumnsPerRow = tableRows.get(i).findElements(By.cssSelector("th"));
                } else {
                    tableColumnsPerRow = tableRows.get(i).findElements(By.cssSelector("td"));
                }
                for (int j = 0; j < tableColumnsPerRow.size(); j++) {
                    table[i][j] = tableColumnsPerRow.get(j).getText();
                }
            }

            storeECValue(variable, table);

            return table;
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in getTableElements2DArray locator:" + locator + " variable:" + variable);
            throw e;
        }
    }

    /**
     * Gets and Stores all the values in table in List of list of string
     *
     * @param locator  the locator for the table
     * @param variable save list value in variable in EC
     *                 getTableInfoAsList(java.lang.String)
     */
    @Override
    public List<List<String>> getTableInfoAsList(String locator, String variable) {
        try {
            WebElement table = waitForElement(locator);
            List<List<String>> tableInfo = new ArrayList<List<String>>();
            List<WebElement> tableRows = table.findElements(By.cssSelector("tbody tr"));
            boolean isFirst = true;
            for (WebElement row : tableRows) {
                List<String> rowText = new ArrayList<String>();
                List<WebElement> columnsPerRow;
                if (isFirst) {
                    columnsPerRow = row.findElements(By.cssSelector("th"));
                    isFirst = false;
                } else {
                    columnsPerRow = row.findElements(By.cssSelector("td"));
                }
                for (WebElement column : columnsPerRow) {
                    rowText.add(column.getText());
                }
                tableInfo.add(rowText);
            }
            storeECValue(variable, tableInfo);
            return tableInfo;
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in getTableInfoAsList locator:" + locator + " variable:" + variable);
            throw e;
        }
    }

    /**
     * Gets and Stores all the values in table in List of list of string
     *
     * @param locator     the locator for the table
     * @param elementName save list value in variable in EC
     *                    getTableElementSpecificHeaderLocator(java.lang.String, java.lang.String,
     *                    java.lang.String)
     */
    @Override
    public String getTableElementSpecificHeaderLocator(String locator, String elementName, String headerName) {
        try {
            if (locator.startsWith(XPATH)) {
                return "//" + findLocatorSubstring(locator) + "//tr[" + getTableElementRowPosition(locator, elementName) + "]//td[" + getTableHeaderPosition(locator, headerName) + "]";
            } else if (locator.startsWith("//")) {
                return locator + "//tr[" + getTableElementRowPosition(locator, elementName) + "]//td[" + getTableHeaderPosition(locator, headerName) + "]";
            } else if (locator.startsWith(CSS)) {
                return locator + " tr:nth-child(" + getTableElementRowPosition(locator, elementName) + ") td:nth-child(" + getTableHeaderPosition(locator, headerName) + ")";
            } else {
                return "css=#" + locator + " tr:nth-child(" + getTableElementRowPosition(locator, elementName) + ") td:nth-child(" + getTableHeaderPosition(locator, headerName) + ")";
            }
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in getTableElementSpecificHeaderLocator locator:" + locator +
                    " element name:" + elementName +
                    " header name:" + headerName
            );
            throw e;
        }
    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * getTableElementSpecificRowAndColumnLocator(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public String getTableElementLocatorForRowAndColumn(String locator, String row, String column) {
        try {
            if (locator.startsWith(XPATH)) {
                return "//" + findLocatorSubstring(locator) + "//tr[" + row + "]//td[" + column + "]";
            } else if (locator.startsWith("//")) {
                return locator + "//tr[" + row + "]//td[" + column + "]";
            } else if (locator.startsWith(CSS)) {
                return locator + " tr:nth-child(" + row + ") td:nth-child(" + column + ")";
            } else {
                return "css=#" + locator + " tr:nth-child(" + row + ") td:nth-child(" + column + ")";
            }
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in getTableElementSpecificHeaderLocator locator:" + locator +
                    " row:" + row +
                    " column: " + column
            );
            throw e;
        }
    }

    /**
     * navigate method Navigate to the Provided URL
     *
     * @param url the url you want to navigate to
     * @return
     */
    public boolean navigate(String url) {
        try {
            Assert.assertTrue(url.length() > 0, "url should be configured");

            if (!url.toLowerCase().contains("http")) {
                url = "http://" + url;
            }
            this.startEvent();
            this.webDriver.navigate().to(url);
            this.endEvent();
            LOGNREPORT.sphnxInfo(ACTIONS, "Launch of " + url + " took " + this.getEventTime() + " seconds.", "yellow", 1, true);
            webDriver.manage().window().maximize();
            return true;
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in navigate url:" + url);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) try {
                throw e;
            } catch (Exception e1) {
                return false;
            }
            return false;
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see (String)
     */
    public boolean validateTextContains(String locator, String text) {
        try {
            moveToElement(locator);
            WebElement element = waitForElement(locator);
            String scrollElementIntoMiddle = "var viewPortHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"
                    + "var elementTop = arguments[0].getBoundingClientRect().top;"
                    + "window.scrollBy(0, elementTop-(viewPortHeight/2));";

            ((JavascriptExecutor) webDriver).executeScript(scrollElementIntoMiddle, element);
            //LOGNREPORT.sphnxInfo(ACTIONS, locator + " has been centered on screen.");
            String elementText = element.getText();
            int location = elementText.indexOf(text);
            if (location > -1) {
                highlight(element);
                LOGNREPORT.sphnxPASS(ACTIONS, "TEXT: '" + text + "' was found.", true);
                return true;
            } else {
                LOGNREPORT.sphnxFAIL(ACTIONS, "TEXT: '" + text + "' was not found.", true);
                return false;
            }
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in validateTextContains locator:" + locator + " text:" + text);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see (String)
     */
    public boolean validateFrameContains(String locator, String text) {
        try {
            String parent = getParent(locator);
            if (parent != null && !parent.equals("")) {
                this.handleFrames(parent);
            }
            WebElement element = webDriver.findElement(getLocator(locator));
            String elementText = element.getText();
            int location = elementText.indexOf(text);
            if (location > -1) {
                highlight(element);
                LOGNREPORT.sphnxPASS(ACTIONS, "TEXT: '" + text + "' was found.", true);
                return true;
            } else {
                LOGNREPORT.sphnxFAIL(ACTIONS, "LOOKING FOR text: '" + text + ", but FOUND text: " + elementText, true);
                return false;
            }
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in validateFrameContains locator:" + locator + " text:" + text);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see (String)
     */
    public boolean validateAndStoreEC(String locator, String key, String value) {
        try {
            moveToElement(locator);
            WebElement element = waitForElement(locator);
            String scrollElementIntoMiddle = "var viewPortHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"
                    + "var elementTop = arguments[0].getBoundingClientRect().top;"
                    + "window.scrollBy(0, elementTop-(viewPortHeight/2));";

            ((JavascriptExecutor) webDriver).executeScript(scrollElementIntoMiddle, element);
            String elementText = element.getText();
            int location = elementText.indexOf(value);
            if (location > -1) {
                highlight(element);
                LOGNREPORT.sphnxInfo(ACTIONS, "TEXT: '" + value + "' was found and saved into Execution Context.", true);
                ExecutionContext.setECValue(key, value);
                ExecutionContext.writeToECFile();
                return true;
            } else {
                LOGNREPORT.sphnxError(ACTIONS, "TEXT: '" + value + "' was not found, no Execution Context saved.", true);
                return false;
            }
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in validateAndStoreEC locator:" + locator + " key:" + key + " value:" + value);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }

    /**
     * The purpose of this method is to store a value, provided by a user for later use
     * <p>
     * To-Do Normalize this code
     */
    public boolean storeECValue(String key, Object value) {
        try {
            ExecutionContext.setECValue(key, value);
            ExecutionContext.writeToECFile();
            value = ExecutionContext.toString(value);
            LOGNREPORT.sphnxPASS(ACTIONS, "Stored Key/Value pair: " + key + ":" + value, true);
            return true;
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in storeECValue locator:" + locator + " key:" + key + " value:" + value);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }

    }


    /**
     * (non-Javadoc)
     *
     * @see (String)
     */
    public boolean validateFromEC(String locator, String value) {
        try {
            WebElement element = waitForElement(locator);
            String scrollElementIntoMiddle = "var viewPortHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"
                    + "var elementTop = arguments[0].getBoundingClientRect().top;"
                    + "window.scrollBy(0, elementTop-(viewPortHeight/2));";
            ((JavascriptExecutor) webDriver).executeScript(scrollElementIntoMiddle, element);
            String elementText = element.getText();
            int location = elementText.indexOf(value);
            if (location > -1) {
                highlight(element);
                LOGNREPORT.sphnxInfo(ACTIONS, "TEXT: '" + value + "' was found and saved into Execution Context.");
                Report.pass("TEXT '" + value + "' was found and saved into Execution Context.");
                return true;
            } else {
                LOGNREPORT.sphnxError(ACTIONS, "TEXT: '" + value + "' was not found, no Execution Context saved.");
                return false;
            }
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in validateFromEC locator:" + locator + " value:" + value);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }

    }

    /**
     * (non-Javadoc)
     *
     * @see
     */
    public String getCurrentUrl() {
        try {
            return this.webDriver.getCurrentUrl();
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in getCurrentUrl");
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return null;
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see
     */
    @Override
    public void close() {
        try {
            this.webDriver.close();
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in close web driver");
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see
     */
    @Override
    public void quit() {
        try {
            this.webDriver.quit();
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in quit web driver");
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
        }
    }

    /**
     * pressLinkName method perform Click action on the provided link name
     *
     * @param linkName the name of the link you want to press
     * @return
     */
    @Override
    public boolean pressLinkName(String linkName) {
        try {
            WebDriverWait wait = new WebDriverWait(this.webDriver, getWaitForElement());
            wait.until(ExpectedConditions.visibilityOfElementLocated((By.linkText(linkName)))).click();
            return true;
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in pressLinkName:" + linkName);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }

    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * pressLinkNameAndWaitForPageToLoad(java.lang.String)
     */
    @Override
    public void pressLinkNameAndWaitForPageToLoad(String linkName) {
        pressLinkName(linkName);
    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * pressLinkNameAndClickOkInAlert(java.lang.String)
     */
    @Override
    public void pressLinkNameAndClickOkInAlert(String linkName) {
        pressLinkName(linkName);
        clickOkInAlert();
    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * pressLinkNameAndClickOkInAlertNoPageLoad(java.lang.String)
     */
    @Override
    public void pressLinkNameAndClickOkInAlertNoPageLoad(String linkName) {
        pressLinkNameAndClickOkInAlert(linkName);
    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * pressLinkNameAndClickCancelInAlert(java.lang.String)
     */
    @Override
    public void pressLinkNameAndClickCancelInAlert(String linkName) {
        pressLinkName(linkName);
        clickCancelInAlert();

    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * executeJavascript(java.lang.String, java.lang.Object[])
     */
    @Override
    public Object executeJavascript(String js, Object... args) {
        try {
            JavascriptExecutor executor = (JavascriptExecutor) this.webDriver;
            return executor.executeScript(js, args);
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in executeJavascript with string:" + js + " and args: " + args);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return null;
        }

    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * getAttributeValue(java.lang.String, java.lang.String)
     */
    @Override
    public String getAttributeValue(String locator, String attribute) {
        return waitForElement(locator).getAttribute(attribute);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean dragAndDrop(String locatorFrom, String locatorTo) throws IOException {
//        try {
        //getBuilder().dragAndDrop(waitForElement(locatorFrom), waitForElement(locatorTo)).perform();
        try {

            InputStream is = this.getClass().getClassLoader().getResourceAsStream("dragAndDrop.js");
            String jquerySimulator = IOUtils.toString(is, StandardCharsets.UTF_8);
            webDriver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
            JavascriptExecutor js = (JavascriptExecutor) webDriver;
            js.executeScript(jquerySimulator);

            js.executeScript("$('#" + this.waitForElement(locatorFrom)
                    .getAttribute("id") + "')" +
                    ".simulateDragDrop({ dropTarget: '#" +
                    this.waitForElement(locatorTo).getAttribute("id") + "'});");
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in dragAndDrop with locatorFrom:" + locatorFrom + " and locatorTo: " + locatorTo);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
        return true;

    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean dragAndDrop(String locatorFrom, int xOffset, int yOffset) {
        try {
            getBuilder().dragAndDropBy(waitForElement(locatorFrom), xOffset, yOffset).perform();
            return true;
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in dragAndDrop with locatorFrom:" + locatorFrom + " and offset: " + xOffset + ":" + yOffset);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * switchToNewWindow()
     */
    @Override
    public void switchToLatestWindow() {
        try {
            Iterator<String> itr = this.webDriver.getWindowHandles().iterator();
            String lastElement = null;
            while (itr.hasNext()) {
                lastElement = itr.next();
            }
            this.webDriver.switchTo().window(lastElement);
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in switchToLatestWindow");
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
        }
    }

    /**
     * selectFrame method Switch to the provided Frame ID
     *
     * @param frameID the frame id
     */
    @Override
    public void selectFrame(String frameID) {
        try {
            selectFrameMain();
            this.webDriver.switchTo().frame(frameID);
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in selectFrame with frame id:" + frameID);
            throw e;
        }
    }

    /**
     * (non-Javadoc)
     * <p>
     * <p>
     * selectFrameMain()
     */
    @Override
    public void selectFrameMain() {
        try {
            this.webDriver.switchTo().defaultContent();
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in select default");
            throw e;
        }
    }

    /**
     * @inheritDoc
     */
    public void maximizeWindow() {
        try {
            this.webDriver.manage().window().maximize();
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in maximizeWindow");
            throw e;
        }
    }

    /**
     * getNumberOfElements method find element and return the size of elements
     *
     * @param locator the element locator
     * @return
     */
    @Override
    public int getNumberOfElements(String locator) {
        try {
            return this.webDriver.findElements(getLocator(locator)).size();
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in getNumberOfElements");
            throw e;
        }

    }

    /**
     * @inheritDoc
     */
    @Override
    public void moveToElement(String locator, int x, int y) {
        try {
            getBuilder().moveToElement(waitForElement(locator), x, y).perform();
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in moveToElement with locator:" + locator + " x:" + x + " y:" + y);
            throw e;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void moveToElement(String locator) {
        try {
            getBuilder().moveToElement(waitForElement(locator)).perform();
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in moveToElement with locator" + locator);
            throw e;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void moveByOffset(int xOffset, int yOffset) {
        try {
            getBuilder().moveByOffset(xOffset, yOffset).perform();
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in moveByOffset with x:" + xOffset + " y:" + yOffset);
            throw e;
        }
    }

    // Logic to handle interacting with Windows Alert dialogs
    public boolean ClickAlert(String button) {
        try {
            boolean isValidButton = false;
            if (button.equalsIgnoreCase("ok")) {
                isValidButton = true;
                try {
                    webDriver.switchTo().alert().accept();
                    for (String windowId : webDriver.getWindowHandles()) {
                        webDriver.switchTo().window(windowId);
                        break;
                    }
                    return true;
                } catch (Exception e) {
                    LOGNREPORT.sphnxError(ACTIONS, "Failed to click on the Alert OK button.");
                    return false;
                }
            }
            if (button.equalsIgnoreCase("cancel")) {
                isValidButton = true;
                try {
                    webDriver.switchTo().alert().dismiss();
                    for (String windowId : webDriver.getWindowHandles()) {
                        webDriver.switchTo().window(windowId);
                        break;
                    }
                    return true;
                } catch (Exception e) {
                    LOGNREPORT.sphnxError(ACTIONS, "Failed to click on the Alert Cancel button.");
                    return false;
                }
            }
            return isValidButton;
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in dragAndDrop with ClickAlert with name:" + button);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getAlertText() {
        try {
            return waitForAlert().getText();
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in getAlertText");
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return null;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void promptInputPressOK(String inputMessage) {
        Alert alert = waitForAlert();
        alert.sendKeys(inputMessage);
        alert.accept();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void promptInputPressCancel(String inputMessage) {
        Alert alert = waitForAlert();
        alert.sendKeys(inputMessage);
        alert.dismiss();

    }

    /**
     * @inheritDoc
     */
    @Override
    public void waitForAjaxComplete(int milliseconds) {
        long endTime;
        boolean ajaxComplete = false;
        long startTime = System.currentTimeMillis();
        do {
            try {
                ajaxComplete = ((Boolean) executeJavascript("return jQuery.active == 0")).booleanValue();
                Thread.sleep(THREAD_SLEEP);
            } catch (InterruptedException e) {
                error(e.getMessage());
            }
            endTime = System.currentTimeMillis();
        } while (!ajaxComplete && endTime - startTime <= milliseconds);

        if (!ajaxComplete) {
            warn("The AJAX call was not completed with in " + milliseconds + " ms");
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public Point getElementPosition(String locator) {
        return waitForElement(locator).getLocation();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void refresh() {
        this.webDriver.navigate().refresh();
    }

    /**
     * @inheritDoc
     */
    @Override
    public int getNumberOfTotalRows(String locator, String variable) {
        return waitForElement(locator).findElements(By.cssSelector("tbody tr")).size();
    }

    /**
     * @inheritDoc
     */
    @Override
    public int getNumberOfTotalRows(String locator) {
        return waitForElement(locator).findElements(By.cssSelector("tbody tr")).size();
    }

    /**
     * @inheritDoc
     */
    @Override
    public int getNumberOfTotalColumns(String locator) {
        return waitForElement(locator).findElements(By.cssSelector("tbody tr:nth-child(1) td")).size();
    }

    /**
     * @inheritDoc
     */
    @Override
    public HttpCookie getCookieByName(String name) {
        return new HttpCookie(name, this.webDriver.manage().getCookieNamed(name).getValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<HttpCookie> getAllCookies() {
        List<HttpCookie> allCookies = new ArrayList<HttpCookie>();
        Iterator<Cookie> it = this.webDriver.manage().getCookies().iterator();
        while (it.hasNext()) {
            Cookie c = it.next();
            allCookies.add(new HttpCookie(c.getName(), c.getValue()));
        }
        return allCookies;
    }


    /**
     * Action to select a value from a dropdown
     *
     * @param locator: locator type to be used to locate the dropdown menu element
     * @param value:   value to be selected from dropdown
     */
    public boolean selectValueFromDropDown(String locator, String value) {
        try {
            if (isValidLocator(locator)) {
                LOGNREPORT.sphnxInfo(ACTIONS, (FULLNAME + " locator found in ControlMap: " + LOCATOR), "blue", 1, false);
                this.waitForPageLoad();
                String parent = this.getParent(locator);
                String parentWindowHandler = "";

                if (parent != null && !(parent.equals(""))) {

                    parentWindowHandler = this.webDriver.getWindowHandle();
                    if (parent.equalsIgnoreCase("popup")) {
                        String subWindowHandler = null;
                        Set<String> handles = this.webDriver.getWindowHandles();

                        for (Iterator iterator = handles.iterator(); iterator.hasNext(); subWindowHandler = (String) iterator.next()) {
                            ;
                        }
                        this.webDriver.switchTo().window(subWindowHandler);
                    } else {
                        this.webDriver.switchTo().defaultContent();
                    }
                }
                if (parent != null) {
                    handleFrames(parent);
                }
                WebDriverWait wait = new WebDriverWait(this.webDriver, this.waitForElement);

                try {
                    WebElement element = (WebElement) wait.until(ExpectedConditions.elementToBeClickable(this.getLocator(locator)));
                    this.outline(element);
                    this.removeOutline(element);
                    Select dropdown = new Select(element);
                    waitForOptions(dropdown);
                    dropdown.selectByVisibleText(value);
                    if (!parentWindowHandler.equals("")) {
                        this.webDriver.switchTo().window(parentWindowHandler);
                    }
                    return true;
                } catch (Exception ex) {
                    this.webDriver.switchTo().defaultContent();
                    LOGNREPORT.sphnxFAIL(ACTIONS, "selectValueFromDropDown() " +
                            locator + " - Failed to select the '" + value + "' from the dropdown box.");
                    if (configurations.getAllConfigurations().containsKey("iscomplex")) throw ex;
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "selectValueFromDropDown() " +
                    locator + " - Failed to select the '" + value + "' from the dropdown box.");
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }

    public void waitForOptions(final Select select) {
        new FluentWait<WebDriver>(this.webDriver)
                .withTimeout(60, TimeUnit.SECONDS)
                .pollingEvery(10, TimeUnit.MILLISECONDS)
                .until(new Function<WebDriver, Boolean>() {
                    public Boolean apply(WebDriver d) {
                        return (select.getOptions().size() >= 1);
                    }
                });
    }

    /**
     * Wait for the page to load till the readyState becomes complete
     *
     * @param timeoutSeconds
     * @see WebDriverException
     */
    public void waitForPageLoad(long timeoutSeconds) {
        Wait<WebDriver> wait = new WebDriverWait(this.webDriver, timeoutSeconds).ignoring(WebDriverException.class);
        wait.until(new Function<WebDriver, Boolean>() {
            public Boolean apply(WebDriver driver) {
                return String
                        .valueOf(((JavascriptExecutor) driver).executeScript("return document.readyState"))
                        .equals("complete");
            }
        });
    }

    /**
     * Wait for the page to load till the readyState becomes complete in default time
     *
     * @see WebDriverException
     */
    public void waitForPageLoad() {
        for (String windowId : webDriver.getWindowHandles()) {
            webDriver.switchTo().window(windowId);
            break;
        }
        LOGNREPORT.sphnxInfo(ACTIONS, "Page Loading...", "purple", 1);
        Wait<WebDriver> wait = new WebDriverWait(this.webDriver, getWaitForPageToLoad()).ignoring(WebDriverException.class);
        wait.until((Function<WebDriver, Boolean>) driver -> String
                .valueOf(((JavascriptExecutor) driver).executeScript("return document.readyState"))
                .equals("complete"));

//        // also waiting for all the div tags to be visible
//        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName("div")));
        LOGNREPORT.sphnxInfo(ACTIONS, "Page Loaded!", "purple", 1);
    }

    /**
     * The purpose of this method is to read a JSON file and convert the key value pairs
     * and load them into execution context for later use
     * <p>
     * To-Do Normalize this code
     */
    public boolean loadJsonFromFile(String filename) {
        return loadJsonFromFile("JSON_", filename);
    }

    public boolean loadJsonFromFile(String prefix, String filename) {
        ParseConfigurations parseConfigurations = new ParseConfigurations();
        filename = parseConfigurations.getAllConfigurations().get(filename);
        File file = new File(filename);

        if (file.exists()) {
            String json = null;
            json = GenerateData.getFileContents(file.getPath());

            String data = json.trim();


            json = json.replace("\"", "");
            json = json.replace("[", "");
            json = json.replace("]", "");
            json = json.replace("}", "");

            String[] arrJson = json.split("\\{");

            for (int i = 0; i < arrJson.length; i++) {
                String[] arrLines = arrJson[i].split(",");
                for (int j = 0; j < arrLines.length; j++) {
                    String[] arrPair = arrLines[j].split(":");
                    if (arrPair.length > 1) {
                        ExecutionContext.setECValue(prefix + arrPair[0], arrPair[1]);
                    }
                }
            }
            // dump the entire content into a key value pair for complex lookups.
            ExecutionContext.setECValue(prefix + "Data", "JSON@@" + data);
        } else {
            LOGNREPORT.sphnxError(ACTIONS, "Upload failed. File not found: '" + file.getPath() + "'");
            return false;
        }

        // the following logic will read the file in and load it into a JSON structure.
        // For our initial purposes, we will parse the input sting ourselves
        // and pull out all of the key value pairs and load them into EC
        //if(json == null) {
        //    System.out.println("Failed to load contents from '" + filename + "'");
        //    return false;
        //}
        //Gson gson = new Gson();
        //Map<String, Object> map = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
        //for (Map.Entry<String, Object> entry : map.entrySet()) {
        //    System.out.println("key=" + entry.getKey() + ", value=" + entry.getValue());
        //}

        return true;
    }

    /**
     * The purpose of this method is to read a XML file and convert the key value pairs
     * and load them into execution context for later use
     * <p>
     * To-Do Normalize this code
     */
    public boolean loadXmlFromFile(String filename) {
        return loadXmlFromFile("XML_", filename);
    }

    public boolean loadXmlFromFile(String prefix, String filename) {

        ParseConfigurations parseConfigurations = new ParseConfigurations();
        filename = parseConfigurations.getAllConfigurations().get(filename);


//        File userDir = new File(System.getProperty("user.dir"));
//
//        File file = new File(userDir.getParent() + File.separator
//                + "resources" + File.separator
//                + "downloads" + File.separator + filename);

//        File userHome = new File(System.getProperty("user.home"));

        File file = new File(filename);

        if (file.exists()) {
            String xml = null;
            LOGNREPORT.sphnxInfo(ACTIONS, "BAM! - LOADING XML FILE");
            xml = GenerateData.getFileContents(file.getPath());
            LOGNREPORT.sphnxInfo(ACTIONS, "BAM! - LOADED XML FILE");

            String data = xml.trim();

            xml = xml.replace("[", "");
            xml = xml.replace("]", "");
            xml = xml.replace("\n", "");
            xml = xml.replace("\r", "");

            String[] arrXML = xml.split("<");
            for (int i = 0; i < arrXML.length; i++) {
                if (arrXML[i].trim().length() > 0) {
                    String first = arrXML[i].trim().substring(0, 1);
                    if (first.equalsIgnoreCase("/") || first.equalsIgnoreCase("!")) {
                        ;
                    } else {
                        String[] arrPair = arrXML[i].split(">");
                        if (arrPair.length > 1) {
                            ExecutionContext.setECValue(prefix + arrPair[0], arrPair[1]);
                        }
                    }
                }
            }
            // dump the entire content into a key value pair for complex lookups.
            ExecutionContext.setECValue(prefix + "Data", "XML@@" + data);
        } else {
            LOGNREPORT.sphnxError(ACTIONS, "Upload failed. File not found: '" + file.getPath() + "'");
            return false;
        }
        return true;
    }

    /**
     * The purpose of this method is to get the value of a control and then store
     * that value in execution context
     * <p>
     * <p>
     * To-Do Normalize this code
     */
    public boolean StoreTextValue(String locator, String key) {
        String value = "";
        try {
            if (isValidLocator(locator)) {
                LOGNREPORT.sphnxInfo(ACTIONS, (FULLNAME + " locator found in ControlMap: " + LOCATOR), "blue", 1, false);
                String parent = getParent(locator);
                if (parent != null && !parent.equals("")) {

                    String parentWindowHandler = webDriver.getWindowHandle(); // Store your parent window
                    if (parent.equalsIgnoreCase("popup")) {
                        String subWindowHandler = null;

                        Set<String> handles = webDriver.getWindowHandles(); // get all window handles
                        Iterator<String> iterator = handles.iterator();
                        while (iterator.hasNext()) {
                            subWindowHandler = iterator.next();
                        }
                        webDriver.switchTo().window(subWindowHandler); // switch to popup window
                    } else {
                        webDriver.switchTo().defaultContent();
                    }

                    handleFrames(parent);
                }

                WebElement element = waitForElement(locator);
                value = element.getText();
                if (value.trim().length() < 1) {
                    value = element.getAttribute("value");
                }

                boolean wasStored = setECValue(key, value);
                if (wasStored) {
                    LOGNREPORT.sphnxPASS(ACTIONS, "Key/Value pair was successfully stored. '" + key + ":" + value + "'");
                    return true;
                } else {
                    LOGNREPORT.sphnxFAIL(ACTIONS, "Failed to store Key/Value pair. '" + key + ":" + value + "'");
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Failed to store Key/Value pair. '" + key + ":" + value + "'");
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }

    }

    private void handleFrames(String parent) {
        int frame_count = webDriver.findElements(By.xpath("//frame | //iframe")).size();
        if (frame_count > 0) {
            if (!parent.equals("")) {
                String frames[] = parent.split("~");
                for (String frame : frames) {
                    String locator = "";
                    try {
                        List<Control> controls = (List<Control>) applicationContext.getBean("controlMap");
                        for (Control control : controls) {
                            if (control.getLogicalName().toLowerCase().equals(frame.toLowerCase())) {
                                By by = (By) this.applicationContext.getBean("lookUp", new Object[]{control.getDescriptor()});
                                webDriver.switchTo().frame((WebElement) webDriver.findElement(by));
                                LOGNREPORT.sphnxInfo(ACTIONS, "Switched to frame: " + frame);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        webDriver.switchTo().defaultContent();
                        LOGNREPORT.sphnxInfo(ACTIONS, "Error navigating to Frame: " + frame);
                    }
                }
            }
        }
    }

    /**
     * The purpose of the following methods are to compare two or more numeric values
     * to make sure they are equal
     * <p>
     * To-Do Add logic to handle rounding and flooring before comparisons
     */
    public boolean compareNumbers(String value1, String value2, String value3, String value4) {
        return compareNumbers(value1 + "~" + value2 + "~" + value3 + "~" + value4);
    }

    public boolean compareNumbers(String value1, String value2, String value3) {
        return compareNumbers(value1 + "~" + value2 + "~" + value3);
    }

    public boolean compareNumbers(String value1, String value2) {
        return compareNumbers(value1 + "~" + value2);
    }

    public boolean compareNumbers(String values) {
        boolean noErrorsFound = true;

        double tempNumber = 0.00;
        double firstNumber = 0.00;
        double nextNumber = 0.00;

        boolean isFloor = false;
        boolean isFormatted = false;
        String format = "";
        String newValues = "";

        // go through array of numbers to see if there is a formatting instruction
        String[] arrItems = values.split("~");
        if (arrItems.length > 1) {
            for (int i = 0; i < arrItems.length; i++) {
                // If the value is empty, we will make it zero
                if (arrItems[i].length() < 1) {
                    arrItems[i] = "0.00";
                }
                boolean isControlElement = false;

                if (arrItems[i].indexOf("#") > -1) {
                    isFormatted = true;
                    format = arrItems[i];
                    isControlElement = true;
                }
                if (arrItems[i].trim().equalsIgnoreCase("floor") || arrItems[i].trim().equalsIgnoreCase("down")) {
                    isFloor = true;
                    arrItems[i] = arrItems[i].replace("-", "");
                    isControlElement = true;
                }

                if (!isControlElement) {
                    arrItems[i] = arrItems[i].replaceAll("[^0-9.~]", "");
                    if (arrItems[i].length() < 1) {
                        arrItems[i] = "0.00";
                    }
                    newValues = newValues + arrItems[i] + "~";
                }
            }
        }

        String[] arrNumbers = newValues.split("~");
        if (arrNumbers.length > 1) {
            for (int i = 0; i < arrNumbers.length; i++) {
                // If the value is empty, we will make it zero
                if (arrNumbers[i].length() < 1) {
                    arrNumbers[i] = "0.00";
                }
                tempNumber = Double.parseDouble(arrNumbers[i]);
                DecimalFormat df = new DecimalFormat(format);

                if (arrNumbers[i].length() > 0) {
                    if (isFormatted) {
                        if (isFloor) {
                            df.setRoundingMode(RoundingMode.FLOOR);
                        } else {
                            df.setRoundingMode(RoundingMode.HALF_UP);
                        }
                    }
                }

                if (i == 0) {
                    firstNumber = Double.parseDouble(df.format(tempNumber));
                } else {
                    if (arrNumbers[i].length() > 0) {
                        nextNumber = Double.parseDouble(df.format(tempNumber));
                        if (firstNumber != nextNumber) {
                            noErrorsFound = false;
                        }
                    }
                }
            }

            // if all of the numbers are valid
            // parse through them and make sure they all match
            if (!noErrorsFound) {
                LOGNREPORT.sphnxError(ACTIONS, "Error: The numbers provided do not match. Values: " + values);
            }
            return noErrorsFound;
        } else {
            LOGNREPORT.sphnxError(ACTIONS, "Error: Two or more tilde separated values must be provided for this function to work properly. Values: " + values);
            return false;
        }
    }

    public boolean storeDBValue(String locator) {
        return storeDBValue(locator, "DB_");
    }

    public boolean storeDBValue(String locator, String prefix) {
        if (isValidLocator(locator)) {
            LOGNREPORT.sphnxInfo(ACTIONS, (FULLNAME + " locator found in ControlMap: " + LOCATOR), "blue", 1, false);
            String descriptor = getDescriptor(locator);
            Parser parser = new Parser();
            String sql = parser.replaceDataReferenceInString(descriptor, false);
            boolean result = false;

            try {
                connection = connectDB();
            } catch (Exception e) {
                LOGNREPORT.sphnxError(ACTIONS, "Error: Failed to establish a database connection.");
            }

            try {
                try (Statement statement = connection.createStatement()) {
                    resultSet = statement.executeQuery(sql);
                    while (resultSet.next()) {
                        for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                            String key = resultSet.getMetaData().getColumnLabel(i);
                            String value = resultSet.getString(i);
                            result = setECValue(prefix + key, value);
                        }
                    }
                    return result;
                } catch (Exception e) {
                    LOGNREPORT.sphnxError(ACTIONS, "Error: Failed to execute sql command. '" + sql + "'");
                    return false;
                }
            } catch (Exception e) {
                LOGNREPORT.sphnxError(ACTIONS, "Error: Failed to establish a database connection.");
                if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
                return false;
            }
        }

        return false;
    }

    public static Connection connectDB() throws ClassNotFoundException {
        String driver = MetaData.getMetaData("DB_Driver");
        String host = MetaData.getMetaData("DB_Host");
        String database = MetaData.getMetaData("DB_Database");
        String port = MetaData.getMetaData("DB_Port");
        String username = MetaData.getMetaData("DB_Username");
        String password = MetaData.getMetaData("DB_Password");

        try {
            Class.forName(driver);
            //jdbc:mysql://10.211.1.240:3306/lxpressLFI_qa9
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?serverTimezone=UTC", username, password);

            return connection;
        } catch (SQLException e) {
            return null;
        } catch (ClassNotFoundException e) {
            LOGNREPORT.sphnxError(ACTIONS, "Error: SQL driver Class Not Found: " + driver);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return null;
        }
    }

    public String getDescriptor(String locator) {
        List<Control> controls = (List<Control>) applicationContext.getBean("controlMap");
        try {
            int i = controls.size();
            for (Control control : controls) {
                String controlFullName = control.getContext() + "." + control.getLogicalName();
                if (controlFullName.equalsIgnoreCase(locator)) {
                    controlDescriptor = control.getDescriptor();
                    return controlDescriptor;
                }
            }
            return null;
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, "Invalid Locator provided: '" + locator + ";");
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return null;
        }
    }

    public By getDescriptorBy(String descriptor) {
        String[] descArray = descriptor.split("=", 2);

        switch (descArray[0].toLowerCase()) {
            case XPATH:
                return By.xpath(descArray[1]);
            case CSS:
                return By.cssSelector(descArray[1]);
            case ID:
                return By.id(descArray[1]);
            case LINKTEXT:
                return By.linkText(descArray[1]);
            case CLASS:
                return By.className(descArray[1]);
            case TAGNAME:
                return By.tagName(descArray[1]);
            case NAME:
                return By.name(descArray[1]);
            default:
                return null;
        }
    }

    //pass in the Locator and will return the DescriptorBy value based on the Descriptor
    public By getDescriptorByFromLocator(String locator) {
        List<Control> controls = (List<Control>) applicationContext.getBean("controlMap");
        try {
            int i = controls.size();
            for (Control control :
                    controls) {
                String controlFullName = control.getContext() + "." + control.getLogicalName();
                if (controlFullName.equalsIgnoreCase(locator)) {
                    controlDescriptor = control.getDescriptor();
                    return getDescriptorBy(controlDescriptor);
                }
            }
            return null;
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, "Invalid Locator provided: '" + locator + ";");
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return null;
        }
    }


    //check to see if an element is present on the screen
    //input is the descriptor string from the UIMap
    public boolean elementIsVisible(String descriptor) {
        //check for existence of the testButton (only shows up if a new browser session was started)
        WebElement element = null;
        boolean isVisible;

        try {
            String elementLocator = this.getDescriptor(descriptor);
            element = webDriver.findElement(this.getDescriptorBy(elementLocator));
            isVisible = element.isDisplayed();

        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, ("Exception executing elementIsVisible with locator:" + descriptor));
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
        return isVisible;
    }


    // region new methods

    /**
     *
     * @param locatorOfChooseFileBtn
     * @param fileAbsolutePathBasedOnOS
     */
    public void uploadFile(String locatorOfChooseFileBtn, String fileAbsolutePathBasedOnOS){
        typeKeys(locatorOfChooseFileBtn,fileAbsolutePathBasedOnOS);
    }

    /**
     *
     * @param urlOfFileLocation
     * @param fileName
     * @return
     */
    public  boolean downloadFileFromURL(String urlOfFileLocation, String fileName){
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpHead request = new HttpHead(urlOfFileLocation);
        HttpResponse response = null;
        try {
            response = httpClient.execute(request);

            String  contentType =  response.getFirstHeader("Content-Type").getValue();

            int contentLength = Integer.parseInt(response.getFirstHeader("Content-Length").getValue());

            if (contentType.equals("application/octet-stream")){
                LOGNREPORT.sphnxInfo(ACTIONS,contentType,false);
            }

            if (contentLength != 0){
                System.out.println(contentLength);

            }

            URL downloadLink =  new URL(urlOfFileLocation);
            InputStream inputStream =  new BufferedInputStream(downloadLink.openStream());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer =  new byte[1024];
            int n = 0;

            while (-1!=(n = inputStream.read(buffer))){
                outputStream.write(buffer,0,n);
            }

            outputStream.close();
            inputStream.close();

            byte[] responsenew = outputStream.toByteArray();

            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(responsenew);
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }


    }

    // endregion

    public boolean waitForPageLoad(String loaderLocator) {
        WebElement loader;
        boolean isDisplayed;
        try {
            By locator = getDescriptorByFromLocator(loaderLocator);
            loader = webDriver.findElement(locator);
        } catch (Exception e) {
            ;
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return true;
        }

        isDisplayed = loader.isDisplayed();
        while (isDisplayed) {
            LOGNREPORT.sphnxInfo(ACTIONS, loader.getText(), "cyan", 1);
            try {
                isDisplayed = loader.isDisplayed();
            } catch (Exception e) {
                LOGNREPORT.sphnxInfo(ACTIONS, "Page Loaded!", "green", 1);
                return true;
            }
        }

        LOGNREPORT.sphnxInfo(ACTIONS, "Page Loaded!", "green", 1);
        return true;
    }


    //region wait methods

    /**
     * Function: WAIT
     * The purpose of this method is to wait a specified amouunt of time.
     * <p>
     * Parm1 is the number to wait
     * Parm2 is the interval to wait for and can be hours, minutes, seconds, or milliseconds
     * <p>
     * if Parm2 is blank or not one of the allowed values, seconds is used
     * <p>
     * <p>
     * To-Do Normalize this code
     */
    public boolean Wait(String amount, String interval) {

        float nbrAmount = Float.parseFloat(amount);

        String[] VALUES = new String[]{"hours", "minutes", "seconds", "milliseconds", "", null};
        boolean isValidInterval = Arrays.asList(VALUES).contains(interval.toLowerCase());

        if (isValidInterval) {
//        if (isValidInterval || (interval.equals("") || interval == null)){

            switch (interval.toUpperCase()) {
                case "HOURS":
                    nbrAmount = nbrAmount * 60 * 60 * 1000;
                    break;
                case "MINUTES":
                    nbrAmount = nbrAmount * 60 * 1000;
                    break;
                case "SECONDS":
                    nbrAmount = nbrAmount * 1000;
                    break;
                case "MILLISECONDS":
                    // already in MILLISECONDS, so nothing to do}
                    break;
                default:        //default to SECONDS if interval is not specified
                    nbrAmount = nbrAmount * 1000;
                    break;
            }

            // wait for specified amount of time
            try {
                double roundedUp = Math.ceil(nbrAmount);

                TimeUnit.MILLISECONDS.sleep((long) roundedUp);
            } catch (InterruptedException e) {
                LOGNREPORT.sphnxError(ACTIONS, "Error: WaitTime failed to wait " + amount + " " + interval);
                return false;
            }
        } else {
            LOGNREPORT.sphnxError(ACTIONS, "Error: Interval of '" + interval + "' is invalid for WaitTime. Valid values are: hours, minutes, seconds, milliseconds");
            return false;
        }

        return true;
    }

    public long getWaitForPageToLoad() {
        return waitForPageToLoad;
    }

    public long getWaitForElement() {
        return waitForElement;
    }

    public long getWaitForElementInvisibility() {
        return waitForElementInvisibility;
    }
    //endregion

    @Override
    public boolean execute() {
        return true;
    }

    /** ------------------------------- FEATURES ADDED IN FEBRUARY, 2018 ------------------------------- **/

    /**
     * The purpose of this method is to click on an element
     */
    public boolean simpleClick(String fullName) {
        WebElement toClick;
        try {
            toClick = this.webDriver.findElement(getLocator(fullName));
            if (isVisibleInViewport(toClick)) {
                this.waitForElementPresent(toClick);
                toClick.click();
            } else {
                LOGNREPORT.sphnxInfo(ACTIONS, ("Element with locator:" + fullName + " was obscured, scrolling element into view..."));
                ((JavascriptExecutor) this.webDriver).executeScript("arguments[0].scrollIntoView(true);", toClick);
                this.waitForElementPresent(toClick);
                toClick.click();
            }
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, "Error: Could not click element: " + fullName);
            return false;
        }
        return true;
    }

    /**
     * The purpose of this method is to click on an elementwait
     */
    public boolean simpleClick(WebElement element) {
        try {
            if (isVisibleInViewport(element)) {
                this.waitForElementPresent(element);
                element.click();
            } else {
                LOGNREPORT.sphnxInfo(ACTIONS, ("Element was obscured, scrolling element into view..."));
                ((JavascriptExecutor) this.webDriver).executeScript("arguments[0].scrollIntoView(true);", element);
                this.waitForElementPresent(element);
                element.click();
            }
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, "Error: Could not click element: ");
            return false;
        }
        return true;
    }

    /**
     * The purpose of this method is to click on an element
     */
    public boolean simpleClick(By selector) {
        WebElement toClick;
        try {
            toClick = this.webDriver.findElement(selector);
            if (isVisibleInViewport(toClick)) {
                this.waitForElementPresent(toClick);
                toClick.click();
            } else {
                LOGNREPORT.sphnxInfo(ACTIONS, ("Element with locator:" + selector + " was obscured, scrolling element into view..."));
                ((JavascriptExecutor) this.webDriver).executeScript("arguments[0].scrollIntoView(true);", toClick);
                this.waitForElementPresent(toClick);
                toClick.click();
            }
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, "Error: Could not click element: " + selector);
            return false;
        }
        return true;
    }

    /**
     * The purpose of this method enter a specified value
     * into the desired input field
     */
    public boolean sendKeys(String fullName, String value) {
        WebElement inputField;
        try {
            inputField = this.webDriver.findElement(getLocator(fullName));
            if (isVisibleInViewport(inputField)) {
                processKeys(value, inputField);
            } else {
                LOGNREPORT.sphnxInfo(ACTIONS, ("Element with locator:" + fullName + " was obscured, scrolling element into view... "));
                ((JavascriptExecutor) this.webDriver).executeScript("arguments[0].scrollIntoView(true);", inputField);
                processKeys(value, inputField);
            }
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, "Error: Could not enter text \"" + value +
                    "\" into input field: " + fullName);
            return false;
        }
        return true;
    }

    private boolean sendKeysHelper(String value, WebElement inputField) {
        inputField.click();
        inputField.sendKeys(value);
        return true;
    }

    /**
     * The purpose of this method enter a specified value
     * into the desired input field, with the option to
     * clear existing text in the input field.
     */
    public boolean sendKeys(String fullName, String value, boolean toClear) {
        WebElement inputField;
        try {
            inputField = this.webDriver.findElement(getLocator(fullName));
            if (isVisibleInViewport(inputField)) {
                if (toClear) {
                    inputField.clear();
                }
                processKeys(value, inputField);
            } else {
                LOGNREPORT.sphnxInfo(ACTIONS, ("Element with locator:" + fullName + " was obscured, scrolling element into view... "));
                ((JavascriptExecutor) this.webDriver).executeScript("arguments[0].scrollIntoView(true);", inputField);
                if (toClear) {
                    inputField.clear();
                }
                processKeys(value, inputField);
            }
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, "Error: Could not enter text \"" + value +
                    "\" into input field: " + fullName);
            return false;
        }
        return true;
    }

    public boolean sendKeys(By selector, String value) {
        WebElement inputField;
        try {
            inputField = this.webDriver.findElement(selector);
            if (isVisibleInViewport(inputField)) {
                processKeys(value, inputField);
            } else {
                LOGNREPORT.sphnxInfo(ACTIONS, ("Element with locator:" + selector + " was obscured, scrolling element into view... "));
                ((JavascriptExecutor) this.webDriver).executeScript("arguments[0].scrollIntoView(true);", inputField);
                processKeys(value, inputField);
            }
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, "Error: Could not enter text \"" + value +
                    "\" into input field: " + selector);
            return false;
        }
        return true;
    }

    /**
     * The purpose of this method enter a specified value
     * into the desired input field, with the option to
     * clear existing text in the input field.
     */
    public boolean sendKeys(By selector, String value, boolean toClear) {
        WebElement inputField;
        try {
            inputField = this.webDriver.findElement(selector);
            if (isVisibleInViewport(inputField)) {
                if (toClear) {
                    inputField.clear();
                }
                processKeys(value, inputField);
            } else {
                LOGNREPORT.sphnxInfo(ACTIONS, ("Element with locator:" + selector + " was obscured, scrolling element into view... "));
                ((JavascriptExecutor) this.webDriver).executeScript("arguments[0].scrollIntoView(true);", inputField);
                if (toClear) {
                    inputField.clear();
                }
                processKeys(value, inputField);
            }
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, "Error: Could not enter text \"" + value +
                    "\" into input field: " + selector);
            return false;
        }
        return true;
    }

    /**
     * The purpose of this method enter a specified value
     * into the desired input field
     */
    public boolean sendKeys(String fullName, Keys key) {
        WebElement inputField;
        try {
            inputField = this.webDriver.findElement(getLocator(fullName));
            if (isVisibleInViewport(inputField)) {
                processKeys(key, inputField);
            } else {
                LOGNREPORT.sphnxInfo(ACTIONS, ("Element with locator:" + fullName + " was obscured, scrolling element into view... "));
                ((JavascriptExecutor) this.webDriver).executeScript("arguments[0].scrollIntoView(true);", inputField);
                processKeys(key, inputField);
            }
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, "Error: Could not enter text \"" + key +
                    "\" into input field: " + fullName);
            return false;
        }
        return true;
    }

    /**
     * The purpose of this method enter a specified value
     * into the desired input field, with the option to
     * clear existing text in the input field.
     */
    public boolean sendKeys(String fullName, Keys key, boolean toClear) {
        WebElement inputField;
        try {
            inputField = this.webDriver.findElement(getLocator(fullName));
            if (isVisibleInViewport(inputField)) {
                if (toClear) {
                    inputField.clear();
                }
                processKeys(key, inputField);
            } else {
                LOGNREPORT.sphnxInfo(ACTIONS, ("Element with locator:" + fullName + " was obscured, scrolling element into view... "));
                ((JavascriptExecutor) this.webDriver).executeScript("arguments[0].scrollIntoView(true);", inputField);
                if (toClear) {
                    inputField.clear();
                }
                processKeys(key, inputField);
            }
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, "Error: Could not enter text \"" + key +
                    "\" into input field: " + fullName);
            return false;
        }
        return true;
    }

    public boolean sendKeys(By selector, Keys key) {
        WebElement inputField;
        try {
            inputField = this.webDriver.findElement(selector);
            if (isVisibleInViewport(inputField)) {
                processKeys(key, inputField);
            } else {
                LOGNREPORT.sphnxInfo(ACTIONS, ("Element with locator:" + selector + " was obscured, scrolling element into view... "));
                ((JavascriptExecutor) this.webDriver).executeScript("arguments[0].scrollIntoView(true);", inputField);
                processKeys(key, inputField);
            }
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, "Error: Could not enter text \"" + key +
                    "\" into input field: " + selector);
            return false;
        }
        return true;
    }

    /**
     * The purpose of this method enter a specified value
     * into the desired input field, with the option to
     * clear existing text in the input field.
     */
    public boolean sendKeys(By selector, Keys key, boolean toClear) {
        WebElement inputField;
        try {
            inputField = this.webDriver.findElement(selector);
            if (isVisibleInViewport(inputField)) {
                if (toClear) {
                    inputField.clear();
                }
                processKeys(key, inputField);
            } else {
                LOGNREPORT.sphnxInfo(ACTIONS, ("Element with locator:" + selector + " was obscured, scrolling element into view... "));
                ((JavascriptExecutor) this.webDriver).executeScript("arguments[0].scrollIntoView(true);", inputField);
                if (toClear) {
                    inputField.clear();
                }
                processKeys(key, inputField);
            }
        } catch (Exception e) {
            LOGNREPORT.sphnxError(ACTIONS, "Error: Could not enter text \"" + key +
                    "\" into input field: " + selector);
            return false;
        }
        return true;
    }

    public WebElement findWebDriverElement(String fullName) {
        WebElement element = this.webDriver.findElement(getLocator(fullName));
        if (isVisibleInViewport(element)) {
            this.waitForElementPresent(element);
            return element;
        } else {
            this.waitForElementPresent(element);
            ((JavascriptExecutor) this.webDriver).executeScript("arguments[0].scrollIntoView(true);", element);
            return element;
        }
    }

    private boolean sendKeysHelper(String value, WebElement inputField, boolean toClear) {
        if (toClear) {
            inputField.click();
            inputField.clear();
        }
        inputField.sendKeys(value);
        return true;
    }

    public WebElement findWebDriverElement(By selector) {
        WebElement element = this.webDriver.findElement(selector);
        if (isVisibleInViewport(element)) {
            this.waitForElementPresent(element);
            return element;
        } else {
            this.waitForElementPresent(element);
            ((JavascriptExecutor) this.webDriver).executeScript("arguments[0].scrollIntoView(true);", element);
            return element;
        }
    }

    public WebElement findWebDriverElement(WebElement element, By selector) {
        element.findElement(selector);
        if (isVisibleInViewport(element)) {
            this.waitForElementPresent(element);
            return element;
        } else {
            this.waitForElementPresent(element);
            ((JavascriptExecutor) this.webDriver).executeScript("arguments[0].scrollIntoView(true);", element);
            return element;
        }
    }

    public List<WebElement> findWebDriverElements(String fullName) {
        List<WebElement> elements = this.webDriver.findElements(getLocator(fullName));
        this.waitForElementsPresent(elements);
        return elements;
    }

    public List<WebElement> findWebDriverElements(By selector) {
        List<WebElement> elements = this.webDriver.findElements(selector);
        this.waitForElementsPresent(elements);
        return elements;
    }

    public void checkVisibility(String fullName) {
        WebElement element = this.webDriver.findElement(getLocator(fullName));
        if (isVisibleInViewport(element)) {
            LoggerReporter.LOGNREPORT.sphnxInfo(ACTIONS, element.getText() + " is in view.");
        } else {
            LoggerReporter.LOGNREPORT.sphnxInfo(ACTIONS, "Scrolling " + element.getText() + " into view...");
            ((JavascriptExecutor) this.webDriver).executeScript("arguments[0].scrollIntoView(true);", element);
        }
    }

    public void checkVisibility(By selector) {
        WebElement element = this.webDriver.findElement(selector);
        if (isVisibleInViewport(element)) {
            LoggerReporter.LOGNREPORT.sphnxInfo(ACTIONS, element.getText() + " is in view.");
        } else {
            LoggerReporter.LOGNREPORT.sphnxInfo(ACTIONS, "Scrolling " + element.getText() + " into view...");
            ((JavascriptExecutor) this.webDriver).executeScript("arguments[0].scrollIntoView(true);", element);
        }
    }

    public void checkVisibility(WebElement element) {
        if (isVisibleInViewport(element)) {
            LoggerReporter.LOGNREPORT.sphnxInfo(ACTIONS, element.getText() + " is in view.");
        } else {
            LoggerReporter.LOGNREPORT.sphnxInfo(ACTIONS, "Scrolling " + element.getText() + " into view...");
            ((JavascriptExecutor) this.webDriver).executeScript("arguments[0].scrollIntoView(true);", element);
        }
    }

    /**
     * WAITS
     **/
    public boolean waitForElementToBeVisible(String fullName) throws Exception{
        boolean isDisplayed;
        int counter = 0;
        this.waitForElementPresent(fullName);
 //       try {
            WebElement element = this.webDriver.findElement(getLocator(fullName));
            isDisplayed = element.isDisplayed();
            while (isDisplayed == false) {
                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
                element = this.webDriver.findElement(getLocator(fullName));
                isDisplayed = element.isDisplayed();
                counter++;
                if (counter == 60) {
                    throw new Exception("Timed out after 30 seconds waiting for " + fullName + " to be visible.");
                }
            }
            return true;
//        } catch (Exception e) {
//            return false;
//        }
    }

    public boolean waitForElementToBeVisible(By selector) throws Exception {
        boolean isDisplayed;
        int counter = 0;
        this.waitForElementPresent(selector);
//        try {
            WebElement element = this.webDriver.findElement(selector);
            isDisplayed = element.isDisplayed();
            while (isDisplayed == false) {
                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
                element = this.webDriver.findElement(locator);
                isDisplayed = element.isDisplayed();
                counter++;
                if (counter == 60) {
                    throw new Exception("Timed out after 30 seconds waiting for " + locator + " to be visible.");
                }
            }
            return true;
//        } catch (Exception e) {
//            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in waitForElement with locator:" +
//                    locator + " and timeout: " + 30);
//            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
//            return false;
//        }
    }

    public boolean waitForElementToBeVisible(WebElement element) throws Exception {
        boolean isDisplayed;
        int counter = 0;
        this.waitForElementPresent(element);
            isDisplayed = element.isDisplayed();
            while (isDisplayed == false) {
                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
                isDisplayed = element.isDisplayed();
                counter++;
                if (counter == 60) {
                    throw new Exception("Timed out after 30 seconds waiting for " + element.getText() + " to be visible.");
                }
            }
            return true;
    }

    public boolean waitForElementToBeVisible(String fullName, long seconds) throws Exception {
        boolean isDisplayed;
        int counter = 0;
        this.waitForElementPresent(fullName, seconds);
            WebElement element = this.webDriver.findElement(getLocator(fullName));
            isDisplayed = element.isDisplayed();
            while (isDisplayed == false) {
                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
                element = this.webDriver.findElement(getLocator(fullName));
                isDisplayed = element.isDisplayed();
                counter++;
                if (counter == (seconds * 2)) {
                    throw new Exception("Timed out after " + seconds + " seconds waiting for " + fullName + " to be visible.");
                }
            }
            return true;
    }

    public boolean waitForElementToBeVisible(By selector, long seconds) throws Exception{
        boolean isDisplayed;
        int counter = 0;
        this.waitForElementPresent(selector);
            WebElement element = this.webDriver.findElement(locator);
            isDisplayed = element.isDisplayed();
            while (isDisplayed == false) {
                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
                element = this.webDriver.findElement(locator);
                isDisplayed = element.isDisplayed();
                counter++;
                if (counter == (seconds * 2)) {
                    throw new Exception("Timed out after " + seconds + " seconds waiting for " + locator + " to be visible.");
                }
            }
            return true;
    }

    public boolean waitForElementToBeVisible(WebElement element, long seconds) throws Exception{
        boolean isDisplayed;
        int counter = 0;
        this.waitForElementPresent(element);
            isDisplayed = element.isDisplayed();
            while (isDisplayed == false) {
                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
                isDisplayed = element.isDisplayed();
                counter++;
                if (counter == (seconds * 2)) {
                    throw new Exception("Timed out after " + seconds + " seconds waiting for " + element.getText() + " to be visible.");
                }
            }
            return true;
    }
    public boolean waitForElementToBeVisible(String fullName, boolean continueNotFound) throws Exception{
        boolean isDisplayed;
        int counter = 0;
        this.waitForElementPresent(fullName);
            WebElement element = this.webDriver.findElement(getLocator(fullName));
            isDisplayed = element.isDisplayed();
            while (isDisplayed == false) {
                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
                element = this.webDriver.findElement(getLocator(fullName));
                isDisplayed = element.isDisplayed();
                counter++;
                if (counter == 60) {
                    return false;
                }
            }
            return true;
    }

    public boolean waitForElementToBeVisible(By selector, boolean continueNotFound) throws Exception {
        boolean isDisplayed;
        int counter = 0;
        this.waitForElementPresent(selector);
            WebElement element = this.webDriver.findElement(selector);
            isDisplayed = element.isDisplayed();
            while (isDisplayed == false) {
                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
                element = this.webDriver.findElement(locator);
                isDisplayed = element.isDisplayed();
                counter++;
                if (counter == 60) {
                    return false;
                }
            }
            return true;
    }

    public boolean waitForElementToBeVisible(WebElement element, boolean continueNotFound) throws Exception {
        boolean isDisplayed;
        int counter = 0;
        this.waitForElementPresent(element);
            isDisplayed = element.isDisplayed();
            while (isDisplayed == false) {
                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
                isDisplayed = element.isDisplayed();
                counter++;
                if (counter == 60) {
                    return false;
                }
            }
            return true;
    }

    public boolean waitForElementToBeVisible(String fullName, long seconds, boolean continueNotFound) throws Exception {
        boolean isDisplayed;
        int counter = 0;
        this.waitForElementPresent(fullName);
            WebElement element = this.webDriver.findElement(getLocator(fullName));
            isDisplayed = element.isDisplayed();
            while (isDisplayed == false) {
                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
                element = this.webDriver.findElement(getLocator(fullName));
                isDisplayed = element.isDisplayed();
                counter++;
                if (counter == (seconds * 2)) {
                    return false;
                }
            }
            return true;
    }

    public boolean waitForElementToBeVisible(By selector, long seconds, boolean continueNotFound) throws Exception{
        boolean isDisplayed;
        int counter = 0;
        this.waitForElementPresent(selector);
            WebElement element = this.webDriver.findElement(locator);
            isDisplayed = element.isDisplayed();
            while (isDisplayed == false) {
                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
                element = this.webDriver.findElement(locator);
                isDisplayed = element.isDisplayed();
                counter++;
                if (counter == (seconds * 2)) {
                    return false;
                }
            }
            return true;
    }

    public boolean waitForElementToBeVisible(WebElement element, long seconds, boolean continueNotFound) throws Exception{
        boolean isDisplayed;
        int counter = 0;
        this.waitForElementPresent(element);
            isDisplayed = element.isDisplayed();
            while (isDisplayed == false) {
                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
                isDisplayed = element.isDisplayed();
                counter++;
                if (counter == (seconds * 2)) {
                    return false;
                }
            }
            return true;
    }

    public boolean waitForElementNotVisible(String fullName) {
        boolean isDisplayed;
        int counter = 0;
        this.waitForElementPresent(fullName);
        try {
            WebElement element = this.webDriver.findElement(getLocator(fullName));
            isDisplayed = element.isDisplayed();
            while (isDisplayed == true) {
                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
                element = this.webDriver.findElement(getLocator(fullName));
                isDisplayed = element.isDisplayed();
                counter++;
                if (counter == 60) {
                    throw new Exception("Timed out after 30 seconds waiting for " + fullName + " to disappear.");
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean waitForElementNotVisible(By selector) {
        boolean isDisplayed;
        int counter = 0;
        this.waitForElementPresent(selector);
        try {
            WebElement element = this.webDriver.findElement(locator);
            isDisplayed = element.isDisplayed();
            while (isDisplayed == true) {
                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
                element = this.webDriver.findElement(locator);
                isDisplayed = element.isDisplayed();
                counter++;
                if (counter == 60) {
                    throw new Exception("Timed out after 30 seconds waiting for " + locator + " to disappear.");
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean waitForElementNotVisible(WebElement element) {
        boolean isDisplayed;
        int counter = 0;
        this.waitForElementPresent(element);
        try {
            isDisplayed = element.isDisplayed();
            while (isDisplayed == true) {
                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
                isDisplayed = element.isDisplayed();
                counter++;
                if (counter == 60) {
                    throw new Exception("Timed out after 30 seconds waiting for " + element.getText() + " to disappear.");
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean waitForElementNotVisible(String fullName, long seconds) {
        boolean isDisplayed;
        int counter = 0;
        this.waitForElementPresent(fullName);
        try {
            WebElement element = this.webDriver.findElement(getLocator(fullName));
            isDisplayed = element.isDisplayed();
            while (isDisplayed == true) {
                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
                element = this.webDriver.findElement(getLocator(fullName));
                isDisplayed = element.isDisplayed();
                counter++;
                if (counter == (seconds * 2)) {
                    throw new Exception("Timed out after " + seconds + " seconds waiting for " + fullName + " to disappear.");
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean waitForElementNotVisible(By selector, long seconds) {
        boolean isDisplayed;
        int counter = 0;
        this.waitForElementPresent(selector);
        try {
            WebElement element = this.webDriver.findElement(locator);
            isDisplayed = element.isDisplayed();
            while (isDisplayed == true) {
                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
                element = this.webDriver.findElement(locator);
                isDisplayed = element.isDisplayed();
                counter++;
                if (counter == (seconds * 2)) {
                    throw new Exception("Timed out after " + seconds + " seconds waiting for " + locator + " to disappear.");
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean waitForElementNotVisible(WebElement element, long seconds) {
        boolean isDisplayed;
        int counter = 0;
        this.waitForElementPresent(element);
        try {
            isDisplayed = element.isDisplayed();
            while (isDisplayed == true) {
                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
                isDisplayed = element.isDisplayed();
                counter++;
                if (counter == (seconds * 2)) {
                    throw new Exception("Timed out after " + seconds + " seconds waiting for " + element.getText() + " to disappear.");
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean waitForElementToBeClickable(String fullName) throws Exception {
        this.waitForElementPresent(fullName);
        try {
            WebDriverWait wait = new WebDriverWait(this.webDriver, 10);
            wait.until(ExpectedConditions.elementToBeClickable(getLocator(fullName)));
            return true;
        } catch (Exception e) {
            throw new Exception("Timed out after 10 seconds waiting for " + fullName + " to be clickable.");
        }
    }

    public boolean waitForElementToBeClickable(By selector) throws Exception {
        this.waitForElementPresent(selector);
        try {
            WebDriverWait wait = new WebDriverWait(this.webDriver, 10);
            wait.until(ExpectedConditions.elementToBeClickable(selector));
            return true;
        } catch (Exception e) {
            throw new Exception("Timed out after 10 seconds waiting for element " + selector + " to be clickable.");

        }
    }

    public boolean waitForElementToBeClickable(WebElement e) throws Exception {
        this.waitForElementPresent(e);
        try {
            WebDriverWait wait = new WebDriverWait(this.webDriver, 10);
            wait.until(ExpectedConditions.elementToBeClickable(e));
            return true;
        } catch (Exception e1) {
            throw new Exception("Timed out after 10 seconds waiting for element " + e.getText() + " to be clickable.");
        }
    }

    public boolean waitForElementToBeClickable(String fullName, long seconds) throws Exception {
        this.waitForElementPresent(fullName);
        try {
            WebDriverWait wait = new WebDriverWait(this.webDriver, seconds);
            wait.until(ExpectedConditions.elementToBeClickable(getLocator(fullName)));
            return true;
        } catch (Exception e) {
            throw new Exception("Timed out after " + seconds + " seconds waiting for " + fullName + " to be clickable.");

        }
    }

    public boolean waitForElementToBeClickable(By selector, long seconds) throws Exception {
        this.waitForElementPresent(selector);
        try {
            WebDriverWait wait = new WebDriverWait(this.webDriver, seconds);
            wait.until(ExpectedConditions.elementToBeClickable(selector));
            return true;
        } catch (Exception e) {
            throw new Exception("Timed out after " + seconds + " waiting for element " + selector + " to be clickable.");

        }
    }

    public boolean waitForElementToBeClickable(WebElement e, long seconds) throws Exception {
        this.waitForElementPresent(e);
        try {
            WebDriverWait wait = new WebDriverWait(this.webDriver, seconds);
            wait.until(ExpectedConditions.elementToBeClickable(e));
            return true;
        } catch (Exception e1) {
            throw new Exception("Timed out after " + seconds + " waiting for element " + e.getText() + " to be clickable.");
        }
    }

    /** END WAITS **/

//    /**
//     * Validate if web element is present
//     *
//     * @param fullName: The locaton for the element
//     * @return boolean: true for present else false
//     */
//    public boolean isElementPresent(String fullName) {
//        WebElement element;
//        try {
//            this.waitForElementPresent(fullName);
//            element = this.webDriver.findElement(getLocator(fullName));
//
//            if (element.isDisplayed()) {
//                LOGNREPORT.sphnxInfo(ACTIONS, "Element " + fullName + " is displayed on the screen.", "blue", 1, true);
//                return true;
//            } else {
//                LOGNREPORT.sphnxError(ACTIONS, "Element " + fullName + " is not displayed on the screen.", "red", 1, true);
//                return false;
//            }
//        } catch (Exception e) {
//            LOGNREPORT.sphnxError(ACTIONS, "Element " + fullName + " could not be located in the HTML.", "red", 1, true);
//            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
//            return false;
//        }
//    }
//
//    //if logAsError is false, then don't report as an error if element is not found as it may not be expected to be found.  It will report as a warning if
//    //logAsError is true and the element is not found
//    public boolean isElementPresent(String fullName, boolean logAsError) {
//        WebElement element;
//        if (logAsError) {
//            return isElementPresent(fullName);
//        } else {
//            try {
//                this.waitForElementPresent(fullName);
//                element = this.webDriver.findElement(getLocator(fullName));
//
//                if (element.isDisplayed()) {
//                    LOGNREPORT.sphnxInfo(ACTIONS, "Element " + locator + " is displayed on the screen.", "blue", 1, true);
//                    return true;
//                } else {
//                    LOGNREPORT.sphnxWarning(ACTIONS, "Element " + locator + " is not displayed on the screen.", "yellow", 1, true);
//                    return false;
//                }
//            } catch (Exception e) {
//                LOGNREPORT.sphnxError(ACTIONS, "Element " + locator + " could not be located in the HTML.", "red", 1, true);
//                if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
//                return false;
//            }
//        }
//    }
//
//    public boolean isElementPresent(String fullName, long waitForSeconds) {
//        WebElement element;
//
//        try {
//            this.waitForElementPresent(fullName);
//            element = this.webDriver.findElement(getLocator(fullName));
//
//            if (element.isDisplayed()) {
//                LOGNREPORT.sphnxInfo(ACTIONS, "Element " + fullName + " is displayed on the screen.", "blue", 1, true);
//                return true;
//            } else {
//                LOGNREPORT.sphnxError(ACTIONS, "Element " + fullName + " is not displayed on the screen.", "red", 1, true);
//                return false;
//            }
//        } catch (Exception e) {
//            LOGNREPORT.sphnxError(ACTIONS, "Element " + fullName + " could not be located in the HTML.", "red", 1, true);
//            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
//            return false;
//        }
//    }
//
//        //if logAsError is false, then don't report as an error if element is not found as it may not be expected to be found.  It will report as a warning if
//        //logAsError is true and the element is not found
//        public boolean isElementPresent (String fullName, long waitForSeconds, boolean logAsError){
//            WebElement element;
//            WebDriverWait wait = new WebDriverWait(this.webDriver, waitForSeconds);
//
//            if (logAsError) {
//                return isElementPresent(fullName, waitForSeconds);
//            } else {
//                wait.withTimeout(waitForSeconds, TimeUnit.SECONDS);
//                try {
//                    element = this.webDriver.findElement(getLocator(fullName));
//
//                    if (element.isDisplayed()) {
//                        LOGNREPORT.sphnxInfo(ACTIONS, "Element " + fullName + " is displayed on the screen.", "blue", 1, true);
//                        return true;
//                    } else {
//                        LOGNREPORT.sphnxWarning(ACTIONS, "Element " + fullName + " is not displayed on the screen.", "red", 1, true);
//                        return false;
//                    }
//                } catch (Exception e) {
//                    LOGNREPORT.sphnxError(ACTIONS, "Element " + fullName + " could not be located in the HTML.", "red", 1, true);
//                    if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
//                    return false;
//                }
//            }
//        }

    public boolean isVisible(String fullName) {
        try {
            this.webDriver.findElement(getLocator(fullName)).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }

    public boolean isVisible(By selector) {
        try {
            this.webDriver.findElement(selector).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }

    public boolean isVisible(WebElement element) {
        try {
            element.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }

    public boolean isVisibleAfter(String fullName, long seconds) {
        WebDriverWait wait = new WebDriverWait(this.webDriver, seconds);
        try {
        wait.until(ExpectedConditions.visibilityOfElementLocated(getLocator(fullName)));

            if (this.webDriver.findElement(getLocator(fullName)).isDisplayed()) {
                return true;
            } else return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isVisibleAfter(By selector, long seconds) {
        WebDriverWait wait = new WebDriverWait(this.webDriver, seconds);
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(selector));
            if (this.webDriver.findElement(selector).isDisplayed()) {
                return true;
            } else return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSelected(String fullName) {
        try {
            if (this.webDriver.findElement(getLocator(fullName)).isSelected()) {
                return true;
            } else return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSelected(By selector) {
        try {
            if (this.webDriver.findElement(selector).isSelected()) {
                return true;
            } else return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSelected(WebElement element) {
        try {
            if (element.isSelected()) {
                return true;
            } else return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * The purpose of this method is to store a value, provided by a user for later use
     * <p>
     * To-Do Normalize this code
     */
    public boolean setECValue(String key, String value) {
        if (key.trim().length() > 0) {
            try {
                ExecutionContext.setECValue(key, value);
                ExecutionContext.writeToECFile();
                LOGNREPORT.sphnxInfo(ACTIONS, "Stored Key/Value pair: " + key + ":" + value, true);
                return true;
            } catch (Exception e) {
                LOGNREPORT.sphnxError(ACTIONS, "Failed to store Key/Value pair: " + key + ":" + value);
                return false;
            }
        } else {
            LOGNREPORT.sphnxError(ACTIONS, "Error: Key is zero length");
            return false;
        }
    }

    /**
     * The purpose of this method is to store a value, provided by a user for later use
     * <p>
     * To-Do Normalize this code
     */
    public boolean removeECKey(String key) {
        if (key.trim().length() > 0) {
            try {
                ExecutionContext.removeECKey(key);
                ExecutionContext.removeFromECFile(key);
                LOGNREPORT.sphnxInfo(ACTIONS, "Removed Key from Execution Context: " + key, true);
                return true;
            } catch (Exception e) {
                LOGNREPORT.sphnxError(ACTIONS, "Failed to remove Key from Execution Conext: " + key);
                return false;
            }
        } else {
            LOGNREPORT.sphnxError(ACTIONS, "Error: Key is zero length");
            return false;
        }
    }

    /**
     * The purpose of this method is to get a value from the execution context
     */
    public String getECValue(String key) {
        try {
            String ecValue = (String) ExecutionContext.getECValue(key);
            LOGNREPORT.sphnxPASS(ACTIONS, "Retrieved Key/Value pair: " + key + ":" + ecValue);
            return ecValue;
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in getECValue locator -  key:" + key + " is not valid.");
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return "";
        }
    }

    /**
     * The purpose of this method is to get a value from the execution context
     */
    public Object getECValueAsObject(String key) {
        try {
            Object ecValue = ExecutionContext.getECValue(key);
            LOGNREPORT.sphnxPASS(ACTIONS, "Retrieved Key/Value pair: " + key + ":" + ecValue);
            return ecValue;
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in getECValueAsObject locator -  key:" + key + " is not valid.");
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return null;
        }
    }

    /**
     * Get the name of the current browser instance
     *
     * @return String: browser name
     */
    public String getBrowserName() {
        Capabilities cap = ((RemoteWebDriver) this.webDriver).getCapabilities();
        return cap.getBrowserName().toLowerCase();
    }


    public void startEvent() {
        Variables.EVENT_START = System.nanoTime();
    }

    public void endEvent() {
        Variables.EVENT_END = System.nanoTime();
    }

    public double getEventTime() throws Exception {
        if (Variables.EVENT_START > Variables.EVENT_END) {
            throw new Exception("There was an error calculating event time.  " +
                    "\nPlease be sure to call startEvent() before the call to endEvent()!");
        } else {
            long elapsedTime = Variables.EVENT_END - Variables.EVENT_START;
            return (double) elapsedTime / 1000000000.0;
        }
    }

    /**
     * ----------------------------------------------- HELPERS ---------------------------------------------------------
     **/

    private void waitForElementPresent(String fullName) {
        WebDriverWait wait = new WebDriverWait(this.webDriver, 10);
        wait.until(ExpectedConditions.presenceOfElementLocated(getLocator(fullName)));
    }

    private void waitForElementPresent(String fullName, long timeOutInSeconds) {
        WebDriverWait wait = new WebDriverWait(this.webDriver, timeOutInSeconds);
        wait.until(ExpectedConditions.presenceOfElementLocated(getLocator(fullName)));
    }

    private void waitForElementPresent(By selector) {
        WebDriverWait wait = new WebDriverWait(this.webDriver, 10);
        wait.until(ExpectedConditions.presenceOfElementLocated(selector));
    }

    private void waitForElementPresent(By selector, long timeOutInSeconds) {
        WebDriverWait wait = new WebDriverWait(this.webDriver, timeOutInSeconds);
        wait.until(ExpectedConditions.presenceOfElementLocated(selector));
    }

    private void waitForElementPresent(WebElement element) {
        WebDriverWait wait = new WebDriverWait(this.webDriver, 10);
        wait.until(new ExpectedCondition<Boolean>() {
            @Nullable
            @Override
            public Boolean apply(@Nullable WebDriver webDriver) {
                if (element != null) {
                    return Boolean.TRUE;
                }
                return null;
            }
        });
    }

    private void waitForElementPresent(WebElement element, long timeOutInSeconds) {
        WebDriverWait wait = new WebDriverWait(this.webDriver, timeOutInSeconds);
        wait.until(new ExpectedCondition<Boolean>() {
            @Nullable
            @Override
            public Boolean apply(@Nullable WebDriver webDriver) {
                if (element != null) {
                    return Boolean.TRUE;
                }
                return null;
            }
        });
    }

    private void waitForElementsPresent(List<WebElement> elements) {
        WebDriverWait wait = new WebDriverWait(this.webDriver, 10);
        wait.until(new ExpectedCondition<Boolean>() {
            @Nullable
            @Override
            public Boolean apply(@Nullable WebDriver webDriver) {
                if (elements.size() > 0) {
                    return Boolean.TRUE;
                }
                return null;
            }
        });
    }

    private void waitForElementsPresent(List<WebElement> elements, long timeOutInSeconds) {
        WebDriverWait wait = new WebDriverWait(this.webDriver, timeOutInSeconds);
        wait.until(new ExpectedCondition<Boolean>() {
            @Nullable
            @Override
            public Boolean apply(@Nullable WebDriver webDriver) {
                if (elements.size() > 0) {
                    return Boolean.TRUE;
                }
                return null;
            }
        });
    }

    public boolean waitForAttributeValue(String locator, String attribute, String value, long timeout){
        try {
            WebDriverWait wait = new WebDriverWait(this.webDriver,timeout);

            return wait.until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver driver) {
                    WebElement element = driver.findElement(getLocator(locator));
                    String enabled = element.getAttribute(attribute);
                    if(enabled.equals(value))
                        return true;
                    else
                        return false;
                }
            });

        }
        catch (Exception e) {
            LOGNREPORT.sphnxFAIL(ACTIONS, "Exception in waitForAttributeValue locator :" + locator +
                    " attribute:" + attribute +
                    " value:" + value +
                    " timeout:" + timeout);
            if (configurations.getAllConfigurations().containsKey("iscomplex")) throw e;
            return false;
        }
    }

    private void processKeys(String value, WebElement inputField) {
        this.waitForElementPresent(inputField);
        inputField.click();
        inputField.sendKeys(value);
    }

    private void processKeys(Keys key, WebElement inputField) {
        this.waitForElementPresent(inputField);
        inputField.click();
        inputField.sendKeys(key);
    }

    private Boolean isVisibleInViewport(WebElement element) {

        return (Boolean) ((JavascriptExecutor) this.webDriver).executeScript(
                "var elem = arguments[0],                 " +
                        "  box = elem.getBoundingClientRect(),    " +
                        "  cx = box.left + box.width / 2,         " +
                        "  cy = box.top + box.height / 2,         " +
                        "  e = document.elementFromPoint(cx, cy); " +
                        "for (; e; e = e.parentElement) {         " +
                        "  if (e === elem)                        " +
                        "    return true;                         " +
                        "}                                        " +
                        "return false;                            "
                , element);
    }
}
