package ph.txtdis.windows;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Time;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.time.DateUtils;

public class DIS {
	public final static BigDecimal VAT;
	public final static String CURRENCY_SIGN, ITEM_FAMILY, SERVER_VERSION;
	public final static Date NO_SO_WITH_OVERDUE_CUTOFF, SI_MUST_HAVE_SO_CUTOFF, CLOSED_DSR_BEFORE_SO_CUTOFF, TODAY;
	public final static Integer VENDOR_ITEM_ID_MINIMUM_LENGTH, MAIN_CASHIER, BRANCH_CASHIER, MONETARY,
	        SALARY_DEDUCTION, SALARY_CREDIT, EWT, LISTING_FEE, DISPLAY_ALLOWANCE, DEALERS_INCENTIVE, PRINCIPAL, SERVER_TIMEZONE,
	        LEAD_TIME;
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
				+ "SELECT CASE WHEN value IS NULL THEN 'epoch' ELSE value END\n" 
				+ "  FROM default_date "
				+ " WHERE name = $$NO-S/O-WITH-OVERDUE CUTOFF$$ "
				);
		SI_MUST_HAVE_SO_CUTOFF =  (Date) new Data().getDatum("" 
				+ "SELECT CASE WHEN value IS NULL THEN 'epoch' ELSE value END\n" 
				+ "  FROM default_date "
				+ " WHERE name = $$S/I-MUST-HAVE-S/O CUTOFF$$ "
				);
		CLOSED_DSR_BEFORE_SO_CUTOFF = (Date) new Data().getDatum("" 
				+ "SELECT CASE WHEN value IS NULL THEN 'epoch' ELSE value END\n" 
				+ "  FROM default_date "
				+ " WHERE name = $$CLOSED-DSR-BEFORE-S/O CUTOFF$$ "
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
		
		MAIN_CASHIER = (Integer) new Data().getDatum(""
				+ "SELECT CAST (value AS int) " 
				+ "  FROM default_number "
		        + " WHERE name = $$MAIN CASHIER$$ "
				); 
		
		BRANCH_CASHIER = (Integer) new Data().getDatum(""
				+ "SELECT CAST (value AS int) " 
				+ "  FROM default_number "
		        + " WHERE name = $$BRANCH CASHIER$$ "
				); 
		
		MONETARY = (Integer) new Data().getDatum(""
				+ "SELECT id " 
				+ "  FROM item_type "
		        + " WHERE name = $$MONETARY$$ "
				); 
		
		SALARY_DEDUCTION = (Integer) new Data().getDatum(""
				+ "SELECT id " 
				+ "  FROM item_master "
		        + " WHERE name = $$SALARY DEDUCTION$$ "
				); 
		
		SALARY_CREDIT = (Integer) new Data().getDatum(""
				+ "SELECT id " 
				+ "  FROM item_master "
		        + " WHERE name = $$SALARY CREDIT$$ "
				); 
		
		EWT = (Integer) new Data().getDatum(""
				+ "SELECT id " 
				+ "  FROM item_master "
		        + " WHERE name = $$EXPANDED WITHHOLDING TAX$$ "
				); 
		
		LISTING_FEE = (Integer) new Data().getDatum(""
				+ "SELECT id " 
				+ "  FROM item_master "
		        + " WHERE name = $$LISTING FEE$$ "
				); 
		
		DISPLAY_ALLOWANCE = (Integer) new Data().getDatum(""
				+ "SELECT id " 
				+ "  FROM item_master "
		        + " WHERE name = $$DISPLAY ALLOWANCE$$ "
				); 
		
		DEALERS_INCENTIVE = (Integer) new Data().getDatum(""
				+ "SELECT id " 
				+ "  FROM item_master "
		        + " WHERE name = $$DEALERS' INCENTIVE$$ "
				); 
		
		PRINCIPAL = (Integer) new Data().getDatum(""
				+ "SELECT CAST (value AS int) " 
				+ "  FROM default_number "
		        + " WHERE name = $$PRINCIPAL$$ "
				); 
		
		LEAD_TIME = (Integer) new Data().getDatum(""
				+ "SELECT CAST (value AS int) " 
				+ "  FROM default_number "
		        + " WHERE name = $$PURCHASE LEAD TIME$$ "
				); 
		
		SERVER_VERSION = (String) new Data().getDatum(""
				+ "SELECT latest " 
				+ "  FROM version "
				); 
		// @sql:off
		SERVER_TIMEZONE = (int) new Data().getDatum("SELECT CAST (EXTRACT(timezone_hour FROM current_time) AS int)");
		TODAY = new Date(DateUtils.truncate((Date) new Data().getDatum("SELECT current_date"), Calendar.DATE).getTime());
	}

	// VERSION
	public final static String CLIENT_VERSION = "0.9.6.7";

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

	public static Calendar cal = Calendar.getInstance();
	
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
	// OVERDUE_CUTOFF = 2013-03-01;
	// BALANCE_CUTOFF = 2013-06-27;
	// SI_WITH_SO_CUTOFF = 2013-06-30;
	// CLOSURE_BEFORE_SO_CUTOFF = 2013-08-13;

	// HELPER METHODS
	public static boolean isNegative(BigDecimal bigDecimal) {
		return bigDecimal.compareTo(BigDecimal.ZERO) < 0;
	}	
	
	public static BigDecimal getQuotient(BigDecimal dividend, BigDecimal divisor) {
		return dividend.divide(divisor, 4, RoundingMode.HALF_EVEN);
	}	
	
	public static BigDecimal getRate(BigDecimal percent) {
		return DIS.getQuotient(percent, DIS.HUNDRED);
	}	
		
	public static Date addDays(Date date, int amount) {
		return new Date(DateUtils.addDays(date, amount).getTime());
	}

	public static Date addMonths(Date date, int amount) {
		return new Date(DateUtils.addMonths(date, amount).getTime());
	}

	public static Date addYears(Date date, int amount) {
		return new Date(DateUtils.addYears(date, amount).getTime());
	}

	public static Time getServerTime() {
		return (Time) new Data().getDatum("SELECT current_time"); 
	}

	public static int getDayOfTheWeek(Date date) {
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	public static Date getFirstOfTheMonth(Date date) {
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return new Date(cal.getTimeInMillis());
	}

	public static Date getLastOfTheMonth(Date date) {
		cal.setTime(date);
		int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		cal.set(Calendar.DAY_OF_MONTH, lastDay);
		return new Date(cal.getTimeInMillis());
	}

	public static boolean isToday(Date date) {
		return DateUtils.isSameDay(date, DIS.TODAY) ? true : false;
	}

	public static boolean isSunday(Date date) {
		return getDayOfTheWeek(date) == Calendar.SUNDAY ? true : false;
	}

	public static boolean isMonday(Date date) {
		return getDayOfTheWeek(date) == Calendar.MONDAY ? true : false;
	}

	public static Date parseDate(String text) {
		try {
			return new Date(DateUtils.truncate(POSTGRES_DATE.parse(text), Calendar.DATE).getTime());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Time parseTime(String text) {
		try {
			return new Time(TIME.parse(text).getTime());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static BigDecimal parseBigDecimal(String text) {
		text = text.trim();
		if (text.equals("-") || text.isEmpty())
			return null;
		text = text.replace(",", "").replace("(", "-").replace(")", "");
		return new BigDecimal(text);
	}
	
	public static String formatTo2Places(BigDecimal number) {
		return TWO_PLACE_DECIMAL.format(number);
	}
	
	@SuppressWarnings("unchecked")
    public static <T> T createObject(String className, Object[] parameters, Class<?>[] parameterTypes) {
		try {
			Class<?> cls = Class.forName(className);
			Constructor<?> constructor = cls.getConstructor(parameterTypes);
			return (T) constructor.newInstance(parameters);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException
		        | SecurityException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		    return null; 
		}
    }
}
