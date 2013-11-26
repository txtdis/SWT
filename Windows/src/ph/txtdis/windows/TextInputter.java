package ph.txtdis.windows;

import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class TextInputter {
	private Control next;
	private Text text;
	protected OrderView view;
	protected BigDecimal numericInput;
	protected String textInput;
	protected boolean shouldReturn;

	public TextInputter(Text thisText, Control nextControl) {
		this(thisText, nextControl, null);
	}

	public TextInputter(Text thisText, Control nextControl, OrderView orderView) {
		text = thisText;
		next = nextControl;
		view = orderView;
		if (text == null || text.isDisposed())
			return;			
		text.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				textInput = text.getText().trim();
				if (isInputValid()) {
					if (next == null || next.isDisposed())
						return;
					if (next.getClass().equals(Text.class)) {
						((Text) next).setEditable(true);
						((Text) next).selectAll();
						next.setBackground(DIS.YELLOW);
					} else
						next.setEnabled(true);
					next.setFocus();
				} else if (!text.isDisposed())
					text.setFocus();
			}
		});
	}

	protected boolean isInputValid() {
		shouldReturn = false;
		if (textInput.isEmpty()) {
			if (isABlankInputNotValid())
				return false;
			if (shouldReturn)
				return true;
		}
		return isTheDataInputValid();
	}

	protected void setNext(Control next) {
		this.next = next;
	}

	protected boolean isABlankInputNotValid() {
		shouldReturn = true;
		return true;
	}

	protected boolean isTheDataInputValid() {
		return isTheNumericInputValid();
	}

	protected boolean isTheNumericInputValid() {
		if (textInput.equals("-"))
			return false;
		numericInput = new BigDecimal(textInput.replace("(", "-").replace(")", ""));
		int sign = numericInput.signum();
		if (sign == 0) {
			if (isZeroNotValid())
				return false;
			if (shouldReturn)
				return true;
		}
		if (sign == -1) {
			if (isTheNegativeNumberNotValid())
				return false;
			if (shouldReturn)
				return true;
		}
		if (sign == 1) {
			if (!isThePositiveNumberValid())
				return false;
			if (shouldReturn)
				return true;
		}
		return (isTheSignedNumberValid());
	}

	protected boolean isTheSignedNumberValid() {
		return true;
	}

	protected boolean isTheNegativeNumberNotValid() {
		shouldReturn = true;
		return true;
	}

	protected boolean isZeroNotValid() {
		shouldReturn = true;
		return true;
	}

	protected boolean isThePositiveNumberValid() {
		return true;
	}
}
