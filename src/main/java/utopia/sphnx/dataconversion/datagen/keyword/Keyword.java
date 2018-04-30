package utopia.sphnx.dataconversion.datagen.keyword;


import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;

import static utopia.sphnx.dataconversion.datagen.configuration.Constants.*;
import static utopia.sphnx.logging.LoggerReporter.LOGNREPORT;

/**
 * Created by heyto on 2/21/2017.
 */
public class Keyword {
    private String name;
    private String type;
    private String dataSource;
    private String dataType;
    private String[] modifiers;

    public static HashMap<String, utopia.sphnx.dataconversion.datagen.keyword.Keyword> keywordDictionary;

    private static final String KW = utopia.sphnx.dataconversion.datagen.keyword.Keyword.class.getCanonicalName();

    public Keyword() {
        this.name = null;
        this.type = null;
        this.dataSource = null;
        this.dataType = null;
        this.modifiers = null;
    }

    public Keyword(String n, String t, String ds, String dt) {
        this.name = n;
        this.type = t;
        this.dataSource = ds;
        this.dataType = dt;
        this.modifiers = null;

    }

    /*************************************************************************
     * Name:		LoadKeywordsFromExcel
     *
     * Description:
     *
     *
     * Params:	IN	mapPath - This is the name of the excel map
     * 				sheet that lists the utopia.sphnx.core.dataconversion.datagen to be stored
     *
     * Return Value:	iLoadStatus indicates the success or failure of the function
     *
     * Author:		Utopia Solutions
     *
     * Change log:
     * 	Date		Who			What
     *  05/02/2017  Jeff Heytow initial creation
     ***************************************************************************/
    public static HashMap<String, utopia.sphnx.dataconversion.datagen.keyword.Keyword> loadKeywordsFromExcel(Path mapPath) {
        boolean loadStatus = true;
        HashMap<String, utopia.sphnx.dataconversion.datagen.keyword.Keyword> keywords = new HashMap<>();
        try {
            FileInputStream excelFile = new FileInputStream(String.valueOf(mapPath));
            Workbook workbook = new XSSFWorkbook(excelFile);
            // add test run to in-memory XML doc:

            Sheet typeSheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = typeSheet.iterator();

            while (rowIterator.hasNext()) {
                Row curRow = rowIterator.next();
                if (curRow.getRowNum() == 0) {
                    continue;
                }

                String kwName = curRow.getCell(KW_NAME_COL).getStringCellValue();
                String kwType = curRow.getCell(KW_TYPE_COL).getStringCellValue();
                String kwDSource = curRow.getCell(KW_DSOURCE_COL).getStringCellValue();
                String kwDType = "";
                if (curRow.getCell(KW_DTYPE_COL) != null) {
                    kwDType = curRow.getCell(KW_DTYPE_COL).getStringCellValue();
                }

                keywords.put(kwName, new utopia.sphnx.dataconversion.datagen.keyword.Keyword(kwName.toUpperCase(), kwType, kwDSource, kwDType));
            }

        } catch (FileNotFoundException e) {
            loadStatus = false;
        } catch (IOException e) {
            loadStatus = false;
        }

        if (!loadStatus) {
            throw new NullPointerException("Error reading file. Please check input.");
        }

        return keywords;
    }

    /**
     * Procedure:  ConvertKeyword
     * <p>
     * Description:
     * Converts the keyword object to its current value based
     * on its properties
     *
     * @return Returns the current value of the keyword object or Null if the keyword
     * cannot be converted
     * <p>
     * Author:
     * Utopia Solutions
     */
//    public String convertKeyword() {
//        switch (this.type.toUpperCase()) {
//            case "FUNCTION":
//                parser.parse(this.dataSource, this.modifier);
//                return parser.getAutoKeyword();
//            case "FILE":
//                parser.parse(this.fileName);
//                return parser.getRandomData();
//            default:
//                return null;
//        }
//    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String[] getModifiers() {
        return modifiers;
    }

    public void setModifier(String[] modifiers) {
        this.modifiers = modifiers;
    }

    public HashMap<String, utopia.sphnx.dataconversion.datagen.keyword.Keyword> getKeywordDictionary() {
        return keywordDictionary;
    }

    public void setKeywordDictionary(HashMap<String, utopia.sphnx.dataconversion.datagen.keyword.Keyword> kd) {
        keywordDictionary = kd;
    }

    public void addKeyword(String key, utopia.sphnx.dataconversion.datagen.keyword.Keyword val) {
        keywordDictionary.put(key, val);
    }

    /**
     * Procedure:  getKeyword
     * <p>
     * Description:
     * Retrieves the specified keyword object from the keyword collection
     *
     * @param key name of the keyword to retrieve
     * @return the specified Keyword object or null if the keyword
     * cannot be found in the Keyword Dictionary.
     * <p>
     * Author:
     * Utopia Solutions
     */
    public static String getKeyword(String key) {
        // currentDate|MM/DD/YYYY
        // colorMerge|Red|Blue
        String[] keyArray = key.split("\\|");
        String[] mods = new String[keyArray.length];

        String retVal = "";
        try {
            utopia.sphnx.dataconversion.datagen.keyword.Keyword keyword = keywordDictionary.get(keyArray[0]);
            retVal = keyword.name;

            //check for modifier
            if (keyArray.length > 1) {
                for (int i = 1; i < keyArray.length; i++) {
                    mods[i - 1] = keyArray[i];
                }
                keyword.setModifier(mods);
            } else {
                mods[0] = "NONE";
                keyword.setModifier(mods);
            }
            switch (keyword.getType().toUpperCase()) {
                case "FUNCTION":
                    retVal = parse(keyword.dataSource, mods[0]);
                    break;
                case "FILE":
                    retVal = ("(" + keyword.name + " value comes from a FILE)");
                    break;
                default:
                    retVal = "";
                    break;
            }
        } catch (Exception e) {
            LOGNREPORT.sphnxError(KW, "Value: '" + key + "' was not found in the Metadata file and is also not a Keyword.");
//            System.out.println("Keyword: '" + key + "' was not found!");
        }

        return retVal;
    }

    private static String parse(String keyword, String modifier) {
        KeywordFactory keywordFactory = new KeywordFactory();
        AutoKeyword aKeyword = keywordFactory.generateKeyword(keyword);

        return aKeyword.generateData(modifier.toUpperCase());
    }
}