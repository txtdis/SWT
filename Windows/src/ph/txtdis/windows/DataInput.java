package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class DataInput {
	private Control next;
	private Text text;
	private String defaultDatum;
	protected String string;

	public DataInput(Text txt, Control ctrl) {
		this(txt, ctrl, "");
	}

	public DataInput(Text txt, Control ctrl, String str) {
		text = txt;
		next = ctrl;
		defaultDatum = str;
		if (text == null || text.isDisposed())
			return;
		text.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				string = text.getText().trim();
				if (isInputValid()) {
					if (next == null || next.isDisposed())
						return;
					if (!text.isDisposed()) {
						text.setEnabled(false);
						text.setBackground(DIS.WHITE);
					}
					if (next.getClass().equals(Text.class)) {
						((Text) next).setText(defaultDatum);
						((Text) next).setEditable(true);
						next.setTouchEnabled(true);
					} else {
						next.setEnabled(true);
					}
					next.setFocus();
				} else if (!text.isDisposed()) {
					text.setText(defaultDatum);
					text.setEditable(true);
					text.setBackground(DIS.YELLOW);
					text.setFocus();
				}
			}
		});
	}

	protected boolean isInputValid() {
		if (string.isEmpty()) {
			return isBlankInputNotValid();
		} else {
			return isDataInputValid();
		}
	}

	protected void setNext(Control next) {
		this.next = next;
	}

	protected boolean isBlankInputNotValid() {
		return false;
	}

	protected boolean isDataInputValid() {
		return true;
	}
}
