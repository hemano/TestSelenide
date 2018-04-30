package utopia.sphnx.dataconversion.datagen.keyword;


import utopia.sphnx.dataconversion.datagen.GenerateData;

/**
 * Created by heyto on 8/23/2017.
 */
public class AutoLastName implements AutoKeyword {
    public AutoLastName() {
    }

    public String getKeyword() {
        return "LAST_NAME";
    }

    public String generateData() {
        return GenerateData.getInstance().getLastName();
    }

    public String generateData(String modifier) {
        return modifier.equals("NONE")? GenerateData.getInstance().getLastName():"Need More Data...";
    }
}