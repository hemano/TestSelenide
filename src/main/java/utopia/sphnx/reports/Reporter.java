package utopia.sphnx.reports;

/**
 * Created by jitendrajogeshwar on 02/06/17.
 */

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static utopia.sphnx.logging.LoggerReporter.LOGNREPORT;

/**
 * @author hemantojha
 * <p>
 * Provides the reporting interface
 * </p>
 */
public class Reporter {

    private static String REPORT = Reporter.class.getCanonicalName();

    private static ExtentReports extent;
    private static ExtentTest test;
    private static ExtentTest childTest;
    private static ExtentHtmlReporter htmlReporter;
    public static ThreadLocal<WebDriver> driver = new ThreadLocal <WebDriver>();
    private static String testCaseName;
    private static String root = System.getProperty("user.dir");
    private static String filePath = "extentreport.html";
    private static Path reportPath;
    private static Path screenshotFolder;
    private static Path ecFolder;
    private static Path logFolder;
    private static StringBuilder htmlStringBuilder = new StringBuilder();
    public static Map<String, List<String>> testStatusMap = new LinkedHashMap<>();
    private static Map<Long, String> threadToExtentTestMap = new HashMap<Long, String>();
    private static Map<String, ExtentTest> nameToTestMap = new HashMap<String, ExtentTest>();
    private static ConcurrentHashMap<Long,ExtentTest> testStorage = new ConcurrentHashMap <>();;
    private static ConcurrentHashMap<Long,ExtentTest> nodeStorage= new ConcurrentHashMap <>();;


    /**
     * <p>
     * Instantiate the Extend report with report configurations
     * </p>
     */
    public static boolean instantiate(String TC_SCENARIO_FILE) {
        // create the report at path root/report/report_<nano time>
        try {
            // generate report folder name
            Path rootPath = getNewReportPath();

            // create directory if not exists
            if (Files.notExists(rootPath))
                reportPath = Files.createDirectories(rootPath);
            else
                reportPath = rootPath;


        } catch (IOException e) {
            LOGNREPORT.sphnxError(REPORT, "Exception details: " + ExceptionUtils.getStackTrace(e.fillInStackTrace()));
            return false;
        }

        // lazy initialization of the extent report object
        if (null == extent) {
            extent = new ExtentReports();

            htmlReporter = new ExtentHtmlReporter(Paths.get(reportPath.toString(), filePath).toAbsolutePath().toFile());


            // report configuration
            htmlReporter.config().setChartVisibilityOnOpen(true);
            htmlReporter.config().setDocumentTitle("Utopia SPHNX Automation");

            htmlReporter.config().setTheme(Theme.STANDARD);
            String js = ReadFile.readFileAsString("ReportJQuery.js");
            htmlReporter.config().setJS(js.trim().toString());

            extent.attachReporter(htmlReporter);
        }
        return true;
    }

    public static ExtentTest getTest(){
        return nodeStorage.get(Thread.currentThread().getId());
    }

    /**
     * <p>
     * Returns the path of the report folder created
     * </p>
     *
     * @return reportPath path of the report to be written to
     */
    public static Path getReportPath() {
        return reportPath;
    }

    /**
     * <p>
     * Generate the unique report name
     * </p>
     *
     * @return
     */
    private static Path getNewReportPath() {

        LocalDateTime dateTime = LocalDateTime.now();
        String reportName = "report" + "_" + dateTime.toLocalDate() + "_" + dateTime.toLocalTime().getHour() + "_"
                + dateTime.toLocalTime().getMinute() + "_" + dateTime.toLocalTime().getSecond();
//        ParseConfigurations parseConfigurations = new ParseConfigurations();
//        String artifactsPath = parseConfigurations.getAllConfigurations().get(ParseConfigurations.Configs.ARTIFACTS_DIR.name());

        String artifactsPath = Paths.get(System.getProperty("user.dir")).toString();
        return Paths.get(artifactsPath, "report", reportName);
    }

