package utopia.sphnx.dataconversion.datagen.keyword;


import utopia.sphnx.dataconversion.datagen.GenerateData;

/**
 * Created by heyto on 8/23/2017.
 */
public class AutoState implements AutoKeyword {
    public AutoState() {
    }

    public String getKeyword() {
        return "STATE";
    }

    public String generateData() {
        return GenerateData.getInstance().getAddress().getStateName();
    }

    public String generateData(String modifier) {
        return modifier.equals("NONE")? GenerateData.getInstance().getAddress().getStateName():"Need more state generator...";
    }
}
