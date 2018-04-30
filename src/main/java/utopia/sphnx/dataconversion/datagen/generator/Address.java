package utopia.sphnx.dataconversion.datagen.generator;

/**
 * Created by heyto on 8/23/2017.
 */
public class Address {
    public static utopia.sphnx.dataconversion.datagen.generator.Address DEFAULT_ADDRESS = new utopia.sphnx.dataconversion.datagen.generator.Address() {
        public String getStreet() {
            return "95A West-Division St.";
        }

        public String getStateName() {
            return "Illinois";
        }

        public String getStateAbbr() {
            return "IL";
        }

        public String getZipCode() {
            return "60616";
        }

        public String getFullAddress() {
            return "95A West-Division St. Chicago, IL, 606160";
        }

        public String getCity() {
            return "Chicago";
        }
    };
    private String stateName;
    private String street;
    private String stateAbbr;
    private String city;
    private String zipCode;
    private String fullAddress;

    public Address() {
    }

    public String getStreet() {
        return this.street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStateName() {
        return this.stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getStateAbbr() {
        return this.stateAbbr;
    }

    public void setStateAbbr(String stateAbbr) {
        this.stateAbbr = stateAbbr;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return this.zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getFullAddress() {
        return this.fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String toString() {
        return this.fullAddress;
    }
}
