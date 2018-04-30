package utopia.sphnx.config;

import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import utopia.sphnx.config.manager.Properties;
import utopia.sphnx.config.manager.Property;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by jitendrajogeshwar on 16/08/17.
 */
@Configuration
@PropertySource("classpath:application-configuration.properties")
public class ParseConfigurations {
    private final static Logger LOGGER = LoggerFactory.getLogger(utopia.sphnx.config.ParseConfigurations.class);

//    private static String PARENT_DIR = Paths.get(System.getProperty("user.dir")).getParent().getParent().toString();
    private static String PARENT_DIR = Paths.get(System.getProperty("user.dir")).toString();

    private static String PATH_SEPARATOR = System.getProperty("file.separator");

    private static String THREAD_COUNT;

    @Value("${THREAD_COUNT}")
    private void setThreadCount(String threadCount){
        THREAD_COUNT = threadCount;
    }

    private static String RUN_HEADLESS;

    @Value("${RUN_HEADLESS}")
    private void setRunHeadless(String runHeadless){
        RUN_HEADLESS = runHeadless;
    }

    private static String DEFAULT_CONFIG_DIR;

    @Value("${LOGGER_PATH}")
    private void setLoggerPath(String loggerPath){
        LOGGER_PATH = loggerPath;
    }

    private static String LOGGER_PATH;

    @Value("${ARTIFACTS_DIR}")
    private void setArtifactsDir(String artifactsDir){
        ARTIFACTS_DIR = artifactsDir;
    }

    private static String ARTIFACTS_DIR;

    @Value("${EC_DIR}")
    private void setDefaultECDir(String ecDirCDir){
        EC_DIR = ecDirCDir;
    }

    private static String EC_DIR;


    @Value("${DEFAULT_CONFIG_DIR}")
    private void setDefaultConfigDir(String defaultConfigDir){
        DEFAULT_CONFIG_DIR = defaultConfigDir;
    }

    private static String DEFAULT_CONFIG_FILE;


    @Value("${DEFAULT_CONFIG_FILE}")
    private void setDefaultConfigFile(String defaultConfigFile){
        DEFAULT_CONFIG_FILE = defaultConfigFile;
    }


    private static String RESOURCES_DIR;

    @Value("${RESOURCES_DIR}")
    private void setResourcesDir(String resourcesDir){
        RESOURCES_DIR = resourcesDir;
    }

    private static String UIMAP_FILE;

    @Value("${UIMAP_FILE}")
    private void setUimapFile(String uimapFile){
        UIMAP_FILE = uimapFile;
    }

    private static String RUN_FILE;

    @Value("${RUN_FILE}")
    private void setRunFile(String runFile){
        RUN_FILE = runFile;
    }

    private static String SCENARIOS_DIR;

    @Value("${SCENARIOS_DIR}")
    private void setScenariosDir(String scenariosDir){
        SCENARIOS_DIR = scenariosDir;
    }

    private static String KEYWORD_FILE;

    @Value("${KEYWORDS_FILE}")
    private void setKeywordFile(String keywordFile){
        KEYWORD_FILE = keywordFile;
    }

    private static String METADATA_FILE;

    @Value("${METADATA_FILE}")
    private void setMetadataFile(String metadataFile){
        METADATA_FILE = metadataFile;
    }

    private static String DRIVER_ID;

    @Value("${DRIVER_ID}")
    private void setDriverId(String driverId){
        DRIVER_ID = driverId;
    }

    private static String BROWSER_VERSION;

    @Value("${browser_version}")
    private void setBrowserVersion(String browser_version){
        BROWSER_VERSION = browser_version;
    }

    private static String PLATFORM;

    @Value("${platform}")
    private void setPlatform(String platform){
        PLATFORM = platform;
    }


    private static String SAUCE_USER;

    @Value("${SAUCE_USER}")
    private void setSauceUser(String sauceUser){
        SAUCE_USER = sauceUser;
    }

    private static String SAUCE_ACCESS_KEY;

    @Value("${SAUCE_ACCESS_KEY}")
    private void setSauceAccessKey(String sauceAccessKey){
        SAUCE_ACCESS_KEY = sauceAccessKey;
    }

    private static Map<String,String> allConfigurations;

    /**
     * Configs representing all the config properties required by the framework
     */
    public enum Configs{

