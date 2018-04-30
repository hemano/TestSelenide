package conversion;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import conversion.scenarios.TestCase;
import conversion.scenarios.TestScenario;
import conversion.scenarios.TestStep;
import conversion.setup.Configurations;
import conversion.setup.Constants;
import conversion.setup.Variables;
import conversion.utils.ReadGoogleSheets;
import conversion.utils.SPHNX_Row;
import conversion.utils.SPHNX_WorkSheet;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import utopia.sphnx.config.ParseConfigurations;
import utopia.sphnx.dataconversion.parsing.Parser;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static conversion.setup.Configurations.DRIVER_TYPE;
import static utopia.sphnx.logging.LoggerReporter.LOGNREPORT;

/**
 * Class:	conversion.TestDefinitionAdapter
 * <p>
 * Description:	Handles the loading of test variables
 * from a Custom (i.e. non-ALM) driver into
 * an in-memory XML for the Utopia Solutions ATF
 * <p>
 * Change Log:
 * Date		By				Changes made
 * -------------------------------------------------------------------
 * 10/28/2013	Lee Barnes		Initial creation
 * 02/02/2017	Lee Barnes		Modified to work with the combined ALM/custom driver framework
 * 05/02/2017  Jeff Heytow     Converted to Java code
 */

@Configuration
@Component
public class TestDefinitionAdapter {

    public int index;
/* XML variables */

    private Element objXMLRoot;
    private Element objXMLTestRun;
    private Element objXMLTestScenario;
    private Element objXMLTestCase;
    private Element objXMLTestStep;
    private Element objXMLLoop;
    private Element objXMLIteration;
    private Element objXMLIterationStep;
    private Element objXMLProperties;
    private Element objXMLTestStepParams;
    private Element objXMLIterationStepParams;
    private Document oXML;

    @Autowired
    private ApplicationContext applicationContext;

/* Test Definition variables (used by Custom Driver) */

    private String mTestRunName;            // holds the test run name
    private String mCurrentTestCase;        // holds the current test case ID
    private String mCurrentTestStep;        // holds the current test step name
    private String mCurrentIteration;        // holds the current iteration (0 if not in a loop)

    private String mDriverType;
    private Path mRunFile;
    private Path mScenarioFolder;
    private String mDataFolder;
    private String runXMLFolder;

    private boolean noApplication;
    private boolean noArea;
    private boolean noFunction;
    private boolean noPCount;
    private boolean noFail;
    private boolean isFirstTestCase = false;
    private String mode;
    private static String TDA = conversion.TestDefinitionAdapter.class.getCanonicalName();
    private boolean isScenarioFromCommandLine;
    private Map<String, String> configurations;


    public TestDefinitionAdapter() {
        this.oXML = DocumentHelper.createDocument();
        this.objXMLRoot = this.oXML.addElement("tests");
    }

    public TestDefinitionAdapter(String runFile, String scenarioFolder, String runXMLFolder) {
        this.mDriverType = DRIVER_TYPE;
        this.mRunFile = Paths.get(runFile);
        this.mScenarioFolder = Paths.get(scenarioFolder);
        this.runXMLFolder = runXMLFolder;
        this.mTestRunName = "SPHNXTestSuite";
        //this.mDataFolder = ATF_LOCAL_DATA;

/* Create in-memory XML doc to hold test variables */

        this.oXML = DocumentHelper.createDocument();
        this.objXMLRoot = this.oXML.addElement("tests");
    }

    public String saveTestXML() throws Exception {
        readRunFile(this.mRunFile.toString());
        loadTestScenarios();
        //if needing to see the xml inthe console during debug, uncomment below line
//        prettyPrintXML();
        return saveTestDefinitionFile(this.runXMLFolder);
    }

    public int getRow() {
        return this.index + 1;
    }

    private void readRunFile(String fileLocation) {
        Configurations.RUN_FILE = new SPHNX_WorkSheet(fileLocation);

        if (Configurations.RUN_FILE.isLocal()) {

            readLocalRunFile(Configurations.RUN_FILE.getLocation());

        } else if (Configurations.RUN_FILE.isGoogleDoc()) {

            readGoogleDocRunFile(Configurations.RUN_FILE.getLocation());

        } // else (ShapeShift Graph?) {
        //        // ???
        //        // }
    }

    private void readLocalRunFile(String fileLocation) {
        try {
            FileInputStream excelFile = new FileInputStream(String.valueOf(fileLocation));
            Workbook workbook = new XSSFWorkbook(excelFile);
            Configurations.RUN_FILE.setPageName(workbook.getSheetName(0));
            Sheet scenarioSheet = workbook.getSheet(Configurations.RUN_FILE.getPageName());

            addTestRun(Configurations.RUN_FILE.getPageName());

            int sheetRows = scenarioSheet.getPhysicalNumberOfRows();
            Configurations.RUN_FILE.setSPHNX_Rows(sheetRows - 1);

            index = 0;
            for (Row row : scenarioSheet) {
                // eliminate the header row
                if (index == 0) {
                    index++;
                    continue;
                }
                SPHNX_Row sphnxRow = new SPHNX_Row(row.getRowNum(), row.getPhysicalNumberOfCells());
                for (int i = 0; i < sphnxRow.getLastCell(); i++) {
                    DataFormatter df = new DataFormatter();
//                        sphnxRow.add(i, row.getCell(i).toString().trim());
                    // use dataformatter to preserve the excel cell format (will save 2 as 2 and not 2.0 since the default is to
                    // save as a double if not using the dataformatter
                    sphnxRow.add(i, df.formatCellValue(row.getCell(i)).trim());
                }
                Configurations.RUN_FILE.addRow(index - 1, sphnxRow);
                index++;
            }
        } catch (FileNotFoundException e) {
            LOGNREPORT.sphnxError(TDA, "File " + Configurations.RUN_FILE.getLocation() + " not found!");
        } catch (IOException e) {
            LOGNREPORT.sphnxError(TDA, "Exception in parsing run file. Exception: " + e.getLocalizedMessage());
        }
    }

