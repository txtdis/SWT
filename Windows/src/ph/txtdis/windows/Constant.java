package ph.txtdis.windows;

import java.math.BigDecimal;

public class Constant {
	private static Constant constant;
	private BigDecimal vat;
	private String currencySign;

	private Constant() {

	}

	public static Constant getInstance() {
		if (constant == null) {
			constant = new Constant();
		}
		return constant;
	}

	public BigDecimal getVat() {
		if (vat == null)
			// @sql:on			
			vat = (BigDecimal) new Data().getDatum("" 
					+ "SELECT (1 + value) AS vat " 
					+ "  FROM default_number "
			        + " WHERE name = 'VAT'; ");
			// @sql:off
		return vat;
	}

	public String getCurrencySign() {
		// @sql:on			
		if (currencySign == null)
			currencySign = (String) new Data().getDatum("" 
					+ "SELECT value " 
					+ "  FROM default_text "
			        + " WHERE name = 'CURRENCY' ");
		// @sql:on			
		return currencySign;
	}
}
