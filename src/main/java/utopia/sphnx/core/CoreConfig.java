package utopia.sphnx.core;

import conversion.ControlMapAdapter;
import conversion.TestDefinitionAdapter;
import conversion.setup.Configurations;
import conversion.setup.Variables;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import utopia.sphnx.config.ParseConfigurations;
import utopia.sphnx.core.support.xmlmapping.controlmap.Control;
import utopia.sphnx.core.support.xmlmapping.controlmap.ControlMap;
import utopia.sphnx.core.support.xmlmapping.testcases.Execution;
import utopia.sphnx.core.support.xmlmapping.testcases.Property;
import utopia.sphnx.core.support.xmlmapping.testcases.TestCase;
import utopia.sphnx.core.support.xmlmapping.testcases.TestScenario;
import utopia.sphnx.dataconversion.datagen.execution.ExecutionContext;
import utopia.sphnx.dataconversion.datagen.keyword.Keyword;
import utopia.sphnx.dataconversion.datagen.metadata.MetaData;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static utopia.sphnx.logging.LoggerReporter.LOGNREPORT;


/**
 * Created by jitendrajogeshwar on 10/05/17.
 */
@Configuration
@Component
public class CoreConfig {

    private static String CORE_CONFIG = utopia.sphnx.core.CoreConfig.class.getCanonicalName();

    private List<String> executionMode = new LinkedList <>();
    private List<TestScenario> testScenarios = new LinkedList <>();

    @Autowired
    private ApplicationContext applicationContext;



