package ph.txtdis.windows;

import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class DataInputter {
	
	protected BigDecimal number;
	protected String textInput;

	private Control next;
	private Text text;

	public DataInputter(Text input, Control nextControl) {
		text = input;
		next = nextControl;
		if(text == null)
			return;
		text.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				textInput = text.getText().trim();
				UI.goToControl((isInputValid()) ? next : text);
			}
		});
	}

	protected void setNext(Control next) {
		this.next = next;
	}

	private boolean isInputValid() {
		Boolean isValid = textInput.isEmpty() ? isBlankNot() : isNonBlank();
		return isValid != null ? isValid : isAnyInput();
	}

	protected boolean isAnyInput() {
		return true;
	}

	protected Boolean isBlankNot() {
		return false;
	}

	protected Boolean isNonBlank() {
		number = DIS.parseBigDecimal(textInput);
		return isNumberValid();
	}

	private boolean isNumberValid() {
		Boolean isValid = DIS.isZero(number) ? isZeroNot() : isNonZero();
		return isValid != null ? isValid : isAnyNumber();
	}

	protected boolean isAnyNumber() {
	    return true;
    }

	protected Boolean isZeroNot() {
		return false;
	}

	private boolean isNonZero() {
		Boolean isValid = DIS.isNegative(number) ? isNegativeNot() : isPositive();
		return isValid != null ? isValid : isAnyNonZero();
	}

	protected boolean isAnyNonZero() {
	    return true;
    }

	protected Boolean isNegativeNot() {
		return false;
	}

	protected Boolean isPositive() {
		return true;
	}
}
