package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

public class OrderDateEntry {
	private Button postButton;
	private OrderData data;
	private OrderView view;
	private Text dueDateDisplay;

	protected Text dateInput;

	public OrderDateEntry(OrderView orderView, OrderData orderData) {
		data = orderData;
		view = orderView;
		dateInput = view.getDateInput();
		dueDateDisplay = view.getDueDisplay();
		postButton = view.getPostButton();

		new DataInputter(dateInput, null) {
			private Date date;
			
			@Override
			protected Boolean isNonBlank() {
				date = DIS.parseDate(textInput);
				if (!isDateInputValid(data, date))
					return false;
				Date reconciledDate = Count.getLatestReconciledDate();
				if (!date.after(reconciledDate)) {
					new ErrorDialog("Only transactions after \nthe latest inventory reconciliation\n-- "
					        + reconciledDate + "--\ncan be entered.");
					return false;
				}
				if (!new CalendarDialog(new Date[] { date }).isEqual())
					return false;

				data.setDate(date);
				dueDateDisplay.setText((DIS.addDays(date, Credit.getTerm(data.getPartnerId(), date)).toString()));
				dateInput.setTouchEnabled(false);
				if (postButton != null)
					new ItemIdInputSwitcher(view, data);
				return true;
			}

		};

	}

	protected boolean isDateInputValid(final OrderData data, Date date) {
		return true;
	}
}
