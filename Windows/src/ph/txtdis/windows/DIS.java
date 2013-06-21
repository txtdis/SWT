package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.time.DateUtils;

public class DIS {
	
	//REPORT OPTIONS
	public final static int ROUTE = 0;
	public final static int STT = 0;

	// ACCESS
	public final static long ITEM = 		0b10000_00000_00000_00000_00000;
	public final static long PARTNER = 		0b01000_00000_00000_00000_00000;
	public final static long TRANSACTION = 	0b00100_00000_00000_00000_00000;
	public final static long DISCREPANCY = 	0b00010_00000_00000_00000_00000;
	public final static long PURCHASE = 	0b00001_00000_00000_00000_00000;
	
	public final static long RECEIPT =		0b00000_10000_00000_00000_00000;
	public final static long INVENTORY =	0b00000_01000_00000_00000_00000;
	public final static long SHIPMENT =	 	0b00000_00100_00000_00000_00000;
	public final static long STOCK_TAKE = 	0b00000_00010_00000_00000_00000;
	public final static long PRICE = 		0b00000_00001_00000_00000_00000;
	
	public final static long REPORT = 		0b00000_00000_10000_00000_00000;
	public final static long CREDIT = 		0b00000_00000_01000_00000_00000;
	public final static long SO =			0b00000_00000_00100_00000_00000;
	public final static long INVOICE =		0b00000_00000_00010_00000_00000;
	public final static long REMITTANCE = 	0b00000_00000_00001_00000_00000;
	
	public final static long AR = 			0b00000_00000_00000_10000_00000;
	public final static long AP = 			0b00000_00000_00000_01000_00000;
	public final static long CMDM = 		0b00000_00000_00000_00100_00000;
	public final static long TAX = 			0b00000_00000_00000_00010_00000;
	public final static long FS = 			0b00000_00000_00000_00001_00000;
	
	public final static long BACKUP = 		0b00000_00000_00000_00000_10000;
	public final static long RESTORE = 		0b00000_00000_00000_00000_01000;
	public final static long SETTINGS = 	0b00000_00000_00000_00000_00100;
	public final static long SMSLOG = 		0b00000_00000_00000_00000_00010;
	public final static long AUDIT = 		0b00000_00000_00000_00000_00001;
	
	public final static long TIMES = 		0b10000_00000_00000_00000_00000_0;

	// NUMBER FORMAT
	public static final DecimalFormat SNF = new DecimalFormat("0.00");
	public static final DecimalFormat LNF = new DecimalFormat("#,##0.00");
	public static final DecimalFormat XNF = new DecimalFormat("#,##0.0000");
	
	public static final DecimalFormat LIF = new DecimalFormat("#,##0");
	public static final DecimalFormat BIF = new DecimalFormat("0;(0)");
	public static final DecimalFormat SIF = new DecimalFormat("0");
	public static final SimpleDateFormat SDF = new SimpleDateFormat("MM/dd/yy");
	public static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat LDF = new SimpleDateFormat("MMM dd, yyyy");
	public static final SimpleDateFormat TF = new SimpleDateFormat("HH:mm");
	
	// CONSTANTS
	public static final BigDecimal VAT = (BigDecimal) new SQL().getDatum("" +
			"SELECT value " +
			"FROM 	default_number " +
			"WHERE 	name = 'VAT' "
			);
	public static final Date TODAY = new Date(
			DateUtils.truncate(Calendar.getInstance(), Calendar.DAY_OF_MONTH)
			.getTimeInMillis());
			
	// DATE INPUT OPTION
	public final static int DATEFROM = 1;
	public final static int DATEFROMTO = 2;
	public final static int DATETO = 3;
	
	// HELPER METHODS
	public static Date parseDate(String strDate) {
		try {
			return new Date (DF.parse(strDate).getTime());
		} catch (ParseException e) {
			new ErrorDialog(e);
			return null;
		}
	}
}
