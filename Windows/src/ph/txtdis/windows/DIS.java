package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.time.DateUtils;

public class DIS {
	public final static BigDecimal VAT;
	public final static String CURRENCY_SIGN;
	public final static String ITEM_FAMILY;
	public final static Date NO_SO_WITH_OVERDUE_CUTOFF;
	public final static Date SI_MUST_HAVE_SO_CUTOFF;
	public final static Date CLOSED_DSR_BEFORE_SO_CUTOFF;
	public final static Integer VENDOR_ITEM_ID_MINIMUM_LENGTH;
	public final static Date TODAY;
	static {
		// @sql:on			
		VAT = (BigDecimal)  new Data().getDatum("" 
				+ "SELECT (1 + value) AS vat " 
				+ "  FROM default_number "
				+ " WHERE name = 'VAT'; "
				);
		
		CURRENCY_SIGN = (String)  new Data().getDatum("" 
				+ "SELECT value " 
				+ "  FROM default_text "
				+ " WHERE name = 'CURRENCY' "
				);
		
		NO_SO_WITH_OVERDUE_CUTOFF = (Date) new Data().getDatum("" 
				+ "SELECT value " 
				+ "  FROM default_date "
				+ " WHERE name = $$No-S/O-with-overdue cutoff$$ "
				);
		SI_MUST_HAVE_SO_CUTOFF =  (Date) new Data().getDatum("" 
				+ "SELECT value " 
				+ "  FROM default_date "
				+ " WHERE name = $$S/I-must-have-S/O cutoff$$ "
				);
		CLOSED_DSR_BEFORE_SO_CUTOFF = (Date) new Data().getDatum("" 
				+ "SELECT value " 
				+ "  FROM default_date "
				+ " WHERE name = $$DSR-closed-before-an-S/O cutoff$$ "
				);
		
		ITEM_FAMILY = (String) new Data().getDatum(""
				+ "SELECT value " 
				+ "  FROM default_text "
		        + " WHERE name = $$ITEM FAMILY$$ "
				); 
		
		VENDOR_ITEM_ID_MINIMUM_LENGTH = (Integer) new Data().getDatum(""
				+ "SELECT value " 
				+ "  FROM default_number "
		        + " WHERE name = $$VENDOR ITEM ID MINIMUM LENGTH$$ "
				); 
		
		TODAY = new Date(Calendar.getInstance().getTimeInMillis());
		// @sql:off
	}
	
	// VERSION
	public final static String VERSION = "0.9.35.04";

	// REPORT OPTIONS
	public final static int ROUTE = 0;
	public final static int STT = 0;

	// NUMBER FORMAT
	public final static DecimalFormat NO_COMMA_DECIMAL = new DecimalFormat("0.00;(0.00)");
	public final static DecimalFormat TWO_PLACE_DECIMAL = new DecimalFormat("#,##0.00;(#,##0.00)");
	public final static DecimalFormat FOUR_PLACE_DECIMAL = new DecimalFormat("#,##0.0000;(#,##0.0000)");

	public final static DecimalFormat INTEGER = new DecimalFormat("#,##0;(#,##0)");
	public final static DecimalFormat NO_COMMA_INTEGER = new DecimalFormat("0;(0)");
	public final static SimpleDateFormat STANDARD_DATE = new SimpleDateFormat("MM/dd/yy");
	public final static SimpleDateFormat POSTGRES_DATE = new SimpleDateFormat("yyyy-MM-dd");
	public final static SimpleDateFormat LONG_DATE = new SimpleDateFormat("MMM dd, yyyy");
	public final static SimpleDateFormat TIME = new SimpleDateFormat("HH:mm");

	// CONSTANTS
	public final static BigDecimal HUNDRED = new BigDecimal(100);
	
	public final static Date TOMORROW = new Date(DateUtils.addDays(TODAY, 1).getTime());
	public final static Date YESTERDAY = new Date(DateUtils.addDays(TODAY, -1).getTime());
	public final static Date DAY_BEFORE_YESTERDAY = new Date(DateUtils.addDays(TODAY, -2).getTime());
	
	public final static Date LAST_MONTH = new Date(DateUtils.addMonths(TODAY, -1).getTime());
	public final static Date FIRST_OF_LAST_MONTH = new Date(DateUtils.setDays(LAST_MONTH, 1).getTime());
	
	public final static Time ZERO_TIME = parseTime("00:00");
	public final static Date FAR_FUTURE = parseDate("9999-12-31");
	public final static Date FAR_PAST = parseDate("0001-01-01");

	// DATE INPUT OPTION
	public final static int DATEFROM = 1;
	public final static int DATEFROMTO = 2;
	public final static int DATETO = 3;

	// CUTOFF DATES
	public final static Date OVERDUE_CUTOFF = parseDate("2013-03-01");
	public final static Date BALANCE_CUTOFF = parseDate("2013-06-27");
	public final static Date SI_WITH_SO_CUTOFF = parseDate("2013-06-30");
	//public final static Date CLOSURE_BEFORE_SO_CUTOFF = parseDate("2014-08-13");
	public final static Date CLOSURE_BEFORE_SO_CUTOFF = parseDate("2013-08-13");

	// HELPER METHODS
	public static int dayToday() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(TODAY);
		return cal.get(Calendar.DAY_OF_WEEK);
	}
	
	public static boolean isSunday(Date date) {
		if (dayToday() == Calendar.SUNDAY)
			return true;
		else
			return false;
	}

	public static boolean isMonday(Date date) {
		if (dayToday() == Calendar.MONDAY)
			return true;
		else
			return false;
	}
	
	public static Date parseDate(String strDate) {
		try {
			return new Date(POSTGRES_DATE.parse(strDate).getTime());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Time parseTime(String strTime) {
		try {
			return new Time(TIME.parse(strTime).getTime());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static BigDecimal parseBigDecimal(String text) {
		text = text.trim();
		if(text.equals("-") ||  text.isEmpty())
			return null;
		text = text.replace(",", "").replace("(", "-").replace(")", "");
		return new BigDecimal(text);
    }
}
