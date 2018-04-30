package conversion.utils;

/**
 * Created by heyto on 12/13/2017.
 */
public class SPHNX_Row {
    private String[] values;
    private int rowNum;

    public SPHNX_Row(int num, int size) {
        this.rowNum = num;
        this.values = new String[size];
    }

    public String get(int index) {
        return this.values[index];
    }

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public void add(int index, String value) {
        this.values[index] = value;
    }

    public int getLastCell() {
        return this.values.length;
    }

    public boolean hasLocalPath(int index) {
        if(this.values[index].toLowerCase().contains("xls")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasGoogleDocURL(int index) {
        if (this.values[index].toLowerCase().contains("docs.google.com")) {
            return true;
        } else {
            return false;
        }
    }
}
