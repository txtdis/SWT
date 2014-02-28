
package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

public class SalesView extends OrderView {
	private Button printerButton;
	private Text salesLimitDisplay;

	public SalesView() {
		this(0);
    }

	public SalesView(int id) {
		this(new SalesData(id));
	}

	public SalesView(SalesData data) {
		super(data);
		type = Type.SALES;
		display();
	}

	@Override
	protected void addHeader() {
		new Header(this, data) {
			@Override
			protected void layButtons() {
				boolean wasPrinted = new SalesOrderPrintOut(id).wasPrinted();
				Date salesDate = data.getDate();
				if (User.isSales())
					new ImgButton(buttons, Type.NEW, type);
				new ImgButton(buttons, Type.OPEN, view);
				if (((InputData) data).getId() == 0)
					postButton = new ImgButton(buttons, Type.SAVE, view).getButton();
				if (!wasPrinted && !salesDate.before(DIS.TODAY))
					printerButton = new PrintingButton(buttons, data, false).getButton();
			}
		};
	}
	
	@Override
    protected void addSubheader() {
		new Subheader(this, (SalesData) data) {
			@Override
            protected void setOrderGroup(OrderView view, OrderData data, Group group) {
				referenceIdInput = new TextDisplayBox(group, "S/O #", data.getId()).getText();
				salesLimitDisplay = new TextDisplayBox(group, "LIMIT", ((SalesData) data).getSalesLimit()).getText();
            }
		};
    }

	@Override
	protected void setFocus() {
		type = Type.SALES;
		if (id == 0) {
			listButton.setEnabled(true);
			partnerIdInput.setTouchEnabled(true);
			partnerIdInput.setFocus();
		} else if (printerButton != null && !new SalesOrderPrintOut(id).wasPrinted()) {
			printerButton.setEnabled(true);
			printerButton.setFocus();
		}
	}

	public Button getPrinterButton() {
		return printerButton;
	}

	public void setPrinterButton(Button printerButton) {
		this.printerButton = printerButton;
	}

	public Text getSalesLimitDisplay() {
		return salesLimitDisplay;
	}

	@Override
    public Posting getPosting() {
	    return new OrderPosting((OrderData) data);
    }
}
