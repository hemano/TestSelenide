package utopia.sphnx.factory;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import utopia.sphnx.config.ParseConfigurations;
import utopia.sphnx.core.appium.manager.AppiumDriverManager;
import utopia.sphnx.exceptions.DriverException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static utopia.sphnx.logging.LoggerReporter.LOGNREPORT;

/**
 * @author hemantojha
 * <p>
 * DriverType implements DriverSetup interface
 * <p>
 * It has enums for all the types of Driver Types
 * This is what is passed from System Property variable as
 * driver_id=SAUCE_FIREFOX
 * </p>
 */
public enum DriverType implements DriverSetup {

    FIREFOX {
        public DesiredCapabilities getDesiredCapabilities() {
            DesiredCapabilities capabilities =
                    DesiredCapabilities.firefox();
            capabilities.setCapability("marionette", true);
            return capabilities;
        }

        public WebDriver getWebDriverObject(DesiredCapabilities capabilities) {
            try {

                configureGecko();

                File userDir = new File(System.getProperty("user.dir"));
//                String dlPath =  Paths.get(userDir.getParent()).getParent().toString() + File.separator
//                        + "resources" + File.separator
//                        + "downloads";
                String dlPath = ParseConfigurations.Configs.RESOURCES_DIR.config() + File.separator
                        + "downloads";
                FirefoxProfile profile = new FirefoxProfile();
                profile.setPreference("browser.download.dir", dlPath);
                profile.setPreference("browser.download.folderList", 2);
                profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
                        "image/jpeg, application/pdf, application/octet-stream, " +
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                profile.setPreference("pdfjs.disabled", true);

                capabilities.setCapability(FirefoxDriver.PROFILE, profile);

                return new FirefoxDriver(capabilities);
            } catch (Exception e) {
                throw new WebDriverException("Unable to launch the browser", e);
            }
        }
    },
    CHROME {
        public DesiredCapabilities getDesiredCapabilities() {

            configureChrome();




//            options.addArguments("disable-infobars");
//            options.setAcceptInsecureCerts(true);
//            options.setCapability("chrome.switches", Arrays.asList("--no-default-browser-check"));


/*            ChromeOptions options = new ChromeOptions();
            Map<String, Object> prefs = new HashMap<String, Object>();
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);
            prefs.put("profile.default_content_settings.popups", 0);


            prefs.put("download.default_directory", dlPath);
            prefs.put("download.directory_upgrade", true);
            prefs.put("download.prompt_for_download", false);

            options.setExperimentalOption("prefs", prefs);
            options.addArguments("--test-type");
            ParseConfigurations configurations = new ParseConfigurations();
            if (configurations.getAllConfigurations()
                    .get(ParseConfigurations.Configs.RUN_HEADLESS.name())
                    .equalsIgnoreCase("true"))
            {
                options.addArguments("--headless");
                options.addArguments("--disable-gpu");
            }
            options.addArguments("--start-maximized");

//            options.addArguments("--kiosk");
            options.addArguments("--disable-save-password-bubble");*/


//            capabilities.setCapability(ChromeOptions.CAPABILITY, options);

            DesiredCapabilities capabilities = new DesiredCapabilities().chrome();
            capabilities.setCapability("chrome.switches", Arrays.asList("--no-default-browser-check"));

            return capabilities;
        }

        public WebDriver getWebDriverObject(DesiredCapabilities capabilities) {
            String dlPath = ParseConfigurations.Configs.RESOURCES_DIR.config() + File.separator
                    + "downloads";

            //downloads folder to automatically save the downloaded files
            File folder = new File(dlPath);
            if (!folder.exists()) {
                folder.mkdir();
            }

            HashMap<String, Object> chromePreferences = new HashMap<>();
            chromePreferences.put("download.default_directory", dlPath);

            ChromeOptions options = new ChromeOptions();
            options.setExperimentalOption("prefs", chromePreferences);

            return new ChromeDriver(options);
        }
    },
    DOCKER_FIREFOX {
        public DesiredCapabilities getDesiredCapabilities() {

            //Creating a profile
            FirefoxProfile profile = new FirefoxProfile();
            profile.setPreference("browser.download.folderList", 2);
            profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
                    "image/jpeg, application/pdf, application/octet-stream, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            profile.setPreference("pdfjs.disabled", true);

            //Create Desired Capability Instance
            DesiredCapabilities capabilities = DesiredCapabilities.firefox();

            capabilities.setCapability(FirefoxDriver.PROFILE, profile);
            return capabilities;
        }

        public WebDriver getWebDriverObject(DesiredCapabilities capabilities) {
            final String URL = getSeleniumHubUrl();

            try {
                RemoteWebDriver driver = new RemoteWebDriver(new URL(URL), capabilities);
                driver.setFileDetector(new LocalFileDetector());
                return driver;
            } catch (MalformedURLException e) {
                LOGNREPORT.sphnxError(this.getClass().getCanonicalName(), "Error in WebDriver, details: " + ExceptionUtils.getStackTrace(e.getCause()));
                return null;
            }
        }
    },
    DOCKER_CHROME {
        public DesiredCapabilities getDesiredCapabilities() {
            //downloads folder to automatically save the downloaded files
            File folder = new File("downloads");
            folder.mkdir();

            DesiredCapabilities capabilities = new DesiredCapabilities().chrome();

            ChromeOptions options = new ChromeOptions();
            Map<String, Object> prefs = new HashMap<String, Object>();
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);
            File userHome = new File(System.getProperty("user.home"));

//            File userHome = new File(System.getProperty("user.home"));
//
//            String dlPath =  userHome + File.separator
//                    + "Downloads";
            String dlPath = ParseConfigurations.Configs.RESOURCES_DIR.config() + File.separator
                    + "downloads";

            prefs.put("download.default_directory", dlPath);
            prefs.put("download.directory_upgrade", true);
            prefs.put("download.prompt_for_download", false);
            prefs.put("profile.default_content_setting_values.automatic_downloads", 1);

            options.setExperimentalOption("prefs", prefs);
            options.addArguments("--test-type");
            options.addArguments("--start-maximized");
            options.addArguments("--disable-save-password-bubble");

            capabilities.setCapability("chrome.switches", Arrays.asList("--no-default-browser-check"));
            HashMap<String, Object> chromePreferences = new HashMap<>();
            chromePreferences.put("profile.password_manager_enabled", "false");
            chromePreferences.put("credentials_enable_service", "false");
            chromePreferences.put("profile.default_content_settings.popups", 0);
//            chromePreferences.put("download.default_directory", folder.getAbsolutePath());
            capabilities.setCapability("chrome.prefs", chromePreferences);

            capabilities.setCapability(ChromeOptions.CAPABILITY, options);
            capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            capabilities.setCapability(ChromeOptions.CAPABILITY, options);

            return capabilities;
        }

