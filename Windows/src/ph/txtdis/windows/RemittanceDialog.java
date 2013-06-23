package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;

public class RemittanceDialog extends DialogView {
	private Button invoice, delivery;

	public RemittanceDialog() {
		super("New", "");
	}

	@Override
	protected void setRightPane() {
		super.setRightPane();
		invoice = new Button(header, SWT.RADIO);
		invoice.setText("S/I");
		delivery = new Button(header, SWT.RADIO);
		delivery.setText("D/R");
		invoice.setSelection(true);
	}

	@Override
	protected void setOkButtonAction() {
		image.dispose();
		if (invoice.getSelection()) {
			shell.close();
			new InvoiceView(0);
		} else {
			shell.close();
			new DeliveryView(0);
		}
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene", "ayin");
		new RemittanceDialog().open();
		Database.getInstance().closeConnection();
	}

}
