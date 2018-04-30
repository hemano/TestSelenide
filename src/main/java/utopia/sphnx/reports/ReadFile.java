package utopia.sphnx.reports;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * <p>This class will handle all the different files reading</p>
 *
 *
 */
public class ReadFile{

    /**
     * @param fileName of the file present in resources
     * @return Content of the file as String
     */
    public static String readFileAsString(String fileName) {

        InputStream in = utopia.sphnx.reports.ReadFile.class.getResourceAsStream("/" + fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String currentLine = "";
        String allText = "";
        try {
            while ((currentLine = reader.readLine()) != null) {
                allText += currentLine;
            }

            return allText;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}