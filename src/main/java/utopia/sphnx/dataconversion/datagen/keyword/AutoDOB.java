package utopia.sphnx.dataconversion.datagen.keyword;

import org.joda.time.LocalDate;
import utopia.sphnx.dataconversion.datagen.GenerateData;

/**
 * Created by heyto on 8/23/2017.
 */
public class AutoDOB implements AutoKeyword {
    public AutoDOB() {
    }

    public String getKeyword() {
        return "DOB";
    }

    public String generateData() {
        LocalDate DOB = GenerateData.getInstance().getDateBetween(18L, 65L);
        return DOB.toString("MM/dd/YYYY");
    }

    public String generateData(String modifier) {
        String retVal = "";
        LocalDate DOB;
        if(modifier.equals("NONE")) {
            DOB = GenerateData.getInstance().getDateBetween(18L, 65L);
            retVal = DOB.toString("MMMM dd, YYYY");
        } else {
            String[] modArray = modifier.split("_");
            String format = modArray[0];
            DOB = GenerateData.getInstance().getDateBetween(18L, 65L);
            if(modArray.length > 1) {
                if(modArray[1].contains("-")) {
                    String[] rangeArray = modArray[1].split("-");
                    long early = Long.parseLong(rangeArray[0]);
                    long later = Long.parseLong(rangeArray[1]);
                    DOB = GenerateData.getInstance().getDateBetween(early, later);
                } else {
                    long early = Long.parseLong(modArray[1]);
                    DOB = GenerateData.getInstance().getDateBetween(early, early + 1L);
                }
            }

            String var12 = format.toUpperCase();
            byte var13 = -1;
            switch(var12.hashCode()) {
                case -2046892343:
                    if(var12.equals("M-D-YY")) {
                        var13 = 24;
                    }
                    break;
                case -2045043379:
                    if(var12.equals("M/D/YY")) {
                        var13 = 13;
                    }
                    break;
                case -2024983465:
                    if(var12.equals("MDYYYY")) {
                        var13 = 10;
                    }
                    break;
                case -2017317568:
                    if(var12.equals("MMDDYY")) {
                        var13 = 20;
                    }
                    break;
                case -1611929504:
                    if(var12.equals("MMDDYYYY")) {
                        var13 = 8;
                    }
                    break;
                case -1460335360:
                    if(var12.equals("MM/DD/YYYY")) {
                        var13 = 0;
                    }
                    break;
                case -949231936:
                    if(var12.equals("DD/MM/YYYY")) {
                        var13 = 2;
                    }
                    break;
                case -734391571:
                    if(var12.equals("D/M/YYYY")) {
                        var13 = 3;
                    }
                    break;
                case -652835776:
                    if(var12.equals("MM-DD-YYYY")) {
                        var13 = 4;
                    }
                    break;
                case -165510432:
                    if(var12.equals("DD-MM-YY")) {
                        var13 = 17;
                    }
                    break;
                case -141732352:
                    if(var12.equals("DD-MM-YYYY")) {
                        var13 = 5;
                    }
                    break;
                case -108250208:
                    if(var12.equals("DD/MM/YY")) {
                        var13 = 14;
                    }
                    break;
                case 2102633:
                    if(var12.equals("DMYY")) {
                        var13 = 23;
                    }
                    break;
                case 2362103:
                    if(var12.equals("MDYY")) {
                        var13 = 22;
                    }
                    break;
                case 31482793:
                    if(var12.equals("M-D-YYYY")) {
                        var13 = 6;
                    }
                    break;
                case 751022176:
                    if(var12.equals("DDMMYYYY")) {
                        var13 = 9;
                    }
                    break;
                case 1783721321:
                    if(var12.equals("D-M-YYYY")) {
                        var13 = 7;
                    }
                    break;
                case 1808337197:
                    if(var12.equals("M/D/YYYY")) {
                        var13 = 1;
                    }
                    break;
                case 1866381654:
                    if(var12.equals("MM-D-YY")) {
                        var13 = 18;
                    }
                    break;
                case 1990680713:
                    if(var12.equals("D-M-YY")) {
                        var13 = 19;
                    }
                    break;
                case 1992529677:
                    if(var12.equals("D/M/YY")) {
                        var13 = 15;
                    }
                    break;
                case 2011952448:
                    if(var12.equals("DDMMYY")) {
                        var13 = 21;
                    }
                    break;
                case 2020633161:
                    if(var12.equals("DMYYYY")) {
                        var13 = 11;
                    }
                    break;
                case 2023899424:
                    if(var12.equals("MM-DD-YY")) {
                        var13 = 16;
                    }
                    break;
                case 2081159648:
                    if(var12.equals("MM/DD/YY")) {
                        var13 = 12;
                    }
            }

            switch(var13) {
                case 0:
                    retVal = DOB.toString("MM/dd/YYYY");
                    break;
                case 1:
                    retVal = DOB.toString("M/d/YYYY");
                    break;
                case 2:
                    retVal = DOB.toString("dd/MM/YYYY");
                    break;
                case 3:
                    retVal = DOB.toString("d/M/YYYY");
                    break;
                case 4:
                    retVal = DOB.toString("MM-dd-YYYY");
                    break;
                case 5:
                    retVal = DOB.toString("dd-MM-YYYY");
                    break;
                case 6:
                    retVal = DOB.toString("M-d-YYYY");
                    break;
                case 7:
                    retVal = DOB.toString("d-M-YYYY");
                    break;
                case 8:
                    retVal = DOB.toString("MMddYYYY");
                    break;
                case 9:
                    retVal = DOB.toString("ddMMYYYY");
                    break;
                case 10:
                    retVal = DOB.toString("MdYYYY");
                    break;
                case 11:
                    retVal = DOB.toString("dMYYYY");
                    break;
                case 12:
                    retVal = DOB.toString("MM/dd/YY");
                    break;
                case 13:
                    retVal = DOB.toString("M/d/YY");
                    break;
                case 14:
                    retVal = DOB.toString("dd/MM/YY");
                    break;
                case 15:
                    retVal = DOB.toString("d/M/YY");
                    break;
                case 16:
                    retVal = DOB.toString("MM-dd-YY");
                    break;
                case 17:
                    retVal = DOB.toString("dd-MM-YY");
                    break;
                case 18:
                    retVal = DOB.toString("MM-d-YY");
                    break;
                case 19:
                    retVal = DOB.toString("d-M-YY");
                    break;
                case 20:
                    retVal = DOB.toString("MMddYY");
                    break;
                case 21:
                    retVal = DOB.toString("ddMMYY");
                    break;
                case 22:
                    retVal = DOB.toString("MdYY");
                    break;
                case 23:
                    retVal = DOB.toString("dMYY");
                    break;
                case 24:
                    retVal = DOB.toString("M-d-YY");
            }
        }

        return retVal;
    }
}
