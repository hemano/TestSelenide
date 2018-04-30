package utopia.sphnx.dataconversion.datagen.keyword;


import org.joda.time.LocalDate;
import utopia.sphnx.dataconversion.datagen.GenerateData;


/**
 * Created by heyto on 2/21/2017.
 */
public class AutoToday implements AutoKeyword {
    private boolean IS_DAYS = false;
    private boolean IS_WEEKS = false;
    private boolean IS_MONTHS = false;
    private boolean IS_YEARS = false;

    @Override
    public String getKeyword() {
        return "date";
    }

    @Override
    public String generateData() {
        LocalDate date;
        date = GenerateData.getInstance().getToday().toLocalDate();
        return date.toString("MM/dd/YYYY");
    }

    @Override
    public String generateData(String modifier) {
        LocalDate date;
        String retVal = "";

        if (modifier.equals("NONE")) {
            date = GenerateData.getInstance().getToday().toLocalDate();
            retVal = date.toString("MMMM dd, YYYY");
        } else {
            String[] mods = modifier.split("_");
            String format = mods[0].trim();

            if (mods.length == 1) {
                getModType(mods[0]);
                if(IS_DAYS || IS_WEEKS || IS_MONTHS || IS_YEARS) {
                    int modLen = mods[0].length();
                    String modNum = mods[0].substring(0, modLen - 1);
                    String modTime = String.valueOf(mods[0].charAt(modLen - 1));
                    int early;

                    if (modNum.contains("-")) {
                        modNum = modNum.replace("-", "").trim();
                        early = Integer.parseInt(modNum);
                        date = GenerateData.getInstance().getTodayMinus(early, modTime).toLocalDate();
                        retVal = date.toString("MMMM dd, YYYY");
                    } else {
                        early = Integer.parseInt(modNum);
                        date = GenerateData.getInstance().getTodayPlus(early, modTime).toLocalDate();
                        retVal = date.toString("MMMM dd, YYYY");
                    }
                } else {
                    date = GenerateData.getInstance().getToday().toLocalDate();
                }
            } else {
                int modLen = mods[1].length();
                String modNum = mods[1].substring(0, modLen - 1);
                String modTime = String.valueOf(mods[1].charAt(modLen - 1));
                int early;

                if (modNum.contains("-")) {
                    modNum = modNum.replace("-", "").trim();
                    early = Integer.parseInt(modNum);
                    date = GenerateData.getInstance().getTodayMinus(early, modTime).toLocalDate();
                } else {
                    early = Integer.parseInt(modNum);
                    date = GenerateData.getInstance().getTodayPlus(early, modTime).toLocalDate();
                }
            }

            switch (format.toUpperCase()) {
                case ("MM/DD/YYYY"):
                    retVal = date.toString("MM/dd/YYYY");
                    break;
                case ("M/D/YYYY"):
                    retVal = date.toString("M/d/YYYY");
                    break;
                case ("DD/MM/YYYY"):
                    retVal = date.toString("dd/MM/YYYY");
                    break;
                case ("D/M/YYYY"):
                    retVal = date.toString("d/M/YYYY");
                    break;
                case ("MM-DD-YYYY"):
                    retVal = date.toString("MM-dd-YYYY");
                    break;
                case ("DD-MM-YYYY"):
                    retVal = date.toString("dd-MM-YYYY");
                    break;
                case ("M-D-YYYY"):
                    retVal = date.toString("M-d-YYYY");
                    break;
                case ("D-M-YYYY"):
                    retVal = date.toString("d-M-YYYY");
                    break;
                case ("MMDDYYYY"):
                    retVal = date.toString("MMddYYYY");
                    break;
                case ("DDMMYYYY"):
                    retVal = date.toString("ddMMYYYY");
                    break;
                case ("MDYYYY"):
                    retVal = date.toString("MdYYYY");
                    break;
                case ("DMYYYY"):
                    retVal = date.toString("dMYYYY");
                    break;
                case ("MM/DD/YY"):
                    retVal = date.toString("MM/dd/YY");
                    break;
                case ("M/D/YY"):
                    retVal = date.toString("M/d/YY");
                    break;
                case ("DD/MM/YY"):
                    retVal = date.toString("dd/MM/YY");
                    break;
                case ("D/M/YY"):
                    retVal = date.toString("d/M/YY");
                    break;
                case ("MM-DD-YY"):
                    retVal = date.toString("MM-dd-YY");
                    break;
                case ("DD-MM-YY"):
                    retVal = date.toString("dd-MM-YY");
                    break;
                case ("MM-D-YY"):
                    retVal = date.toString("MM-d-YY");
                    break;
                case ("D-M-YY"):
                    retVal = date.toString("d-M-YY");
                    break;
                case ("MMDDYY"):
                    retVal = date.toString("MMddYY");
                    break;
                case ("DDMMYY"):
                    retVal = date.toString("ddMMYY");
                    break;
                case ("MDYY"):
                    retVal = date.toString("MdYY");
                    break;
                case ("DMYY"):
                    retVal = date.toString("dMYY");
                    break;
                case ("M-D-YY"):
                    retVal = date.toString("M-d-YY");
                    break;
                default:
                    break;
            }
        }
        return retVal;
    }

    private void getModType(String mod) {
        if(mod.toLowerCase().contains("d") && !mod.toLowerCase().contains("w") &&
                !mod.toLowerCase().contains("m") && !mod.toLowerCase().contains("y")) {
            IS_DAYS = true;
            return;
        }
        if(mod.toLowerCase().contains("w") && !mod.toLowerCase().contains("d") &&
                !mod.toLowerCase().contains("m") && !mod.toLowerCase().contains("y")) {
            IS_WEEKS = true;
            return;
        }
        if(mod.toLowerCase().contains("m") && !mod.toLowerCase().contains("d") &&
                !mod.toLowerCase().contains("w") && !mod.toLowerCase().contains("y")) {
            IS_MONTHS = true;
            return;
        }
        if(mod.toLowerCase().contains("y") && !mod.toLowerCase().contains("d") &&
                !mod.toLowerCase().contains("w") && !mod.toLowerCase().contains("m")) {
            IS_YEARS = true;
            return;
        }
    }
}
