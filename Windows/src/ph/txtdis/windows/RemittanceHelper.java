package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;

public class RemittanceHelper {

	public RemittanceHelper() {
	}
	
	public int getRemitId(int bankId, Date date, Time time, int refId) {
		Object o = new SQL().getDatum(new Object[] {bankId, date, time, refId}, "" + 
				"SELECT remit_id  " +
				"FROM 	remittance_header " +
				"WHERE 	bank_id = ? " +
				"	AND	remit_date = ? " +
				"	AND	remit_time = ? " +
				"	AND	ref_id = ? " +
				"");
	return (o == null ? 0 : (int) o);
	}
	
	public boolean hasId(int remitId) {
		Object o = new SQL().getDatum(remitId, "" + 
				"SELECT remit_id  " +
				"FROM 	remittance_header " +
				"WHERE 	remit_id = ? " 
				);
		return (o == null ? false : true);
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
}
