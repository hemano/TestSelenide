package utopia.sphnx.reports;

import org.testng.*;

public class ReporterListener implements ITestListener, ISuiteListener {

    @Override
    public void onTestStart(ITestResult iTestResult) {
        Reporter.addTest(iTestResult.getName(), "");
    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {

    }

    @Override
    public void onTestFailure(ITestResult iTestResult) {
        Reporter.fail("Test has failed");
    }

    @Override
    public void onTestSkipped(ITestResult iTestResult) {

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {

    }

    @Override
    public void onStart(ITestContext iTestContext) {

    }

    @Override
    public void onFinish(ITestContext iTestContext) {

    }

    @Override
    public void onStart(ISuite iSuite) {
        Reporter.instantiate(iSuite.getName());
    }

    @Override
    public void onFinish(ISuite iSuite) {
        Reporter.flush();
    }
}
