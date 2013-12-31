package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class DateInputter extends TextInputter {
	protected Date date;

	public DateInputter(Text thisDatetext, Control nextControl) {
		super(thisDatetext, nextControl);
	}

	@Override
    protected boolean isInputValid() {
	    date = DIS.parseDate(textInput);
	    return isTheDataInputValid();
    }
}
