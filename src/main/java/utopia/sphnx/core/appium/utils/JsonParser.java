package utopia.sphnx.core.appium.utils;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class JsonParser {

    JSONParser parser = new JSONParser();
    Object object;

    public JsonParser(String filePath) {
        try {
            object = parser.parse(new FileReader(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JSONArray array = new JSONArray();
        array.add(object);
    }

    public JSONArray getJsonParsedObject() {
        return (JSONArray) object;
    }
}