    @Bean(name = "controlMap")
    public List<Control> getControlMapXMLFile() throws IOException, JAXBException {
        Map<String, String> configurations = allConfigurations();
        String controlMapFile = configurations.get(ParseConfigurations.Configs.UIMAP_FILE.name());
        
        Configurations.UI_DEFINITION_FILE = Paths.get(controlMapFile);
        LOGNREPORT.sphnxInfo(this.getClass().getCanonicalName(), "Found UI Definition File at: " + Paths.get(controlMapFile));
        ControlMapAdapter controlMapAdapter = new ControlMapAdapter(Paths.get(controlMapFile).getParent().toString());
        String controlMapXMLFile = controlMapAdapter.load();
        LOGNREPORT.sphnxInfo(CORE_CONFIG, "Control Map loaded.");
        JAXBElement<ControlMap> et = null;
        try {
            //
            JAXBContext jc = JAXBContext.newInstance(ControlMap.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            File xmlFile = new File(controlMapXMLFile);
            FileInputStream fileInputStream = new FileInputStream(xmlFile);
            Source source = new StreamSource(fileInputStream);
            et = unmarshaller.unmarshal(source, ControlMap.class);
            List<Control> controls = et.getValue().getExecutionMap().getControl();
            LOGNREPORT.sphnxInfo(CORE_CONFIG, "Control Map loaded.");
            return controls;
        } catch (Exception e) {
            LOGNREPORT.sphnxError(CORE_CONFIG, "Control Map not loaded: Exception" + e.getLocalizedMessage());
            throw e;
        }
    }


    @Bean(name = "test-cases")
    @DependsOn("setExecutionContext")
    public List<TestCase> testCases() throws IOException, JAXBException {
        Map<String,String> configurations = allConfigurations();
        String runFile = configurations.get(ParseConfigurations.Configs.RUN_FILE.name());
        Path RUN_FILE = Paths.get(runFile);
        LOGNREPORT.sphnxInfo(CORE_CONFIG, "Run File found at: " + Paths.get(runFile));
        String scenariosFolder = configurations.get(ParseConfigurations.Configs.SCENARIOS_DIR.name());
        Path SCENARIOS = Paths.get(scenariosFolder);
        LOGNREPORT.sphnxInfo(CORE_CONFIG, "Scenarios File path found at: " + Paths.get(scenariosFolder));
        String resources  =  configurations.get(ParseConfigurations.Configs.RESOURCES_DIR.name());
        Configurations.LOOP_DATA = Paths.get(resources);
        LOGNREPORT.sphnxInfo(CORE_CONFIG, "Loop Data File path found at: " + Paths.get(resources));
        String runXMLFolder = configurations.get(ParseConfigurations.Configs.ARTIFACTS_DIR.name());

        if(Files.notExists(Paths.get(runXMLFolder))){
            try {
                Files.createDirectories(Paths.get(runXMLFolder));
            } catch (IOException e) {
                LOGNREPORT.sphnxError(this.getClass().getCanonicalName(), "Exception details: " + ExceptionUtils.getStackTrace(e.fillInStackTrace()));
                throw e;
            }
        }

        String xmlFilePath = null;
        TestDefinitionAdapter testDefinitionAdapter = null;

        try {
            testDefinitionAdapter = new TestDefinitionAdapter(RUN_FILE.toString(),
                    SCENARIOS.toString(),
                    runXMLFolder);
            LOGNREPORT.sphnxInfo(CORE_CONFIG, "Loading Test Definition Adapter.");
            xmlFilePath = testDefinitionAdapter.saveTestXML();
        } catch (IllegalArgumentException e1){
            LOGNREPORT.sphnxError(CORE_CONFIG, "Error parsing excel:" + e1.getLocalizedMessage());
            throw e1;
        }
        catch (Exception e) {
            LOGNREPORT.sphnxFAIL(CORE_CONFIG, "Error Creating Test Definition Adapter on row " + testDefinitionAdapter.getRow());
            throw new IllegalArgumentException("Throwing illegal exception instead of origin exception: " + e.getClass() + "Exception message: " + e.getLocalizedMessage());
        }
        LOGNREPORT.sphnxInfo(CORE_CONFIG, "Test Definition File saved at: " + xmlFilePath);

        List<TestCase> enabledTestCases = new LinkedList<>();

        JAXBElement<Execution> et = null;
        try {
            JAXBContext jc = JAXBContext.newInstance(Execution.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            File xmlFile = new File(xmlFilePath);
            FileInputStream fileInputStream = new FileInputStream(xmlFile);
            Source source = new StreamSource(fileInputStream);
            et = unmarshaller.unmarshal(source, Execution.class);
        } catch (IllegalArgumentException e1){
            LOGNREPORT.sphnxError(CORE_CONFIG, "Error parsing excel:" + e1.getLocalizedMessage());
            throw e1;
        } catch (Exception e) {
            if (Variables.hasEmptyCell) {
                LOGNREPORT.sphnxError(CORE_CONFIG, "Required field(s) were blank in test scanario!");
                throw new IllegalArgumentException(e.getLocalizedMessage());
            } else {
                LOGNREPORT.sphnxError(CORE_CONFIG, "File not found at: " + xmlFilePath);
            }
            throw e;
        }
        testScenarios = et.getValue().getTestRun().getTestScenario();
        for (TestScenario s : testScenarios) {
            for (Property property:
                    s.getProperties().getProperty()) {
                if(property.getName().equalsIgnoreCase("MODE")){
                    executionMode.add(property.getValue());
                }
            }
            for (TestCase t : s.getTestCase()) {
                enabledTestCases.add(t);

            }
        }
        LOGNREPORT.sphnxInfo(CORE_CONFIG, "Enabled Test Cases loaded.");
        return enabledTestCases;
    }

    @Bean(name="execution-mode")
    public List<String> executionModes(){
        return executionMode;
    }

    @Bean(name="test-scenarios")
    public List<TestScenario> testScenarios(){
        return testScenarios;
    }

    @Bean(name = "setExecutionContext")
    public HashMap<String, Object> executionContext() {
        try {
            Map<String, String> configurations = allConfigurations();
            String keywordFile = configurations.get(ParseConfigurations.Configs.KEYWORD_FILE.name());
            Configurations.KEYWORDS_FILE = Paths.get(keywordFile);
            LOGNREPORT.sphnxInfo(CORE_CONFIG, "Keyword File found at: " + Paths.get(keywordFile));
            String metaDataFile = configurations.get(ParseConfigurations.Configs.METADATA_FILE.name());
            Configurations.METADATA_FILE = Paths.get(metaDataFile);
            LOGNREPORT.sphnxInfo(CORE_CONFIG, "MetaData File found at: " + Paths.get(metaDataFile));
            File ecFile = new File(System.getProperty("user.dir"));
            //EC file is two levels up from the user.dir, so we get the parent of ecFile, then get the parent again to get to the
            //correct level for the EC file and append the resources\ec\ec.txt to the file location
            ecFile = new File(ecFile.getParent() + File.separator);
            ecFile = new File(ecFile.getParent() + File.separator
                    + "resources" + File.separator
                    + "ec" + File.separator
                    + "EC.txt");
            String ecFilePath = ecFile.getAbsolutePath();
            Configurations.EXECUTON_CONTEXZT_FILE = Paths.get(ecFilePath);
            if (ecFile.exists()) {
                LOGNREPORT.sphnxInfo(CORE_CONFIG, "Execution Context File found at: " + Paths.get(ecFilePath));
            } else {
                Configurations.EXECUTON_CONTEXZT_FILE = null;
                LOGNREPORT.sphnxInfo(CORE_CONFIG, "No Execution Context File found...");
            }
            Keyword.keywordDictionary = Keyword.loadKeywordsFromExcel(Configurations.KEYWORDS_FILE);
            LOGNREPORT.sphnxInfo(CORE_CONFIG, "Keywords Loaded.");
            MetaData.metaDictionary = MetaData.loadMetaDataFromExcel(Configurations.METADATA_FILE);
            LOGNREPORT.sphnxInfo(CORE_CONFIG, "MetaData Loaded.");
            if (Configurations.EXECUTON_CONTEXZT_FILE == null) {
                LOGNREPORT.sphnxInfo(CORE_CONFIG, "No Execution Context loaded... saved in memory.");
                return ExecutionContext.loadExecutionContextFromTextFile();
            } else {
                LOGNREPORT.sphnxInfo(CORE_CONFIG, "Execution Context loaded.");
                Configurations.hasECFile = true;
                ExecutionContext.ecDictionary = ExecutionContext.loadExecutionContextFromTextFile(Configurations.EXECUTON_CONTEXZT_FILE);
                return ExecutionContext.ecDictionary;
            }
        }
        catch (Exception e){
            LOGNREPORT.sphnxError(this.getClass().getCanonicalName(),
                    "Error loading execution context: Exception: " + e.getLocalizedMessage());
            throw e;
        }
    }

    private Map<String, String> allConfigurations() {
        ParseConfigurations parseConfigurations = new ParseConfigurations();
        return parseConfigurations.getAllConfigurations();
    }
}
