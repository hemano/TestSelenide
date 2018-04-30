package utopia.sphnx.dataconversion.datagen.keyword;



import utopia.sphnx.dataconversion.datagen.GenerateData;

import java.util.Random;

/**
 * Created by heyto on 8/23/2017.
 */
public class AutoStreet implements AutoKeyword {
    public AutoStreet() {
    }

    public String getKeyword() {
        return "STREET";
    }

    public String generateData() {
        String num = GenerateData.getInstance().numerify("###");
        String retNum = "";
        if(num.charAt(0) == 48) {
            Random random = new Random();
            int randNum = random.nextInt(9) + 1;
            retNum = num.replace("0", Integer.toString(randNum));
        } else {
            retNum = num;
        }

        return retNum + " " + GenerateData.getInstance().getStreet();
    }

    public String generateData(String modifier) {
        String num;
        String retNum;
        Random random;
        int randNum;
        if(modifier.equals("NONE")) {
            num = GenerateData.getInstance().numerify("###");
            if(num.charAt(0) == 48) {
                random = new Random();
                randNum = random.nextInt(9) + 1;
                retNum = num.replace("0", Integer.toString(randNum));
            } else {
                retNum = num;
            }

            return retNum + " " + GenerateData.getInstance().getStreet();
        } else {
            num = GenerateData.getInstance().numerify(modifier);
            if(num.charAt(0) == 48) {
                random = new Random();
                randNum = random.nextInt(9) + 1;
                retNum = num.replace("0", Integer.toString(randNum));
            } else {
                retNum = num;
            }

            return retNum + " " + GenerateData.getInstance().getStreet();
        }
    }
}

