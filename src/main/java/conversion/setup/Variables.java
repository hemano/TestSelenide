package conversion.setup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heyto on 5/16/2017.
 */
public class Variables {
    /* Test Definition Variables */
    public static boolean TEST_TO_RUN;
    public static boolean HAS_MODE;
    public static boolean HAS_DEVICE;
    public static boolean HAS_BROWSER;
    public static boolean TD_HAS_PROPERTIES;

    /* Test Case Variables */
    public static List<String> TESTS_TO_RUN = new ArrayList<>();
    public static String TC_SCENARIO_FILE = "";
    public static String TC_TO_RUN = "";

    /* Test Step Variables */
    public static String TEST_CASE_ID = "";
    public static String RUN = "";
    public static String APPLICATION = "";
    public static String AREA = "";
    public static String FUNCTION_NAME = "";
    public static String PARAM_COUNT = "";
    public static String ON_FAIL = "";

    /* Looping Variables */
    public static int PARAM_ROW = 0;
    public static int ITERATIONS = 0;
    public static int FIRST_ITERATION_STEP;
    public static String FILENAME = "";
    public static String COL_NAME = "";

    /* Conditional Variables */
    public static boolean IS_TEST_CASE_ROW;
    public static boolean IS_TEST_STEP_ROW;
    public static boolean IS_BEGIN_LOOP_ROW;
    public static boolean IS_END_LOOP_ROW;
    public static boolean IS_ITERATION_STEP_ROW;
    public static boolean S_HAS_PARAMETERS;
    public static boolean IS_BLANK_ROW;
    public static boolean IS_INVALID_ROW;
    public static boolean IS_INVALID_LOOPDATA;

    public static boolean OUTPUT_TO_XML = false;
    public static boolean IS_LOOPING = false;
    public static boolean END_OF_FILE = false;
    public static ArrayList<String> INVALID_TESTS = new ArrayList<String>();

    public static int CURRENT_ROW_NUMBER = 2;

    public static boolean hasLoader = false;
    public static boolean hasEmptyCell = false;
    public static String LOADER;

    public final static char CR  = (char) 0x0D;
    public final static char LF  = (char) 0x0A;
    public final static String CRLF  = "" + CR + LF;     // "" forces conversion to string

    public static long EVENT_START;
    public static long EVENT_END;
}
