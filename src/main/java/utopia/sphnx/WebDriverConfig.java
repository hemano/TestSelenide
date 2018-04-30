package utopia.sphnx;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Component;
import utopia.sphnx.actions.Actions;
import utopia.sphnx.factory.DriverFactory;
import utopia.sphnx.parser.LocatorLookup;

import java.io.IOException;

import static utopia.sphnx.logging.LoggerReporter.LOGNREPORT;

/**
 * Created by jitendrajogeshwar on 19/05/17.
 */
@Configuration
@Component
@ComponentScan("utopia.sphnx")
public class WebDriverConfig {

    private static String WEBDRIVERCONFIG = utopia.sphnx.WebDriverConfig.class.getCanonicalName();
    private static ThreadLocal<WebDriver> webDriver = new ThreadLocal<WebDriver>();

    @Bean
    public LocatorLookup locatorLookup() {
        LocatorLookup locatorLookup = new LocatorLookup();
        return locatorLookup;
    }

    public static void setWebDriver(WebDriver driver){
        webDriver.set(driver);
    }

    @Bean(name = "actions")
    @Scope(value = "prototype")
    @Lazy(value = true)
    public Actions getActions(ApplicationContext applicationContext) {
        return new Actions(applicationContext);
    }

    /**
     * Use this bean to create instance of web
     *
     * @param browserName
     * @return
     * @throws IOException
     */
    @Bean
    @Scope(value = "prototype")
    @Autowired(required = false)
    public static WebDriver createWebDriverInstance(String browserName) {
        if (webDriver.get() == null) {
            WebDriver driver = null;
            //DriverFactory.instantiateDriverObject();
            driver = DriverFactory.getDriver();
            utopia.sphnx.WebDriverConfig.setWebDriver(driver);
//            //Setting browser dimensions
//
//            //browser size not required for Android or iOS devices
//           if (!(browserName.toLowerCase().contains("android") || browserName.toLowerCase().contains("ios"))) {
//
//                if (browserName.toLowerCase().contains("firefox") || browserName.toLowerCase().contains("safari")) {
//                    driver.manage().window().maximize();
//                } else {
//                    ParseConfigurations configurations = new ParseConfigurations();
//                    if (configurations.getAllConfigurations().containsKey(ParseConfigurations.Configs.RUN_HEADLESS.name())) {
//                        if (configurations.getAllConfigurations().get(ParseConfigurations.Configs.RUN_HEADLESS.name()).equalsIgnoreCase("true")) {
//                            if (browserName.toLowerCase().contains("chrome")) {
//                                Dimension d = new Dimension(1920, 1080);
//                                driver.manage().window().setSize(d);
//                            }
//                        } else {
//                            driver.manage().window().maximize();
//                        }
//                    }
//                }
//            }
            webDriver.set(driver);
        }
        return webDriver.get();
    }


    @Bean
    @Scope(value = "prototype")
    @Autowired(required = false)
    public static Object killInstance(){
        try {
            //kill the browsers
            DriverFactory.closeDriverObjects();
            webDriver.set(null);
            return null;
        } catch (Exception e) {
            LOGNREPORT.sphnxError(WEBDRIVERCONFIG, "Error closing browser :" + e.getLocalizedMessage());
            throw e;
        }
    }

    @Bean(name = "actionsClassName")
    public String getActionClassName() {
        return Actions.class.getCanonicalName();
    }

    @Bean(name = "lookUp")
    @Scope(value = "prototype")
    @Lazy(value = true)
    @Autowired(required = false)
    public By locatorLookup(String locator) {
        try {
            return (By) new LocatorLookup().resolve(locator);
        } catch (Exception e) {
            LOGNREPORT.sphnxError(WEBDRIVERCONFIG,
                    "Error in fetching BY object for locator :" + locator
                            + " Error is " + e.getLocalizedMessage());
            throw e;
        }
    }

}