    private void readGoogleDocRunFile(String fileLocation) {
        try {
            Sheets service = ReadGoogleSheets.getSheetService();

            String spreadSheetID = getSpreadSheetID(fileLocation);
            Spreadsheet sheetMeta = service.spreadsheets().get(spreadSheetID).execute();
            List<com.google.api.services.sheets.v4.model.Sheet> sheets = sheetMeta.getSheets();
            Configurations.RUN_FILE.setPageName(sheets.get(0).getProperties().getTitle());

            addTestRun(Configurations.RUN_FILE.getPageName());

            String range = Configurations.RUN_FILE.getPageName() + "!A1:I";

            ValueRange response = service.spreadsheets().values()
                    .get(spreadSheetID, range)
                    .execute();

            List<List<Object>> values = response.getValues();

            Configurations.RUN_FILE.setSPHNX_Rows(values.size() - 1);

            index = 0;
            int physicalNumberOfCells = 0;
            for (List<Object> objects : values) {
                if (index == 0) {
                    physicalNumberOfCells = objects.size();
                    index++;
                    continue;
                }
                SPHNX_Row sphnxRow = new SPHNX_Row((index + 1), physicalNumberOfCells);
                for (int i = 0; i < physicalNumberOfCells; i++) {
                    try {
                        sphnxRow.add(i, objects.get(i).toString().trim());
                    } catch (IndexOutOfBoundsException e) {
                        sphnxRow.add(i, "");
                    }
                }
                Configurations.RUN_FILE.addRow(index - 1, sphnxRow);
                index++;
            }

        } catch (IOException e) {
//            LOGNREPORT.sphnxError(TDA, "File " + Configurations.RUN_FILE.getLocation() + " not found!");
        }

    }

    private void loadTestScenarios() throws Exception {
        for (SPHNX_Row row : Configurations.RUN_FILE.getSPHNX_Rows()) {
            if (row != null) {
                if (row.hasLocalPath(Constants.TD_SCENARIO_COL)) {
                    loadTestCasesFromExcelFile(row.get(Constants.TD_SCENARIO_COL));

                } else if (row.hasGoogleDocURL(Constants.TD_SCENARIO_COL)) {
                    loadTestCasesFromGoogleDoc(row.get(Constants.TD_SCENARIO_COL));
                } // else {
                // ???
                // }

                processTestCases(row);
            }
        }
    }

    private void loadTestCasesFromExcelFile(String location) {
        String scenarioFilePath, sheet;
        if (location.contains("::")) {
            scenarioFilePath = getScenarioFilePath(location.split("::")[0]);
            sheet = location.split("::")[1];
        } else {
            scenarioFilePath = getScenarioFilePath(location);
            sheet = null;
        }
        Configurations.TEST_SCENARIO_FILE = new SPHNX_WorkSheet(scenarioFilePath);
        Configurations.TEST_SCENARIO_FILE.setPageName(sheet);

        FileInputStream excelFile;
        try {
            excelFile = new FileInputStream(String.valueOf(scenarioFilePath));
            Workbook workbook = new XSSFWorkbook(excelFile);

            Sheet scenarioSheet = null;
            if (sheet != null && !sheet.equals("")) {
                scenarioSheet = workbook.getSheet(sheet);
            }

            int sheetRows = scenarioSheet.getPhysicalNumberOfRows();
            Configurations.TEST_SCENARIO_FILE.setSPHNX_Rows(sheetRows - 1);
            index = 0;
            for (Row row : scenarioSheet) {
                // eliminate the header rows
                if (index == 0 || index == 1) {
                    index++;
                    continue;
                }
                SPHNX_Row sphnxRow = new SPHNX_Row((index + 1), (Constants.S_ONFAIL_COL + 1) + Constants.MAX_PARAMS);
                for (int i = 0; i < sphnxRow.getLastCell(); i++) {
                    try {
                        DataFormatter df = new DataFormatter();
//                        sphnxRow.add(i, row.getCell(i).toString().trim());
                        // use dataformatter to preserve the excel cell format (will save 2 as 2 and not 2.0 since the default is to
                        // save as a double if not using the dataformatter
                        sphnxRow.add(i, df.formatCellValue(row.getCell(i)).trim());
                    } catch (IndexOutOfBoundsException e) {
                        sphnxRow.add(i, "");
                    } catch (NullPointerException e1) {
                        sphnxRow.add(i, "");
                    }
                }
                Configurations.TEST_SCENARIO_FILE.addRow(index - 1, sphnxRow);
                index++;
            }

        } catch (FileNotFoundException e) {
//            LOGNREPORT.sphnxError(TDA, "File " + location + " not found in scenarios folder!");
        } catch (IOException e) {
//            LOGNREPORT.sphnxError(TDA, "Error reading scenarios file. Exception: " + e.getLocalizedMessage());
        }

    }

