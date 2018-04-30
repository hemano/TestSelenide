package utopia.sphnx.dataconversion.datagen.execution;

import com.google.common.base.Joiner;
import utopia.sphnx.config.ParseConfigurations;
import utopia.sphnx.dataconversion.datagen.configuration.Configurations;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static utopia.sphnx.logging.LoggerReporter.LOGNREPORT;

/**
 * Created by heyto on 4/12/2017.
 */
public class ExecutionContext {

    public static HashMap<String, Object> ecDictionary;

    private static final String DELIMITER = ":=";

    private static final String EC = utopia.sphnx.dataconversion.datagen.execution.ExecutionContext.class.getCanonicalName();

    public ExecutionContext() {
        this.ecDictionary = new HashMap<>();
    }

    public ExecutionContext(String ec, String value) {
        this.ecDictionary = new HashMap<>();
        this.ecDictionary.put(ec, value);
    }

    public static void setECValue(String key, Object value) {
        String uKey = key.toUpperCase();
        if(value instanceof String) {
            ecDictionary.put(uKey, value);
        }
        else if(value instanceof String[][]){
            ecDictionary.put(uKey, value);
        }
        else if(value instanceof List){
            ecDictionary.put(uKey, value);
        }
        else if (value instanceof String[]){
            ecDictionary.put(uKey, value);
        }
        else {
            ecDictionary.put(uKey,value);
        }
    }

    public static void removeECKey(String key) {
        String uKey = key.toUpperCase();
            ecDictionary.remove(uKey);
    }

    public static Object getECValue(String key) {
        String keyName = key;
        String result = "";

        boolean isComplex = false;

        if(key.indexOf(".") > -1) {
            String[] arrKeys = keyName.split("\\.");
            keyName = arrKeys[0];
            isComplex = true;
        }

        try {
            Object value = ecDictionary.get(keyName.toUpperCase());
            if(value == null) {
                LOGNREPORT.sphnxError(EC, "Failed to find EC Key '" + key + "'");
                return "";
            }
            if(value instanceof String) {
                result = (String) value;
                // Key = JSON_Data.SavingsType.Maintenance.SavingsAmount
                if(isComplex) {
                    String[] arrValues = result.split("@@");
                    result = "-1";
                    if(arrValues[0].equalsIgnoreCase("json")) {
                        result = getJsonValue(key, arrValues[1]);
                    }
                    if(arrValues[0].equalsIgnoreCase("xml")) {
                        result = getXmlValue(key, arrValues[1]);
                    }
                }
                else {
                    return result;
                }
            }
            else if(value instanceof String[][]){
                return value;
            }
            else if(value instanceof String[]){
                return value;
            }

        } catch (Exception e) {
            LOGNREPORT.sphnxError(EC, "Failed to find EC Key '" + key + "'");
            return "";
        }
        return result;

    }

    public static String getJsonValue(String key, String json) {
        String value = "";
        String[] arrKeys = key.split("\\.");
        int valueOffset = arrKeys.length - 1;

        json = json.replace("\"", "");
        json = json.replace("[", "");
        json = json.replace("]", "");
        json = json.replace("}", "");

        String[] arrItems = new String[100000]; // large enough to never be exceeded.
        int itemIndex = 0;

        // build an array of all of the json components
        String[] arrJson = json.split("\\{");
        for (int i = 0; i < arrJson.length; i++) {
            String[] arrLines = arrJson[i].split(",");
            for (int j = 0; j < arrLines.length; j++) {
                String[] arrPair = arrLines[j].split(":");
                if (arrPair[0].length() > 1) {
                    arrItems[itemIndex] = arrPair[0];
                    itemIndex++;
                }
                if(arrPair.length > 1) {
                    if (arrPair[1].length() > 1) {
                        arrItems[itemIndex] = arrPair[1];
                        itemIndex++;
                    }
                }
            }
        }

        // Iterate through the items array and look for a pattern match

        for (int i = 0; i < arrItems.length; i++) {
            boolean isMatch = true;
            // IE: JSON_Data.SavingsType.Maintenance.SavingsAmount
            // Loop through all of the fields in the key to see if they match a sequence in the json
            for (int j = 1; j < arrKeys.length; j++) {
                int x = j - 1; //
                if (arrItems[i + x].equalsIgnoreCase(arrKeys[j])) {
                    // If it does match, keep walking through the fields to see if we have a pattern match
                } else {
                    // If any element in the key does not match, break from the loop and continue searching
                    isMatch = false;
                    break;
                }
            }
            // If we found a pattern match, then the value will be the very next field in the array.
            if (isMatch) {
                value = arrItems[i + valueOffset];
                break;
            }
        }
        return value;
    }

