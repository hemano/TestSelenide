package conversion.scenarios;

/**
 * Created by heyto on 12/15/2017.
 */
public class LoopStep extends TestStep {

    private boolean isLoopStep;


    public LoopStep() {
        this.startRow = 0;
        this.iterations = 0;

        this.isLoopStep = true;

        this.iterationRange = null;
        this.loopFile = null;

        this.filterData = null;
    }

    public LoopStep(String app, String area, String func, String paramCount, String onFail) {
        super(app, area, func, paramCount, onFail);

        this.startRow = 0;
        this.iterations = 0;

        this.isLoopStep = true;

        this.iterationRange = null;
        this.loopFile = null;

        this.filterData = null;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public String getLoopFile() {
        return loopFile;
    }

    public void setLoopFile(String loopFile) {
        this.loopFile = loopFile;
    }

    @Override
    public boolean isLoopStep() {
        return isLoopStep;
    }

    @Override
    public String getIterationRange() {
        return iterationRange;
    }

    @Override
    public void setIterationRange(String iterationRange) {
        this.iterationRange = iterationRange;
    }

    @Override
    public boolean isRanged() {
        if(this.iterationRange != null) {
            return (!this.iterationRange.equals(""));
        } else {
            return false;
        }
    }

    @Override
    public boolean hasFilter() {
        if(this.filterData != null) {
            return true;
        } else {
            return false;
        }
    }
}
