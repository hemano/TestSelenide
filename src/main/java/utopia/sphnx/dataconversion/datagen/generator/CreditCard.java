package utopia.sphnx.dataconversion.datagen.generator;

/**
 * Created by heyto on 8/23/2017.
 */
public class CreditCard {
    private String number;
    private String creditCardName;
    private CreditCardType creditCardType;
    public static final utopia.sphnx.dataconversion.datagen.generator.CreditCard DEFAULT_CREDIT_CARD = new utopia.sphnx.dataconversion.datagen.generator.CreditCard() {
        public String getNumber() {
            return "4556 3447 5526 0439";
        }

        public CreditCardType getCreditCardType() {
            return CreditCardType.VISA;
        }
    };

    public CreditCard() {
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public CreditCardType getCreditCardType() {
        return this.creditCardType;
    }

    private CreditCardType getCreditCardType(String type) {
        return type.endsWith("MasterCard")? CreditCardType.MASTERCARD:(type.endsWith("Visa")? CreditCardType.VISA:(type.endsWith("Express")? CreditCardType.AMEX:(type.endsWith("Discover")? CreditCardType.DISCOVER:(type.endsWith("Diners Club")? CreditCardType.DINERS:(type.endsWith("JCB")? CreditCardType.JCB:(type.endsWith("Express Corporate")? CreditCardType.AMEX_CORPORATE: CreditCardType.VISA))))));
    }

    private void setCreditCardType(CreditCardType creditCardType) {
        this.creditCardType = creditCardType;
    }

    public void setCreditCardName(String name) {
        this.creditCardName = name;
        this.setCreditCardType(this.getCreditCardType(name));
    }

    public static enum CreditCardType {
        MASTERCARD,
        VISA,
        AMEX,
        DISCOVER,
        AMEX_CORPORATE,
        DINERS,
        JCB;

        private CreditCardType() {
        }
    }
}