    public static String getXmlValue(String key, String xml) {
        String value = "";
        String[] arrKeys = key.split("\\.");
        int valueOffset = arrKeys.length - 1;

        //<![CDATA[
        // ]]>
        xml = xml.replace("<![CDATA[", "");
        xml = xml.replace("]]>", "");

        xml = xml.replace("[", "");
        xml = xml.replace("]", "");
        xml = xml.replace("\n", "");
        xml = xml.replace("\r", "");

        String[] arrItems = new String[100000]; // large enough to never be exceeded.
        int itemIndex = 0;

        // build an array of all of the json components
        String[] arrXML = xml.split("<");
        for (int i = 0; i < arrXML.length; i++) {
            if (arrXML[i].trim().length() > 0) {
                String first = arrXML[i].trim().substring(0, 1);
                if (first.equalsIgnoreCase("/") || first.equalsIgnoreCase("!")) {
                    // Ignore end tags and other noise
                } else {
                    String[] arrPair = arrXML[i].split(">");
                    if (arrPair[0].length() > 1) {
                        arrItems[itemIndex] = arrPair[0];
                        itemIndex++;
                    }
                    if(arrPair.length > 1) {
                        if (arrPair[1].length() > 1) {
                            arrItems[itemIndex] = arrPair[1];
                            itemIndex++;
                        }
                    }
                }
            }
        }

        // Iterate through the items array and look for a pattern match
        for (int i = 0; i < arrItems.length; i++) {
            boolean isMatch = true;
            // IE: JSON_Data.SavingsType.Maintenance.SavingsAmount
            // Loop through all of the fields in the key to see if they match a sequence in the json
            for (int j = 1; j < arrKeys.length; j++) {
                int x = j - 1; //
                if (arrItems[i + x].equalsIgnoreCase(arrKeys[j])) {
                    // If it does match, keep walking through the fields to see if we have a pattern match
                } else {
                    // If any element in the key does not match, break from the loop and continue searching
                    isMatch = false;
                    break;
                }
            }
            // If we found a pattern match, then the value will be the very next field in the array.
            if (isMatch) {
                value = arrItems[i + valueOffset];
                break;
            }
        }
        return value;
    }

    public static Map<String, Object> getECDictionary() {
        return ecDictionary;
    }

    public static void setEcDictionary(HashMap<String, Object> values) {
        ecDictionary = values;
    }

    public static boolean reset() {
        try {
            ecDictionary.clear();
            return true;
        } catch (Exception e){
            LOGNREPORT.sphnxError(EC,"Failed to clear out Execution Context");
            return false;
        }
    }

    /*************************************************************************
     * Name:		loadExecutionContextFromTextFile
     *
     * Description:  Load the variables from past tests that user saved previously
     *
     *
     * Params:	IN	ecPath - This is the name of the excel execution context
     * 				sheet that lists the execution context variables that were
     * 				previously saved to be reloaded
     *
     * Return Value:	iLoadStatus indicates the success or failure of the function
     *
     * Author:		Utopia Solutions
     *
     * Change log:
     * 	Date		Who			What
     *  05/02/2017  Jeff Heytow initial creation
     ***************************************************************************/
    public static HashMap<String, Object> loadExecutionContextFromTextFile(Path ecPath) {
        if (ecDictionary == null) {
            ecDictionary = new HashMap<>();
        }

        boolean loadStatus = true;

        String tagName = "";
        String value = "";

        BufferedReader bufferedReader = null;
        try {
            String currentLine;

            bufferedReader = new BufferedReader(new FileReader(String.valueOf(ecPath)));

            while ((currentLine = bufferedReader.readLine()) != null) {
                String[] lineArr = currentLine.split(DELIMITER);
                if (lineArr.length != 2) {
                    LOGNREPORT.sphnxError(EC,"Execution context value " + lineArr[0] + " has an invalid value! Please remove or fix this entry and rerun the test.");
                }
                tagName = lineArr[0].trim();
                value = lineArr[1].trim();

                ecDictionary.put(tagName, value);
            }

        } catch (FileNotFoundException e) {
            loadStatus = false;
            LOGNREPORT.sphnxError(EC,ecPath.toString() + " file not found! Please check input.");
        } catch (IOException e) {
            loadStatus = false;
            LOGNREPORT.sphnxError(EC,ecPath.toString() + " is not valid! Please check input.");
        }
        if (!loadStatus) {
            throw new NullPointerException("Error reading file. Please check input.");
        }
        LOGNREPORT.sphnxInfo(EC,"Execution Context Dictionary loaded.");
        return ecDictionary;
    }

    public static HashMap<String, Object> loadExecutionContextFromTextFile() {
        if (ecDictionary == null) {
            ecDictionary = new HashMap<>();
        }
        LOGNREPORT.sphnxInfo(EC,"No Execution Context File provided, Dictionary stored in memory.");
        return ecDictionary;
    }