        RESOURCES_DIR(PARENT_DIR + PATH_SEPARATOR + utopia.sphnx.config.ParseConfigurations.RESOURCES_DIR),
        ARTIFACTS_DIR(PARENT_DIR + PATH_SEPARATOR + utopia.sphnx.config.ParseConfigurations.ARTIFACTS_DIR),
        RUN_FILE(RESOURCES_DIR.config + PATH_SEPARATOR + utopia.sphnx.config.ParseConfigurations.RUN_FILE),
        UIMAP_FILE(RESOURCES_DIR.config + PATH_SEPARATOR + utopia.sphnx.config.ParseConfigurations.UIMAP_FILE),
        SCENARIOS_DIR(RESOURCES_DIR.config + PATH_SEPARATOR + utopia.sphnx.config.ParseConfigurations.SCENARIOS_DIR),
        KEYWORD_FILE(RESOURCES_DIR.config + PATH_SEPARATOR + utopia.sphnx.config.ParseConfigurations.KEYWORD_FILE),
        METADATA_FILE(RESOURCES_DIR.config + PATH_SEPARATOR + utopia.sphnx.config.ParseConfigurations.METADATA_FILE),
        DEFAULT_CONFIG_FILE(utopia.sphnx.config.ParseConfigurations.DEFAULT_CONFIG_FILE),
        RUN_HEADLESS(utopia.sphnx.config.ParseConfigurations.RUN_HEADLESS),
        driver_id(utopia.sphnx.config.ParseConfigurations.DRIVER_ID),
        PLATFORM(utopia.sphnx.config.ParseConfigurations.PLATFORM),
        BROWSER_VERSION(utopia.sphnx.config.ParseConfigurations.BROWSER_VERSION),
        SAUCE_USER(utopia.sphnx.config.ParseConfigurations.SAUCE_USER),
        SAUCE_ACCESS_KEY(utopia.sphnx.config.ParseConfigurations.SAUCE_ACCESS_KEY),
        DRIVER_ID(utopia.sphnx.config.ParseConfigurations.DRIVER_ID),
        EC_DIR(RESOURCES_DIR.config + PATH_SEPARATOR + utopia.sphnx.config.ParseConfigurations.EC_DIR),
        LOGGER_PATH(PARENT_DIR + PATH_SEPARATOR +
                utopia.sphnx.config.ParseConfigurations.ARTIFACTS_DIR +
                PATH_SEPARATOR + utopia.sphnx.config.ParseConfigurations.LOGGER_PATH),
        THREAD_COUNT(utopia.sphnx.config.ParseConfigurations.THREAD_COUNT),
        SCENARIO_FILE(""),
        BASE_URL("");

        private String config;

        Configs(String config) {
            this.config = config;
        }

        public String config() {
            return config;
        }
    }

    public Map<String, String> getAllConfigurations(){
        if(allConfigurations == null){
          return setConfiguration();
        }
        return allConfigurations;
    }


