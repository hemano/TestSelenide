package utopia.sphnx.core.listeners;

import org.apache.commons.lang3.StringUtils;
import org.testng.*;
import utopia.sphnx.core.support.xmlmapping.testcases.TestStep;
import utopia.sphnx.reports.Report;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static conversion.setup.Constants.*;

public class TestListenerNew implements ITestListener, IInvokedMethodListener {


    @Override
    public void beforeInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {


    }

    @Override
    public void afterInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {
        if (!iInvokedMethod.isConfigurationMethod()) {
            ITestContext context = iTestResult.getTestContext();
            if (context.getAttributeNames().contains("BEGIN_LOOP")) {
                Report.node("BEGIN LOOP", "Iteration started");
                Report.nodeLog("DataFile         :" + context.getAttribute("dataFile"));
                Report.nodeLog("Range            :" + context.getAttribute("startRange"));
                Report.nodeLog("Iterations Count :" + context.getAttribute("iterations"));
            }
            if (context.getAttributeNames().contains("END_LOOP")) {
                Report.node("END LOOP", "Iteration ended");
            }
        }
        if (iTestResult.getMethod().isAfterClassConfiguration() && !iTestResult.getMethod().getMethodName().equals("springTestContextAfterTestClass")) {
            System.out.println(ANSI_RESET + "|" + ANSI_BLACK_BACKGROUND + ANSI_WHITE_BOLD_BRIGHT + " Cumulative Results " + ANSI_RESET + "|");
            printCumulativeResult();

        }
    }