    /**
     * Procedure:  isECVar
     * <p>
     * Description:
     * Determines if the specified string is an Execution Context variable.
     *
     * @param context string to be checked against the collection of
     *                loaded execution context.
     *                <p>
     * @return Returns true if the string is execution context, or false if not
     * <p>
     * Author:
     * Utopia Solutions
     */
    public boolean isECVar(String context) {
        int preSize = Configurations.EC_PREFIX.length();
        String ecPre = context.substring(0, preSize);

        if (Configurations.EC_PREFIX.toUpperCase().equals(ecPre.toUpperCase())) {
            return true;
        }
        // if not...
        return false;
    }

    public static void writeToECFile() {

        ParseConfigurations parseConfigurations = new ParseConfigurations();
        String ecDirPath = parseConfigurations
                .getAllConfigurations().get(ParseConfigurations.Configs.EC_DIR.name());
        BufferedWriter bufferedWriter = null;

        FileWriter fileWriter = null;
            String FILENAME = ecDirPath + File.separator + "EC.txt";

        try {
            LOGNREPORT.sphnxInfo(EC, "Saving Execution Context file to: " + FILENAME);
            fileWriter = new FileWriter(FILENAME, false);
            bufferedWriter = new BufferedWriter(fileWriter);

            int ecCount = 0;
            for (Map.Entry<String, Object> entry : ecDictionary.entrySet()) {
                String key = entry.getKey();
                String val = "";
                val = toString(entry.getValue());
                if (val.equals("")) {
                    LOGNREPORT.sphnxWarning(EC,System.getProperty("line.separator") + key + " is being stored with a null value.", "purple", 1);
                }
                bufferedWriter.write(key + DELIMITER + val);
                if(ecCount != (ecDictionary.size() - 1)) {
                    bufferedWriter.newLine();
                }
                ecCount++;
            }

            if(ecCount == 1) {
                LOGNREPORT.sphnxInfo(EC, "Execution Context file 'EC.txt' created!");
            } else {
                LOGNREPORT.sphnxInfo(EC, "Execution Context file 'EC.txt' updated!");
            }

        } catch (Exception e) {
            LOGNREPORT.sphnxError(EC, "Execution Context was not stored!");
        } finally {
            try {
                if(bufferedWriter != null) {
                    bufferedWriter.close();
                }
                if(fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                LOGNREPORT.sphnxError(EC, "Execution Context was not stored!");
            }
        }
    }

    public static void removeFromECFile(String keyToRemove) throws IOException{
        ParseConfigurations parseConfigurations = new ParseConfigurations();
        String ecDirPath = parseConfigurations
                .getAllConfigurations().get(ParseConfigurations.Configs.EC_DIR.name());

        FileWriter fileWriter = null;
        String FILENAME = ecDirPath + File.separator + "EC.txt";

        File ecFile = new File(FILENAME);
        BufferedReader br = new BufferedReader(new FileReader(FILENAME));

        File temp = new File("temp.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
        String removeID = keyToRemove.toUpperCase();
        String currentLine;
        while((currentLine = br.readLine()) != null){
            String trimmedLine = currentLine.trim();
            if(!trimmedLine.startsWith(removeID)){
                bw.write(currentLine + System.getProperty("line.separator"));
            }
        }
        bw.close();
        br.close();
        boolean delete = ecFile.delete();
        boolean b = temp.renameTo(ecFile);
    }

    public static String toString(Object value) {
        String valueInString = "";
        if(value instanceof String) {
            valueInString = (String) value;
        }
        else if(value instanceof String[][]){
           valueInString = Arrays
                    .stream((String[][])value)
                    .map(Arrays::toString)
                    .collect(Collectors.joining(";"));
                    //.collect(Collectors.joining(System.lineSeparator()));

        }
        else if(value instanceof String[]){
            valueInString = Arrays.stream((String[]) value)
                    .map(Object::toString)
                    .collect(Collectors.joining(";"));
                    //.collect(Collectors.joining(System.lineSeparator()));
        }
        else if(value instanceof List) {
            if(((List) value).get(0) instanceof List){
                List<List<String>> values = (List<List<String>>) value;
                String[][] array = values.stream()
                        .map(l -> l.toArray(new String[0]))
                        .toArray(String[][]::new);
                valueInString = Arrays
                        .stream(array)
                        .map(Arrays::toString)
                        //.collect(Collectors.joining(System.lineSeparator()));
                        .collect(Collectors.joining(";"));
            }
            else {
                valueInString = Joiner.on(",").join((List) value);
            }
        }

        return valueInString;
    }
}
