package utopia.sphnx.core.test;

import com.aventstack.extentreports.ExtentTest;
import com.google.common.collect.Lists;
import conversion.setup.Variables;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.ClassUtils;
import org.testng.*;
import org.testng.annotations.*;
import utopia.sphnx.config.ParseConfigurations;
import utopia.sphnx.core.CoreConfig;
import utopia.sphnx.core.listeners.TestListenerNew;
import utopia.sphnx.core.listeners.TestSortInterceptor;
import utopia.sphnx.core.support.Action;
import utopia.sphnx.core.support.GetAction;
import utopia.sphnx.core.support.xmlmapping.testcases.*;
import utopia.sphnx.dataconversion.datagen.execution.ExecutionContext;
import utopia.sphnx.reports.Report;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static utopia.sphnx.logging.LoggerReporter.LOGNREPORT;

/**
 * Created by jitendrajogeshwar on 17/05/17.
 */
@SpringBootTest
@Test
@ContextConfiguration(classes = {CoreConfig.class})
@Listeners({TestSortInterceptor.class, TestListenerNew.class})
public class CoreBaseTest extends AbstractTestNGSpringContextTests implements ITest {

    private final TestCase testCaseType;

    private static boolean isReportInitialized;

    private final String CORE_BASE_TEST = utopia.sphnx.core.test.CoreBaseTest.class.getCanonicalName();

    private ThreadLocal<Boolean> isSkip =  new ThreadLocal<Boolean>();

    private static ThreadLocal<Boolean> isNewTestCase = new ThreadLocal<Boolean>();

    protected ClassLoader classLoader;

    protected ThreadLocal<WebDriver> webDriver = new ThreadLocal <WebDriver>();

    protected ExtentTest logger;

    protected String stepName;

    protected boolean didPass = true;

    protected List<String> testStatusList = new ArrayList<>();

    protected String objectName;

    private static int testCasesCount = 0;

    private static ThreadLocal<Boolean> isEndTestCase = new ThreadLocal<Boolean>();
    private static ThreadLocal<Boolean> isEndScenarioCase = new ThreadLocal<Boolean>();
    private static ThreadLocal<Boolean> isIgnoreFailure = new ThreadLocal<Boolean>();

    public CoreBaseTest() {
        this.testCaseType = null;
    }

//    @Autowired(required = true)
//    @Qualifier("test-cases")
    private List testCases;

    protected String mode;

    private List<TestScenario> testScenarios;

    protected TestNG testNG;

        //@Autowired
    //@Qualifier("setExecutionContext")
    private HashMap executionContext;

    private Map configurations;

    public CoreBaseTest(TestCase testCaseType) throws Exception {
        this.testCaseType = testCaseType;
        springTestContextPrepareTestInstance();
        ParseConfigurations parseConfigurations = new ParseConfigurations();
        configurations = parseConfigurations.getAllConfigurations();
    }

    @Override
    public String getTestName() {
        if (testCaseType == null) {
            return "";
        }
        return testCaseType.getTestCase();
    }

    @BeforeSuite
    public void setParallel(ITestContext testContext){
        try {
            classLoader = Thread.currentThread().getContextClassLoader();
            if(!isReportInitialized) {
                isReportInitialized = Report.instantiate(Variables.TC_SCENARIO_FILE);
            }
        }

        catch (Exception e){
            LOGNREPORT.sphnxError(CORE_BASE_TEST,"Error in @BeforeTest. Exception : " + e.getLocalizedMessage());
            throw e;
        }


    }


