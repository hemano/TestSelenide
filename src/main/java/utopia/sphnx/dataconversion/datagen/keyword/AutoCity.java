package utopia.sphnx.dataconversion.datagen.keyword;


import utopia.sphnx.dataconversion.datagen.GenerateData;

/**
 * Created by heyto on 8/23/2017.
 */
public class AutoCity implements AutoKeyword {
    public AutoCity() {
    }

    public String getKeyword() {
        return "CITY";
    }

    public String generateData() {
        return GenerateData.getInstance().getAddress().getCity();
    }

    public String generateData(String modifier) {
        return modifier.equals("NONE")? GenerateData.getInstance().getAddress().getCity():"Need more city generator...";
    }
}
