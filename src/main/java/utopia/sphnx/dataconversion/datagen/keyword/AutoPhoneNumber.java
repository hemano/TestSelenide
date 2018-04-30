package utopia.sphnx.dataconversion.datagen.keyword;


import utopia.sphnx.dataconversion.datagen.generator.DataGenerator;

/**
 * Created by heyto on 8/23/2017.
 */
public class AutoPhoneNumber implements AutoKeyword {
    public AutoPhoneNumber() {
    }

    public String getKeyword() {
        return "PHONE_NUMBER";
    }

    public String generateData() {
        return DataGenerator.getInstance().getTelephoneNumber();
    }

    public String generateData(String modifier) {
        return modifier.equals("NONE")? DataGenerator.getInstance().getTelephoneNumber():"Need more phone generator...";
    }
}
