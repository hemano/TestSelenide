package utopia.sphnx.dataconversion.datagen.keyword;


import utopia.sphnx.dataconversion.datagen.GenerateData;

/**
 * Created by heyto on 8/23/2017.
 */
public class AutoAddress implements AutoKeyword {
    public AutoAddress() {
    }

    public String getKeyword() {
        return "ADDRESS";
    }

    public String generateData() {
        Address address = GenerateData.getInstance().getAddress();
        return address.getStreet() + ", " + address.getCity() + ", " + address.getStateName() + ", " + address.getZipCode();
    }

    public String generateData(String modifier) {
        if(modifier.equals("NONE")) {
            Address address = GenerateData.getInstance().getAddress();
            return address.getStreet() + ", " + address.getCity() + ", " + address.getStateName() + ", " + address.getZipCode();
        } else {
            return "Need more address generator...";
        }
    }
}
