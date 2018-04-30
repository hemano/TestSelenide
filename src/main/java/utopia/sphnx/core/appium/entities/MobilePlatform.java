package utopia.sphnx.core.appium.entities;

public enum MobilePlatform {

    IOS("IOS"),
    ANDROID("ANDROID");

    public final String platformName;
    MobilePlatform(String platformName) {
        this.platformName = platformName;
    }

}