    /**
     * Set all configurations for the framework
     * @return hashmap containing key pair values for configurations
     */
    private Map<String, String> setConfiguration() {
        allConfigurations = Maps.newHashMap();


        try {
            //if CONFIG_FILE is passed from command line use parseConfigurationWithConfigFile
            if (System.getProperties().containsKey("CONFIG_FILE")) {
                String configFile = (String) System.getProperties().get("CONFIG_FILE");

                parseConfigurationWithConfigFile(configFile);

            }

            //search for the config file in the resources directory
            //by default framework searches for config file in the resources directory
            else if(searchFile(Configs.RESOURCES_DIR.config, Configs.DEFAULT_CONFIG_FILE.config, false) != null){
                String configFile =  Configs.RESOURCES_DIR.config  + PATH_SEPARATOR + Configs.DEFAULT_CONFIG_FILE.config;;
                parseConfigurationWithConfigFile(configFile);
            }
            else {
                LOGGER.info("External Config file is not provided using default values");
                for (utopia.sphnx.config.ParseConfigurations.Configs config: utopia.sphnx.config.ParseConfigurations.Configs.values()) {
                    if(config.name().equalsIgnoreCase("driver_id") && config.config.equalsIgnoreCase("")){
                        allConfigurations.put(config.name().toUpperCase(),"CHROME");
                    }
                    else if (config.name().equalsIgnoreCase("driver_id") && config.config.toLowerCase().contains("sauce")) {
                        if (Configs.SAUCE_ACCESS_KEY.config.equalsIgnoreCase("") || Configs.SAUCE_USER.config.equalsIgnoreCase("")) {
                            throw new IllegalArgumentException("SAUCE USER NAME or ACCESS KEY is not provided");
                        }
                        if (config.config.toLowerCase().contains("chrome")) {
                            //configure capability with chrome version
                            if (System.getProperties().containsKey("BROWSER_VERSION") || System.getProperties().containsKey("browser_version")  ) {
                                String browser_version = System.getProperty("BROWSER_VERSION") == null ?
                                        System.getProperty("browser_version")
                                        :System.getProperty("BROWSER_VERSION") ;
                                allConfigurations.put("BROWSER_VERSION", browser_version);
                            } else {
                                allConfigurations.put("BROWSER_VERSION", "58");
                            }
                        } else if (config.config.toLowerCase().contains("firefox")) {
                            //configure capability with chrome version
                            if (System.getProperties().containsKey("BROWSER_VERSION") || System.getProperties().containsKey("browser_version") ) {
                                String browser_version = System.getProperty("BROWSER_VERSION") == null ?
                                        System.getProperty("browser_version")
                                        :System.getProperty("BROWSER_VERSION") ;
                                allConfigurations.put("BROWSER_VERSION", browser_version);
                            } else {
                                allConfigurations.put("BROWSER_VERSION", "54.0");
                            }
                        }
                        if (System.getProperties().containsKey("PLATFORM") || System.getProperties().containsKey("platform") ) {
                            //configure capability with platform type
                            String platform = System.getProperty("PLATFORM")  == null ?
                                    System.getProperty("platform")
                                    :System.getProperty("PLATFORM") ;
                            allConfigurations.put("PLATFORM", platform);
                        }
                         else {
                            allConfigurations.put("PLATFORM", "Windows 10");
                        }

                    }
                    else {
                        if(! (config.name().equalsIgnoreCase("BROWSER_VERSION") || config.name().equalsIgnoreCase("PLATFORM"))) {
                            allConfigurations.put(config.name().toUpperCase(), config.config);
                        }
                    }
                }

            }
            if (!allConfigurations.containsKey("driver_id")){
                allConfigurations.put("driver_id".toUpperCase(), allConfigurations.get(Configs.DRIVER_ID));
            }
            for (utopia.sphnx.config.ParseConfigurations.Configs config: utopia.sphnx.config.ParseConfigurations.Configs.values()) {
                if(!allConfigurations.containsKey(config.name())) {
                    allConfigurations.put(config.name().toUpperCase(), config.config);
                }
            }
            for(Object property : System.getProperties().keySet()){
                String propertyName = (String) property;
                if(Arrays.stream(Configs.values()).anyMatch(p -> p.name().equalsIgnoreCase(propertyName))) {
                    allConfigurations.put(propertyName.toUpperCase(), System.getProperty(propertyName));
                }
            }
            if (System.getProperties().containsKey("BASE_URL")) {
                String baseUrl = (String) System.getProperties().get("BASE_URL");
                allConfigurations.put(Configs.BASE_URL.name(), baseUrl);
            }
            else if (System.getProperties().containsKey("base_url") ) {
                String baseUrl = (String) System.getProperties().get("base_url");
                allConfigurations.put(Configs.BASE_URL.name(),baseUrl);
            }
            setSystemPropertyForLoggerPath();
            return allConfigurations;

        } catch (Exception e) {
            setSystemPropertyForLoggerPath();
            LOGGER.error("Data Map (Properties File Map) not found!" +
                    "Stack trace:" + e.getStackTrace());

            return null;
        }
    }

    private void setSystemPropertyForLoggerPath() {
     if(!System.getProperties().contains("LOGGER_PATH")) {
         System.setProperty("LOGGER_PATH",allConfigurations.get(Configs.LOGGER_PATH.name()));
     }
    }

    /**
     * Read the config file passed by the client
     * All the default config values will be replaced by the values in xml file
     * @param configFile config file path
     */
    private void parseConfigurationWithConfigFile(String configFile) throws JAXBException, FileNotFoundException {
        JAXBContext jc = JAXBContext.newInstance(Properties.class);

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        if(!configFile.contains(File.separator)){
            configFile = PARENT_DIR + PATH_SEPARATOR + DEFAULT_CONFIG_DIR + PATH_SEPARATOR + configFile;
        }
        LOGGER.info("Using config file from " + configFile);
        File xmlFile = new File(configFile);
        FileInputStream fileInputStream = new FileInputStream(xmlFile);
        Source source = new StreamSource(fileInputStream);
        JAXBElement<Properties> et = unmarshaller.unmarshal(source, Properties.class);
        List<Property> properties = et.getValue().getProperty();
        allConfigurations = properties.stream().collect(Collectors.toMap(Property::getKey, Property::getValue));

        for (Map.Entry<String, String> entry : allConfigurations.entrySet()) {
            String key = entry.getKey();
            String value = (String) entry.getValue();
            if(entry.getValue() instanceof String){
                if (value.equalsIgnoreCase("") ){
                    allConfigurations.put(key.toUpperCase(), (String) Configs.valueOf(key).config);
                }
                else {
                    allConfigurations.put(key.toUpperCase(), value);
                }
            }
        }

        LOGGER.info("Data Map (Properties File Map) Loaded.");
    }

    private String searchFile(String rootDirectory, String fileName, boolean recursive){
        try {
            File root = new File(rootDirectory);
            Collection files = FileUtils.listFiles(root, null, recursive);
            for (Object file1 : files) {
                File file = (File) file1;
                if (file.getName().equals(fileName))
                    return fileName;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
