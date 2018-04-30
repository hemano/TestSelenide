package utopia.sphnx.dataconversion.datagen.metadata;

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

import static utopia.sphnx.logging.LoggerReporter.LOGNREPORT;

/**
 * Class:  Metadata
 * <p>df
 * Purpose:	Store the Tagname / value pair of Metadata
 * <p>
 * Created by heyto on 3/5/2017.
 */
public class MetaData {
    //region initialization and constructors section

    public static HashMap<String, String> metaDictionary = new HashMap<>();

    private static final String MD = utopia.sphnx.dataconversion.datagen.metadata.MetaData.class.getCanonicalName();

    public MetaData() {

    }

    public MetaData(String tag, String value) {
        this.metaDictionary.put(tag, value);
    }

    /*************************************************************************
     * Name:		LoadMetaDataFromExcel
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
    public static HashMap<String, String> loadMetaDataFromExcel(Path mapPath) {
        boolean loadStatus = true;
        HashMap<String, String> metadata = new HashMap<>();
        String fileName = "";
        String sheetName = "";
        String tagName = "";
        String value = "";
        try {
            FileInputStream excelFile = new FileInputStream(String.valueOf(mapPath));
            Workbook workbook = new XSSFWorkbook(excelFile);
            // add test run to in-memory XML doc:

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet typeSheet = workbook.getSheetAt(i);
                Iterator<Row> rowIterator = typeSheet.iterator();

                while (rowIterator.hasNext()) {
                    Row curRow = rowIterator.next();
                    if (curRow.getRowNum() == 0) {
                        continue;
                    }
                    tagName = curRow.getCell(0).getStringCellValue();
                    //if value in excel is entered as numeric, convert the value to string, otherwise
                    //just use the string value
                    try {
                        value = curRow.getCell(1).getStringCellValue();
                    } catch (Exception e) {
                        int intValue = (int) curRow.getCell(1).getNumericCellValue();
                        value = Integer.toString(intValue);
                    }

                    if (metadata.containsKey(tagName.toUpperCase())) {
                        LOGNREPORT.sphnxWarning(MD, "Metadata '" + tagName + "' is a duplicate tagname. Value: '" + value + "' was not assigned.  Original value of: '" + metadata.get(tagName.toUpperCase()) + "' will be used!");
                    } else {
                        metadata.put(workbook.getSheetName(i).toUpperCase() + "." + tagName.toUpperCase(), value);
                    }
                }
            }

        } catch (FileNotFoundException e) {
            loadStatus = false;
        } catch (IOException e) {
            loadStatus = false;
        }

        if (!loadStatus) {
            throw new NullPointerException("Error reading file. Please check input.");
        }

        return metadata;
    }

    /**
     * Procedure:  getMetaData
     * <p>
     * Description:
     * Retrieves the specified metadata from the
     * metadata keyword dictionary
     *
     * @param key name of the metadata to retrieve
     * @return the specified metadata object or null if the metadata keyword
     * cannot be found in the metadata dictionary
     * <p>
     * Author:
     * Utopia Solutions
     */
    public static String getMetaData(String key) {
        String metaVal = "";
        try {
            metaVal = metaDictionary.get(key.toUpperCase());
        } catch (Exception e) {
            System.out.println("MetaData not found!");
        }
        return metaVal;
    }

    public static boolean setMetaData(String key, String value) {
        String metaVal = "";
        try {
            metaVal = metaDictionary.put(key, value);
            return true;
        } catch (Exception e) {
            LOGNREPORT.sphnxError(MD, "Failed to store Key/Value pair: " + key + ":" + value);
            return false;
        }
    }
}