    private void loadTestCasesFromGoogleDoc(String location) {
        String scenarioID, sheet;
        if (location.contains("::")) {
            scenarioID = getSpreadSheetID(location.split("::")[0]);
            sheet = location.split("::")[1];
        } else {
            scenarioID = getScenarioFilePath(location);
            sheet = null;
        }
        Configurations.TEST_SCENARIO_FILE = new SPHNX_WorkSheet(scenarioID);
        Configurations.TEST_SCENARIO_FILE.setPageName(sheet);

        try {
            Sheets service = ReadGoogleSheets.getSheetService();

            Spreadsheet sheetMeta = service.spreadsheets().get(scenarioID).execute();
            List<com.google.api.services.sheets.v4.model.Sheet> sheets = sheetMeta.getSheets();
            Configurations.TEST_SCENARIO_FILE.setPageName(sheets.get(0).getProperties().getTitle());


            String range = Configurations.TEST_SCENARIO_FILE.getPageName() + "!A1:O";

            ValueRange response = service.spreadsheets().values()
                    .get(scenarioID, range)
                    .execute();

            List<List<Object>> values = response.getValues();

            Configurations.TEST_SCENARIO_FILE.setSPHNX_Rows(values.size() - 1);

            index = 0;
            int physicalNumberOfCells = 0;
            for (List<Object> objects : values) {
                if (index == 0 || index == 1) {
                    physicalNumberOfCells = objects.size();
                    index++;
                    continue;
                }
                SPHNX_Row sphnxRow = new SPHNX_Row((index + 1), (Constants.S_ONFAIL_COL + 1) + Constants.MAX_PARAMS);
                for (int i = 0; i < physicalNumberOfCells; i++) {
                    try {
                        sphnxRow.add(i, objects.get(i).toString().trim());
                    } catch (IndexOutOfBoundsException e) {
                        sphnxRow.add(i, "");
                    } catch (NullPointerException e1) {
                        sphnxRow.add(i, "");
                    }
                }
                Configurations.TEST_SCENARIO_FILE.addRow(index - 1, sphnxRow);
                index++;
            }
        } catch (IOException e) {
//            LOGNREPORT.sphnxError(TDA, "File " + Configurations.LOOP_FILE.getLocation() + " not found!");
        }

    }

    private void processTestCases(SPHNX_Row row) throws Exception {
        if (row.get(Constants.TD_RUN_COL).toString().toUpperCase().equals("Y")) {
            addTestScenario(row.get(Constants.TD_SCENARIO_COL).toString().trim(), "Y");

            getRunFileProperties(row);

            TestScenario testScenario = new TestScenario(Configurations.TEST_SCENARIO_FILE);
            testScenario.createTestCases();

            for (TestCase testCase : testScenario.getTestCases()) {
                processTestCaseData(testCase);
            }
        } else {
            return;
        }
    }

    private void getRunFileProperties(SPHNX_Row row) {
        if (!row.get(Constants.TD_MODE_COL).equals("") &&
                row.get(Constants.TD_MODE_COL) != null) {
            if (this.objXMLProperties == null) {
                addTestScenarioProperties();
            }
            addScenarioProp("MODE",
                    row.get(Constants.TD_MODE_COL).toString().trim());
        }
        if (!row.get(Constants.TD_DEVICE_COL).equals("") &&
                row.get(Constants.TD_DEVICE_COL) != null) {
            if (this.objXMLProperties == null) {
                addTestScenarioProperties();
            }
            addScenarioProp("DEVICE",
                    row.get(Constants.TD_DEVICE_COL).toString().trim());
        }
        if (!row.get(Constants.TD_BROWSER_COL).equals("") &&
                row.get(Constants.TD_BROWSER_COL) != null) {
            if (this.objXMLProperties == null) {
                addTestScenarioProperties();
            }
            addScenarioProp("BROWSER",
                    row.get(Constants.TD_BROWSER_COL).toString().trim());
        }
        if (!row.get(Constants.TD_PARAMS).equals("") &&
                row.get(Constants.TD_PARAMS) != null) {
            if (this.objXMLProperties == null) {
                addTestScenarioProperties();
            }
            int paramNum = 1;
            for (int i = Constants.TD_PARAMS; i < row.getLastCell(); i++) {
                String val = row.get(i).toString().trim();
                if (!val.equals("")) {
                    System.out.println("Param" + paramNum + ": " + val);
                    addScenarioProp("Param" + paramNum, val);
                }
                paramNum++;
            }
        }
    }

    private void processTestCaseData(TestCase testCase) {
        if (this.objXMLProperties != null) {
            this.objXMLProperties = null;
        }
        if (testCase.isToRun()) {
            addTestCase(testCase.getName(), "Y");
            for (TestStep step : testCase.getTestSteps()) {
                processStepType(step);
            }
        }
    }

    private void processStepType(TestStep step) {
        if (step.isLoopStep()) {
            processLoop(step);
        } else {
            processTestStep(step);
        }
    }

