package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.Arrays;

public class RemittanceHelper {

	public int getRemitId(int bankId, Date date, Time time, int refId) {
		Object object = new SQL().getDatum(new Object[] {bankId, date, time, refId}, "" + 
				"SELECT remit_id  " +
				"FROM 	remittance_header " +
				"WHERE 	bank_id = ? " +
				"	AND	remit_date = ? " +
				"	AND	remit_time = ? " +
				"	AND	ref_id = ? " +
				"");
		return (object == null ? 0 : (int) object);
	}

	public boolean isIdOnFile(int remitId) {
		Object object = new SQL().getDatum(remitId, "" + 
				"SELECT remit_id  " +
				"FROM 	remittance_header " +
				"WHERE 	remit_id = ? " 
				);
		return (object == null ? false : true);
	}

	public BigDecimal getPayment(String series, int orderId) {
		Object object = new SQL().getDatum(new Object[]{series, orderId}, "" + 
				"SELECT payment " +
				"FROM 	payment " +
				"WHERE 	series = ? " +
				"AND 	order_id = ? " +
				"");
		return object == null ? BigDecimal.ZERO : (BigDecimal) object;
	}

	public boolean wasPaidByCheck(int remitId) {
		Object object = new SQL().getDatum(remitId, "" + 
				"SELECT remit_id\n" +
				"FROM 	remittance_header\n" +
				"WHERE 	remit_id = ?\n" +
				"AND	remit_time = '00:00:00';" 
				);
		return (object == null ? false : true);		
	}

	public Integer[] getRemitIds(int orderId) {
		Object[] objects = new SQL().getData(orderId, "" + 
				"SELECT remit_id " +
				"FROM	remittance_detail " +
				"WHERE 	order_id = ? " +
				"");
		return Arrays.copyOf(objects, objects.length, Integer[].class);
	}
}
