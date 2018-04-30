package utopia.sphnx.factory;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author hemantojha
 *         <p>
 *         DriverFactory enables to create a driver instance save it in a driverThread
 *
 *         </p>
 */
public class DriverFactory {

    private static String DRIVER_FACTORY = utopia.sphnx.factory.DriverFactory.class.getCanonicalName();

    private static List<WebDriverThread> webDriverThreadPool =
            Collections.synchronizedList(new ArrayList<WebDriverThread>());

    private static ThreadLocal<WebDriverThread> driverThread;
    static {
        driverThread = new ThreadLocal<WebDriverThread>() {

            @Override
            public WebDriverThread initialValue() {
                WebDriverThread webDriverThread = new WebDriverThread();
                webDriverThreadPool.add(webDriverThread);
                return webDriverThread;
            }
        };
    }



    /**
     * It returns the instance of the webDriver in current thread
     *
     * @return E a desired capabilities instance It instantiate the web driver by determining the effective driver type.
     */
    public static <E> WebDriver getDriver() {
        try {
            return driverThread.get().getDriver();

        } catch (Exception e) {
            throw new WebDriverException("Could not start the Driver", e);
        }
    }


    /**
     * Closes all the driver objects in all the threads
     */
    public static void closeDriverObjects() {
        try{
        System.out.format("%nClosing the browsers.%n");
//        for (WebDriverThread webDriverThread : webDriverThreadPool) {
//            webDriverThread.quitDriver();
//            webDriverThread = null;
//        }
            driverThread.get().quitDriver();
        }
        catch (Exception e) {
            throw new WebDriverException("Could not stop the Driver", e);
        }


    }

}