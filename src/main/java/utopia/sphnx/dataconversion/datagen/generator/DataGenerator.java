package utopia.sphnx.dataconversion.datagen.generator;

import ch.qos.logback.core.spi.LogbackLock;
import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.rmi.server.UID;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created by heyto on 8/23/2017.
 */
public class DataGenerator {
    private Random random = null;
    private static PeriodFormatter yearMonthDayFormatter = (new PeriodFormatterBuilder()).appendYears().appendSuffix(" year", " years").appendSeparator(", ").appendMonths().appendSuffix(" month", " months").appendSeparator(" and ").appendDays().appendSuffix(" day", " days").toFormatter();
    private static final LogbackLock lock = new LogbackLock();
    private static final Charset DEFAULT_CHARSET;
    private static final String DEFAULT_STRING_FORMAT = "MM/dd/yyyy";
    private static utopia.sphnx.dataconversion.datagen.generator.DataGenerator instance;
    private static final Resource addressesResource;
    private static final Resource streetsResource;
    private static final Resource phoneNumberResource;
    private static final Resource urlsResource;
    private static final Resource creditCardsResource;
    private static final Resource emailsResource;
    private static final Resource femaleNamesResource;
    private static final Resource maleNamesResource;
    private static final Resource surNamesResource;
    private static final Resource wordsResource;
    private static final Resource questionsResource;
    private static final Resource macAddressResource;
    private static final Resource countriesResource;
    private static final Resource companiesResources;
    private static ImmutableList<String> URL_HOSTS;
    private static LoadingCache<Integer, List> resourceListsCache;
    private static Charset charset;

    private DataGenerator(Long seed) {
        if(seed == null) {
            this.random = new Random();
        } else {
            this.random = new Random(seed.longValue());
        }

        this.initializeCache();
    }

    public static utopia.sphnx.dataconversion.datagen.generator.DataGenerator getInstance() {
        if(instance == null) {
            LogbackLock var0 = lock;
            synchronized(lock) {
                if(instance == null) {
                    instance = new utopia.sphnx.dataconversion.datagen.generator.DataGenerator((Long)null);
                }
            }
        }

        return instance;
    }

    public static utopia.sphnx.dataconversion.datagen.generator.DataGenerator getInstance(Long seed) {
        if(instance == null) {
            LogbackLock var1 = lock;
            synchronized(lock) {
                if(instance == null) {
                    instance = new utopia.sphnx.dataconversion.datagen.generator.DataGenerator(seed);
                }
            }
        } else {
            instance.random = new Random(seed.longValue());
        }

        return instance;
    }

    public void clean() {
        if(null != resourceListsCache) {
            resourceListsCache.cleanUp();
        }

    }

    public void setDefaultCharset() {
    }

    public Pair<String, Gender> getFirstName() {
        String n;
        if(this.random.nextBoolean()) {
            n = this.getGenderFirstName(Gender.FEMALE);
            return new ImmutablePair(n, Gender.FEMALE);
        } else {
            n = this.getGenderFirstName(Gender.MALE);
            return new ImmutablePair(n, Gender.MALE);
        }
    }

    public String getGenderFirstName(Gender gender) {
        try {
            List males;
            if(gender.equals(Gender.FEMALE)) {
                males = (List)this.getLoadingCache().get(Integer.valueOf(1));
                return (String)String.class.cast(males.get(this.random.nextInt(males.size())));
            } else {
                males = (List)this.getLoadingCache().get(Integer.valueOf(2));
                return (String)String.class.cast(males.get(this.random.nextInt(males.size())));
            }
        } catch (ExecutionException var3) {
            var3.printStackTrace();
            return "";
        }
    }

    public Pair<String, Gender> getMiddleName() {
        return this.getFirstName();
    }

    public String getGenderMiddleName(Gender gender) {
        return this.getGenderFirstName(gender);
    }

