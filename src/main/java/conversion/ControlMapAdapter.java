package conversion;

import conversion.setup.Configurations;
import conversion.setup.Variables;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import utopia.sphnx.config.ParseConfigurations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

import static conversion.setup.Variables.FILENAME;
import static utopia.sphnx.logging.LoggerReporter.LOGNREPORT;

/**
 * Created by heyto on 5/11/2017.
 */
public class ControlMapAdapter {

    private final String CONTROLMAPADAPTER = this.getClass().getCanonicalName();

    private Map<String, String> configurations;
    private Path mMapFile;

    /* XML Variables */
    private Element objXMLRoot;
    private Element objControlMap;
    private Element objControl;
    private Document oXML;

    /* Excel Control Map Constants */
    private final int I_COL = 0;
    private final int C_COL = 1;
    private final int LN_COL = 2;
    private final int P_COL = 3;
    private final int OT_COL = 4;
    private final int D_COL = 5;

    private String mControlMapName; // holds the name of the control map.
    private String conrolMapXMLFolder;


    public ControlMapAdapter() {
        this.configurations = new ParseConfigurations().getAllConfigurations();
        this.mMapFile = Configurations.UI_DEFINITION_FILE;

        /* Create in-memory XML doc to hold test variables */
        this.oXML = DocumentHelper.createDocument();
        this.objXMLRoot = oXML.addElement("ControlMap");
    }

    public ControlMapAdapter(String controlMapXMLFolder) {
        LOGNREPORT.sphnxInfo(CONTROLMAPADAPTER,"Starting Control Map reading...");
        this.configurations = new ParseConfigurations().getAllConfigurations();
        this.mMapFile = Paths.get(configurations.get(ParseConfigurations.Configs.UIMAP_FILE.name()));
        /* Create in-memory XML doc to hold test variables */
        this.oXML = DocumentHelper.createDocument();
        this.objXMLRoot = oXML.addElement("ControlMap");
        this.conrolMapXMLFolder = controlMapXMLFolder;
    }

    /**************************************************************************
     * Name:		Load
     *
     * Description:	Loads test variables based on driver type
     * 				(currently the only custom driver type supported is Excel)
     *
     *
     * Params:	None
     *
     * Return Value:	iLoadStatus indicates the success or failure of the function
     *
     * Author:		Utopia Solutions
     *
     * Change log:
     * 	Date		Who			What
     * 	02/01/17	Lee Barnes	Initial Creation
     *  05/02/17    Jeff Heytow Converted to Java code
     ***************************************************************************/
    public String load() throws IOException{
        boolean loadStatus = loadMapFromExcel(this.mMapFile);
        return this.saveTestDefinitionFile(this.conrolMapXMLFolder);
        // for debugging
        //prettyPrintXML();
    }

    /*************************************************************************
     * Name:		LoadMapFromExcel
     *
     * Description:
     *
     *
     * Params:	IN	mapPath - This is the name of the excel map
     * 				sheet that lists the datagen to be stored
     *
     * Return Value:	iLoadStatus indicates the success or failure of the function
     *
     * Author:		Utopia Solutions
     *
     * Change log:
     * 	Date		Who			What
     *  05/02/2017  Jeff Heytow initial creation
     ***************************************************************************/
    private boolean loadMapFromExcel(Path mapPath) throws IOException {
        boolean loadStatus = true;

        try {
            FileInputStream excelFile = new FileInputStream(String.valueOf(mapPath));
            Workbook workbook = new XSSFWorkbook(excelFile);
            this.mControlMapName = workbook.getSheetName(0);
            // add test run to in-memory XML doc:
            addControlMap(this.mControlMapName);

            Sheet typeSheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = typeSheet.iterator();

            while (rowIterator.hasNext()) {
                Row curRow = rowIterator.next();
                if (curRow.getRowNum() == 0) {
                    continue;
                }

                String context = curRow.getCell(C_COL).getStringCellValue();
                String lName = curRow.getCell(LN_COL).getStringCellValue();
                String cName = (context + ":" + lName);
                addControl(cName);
                // TODO - check for null required fields!
                addControlProperty("Interface", curRow.getCell(I_COL), true);
                addControlProperty("Context", curRow.getCell(C_COL), true);
                addControlProperty("LogicalName", curRow.getCell(LN_COL), true);
                addControlProperty("Parent", curRow.getCell(P_COL), false);
                addControlProperty("ObjectType", curRow.getCell(OT_COL), true);
                addControlProperty("Descriptor", curRow.getCell(D_COL), true);

                if(lName.equalsIgnoreCase("loader")) {
                    Variables.hasLoader = true;
                    Variables.LOADER = context + "." + lName;
                }
            }

        } catch (IOException e) {
            LOGNREPORT.sphnxError(CONTROLMAPADAPTER, "File " + FILENAME + " not found!");
            LOGNREPORT.sphnxError(CONTROLMAPADAPTER,"Exception in parsing scenario file. Exception: " + e.getLocalizedMessage());
            loadStatus = false;
            throw e;
        }
        return loadStatus;
    }

