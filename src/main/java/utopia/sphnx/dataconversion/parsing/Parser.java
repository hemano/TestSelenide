package utopia.sphnx.dataconversion.parsing;


import org.apache.commons.lang3.StringUtils;
import utopia.sphnx.dataconversion.datagen.execution.ExecutionContext;
import utopia.sphnx.dataconversion.datagen.keyword.Keyword;
import utopia.sphnx.dataconversion.datagen.metadata.MetaData;

import static utopia.sphnx.dataconversion.datagen.configuration.Configurations.*;
import static utopia.sphnx.logging.LoggerReporter.LOGNREPORT;


/**
 * TODO describe class
 * Created by heyto on 5/17/2017.
 */
public class Parser {
    private static final String PARSER = utopia.sphnx.dataconversion.parsing.Parser.class.getCanonicalName();

    public Parser() {

    }

    public void setStaticDelimiters(String left, String right) {
        KWMD_PREFIX = left;
        KWMD_SUFFIX = right;
    }


    public void setDynamicDelimiters(String left, String right) {
        EC_PREFIX = left;
        EC_SUFFIX = right;
    }

    public String replaceDataReferenceInString(String input, Boolean toLog) {
        boolean isInputValid = true;

        String retVal = input;
        String dataReference = "";
        String metaData = "";
        String keyword = "";
        String executionContext = "";
        String ERROR = "";
        String inputValue = retVal;
        boolean isDirty = false;

        //LOGNREPORT.sphnxInfo(PARSER, "Parser input value: " + retVal);
        // Enter EC Logic
        int lDelim = StringUtils.countMatches(retVal, EC_PREFIX);
        int rDelim = StringUtils.countMatches(retVal, EC_SUFFIX);

        // make sure the left delimiter and the right delimiter are the same:
        if (lDelim != rDelim) {
            ERROR = "Prefix and Suffix counts do not match.";
            isInputValid = false;
        }
        // make sure there are the same amount of left and right delimiters
        if (EC_PREFIX.equals(EC_SUFFIX)) {
            int modCount = StringUtils.countMatches(retVal, EC_PREFIX);
            if ((modCount % 2) != 0) {
                ERROR = "Beginning Tag and End Tag count does not match.";
                isInputValid = false;
            }
        }
        if (isInputValid) {
            try {
                while (retVal.contains(EC_PREFIX)) {
                    // getting first parameter from parameter input String:
                    dataReference = retVal.substring(retVal.indexOf(EC_PREFIX) + EC_PREFIX.length(), retVal.indexOf(EC_SUFFIX, retVal.indexOf(EC_PREFIX) + EC_PREFIX.length()));
                    // replacing dataReference with actual value:
                    executionContext = (String) ExecutionContext.getECValue(dataReference.toUpperCase());

                    retVal = retVal.replace((EC_PREFIX + dataReference + EC_SUFFIX), executionContext);
                    isDirty = true;
                }
            } catch (Exception e) {
                LOGNREPORT.sphnxInfo(PARSER, e.getMessage());
                throw new NullPointerException(e.getMessage() + "... Please Check input.");
            }
        } else {
            LOGNREPORT.sphnxInfo(PARSER, ERROR);
            throw new NullPointerException(ERROR + " Please Check input.");
        }

        // Enter Keyword/MetaData logic:
        lDelim = StringUtils.countMatches(retVal, KWMD_PREFIX);
        rDelim = StringUtils.countMatches(retVal, KWMD_SUFFIX);
        isInputValid = true;

        // make sure the left delimiter and the right delimiter are the same:
        if (lDelim != rDelim && !retVal.toLowerCase().contains("css")) {
            ERROR = "Prefix and Suffix counts do not match.";
            isInputValid = false;
        }
        // make sure there are the same amount of left and right delimiters
        if (KWMD_PREFIX.equals(KWMD_SUFFIX)) {
            int modCount = StringUtils.countMatches(retVal, KWMD_PREFIX);
            if ((modCount % 2) != 0) {
                ERROR = "Beginning Tag and End Tag count does not match.";
                isInputValid = false;
            }
        }
        /* this is a sample <TAGNAME> input string from utopia.sphnx.core.dataconversion.datagen sheet. */
        if (isInputValid) {
            try {
                while (retVal.contains(KWMD_PREFIX)) {
                    // getting first parameter from parameter input String:
                    retVal = getDataSource(retVal, toLog);

                    // check return value for EC
                    if(retVal.contains(EC_PREFIX) && retVal.contains(EC_SUFFIX)) {
                        retVal = getEC(retVal);
                    }
                }
            } catch (Exception e) {
                throw new NullPointerException("Error utopia.sphnx.core.dataconversion.parsing Data Reference... Please Check input.");
            }
        } else {
            LOGNREPORT.sphnxError(PARSER, ERROR);
            throw new NullPointerException(ERROR + " Please Check input.");
        }
        if(isDirty) {
            LOGNREPORT.sphnxInfo(PARSER, "Parser converted '" + inputValue + "' to '" + retVal + "'");
        }
        return retVal;
    }

    private String getEC(String retVal) {
        String ECKey = retVal.substring(retVal.indexOf(EC_PREFIX) + EC_PREFIX.length(), retVal.indexOf(EC_SUFFIX, retVal.indexOf(EC_PREFIX) + EC_PREFIX.length()));
        String ECVal = (String) ExecutionContext.getECValue(ECKey.toUpperCase());
        retVal = retVal.replace((EC_PREFIX + ECKey + EC_SUFFIX), ECVal);
        return retVal;
    }

    private String getDataSource(String retVal, Boolean toLog) {
        String dataReference;
        String metaData;
        String keyword;
        String inputValue = retVal;

        dataReference = retVal.substring(retVal.indexOf(KWMD_PREFIX) + KWMD_PREFIX.length(), retVal.indexOf(KWMD_SUFFIX, retVal.indexOf(KWMD_PREFIX) + KWMD_PREFIX.length()));
        // replacing dataReference with actual value:
        // check for MetaData:
        metaData = MetaData.getMetaData(dataReference);
        if (metaData == null) {
            keyword = Keyword.getKeyword(dataReference);
            retVal = retVal.replace((KWMD_PREFIX + dataReference + KWMD_SUFFIX), keyword);
            if(toLog) {
                LOGNREPORT.sphnxInfo(PARSER, "Keyword was converted from '" + inputValue + "' to '" + retVal + "'");
            }
        } else {
            retVal = retVal.replace((KWMD_PREFIX + dataReference + KWMD_SUFFIX), metaData);
            if(toLog) {
                LOGNREPORT.sphnxInfo(PARSER, "METADATA was converted from '" + inputValue + "' to '" + retVal + "'");
            }
        }
        return retVal;
    }
}
