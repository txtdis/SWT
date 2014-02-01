package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class ItemExpiryInput {

	public ItemExpiryInput(final OrderView view, final Expirable data, Date date) {
		final int QUALITY_COLUMN = 5;
		final int QUANTITY_COLUMN = 6;
		final TableItem tableItem = view.getTableItem();
		final Text expiryInput = new TableTextInput(tableItem, QUALITY_COLUMN, date).getText();
		expiryInput.setFocus();
		
		new DataInputter(expiryInput, null) {
			private Date date;

			@Override
			protected Boolean isNonBlank() {
				date = DIS.parseDate(textInput);
				if (date.after(DIS.getDatePerQuality(data.getQuality()))) {
					new ErrorDialog("Good/on-hold items\ncannot be expired");
					return false;
				} else {
					tableItem.setText(QUALITY_COLUMN, textInput);
					data.setExpiry(date);
					view.setQtyColumnIdx(QUANTITY_COLUMN);
					expiryInput.dispose();
					new ItemQtyInput(view, (OrderData) data);
					return true;
				}
			}
		};
	}
}
