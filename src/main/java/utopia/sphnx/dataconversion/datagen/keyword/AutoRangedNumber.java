package utopia.sphnx.dataconversion.datagen.keyword;

import org.joda.time.LocalDate;
import utopia.sphnx.dataconversion.datagen.GenerateData;

/**
 * Created by heyto on 8/23/2017.
 */
public class AutoRangedNumber implements AutoKeyword {
    public AutoRangedNumber() {
    }

    public String getKeyword() {
        return "RANGED_NUMBER";
    }

    public String generateData() {
        LocalDate year = new LocalDate();
        LocalDate date = GenerateData.getInstance().getDateBetween((long)year.getYear(), (long)(year.getYear() + 10));
        return date.toString("YYYY");
    }

    public String generateData(String modifier) {
        LocalDate now = new LocalDate();
        byte var5 = -1;
        switch(modifier.hashCode()) {
            case 67452:
                if(modifier.equals("DAY")) {
                    var5 = 0;
                }
                break;
            case 2719805:
                if(modifier.equals("YEAR")) {
                    var5 = 2;
                }
                break;
            case 73542240:
                if(modifier.equals("MONTH")) {
                    var5 = 1;
                }
        }

        LocalDate date;
        switch(var5) {
            case 0:
                date = GenerateData.getInstance().getDateBetween((long)now.getDayOfMonth(), (long)(now.getDayOfMonth() + 7));
                return date.toString("dd");
            case 1:
                date = GenerateData.getInstance().getDateBetween((long)now.getMonthOfYear(), (long)(now.getMonthOfYear() + 12));
                return date.toString("mm");
            case 2:
                date = GenerateData.getInstance().getDateBetween((long)now.getYear(), (long)(now.getYear() + 10));
                return date.toString("YYYY");
            default:
                return null;
        }
    }
}