    public String getName() {
        return this.getFirstName() + " " + this.getLastName();
    }

    public String getName(Gender gender) {
        return this.getGenderFirstName(gender) + " " + this.getLastName();
    }

    public String getLastName() {
        try {
            List surnames = (List)this.getLoadingCache().get(Integer.valueOf(3));
            return (String)String.class.cast(surnames.get(this.random.nextInt(surnames.size())));
        } catch (ExecutionException var2) {
            var2.printStackTrace();
            return "";
        }
    }

    public String getFQName() {
        return this.getFirstName() + " " + this.getMiddleName() + " " + this.getLastName();
    }

    public String getFQName(Gender gender) {
        return this.getGenderFirstName(gender) + " " + this.getGenderMiddleName(gender) + " " + this.getLastName();
    }

    public Address getAddress() {
        try {
            List addresses = (List)this.getLoadingCache().get(Integer.valueOf(6));
            Collections.shuffle(addresses);
            String a = (String ) addresses.get(this.random.nextInt(addresses.size()));
            Address add =new Address();
            String addr = a.split("]") [0].replace("[","");
            add.setFullAddress(addr);
            String addr1 = a.split("]")[1].replace("[","");
            String street = addr.split(",")[0];
            String city = addr1.split("\\|")[0];
            String stateAbbr = addr1.split("\\|")[1];
            String state = addr1.split("\\|")[2];
            String zip = addr1.split("\\|")[3];
            add.setStreet(street);
            add.setCity(city);
            add.setStateAbbr(stateAbbr);
            add.setStateName(state);
            add.setZipCode(zip);
            return add;


        } catch (ExecutionException var2) {
            var2.printStackTrace();
            return Address.DEFAULT_ADDRESS;
        }
    }

    public Address getFirstAddress(Predicate<Address> predicate) {
        try {
            List<Address> addresses = (List)this.getLoadingCache().get(Integer.valueOf(6));
            Collections.shuffle(addresses);
            Supplier<Address> su = () -> {
                return Address.DEFAULT_ADDRESS;
            };
            return (Address)addresses.stream().filter(predicate).findFirst().orElseGet(() -> {
                return Address.DEFAULT_ADDRESS;
            });
        } catch (ExecutionException var4) {
            var4.printStackTrace();
            return Address.DEFAULT_ADDRESS;
        }
    }

    public String getStreet() {
        try {
            List streets = (List)this.getLoadingCache().get(Integer.valueOf(7));
            Collections.shuffle(streets);
            return (String)streets.get(this.random.nextInt(streets.size()));
        } catch (ExecutionException var2) {
            var2.printStackTrace();
            return "Kenmore Ave. Unit 2E";
        }
    }

