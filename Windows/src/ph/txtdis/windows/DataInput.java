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
		if(text == null) return;
		text.addListener (SWT.DefaultSelection, new Listener () {
			@Override
			public void handleEvent (Event e) {
				string = text.getText().trim();
				if(act()) {
					if(next == null || next.isDisposed())
						return;						
					if (!text.isDisposed()) {
						text.setEnabled(false);
						text.setBackground(View.white());
					}
					if(next.getClass().equals(Text.class)) {
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
					text.setBackground(View.yellow());
					text.setFocus();
				}
			}
		});
	}

	protected boolean act() {
		if(string.isEmpty()) {
			return ifBlank();
		} else {
			return ifHasText();
		}
	}
		
	protected void setNext(Control next) {
		this.next = next;
	}
	
	protected boolean ifBlank() {
		return false;
	}

	protected boolean ifHasText() {
		return true;
	}
}