    @BeforeClass(alwaysRun = true)
    public void before(ITestContext testContext) throws Exception {

        try {
            if(this.isNewTestCase.get() == null){
                this.isNewTestCase.set(false);
            }
            if(this.isEndTestCase.get() == null){
                this.isEndTestCase.set(false);
            }
            if(this.isEndScenarioCase.get() == null){
                this.isEndScenarioCase.set(false);
            }
            if(this.isIgnoreFailure.get() == null){
                this.isIgnoreFailure.set(false);
            }

//            if( testCaseType.getLastTest() != null) {
//                String mode = testCaseType.getMode().toLowerCase();
//                int threadCount = 2;
//                if (mode.contains("parallel")) {
//                    if (mode.contains(":")) {
//                        threadCount = Integer.parseInt(mode.split(":")[1]);
//                    }
//                    testContext.getCurrentXmlTest().getSuite().setParallel(XmlSuite.ParallelMode.METHODS);
//                    testContext.getCurrentXmlTest().setThreadCount(threadCount);
//                    testContext.getCurrentXmlTest().getSuite().setDataProviderThreadCount(3);
//                }
//                else {
//                    testContext.getCurrentXmlTest().getSuite().setDataProviderThreadCount(1);
//                }
//            }
            if (!this.isEndScenarioCase.get()) {
                Report.addTest(getTestName(), "Description for the tests: " + getTestName());
                String browserName = (String) configurations.get(ParseConfigurations.Configs.DRIVER_ID.name());
                //this.webDriver = (WebDriver) applicationContext.getBean("createWebDriverInstance", browserName);
                this.webDriver.set((WebDriver) applicationContext.getBean("createWebDriverInstance", browserName));
                //this.webDriver = (WebDriver) applicationContext.getBean("webDriver");
                Report.driver.set(webDriver.get());
                this.isNewTestCase.set(true);
                this.isEndTestCase.set(false);
                testStatusList.clear();
                LOGNREPORT.sphnxInfo(CORE_BASE_TEST,System.getProperty("line.separator") + "-------------->  Starting Test Case : << " + getTestName() + " >>", "blue", 1);
            } else if (this.isEndScenarioCase.get()) {
                LOGNREPORT.sphnxInfo(CORE_BASE_TEST,System.getProperty("line.separator") + "-------------->  Skipping Test Case : << " + getTestName() + " >>", "yellow", 1);
            }
        }
        catch (BeanCreationException e){
            LOGNREPORT.sphnxError(CORE_BASE_TEST,"Error in creating web driver instance. Exception : " + e.getLocalizedMessage());
            throw e;
        }
        catch (Exception e){
            LOGNREPORT.sphnxError(CORE_BASE_TEST,"Error in @BeforeClass. Exception : " + e.getLocalizedMessage());
            throw e;
        }
    }

    @DataProvider(name = "testCasesProvider",parallel = true)
    public Object[][] testCasesProvider(ITestContext testContext) throws Exception {
        this.testCases = Lists.newArrayList();
        try {
            this.springTestContextPrepareTestInstance();
            //this.testCases = (List) applicationContext.getBean("test-cases");
            this.testScenarios = (List) applicationContext.getBean("test-scenarios");
            testContext.getSuite().getXmlSuite().getTests().get(testCasesCount);
            for (TestScenario s : testScenarios) {
                if(s.getTestScenario().equalsIgnoreCase(testContext.getName())) {
                    this.testCases.addAll(s.getTestCase());
                }
            }
            List <Object[]> enabledTestCases = new LinkedList<Object[]>();

            this.testCases.forEach((testCaseType) -> {
                enabledTestCases.add(new Object[]{testCaseType});
            });
            testContext.setAttribute("tests", this.testCases);
            return (Object[][]) enabledTestCases.toArray(new Object[enabledTestCases.size()][]);
        }
        catch (Exception e){
            LOGNREPORT.sphnxError(CORE_BASE_TEST,"Error in TestCaseProvider. Exception : " + e.getCause());
            throw e;
        }

    }

    @BeforeTest(alwaysRun = true)
    public void beforeTest(ITestContext testContext) throws Exception {
        try {
            this.isNewTestCase.set(false);
            this.isEndTestCase.set(false);
            this.isEndScenarioCase.set(false);
            this.isIgnoreFailure.set(false);
            this.isSkip.set(false);

            ExecutionContext.ecDictionary = (HashMap<String, Object>) this.applicationContext.getBean("setExecutionContext");
        }

        catch (Exception e){
            LOGNREPORT.sphnxError(CORE_BASE_TEST,"Error in @BeforeTest. Exception : " + e.getLocalizedMessage());
            throw e;
        }

    }

