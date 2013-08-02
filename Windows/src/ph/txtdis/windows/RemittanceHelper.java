package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.Arrays;

public class RemittanceHelper {
	private Data sql;

	public RemittanceHelper() {
		sql = new Data();
	}

	public int getRemitId(int bankId, Date date, Time time, int refId) {
		Object[] parameters = new Object[] { bankId, date, time, refId };
		Object object = sql.getDatum(parameters, ""
				+ "SELECT remit_id FROM remittance_header "
				+ "WHERE bank_id = ? AND remit_date = ? "
				+ "	AND	remit_time = ? AND ref_id = ?;");
		return (object == null ? 0 : (int) object);
	}

	public boolean isRemitIdOnFile(int remitId) {
		Object object = sql.getDatum(remitId, ""
				+ "SELECT remit_id FROM remittance_header "
				+ "WHERE remit_id = ?;");
		return (object == null ? false : true);
	}

	public BigDecimal getPayment(String series, int orderId) {
		Object object = sql.getDatum(new Object[] { series, orderId }, ""
				+ "SELECT payment FROM payment "
				+ "WHERE series = ? AND order_id = ?;");
		return object == null ? BigDecimal.ZERO : (BigDecimal) object;
	}

	public boolean isPaymentByCheck(int remitId) {
		Object object = sql.getDatum(remitId, ""
				+ "SELECT remit_id FROM remittance_header "
				+ "WHERE remit_id = ? AND remit_time = '00:00:00';");
		return (object == null ? false : true);
	}

	public Integer[] getRemitIds(int orderId) {
		Object[] objects = sql.getData(orderId, ""
				+ "SELECT remit_id FROM remittance_detail "
				+ "WHERE order_id = ?;");
		return Arrays.copyOf(objects, objects.length, Integer[].class);
	}

	public BigDecimal getCashPaymentVersusRemittanceVariance(
			Date[] beginAndEndDates, int routeId) {
		Object[] parameters = new Object[] { beginAndEndDates[0],
				beginAndEndDates[1], routeId };
		Object object = sql.getDatum(parameters, ""
				+ "SELECT payment FROM payment "
				+ "WHERE series = ? AND order_id = ?;");
		return object == null ? BigDecimal.ZERO : (BigDecimal) object;
	}
}