    /**************************************************************************
     * Name:		AddTestRun
     *
     * Description:
     *
     *
     * Params:	IN	TestRunID
     *
     * Return Value:	N/A
     *
     * Author:		Utopia Solutions
     *
     * Change log:
     * 	Date		Who			What
     *  05/03/2017  Jeff Heytow initial creation
     ***************************************************************************/
    private void addControlMap(String controlMapName) {
        this.objControlMap = this.objXMLRoot.addElement("ExecutionMap")
                .addAttribute("Name", mControlMapName);

    }

    private void addControl(String controlName) {
        this.objControl = this.objControlMap.addElement("Control")
                .addAttribute("Control", controlName);
    }

    private void addControlProperty(String propName, String value) {
        this.objControl.addElement(propName)
                .addText(value);
    }

    private void addControlProperty(String propName, Cell cell, boolean isRequired) {
        if(cell == null) {
            if(isRequired) {
                System.out.println(propName + " is a required input! Please check " + mControlMapName + " datagen sheet.");
            }
        } else {
            this.objControl.addElement(propName).addText(cell.getStringCellValue());
        }

    }

    /*************************************************************************
    * Name:		SaveTestDefinitionToFile
    *
    * Description:
    *
    *
    * Params:	IN	FilePath - folder in which to save the file
    *
    *
    * Return Value:	N/A
    *
    * Author:		Utopia Solutions
    *
    * Change log:
    * 	Date		Who			What
    * 	mm/dd/yyyy	name		initial creation
    *
* **************************************************************************/
    private String saveTestDefinitionFile(String controlMapFilePath) throws IOException {
        String controlMapXMLFile="";
        try {
            controlMapXMLFile = controlMapFilePath + File.separatorChar + mControlMapName + ".xml";
            FileOutputStream xmlFile = new FileOutputStream(controlMapXMLFile);
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter(xmlFile, format);
            writer.write(this.oXML);
            writer.flush();
            return controlMapXMLFile;
        } catch (IOException e) {
            LOGNREPORT.sphnxError(CONTROLMAPADAPTER, "File " + controlMapXMLFile + "not found!");
            LOGNREPORT.sphnxError(CONTROLMAPADAPTER,"Error in saving test definition. Exception:" + e.getLocalizedMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**************************************************************************
     * Name:		prettyPrintXML
     *
     * Description: a method to print out the XML doc to the console,
     *              in a readable format, for debugging purposes only.
     *
     * Author:		Utopia Solutions
     *
     * Change log:
     * 	Date		Who			What
     * 	05/03/2017	Jeff Heytow		initial creation
     ***************************************************************************/
    private void prettyPrintXML() {
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer;
        try {
            System.out.println();
            writer = new XMLWriter(System.out, format);
            writer.write(this.oXML);
            System.out.println();
        } catch (IOException e) {
            LOGNREPORT.sphnxError(CONTROLMAPADAPTER,"Error in printing xml. Exception:" + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}