    @DataProvider(name = "testStepProvider")
    public Object[][] componentProvider(@TestInstance Object object, ITestContext context, Method method) throws Exception {
        try {
            List <Object[]> components = new LinkedList <>();
            for (Object o : testCaseType.getTestStepOrLOOPOrENDLOOP()) {
                if (o instanceof TestStep) {
                    TestStep tst = (TestStep) o;
                    components.add(new Object[]{tst});
                }
                if (o instanceof LOOP) {
                    LOOP loop = (LOOP) o;
                    components.add(new Object[]{loop});
                }
                if (o instanceof ENDLOOP) {
                    ENDLOOP endloop = (ENDLOOP) o;
                    components.add(new Object[]{endloop});
                }
            }

            return components.toArray(new Object[components.size()][]);
        }
        catch (Exception e){
            e.printStackTrace();
            LOGNREPORT.sphnxError(CORE_BASE_TEST,"Error in TestStepProver. Exception : " + e.getLocalizedMessage());
            throw e;
        }
    }

    @Test(dataProvider = "testStepProvider", enabled = true, alwaysRun = true)
    public void testStepRunner(Object stepType, ITestContext iTestContext) throws Exception {
        try {
            int parameterCount = 0;

            if (stepType instanceof TestStep) {
                executeTestStep((TestStep) stepType, iTestContext, parameterCount);
            } else if (stepType instanceof LOOP) {
                manageBeginLoop((LOOP) stepType, iTestContext);
            } else if (stepType instanceof ENDLOOP) {
                manageEndLoop(iTestContext);
            }
        }
        catch (Exception e){
            LOGNREPORT.sphnxError(CORE_BASE_TEST,"Error in test execution @Test method. Exception : " + e.getLocalizedMessage());
            e.printStackTrace();
            throw e;
        }

    }

    private void executeTestStep(TestStep stepType, ITestContext iTestContext, int parameterCount) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, Exception{

        removeContextValuesForBeginLoop(iTestContext);
        removeContextAttributeForEndLoop(iTestContext);

        TestStep step = stepType;

        skipExecutionIfEndScenario(step);

        //Store the Action Name/ Step Name
        this.stepName = step.getFunctionName();

        //Get class name for the actions; this helps in identifying the actions which are executed
        String className = (String) applicationContext.getBean("actionsClassName");

        Class <?> instanceClass = ClassUtils.forName(className, classLoader);
        Constructor<?> constructor = instanceClass.getDeclaredConstructor(ApplicationContext.class);

        //Decide if the action executed failed or passed
        boolean didPass = true;

        //Get class type of parameters passed
        Class[] type = new Class[step.getParameters().getParameter().size()];

        ManageParameter manageParameter = new ManageParameter(parameterCount,step,type);
        manageParameter.invoke();
        String parms = manageParameter.getParms();
        List<String> parameters = manageParameter.getParameters();

        addReportNode(step, parms);

        //decide whether to skip the next step/action
        if (isNewTestCase.get()) {
            isSkip.set(false);
        } else if (isEndTestCase.get()) {
            isSkip.set(true);
            didPass = false;
        }

        LOGNREPORT.sphnxInfo(CORE_BASE_TEST, "Test Step: '" + step.getFunctionName() + "' Parameters: '" + parms + "'");
        didPass = executeStep(step, instanceClass, (Constructor <ApplicationContext>) constructor, didPass, type, parameters);


        //flag to identify whether the new step will be new of following
        isNewTestCase.set(false);

        //Update the step status for executed step
        updateStepStatus(step, didPass);

        iTestContext.setAttribute("TestStatus", testStatusList);
    }

    private void skipExecutionIfEndScenario(TestStep step) {
        if (this.isEndScenarioCase.get()) {
            LOGNREPORT.sphnxSkip(CORE_BASE_TEST, step.getFunctionName());
            testStatusList.add("SKIPPED");
            throw new SkipException("Step Skipped ");
        }
    }

    /**
     * execute the step it could be either simple or complex action
     * @param step
     * @param instanceClass
     * @param constructor
     * @param didPass
     * @param type
     * @param parameters
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws ClassNotFoundException
     */
    private boolean executeStep(TestStep step, Class <?> instanceClass, Constructor <ApplicationContext> constructor, boolean didPass, Class[] type, List <String> parameters) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, Exception {
        //Identify if passed action is simple or complex.
        //If searchAction returns true means it is a simple action
        if (searchAction(step, instanceClass)) {
            didPass = executeSimpleAction(instanceClass, constructor, didPass, type, parameters);

        } else {
            //Executing Complex Client Actions
            didPass = executeComplexAction(step, didPass);
        }
        return didPass;
    }