    private void processLoop(TestStep step) {
        switch (step.getFunctionName().toUpperCase()) {
            case "BEGINLOOP":
                Configurations.LOOP_STEPS = new ArrayList<>();
                Variables.FILENAME = step.getLoopFile();
                getLoopParameters(Variables.FILENAME);
                if (step.hasFilter()) {
                    getFilterData(Configurations.LOOP_FILE, step);
                } else if (step.isRanged()) {
                    getRangedData(step);
                } else {
                    getIterationData(step);
                }
                break;
            case "ENDLOOP":
                processEndLoop();
                break;
            // if it is not a Begin Loop row or an End Loop row
            // it is a loop step row - add the step
            // to the list of steps to iterate
            default:
                Configurations.LOOP_STEPS.add(step);
                break;
        }
    }

    private void getLoopParameters(String fileName) {
        Configurations.LOOP_FILE = new SPHNX_WorkSheet(fileName.trim());
        if (Configurations.LOOP_FILE.isLocal()) {
            loadLoopDataFromExcelFile(
                    getLoopFilePath(
                            Configurations.LOOP_FILE.getLocation()
                    )
            );
        } else {
            loadLoopDataFromGoogleDoc(
                    getSpreadSheetID(
                            Configurations.LOOP_FILE.getLocation()
                    )
            );
        }
    }

    private void loadLoopDataFromExcelFile(String location) {

        FileInputStream excelFile;
        try {
            excelFile = new FileInputStream(String.valueOf(location));
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet scenarioSheet = workbook.getSheetAt(0);

            int sheetRows = scenarioSheet.getPhysicalNumberOfRows();
            Configurations.LOOP_FILE.setSPHNX_Rows(sheetRows);

            index = 0;
            for (Row row : scenarioSheet) {
                // first row is used to identify column header
                SPHNX_Row sphnxRow = new SPHNX_Row((index + 1), row.getLastCellNum());
                for (int i = 0; i < sphnxRow.getLastCell(); i++) {
                    try {
                        DataFormatter df = new DataFormatter();
//                        sphnxRow.add(i, row.getCell(i).toString().trim());
                        // use dataformatter to preserve the excel cell format (will save 2 as 2 and not 2.0 since the default is to
                        // save as a double if not using the dataformatter
                        sphnxRow.add(i, df.formatCellValue(row.getCell(i)).trim());
                    } catch (IndexOutOfBoundsException | NullPointerException e) {
                        sphnxRow.add(i, "");
                    }
                }
                Configurations.LOOP_FILE.addRow(index, sphnxRow);
                index++;
            }

        } catch (FileNotFoundException e) {
//            LOGNREPORT.sphnxError(TDA, "File " + location + " not found in scenarios folder!");
        } catch (IOException e) {
//            LOGNREPORT.sphnxError(TDA, "Error reading scenarios file. Exception: " + e.getLocalizedMessage());
        }

    }

    private void loadLoopDataFromGoogleDoc(String location) {
        try {
            Sheets service = ReadGoogleSheets.getSheetService();

            Spreadsheet sheetMeta = service.spreadsheets().get(location).execute();
            List<com.google.api.services.sheets.v4.model.Sheet> sheets = sheetMeta.getSheets();
            Configurations.LOOP_FILE.setPageName(sheets.get(0).getProperties().getTitle());

            int lastCol = 0;
            for (int i = 0; i < sheets.get(0).getProperties().size(); i++) {
                lastCol++;
            }

            String range = Configurations.LOOP_FILE.getPageName() + "!A1:" + Constants.englishAlphabet[lastCol];

            ValueRange response = service.spreadsheets().values()
                    .get(location, range)
                    .execute();

            List<List<Object>> values = response.getValues();

            Configurations.LOOP_FILE.setSPHNX_Rows(values.size());

            index = 0;
            for (List<Object> objects : values) {
                SPHNX_Row sphnxRow = new SPHNX_Row((index + 1), lastCol);
                for (int i = 0; i < lastCol; i++) {
                    try {
                        sphnxRow.add(i, objects.get(i).toString().trim());
                    } catch (IndexOutOfBoundsException e) {
                        sphnxRow.add(i, "");
                    } catch (NullPointerException e1) {
                        sphnxRow.add(i, "");
                    }
                }
                Configurations.LOOP_FILE.addRow(index, sphnxRow);
                index++;
            }
        } catch (IOException e) {
//            LOGNREPORT.sphnxError(TDA, "File " + Configurations.LOOP_FILE.getLocation() + " not found!");
        }
    }

    private void processEndLoop() {
        int iterNum = 1;
        for (int i = 0; i < Variables.ITERATIONS; i++) {
            for (TestStep s : Configurations.LOOP_STEPS) {
                addTestStep(s.getApplication().trim(),
                        s.getArea().trim(), s.getFunctionName().trim(),
                        Integer.toString(iterNum), Integer.toString(Variables.PARAM_ROW),
                        s.getParameterCount().trim(), s.getOnFail().trim());
                getLoopParameters(s);
            }
            Variables.PARAM_ROW++;
            iterNum++;
        }
        addEndLoop();
    }

    private void getLoopParameters(TestStep s) {
        if (Integer.valueOf(s.getParameterCount()) > 0) {
            int paramCount =
                    Integer.valueOf(s.getParameterCount());
            if (paramCount > 0) {
                addTestStepParameters();
                String[] params = s.getParameters();
                for (int p = 0; p < paramCount; p++) {
                    String param = params[p];
                    if (param.contains(Configurations.LOOP_PREFIX)) {
                        Variables.COL_NAME = param.replace(
                                Configurations.LOOP_PREFIX, "").trim();
                        Variables.COL_NAME = Variables.COL_NAME.replace(
                                Configurations.LOOP_SUFFIX, "").trim();

                        param = getLoopParameter(Variables.COL_NAME, Variables.PARAM_ROW);
                    }
                    addTestStepParameter("Param" + (p + 1), param.trim());
                }
            }
        }
    }

