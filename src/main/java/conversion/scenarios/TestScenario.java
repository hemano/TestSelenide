package conversion.scenarios;

import conversion.setup.Constants;
import conversion.utils.SPHNX_Row;
import conversion.utils.SPHNX_WorkSheet;
import utopia.sphnx.logging.LoggerReporter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heyto on 12/14/2017.
 */
public class TestScenario {
    private SPHNX_WorkSheet workSheet;
    private List<TestCase> testCases;

    private static String TESTSCENARIO = conversion.scenarios.TestScenario.class.getCanonicalName();

    public TestScenario(SPHNX_WorkSheet sheet) {
        this.workSheet = sheet;
        this.testCases = new ArrayList<>();
    }

    public List<TestCase> getTestCases() {
        return this.testCases;
    }

    public String getName() {
        return this.workSheet.getPageName();
    }

    public void createTestCases() throws Exception {
        TestCase testCase = new TestCase();
        LoopStep loopStep = null;
        for (int i = 1; i < this.workSheet.getSPHNX_Rows().length; i++) {
            SPHNX_Row curRow = normalizeRow((i + 2), this.workSheet.getSPHNX_Rows()[i]);
            SPHNX_WorkSheet.RowType rowType = SPHNX_WorkSheet.getRowType(curRow);
            if (rowType == null) {
                LoggerReporter.LOGNREPORT.sphnxError(TESTSCENARIO, "Could not determine row type for row number " + i + " in the test scenario.");
                throw new Exception("Could not determine row type for row number " + i + " in the test scenario.");
            }

            switch (rowType) {
                case TEST_CASE:
                    if (testCase.getTestSteps().size() > 0) {
                        this.testCases.add(testCase);
                        testCase = new TestCase();
                    }

                    testCase.setName(curRow.get(Constants.S_TC_COL));
                    testCase.setToRun(SPHNX_WorkSheet.toRun(curRow.get(Constants.S_RUN_COL)));

                    break;
                case TEST_STEP:
                    if (!testCase.hasLooping()) {
                        TestStep testStep = new TestStep(
                                curRow.get(Constants.S_APP_COL),
                                curRow.get(Constants.S_AREA_COL),
                                curRow.get(Constants.S_FUNC_COL),
                                curRow.get(Constants.S_PARAM_COUNT_COL),
                                curRow.get(Constants.S_ONFAIL_COL)
                        );
                        int paramIndex = 0;
                        int paramCount = Constants.S_PARAMS + Integer.valueOf(testStep.getParameterCount());
                        testStep.setParameters(Constants.S_PARAMS + Integer.valueOf(testStep.getParameterCount()));
                        if(paramCount > Constants.S_PARAMS) {
                            for (int x = Constants.S_PARAMS; x < paramCount; x++) {
                                try {
                                    testStep.getParameters()[paramIndex] = curRow.get(x);
                                    paramIndex++;
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    testStep.getParameters()[paramIndex] = "";
                                    paramIndex++;
                                }
                            }
                        }
                        testCase.getTestSteps().add(testStep);
                    } else {
                        loopStep = new LoopStep(
                                curRow.get(Constants.S_APP_COL),
                                curRow.get(Constants.S_AREA_COL),
                                curRow.get(Constants.S_FUNC_COL),
                                curRow.get(Constants.S_PARAM_COUNT_COL),
                                curRow.get(Constants.S_ONFAIL_COL)
                        );

                        try {
                            if (!curRow.get(Constants.S_PARAMS).equals("") &&
                                    curRow.get(Constants.S_PARAMS) != null) {
                                loopStep.setParameters(curRow.getLastCell() - Constants.S_PARAMS);
                                int paramIndex = 0;
                                for (int x = Constants.S_PARAMS; x < curRow.getLastCell(); x++) {
                                    loopStep.getParameters()[paramIndex] = curRow.get(x);
                                    paramIndex++;
                                }
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            // no parameters - do nothing
                        }
                        testCase.getTestSteps().add(loopStep);
                    }
                    break;
                case BEGIN_LOOP:
                    loopStep = new LoopStep(
                            curRow.get(Constants.S_APP_COL),
                            curRow.get(Constants.S_AREA_COL),
                            curRow.get(Constants.S_FUNC_COL),
                            curRow.get(Constants.S_PARAM_COUNT_COL),
                            curRow.get(Constants.S_ONFAIL_COL)
                    );

                    if (curRow.get(Constants.S_PARAMS + 1).contains("-") ||
                            curRow.get(Constants.S_PARAMS + 1).contains(",")) {
                        loopStep.setIterationRange(curRow.get(Constants.S_PARAMS + 1));
                        loopStep.setStartRow(0);
                    } else {
                        String[] cellData = curRow.get(Constants.S_PARAMS + 1).split(";");
                        int rowNum;

                        if (cellData.length > 1) {
                            loopStep.setFilterData(curRow.get(Constants.S_PARAMS + 1));
                            loopStep.setStartRow(1);
                        } else {
                            try {
                                rowNum = Integer.valueOf(cellData[0]);
                                loopStep.setStartRow(rowNum);
                            } catch (Exception e) {
                                // Only one entry in array and it is non-numeric
                                // so it is single-condition filter data
                                loopStep.setFilterData(curRow.get(Constants.S_PARAMS + 1));
                                loopStep.setStartRow(1);
                            }
                        }
                    }
                    loopStep.setLoopFile(curRow.get(Constants.S_PARAMS));

                    try {
                        if (!curRow.get(Constants.S_PARAMS + 2).equals("") ||
                                curRow.get(Constants.S_PARAMS + 2) != null) {

                            loopStep.setIterations(Integer.valueOf(curRow.get(
                                    Constants.S_PARAMS + 2)
                            ));
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        // no iterations - do nothing
                    }
                    testCase.setLooping(true);
                    testCase.getTestSteps().add(loopStep);
                    break;
                case END_LOOP:
                    loopStep = new LoopStep(
                            curRow.get(Constants.S_APP_COL),
                            curRow.get(Constants.S_AREA_COL),
                            curRow.get(Constants.S_FUNC_COL),
                            curRow.get(Constants.S_PARAM_COUNT_COL),
                            curRow.get(Constants.S_ONFAIL_COL)
                    );
                    testCase.setLooping(false);
                    testCase.getTestSteps().add(loopStep);
                    break;
                default:
                    testCase = null;
                    break;

            }
            if (i == (this.workSheet.getSPHNX_Rows().length - 1)) {
                this.testCases.add(testCase);
            }
        }
    }

    private SPHNX_Row normalizeRow(int rowNum, SPHNX_Row curRow) {
        int cellCount = 0;
        if (curRow != null) {
            for (int i = 0; i < curRow.getLastCell(); i++) {
                if (i == Constants.S_APP_COL && curRow.get(i).equals("")) {
                    break;
                } else {
                    cellCount++;
                }
            }
        }
        SPHNX_Row normalizedRow = new SPHNX_Row(rowNum, cellCount);
        for (int i = 0; i < cellCount; i++) {
            normalizedRow.add(i, curRow.get(i));
        }


        return normalizedRow;
    }
}