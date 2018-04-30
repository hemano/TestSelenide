package conversion.setup;

import conversion.scenarios.TestStep;
import conversion.utils.SPHNX_WorkSheet;

import java.nio.file.Path;
import java.util.List;

/**
 * Created by heyto on 5/2/2017.
 */
public class Configurations {
    /* Test Configuration */
    public static String DRIVER_TYPE;
    public static SPHNX_WorkSheet RUN_FILE;
    public static SPHNX_WorkSheet TEST_SCENARIO_FILE;
    public static SPHNX_WorkSheet LOOP_FILE;
    public static List<TestStep> LOOP_STEPS;



    /* Looping */
    public static Path LOOP_DATA;

    /* Flie Locations */
    public static Path KEYWORDS_FILE;
    public static Path METADATA_FILE;
    public static Path UI_DEFINITION_FILE;
    public static Path EXECUTON_CONTEXZT_FILE;
    public static Path RESOURCES_DIR;


    /* Keyword, MetaData, Execution Context */
    public static String LOOP_PREFIX = "[~";
    public static String LOOP_SUFFIX = "~]";
    public static String KWMD_PREFIX = "<";
    public static String KWMD_SUFFIX = ">";
    public static String EC_PREFIX = "{";
    public static String EC_SUFFIX = "}";
    public static String LOCATOR_SEPARATOR = ":";

    public static String APPLICATION_URL;


    public static boolean hasECFile = false;

}