    private void getIterationData(TestStep step) {
        Variables.PARAM_ROW = step.getStartRow();
        Variables.ITERATIONS = step.getIterations();
        addLoop(Variables.FILENAME.trim(),
                Integer.toString(Variables.PARAM_ROW),
                Integer.toString(Variables.ITERATIONS));
    }

    private void getRangedData(TestStep step) {
        Constants.RANGE = step.getIterationRange();
        String[] rangeVals = Constants.RANGE.split(",");
        for (String num : rangeVals) {
            int start;
            int end;
            if (num.contains("-")) {
                String[] startEnd = num.split("-");
                start = Integer.parseInt(startEnd[0].trim());
                end = Integer.parseInt(startEnd[1].trim());
                for (int n = start; n < end + 1; n++) {
                    Constants.ITERATION_VALS.add(n);
                }
            } else {
                Constants.ITERATION_VALS.add(Integer.parseInt(num.trim()));
            }
        }
        Variables.ITERATIONS = Constants.ITERATION_VALS.size();
        Constants.MAX_ROWNUMBER = Collections.max(Constants.ITERATION_VALS);
        Variables.PARAM_ROW = Constants.ITERATION_VALS.get(0);

        addLoop(Variables.FILENAME.trim(), Constants.RANGE.trim(),
                Integer.toString(Variables.ITERATIONS));
    }

