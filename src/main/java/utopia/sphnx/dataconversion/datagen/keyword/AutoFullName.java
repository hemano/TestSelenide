package utopia.sphnx.dataconversion.datagen.keyword;


import utopia.sphnx.dataconversion.datagen.GenerateData;

/**
 * Created by heyto on 8/23/2017.
 */
public class AutoFullName implements AutoKeyword {
    public AutoFullName() {
    }

    public String getKeyword() {
        return "FIRST_NAME";
    }

    public String generateData() {
        String firstName = (String) GenerateData.getInstance().getFirstName().getLeft();
        String lastName = GenerateData.getInstance().getLastName();
        return firstName + " " + lastName;
    }

    public String generateData(String modifier) {
        String firstName = null;
        String lastName = null;

        switch(modifier.toUpperCase()) {
            case "MALE":
                firstName = GenerateData.getInstance().getGenderFirstName(GenerateData.Gender.MALE);
                lastName = GenerateData.getInstance().getLastName();
                return firstName + " " + lastName;

            case "FEMALE":
                firstName =  GenerateData.getInstance().getGenderFirstName(GenerateData.Gender.FEMALE);
                lastName = GenerateData.getInstance().getLastName();
                return firstName + " " + lastName;
            default:
                firstName = (String) GenerateData.getInstance().getFirstName().getLeft();
                lastName = GenerateData.getInstance().getLastName();
                return firstName + " " + lastName;
        }
    }
}
