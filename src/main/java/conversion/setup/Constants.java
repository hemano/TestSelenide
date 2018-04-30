package conversion.setup;

import java.util.ArrayList;

/**
 * Created by heyto on 5/15/2017.
 */
public class Constants {
    public static final String DELIMITER = "~";

    /* Step Type Constants */
    public static final String BEGIN_LOOP = "beginloop";
    public static final String END_LOOP = "endloop";

    /* Excel Test Definition Constants */
    public static final int TD_RUN_COL = 0;
    public static final int TD_SCENARIO_COL = 1;
    public static final int TD_MODE_COL = 2;
    public static final int TD_DEVICE_COL = 3;
    public static final int TD_BROWSER_COL = 4;
    public static final int TD_PARAMS = 5;

    /* Excel Scenario Definition Constants */
    public static final int S_TC_COL = 0;
    public static final int S_RUN_COL = 1;
    public static final int S_APP_COL = 2;
    public static final int S_AREA_COL = 3;
    public static final int S_FUNC_COL = 4;
    public static final int S_PARAM_COUNT_COL = 5;
    public static final int S_ONFAIL_COL = 6;
    public static final int S_PARAMS = 7;
    public static final int MAX_PARAMS = 20;

    /* Keyword Data File Constants */
    public static final int KW_NAME_COL = 0;
    public static final int KW_TYPE_COL = 1;
    public static final int KW_DSOURCE_COL = 2;
    public static final int KW_DTYPE_COL = 4;

    public static boolean IS_RANGED = false;
    public static ArrayList<Integer> ITERATION_VALS = new ArrayList<>();
    public static int MAX_ROWNUMBER;    //used for looping data to track the max row number if a range was specified
    public static String RANGE; //for looping, if a range is specified, the value is stored in this variable

    //Color constants
    /*
    examples for using color constants:
        System.out.println(ANSI_GREEN_BACKGROUND + "This text has a green background but default text!" + ANSI_RESET");
        System.out.println(ANSI_RED + "This text has red text but a default background!" + ANSI_RESET");
        System.out.println(ANSI_GREEN_BACKGROUND + ANSI_RED + "This text has a green background and red text!" + ANSI_RESET");
    */
    // Reset
    public static final String ANSI_RESET = "\033[0m";  // Text Reset

    // Regular Colors
    public static final String ANSI_BLACK = "\033[0;30m";   // BLACK
    public static final String ANSI_RED = "\033[0;31m";     // RED
    public static final String ANSI_GREEN = "\033[0;32m";   // GREEN
    public static final String ANSI_YELLOW = "\033[0;33m";  // YELLOW
    public static final String ANSI_BLUE = "\033[0;34m";    // BLUE
    public static final String ANSI_PURPLE = "\033[0;35m";  // PURPLE
    public static final String ANSI_CYAN = "\033[0;36m";    // CYAN
    public static final String ANSI_WHITE = "\033[0;37m";   // WHITE

    // Bold
    public static final String ANSI_BLACK_BOLD = "\033[1;30m";  // BLACK
    public static final String ANSI_RED_BOLD = "\033[1;31m";    // RED
    public static final String ANSI_GREEN_BOLD = "\033[1;32m";  // GREEN
    public static final String ANSI_YELLOW_BOLD = "\033[1;33m"; // YELLOW
    public static final String ANSI_BLUE_BOLD = "\033[1;34m";   // BLUE
    public static final String ANSI_PURPLE_BOLD = "\033[1;35m"; // PURPLE
    public static final String ANSI_CYAN_BOLD = "\033[1;36m";   // CYAN
    public static final String ANSI_WHITE_BOLD = "\033[1;37m";  // WHITE

