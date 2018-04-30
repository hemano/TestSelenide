package utopia.sphnx.dataconversion.datagen.keyword;

/**
 * Created by heyto on 2/21/2017.
 */
public class KeywordFactory {
    public KeywordFactory() {
    }

    public AutoKeyword generateKeyword(String function) {
        switch(function.toUpperCase()) {
            case "FULL_NAME":
                return new AutoFullName();
            case "FIRST_NAME":
                return new AutoFirstName();
            case "MIDDLE_NAME":
                return new AutoMiddleName();
            case "LAST_NAME":
                return new AutoLastName();
            case "STREET":
                return new AutoStreet();
            case "CITY":
                return new AutoCity();
            case "STATE":
                return new AutoState();
            case "ZIP_CODE":
                return new AutoZipCode();
            case "ADDRESS":
                return new AutoAddress();
            case "DOB":
                return new AutoDOB();
            case "TODAY":
                return new AutoToday();
            case "PHONE_NUMBER":
                return new AutoPhoneNumber();
            case "FORMATTED_NUMBER":
                return new AutoNumber();
            case "RANGED_NUMBER":
                return new AutoRangedNumber();
            default:
                return new AutoFirstName();
        }
    }
}