package utopia.sphnx.core.appium.manager;

import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.AndroidServerFlag;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import io.appium.java_client.service.local.flags.ServerArgument;
import utopia.sphnx.core.appium.ios.IOSDeviceConfiguration;
import utopia.sphnx.core.appium.utils.AvailablePorts;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class AppiumServerManager {

    private AvailablePorts ap;
    private IOSDeviceConfiguration iosDeviceConfiguration;

    public static AppiumDriverLocalService getAppiumDriverLocalService() {
        return appiumDriverLocalService;
    }

    public static void setAppiumDriverLocalService(
            AppiumDriverLocalService appiumDriverLocalService) {
        utopia.sphnx.core.appium.manager.AppiumServerManager.appiumDriverLocalService = appiumDriverLocalService;
    }

    static AppiumDriverLocalService appiumDriverLocalService;

    public AppiumServerManager() throws IOException {
        iosDeviceConfiguration = new IOSDeviceConfiguration();
        ap = new AvailablePorts();
    }


    /**
     * start appium with auto generated ports : appium port, chrome port,
     * bootstrap port and device UDID
     */

    private void startAppiumServerForAndroid(String methodName)
            throws Exception {
        System.out.println(
                "**************************************************************************\n");
        System.out.println("Starting Appium Server to handle Android Device::"
                + DeviceManager.getDeviceUDID() + "\n");
        System.out.println(
                "**************************************************************************\n");
        AppiumDriverLocalService appiumDriverLocalService;
        int port = ap.getPort();
        int chromePort = ap.getPort();
        int bootstrapPort = ap.getPort();
        int selendroidPort = ap.getPort();
        AppiumServiceBuilder builder =
                new AppiumServiceBuilder().withAppiumJS(new File(ConfigFileManager
                        .configFileMap.get("APPIUM_JS_PATH")))
                        .withArgument(GeneralServerFlag.LOG_LEVEL, "info").withLogFile(new File(
                        System.getProperty("user.dir") + "/target/appiumlogs/"
                                + DeviceManager.getDeviceUDID()
                                + "__" + methodName + ".txt"))
                        .withArgument(AndroidServerFlag.CHROME_DRIVER_PORT,
                                Integer.toString(chromePort))
                        .withArgument(AndroidServerFlag.BOOTSTRAP_PORT_NUMBER,
                                Integer.toString(bootstrapPort))
                        .withIPAddress("127.0.0.1")
                        .withArgument(AndroidServerFlag.SUPPRESS_ADB_KILL_SERVER)
                        .withArgument(AndroidServerFlag.SELENDROID_PORT,
                                Integer.toString(selendroidPort))
                        .usingPort(port);
        /* and so on */
        ;
        appiumDriverLocalService = builder.build();
        appiumDriverLocalService.start();
        setAppiumDriverLocalService(appiumDriverLocalService);
    }

    /**
     * start appium with auto generated ports : appium port, chrome port,
     * bootstrap port and device UDID
     */
    ServerArgument webKitProxy = new ServerArgument() {
        @Override
        public String getArgument() {
            return "--webkit-debug-proxy-port";
        }
    };

    private void startAppiumServerSingleSession()
            throws Exception {
        System.out
                .println("***********************************************************\n");
        System.out.println("Starting Appium Server......");
        System.out.println("***********************************************************\n");


        File classPathRoot = new File(System.getProperty("user.dir"));
        int port = ap.getPort();
        AppiumDriverLocalService appiumDriverLocalService;
        AppiumServiceBuilder builder =
                new AppiumServiceBuilder().withAppiumJS(new File(ConfigFileManager
                        .configFileMap.get("APPIUM_JS_PATH")))
                        .withArgument(GeneralServerFlag.LOG_LEVEL, "info").withLogFile(new File(
                        System.getProperty("user.dir") + "/appiumlogs/appium_logs.text"))
                        .withArgument(GeneralServerFlag.LOG_LEVEL, "debug")
                        .withArgument(GeneralServerFlag.TEMP_DIRECTORY,
                                new File(System.getProperty("user.dir") + "/appiumlogs/"
                                        + "tmp_"
                                        + port).getAbsolutePath())
                        .usingPort(port);
        appiumDriverLocalService = builder.build();
        appiumDriverLocalService.start();
        System.out
                .println("***********************************************************\n");
        System.out.println("Started AppiumServer on Port......"
                + appiumDriverLocalService.getUrl());
        System.out
                .println("***********************************************************\n");
        setAppiumDriverLocalService(appiumDriverLocalService);
    }

    public URL getAppiumUrl() {
        return getAppiumDriverLocalService().getUrl();
    }

    private void destroyAppiumNode() {
        getAppiumDriverLocalService().stop();
        if (getAppiumDriverLocalService().isRunning()) {
            System.out.println("AppiumServer didn't shut... Trying to quit again....");
            getAppiumDriverLocalService().stop();
        }
    }

    public void startAppiumServer() throws Exception {
        startAppiumServerSingleSession();
    }

    public void stopAppiumServer() throws IOException, InterruptedException {
        destroyAppiumNode();
    }


}
