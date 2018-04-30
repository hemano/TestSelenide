package conversion.utils;

import conversion.setup.Constants;

import java.io.File;

/**
 * Created by heyto on 12/13/2017.
 */
public class SPHNX_WorkSheet {
    public enum RowType {
        TEST_CASE, TEST_STEP,
        BEGIN_LOOP, END_LOOP;
    }

    private String location;   // the path or URL to the runFile
    private String pageName;   // the name of the page containing the test scenarios
    private SPHNX_Row[] SPHNX_Rows;       // all of the SPHNX_Rows in the runfile

    public SPHNX_WorkSheet() {
        this.location = null;
        this.pageName = null;
        this.SPHNX_Rows = null;
    }

    public SPHNX_WorkSheet(String fileLocation) {
        this.location = fileLocation;
        this.pageName = null;
        this.SPHNX_Rows = null;
    }

    public String getLocation() {
        return location;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public int getRowlength() {
        return SPHNX_Rows.length;
    }

    public SPHNX_Row[] getSPHNX_Rows() {
        return this.SPHNX_Rows;
    }

    public void setSPHNX_Rows(int SPHNX_Rows) {
        this.SPHNX_Rows = new SPHNX_Row[SPHNX_Rows];
    }

    public void addRow(int index, SPHNX_Row SPHNX_Row) {
        this.SPHNX_Rows[index] = SPHNX_Row;
    }

    public boolean isLocal() {
        if (this.location.contains(File.separator) && this.location.toLowerCase().contains("xls")) {
            return true;
        } else if (this.location.toLowerCase().contains("xls")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isGoogleDoc() {
        if (this.location.toLowerCase().contains("docs.google.com")) {
            return true;
        } else {
            return false;
        }
    }

    public SPHNX_Row getRow(int i) {
        return SPHNX_Rows[i];
    }

    public static conversion.utils.SPHNX_WorkSheet.RowType getRowType(SPHNX_Row curRow) {
        conversion.utils.SPHNX_WorkSheet.RowType rowType = null;
        if (!curRow.get(Constants.S_TC_COL).equals("") &&
                curRow.getLastCell() == 2) {
            rowType = conversion.utils.SPHNX_WorkSheet.RowType.TEST_CASE;
        }
        if (curRow.get(Constants.S_TC_COL).equals("") &&
                !curRow.get(Constants.S_FUNC_COL).toLowerCase().equals("beginloop") &&
                !curRow.get(Constants.S_FUNC_COL).toLowerCase().equals("endloop")) {
            rowType = conversion.utils.SPHNX_WorkSheet.RowType.TEST_STEP;
        }
        if (curRow.get(Constants.S_TC_COL).equals("") &&
                curRow.get(Constants.S_FUNC_COL).toLowerCase().equals("beginloop")) {
            rowType = conversion.utils.SPHNX_WorkSheet.RowType.BEGIN_LOOP;
        }
        if (curRow.get(Constants.S_TC_COL).equals("") &&
                curRow.get(Constants.S_FUNC_COL).toLowerCase().equals("endloop")) {
            rowType = conversion.utils.SPHNX_WorkSheet.RowType.END_LOOP;
        }

        /*
        if (curRow.get(Constants.S_TC_COL).equals("") &&
                !curRow.get(Constants.S_AREA_COL).toLowerCase().equals("looping") &&
                curRow.getLastCell() >= 7) {
            rowType = SPHNX_WorkSheet.RowType.TEST_STEP;
        }
        if (curRow.get(Constants.S_TC_COL).equals("") &&
                curRow.get(Constants.S_FUNC_COL).toLowerCase().equals("beginloop") &&
                curRow.getLastCell() >= 8) {
            rowType = SPHNX_WorkSheet.RowType.BEGIN_LOOP;
        }
        if (curRow.get(Constants.S_TC_COL).equals("") &&
                curRow.get(Constants.S_FUNC_COL).toLowerCase().equals("endloop") &&
                curRow.getLastCell() == 7) {
            rowType = SPHNX_WorkSheet.RowType.END_LOOP;
        }
*/
        return rowType;
    }

    public static boolean toRun(String toRun) {
        if (toRun.toLowerCase().equals("y")) {
            return true;
        } else {
            return false;
        }
    }
}