    /**
     * Update the status for executed step
     * @param step
     * @param didPass
     */
    private void updateStepStatus(TestStep step, boolean didPass) {
        if (didPass) {
            //LOGNREPORT.sphnxPASS(CORE_BASE_TEST, step.getFunctionName(),true);
            testStatusList.add("PASSED");
            Reporter.getCurrentTestResult().setStatus(ITestResult.SUCCESS);
            Assert.assertTrue(true, "Test Step has passed");
        } else if (isSkip.get()) {
            //LOGNREPORT.sphnxSkip(CORE_BASE_TEST, step.getFunctionName(),true);
            Reporter.getCurrentTestResult().setStatus(ITestResult.SKIP);
            testStatusList.add("SKIPPED");
            throw new SkipException("Step Skipped ");
        } else {
            //LOGNREPORT.sphnxFAIL(CORE_BASE_TEST, step.getFunctionName(),true);
            setEndTestCase(step.getOnFail());
            // if ignore then pass
            if (isIgnoreFailure.get()) {
                testStatusList.add("PASSED");
                isIgnoreFailure.set(false);
                Reporter.getCurrentTestResult().setStatus(ITestResult.SUCCESS);
                Assert.assertTrue(true, ("A failure in " + step.getFunctionName() + " is being ignored."));
            } else {
                testStatusList.add("FAILED");
                Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
                Assert.fail( "Test Step has failed");
            }
        }
    }

    /**
     * Execute complex action this will actions defined in the client project
     * @param step
     * @param didPass
     * @return
     * @throws ClassNotFoundException
     */
    private boolean executeComplexAction(TestStep step, boolean didPass) throws ClassNotFoundException, Exception {
        if (!isSkip.get()) {
            ParseConfigurations parseConfigurations = new ParseConfigurations();
            parseConfigurations.getAllConfigurations().put("iscomplex","true");
            GetAction getAction = new GetAction(stepName, step.getParameters().getParameter(), applicationContext);
            Action action = getAction.get();
            didPass = action.execute();
            parseConfigurations.getAllConfigurations().remove("iscomplex");
        }
        return didPass;
    }

