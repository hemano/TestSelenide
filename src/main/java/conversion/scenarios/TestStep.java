package conversion.scenarios;

/**
 * Created by heyto on 12/15/2017.
 */
public class TestStep {
    private boolean isLoopStep;

    private String      application;
    private String      area;
    private String      functionName;
    private String      parameterCount;
    private String      onFail;
    private String[]    parameters;

    // for looping
    protected String loopFile;
    protected int startRow;
    protected int iterations;
    protected String iterationRange;
    protected String filterData;


    public TestStep() {
        this.isLoopStep = false;

        this.application = null;
        this.area = null;
        this.functionName = null;
        this.parameterCount = null;
        this.onFail = null;

        this.parameters = null;
    }

    public TestStep(String app, String ar, String func, String paramCount, String onFail) {
        this.isLoopStep = false;

        this.application = app;
        this.area = ar;
        this.functionName = func;
        this.parameterCount = paramCount;
        this.onFail = onFail;

        this.parameters = null;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getParameterCount() {
        return parameterCount;
    }

    public void setParameterCount(String parameterCount) {
        this.parameterCount = parameterCount;
    }

    public String getOnFail() {
        return onFail;
    }

    public void setOnFail(String onFail) {
        this.onFail = onFail;
    }

    public String[] getParameters() {
        return parameters;
    }

    public void setParameters(int i) {
        this.parameters = new String[i];
    }

    public String getLoopFile() {
        return loopFile;
    }

    public boolean isLoopStep() {
        return isLoopStep;
    }

    public int getStartRow() {
        return startRow;
    }

    public int getIterations() {
        return iterations;
    }

    public boolean isRanged() {
        // do nothing
        return false;
    }

    public String getIterationRange() {
        return iterationRange;
    }

    public void setIterationRange(String iterationRange) {
        this.iterationRange = iterationRange;
    }

    public boolean hasFilter() {
        // do nothing
        return false;
    }

    public String getFilterData() {
        return filterData;
    }

    public void setFilterData(String filterData) {
        this.filterData = filterData;
    }
}
