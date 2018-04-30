package utopia.sphnx.core.appium.manager;


import utopia.sphnx.core.appium.android.AndroidDeviceConfiguration;
import utopia.sphnx.core.appium.entities.MobilePlatform;
import utopia.sphnx.core.appium.ios.IOSDeviceConfiguration;

import java.io.IOException;

/**
 * Device Manager - Handles all device related information's e.g UDID, Model, etc
 */
public class DeviceManager {

    private static ThreadLocal<String> deviceUDID = new ThreadLocal<>();
    private IOSDeviceConfiguration iosDeviceConfiguration;
    private AndroidDeviceConfiguration androidDeviceConfiguration;

    public DeviceManager() {
        try {
            iosDeviceConfiguration = new IOSDeviceConfiguration();
            androidDeviceConfiguration = new AndroidDeviceConfiguration();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getDeviceUDID() {
        return deviceUDID.get();
    }

    protected static void setDeviceUDID(String UDID) {
        deviceUDID.set(UDID);
    }

    public static MobilePlatform getMobilePlatform() {
        if (utopia.sphnx.core.appium.manager.DeviceManager.getDeviceUDID().length()
                == IOSDeviceConfiguration.IOS_UDID_LENGTH
                || utopia.sphnx.core.appium.manager.DeviceManager.getDeviceUDID().length()
                == IOSDeviceConfiguration.SIM_UDID_LENGTH) {
            return MobilePlatform.IOS;
        } else {
            return MobilePlatform.ANDROID;
        }
    }

    public String getDeviceModel() throws InterruptedException, IOException {
        if (getMobilePlatform().equals(MobilePlatform.ANDROID)) {
            return androidDeviceConfiguration.getDeviceModel();
        } else if (getMobilePlatform().equals(MobilePlatform.IOS)) {
            return iosDeviceConfiguration.getIOSDeviceProductTypeAndVersion();
        }
        throw new IllegalArgumentException("DeviceModel is Empty");
    }

    public String getDeviceCategory() throws Exception {
        if (iosDeviceConfiguration.deviceUDIDiOS.contains(utopia.sphnx.core.appium.manager.DeviceManager.getDeviceUDID())) {
            return iosDeviceConfiguration.getDeviceName().replace(" ", "_");
        } else {
            return androidDeviceConfiguration.getDeviceModel();
        }
    }

    public String getDeviceVersion() {
        if (getMobilePlatform().equals(MobilePlatform.ANDROID)) {
            return androidDeviceConfiguration.deviceOS();
        } else if (getMobilePlatform().equals(MobilePlatform.IOS)) {
            try {
                return iosDeviceConfiguration.getIOSDeviceProductVersion();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        throw new IllegalArgumentException("DeviceVersion is Empty");
    }
}
