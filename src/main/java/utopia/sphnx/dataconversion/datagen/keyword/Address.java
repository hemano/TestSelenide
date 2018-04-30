package utopia.sphnx.dataconversion.datagen.keyword;


public class Address
{
	
	//region initialization and constructors section
	
	public static utopia.sphnx.dataconversion.datagen.keyword.Address DEFAULT_ADDRESS = new utopia.sphnx.dataconversion.datagen.keyword.Address()
	{
		@Override
		public String getStreet()
		{
			return "95A West-Division St.";
		}
		
		@Override
		public String getStateName()
		{
			return "Illinois";
		}
		
		@Override
		public String getStateAbbr()
		{
			return "IL";
		}
		
		@Override
		public String getZipCode()
		{
			return "60616";
		}
		
		@Override
		public String getFullAddress()
		{
			return "95A West-Division St. Chicago, IL, 606160";
		}
		
		@Override
		public String getCity()
		{
			return "Chicago";
		}
	};
	
	
	private String stateName;
	
	private String street;
	
	private String stateAbbr;
	
	private String city;
	
	private String zipCode;
	
	private String fullAddress;
	
	public Address()
	{
		super();
	}
	
	//endregion
	
	
	
	public String getStreet()
	{
		return street;
	}
	
	public void setStreet( String street )
	{
		this.street = street;
	}
	
	public String getStateName()
	{
		return stateName;
	}
	
	public void setStateName( String stateName )
	{
		this.stateName = stateName;
	}
	
	public String getStateAbbr()
	{
		return stateAbbr;
	}
	
	public void setStateAbbr( String stateAbbr )
	{
		this.stateAbbr = stateAbbr;
	}
	
	public String getCity()
	{
		return city;
	}
	
	public void setCity( String city )
	{
		this.city = city;
	}
	
	public String getZipCode()
	{
		return zipCode;
	}
	
	public void setZipCode( String zipCode )
	{
		this.zipCode = zipCode;
	}
	
	public String getFullAddress()
	{
		return fullAddress;
	}
	
	public void setFullAddress( String fullAddress )
	{
		this.fullAddress = fullAddress;
	}
	
	@Override
	public String toString()
	{
		return fullAddress;
	}
}