    public String getEmailAddress() {
        try {
            List emails = (List)this.getLoadingCache().get(Integer.valueOf(4));
            return (String)String.class.cast(emails.get(this.random.nextInt(emails.size())));
        } catch (ExecutionException var2) {
            var2.printStackTrace();
            return "ab.cd@example.com";
        }
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
            List emails = (List)this.getLoadingCache().get(Integer.valueOf(5));
            return (String)String.class.cast(emails.get(this.random.nextInt(emails.size())));
        } catch (ExecutionException var2) {
            var2.printStackTrace();
            return "(123) 935-1340";
        }
    }

    public String getCompany() {
        try {
            List companies = (List)this.getLoadingCache().get(Integer.valueOf(12));
            return (String)String.class.cast(companies.get(this.random.nextInt(companies.size())));
        } catch (ExecutionException var2) {
            var2.printStackTrace();
            return "TESLA";
        }
    }

    public Years getApproxAge(LocalDateTime birthDate) {
        return birthDate != null?Years.yearsBetween(birthDate, LocalDateTime.now()):Years.ZERO;
    }

    public String getAge(LocalDateTime birthDate) {
        Period p = new Period(birthDate, LocalDateTime.now());
        return yearMonthDayFormatter.print(p.toPeriod());
    }

    public boolean randomBoolean() {
        return this.random.nextBoolean();
    }

    public String randomBoolean(String trueValue, String falseValue) {
        boolean b = this.randomBoolean();
        return BooleanUtils.toString(b, trueValue, falseValue);
    }

    public char randomBetween(char min, char max) {
        return (char)this.randomBetween((int)min, (int)max);
    }

    public long randomBetween(long startInclusive, long endExclusive) {
        return RandomUtils.nextLong(startInclusive, endExclusive);
    }

    public long randomLong() {
        return this.randomBetween(0L, 9223372036854775807L);
    }

    public int randomBetween(int startInclusive, int endExclusive) {
        return RandomUtils.nextInt(startInclusive, endExclusive);
    }

    public int randomInt() {
        return this.randomBetween((int)0, (int)2147483647);
    }

    public byte[] randomBytes(int count) {
        byte[] result = new byte[count];
        this.random.nextBytes(result);
        return result;
    }

    public double randomBetween(double startInclusive, double endInclusive) {
        return RandomUtils.nextDouble(startInclusive, endInclusive);
    }

    public double randomDouble() {
        return this.randomBetween(0.0D, 1.7976931348623157E308D);
    }

    public float randomBetween(float startInclusive, float endInclusive) {
        return RandomUtils.nextFloat(startInclusive, endInclusive);
    }

    public float randomFloat() {
        return this.randomBetween(0.0F, 3.4028235E38F);
    }

    public <T> T randomElement(List<T> elements) {
        return elements.get(this.randomBetween((int)0, (int)(elements.size() - 1)));
    }

    public <T> T randomElement(T... elements) {
        return this.randomElement(Arrays.asList(elements));
    }

    public <T extends Enum<?>> T randomElement(Class<T> enumType) {
        return (T) (enumType.getEnumConstants() )[randomBetween((int)0, (int)(enumType.getEnumConstants()).length - 1)];
    }

    public <T> List<T> randomElements(List<T> elements, int count) {
        Collections.shuffle(elements, this.random);
        return elements.subList(0, count % elements.size());
    }

    public <T> List<T> randomElements(int count, T... elements) {
        return this.randomElements(Arrays.asList(elements), count);
    }

    public String randomUUID() {
        return UUID.randomUUID().toString();
    }

    public String randomUserId() {
        return (new UID()).toString();
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

    public String getTitle(int min, int max) {
        return this.getWords(min, max, true);
    }

    public String getTitle(int count) {
        return this.getWords(count, count, true);
    }

    public String getQuestion() {
        try {
            List questions = (List)this.getLoadingCache().get(Integer.valueOf(9));
            return (String)String.class.cast(questions.get(this.random.nextInt(questions.size())));
        } catch (ExecutionException var2) {
            var2.printStackTrace();
            return "Do you have pets?";
        }
    }

    public String getHtmlParagraphs(int min, int max) {
        return null;
    }

    public String getWords(int count) {
        return this.getWords(count, count, false);
    }

    public String getWords(int min, int max) {
        return this.getWords(min, max, false);
    }

    private String getWords(int min, int max, boolean title) {
        int count = this.getCount(min, max);
        return this.getWords(count, title);
    }

    private String getWords(int count, boolean title) {
        try {
            List words = (List)this.getLoadingCache().get(Integer.valueOf(4));
            StringBuilder sb = new StringBuilder();
            int size = words.size();

            for(int wordCount = 0; wordCount < count; ++wordCount) {
                String word = (String)words.get(this.random.nextInt(size));
                if(title && (wordCount == 0 || word.length() > 3)) {
                    word = word.substring(0, 1).toUpperCase() + word.substring(1);
                }

                sb.append(word);
                sb.append("");
            }

            return sb.toString().trim();
        } catch (ExecutionException var8) {
            var8.printStackTrace();
            return RandomStringUtils.random(count);
        }
    }

    private int getCount(int min, int max) {
        if(min < 0) {
            min = 0;
        }

        if(max < min) {
            max = min;
        }

        return max != min?this.random.nextInt(max - min) + min:min;
    }

    public String getParagraphs(int min, int max) {
        int count = this.getCount(min, max);
        StringBuilder sb = new StringBuilder();

        for(int j = 0; j < count; ++j) {
            int sentences = this.random.nextInt(5) + 2;

            for(int i = 0; i < sentences; ++i) {
                String first = this.getWords(1, 1, false);
                first = first.substring(0, 1).toUpperCase() + first.substring(1);
                sb.append(first);
                sb.append(this.getWords(2, 20, false));
                sb.append(".  ");
            }

            sb.append("\n");
        }

        return sb.toString().trim();
    }

    public LocalDateTime getToday() {
        return LocalDateTime.now();
    }

    public LocalDateTime getTodayMinus(int num, String time) {
        LocalDateTime today = LocalDateTime.now();
        String var4 = time.toUpperCase();
        byte var5 = -1;
        switch(var4.hashCode()) {
            case 68:
                if(var4.equals("D")) {
                    var5 = 0;
                }
                break;
            case 77:
                if(var4.equals("M")) {
                    var5 = 2;
                }
                break;
            case 87:
                if(var4.equals("W")) {
                    var5 = 1;
                }
                break;
            case 89:
                if(var4.equals("Y")) {
                    var5 = 3;
                }
        }

        switch(var5) {
            case 0:
                return today.minusDays(num);
            case 1:
                return today.minusWeeks(num);
            case 2:
                return today.minusMonths(num);
            case 3:
                return today.minusYears(num);
            default:
                return today.minusYears(num);
        }
    }

    public LocalDateTime getTodayPlus(int num, String time) {
        LocalDateTime today = LocalDateTime.now();
        String var4 = time.toUpperCase();
        byte var5 = -1;
        switch(var4.hashCode()) {
            case 68:
                if(var4.equals("D")) {
                    var5 = 0;
                }
                break;
            case 77:
                if(var4.equals("M")) {
                    var5 = 2;
                }
                break;
            case 87:
                if(var4.equals("W")) {
                    var5 = 1;
                }
                break;
            case 89:
                if(var4.equals("Y")) {
                    var5 = 3;
                }
        }

        switch(var5) {
            case 0:
                return today.plusDays(num);
            case 1:
                return today.plusWeeks(num);
            case 2:
                return today.plusMonths(num);
            case 3:
                return today.plusYears(num);
            default:
                return today.plusYears(num);
        }
    }

    public LocalDateTime getDateTimeBetween(long early, long later) {
        LocalDateTime today = LocalDateTime.now();
        Long e = Long.valueOf(early);
        Long l = Long.valueOf(later);
        LocalDateTime newestDate = today.minusYears(e.intValue());
        LocalDateTime olderDate = today.minusYears(l.intValue());
        return this.getDateTimeBetween(newestDate, olderDate);
    }

    public LocalDateTime getDateTimeBetween(LocalDateTime early, LocalDateTime later) {
        LocalDateTime today = LocalDateTime.now();
        Long diff = Long.valueOf(early.toDate().getTime() - later.toDate().getTime() + 1L);
        Long rnd = Long.valueOf(this.randomBetween(0L, diff.longValue()));
        Long newTimestamp = Long.valueOf(later.toDate().getTime() + rnd.longValue());
        return new LocalDateTime(newTimestamp);
    }

    public LocalDate getDateBetween(long early, long later) {
        LocalDate today = LocalDate.now();
        Long e = Long.valueOf(early);
        Long l = Long.valueOf(later);
        LocalDate newestDate = today.minusYears(e.intValue());
        LocalDate olderDate = today.minusYears(l.intValue());
        return this.getDateBetween(newestDate, olderDate);
    }

    public LocalDate getDateBetween(LocalDate early, LocalDate later) {
        LocalDateTime today = LocalDateTime.now();
        Long diff = Long.valueOf(early.toDate().getTime() - later.toDate().getTime() + 1L);
        Long rnd = Long.valueOf(this.randomBetween(0L, diff.longValue()));
        Long newTimestamp = Long.valueOf(later.toDate().getTime() + rnd.longValue());
        return new LocalDate(newTimestamp);
    }

    public LocalDate getPastDate(ReadablePeriod max) {
        LocalDate now = LocalDate.now();
        LocalDate past = now.minus(max);
        long toAdd = this.randomBetween(past.toDate().getTime(), now.toDate().getTime());
        return new LocalDate(past.toDate().getTime() + toAdd);
    }

    public LocalDateTime getPastDateTime(ReadablePeriod max) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = now.minus(max);
        long toAdd = this.randomBetween(past.toDate().getTime(), now.toDate().getTime());
        return new LocalDateTime(past.toDate().getTime() + toAdd);
    }

    public LocalDate getFutureDate(ReadablePeriod max) {
        LocalDate now = LocalDate.now();
        LocalDate future = now.plus(max);
        long toAdd = this.randomBetween(now.toDate().getTime(), future.toDate().getTime());
        return new LocalDate(now.toDate().getTime() + toAdd);
    }

    public LocalDateTime getFutureDateTime(ReadablePeriod max) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plus(max);
        long toAdd = this.randomBetween(now.toDate().getTime(), future.toDate().getTime());
        return new LocalDateTime(now.toDate().getTime() + toAdd);
    }

    public LocalDate getDateBetween(LocalDate date, ReadablePeriod period) {
        return null;
    }

    public LocalDateTime getDateTimeBetween(LocalDateTime date, ReadablePeriod later) {
        return null;
    }

    public String letterify(String letterString) {
        return this.letterify(letterString, 'a', 'z');
    }

    public String letterify(String letterString, char from, char to) {
        return this.replaceSymbolWithCharsFromTo(letterString, '?', from, to);
    }

    public String numerify(String numberString) {
        return this.numerify(numberString, 0, 9);
    }

    public String numerify(String numberString, int from, int to) {
        return this.replaceSymbolWithCharsFromTo(numberString, '#', Character.forDigit(from, 10), Character.forDigit(to, 10));
    }

    public String bothify(String string) {
        return this.letterify(this.numerify(string));
    }

    private String replaceSymbolWithCharsFromTo(String string, char symbol, char from, char to) {
        StringBuilder result = new StringBuilder();
        char[] var6 = string.toCharArray();
        int var7 = var6.length;

        for(int var8 = 0; var8 < var7; ++var8) {
            char aChar = var6[var8];
            if(aChar == symbol) {
                result.append(this.randomBetween(from, to));
            } else {
                result.append(aChar);
            }
        }

        return result.toString();
    }

    public String getUrl() {
        StringBuilder sb = new StringBuilder();
        int hostId = this.random.nextInt(URL_HOSTS.size());
        String host = String.format((String)URL_HOSTS.get(hostId), new Object[]{this.getWords(1)});
        sb.append(host);
        return sb.toString();
    }

    public String getMacAddress() {
        return null;
    }

    public CreditCard creditCardNumber() {
        try {
            List creditCards = (List)this.getLoadingCache().get(Integer.valueOf(10));
            return (CreditCard) CreditCard.class.cast(creditCards.get(this.random.nextInt(creditCards.size())));
        } catch (ExecutionException var2) {
            var2.printStackTrace();
            return CreditCard.DEFAULT_CREDIT_CARD;
        }
    }

    public CreditCard creditCardNumber(CreditCard.CreditCardType type) {
        return null;
    }

    private void initializeCache() {
        resourceListsCache = CacheBuilder.newBuilder().maximumSize(10L).softValues().expireAfterWrite(5L, TimeUnit.MINUTES).build(new CacheLoader<Integer, List>() {
            public List load(Integer integer) throws Exception {
                return getListById(integer.intValue());
            }
        });
    }

    private List getListById(int id) {
        BufferedReader bReader = null;
        new StringBuilder();
        String var4 = "";

        try {
            InputStream input;
            switch(id) {
                case 1:
                    input = femaleNamesResource.getInputStream();
                    return this.getList(1, (BufferedReader)bReader, input);
                case 2:
                    input = maleNamesResource.getInputStream();
                    return this.getList(2, (BufferedReader)bReader, input);
                case 3:
                    input = surNamesResource.getInputStream();
                    return this.getList(7, (BufferedReader)bReader, input);
                case 4:
                    input = emailsResource.getInputStream();
                    return this.getList(4, (BufferedReader)bReader, input);
                case 5:
                    input = phoneNumberResource.getInputStream();
                    return this.getList(5, (BufferedReader)bReader, input);
                case 6:
                    input = addressesResource.getInputStream();
                    return this.getList(6, bReader, input);
                case 10:
                default:
                    break;
                case 7:
                    input = streetsResource.getInputStream();
                    return this.getList(7, (BufferedReader)bReader, input);
                case 8:
                    input = wordsResource.getInputStream();
                    return this.getList(8, (BufferedReader)bReader, input);
                case 9:
                    input = questionsResource.getInputStream();
                    return this.getList(9, (BufferedReader)bReader, input);
                case 11:
                    input = macAddressResource.getInputStream();
                    return this.getList(11, (BufferedReader)bReader, input);
                case 12:
                    input = companiesResources.getInputStream();
                    return this.getList(12, bReader, input);
            }
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        return null;
    }

    private List getList(int ID, BufferedReader bReader, InputStream input) throws Exception {
        ArrayList list = new ArrayList();

        try {
            bReader = new BufferedReader(new InputStreamReader(input,"utf-8"));

            String line;
            while((line = bReader.readLine()) != null) {
                if(bReader.ready()) {
                    list.add(line);
                }
            }
        } finally {
            try {
                if(bReader != null) {
                    bReader.close();
                }
            } catch (IOException var12) {
                ;
            }

        }

        Collections.shuffle(list, this.random);
        this.getLoadingCache().put(Integer.valueOf(ID), list);
        this.getLoadingCache().cleanUp();
        return list;
    }

    private LoadingCache<Integer, List> getLoadingCache() {
        return resourceListsCache;
    }

    static {
        DEFAULT_CHARSET = Charsets.ISO_8859_1;
        addressesResource = new ClassPathResource("addresses.txt");
        streetsResource = new ClassPathResource("streets.txt");
        phoneNumberResource = new ClassPathResource("phones.txt");
        urlsResource = new ClassPathResource("urls.txt");
        creditCardsResource = new ClassPathResource("credit_cards.txt");
        emailsResource = new ClassPathResource("emails.txt");
        femaleNamesResource = new ClassPathResource("female_names.txt");
        maleNamesResource = new ClassPathResource("male_names.txt");
        surNamesResource = new ClassPathResource("surnames.txt");
        wordsResource = new ClassPathResource("words.txt");
        questionsResource = new ClassPathResource("questions.txt");
        macAddressResource = new ClassPathResource("mac_address.txt");
        companiesResources = new ClassPathResource("companies.txt");
        countriesResource = new ClassPathResource("countries.txt");
        URL_HOSTS = ImmutableList.of("https://www.google.com/#q=%s", "http://www.bing.com/search?q=%s", "https://search.yahoo.com/search?p=%s", "https://duckduckgo.com/?q=%s");
        charset = DEFAULT_CHARSET;
    }

    public static enum Gender {
        MALE,
        FEMALE;

        private Gender() {
        }
    }
}