    // Underline
    public static final String ANSI_BLACK_UNDERLINED = "\033[4;30m";  // BLACK
    public static final String ANSI_RED_UNDERLINED = "\033[4;31m";    // RED
    public static final String ANSI_GREEN_UNDERLINED = "\033[4;32m";  // GREEN
    public static final String ANSI_YELLOW_UNDERLINED = "\033[4;33m"; // YELLOW
    public static final String ANSI_BLUE_UNDERLINED = "\033[4;34m";   // BLUE
    public static final String ANSI_PURPLE_UNDERLINED = "\033[4;35m"; // PURPLE
    public static final String ANSI_CYAN_UNDERLINED = "\033[4;36m";   // CYAN
    public static final String ANSI_WHITE_UNDERLINED = "\033[4;37m";  // WHITE

    // Background
    public static final String ANSI_BLACK_BACKGROUND = "\033[40m";  // BLACK
    public static final String ANSI_RED_BACKGROUND = "\033[41m";    // RED
    public static final String ANSI_GREEN_BACKGROUND = "\033[42m";  // GREEN
    public static final String ANSI_YELLOW_BACKGROUND = "\033[43m"; // YELLOW
    public static final String ANSI_BLUE_BACKGROUND = "\033[44m";   // BLUE
    public static final String ANSI_PURPLE_BACKGROUND = "\033[45m"; // PURPLE
    public static final String ANSI_CYAN_BACKGROUND = "\033[46m";   // CYAN
    public static final String ANSI_WHITE_BACKGROUND = "\033[47m";  // WHITE

    // High Intensity
    public static final String ANSI_BLACK_BRIGHT = "\033[0;90m";  // BLACK
    public static final String ANSI_RED_BRIGHT = "\033[0;91m";    // RED
    public static final String ANSI_GREEN_BRIGHT = "\033[0;92m";  // GREEN
    public static final String ANSI_YELLOW_BRIGHT = "\033[0;93m"; // YELLOW
    public static final String ANSI_BLUE_BRIGHT = "\033[0;94m";   // BLUE
    public static final String ANSI_PURPLE_BRIGHT = "\033[0;95m"; // PURPLE
    public static final String ANSI_CYAN_BRIGHT = "\033[0;96m";   // CYAN
    public static final String ANSI_WHITE_BRIGHT = "\033[0;97m";  // WHITE

    // Bold High Intensity
    public static final String ANSI_BLACK_BOLD_BRIGHT = "\033[1;90m"; // BLACK
    public static final String ANSI_RED_BOLD_BRIGHT = "\033[1;91m";   // RED
    public static final String ANSI_GREEN_BOLD_BRIGHT = "\033[1;92m"; // GREEN
    public static final String ANSI_YELLOW_BOLD_BRIGHT = "\033[1;93m";// YELLOW
    public static final String ANSI_BLUE_BOLD_BRIGHT = "\033[1;94m";  // BLUE
    public static final String ANSI_PURPLE_BOLD_BRIGHT = "\033[1;95m";// PURPLE
    public static final String ANSI_CYAN_BOLD_BRIGHT = "\033[1;96m";  // CYAN
    public static final String ANSI_WHITE_BOLD_BRIGHT = "\033[1;97m"; // WHITE

    // High Intensity backgrounds
    public static final String ANSI_BLACK_BACKGROUND_BRIGHT = "\033[0;100m";// BLACK
    public static final String ANSI_RED_BACKGROUND_BRIGHT = "\033[0;101m";// RED
    public static final String ANSI_GREEN_BACKGROUND_BRIGHT = "\033[0;102m";// GREEN
    public static final String ANSI_YELLOW_BACKGROUND_BRIGHT = "\033[0;103m";// YELLOW
    public static final String ANSI_BLUE_BACKGROUND_BRIGHT = "\033[0;104m";// BLUE
    public static final String ANSI_PURPLE_BACKGROUND_BRIGHT = "\033[0;105m"; // PURPLE
    public static final String ANSI_CYAN_BACKGROUND_BRIGHT = "\033[0;106m";  // CYAN
    public static final String ANSI_WHITE_BACKGROUND_BRIGHT = "\033[0;107m";   // WHITE

    // for Google Doc dynamic range calculation
    public static final char[] englishAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
}