package utopia.sphnx.dataconversion.datagen.keyword;


import utopia.sphnx.dataconversion.datagen.GenerateData;

/**
 * Created by heyto on 2/21/2017.
 */
public class AutoNumber implements AutoKeyword {

    @Override
    public String getKeyword() {
        return "NUMBER";
    }

    @Override
    public String generateData() {
        return GenerateData.getInstance().numerify("#");
    }

    @Override
    public String generateData(String modifier) {
        String retVal = GenerateData.getInstance().numerify(modifier);

        if(retVal.charAt(0) == '0') {
            retVal = replace(modifier, retVal);
        }
        if(retVal.charAt(1) == '0') {
            retVal = replace(modifier, retVal);
        }
        return retVal;
    }

    private String replace(String modifier, String retVal) {
        boolean replaceZero = false;
        while (!replaceZero) {
            retVal = GenerateData.getInstance().numerify(modifier);
            if(retVal.charAt(0) != '0') {
                replaceZero = true;
            }
        }
        return retVal;
    }
}
