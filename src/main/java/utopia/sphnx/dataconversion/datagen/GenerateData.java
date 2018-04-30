package utopia.sphnx.dataconversion.datagen;


import ch.qos.logback.core.spi.LogbackLock;
import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.*;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.collections.Lists;
import utopia.sphnx.dataconversion.datagen.keyword.Address;

import java.io.*;
import java.nio.charset.Charset;
import java.rmi.server.UID;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created with IntelliJ IDEA
 * <p>
 * Package: utopia.reportutils.text
 * <p>
 * Name   : LoremPersonGenerator
 * <p>
 * User   : solmarkn / Dani Vainstein
 * <p>
 * Date   : 2016-05-05
 * <p>
 * Time   : 13:17
 */
public class GenerateData {

    //region Variables Declaration and Initialization Section.

    private Random random = null;

    private static PeriodFormatter yearMonthDayFormatter =
            new PeriodFormatterBuilder()
                    .appendYears()
                    .appendSuffix(" year", " years")
                    .appendSeparator(", ")
                    .appendMonths().appendSuffix(" month", " months")
                    .appendSeparator(" and ")
                    .appendDays()
                    .appendSuffix(" day", " days")
                    .toFormatter();

    public enum Gender {MALE, FEMALE}

    private static final LogbackLock lock = new LogbackLock();

    private static final Charset DEFAULT_CHARSET = Charsets.ISO_8859_1;

    private static final String DEFAULT_STRING_FORMAT = "MM/dd/yyyy";

    private static utopia.sphnx.dataconversion.datagen.GenerateData instance;

    private static final Resource addressesResource = new ClassPathResource("addresses.txt");

    private static final Resource streetsResource = new ClassPathResource("streets.txt");

    private static final Resource phoneNumberResource = new ClassPathResource("phones.txt");

    private static final Resource emailsResource = new ClassPathResource("emails.txt");

    private static final Resource femaleNamesResource = new ClassPathResource("female_names.txt");

    private static final Resource maleNamesResource = new ClassPathResource("male_names.txt");

    private static final Resource surNamesResource = new ClassPathResource("surnames.txt");

    private static final Resource wordsResource = new ClassPathResource("words.txt");

    private static final Resource questionsResource = new ClassPathResource("questions.txt");

    private static final Resource macAddressResource = new ClassPathResource("mac_address.txt");

    private static final Resource countriesResource = new ClassPathResource("countries.txt");

    private static final int FEMALES_LIST_ID = 1;

    private static final int MALES_LIST_ID = 2;

    private static final int SURNAMES_LIST_ID = 3;

    private static final int EMAIL_LIST_ID = 4;

    private static final int TELEPHONE_LIST_ID = 5;

    private static final int ADDRESS_LIST_ID = 6;

    private static final int STREETS_LIST_ID = 7;

    private static final int WORD_LIST_ID = 8;

    private static final int QUESTION_LIST_ID = 9;

    private static final int MAC_ADDRESS_ID = 11;

    private static LoadingCache<Integer, List> resourceListsCache;

    private static Charset charset = DEFAULT_CHARSET;

    // private List<String> urls;


    private GenerateData(Long seed) {
        if (seed == null) {
            this.random = new Random();
        } else {
            this.random = new Random(seed);
        }


        initializeCache();
    }

