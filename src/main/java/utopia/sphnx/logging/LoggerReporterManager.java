package utopia.sphnx.logging;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import utopia.sphnx.config.ParseConfigurations;
import utopia.sphnx.reports.Report;

import static utopia.sphnx.logging.LoggerReporter.LOGNREPORT;

/**
 * Created by heyto on 6/16/2017.
 */

@Configuration
public class LoggerReporterManager {

    private static Logger sphnxLog;

    private Class<?> classLog;
    
    private LoggerContext lc;

    FileAppender <ILoggingEvent> fileAppender;

    ConsoleAppender<ILoggingEvent> consoleAppender;

    public LoggerReporterManager() {
        sphnxLog = null;
    }

    private void initialiseLogger(String className){
        if (lc == null) {
            lc = (LoggerContext) LoggerFactory.getILoggerFactory();

            if(fileAppender == null) {
                PatternLayoutEncoder ple = new PatternLayoutEncoder();

                ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
                ple.setContext(lc);
                ple.start();
                fileAppender = new FileAppender <ILoggingEvent>();
                ParseConfigurations configurations = new ParseConfigurations();
                String logFile = configurations.getAllConfigurations().get(ParseConfigurations.Configs.LOGGER_PATH.name());
                fileAppender.setFile(logFile);
                fileAppender.setEncoder(ple);
                fileAppender.setContext(lc);
                fileAppender.setName("timestamp");
                fileAppender.start();
            }
            if (consoleAppender == null) {
                PatternLayoutEncoder ple = new PatternLayoutEncoder();

                ple.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
                ple.setContext(lc);
                ple.start();
                consoleAppender = new ConsoleAppender <>();
                consoleAppender.setContext(lc);
                consoleAppender.setEncoder(ple);
                consoleAppender.setName("STDOUT");
                consoleAppender.start();
            }
            if (sphnxLog == null) {
                sphnxLog = lc.getLogger(className);

                sphnxLog.addAppender(fileAppender);
                sphnxLog.addAppender(consoleAppender);

                sphnxLog.setLevel(Level.DEBUG);
                StatusPrinter.print(lc);
                sphnxLog.setAdditive(false); /* set to true if root should log too */
            }
        }
    }
    
    public LoggerReporterManager(String className) {
        
        
    }

