package conversion.scenarios;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heyto on 12/14/2017.
 */
public class TestCase {
    private boolean toRun;
    private boolean hasLooping;

    private String name;
    private List<TestStep> testSteps;

    public TestCase() {
        this.name = null;
        this.toRun = false;

        this.testSteps = new ArrayList<>();

        this.hasLooping = false;
    }

    public TestCase(String testCase, boolean toExecute) {
        this.name = testCase;
        this.toRun = toExecute;

        this.testSteps = new ArrayList<>();

        this.hasLooping = false;
    }

    public boolean isToRun() {
        return toRun;
    }

    public void setToRun(boolean toRun) {
        this.toRun = toRun;
    }

    public boolean hasLooping() {
        return hasLooping;
    }

    public void setLooping(boolean hasLooping) {
        this.hasLooping = hasLooping;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TestStep> getTestSteps() {
        return testSteps;
    }

    public void setTestSteps(List<TestStep> testSteps) {
        this.testSteps = testSteps;
    }


}
