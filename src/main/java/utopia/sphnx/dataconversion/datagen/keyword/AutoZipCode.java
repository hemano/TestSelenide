package utopia.sphnx.dataconversion.datagen.keyword;


import utopia.sphnx.dataconversion.datagen.GenerateData;

/**
 * Created by heyto on 8/23/2017.
 */
public class AutoZipCode implements AutoKeyword {
    public AutoZipCode() {
    }

    public String getKeyword() {
        return "ZIP_CODE";
    }

    public String generateData() {
        String ZIP = GenerateData.getInstance().bothify("#####");
        return ZIP.toUpperCase();
    }

    public String generateData(String modifier) {
        byte var4 = -1;
        switch(modifier.hashCode()) {
            case 53:
                if(modifier.equals("5")) {
                    var4 = 1;
                }
                break;
            case 1567:
                if(modifier.equals("10")) {
                    var4 = 2;
                }
                break;
            case 2402104:
                if(modifier.equals("NONE")) {
                    var4 = 0;
                }
        }

        String ZIP;
        switch(var4) {
            case 0:
                return GenerateData.getInstance().getAddress().getZipCode();
            case 1:
                ZIP = GenerateData.getInstance().getAddress().getZipCode();
                if(ZIP.length() > 5) {
                    return ZIP.substring(0, 5);
                }

                return ZIP;
            case 2:
                return GenerateData.getInstance().numerify("##########");
            default:
                ZIP = GenerateData.getInstance().bothify(modifier.toUpperCase());
                return ZIP.toUpperCase();
        }
    }
}
