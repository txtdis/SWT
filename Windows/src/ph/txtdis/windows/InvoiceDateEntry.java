package ph.txtdis.windows;

import java.sql.Date;

import org.apache.commons.lang3.time.DateUtils;

public class InvoiceDateEntry extends OrderDateEntry {

	public InvoiceDateEntry(OrderView orderView, OrderData orderData) {
		super(orderView, orderData);
	}

	@Override
	protected boolean isDateInputValid(OrderData data, Date date) {
		int id = data.getId();
		int lastId = id - 1;
		String series = ((InvoiceData) data).getSeries();
		Date lastOrderDate = OrderControl.getDate(lastId);
		Date referenceDate = OrderControl.getReferenceDate(((DeliveryData) data).getReferenceId());
		if (DIS.isNegative(((DeliveryData) data).getEnteredTotal()))
			referenceDate = date;
		if (OrderControl.isIdStartOfBooklet(id, series)) {
			lastOrderDate = date;
		} else if (lastOrderDate == null) {
			new ErrorDialog("Invoice #" + lastId + "\nmust be entered first");
			return false;
		} else if (lastOrderDate.after(date)) {
			new ErrorDialog("Invoice date must on or after\npreceding S/I #" + lastId + " dated " + lastOrderDate);
			return false;
		} else if (date.after(DIS.SI_MUST_HAVE_SO_CUTOFF) && ((DeliveryData) data).getReferenceId() == 0
		        && !DIS.isNegative(((DeliveryData) data).getEnteredTotal())) {

			new ErrorDialog("S/O(P/O) # cannot be blank");
			new InvoiceView();
			return true;
		} else if (!DateUtils.isSameDay(date, referenceDate)) {
			new ErrorDialog("Invoice and S/O(P/O) dates\nmust be the same");
			return false;
		}
		return true;
	}

}