    public void sphnxDebug(String className, String message) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        sphnxLog.debug(message);
    }

    public void sphnxDebug(String className, String message, String color) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        String colorMessage = getColorMessage(message, color);
        sphnxLog.debug(colorMessage);
    }

    public void sphnxDebug(String className, String message, String color, int brightness) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        String colorMessage = getColorMessage(message, color, brightness);
        sphnxLog.debug(colorMessage);
    }

    /**
     * sphnxInfo method is used to add info logs
     *
     * @param className Class Name
     * @param message   Info messages to add in the logs
     */
    public void sphnxInfo(String className, String message) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        sphnxLog.info(message);
    }

    public void sphnxInfo(String className, String message, boolean toReport) {
        sphnxInfo(className,message);
        reportLog(message, toReport);
    }

    private void reportLog(String message, boolean toReport) {
        if(toReport) {
            Report.log(message);
        }
    }

    public void sphnxInfo(String className, String message, String color) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        String colorMessage = getColorMessage(message, color);
        sphnxLog.info(colorMessage);
    }

    public void sphnxInfo(String className, String message, String color, boolean toReport) {
        sphnxInfo(className,message,color);
        reportLog(message, toReport);
    }

    public void sphnxInfo(String className, String message, String color, int brightness) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        String colorMessage = getColorMessage(message, color, brightness);
        sphnxLog.info(colorMessage);
    }

    public void sphnxInfo(String className, String message, String color, int brightness, boolean toReport) {
        sphnxInfo(className,message,color,brightness);
        reportLog(message, toReport);
    }

    public void sphnxError(String className, String message) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        String colorMessage = getColorMessage(message, "red");
        sphnxLog.error(colorMessage);
    }

    public void sphnxError(String className, String message, boolean toReport) {
        sphnxError(className,message);
        reportFailure(message, toReport);
    }

    private void reportFailure(String message, boolean toReport) {
        if(toReport) {
            Report.fail("ERROR: " + message);
        }
    }

    public void sphnxError(String className, String message, String color) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        String colorMessage = getColorMessage(message, color);
        sphnxLog.error(colorMessage);

    }

    public void sphnxError(String className, String message, String color, boolean toReport) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        String colorMessage = getColorMessage(message, color);
        sphnxLog.error(colorMessage);
        reportFailure(message, toReport);
    }

    public void sphnxError(String className, String message, String color, int brightness) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        String colorMessage = getColorMessage(message, color, brightness);
        sphnxLog.error(colorMessage);

    }

    public void sphnxError(String className, String message, String color, int brightness, boolean toReport) {
        sphnxError(className,message,color,brightness);
        reportFailure(message, toReport);
    }

    /**
     * sphnxPASS method use to add logs when execution pass successfully
     *
     * @param className Class Name
     * @param message   Pass Message
     */
    public void sphnxPASS(String className, String message) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        String colorMessage = getColorMessage(message, "green");
        sphnxLog.info("PASSED: " + colorMessage);
    }

    public void sphnxPASS(String className, String message, boolean toReport) {
        sphnxPASS(className,message);
        reportSuccess(message, toReport);
    }

    private void reportSuccess(String message, boolean toReport) {
        if(toReport) {
            Report.pass("PASSED: " + message);
        }
    }

    public void sphnxPASS(String className, String message, String color) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        String colorMessage = getColorMessage(message, color);
        sphnxLog.info("PASSED: " + colorMessage);
    }

    public void sphnxPASS(String className, String message, String color, boolean toReport) {
        sphnxPASS(className,message,color);
        reportSuccess(message, toReport);
    }

    public void sphnxPASS(String className, String message, String color, int brightness) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        String colorMessage = getColorMessage(message, color, brightness);
        sphnxLog.info("PASSED: " + colorMessage);
    }

    public void sphnxPASS(String className, String message, String color, int brightness, boolean toReport) {
        sphnxPASS(className,message,color,brightness);
        reportSuccess(message, toReport);
    }

    /**
     * sphnxFAIL method use to add error logs when execution fails
     *
     * @param className Class Name
     * @param message   Fail message
     */
    public void sphnxFAIL(String className, String message) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        String colorMessage = getColorMessage(message, "red");
        sphnxLog.error("FAILED: " + colorMessage);
    }

    public void sphnxFAIL(String className, String message, boolean toReport) {
        sphnxFAIL(className,message);
        reportFailure(message,toReport);
    }

    public void sphnxFAIL(String className, String message, String color) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        String colorMessage = getColorMessage(message, color);
        sphnxLog.error("FAILED: " + colorMessage);
    }


    public void sphnxFAIL(String className, String message, String color, boolean toReport) {
        sphnxFAIL(className,message,color);
        reportFailure(message,toReport);
    }

    public void sphnxFAIL(String className, String message, String color, int brightness) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        String colorMessage = getColorMessage(message, color, brightness);
        sphnxLog.error(colorMessage);
    }

    public void sphnxFAIL(String className, String message, String color, int brightness, boolean toReport) {
        sphnxFAIL(className,message,color,brightness);
        reportFailure(message,toReport);
    }

    /**
     * sphnxFWarning method use to add warning logs when execution fails
     *
     * @param className Class Name
     * @param message   Warning message
     */
    public void sphnxWarning(String className, String message) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        String colorMessage = getColorMessage(message, "yellow");
        sphnxLog.warn(colorMessage);
    }

    public void sphnxWarning(String className, String message, boolean toReport) {
        sphnxWarning(className,message);
        reportInfo(message, toReport);
    }

    private void reportInfo(String message, boolean toReport) {
        if(toReport) {
            Report.info("WARNING: " + message);
        }
    }

    public void sphnxWarning(String className, String message, String color) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        String colorMessage = getColorMessage(message, color);
        sphnxLog.warn(colorMessage);
    }

    public void sphnxWarning(String className, String message, String color, boolean toReport) {
        sphnxWarning(className,message,color);
        reportInfo(message, toReport);
    }

    public void sphnxWarning(String className, String message, String color, int brightness) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        String colorMessage = getColorMessage(message, color, brightness);
        sphnxLog.warn(colorMessage);
    }

    public void sphnxWarning(String className, String message, String color, int brightness, boolean toReport) {
        sphnxWarning(className,message,color,brightness);
        reportInfo(message, toReport);
    }

    /**
     * sphnxSkip method use to add warning logs when execution fails
     *
     * @param className Class Name
     * @param message   Skip message
     */
    public void sphnxSkip(String className, String message) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        String colorMessage = getColorMessage(message, "yellow");
        sphnxLog.warn(colorMessage);
    }

    public void sphnxSkip(String className, String message, boolean toReport) {
        sphnxSkip(className,message);
        reportSkip(message, toReport);
    }

    private void reportSkip(String message, boolean toReport) {
        if(toReport) {
            Report.skip("SKIPPED: " + message);
        }
    }

    public void sphnxSkip(String className, String message, String color) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        String colorMessage = getColorMessage(message, color);
        sphnxLog.warn(colorMessage);
    }

    public void sphnxSkip(String className, String message, String color, boolean toReport) {
        sphnxSkip(className,message,color);
        reportSkip(message, toReport);
    }

    public void sphnxSkip(String className, String message, String color, int brightness) {
        if (sphnxLog == null) {
            initialiseLogger(className);
        }
        String colorMessage = getColorMessage(message, color, brightness);
        sphnxLog.warn(colorMessage);
    }

    public void sphnxSkip(String className, String message, String color, int brightness, boolean toReport) {
        sphnxSkip(className,message,color,brightness);
        reportSkip(message, toReport);
    }

    public void setClassName(String className) {
        sphnxLog = (Logger) LoggerFactory.getLogger(className);
    }

    public Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            String message = "Class with name :" + className + " not found.";
            sphnxLog = (Logger  ) LoggerFactory.getLogger(utopia.sphnx.logging.LoggerReporterManager.class.getCanonicalName());
            String colorMessage =
                    "\u001b["                   // Prefix
                            + "0"                       // Brightness
                            + ";"                       // Separator
                            + "31"                      // Red foreground
                            + "m"                       // Suffix
                            + message                   // the text to output
                            + "\u001b[m ";
            sphnxLog.error(colorMessage);
            e.printStackTrace();
            return null;
        }
    }

    public static utopia.sphnx.logging.LoggerReporterManager getLogger() {
        return LOGNREPORT;
    }

    public Logger getSPHNXLoger() {
        return sphnxLog;
    }

    private String getMethodName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // TODO get correct index of calling method!
        StackTraceElement element = stackTrace[3];
        return element.getMethodName();
    }

    private String getColorMessage(String message, String color) {

        String ansciiColor = getAnsciiForegroundColorNumber(color);

        return "\u001b["                    // Prefix
                + "0"                       // Brightness
                + ";"                       // Separator
                + ansciiColor               // foreground color specified
                + "m"                       // Suffix
                + message                   // the text to output
                + "\u001b[m ";
    }

    private String getColorMessage(String message, String color, int brightness) {

        String ansciiColor = getAnsciiForegroundColorNumber(color);

        return "\u001b["                    // Prefix
                + brightness                // Brightness
                + ";"                       // Separator
                + ansciiColor               // foreground color specified
                + "m"                       // Suffix
                + message                   // the text to output
                + "\u001b[m ";
    }

    private String getAnsciiForegroundColorNumber(String color) {
        switch (color.toLowerCase()) {
            case "black":
                return "30";
            case "red":
                return "31";
            case "green":
                return "32";
            case "yellow":
                return "33";
            case "blue":
                return "34";
            case "purple":
                return "35";
            case "cyan":
                return "36";
            case "white":
                return "37";
            default:
                return "0";    //default is reset to default
        }
    }
    private String getAnsciiBackgroundColorNumber(String color) {
        switch (color.toLowerCase()) {
            case "black":
                return "40";
            case "red":
                return "41";
            case "green":
                return "42";
            case "yellow":
                return "43";
            case "blue":
                return "44";
            case "purple":
                return "45";
            case "cyan":
                return "46";
            case "white":
                return "47";
            default:
                return "47";    //default background is white
        }
    }
}