    /**
     * <p>
     * Add test to the report with the give test name
     * </p>
     *
     * @param testName    name of the test executing
     * @param description populate information about the test executing
     */
    public static void addTest(String testName, String description) {
        testCaseName = testName;
        testStorage.put(Thread.currentThread().getId(),extent.createTest(testName, description));

    }

    /**
     * <p>
     * addScreenshot to the immediate step To be used in case of failure
     * </p>
     */
    public static void addScreenshot() {
        try {
            String path = takeScreenshot();
            if (path == null){
                testStorage.get(Thread.currentThread().getId()).fail("failed to take screenshot");
            }
            else {
                String screenshotPath = path.substring(path.indexOf("report"));
                testStorage.get(Thread.currentThread().getId())
                        .fail("details", MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
            }
        } catch (IOException e) {
            LOGNREPORT.sphnxError(REPORT, "Exception details: " +
                    ExceptionUtils.getStackTrace(e.fillInStackTrace()));
        }
    }

    /**
     * <p>
     * Set simple log to the test
     * </p>
     *
     * @param log identify the log for test execution
     */
    public static void log(String log) {
        if (nodeStorage.get(Thread.currentThread().getId()) == null) {
            testStorage.get(Thread.currentThread().getId()).info(log);
        }
        else {
            nodeStorage.get(Thread.currentThread().getId()).info(log);
        }
    }

    /**
     * Set simple log to the test
     *
     * @param log
     */
    public static void testLog(String log) {
        testStorage.get(Thread.currentThread().getId()).info(log);
    }

    /**
     * <p>
     * Input the log as passing step of the test and attach the screenshot
     * </p>
     *
     * @param log identify the log for test execution
     */
    public static void pass(String log) {
        try {
            passWithHeadlessCheck(log);
            org.testng.Reporter.getCurrentTestResult().setStatus(ITestResult.SUCCESS);
        } catch (Exception e) {
            LOGNREPORT.sphnxError(REPORT, "Exception details: " + ExceptionUtils.getStackTrace(e.fillInStackTrace()));
            if (nodeStorage.get(Thread.currentThread().getId()) == null) {
                testStorage.get(Thread.currentThread().getId()).pass(log);
                }
                else {
                nodeStorage.get(Thread.currentThread().getId()).pass(log);
                }
            }


    }

    private static void passWithHeadlessCheck(String log) throws IOException {
        if (nodeStorage.get(Thread.currentThread().getId()) != null) {
            if(System.getProperties().containsKey("HEADLESS")){
                nodeStorage.get(Thread.currentThread().getId()).pass(log);
            }
            else {
                String path = takeScreenshot();
                if (path == null){
                    nodeStorage.get(Thread.currentThread().getId()).pass(log);
                }
                else {
                    nodeStorage.get(Thread.currentThread().getId()).pass(log, MediaEntityBuilder.createScreenCaptureFromPath(path).build());
                }
            }
        } else {
            if(System.getProperties().containsKey("HEADLESS")){
                testStorage.get(Thread.currentThread().getId()).pass(log);
            }
            else {
                String path = takeScreenshot();
                if (path == null){
                    testStorage.get(Thread.currentThread().getId()).pass(log);
                }
                else {
                    testStorage.get(Thread.currentThread().getId()).pass(log, MediaEntityBuilder.createScreenCaptureFromPath(path).build());
                }
            }
        }
    }

    public static void passWithScreenshot(String log) {

        try {
            passWithHeadlessCheck(log);
            org.testng.Reporter.getCurrentTestResult().setStatus(ITestResult.SUCCESS);
        } catch (IOException e) {
            LOGNREPORT.sphnxPASS(REPORT, "Exception details: " + ExceptionUtils.getStackTrace(e.fillInStackTrace()));
            if (nodeStorage.get(Thread.currentThread().getId()) == null) {
                testStorage.get(Thread.currentThread().getId()).pass(log);
            } else {
                nodeStorage.get(Thread.currentThread().getId()).pass(log);
            }
        }
    }

    /**
     * <p>
     * Input the log as failing step of the test and attach the screenshot
     * </p>
     *
     * @param log identify the log for test execution
     */
    public static void fail(String log) {
        try {
            if (nodeStorage.get(Thread.currentThread().getId()) == null) {
                if(System.getProperties().containsKey("HEADLESS")){
                    testStorage.get(Thread.currentThread().getId()).fail(log);
                }
                else {
                    String path = takeScreenshot();
                    if (path == null) {
                        testStorage.get(Thread.currentThread().getId()).fail(log);
                    }
                    else {
                        String screenshotPath = path.substring(path.indexOf("screenshots"));
                        testStorage.get(Thread.currentThread().getId()).fail(log, MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
                    }
                }
            } else {
                if(System.getProperties().containsKey("HEADLESS")){
                    nodeStorage.get(Thread.currentThread().getId()).fail(log);
                }
                else {
                    String path = takeScreenshot();
                    if (path == null) {
                        nodeStorage.get(Thread.currentThread().getId()).fail(log);
                    }
                    else {
                        String screenshotPath = path.substring(path.indexOf("screenshots"));
                        nodeStorage.get(Thread.currentThread().getId()).fail(log, MediaEntityBuilder.createScreenCaptureFromPath(screenshotPath).build());
                    }
                }
            }

        } catch (IOException e) {
            org.testng.Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
            testStorage.get(Thread.currentThread().getId()).fail(log);
        }
    }

    public static void failNoScreenshot(String log) {
        testStorage.get(Thread.currentThread().getId()).fail(log);
    }

    /**
     * <p>
     * skip log
     * </p>
     *
     * @param log identify the log for test execution
     */
    public static void skip(String log) {
        if (nodeStorage.get(Thread.currentThread().getId()) == null) {
            testStorage.get(Thread.currentThread().getId()).skip(log);
        }
        else {
            nodeStorage.get(Thread.currentThread().getId()).skip(log);
        }
    }

    /**
     * <p>
     * Attach a child node to the step, all the function are similar to the step
     * </p>
     *
     * @param businessProcess name of the business process for the node
     * @param log             identify the log for test execution
     */
    public static void node(String businessProcess, String log) {
        ExtentTest childTest = testStorage.get(Thread.currentThread().getId()).createNode(businessProcess).info(log);
        nodeStorage.put(Thread.currentThread().getId(),childTest);
    }

    /**
     * <p>
     * Attach a child node to the step, all the function are similar to the step
     * </p>
     *
     * @param businessProcess name of the business process for the node
     * @param log             identify the log for test execution
     */
    public static void nodeToLoop(String businessProcess, String log) {
        nodeStorage.put(
                Thread.currentThread().getId(),
                nodeStorage.get(Thread.currentThread().getId()).createNode(businessProcess).info(log));
        //childTest = childTest.createNode(businessProcess).info(log);
    }

    /**
     * <p>
     * Attach a child log in the node.
     * </p>
     *
     * @param log identify the log for test execution
     */
    public static void nodeLog(String log) {
        if (nodeStorage.get(Thread.currentThread().getId()) == null) {
            testStorage.get(Thread.currentThread().getId()).info(log);
        }
        else {
            nodeStorage.get(Thread.currentThread().getId()).info(log);
        }
    }

    /**
     * <p>
     * Flush the test data in the test of the report Need to get called after
     * every test finishes
     * </p>
     */
    public static void flush() {
        extent.flush();
    }

    /**
     * <p>
     * Take the screenshot
     * </p>
     *
     * @return
     */
    private static String takeScreenshot() {

        try {
            File src = ((TakesScreenshot) driver.get()).getScreenshotAs(OutputType.FILE);
            // now copy the screenshot to desired location using copyFile
            // //method
            screenshotFolder = Paths.get(reportPath.toString(), "screenshots");

            if (Files.notExists(screenshotFolder))
                Files.createDirectory(screenshotFolder);

            String fileName = testCaseName.replace(" ", "_") + "_" + System.nanoTime();
            Path screenshotPath = Paths.get(screenshotFolder.toString(), fileName + ".png");

            Files.copy(src.toPath(), screenshotPath);
            return screenshotPath.toString().substring(screenshotPath.toString().indexOf("screenshots"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnhandledAlertException u) {
            LOGNREPORT.sphnxInfo(REPORT, "Not able to take screenshot because of alert with text :" + u.getAlertText());
                LOGNREPORT.sphnxInfo(REPORT, "Taking os screen shot");
            return takeOSScreenShot();
        }

        return null;
    }

    /**
     * Take screenshot at OS level
     * @return
     */

    public static String takeOSScreenShot(){
        String orgHeadless = "default";
        if (System.getProperties().containsKey("java.awt.headless")) {
            orgHeadless = System.getProperty("java.awt.headless");
        }
        try {

            screenshotFolder = Paths.get(reportPath.toString(), "screenshots");
            if (Files.notExists(screenshotFolder))
                Files.createDirectory(screenshotFolder);

            String fileName = testCaseName.replace(" ", "_") + "_" + System.nanoTime();
            Path screenshotPath = Paths.get(screenshotFolder.toString(), fileName + ".png");
            System.setProperty("java.awt.headless", "false");
            Robot robot = new Robot();

            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit()
                    .getScreenSize());
            BufferedImage screenFullImage = robot.createScreenCapture(screenRect);
            ImageIO.write(screenFullImage, "jpg", new File(screenshotPath.toString()));

            return screenshotPath.toString().substring(screenshotPath.toString().indexOf("screenshots"));

        } catch (AWTException | IOException ex) {
            LOGNREPORT.sphnxInfo(REPORT, "Not able to take screenshot. Error :" + ex.getLocalizedMessage());
            return null;
        }
        finally {
            if(!orgHeadless.equalsIgnoreCase("default")) {
                System.setProperty("java.awt.headless",orgHeadless);
            }
        }
    }

    public static Path getScreenshotFolder() {
        return screenshotFolder;
    }

    /**
     * <p>
     * Make relative path.
     * </p>
     *
     * @param file the file instance
     * @return the file path
     */
    public static String makeRelativePath(File file) {

        String absolute = file.getAbsolutePath();
        String outputDir = targetDir().toString();
        int beginIndex = absolute.indexOf(outputDir) + outputDir.length();
        String relative = absolute.substring(beginIndex);
        return ".." + relative.replace('\\', '/');
    }

    private static File targetDir() {

        String relPath = Reporter.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath + "../../target");
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }
        return targetDir;
    }

    /**
     * <p>
     * Build html report with Execution Context data
     * </p>
     *
     * @param fileContent reference information for EC html
     * @throws IOException Signals that an I/O exception of some sort has occurred. This
     *                     class is the general class of exceptions produced by failed
     *                     or interrupted I/O operations.
     */
    public static void createHtml(String fileContent) throws IOException {

        htmlStringBuilder.append("<html>");
        htmlStringBuilder.append("<head>");
        htmlStringBuilder.append("<style>" + "body {font-weight: italic; color: blue}" + "</style>");
        htmlStringBuilder.append("</head>");
        htmlStringBuilder.append("<h3></h3>");
        htmlStringBuilder.append(fileContent);
        htmlStringBuilder.append("<body>");
        htmlStringBuilder.append("</body></html>");
    }

    /**
     * <p>
     * Write html report with Execution Context data into file
     * </p>
     *
     * @throws IOException Signals that an I/O exception of some sort has occurred. This
     *                     class is the general class of exceptions produced by failed
     *                     or interrupted I/O operations.
     */
    public static void writeToFile() throws IOException {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM.dd.yyyy_HH.mm.ss");
        LocalDateTime dateTime = LocalDateTime.now();
        String formattedDateTime = dateTime.format(formatter);

        ecFolder = Paths.get(reportPath.toString(), "ExecutionContext");
        try {
            ecFolder = Paths.get(reportPath.toString(), "ExecutionContext");
            if (Files.notExists(ecFolder))
                Files.createDirectory(ecFolder);
        } catch (IOException e) {
            LOGNREPORT.sphnxInfo(REPORT, "Unable to create folder to store EC variables");
        }

        String fileName = "EC_" + "Test_" + testCaseName.replace(" ", "_") + "_" + formattedDateTime + ".html";
        String tempFile = ecFolder + File.separator + fileName;
        File file = new File(tempFile);

        if (!htmlStringBuilder.toString().isEmpty()) {
            OutputStream outputStream = new FileOutputStream(file.getAbsoluteFile(), true);
            Writer writer = new OutputStreamWriter(outputStream);
            writer.write(htmlStringBuilder.toString());
            writer.close();
            htmlStringBuilder.setLength(0);
        } else {
            LOGNREPORT.sphnxInfo(REPORT, "During the " + testCaseName
                    + " test execution no EC variables were found to be saved into report.");
        }
    }

//    /**
//     * <p>
//     * Create log for every test in test suite
//     * </p>
//     */
////    public static void logForEveryTest(){
////        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM.dd.yyyy_HH.mm");
////        LocalDateTime dateTime = LocalDateTime.now();
////        String formattedDateTime = dateTime.format(formatter);
////
////        logFolder = Paths.get(reportPath.toString(), "Logs");
////        try {
////            logFolder = Paths.get(reportPath.toString(), "Logs");
////            if (Files.notExists(logFolder))
////                Files.createDirectory(logFolder);
////        } catch (IOException e) {
////            logger.info("Unable to create logs folder");
////        }
////
////        String fileName = testCaseName.replace(" ", "_") + "_" + formattedDateTime + ".log";
////        FileAppender fileApp = new FileAppender();
////        fileApp.setFile(logFolder +"/"+ fileName);
////        fileApp.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %C{1}:%L - %m%n"));
////        fileApp.setAppend(true);
////        fileApp.activateOptions();
////        Logger.getRootLogger().addAppender(fileApp);
////    }

//    public static void updateSauceJob(String jobStatus, String sessionId) {
//        try {
//
//            String payload = new String();
//            if (jobStatus.contains("pass")) {
//                payload = String.format("{\"passed\":true}");
//            } else {
//                payload = String.format("{\"passed\":false}");
//            }
//
//            String userName = PropertiesHelper.determineEffectivePropertyValue("sauce.username");
//            String password = PropertiesHelper.determineEffectivePropertyValue("sauce.password");
//
//            URL url = new URL(String.format("https://saucelabs.com/rest/v1/%s/jobs/%s/", userName, sessionId));
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setDoOutput(true);
//            connection.setRequestMethod("PUT");
//            connection.setRequestProperty("Content-Type", "application/json");
//            connection.setRequestProperty("Accept", "application/json");
//
//            connection.setRequestProperty("Authorization",
//                    "Basic " + getBasicAuthenticationEncoding(userName, password));
//
//            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
//            osw.write(payload);
//            osw.flush();
//            osw.close();
//
//            if (connection.getResponseCode() == 200) {
//                Report.log("SauceLabs job has been updated successfully");
//            } else {
//                Report.log("SauceLabs job has NOT been updated");
//            }
//
//            System.err.println(connection.getResponseCode());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

    public static String getScreencastLinkFromSauce(String sessionId) {
        return String.format("https://saucelabs.com/beta/tests/%s/watch", sessionId);
    }

    public static void addLinkToReport(String ref) {
        String link = String.format("<a target='_blank' href='%s'>Screencast Link</a>", ref);
        log(link);
    }

    private static String getBasicAuthenticationEncoding(String username, String password) {

        String userPassword = username + ":" + password;
        return new String(Base64.encodeBase64(userPassword.getBytes()));
    }

    public static void info(String log) {
        if (nodeStorage.get(Thread.currentThread().getId()) == null) {
            testStorage.get(Thread.currentThread().getId()).info(log);
        }
        else {
            nodeStorage.get(Thread.currentThread().getId()).info(log);
        }
    }

}