package utopia.sphnx.logging;

import java.time.LocalDateTime;

/**
 * Created by heyto on 6/16/2017.
 */
public class LogMessage {

    private LocalDateTime date;
    private String messageID;
    private String messageParentID;
    private String testRunID;
    private String testRunParentStepID;
    private String function;
    private String metaData;
    private String logMessage;
    private String logInfo;

    public LogMessage() {
        this.messageID = null;
        this.messageParentID = null;
        this.testRunID = null;
        this.testRunParentStepID = null;
        this.function = null;
        this.metaData = null;
        this.logMessage = null;

        this.date = LocalDateTime.now();
    }

    public LogMessage(String method, String message) {
        this.date = LocalDateTime.now();
        this.setLogInfo(method, message);

        this.logMessage = function + ":\t" + method + "\t---\t" + message;
    }


    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getMessageParentID() {
        return messageParentID;
    }

    public void setMessageParentID(String messageParentID) {
        this.messageParentID = messageParentID;
    }

    public String getTestRunID() {
        return testRunID;
    }

    public void setTestRunID(String testRunID) {
        this.testRunID = testRunID;
    }

    public String getTestRunParentStepID() {
        return testRunParentStepID;
    }

    public void setTestRunParentStepID(String testRunParentStepID) {
        this.testRunParentStepID = testRunParentStepID;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    private String getLogInfo() {
        return logInfo;
    }

    private void setLogInfo(String method, String message) {
        this.logInfo = "\t" + method + " " + message ;
    }
}
