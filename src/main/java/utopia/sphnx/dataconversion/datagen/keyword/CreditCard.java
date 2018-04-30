package utopia.sphnx.dataconversion.datagen.keyword;


/**
 * @author <a href="mailto:solmarkn@gmail.com">Dani Vainstein</a>
 * @version %I%, %G%
 * @since 2.0
 */
public class CreditCard
{
	//region initialization and constructors section
	
	private String number;
	
	private String creditCardName;
	
	private CreditCardType creditCardType;
	
	public static final utopia.sphnx.dataconversion.datagen.keyword.CreditCard DEFAULT_CREDIT_CARD = new utopia.sphnx.dataconversion.datagen.keyword.CreditCard()
	{
		@Override
		public String getNumber()
		{
			return "4556 3447 5526 0439";
		}
		
		@Override
		public CreditCardType getCreditCardType()
		{
			return CreditCardType.VISA;
		}
	};
	
	
	public enum CreditCardType
	{
		MASTERCARD, VISA, AMEX, DISCOVER, AMEX_CORPORATE, DINERS, JCB
	}
	
	public CreditCard()
	{
		super();
	}
	
	//endregion
	
	
	public String getNumber()
	{
		return number;
	}
	
	public void setNumber( String number )
	{
		this.number = number;
	}
	
	public CreditCardType getCreditCardType()
	{
		return creditCardType;
	}
	
	private CreditCardType getCreditCardType( String type )
	{
		if( type.endsWith( "MasterCard" ) )
		{
			return CreditCardType.MASTERCARD;
		}
		if( type.endsWith( "Visa" ) )
		{
			return CreditCardType.VISA;
		}
		if( type.endsWith( "Express" ) )
		{
			return CreditCardType.AMEX;
		}
		if( type.endsWith( "Discover" ) )
		{
			return CreditCardType.DISCOVER;
		}
		if( type.endsWith( "Diners Club" ) )
		{
			return CreditCardType.DINERS;
		}
		if( type.endsWith( "JCB" ) )
		{
			return CreditCardType.JCB;
		}
		if( type.endsWith( "Express Corporate" ) )
		{
			return CreditCardType.AMEX_CORPORATE;
		}
		
		return CreditCardType.VISA;
		
	}
	
	
	private void setCreditCardType( CreditCardType creditCardType )
	{
		this.creditCardType = creditCardType;
	}
	public void setCreditCardName( String name )
	{
		this.creditCardName = name;
		setCreditCardType( getCreditCardType( name ) );
	}
	
}