        public WebDriver getWebDriverObject(DesiredCapabilities capabilities) {
            final String URL = getSeleniumHubUrl();

            try {
                RemoteWebDriver driver = new RemoteWebDriver(new URL(URL), capabilities);
                driver.setFileDetector(new LocalFileDetector());
                return driver;
            } catch (MalformedURLException e) {
                LOGNREPORT.sphnxError(this.getClass().getCanonicalName(), "Error in WebDriver, details: " + ExceptionUtils.getStackTrace(e.getCause()));
                return null;
            }
        }
    },
    SAUCE_FIREFOX {
        public DesiredCapabilities getDesiredCapabilities() {

            //Creating a profile
            FirefoxProfile profile = new FirefoxProfile();
            profile.setPreference("browser.download.folderList", 2);
            profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
                    "image/jpeg, application/pdf, application/octet-stream, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            profile.setPreference("pdfjs.disabled", true);

            ParseConfigurations configurations = new ParseConfigurations();
            //Create Desired Capability Instance
            DesiredCapabilities capabilities = DesiredCapabilities.firefox();

            capabilities.setCapability("version", configurations
                    .getAllConfigurations()
                    .get(ParseConfigurations.Configs.BROWSER_VERSION.name()));
            capabilities.setCapability("platform", configurations
                    .getAllConfigurations()
                    .get(ParseConfigurations.Configs.PLATFORM.name()));


            //configure capability for setting up Test Case name for Sauce Jobs
            String testName = System.getProperty("test_name");
            capabilities.setCapability("name", testName);

            capabilities.setCapability(FirefoxDriver.PROFILE, profile);
            return capabilities;
        }

        public WebDriver getWebDriverObject(DesiredCapabilities capabilities) {
            final String URL = getSauceHubUrl();

            try {
                RemoteWebDriver driver = new RemoteWebDriver(new URL(URL), capabilities);
                driver.setFileDetector(new LocalFileDetector());
                return driver;
            } catch (MalformedURLException e) {
                LOGNREPORT.sphnxError(this.getClass().getCanonicalName(), "Error in WebDriver, details: " + ExceptionUtils.getStackTrace(e.getCause()));
                return null;
            }
        }
    },
    SAUCE_CHROME {
        public DesiredCapabilities getDesiredCapabilities() {
            //downloads folder to automatically save the downloaded files
            File folder = new File("downloads");
            folder.mkdir();

            //new DesiredCapabilities();
            DesiredCapabilities capabilities = DesiredCapabilities.chrome();

            ChromeOptions options = new ChromeOptions();
            Map<String, Object> prefs = new HashMap<String, Object>();
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);

            options.setExperimentalOption("prefs", prefs);
            options.addArguments("--test-type");
            options.addArguments("--start-maximized");
            options.addArguments("--disable-save-password-bubble");

            capabilities.setCapability("chrome.switches", Arrays.asList("--no-default-browser-check"));
            HashMap<String, Object> chromePreferences = new HashMap<>();
            chromePreferences.put("profile.password_manager_enabled", "false");
            chromePreferences.put("credentials_enable_service", "false");
            chromePreferences.put("profile.default_content_settings.popups", 0);
            chromePreferences.put("download.default_directory", folder.getAbsolutePath());
            capabilities.setCapability("chrome.prefs", chromePreferences);

            capabilities.setCapability(ChromeOptions.CAPABILITY, options);
            capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            capabilities.setCapability(ChromeOptions.CAPABILITY, options);

            ParseConfigurations parseConfigurations = new ParseConfigurations();
            capabilities.setCapability("version", parseConfigurations
                    .getAllConfigurations()
                    .get(ParseConfigurations.Configs.BROWSER_VERSION.name()));
            capabilities.setCapability("platform", parseConfigurations
                    .getAllConfigurations()
                    .get(ParseConfigurations.Configs.PLATFORM.name()));

            //configure capability to set the job name with Test Case name
            String testName = System.getProperty("test_name");
            capabilities.setCapability("name", testName);

            return capabilities;
        }

        public WebDriver getWebDriverObject(DesiredCapabilities capabilities) {
            final String URL = getSauceHubUrl();

            try {
                RemoteWebDriver driver = new RemoteWebDriver(new URL(URL), capabilities);
                driver.setFileDetector(new LocalFileDetector());
                return driver;
            } catch (MalformedURLException e) {
                LOGNREPORT.sphnxError(this.getClass().getCanonicalName(), "Error in WebDriver, details: " + ExceptionUtils.getStackTrace(e.getCause()));
                return null;
            }
        }
    },
    SAUCE_IE {
        public DesiredCapabilities getDesiredCapabilities() {
            DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();

            capabilities.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION,
                    true);
            capabilities.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING,
                    true);
            capabilities.setCapability("requireWindowFocus",
                    true);
            capabilities.setJavascriptEnabled(true);
            capabilities.setCapability("ignoreZoomSetting", true);
            capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);

            //configure capability with chrome version
            String ie_version = determineEffectivePropertyValue("ie_version");
            if (null == ie_version) {
                capabilities.setCapability("version", "11");
            } else {
                capabilities.setCapability("version", ie_version);
            }

            //configure capability with platform type
            String platform = determineEffectivePropertyValue("platform");
            if (null == platform) {
                capabilities.setCapability("platform", "Windows XP");
            } else {
                platform = platform.replace("_", " ");
                capabilities.setCapability("platform", platform);
            }

            //configure capability to set the job name with Test Case name
            String testName = System.getProperty("test_name");
            capabilities.setCapability("name", testName);

            return capabilities;
        }

        public WebDriver getWebDriverObject(DesiredCapabilities capabilities) {
            final String URL = getSauceHubUrl();

            try {
                RemoteWebDriver driver = new RemoteWebDriver(new URL(URL), capabilities);
                driver.setFileDetector(new LocalFileDetector());
                return driver;
            } catch (MalformedURLException e) {
                LOGNREPORT.sphnxError(this.getClass().getCanonicalName(), "Error in WebDriver, details: " + ExceptionUtils.getStackTrace(e.getCause()));
                return null;

            }
        }
    },
    IE {
        public DesiredCapabilities getDesiredCapabilities() {

            configureIE();

            DesiredCapabilities capabilities =
                    DesiredCapabilities.internetExplorer();
            capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
            capabilities.setCapability(CapabilityType.ForSeleniumServer.ENSURING_CLEAN_SESSION,
                    true);
            capabilities.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING,
                    true);
            capabilities.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS,
                    true);
            capabilities.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);
            capabilities.setCapability(InternetExplorerDriver.UNEXPECTED_ALERT_BEHAVIOR, "accept");
            capabilities.setCapability("ignoreProtectedModeSettings", true);
            capabilities.setCapability("disable-popup-blocking", true);
            capabilities.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);

            capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            capabilities.setJavascriptEnabled(true);
            return capabilities;
        }

        public WebDriver getWebDriverObject(DesiredCapabilities capabilities) {
            return new InternetExplorerDriver(capabilities);
        }
    },

    SAFARI {
        public DesiredCapabilities getDesiredCapabilities() {
            SafariOptions sOptions = new SafariOptions();
            DesiredCapabilities capabilities =
                    DesiredCapabilities.safari();
            capabilities.setCapability(SafariOptions.CAPABILITY, sOptions);

            return capabilities;
        }

        public WebDriver getWebDriverObject(DesiredCapabilities capabilities) {
            return new SafariDriver(capabilities);
        }
    },
    ANDROID {
        public DesiredCapabilities getDesiredCapabilities() {
            return null;
        }

        public WebDriver getWebDriverObject(DesiredCapabilities capabilities) {
            return AppiumDriverManager.getDriver();
        }
    };


    private static String getSauceHubUrl() {
        ParseConfigurations parseConfigurations = new ParseConfigurations();
        String USERNAME = parseConfigurations.getAllConfigurations().get(ParseConfigurations.Configs.SAUCE_USER.name());
        String ACCESS_KEY = parseConfigurations.getAllConfigurations().get(ParseConfigurations.Configs.SAUCE_ACCESS_KEY.name());

        return "https://" + USERNAME + ":" + ACCESS_KEY + "@ondemand.saucelabs.com:443/wd/hub";
    }

    private static String getSeleniumHubUrl() {

        String hubUrl = determineEffectivePropertyValue("remote");
        return "http://" + hubUrl + "/wd/hub";
    }

    /**
     * It configures the Gecko driver
     */
    private static void configureGecko() {

        String os = System.getProperty("os.name").toLowerCase();
        String geckoPath = null;
        if (os.indexOf("mac") >= 0) {
            geckoPath = root + "/vendors/gecko/mac/geckodriver";
        } else if (os.indexOf("win") >= 0) {
            geckoPath = root + "/vendors/gecko/win/geckodriver.exe";
        } else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0) {
            geckoPath = root + "/vendors/gecko/linux/geckodriver";
        } else {
            throw new IllegalArgumentException("Operating System : " + os + " is not supported");
        }
        System.setProperty("webdriver.gecko.driver", geckoPath);

    }

    /**
     * It configures the Chrome driver
     */
    private static void configureChrome() {
        String os = System.getProperty("os.name").toLowerCase();
        String chromePath = null;
        if (os.indexOf("mac") >= 0) {
            chromePath = root + "/vendors/chrome/mac/chromedriver";
        } else if (os.indexOf("win") >= 0) {
            chromePath = root + "/vendors/chrome/win/chromedriver.exe";
        } else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0) {
            chromePath = root + "/vendors/chrome/linux/chromedriver";
        } else {
            throw new IllegalArgumentException("Operating System : " + os + " is not supported");
        }
        System.setProperty("webdriver.chrome.driver", chromePath);
    }

    /**
     * It configures the Internet Explorer driver
     */
    private static void configureIE() {
        String os = System.getProperty("os.name").toLowerCase();
        String ieDriverPath = null;
        if (os.indexOf("mac") >= 0) {
            throw new IllegalArgumentException("Internet Explorer not available on Mac");
        } else if (os.indexOf("win") >= 0) {
            ieDriverPath = root + "/vendors/ie/IEDriverServer.exe";
        } else {
            throw new IllegalArgumentException("Operating System : " + os + " is not supported");
        }
        System.setProperty("webdriver.ie.driver", ieDriverPath);
    }

    /**
     * It returns the property value specified in either environment variable or configuration.properties
     * It gives priority to the property specified in Java environment variable For e.g. -Ddriver_id=FIREFOX
     *
     * @param key
     * @return
     */
    private static String determineEffectivePropertyValue(String key) {

        if (null != System.getProperty(key)) {
            return System.getProperty(key);
        } else {
            throw new DriverException("please provide the value for key: " + key + " in maven parameter");
        }

    }
}
