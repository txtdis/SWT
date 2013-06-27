package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Shell;

public class CalendarDialog extends DialogView {
	private DateTime start, end;
	private Calendar calStart, calEnd;
	private Date[] dates;
	private int length;
	private boolean disposeAllShells;

	public CalendarDialog(Date[] dates) {
		this(dates, true);
	}

	public CalendarDialog(Date[] dates, boolean disposeAllShells) {
		super();
		this.dates = dates;
		this.disposeAllShells = disposeAllShells;
		length = dates.length;
		setName(length > 1 ? "Choose Start and End Dates" : "Choose Date");
		open();
	}

	@Override
	protected void setLeftPane() {
		start = new DateTime(header, SWT.CALENDAR | SWT.BORDER);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dates[0]);
		start.setDate(
				cal.get(Calendar.YEAR), 
				cal.get(Calendar.MONTH), 
				cal.get(Calendar.DAY_OF_MONTH));	
	}

	@Override
	protected void setRightPane() {
		if(length > 1) {
			end = new DateTime(header, SWT.CALENDAR | SWT.BORDER);
			Calendar cal = Calendar.getInstance();
			cal.setTime(dates[1]);
			end.setDate(
					cal.get(Calendar.YEAR), 
					cal.get(Calendar.MONTH), 
					cal.get(Calendar.DAY_OF_MONTH));
		}
	}

	@Override
	protected void setOkButtonAction() {
		calStart = Calendar.getInstance();
		calStart.set(start.getYear(), start.getMonth(), start.getDay());
		if (length > 1) {
			calEnd = Calendar.getInstance();
			calEnd.set(end.getYear(), end.getMonth(), end.getDay());
			if (calEnd.before(calStart)) {
				new ErrorDialog("Must not be after" + "\n" +
						"" + DIS.DF.format(new Date(calStart.getTimeInMillis())));
				dates = null;
			} else {
				dates[1] = new Date(calEnd.getTimeInMillis());
			}
		} 
		dates[0] = new Date(calStart.getTimeInMillis());
		if(disposeAllShells) {
			for (Shell shell: display.getShells()) {
				shell.dispose();
			}
		} else {
			shell.dispose();
		}
	}

	public Date[] getDates() {
		return dates;
	}

	public Date getDate() {
		return dates == null ? null : dates[0];
	}
}
