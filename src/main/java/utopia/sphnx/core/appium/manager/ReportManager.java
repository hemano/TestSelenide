package utopia.sphnx.core.appium.manager;

import com.aventstack.extentreports.ExtentTest;
import org.testng.ITestResult;
import utopia.sphnx.reports.Report;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

public class ReportManager {

    private TestLogger testLogger;
    private DeviceManager deviceManager;
    public ThreadLocal<ExtentTest> parentTest = new ThreadLocal<ExtentTest>();
    public ThreadLocal<ExtentTest> test = new ThreadLocal<ExtentTest>();
    public ExtentTest parent;
    public ExtentTest child;
    public String category = null;


    public ReportManager() {
        testLogger = new TestLogger();
        deviceManager = new DeviceManager();
    }

    public void startLogResults(String methodName,String className) throws FileNotFoundException {
        testLogger.startLogging(methodName, className);
    }

    public HashMap<String, String> endLogTestResults(ITestResult result)
            throws IOException, InterruptedException {
        this.test.set(Report.getTest());
        return testLogger.endLog(result, deviceManager.getDeviceModel(), test);
    }

}
