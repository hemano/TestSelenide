package utopia.sphnx.actions;

import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;
import org.testng.Reporter;

import java.util.HashMap;
import java.util.Map;

import static utopia.sphnx.logging.LoggerReporter.LOGNREPORT;

/**
 * Created by jitendrajogeshwar on 31/05/17.
 */
public abstract class BaseActions extends AbstractAction implements ActionsController {

    private static final String BASE_ACTIONS = utopia.sphnx.actions.BaseActions.class.getCanonicalName();

    public BaseActions(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    public BaseActions(WebDriver webDriver) {
        super(webDriver);
    }

    /**
     * uses Report to log the screenshot of a failed log entry (error(...)) to
     * the logs
     *
     * @param file
     *            file must exist
     */
    /**
     * Info.
     *
     * @param message
     *            the message
     */
    public void info(String message) {
//        WEB_CONTROLLER_BASE_LOGR_BASE_LOG.info(message);
        Reporter.log("<p class=\"testOutput\" style=\"font-size:1em;\">" + message + "</p>");
        LOGNREPORT.sphnxInfo(BASE_ACTIONS, "\"<p class=\\\"testOutput\\\" style=\\\"font-size:1em;\\\">\" + message + \"</p>\"");
    }

    /**
     * Warn.
     *
     * @param message
     *            the message
     */
    public void warn(String message) {
//        WEB_CONTROLLER_BASE_LOG.warn(message);
        Reporter.log("<p class=\"testOutput\" style=\"color:orange; font-size:1em;\">" + message + "</p>");
        LOGNREPORT.sphnxError(BASE_ACTIONS, "<p class=\"testOutput\" style=\"color:orange; font-size:1em;\">" + message + "</p>");
    }

    /**
     * Error.
     *
     * @param message
     *            the message
     */
    public void error(String message) {
//        WEB_CONTROLLER_BASE_LOG.error(message);
        Reporter.log("<p class=\"testOutput\" style=\"color:red; font-size:1em;\">" + message + "</p>");
        LOGNREPORT.sphnxError(BASE_ACTIONS, "<p class=\"testOutput\" style=\"color:red; font-size:1em;\">" + message + "</p>" );
    }


    /*
     * (non-Javadoc)
     *
     *
     * (java.lang.String, int)
     */
    @Override
    public Map<String, Map<String, String>> getTableInfo(String locator, int numberOfColumns) {
        Map<String, Map<String, String>> tableData = new HashMap<String, Map<String, String>>();
        Map<String, String> tableColumns = null;
        waitForElement(locator);
        int rowNumber = 1;
        for (int counter = 1; counter <= getNumberOfTotalRows(locator); counter++) {
            tableColumns = new HashMap<String, String>();
            for (int columns = 1; columns <= numberOfColumns; columns++) {
                if (locator.startsWith("css")) {
                    tableColumns.put("column_" + Integer.toString(columns), getText(locator + " *:nth-child(" + counter + ") *:nth-child(" + columns + ")"));
                } else if (locator.startsWith("//") || locator.startsWith("xpath")) {
                    tableColumns.put("column_" + Integer.toString(columns), getText(locator + "//*[" + counter + "]//*[" + columns + "]"));
                } else {
                    tableColumns.put("column_" + Integer.toString(columns), getText("css=#" + locator + " *:nth-child(" + counter + ") *:nth-child(" + columns + ")"));

                }
            }
            tableData.put("row_" + Integer.toString(rowNumber), tableColumns);
            rowNumber = rowNumber + 1;
        }

        return tableData;
    }
}