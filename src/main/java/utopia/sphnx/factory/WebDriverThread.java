package utopia.sphnx.factory;


import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import utopia.sphnx.exceptions.DriverException;

import java.net.MalformedURLException;

/**
 * @author hemantojha
 * <p>
 * <p>
 * This enables us to create webDriver instance for a thread
 * </p>
 */
public class WebDriverThread {
    private static String WEB_DRIVER_THREAD = utopia.sphnx.factory.WebDriverThread.class.getCanonicalName();

    private WebDriver driver;
    private DriverType driverType;
    private DriverType selectedDriverType;

    // setting the default DriverType as FIREFOX_LOCAL
    private final DriverType defaultDriverType = DriverType.CHROME;

    private String driver_id;

    // setting the other variables
    private final String operatingSystem = System.getProperty("os.name").toUpperCase();
    private final String systemArchitecture = System.getProperty("os.arch").toUpperCase();
    private final boolean useRemoteWebDriver = Boolean.getBoolean("remoteDriver");

    /**
     * <p>
     * Create a desired capabilities instance It instantiate the web driver by
     * determining the effective driver type.
     * </p>
     *
     * @return driver
     * @throws Exception The class Exception and its subclasses are a form of
     *                   Throwable that indicates conditions that a reasonable
     *                   application might want to catch.
     *                   <p>
     *                   The class Exception and any subclasses that are not also
     *                   subclasses of RuntimeException are checked exceptions.
     *                   Checked exceptions need to be declared in a method or
     *                   constructor's throws clause if they can be thrown by the
     *                   execution of the method or constructor and propagate outside
     *                   the method or constructor boundary.
     */
    public WebDriver getDriver() throws Exception {

        if (null == driver) {
            selectedDriverType = determineEffectiveDriverType();

            DesiredCapabilities desiredCapabilities = selectedDriverType.getDesiredCapabilities();

            instantiateWebDriver(desiredCapabilities);
        }
        return driver;
    }

    /**
     * <p>
     * Returns the most appropriate driver type It allows to read the driver_id
     * from the environment variables
     * </p>
     *
     * @return DriverType
     */
    private DriverType determineEffectiveDriverType() {
        driverType = defaultDriverType;
        try {

            // get the browser id from Command Line
            if (null != System.getProperty("driver_id")) {
                driver_id = System.getProperty("driver_id");
                driverType = DriverType.valueOf(driver_id);
            } else {
                throw new DriverException("please provide the driver_id parameter in mvn command");
            }

        } catch (DriverException ignore) {
            System.err.println("Unknown DriverType specified... defaulting to " + driverType);
        }

        return driverType;
    }

    /**
     * <p>
     * Instantiate the webDriver using desiredCapabilities object.
     * </p>
     *
     * @param desiredCapabilities
     * @throws MalformedURLException
     */
    private void instantiateWebDriver(DesiredCapabilities desiredCapabilities) throws MalformedURLException {
        System.out.println(String.format("Current Browser : {%s}\n", driverType));
        driver = selectedDriverType.getWebDriverObject(desiredCapabilities);
    }

    /**
     * Facilitate to quit the driver in the thread
     */
    public void quitDriver() {
        if (null != driver) {
            driver.quit();
            driver = null;
        }
    }

}