    @Override
    public void onTestStart(ITestResult iTestResult) {

    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {
        if (iTestResult.getParameters()[0] instanceof TestStep) {
            System.out.printf("\033[30;42;1m");   //set test to black with green background and bright
            Reporter.log("<< " + ((TestStep) iTestResult.getParameters()[0]).getFunctionName() + " >> step has < PASSED > ", true);
            System.out.printf("\033[0m");  //clear all formats and back to defaults
        }
    }

    @Override
    public void onTestFailure(ITestResult iTestResult) {
        if (iTestResult.getParameters()[0] instanceof TestStep) {
            System.out.printf("\033[30;41m");   //set test to black with red background and bright
            Reporter.log("<< " + ((TestStep) iTestResult.getParameters()[0]).getFunctionName() + " >> step has < FAILED > ", true);
            System.out.printf("\033[0m");  //clear all formats and back to defaults
        }
    }

    @Override
    public void onTestSkipped(ITestResult iTestResult) {
        if (iTestResult.getParameters()[0] instanceof TestStep) {
            System.out.printf("\033[30;43;1m");   //set test to black with yellow background and bright
            Reporter.log("<< " + ((TestStep) iTestResult.getParameters()[0]).getFunctionName() + " >> step has < SKIPPED > ", true);
            System.out.printf("\033[0m");  //clear all formats and back to defaults
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {

    }

    @Override
    public void onStart(ITestContext iTestContext) {

    }

    @Override
    public void onFinish(ITestContext context) {

        Map<String, List<String>> testStatusMap = Report.testStatusMap;
        //printing the results
        String leftAlignFormat = "| %-20s | %-7d | %-7d | %-7d |%n";

        //find the longest test case name and build the results table to fit that length
        int maxLength = 0;

        Set set = testStatusMap.keySet();
        Iterator iter = set.iterator();

        while (iter.hasNext()) {
            int curLength = iter.next().toString().length();
            if (curLength > maxLength) {
                maxLength = curLength;
            }
        }

        //result table is by default handles test case names up to 20 chars.  if our maxValue is < 20, set it to 20
        //if maxLength > 20 then the format will auto correct for the additional length by padding in the correct places
        if (maxLength < 20) {
            maxLength = 20;
        }

        //Build the result header based on the longest test case name
        System.out.format(ANSI_RESET + "|" + ANSI_BLUE_BACKGROUND + ANSI_WHITE_BOLD_BRIGHT + StringUtils.leftPad("", (maxLength + 2) / 2, "-") + " TEST CASES WITH STEPS SUMMARY " + StringUtils.leftPad("", ((maxLength + 2) / 2) + ((maxLength % 2) - 1), "-") + ANSI_RESET + "|%n");
        System.out.format("+" + StringUtils.leftPad("", maxLength + 2, "-") + "+---------+---------+---------+%n");
        System.out.format("| Test Cases" + StringUtils.leftPad("", maxLength - 9, " ") + "| Passed  | Failed  | Skipped |%n");
        System.out.format("+" + StringUtils.leftPad("", maxLength + 2, "-") + "+---------+---------+---------+%n");
        System.out.print(ANSI_RESET);

        for (Map.Entry<String, List<String>> entry : testStatusMap.entrySet()) {

            List<String> resultList = entry.getValue();
            long failed = resultList.stream().filter(s -> s.equalsIgnoreCase("FAILED")).count();
            long passed = resultList.stream().filter(s -> s.equalsIgnoreCase("PASSED")).count();
            long skipped = resultList.stream().filter(s -> s.equalsIgnoreCase("SKIPPED")).count();

            //set text color for summary display
            String textColor;
            if (failed > 0) {
                textColor = ANSI_RED;
            } else if (skipped > 0) {
                textColor = ANSI_YELLOW;
            } else {
                textColor = ANSI_GREEN;
            }

            String formattedMessage = new String();

            leftAlignFormat = ANSI_RESET + "|" + textColor + " %-" + maxLength + "s " + ANSI_RESET + "|" + textColor + " %-7d "
                    + ANSI_RESET + "|" + textColor + " %-7d " + ANSI_RESET + "|" + textColor + " %-7d " + ANSI_RESET + "|%n";
            // right pad the test case name to maxLength value to align the result row with the header layout
            System.out.printf(leftAlignFormat, StringUtils.rightPad(entry.getKey().trim(), maxLength, " "), passed, failed, skipped);
            System.out.print(ANSI_RESET);
        }

        System.out.format("+" + StringUtils.leftPad("", maxLength + 2, "-") + "+---------+---------+---------+%n");
    }


    /**
     * <p> Print the result in table format on console based on testStatusMap</p>
     */
    private void printCumulativeResult() {
        int passedCnt = 0;
        int failedCnt = 0;
        int skippedCnt = 0;

        Map<String, List<String>> testStatusMap = Report.testStatusMap;

        //calculate passed or failed tests count
        for (Map.Entry<String, List<String>> entry : testStatusMap.entrySet()) {

            List<String> resultList = entry.getValue();
            if (resultList.contains("FAILED")) {
                failedCnt += 1;
            } else if (resultList.contains("PASSED")) {
                passedCnt += 1;
            } else {
                skippedCnt += 1;
            }
        }

        //printing the results
        System.out.print(ANSI_RESET);
        System.out.format("+----------+---------+%n");
        System.out.format("|  Status  |  Count  |%n");
        System.out.format("+----------+---------+%n");

        //set text color for summary display
        //if passed count > 0 display in Green, otherwise use default of Black
        String textColor;
        if (passedCnt > 0) {
            textColor = ANSI_GREEN;
        } else {
            textColor = ANSI_RESET;
        }
        String leftAlignFormat = ANSI_RESET + "|" + textColor + " %-8s " + ANSI_RESET + "|" + textColor + " %-7d " + ANSI_RESET + "|%n";
        System.out.format(leftAlignFormat, "Passed", passedCnt);
        System.out.print(ANSI_RESET);

        //if failed count > 0 display in Red, otherwise use default of Black
        if (failedCnt > 0) {
            textColor = ANSI_RED;
        } else {
            textColor = ANSI_RESET;
        }
        leftAlignFormat = ANSI_RESET + "|" + textColor + " %-8s " + ANSI_RESET + "|" + textColor + " %-7d " + ANSI_RESET + "|%n";
        System.out.format(leftAlignFormat, "Failed", failedCnt);
        System.out.print(ANSI_RESET);

        //if skipped count > 0 display in Yellow, otherwise use default of Black
        if (skippedCnt > 0) {
            textColor = ANSI_YELLOW;
        } else {
            textColor = ANSI_RESET;
        }
        leftAlignFormat = ANSI_RESET + "|" + textColor + " %-8s " + ANSI_RESET + "|" + textColor + " %-7d " + ANSI_RESET + "|%n";
        System.out.format(leftAlignFormat, "Skipped", skippedCnt);

        System.out.print(ANSI_RESET);
        System.out.format("+----------+---------+%n%n%n");
    }
}