    private void getFilterData(SPHNX_WorkSheet workSheet, TestStep step) {
        String filterData = step.getFilterData();
        String[] filterDataVals = filterData.split(";(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        List<String[]> filterRows = new ArrayList<>();

        for (String s : filterDataVals) {
            String[] splitData = s.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            splitData[2] = splitData[2].replaceAll("\"", "");

            filterRows.add(splitData);
        }
        String[] headerVals = new String[workSheet.getRow(0).getLastCell()];
        for (int i = 0; i < workSheet.getRow(0).getLastCell(); i++) {
            headerVals[i] = workSheet.getRow(0).get(i);
        }


        for (String[] row : filterRows) {

            int col = getColumn(headerVals, row[0]);    // get column number for each operation
            SPHNX_WorkSheet tempSheet = filter(row[1], row[2], col);

            Configurations.LOOP_FILE = tempSheet;
        }
    }

    private SPHNX_WorkSheet filter(String type, String value, int col) {
        Parser parser = new Parser();
        String parsedValue = parser.replaceDataReferenceInString(value, false);
        int index;
        int colNum;
        int valNum ;
        Date valDate;
        Date colDate;
        SPHNX_WorkSheet tempSheet;
        List<SPHNX_Row> rowVals;
        switch (type.toLowerCase().trim()) {
            case "=":
                index = 0;
                rowVals = new ArrayList<>();
                for (SPHNX_Row row : Configurations.LOOP_FILE.getSPHNX_Rows()) {
                    if (row.getRowNum() == 1) {
                        rowVals.add(row);
                        continue;
                    }
                    if (row.get(col).toLowerCase().equals(parsedValue.toLowerCase())) {
                        rowVals.add(row);
                    }
                    index++;
                }
                index = 0;
                tempSheet = new SPHNX_WorkSheet();
                tempSheet.setSPHNX_Rows(rowVals.size());
                for (SPHNX_Row r1 : rowVals) {
                    tempSheet.addRow(index, r1);
                    index++;
                }
                return tempSheet;
            case "contains":
                index = 0;
                rowVals = new ArrayList<>();
                for (SPHNX_Row row : Configurations.LOOP_FILE.getSPHNX_Rows()) {
                    if (row.getRowNum() == 1) {
                        rowVals.add(row);
                        continue;
                    }
                    if (row.get(col).toLowerCase().contains(parsedValue.toLowerCase())) {
                        rowVals.add(row);
                    }
                    index++;
                }
                index = 0;
                tempSheet = new SPHNX_WorkSheet();
                tempSheet.setSPHNX_Rows(rowVals.size());
                for (SPHNX_Row r1 : rowVals) {
                    tempSheet.addRow(index, r1);
                    index++;
                }
                return tempSheet;
            case "startswith":
                index = 0;
                rowVals = new ArrayList<>();
                for (SPHNX_Row row : Configurations.LOOP_FILE.getSPHNX_Rows()) {
                    if (row.getRowNum() == 1) {
                        rowVals.add(row);
                        continue;
                    }
                    if (row.get(col).toLowerCase().startsWith(parsedValue.toLowerCase())) {
                        rowVals.add(row);
                    }
                    index++;
                }
                index = 0;
                tempSheet = new SPHNX_WorkSheet();
                tempSheet.setSPHNX_Rows(rowVals.size());
                for (SPHNX_Row r1 : rowVals) {
                    tempSheet.addRow(index, r1);
                    index++;
                }
                return tempSheet;
            case "endsWith":
                index = 0;
                rowVals = new ArrayList<>();
                for (SPHNX_Row row : Configurations.LOOP_FILE.getSPHNX_Rows()) {
                    if (row.getRowNum() == 1) {
                        rowVals.add(row);
                        continue;
                    }
                    if (row.get(col).toLowerCase().endsWith(parsedValue.toLowerCase())) {
                        rowVals.add(row);
                    }
                    index++;
                }
                index = 0;
                tempSheet = new SPHNX_WorkSheet();
                tempSheet.setSPHNX_Rows(rowVals.size());
                for (SPHNX_Row r1 : rowVals) {
                    tempSheet.addRow(index, r1);
                    index++;
                }
                return tempSheet;
            case ">":
                tempSheet = new SPHNX_WorkSheet();
                rowVals = new ArrayList<>();
                try {
                    valNum = Integer.valueOf(parsedValue);
                    index = 0;
                    for (SPHNX_Row row : Configurations.LOOP_FILE.getSPHNX_Rows()) {
                        if (row.getRowNum() == 0) {
                            rowVals.add(row);
                            continue;
                        }
                        colNum = Integer.valueOf(row.get(col));
                        if (colNum > valNum) {
                            rowVals.add(row);
                        }
                        index++;
                    }
                    index = 0;
                    tempSheet.setSPHNX_Rows(rowVals.size());
                    for (SPHNX_Row r1 : rowVals) {
                        tempSheet.addRow(index, r1);
                        index++;
                    }

                } catch (Exception e) {
                    index = 0;
                    try {
                        valDate = new SimpleDateFormat("MM/dd/yyyy").parse(parsedValue);
                        for (SPHNX_Row row : Configurations.LOOP_FILE.getSPHNX_Rows()) {
                            if (row.getRowNum() == 1) {
                                rowVals.add(row);
                                continue;
                            }
                            colDate = new SimpleDateFormat("MM/dd/yyy").parse(
                                    parser.replaceDataReferenceInString(row.get(col), false));
                            int dateVal = colDate.compareTo(valDate);
                            if (dateVal > 0) {
                                rowVals.add(row);
                            }
                            index++;
                        }
                        index = 0;
                        tempSheet.setSPHNX_Rows(rowVals.size());
                        for (SPHNX_Row r1 : rowVals) {
                            tempSheet.addRow(index, r1);
                            index++;
                        }
                    } catch (ParseException pe) {
                        LOGNREPORT.sphnxError(TDA, "Error parsing date.");
                    }
                }
                return tempSheet;
            case ">=":
                tempSheet = new SPHNX_WorkSheet();
                rowVals = new ArrayList<>();
                if (StringUtils.isNumeric(parsedValue)) {
                    valNum = Integer.valueOf(parsedValue);
                    index = 0;
                    for (SPHNX_Row row : Configurations.LOOP_FILE.getSPHNX_Rows()) {
                        if (row.getRowNum() == 1) {
                            rowVals.add(row);
                            continue;
                        }
                        colNum = Integer.valueOf(row.get(col));
                        if (colNum >= valNum) {
                            rowVals.add(row);
                        }
                        index++;
                    }
                    index = 0;
                    tempSheet.setSPHNX_Rows(rowVals.size());
                    for (SPHNX_Row r1 : rowVals) {
                        tempSheet.addRow(index, r1);
                        index++;
                    }
                } else {
                    index = 0;
                    try {
                        valDate = new SimpleDateFormat("MM/dd/yyyy").parse(parsedValue);
                        for (SPHNX_Row row : Configurations.LOOP_FILE.getSPHNX_Rows()) {
                            if (row.getRowNum() == 1) {
                                rowVals.add(row);
                                continue;
                            }
                            colDate = new SimpleDateFormat("MM/dd/yyyy").parse(
                                    parser.replaceDataReferenceInString(row.get(col), false));
                            int dateVal = colDate.compareTo(valDate);
                            if (dateVal >= 0) {
                                rowVals.add(row);
                            }
                            index++;
                        }
                        index = 0;
                        tempSheet.setSPHNX_Rows(rowVals.size());
                        for (SPHNX_Row r1 : rowVals) {
                            tempSheet.addRow(index, r1);
                            index++;
                        }
                    } catch (ParseException pe) {
                        LOGNREPORT.sphnxError(TDA, "Error parsing date.");
                    }

                }

                return tempSheet;
            case "<":
                tempSheet = new SPHNX_WorkSheet();
                rowVals = new ArrayList<>();
                try {
                    valNum = Integer.valueOf(parsedValue);
                    index = 0;
                    for (SPHNX_Row row : Configurations.LOOP_FILE.getSPHNX_Rows()) {
                        if (row.getRowNum() == 1) {
                            rowVals.add(row);
                            continue;
                        }
                        colNum = Integer.valueOf(row.get(col));
                        if (colNum < valNum) {
                            rowVals.add(row);
                        }
                        index++;
                    }
                    index = 0;
                    tempSheet.setSPHNX_Rows(rowVals.size());
                    for (SPHNX_Row r1 : rowVals) {
                        tempSheet.addRow(index, r1);
                        index++;
                    }

                } catch (Exception e) {
                    index = 0;
                    try {
                        valDate = new SimpleDateFormat("MM/dd/yyyy").parse(parsedValue);
                        for (SPHNX_Row row : Configurations.LOOP_FILE.getSPHNX_Rows()) {
                            if (row.getRowNum() == 1) {
                                rowVals.add(row);
                                continue;
                            }
                            colDate = new SimpleDateFormat("MM/dd/yyyy").parse(
                                    parser.replaceDataReferenceInString(row.get(col), false));
                            int dateVal = colDate.compareTo(valDate);
                            if (dateVal < 0) {
                                rowVals.add(row);
                            }
                            index++;
                        }
                        index = 0;
                        tempSheet.setSPHNX_Rows(rowVals.size());
                        for (SPHNX_Row r1 : rowVals) {
                            tempSheet.addRow(index, r1);
                            index++;
                        }
                    } catch (ParseException pe) {
                        LOGNREPORT.sphnxError(TDA, "Error parsing date.");
                    }
                }
                return tempSheet;
            case "<=":
                tempSheet = new SPHNX_WorkSheet();
                rowVals = new ArrayList<>();
                try {
                    valNum = Integer.valueOf(parsedValue);
                    index = 0;
                    for (SPHNX_Row row : Configurations.LOOP_FILE.getSPHNX_Rows()) {
                        if (row.getRowNum() == 1) {
                            rowVals.add(row);
                            continue;
                        }
                        colNum = Integer.valueOf(row.get(col));
                        if (colNum <= valNum) {
                            rowVals.add(row);
                        }
                        index++;
                    }
                    index = 0;
                    tempSheet.setSPHNX_Rows(rowVals.size());
                    for (SPHNX_Row r1 : rowVals) {
                        tempSheet.addRow(index, r1);
                        index++;
                    }
                } catch (Exception e) {
                    index = 0;
                    try {
                        valDate = new SimpleDateFormat("MM/dd/yyyy").parse(parsedValue);
                        for (SPHNX_Row row : Configurations.LOOP_FILE.getSPHNX_Rows()) {
                            if (row.getRowNum() == 1) {
                                rowVals.add(row);
                                continue;
                            }
                            colDate = new SimpleDateFormat("MM/dd/yyyy").parse(
                                    parser.replaceDataReferenceInString(row.get(col), false));
                            int dateVal = colDate.compareTo(valDate);
                            if (dateVal <= 0) {
                                rowVals.add(row);
                            }
                            index++;
                        }
                        index = 0;
                        tempSheet.setSPHNX_Rows(rowVals.size());
                        for (SPHNX_Row r1 : rowVals) {
                            tempSheet.addRow(index, r1);
                            index++;
                        }
                    } catch (ParseException pe) {
                        LOGNREPORT.sphnxError(TDA, "Error parsing date.");
                    }
                }
                return tempSheet;
            default:
                return null;
        }
    }

    private int getColumn(String[] headerVals, String s) {
        int column = 0;
        for (String h : headerVals) {
            if (s.toLowerCase().equals(h.toLowerCase())) {
                return column;
            }
            column++;
        }
        return column;
    }


    private void processTestStep(TestStep step) {
        addTestStep(step.getApplication().trim(),
                step.getArea().trim(), step.getFunctionName().trim(),
                step.getParameterCount().trim(), step.getOnFail().trim());
        int paramCount =
                Integer.valueOf(step.getParameterCount());
        if (paramCount > 0) {
            addTestStepParameters();
            String[] params = step.getParameters();
            for (int i = 0; i < paramCount; i++) {
                try {
                    addTestStepParameter("Param" + (i + 1), params[i].trim());
                } catch (IndexOutOfBoundsException e) {
                    addTestStepParameter("Param" + (i + 1), "");
                }
            }
        } else {
            addTestStepParameters();
            addTestStepParameter("None", "");
        }
    }

    private String getLoopParameter(String colName, int paramRow) {
        int colNum = 0;
        for (int i = 0; i < Configurations.LOOP_FILE.getRow(0).getLastCell(); i++) {
            if (Configurations.LOOP_FILE.getRow(0).get(i).toUpperCase()
                    .equals(colName.toUpperCase())) {
                colNum = i;
                break;
            }
        }
        return Configurations.LOOP_FILE.getRow(paramRow - 1).get(colNum);
    }


    private String getSpreadSheetID(String URL) {
        String[] URLsplit = URL.split("https://docs.google.com/spreadsheets/d/");
        return URLsplit[1].split("/")[0];
    }

    private String getScenarioFilePath(String fileName) {
        ParseConfigurations parseConfigurations = new ParseConfigurations();
        String scenarioDir = parseConfigurations.getAllConfigurations()
                .get(ParseConfigurations.Configs.SCENARIOS_DIR.name());
//        String currentDir = System.getProperty("user.dir");
//        File file = new File(currentDir);
//        File parentDir =
//                new File(file.getParent() + File.separator +
//                        "resources" + File.separator +
//                        "scenarios" + File.separator +
//                        fileName );
        //return parentDir.getAbsolutePath();
        return scenarioDir + File.separator + fileName;
    }

    private String getLoopFilePath(String fileName) {
        String currentDir = System.getProperty("user.dir");
        File file = new File(currentDir);
        File parentDir = new File(file.getParent()+ File.separator);
        parentDir =
                new File(parentDir.getParent() + File.separator +
                        "resources" + File.separator +
                        fileName);
        return parentDir.getAbsolutePath();
    }

    /**************************************************************************
     * Name:		AddTestRun
     *
     * Description:
     *
     *
     * Params:	IN	TestRunID
     *
     * Return Value:	N/A
     *
     * Author:		Utopia Solutions
     *
     * Change log:
     * 	Date		Who			What
     *  05/03/2017  Jeff Heytow initial creation
     ***************************************************************************/
    private void addTestRun(String testRunName) {
        this.objXMLTestRun = this.objXMLRoot.addElement("TestRun")
                .addAttribute("testRun", testRunName);
    }

    private void addTestScenario(String scenarioID, String toRun) {
        this.objXMLTestScenario = this.objXMLTestRun.addElement("TestScenario")
                .addAttribute("TestScenario", scenarioID)
                .addAttribute("Run", toRun);
        isFirstTestCase = true;
    }

    private void addTestScenarioProperties() {
        this.objXMLProperties = this.objXMLTestScenario.addElement("Properties");
    }

    private void addScenarioProp(String propName, String value) {
        this.objXMLProperties.addElement("property")
                .addAttribute("name", propName)
                .setText(value);
        if (propName.equalsIgnoreCase("MODE")) {
            this.mode = value;
        }
    }

    private void addTestCase(String tcID, String toRun) {
        this.objXMLTestCase = this.objXMLTestScenario.addElement("TestCase")
                .addAttribute("TestCase", tcID)
                .addAttribute("run", toRun);
        if (isFirstTestCase) {
            this.objXMLTestCase.addAttribute("lastTest", "true")
                    .addAttribute("MODE", this.mode);
            isFirstTestCase = false;
        }

    }

    private void addTestStep(String app, String area, String func, String pCount, String fail) {
        this.objXMLTestStep = this.objXMLTestCase.addElement("TestStep")
                .addAttribute("application", app)
                .addAttribute("area", area)
                .addAttribute("functionName", func)
                .addAttribute("onFail", fail);
    }

    //overloaded method to add test step row for those test steps that are inside a loop (basically an iteration)
    private void addTestStep(String app, String area, String func, String dataRow, String pCount, String fail) {
        this.objXMLTestStep = this.objXMLTestCase.addElement("TestStep")
                .addAttribute("application", app)
                .addAttribute("area", area)
                .addAttribute("functionName", func)
                .addAttribute("dataRow", dataRow)
                .addAttribute("onFail", fail);
    }

    //overloaded method to add test step row for those test steps that are inside a loop (basically an iteration)
    private void addTestStep(String app, String area, String func, String iter, String dataRow, String pCount, String fail) {
        this.objXMLTestStep = this.objXMLTestCase.addElement("TestStep")
                .addAttribute("application", app)
                .addAttribute("area", area)
                .addAttribute("functionName", func)
                .addAttribute("iteration", iter)
                .addAttribute("dataRow", dataRow)
                .addAttribute("onFail", fail);
    }

    private void addLoop(String dataFile, String startRange, String iterations) {
        this.objXMLLoop = this.objXMLTestCase.addElement("LOOP")
                .addAttribute("dataFile", dataFile)
                .addAttribute("startRange", startRange)
                .addAttribute("iterations", iterations);
    }

    private void addEndLoop() {
        this.objXMLLoop = this.objXMLTestCase.addElement("END_LOOP");
    }

    private void addIteration(String iterationNum, String dataRow) {
        this.objXMLIteration = this.objXMLLoop.addElement("iteration")
                .addAttribute("iterationNumber", iterationNum)
                .addAttribute("dataRow", dataRow)
                .addAttribute("status", "0");
    }

    private void addIterationStep(String app, String area, String func, String pCount, String fail) {
        this.objXMLIterationStep = objXMLIteration.addElement("iterationStep")
                .addAttribute("application", app)
                .addAttribute("area", area)
                .addAttribute("functionName", func)
                .addAttribute("onFail", fail);
    }

    private void addTestStepParameters() {
        this.objXMLTestStepParams = this.objXMLTestStep.addElement("Parameters");
    }

    private void addTestStepParameter(String paramName, String value) {
        this.objXMLTestStepParams.addElement("parameter")
                .addAttribute("name", paramName)
                .setText(value);
    }

    private void addIterationStepParameters() {
        this.objXMLIterationStepParams = this.objXMLIterationStep.addElement("Parameters");
    }

    private void addIterationStepParameter(String paramName, String value) {
        this.objXMLIterationStepParams.addElement("parameter")
                .addAttribute("name", paramName)
                .setText(value);
    }

    private void prettyPrintXML() {
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer;
        try {
            System.out.println();
            writer = new XMLWriter(System.out, format);
            writer.write(this.oXML);
            System.out.println();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*************************************************************************
     * Name:		SaveTestDefinitionToFile
     *
     * Description:
     *
     *
     * Params:	IN	FilePath - folder in which to save the file
     *
     *
     * Return Value:	N/A
     *
     * Author:		Utopia Solutions
     *
     * Change log:
     * 	Date		Who			What
     * 	mm/dd/yyyy	name		initial creation
     *
     * **************************************************************************/
    private String saveTestDefinitionFile(String runXMLFolder) throws IOException {
        String xmlFilePath = "";
        try {
            xmlFilePath = runXMLFolder + File.separatorChar + mTestRunName + ".xml";
            FileOutputStream xmlFile = new FileOutputStream(xmlFilePath);
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter(xmlFile, format);
            writer.write(this.oXML);
            writer.flush();
            return xmlFilePath;
        } catch (UnsupportedEncodingException e) {
            LOGNREPORT.sphnxError(TDA, "Error in saving test definition. Exception:" + e.getLocalizedMessage());
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            LOGNREPORT.sphnxError(TDA, "File " + xmlFilePath + "not found!");
            LOGNREPORT.sphnxError(TDA, "Error in saving test definition. Exception:" + e.getLocalizedMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