    /**
     * Execute simple action from Actions class which is used to interact with application
     * @param instanceClass
     * @param constructor
     * @param didPass
     * @param type
     * @param parameters
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private boolean executeSimpleAction(Class <?> instanceClass, Constructor <ApplicationContext> constructor, boolean didPass, Class[] type, List <String> parameters) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        //Get object of simple action class
        Object obj = BeanUtils.instantiateClass(constructor, applicationContext);

        if (!isSkip.get()) {
            //Instantiate the action class so that method can be invoked
            Method action = instanceClass.getDeclaredMethod(this.stepName, type);
            didPass = (boolean) action.invoke((Action) obj, parameters.toArray());
        }
        return didPass;
    }

    /**
     * Search for action in the Action class
     * @param stepType
     * @param instanceClass
     * @return
     */
    private boolean searchAction(TestStep stepType, Class<?> instanceClass) {
        try {
            for (Method m : instanceClass.getMethods()) {
                if (m.getName().equalsIgnoreCase(stepName)) {
                    stepName = m.getName();
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            LOGNREPORT.sphnxFAIL(CORE_BASE_TEST,"Exception raised in search action, details: " +
                    "Exception:" + "\n" + e.getLocalizedMessage(),true);
            return false;
        }
    }

    /**
     * Add node for the report base on step name
     * @param step
     * @param parms
     */
    private void addReportNode(TestStep step, String parms) {
        if (step.getIteration() == null) {
            Report.node("Test Step: '"
                            + step.getFunctionName()
                            + "' Parameters: '"
                            + parms + "'",
                    " Execution started");
        } else {
            Report.node("Test Step: '"
                            + step.getFunctionName()
                            + " - Iteration: " + step.getIteration()
                            + "' Parameters: '"
                            + parms + "'",
                    " Execution started");

        }
    }

    /**
     * Remove attribute END Loop
     * @param iTestContext
     */
    private void removeContextAttributeForEndLoop(ITestContext iTestContext) {
        if (iTestContext.getAttributeNames().contains("END_LOOP")) {
            iTestContext.removeAttribute("END_LOOP");
        }
    }

    /**
     * Remove attribute BEGIN Loop
     * @param iTestContext
     */
    private void removeContextValuesForBeginLoop(ITestContext iTestContext) {
        if (iTestContext.getAttributeNames().contains("BEGIN_LOOP")) {
            iTestContext.removeAttribute("BEGIN_LOOP");
            iTestContext.removeAttribute("dataFile");
            iTestContext.removeAttribute("startRange");
            iTestContext.removeAttribute("iterations");
        }
    }

    private void manageEndLoop(ITestContext iTestContext) {
        LOGNREPORT.sphnxInfo(ENDLOOP.class.getCanonicalName(), "End Loop");
        iTestContext.setAttribute("END_LOOP", true);
        removeContextValuesForBeginLoop(iTestContext);
    }

    private void manageBeginLoop(LOOP stepType, ITestContext iTestContext) {
        LOOP loop = stepType;
        LOGNREPORT.sphnxInfo(LOOP.class.getCanonicalName(),
                "Begin Loop:  Datafile: '" + loop.getDataFile() + "' StartRange: '" + loop.getStartRange() + "' Iterations: '" + loop.getIterations());
        iTestContext.setAttribute("BEGIN_LOOP", true);
        iTestContext.setAttribute("dataFile", loop.getDataFile());
        iTestContext.setAttribute("startRange", loop.getStartRange());
        iTestContext.setAttribute("iterations", loop.getIterations());
        removeContextAttributeForEndLoop(iTestContext);
    }

    @AfterMethod(alwaysRun = true)
    public void getResult(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            LOGNREPORT.sphnxFAIL(CORE_BASE_TEST,
                    this.stepName + " - " +
                    " Parameters -" + this.objectName, true);
        } else if (result.getStatus() == ITestResult.SKIP) {
            LOGNREPORT.sphnxSkip(CORE_BASE_TEST, "SKIPPED: " +
                    this.stepName + " - " +
                    " Parameters -" + this.objectName, true);
        } else {
            LOGNREPORT.sphnxPASS(CORE_BASE_TEST,
                    this.stepName + " - " +
                            " Parameters -" + this.objectName, true);
        }
        Report.flush();
    }

    private void setEndTestCase(String onFail) {
        if (onFail.trim().equalsIgnoreCase("continue")) {
            this.isEndTestCase.set(false);
        }
        if (onFail.trim().equalsIgnoreCase("endtest")) {
            this.isEndTestCase.set(true);
        }
        if (onFail.trim().equalsIgnoreCase("endtestscenario")) {
            this.isEndScenarioCase.set(true);
        }
        if(onFail.trim().equalsIgnoreCase("ignore")) {
            this.isIgnoreFailure.set(true);
            this.isEndTestCase.set(false);
        }
    }

    @AfterTest
    public void endReport() {
        Report.flush();
    }

    @AfterClass(enabled = true)
    public void tearDown() {
        testCasesCount = testCasesCount + 1;
        applicationContext.getBean("killInstance");
        this.webDriver.set(null);
        String status = "";
        if (testStatusList.contains("FAILED")) {
            status = "FAILED";
        } else if (testStatusList.contains("PASSED")){
            status = "PASSED";
        } else {
            status = "SKIPPED";
        }

        LOGNREPORT.sphnxInfo(CORE_BASE_TEST, System.getProperty("line.separator") + "--------------> Test Case Execution Finished : << "
                + getTestName() + " >>  Status : " + status);

        Report.testStatusMap.put(getTestName(), testStatusList);
    }

    /**
     * Class created to manage parameters in the test step
     */
    private class ManageParameter {
        private int parameterCount;
        private TestStep step;
        private Class[] type;
        private String params;
        private List <String> parameters;

        private ManageParameter(int parameterCount, TestStep step, Class... type) {
            this.parameterCount = parameterCount;
            this.step = step;
            this.type = type;
        }

        private String getParms() {
            return params;
        }

        public List <String> getParameters() {
            return parameters;
        }

        private ManageParameter invoke() {
            params = "";
            parameters = Lists.newArrayList();
            int i = 0;
            for (Parameter p : step.getParameters().getParameter()) {
                type[i] = params.getClass(); // doing this to load a class entity into this array
                String value = p.getValue();

                parameters.add(value);
                params = params + value + ";";
                utopia.sphnx.core.test.CoreBaseTest.this.objectName = parameters.get(0);
                i = i + 1;
                parameterCount++;
            }
            //remove the last ';' as it is added to the end of the string
            params = params.substring(0, (params.length() - 1));
            return this;
        }
    }


}