    public static utopia.sphnx.dataconversion.datagen.GenerateData getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new utopia.sphnx.dataconversion.datagen.GenerateData(null);
                }
            }
        }
        return instance;
    }

    public static utopia.sphnx.dataconversion.datagen.GenerateData getInstance(Long seed) {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new utopia.sphnx.dataconversion.datagen.GenerateData(seed);
                }
            }
        } else {
            instance.random = new Random(seed);
        }
        return instance;
    }


    //endregion

    public void clean() {
        if (null != resourceListsCache) {
            resourceListsCache.cleanUp();
        }
    }


    public void setDefaultCharset() {

    }


    //region Implementation of Names generators

    //---------------------------------------------------------------------
    // Implementation of Names generators
    //---------------------------------------------------------------------


    public Pair<String, Gender> getFirstName() {
        if (random.nextBoolean()) {
            String n = getGenderFirstName(Gender.FEMALE);
            return new ImmutablePair<>(n, Gender.FEMALE);

        } else {
            String n = getGenderFirstName(Gender.MALE);
            return new ImmutablePair<>(n, Gender.MALE);
        }

    }


    public String getGenderFirstName(Gender gender) {
        try {
            if (gender.equals(Gender.FEMALE)) {
                List femaleNames = getLoadingCache().get(FEMALES_LIST_ID);
                return String.class.cast(femaleNames.get(random.nextInt(femaleNames.size())));
            } else {
                List males = getLoadingCache().get(MALES_LIST_ID);
                return String.class.cast(males.get(random.nextInt(males.size())));
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return "";
    }


    public Pair<String, Gender> getMiddleName() {
        return getFirstName();
    }


    public String getGenderMiddleName(Gender gender) {
        return getGenderFirstName(gender);
    }


    public String getName() {
        return getFirstName() + " " + getLastName();
    }


    public String getName(Gender gender) {
        return getGenderFirstName(gender) + " " + getLastName();
    }


    public String getLastName() {
        try {
            List surnames = getLoadingCache().get(SURNAMES_LIST_ID);
            return String.class.cast(surnames.get(random.nextInt(surnames.size())));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return "";
    }


    public String getFQName() {
        return (getFirstName() + " " + getMiddleName() + " " + getLastName());
    }


    public String getFQName(Gender gender) {
        return (getGenderFirstName(gender) + " " + getGenderMiddleName(gender) + " " + getLastName());
    }

    //endregion


    //region Implementation of personal information generators

    //---------------------------------------------------------------------
    // Implementation of personal information generators
    //---------------------------------------------------------------------

    public Address getAddress() {
        try {
            List addresses = getLoadingCache().get(ADDRESS_LIST_ID);
            Collections.shuffle(addresses);
            return Address.class.cast(addresses.get(random.nextInt(addresses.size())));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return Address.DEFAULT_ADDRESS;
    }

    @SuppressWarnings("unchecked")
    public Address getFirstAddress(Predicate<Address> predicate) {
        try {
            List<Address> addresses = (List<Address>) getLoadingCache().get(ADDRESS_LIST_ID);
            Collections.shuffle(addresses);
            Supplier<Address> su = () -> Address.DEFAULT_ADDRESS;
            return addresses.stream().filter(predicate).findFirst().orElseGet(() -> Address.DEFAULT_ADDRESS);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return Address.DEFAULT_ADDRESS;
    }

    public String getStreet() {
        try {
            List streets = getLoadingCache().get(STREETS_LIST_ID);
            Collections.shuffle(streets);
            return (String) streets.get(random.nextInt(streets.size()));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return "Kenmore Ave. Unit 2E";
    }


    public String getEmailAddress() {
        try {
            List emails = getLoadingCache().get(EMAIL_LIST_ID);
            return String.class.cast(emails.get(random.nextInt(emails.size())));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return "ab.cd@example.com";
    }


    public String getEmailAddress(String firstName, String lastName) {
        return firstName.toLowerCase() + "." + lastName.toLowerCase() + "@example.com";
    }

    public String getEmailAddress(String firstName, String middleName, String lastName) {
        String i = StringUtils.left(middleName, 1).toLowerCase();
        return firstName.toLowerCase() + i + "." + lastName.toLowerCase() + "@example.com";
    }

    public String getTelephoneNumber() {
        try {
            List emails = getLoadingCache().get(TELEPHONE_LIST_ID);
            return String.class.cast(emails.get(random.nextInt(emails.size())));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return "(123) 935-1340";
    }

    public Years getApproxAge(LocalDateTime birthDate) {
        if ((birthDate != null)) {
            return Years.yearsBetween(birthDate, LocalDateTime.now());
        } else {
            return Years.ZERO;
        }
    }

    public String getAge(LocalDateTime birthDate) {
        Period p = new Period(birthDate, LocalDateTime.now());
        return yearMonthDayFormatter.print(p.toPeriod());
    }

    public String getStateCodeToName(String stateAbbreviation){
        return stateCodeToNameMap.get(stateAbbreviation);
    }

    public String getStateNameToCode(String state){
        return stateCodeToNameMap.get(state);
    }

    private static final Map<String, String> stateCodeToNameMap = new LinkedHashMap<>();

    private static final Map<String, String> stateNameToCodeMap = new LinkedHashMap<>();

    static {
        // Load US Codes.
        stateCodeToNameMap.put("AK", "Alaska");
        stateCodeToNameMap.put("AL", "Alabama");
        stateCodeToNameMap.put("AR", "Akansas");
        stateCodeToNameMap.put("AZ", "Arizona");
        stateCodeToNameMap.put("CA", "California");
        stateCodeToNameMap.put("CO", "Colorado");
        stateCodeToNameMap.put("CT", "Connecticut");
        stateCodeToNameMap.put("DC", "District of Columbia");
        stateCodeToNameMap.put("DE", "Delaware");
        stateCodeToNameMap.put("FL", "Florida");
        stateCodeToNameMap.put("GA", "Georgia");
        stateCodeToNameMap.put("HI", "Hawaii");
        stateCodeToNameMap.put("IA", "Iowa");
        stateCodeToNameMap.put("ID", "Idaho");
        stateCodeToNameMap.put("IL", "Illinois");
        stateCodeToNameMap.put("IN", "Indiana");
        stateCodeToNameMap.put("KS", "Kansas");
        stateCodeToNameMap.put("KY", "Kentucky");
        stateCodeToNameMap.put("LA", "Louisiana");
        stateCodeToNameMap.put("MA", "Massachusetts");
        stateCodeToNameMap.put("MD", "Maryland");
        stateCodeToNameMap.put("ME", "Maine");
        stateCodeToNameMap.put("MI", "Michigan");
        stateCodeToNameMap.put("MN", "Minnesota");
        stateCodeToNameMap.put("MO", "Missouri");
        stateCodeToNameMap.put("MS", "Mississippi");
        stateCodeToNameMap.put("MT", "Montana");
        stateCodeToNameMap.put("NC", "North Carolina");
        stateCodeToNameMap.put("ND", "North Dakota");
        stateCodeToNameMap.put("NE", "Nebraska");
        stateCodeToNameMap.put("NH", "New Hampshire");
        stateCodeToNameMap.put("NJ", "New Jersey");
        stateCodeToNameMap.put("NM", "New Mexico");
        stateCodeToNameMap.put("NV", "Nevada");
        stateCodeToNameMap.put("NY", "New York");
        stateCodeToNameMap.put("OH", "Ohio");
        stateCodeToNameMap.put("OK", "Oklahoma");
        stateCodeToNameMap.put("OR", "Oregon");
        stateCodeToNameMap.put("PA", "Pennsylvania");
        stateCodeToNameMap.put("RI", "Rhode Island");
        stateCodeToNameMap.put("SC", "South Carolina");
        stateCodeToNameMap.put("SD", "South Dakota");
        stateCodeToNameMap.put("TN", "Tennessee");
        stateCodeToNameMap.put("TX", "Texas");
        stateCodeToNameMap.put("UT", "Utah");
        stateCodeToNameMap.put("VA", "Virginia");
        stateCodeToNameMap.put("VT", "Vermont");
        stateCodeToNameMap.put("WA", "Washington");
        stateCodeToNameMap.put("WI", "Wisconsin");
        stateCodeToNameMap.put("WV", "West Virginia");
        stateCodeToNameMap.put("WY", "Wyoming");
        stateCodeToNameMap.put("GU", "Guam");
        stateCodeToNameMap.put("VI", "Virgin Islands");
        stateCodeToNameMap.put("PR", "Puerto Rico");
        stateCodeToNameMap.put("AE", "Armed forces - Europe");
        stateCodeToNameMap.put("AA", "Armed forces - America");
        stateCodeToNameMap.put("AP", "Armed forces - Pacific");

        // Load Canada Codes.
        stateCodeToNameMap.put("AB", "Alberta");
        stateCodeToNameMap.put("BC", "British Columbia");
        stateCodeToNameMap.put("MB", "Manitoba");
        stateCodeToNameMap.put("NB", "New Brunswick");
        stateCodeToNameMap.put("NL", "Newfoundland and Labrador");
        stateCodeToNameMap.put("NT", "Northwest Territories");
        stateCodeToNameMap.put("NS", "Nova Scotia");
        stateCodeToNameMap.put("NU", "Nunavut");
        stateCodeToNameMap.put("ON", "Ontario");
        stateCodeToNameMap.put("PE", "Prince Edward Island");
        stateCodeToNameMap.put("QC", "Quebec");
        stateCodeToNameMap.put("SK", "Saskatchewan");
        stateCodeToNameMap.put("YT", "Yukon");

        // Load México Codes.
        stateCodeToNameMap.put("AGU", "Aguascalientes");
        stateCodeToNameMap.put("BCN", "Baja California");
        stateCodeToNameMap.put("BCS", "Baja California Sur");
        stateCodeToNameMap.put("CAM", "Campeche");
        stateCodeToNameMap.put("CHP", "Chiapas");
        stateCodeToNameMap.put("CHH", "Chihuahua");
        stateCodeToNameMap.put("COA", "Coahuila");
        stateCodeToNameMap.put("COL", "Colima");
        stateCodeToNameMap.put("DIF", "Distrito Federal");
        stateCodeToNameMap.put("DUR", "Durango");
        stateCodeToNameMap.put("GUA", "Guanajuato");
        stateCodeToNameMap.put("GRO", "Guerrero");
        stateCodeToNameMap.put("HID", "Hidalgo");
        stateCodeToNameMap.put("JAL", "Jalisco");
        stateCodeToNameMap.put("MEX", "México");
        stateCodeToNameMap.put("MIC", "Michoacán");
        stateCodeToNameMap.put("MOR", "Morelos");
        stateCodeToNameMap.put("NAY", "Nayarit");
        stateCodeToNameMap.put("NLE", "Nuevo León");
        stateCodeToNameMap.put("OAX", "Oaxaca");
        stateCodeToNameMap.put("PUE", "Puebla");
        stateCodeToNameMap.put("QUE", "Querétaro");
        stateCodeToNameMap.put("ROO", "Quintana Roo");
        stateCodeToNameMap.put("SLP", "San Luis Potosí");
        stateCodeToNameMap.put("SIN", "Sinaloa");
        stateCodeToNameMap.put("SON", "Sonora");
        stateCodeToNameMap.put("TAB", "Tabasco");
        stateCodeToNameMap.put("TAM", "Tamaulipas");
        stateCodeToNameMap.put("TLA", "Tlaxcala");
        stateCodeToNameMap.put("VER", "Veracruz");
        stateCodeToNameMap.put("YUC", "Yucatán");
        stateCodeToNameMap.put("ZAC", "Zacatecas");


        // Load US State Names.
        stateNameToCodeMap.put("Alabama","AL");
        stateNameToCodeMap.put("Alaska","AK");
        stateNameToCodeMap.put("Arizona","AZ");
        stateNameToCodeMap.put("Arkansas","AR");
        stateNameToCodeMap.put("California","CA");
        stateNameToCodeMap.put("Colorado","CO");
        stateNameToCodeMap.put("Connecticut","CT");
        stateNameToCodeMap.put("Delaware","DE");
        stateNameToCodeMap.put("District Of Columbia","DC");
        stateNameToCodeMap.put("Florida","FL");
        stateNameToCodeMap.put("Georgia","GA");
        stateNameToCodeMap.put("Hawaii","HI");
        stateNameToCodeMap.put("Idaho","ID");
        stateNameToCodeMap.put("Illinois","IL");
        stateNameToCodeMap.put("Indiana","IN");
        stateNameToCodeMap.put("Iowa","IA");
        stateNameToCodeMap.put("Kansas","KS");
        stateNameToCodeMap.put("Kentucky","KY");
        stateNameToCodeMap.put("Louisiana","LA");
        stateNameToCodeMap.put("Maine","ME");
        stateNameToCodeMap.put("Maryland","MD");
        stateNameToCodeMap.put("Massachusetts","MA");
        stateNameToCodeMap.put("Michigan","MI");
        stateNameToCodeMap.put("Minnesota","MN");
        stateNameToCodeMap.put("Mississippi","MS");
        stateNameToCodeMap.put("Missouri","MO");
        stateNameToCodeMap.put("Montana","MT");
        stateNameToCodeMap.put("Nebraska","NE");
        stateNameToCodeMap.put("Nevada","NV");
        stateNameToCodeMap.put("New Hampshire","NH");
        stateNameToCodeMap.put("New Jersey","NJ");
        stateNameToCodeMap.put("New Mexico","NM");
        stateNameToCodeMap.put("New York","NY");
        stateNameToCodeMap.put("North Carolina","NC");
        stateNameToCodeMap.put("North Dakota","ND");
        stateNameToCodeMap.put("Ohio","OH");
        stateNameToCodeMap.put("Oklahoma","OK");
        stateNameToCodeMap.put("Oregon","OR");
        stateNameToCodeMap.put("Pennsylvania","PA");
        stateNameToCodeMap.put("Rhode Island","RI");
        stateNameToCodeMap.put("South Carolina","SC");
        stateNameToCodeMap.put("South Dakota","SD");
        stateNameToCodeMap.put("Tennessee","TN");
        stateNameToCodeMap.put("Texas","TX");
        stateNameToCodeMap.put("Utah","UT");
        stateNameToCodeMap.put("Vermont","VT");
        stateNameToCodeMap.put("Virginia","VA");
        stateNameToCodeMap.put("Washington","WA");
        stateNameToCodeMap.put("West Virginia","WV");
        stateNameToCodeMap.put("Wisconsin","WI");
        stateNameToCodeMap.put("Wyoming","WY");
        stateNameToCodeMap.put("Guam", "GU");
        stateNameToCodeMap.put("Puerto Rico","PR");
        stateNameToCodeMap.put("Virgin Islands","VI");
        stateNameToCodeMap.put("Armed Forces (AE)","AE");
        stateNameToCodeMap.put("Armed Forces Americas","AA");
        stateNameToCodeMap.put("Armed Forces Pacific","AP");


        // Load Canada State Names.
        stateNameToCodeMap.put("Alberta","AB");
        stateNameToCodeMap.put("British Columbia","BC");
        stateNameToCodeMap.put("Manitoba","MB");
        stateNameToCodeMap.put("New Brunswick","NB");
        stateNameToCodeMap.put("Newfoundland and Labrador","NF");
        stateNameToCodeMap.put("Northwest Territories","NT");
        stateNameToCodeMap.put("Nova Scotia","NS");
        stateNameToCodeMap.put("Nunavut","NU");
        stateNameToCodeMap.put("Ontario","ON");
        stateNameToCodeMap.put("Prince Edward Island","PE");
        stateNameToCodeMap.put("Quebec","QC");
        stateNameToCodeMap.put("Saskatchewan","SK");
        stateNameToCodeMap.put("Yukon Territory","YT");


        // Load México State Names.
        stateNameToCodeMap.put("Aguascalientes", "AGU");
        stateNameToCodeMap.put("Baja California", "BCN");
        stateNameToCodeMap.put("Baja California Sur", "BCS");
        stateNameToCodeMap.put("Campeche", "CAM");
        stateNameToCodeMap.put("Chiapas", "CHP");
        stateNameToCodeMap.put("Chihuahua", "CHH");
        stateNameToCodeMap.put("Coahuila", "COA");
        stateNameToCodeMap.put("Colima", "COL");
        stateNameToCodeMap.put("Distrito Federal", "DIF");
        stateNameToCodeMap.put("Durango", "DUR");
        stateNameToCodeMap.put("Guanajuato", "GUA");
        stateNameToCodeMap.put("Guerrero", "GRO");
        stateNameToCodeMap.put("Hidalgo", "HID");
        stateNameToCodeMap.put("Jalisco", "JAL");
        stateNameToCodeMap.put("México", "MEX");
        stateNameToCodeMap.put("Michoacán", "MIC");
        stateNameToCodeMap.put("Morelos", "MOR");
        stateNameToCodeMap.put("Nayarit", "NAY");
        stateNameToCodeMap.put("Nuevo León", "NLE");
        stateNameToCodeMap.put("Oaxaca", "OAX");
        stateNameToCodeMap.put("Puebla", "PUE");
        stateNameToCodeMap.put("Querétaro", "QUE");
        stateNameToCodeMap.put("Quintana Roo", "ROO");
        stateNameToCodeMap.put("San Luis Potosí", "SLP");
        stateNameToCodeMap.put("Sinaloa", "SIN");
        stateNameToCodeMap.put("Sonora", "SON");
        stateNameToCodeMap.put("Tabasco", "TAB");
        stateNameToCodeMap.put("Tamaulipas", "TAM");
        stateNameToCodeMap.put("Tlaxcala", "TLA");
        stateNameToCodeMap.put("Veracruz", "VER");
        stateNameToCodeMap.put("Yucatán", "YUC");
        stateNameToCodeMap.put("Zacatecas", "ZAC");
    }

    //endregion


    //region Implementation of  random numbers and random numeric ranges

    //---------------------------------------------------------------------
    // Implementation of  random numbers and random numeric ranges
    //---------------------------------------------------------------------


    public boolean randomBoolean() {
        return random.nextBoolean();
    }

    /**
     * @return a random boolean value
     */
    public String randomBoolean(String trueValue, String falseValue) {
        boolean b = randomBoolean();
        return BooleanUtils.toString(b, trueValue, falseValue);
    }

    public char randomBetween(char min, char max) {
        return (char) randomBetween((int) min, (int) max);
    }


    /**
     * Returns a random long within the specified range.
     *
     * @param startInclusive the smallest value that can be returned, must be non-negative
     * @param endExclusive   the upper bound (not included)
     * @return the random long
     * @throws IllegalArgumentException if {@code startInclusive > endExclusive} or if {@code startInclusive} is negative
     */
    public long randomBetween(long startInclusive, long endExclusive) {
        return RandomUtils.nextLong(startInclusive, endExclusive);
    }


    public long randomLong() {
        return randomBetween(0, Long.MAX_VALUE);
    }


    public int randomBetween(int startInclusive, int endExclusive) {
        return RandomUtils.nextInt(startInclusive, endExclusive);
    }


    public int randomInt() {
        return randomBetween(0, Integer.MAX_VALUE);
    }

    /**
     * Creates an array of random bytes.
     *
     * @param count the size of the returned array
     * @return the random byte array
     * @throws IllegalArgumentException if {@code count} is negative
     */
    public byte[] randomBytes(int count) {
        final byte[] result = new byte[count];
        random.nextBytes(result);
        return result;
    }


    public double randomBetween(double startInclusive, double endInclusive) {
        return RandomUtils.nextDouble(startInclusive, endInclusive);
    }


    public double randomDouble() {
        return randomBetween(0, Double.MAX_VALUE);
    }


    public float randomBetween(float startInclusive, float endInclusive) {
        return RandomUtils.nextFloat(startInclusive, endInclusive);
    }


    public float randomFloat() {
        return randomBetween(0, Float.MAX_VALUE);
    }

    //endregion


    //region Implementation of list selection

    //---------------------------------------------------------------------
    // Implementation of list selection
    //---------------------------------------------------------------------


    public <T> T randomElement(List<T> elements) {
        return elements.get(randomBetween(0, elements.size() - 1));
    }


    public <T> T randomElement(T... elements) {
        return randomElement(Arrays.asList(elements));
    }


    public <T extends Enum<?>> T randomElement(Class<T> enumType) {
        return enumType.getEnumConstants()[randomBetween(0, enumType.getEnumConstants().length - 1)];
    }


    public <T> List<T> randomElements(List<T> elements, int count) {
        Collections.shuffle(elements, random);
        return elements.subList(0, count % elements.size());
    }


    public <T> List<T> randomElements(int count, T... elements) {
        return randomElements(Arrays.asList(elements), count);
    }


    //endregion


    //region Implementation of Random id's

    //---------------------------------------------------------------------
    //  Implementation of Random id's
    //---------------------------------------------------------------------


    public String randomUUID() {
        return UUID.randomUUID().toString();
    }


    public String randomUserId() {
        return new UID().toString();
    }

    public String randomNumericCode(int length) {
        return RandomStringUtils.randomNumeric(length);
    }


    public String randomAlphaCode(int length) {
        return RandomStringUtils.randomAlphabetic(length).toUpperCase();
    }

    public String randomAlphaNumericCode(int length) {
        return RandomStringUtils.randomAlphanumeric(length).toUpperCase();
    }


    //endregion


    //region Generic words and paragraphs

    //---------------------------------------------------------------------
    // Generic words and paragraphs
    //---------------------------------------------------------------------


    public String getTitle(int min, int max) {
        return getWords(min, max, true);
    }


    public String getTitle(int count) {
        return getWords(count, count, true);
    }


    public String getQuestion() {
        try {
            List questions = getLoadingCache().get(QUESTION_LIST_ID);
            return String.class.cast(questions.get(random.nextInt(questions.size())));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return "Do you have pets?";
    }


    public String getHtmlParagraphs(int min, int max) {
        return null;
    }


    public String getWords(int count) {
        return getWords(count, count, false);
    }


    public String getWords(int min, int max) {
        return getWords(min, max, false);
    }

    private String getWords(int min, int max, boolean title) {
        int count = getCount(min, max);
        return getWords(count, title);
    }

    private String getWords(int count, boolean title) {
        try {
            List words = getLoadingCache().get(EMAIL_LIST_ID);

            StringBuilder sb = new StringBuilder();
            int size = words.size();
            int wordCount = 0;
            while (wordCount < count) {
                String word = (String) words.get(random.nextInt(size));
                if (title) {
                    if (wordCount == 0 || word.length() > 3) {
                        word = word.substring(0, 1).toUpperCase() + word.substring(1);
                    }
                }
                sb.append(word);
                sb.append("");
                wordCount++;
            }

            return sb.toString().trim();

        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return RandomStringUtils.random(count);

    }

    private int getCount(int min, int max) {
        if (min < 0) {
            min = 0;
        }
        if (max < min) {
            max = min;
        }
        return max != min ? random.nextInt(max - min) + min : min;
    }


    public String getParagraphs(int min, int max) {
        int count = getCount(min, max);
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < count; j++) {
            int sentences = random.nextInt(5) + 2; // 2 to 6
            for (int i = 0; i < sentences; i++) {
                String first = getWords(1, 1, false);
                first = first.substring(0, 1).toUpperCase() + first.substring(1);
                sb.append(first);

                sb.append(getWords(2, 20, false));
                sb.append(".  ");
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    //endregion


    //region Generic Random Dates

    //---------------------------------------------------------------------
    // Generic Random Dates
    //---------------------------------------------------------------------
    public LocalDateTime getToday() {
        return LocalDateTime.now();
    }

    public LocalDateTime getTodayMinus(int num, String time) {
        LocalDateTime today = LocalDateTime.now();

        switch (time.toUpperCase()) {
            case ("D"):
                return today.minusDays(num);
            case ("W"):
                return today.minusWeeks(num);
            case ("M"):
                return today.minusMonths(num);
            case ("Y"):
                return today.minusYears(num);
            default:
                return today.minusYears(num);
        }

    }

    public LocalDateTime getTodayPlus(int num, String time) {
        LocalDateTime today = LocalDateTime.now();

        switch (time.toUpperCase()) {
            case ("D"):
                return today.plusDays(num);
            case ("W"):
                return today.plusWeeks(num);
            case ("M"):
                return today.plusMonths(num);
            case ("Y"):
                return today.plusYears(num);
            default:
                return today.plusYears(num);
        }

    }

    public LocalDateTime getDateTimeBetween(long early, long later) {
        LocalDateTime today = LocalDateTime.now();

        Long e = early;
        Long l = later;
        LocalDateTime newestDate = today.minusYears(e.intValue());
        LocalDateTime olderDate = today.minusYears(l.intValue());

        return getDateTimeBetween(newestDate, olderDate);

    }

    public LocalDateTime getDateTimeBetween(LocalDateTime early, LocalDateTime later) {
        LocalDateTime today = LocalDateTime.now();

        Long diff = (early.toDate().getTime() - later.toDate().getTime()) + 1;

        Long rnd = randomBetween(0L, diff);
        Long newTimestamp = later.toDate().getTime() + rnd;

        return new LocalDateTime(newTimestamp);
    }

    public LocalDate getDateBetween(long early, long later) {
        LocalDate today = LocalDate.now();

        Long e = early;
        Long l = later;
        LocalDate newestDate = today.minusYears(e.intValue());
        LocalDate olderDate = today.minusYears(l.intValue());

        return getDateBetween(newestDate, olderDate);

    }

    public LocalDate getDateBetween(LocalDate early, LocalDate later) {
        LocalDateTime today = LocalDateTime.now();

        Long diff = (early.toDate().getTime() - later.toDate().getTime()) + 1;

        Long rnd = randomBetween(0L, diff);
        Long newTimestamp = later.toDate().getTime() + rnd;

        return new LocalDate(newTimestamp);
    }

    public LocalDate getPastDate(ReadablePeriod max) {
        LocalDate now = LocalDate.now();
        LocalDate past = now.minus(max);

        long toAdd = randomBetween(past.toDate().getTime(), now.toDate().getTime());

        return new LocalDate(past.toDate().getTime() + toAdd);
    }

    public LocalDateTime getPastDateTime(ReadablePeriod max) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = now.minus(max);

        long toAdd = randomBetween(past.toDate().getTime(), now.toDate().getTime());

        return new LocalDateTime(past.toDate().getTime() + toAdd);
    }


    public LocalDate getFutureDate(ReadablePeriod max) {
        LocalDate now = LocalDate.now();
        LocalDate future = now.plus(max);

        long toAdd = randomBetween(now.toDate().getTime(), future.toDate().getTime());

        return new LocalDate(now.toDate().getTime() + toAdd);
    }


    public LocalDateTime getFutureDateTime(ReadablePeriod max) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plus(max);

        long toAdd = randomBetween(now.toDate().getTime(), future.toDate().getTime());

        return new LocalDateTime(now.toDate().getTime() + toAdd);
    }


    public LocalDate getDateBetween(LocalDate date, ReadablePeriod period) {
        return null;
    }


    public LocalDateTime getDateTimeBetween(LocalDateTime date, ReadablePeriod later) {
        return null;
    }

    //endregion


    //region Character replacement

    //---------------------------------------------------------------------
    // Character replacement
    //---------------------------------------------------------------------

    /**
     * Replaces all {@code '?'} characters with random chars from [a - z] range
     *
     * @param letterString text to process
     * @return text with replaces {@code '?'} chars
     */
    public String letterify(String letterString) {
        return letterify(letterString, 'a', 'z');
    }

    /**
     * Replaces all {@code '?'} characters with random chars from [{@code from} - {@code to}] range
     *
     * @param letterString text to process
     * @param from         start of the range
     * @param to           end of the range
     * @return text with replaced {@code '?'} chars
     */
    public String letterify(String letterString, char from, char to) {
        return replaceSymbolWithCharsFromTo(letterString, '?', from, to);
    }


    /**
     * Replaces all {@code '#'} characters with random numbers from [0 - 9] range
     *
     * @param numberString text to process
     * @return text with replaced '#' characters
     */
    public String numerify(String numberString) {
        return numerify(numberString, 0, 9);
    }

    /**
     * Replaces all {@code '#'} characters with random numbers from [{@code from} - {@code to}] range
     *
     * @param numberString text to process
     * @param from         start of the range
     * @param to           end of the range
     * @return text with replaced '#' characters
     */
    public String numerify(String numberString, int from, int to) {
        return replaceSymbolWithCharsFromTo(numberString, '#', Character.forDigit(from, 10), Character.forDigit(to, 10));
    }

    /**
     * Processes text with {@code numerify()} and {@code letterify()} methods
     *
     * @param string text to process
     * @return text with replaced '#' and '?' characters
     */
    public String bothify(String string) {
        return letterify(numerify(string));
    }

    private String replaceSymbolWithCharsFromTo(String string, char symbol, char from, char to) {
        StringBuilder result = new StringBuilder();
        for (char aChar : string.toCharArray()) {
            if (aChar == symbol) {
                result.append(randomBetween(from, to));
            } else {
                result.append(aChar);
            }
        }
        return result.toString();
    }

    //endregion


    //region Random Entities

    //---------------------------------------------------------------------
    // Random Entities
    //---------------------------------------------------------------------

    //endregion


    private void initializeCache() {
        resourceListsCache =
                CacheBuilder.newBuilder()
                        .maximumSize(10)
                        .softValues()
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .build(
                                new CacheLoader<Integer, List>() {
                                    @Override
                                    public List load(Integer integer) throws Exception {
                                        return getListById(integer);
                                    }
                                }
                        );

    }

    private List getListById(int id) {
        BufferedReader bReader = null;
        StringBuilder sBuilder = new StringBuilder();
        String line = "";
        try {
            switch (id) {
                case FEMALES_LIST_ID: {
                    InputStream input = femaleNamesResource.getInputStream();
                    return getRandomValueFromList(bReader, input);
                }

                case MALES_LIST_ID: {
                    InputStream input = maleNamesResource.getInputStream();
                    return getRandomValueFromList(bReader, input);
                }

                case SURNAMES_LIST_ID: {
                    InputStream input = surNamesResource.getInputStream();
                    return getRandomValueFromList(bReader, input);
                }

                case EMAIL_LIST_ID: {
                    InputStream input = emailsResource.getInputStream();
                    return getRandomValueFromList(bReader, input);
                }

                case TELEPHONE_LIST_ID: {
                    InputStream input = phoneNumberResource.getInputStream();
                    return getRandomValueFromList(bReader, input);
                }

                case ADDRESS_LIST_ID: {
                    List<Address> addresses = Lists.newArrayList();
                    InputStream input = addressesResource.getInputStream();
                    List<String> rawAddresses = Lists.newArrayList();

                    StringWriter writer = new StringWriter();
                    IOUtils.copy(input, writer, charset);
                    String s = writer.toString();

                    String[] splitS = s.split("\\n");
                    for (String s1 : splitS) {
                        s1 = s1.replace("\\r", "");
                        rawAddresses.add(s1.trim());
                    }

                    Collections.shuffle(rawAddresses, random);

                    final Pattern pattern = Pattern.compile("(\\[.*\\])(\\[.*\\])");
//					DEBUG
//					int line = 1;
                    for (String rawAddress : rawAddresses) {
                        Matcher m = pattern.matcher(rawAddress);
                        if (m.find()) {
                            Address a = new Address();
                            String[] split = new String[]{m.group(1), m.group(2)};
                            String bracket = StringUtils.remove(split[0], "[");
                            bracket = StringUtils.remove(bracket, "]");
                            a.setFullAddress(bracket);
                            String[] split2 = StringUtils.split(split[1], "|");
                            a.setCity(split2[0].replace("[", ""));
                            a.setStateAbbr(split2[1]);
                            a.setStateName(split2[2]);
//							DEBUG:
//							System.out.println(line);
//							line++;
                            a.setZipCode(split2[3].replace("]", ""));
                            String street = StringUtils.remove(bracket, a.getStateAbbr());
                            street = StringUtils.remove(street, a.getZipCode());
                            street = StringUtils.remove(street, a.getCity() + ",");
                            a.setStreet(street.trim());
                            addresses.add(a);
                        }
                    }

                    getLoadingCache().put(ADDRESS_LIST_ID, addresses);
                    return addresses;
                }

                case STREETS_LIST_ID: {
                    InputStream input = streetsResource.getInputStream();
                    return getRandomValueFromList(bReader, input);
                }

                case WORD_LIST_ID: {
                    InputStream input = wordsResource.getInputStream();
                    return getRandomValueFromList(bReader, input);
                }

                case QUESTION_LIST_ID: {
                    InputStream input = questionsResource.getInputStream();
                    return getRandomValueFromList(bReader, input);
                }

                case MAC_ADDRESS_ID: {
                    InputStream input = macAddressResource.getInputStream();
                    return getRandomValueFromList(bReader, input);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private List getRandomValueFromList(BufferedReader bReader, InputStream input) throws IOException {
        String line;
        List<String> list = new ArrayList<>();
        try {
            bReader = new BufferedReader(new InputStreamReader(input));
            while((line = bReader.readLine()) != null) {
                list.add(line);
            }
        } finally {
            bReader.close();
        }

        Collections.shuffle(list, random);
        getLoadingCache().put(STREETS_LIST_ID, list);
        return list;
    }

    public static String getFileContents(String filename) {
        String line = "";
        String output = null;
        String ls = System.getProperty("line.separator");
        StringBuilder  stringBuilder = new StringBuilder();

        try {
            BufferedReader file = new BufferedReader(new FileReader(filename));
            while((line = file.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            file.close();
            return stringBuilder.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private LoadingCache<Integer, List> getLoadingCache() {
        return resourceListsCache;
    }

    //endregion

}
